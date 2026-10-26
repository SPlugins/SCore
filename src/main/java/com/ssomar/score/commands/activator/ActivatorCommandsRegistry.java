package com.ssomar.score.commands.activator;

import com.ssomar.score.SCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registers on the server the commands of the activators that listen to a written command
 * (PLAYER_WRITE_COMMAND with {@code registerCommand: true}), so the server and the client know them:
 * no "Unknown command", no red command in the chat bar, tab completion.
 * <p>
 * Each plugin (ExecutableItems, ExecutableEvents...) gives the detailedCommands of its activators that have the
 * option; the registry keeps one command per label for all the plugins, registers the new ones, unregisters
 * the ones nobody asks for anymore (reload, item or activator deleted) and never replaces a command that it did
 * not register (a command of Minecraft or of another plugin): it logs it and does nothing for this label.
 * <p>
 * The command does nothing by itself: the activators keep running from the command event, which the server fires
 * before dispatching the command, so an activator never runs twice.
 */
public class ActivatorCommandsRegistry {

    private static final Logger LOGGER = Logger.getLogger("SCore");
    private static ActivatorCommandsRegistry instance;

    /* owner (plugin name) -> label -> argument chains */
    private final Map<String, Map<String, List<List<String>>>> wantedByOwner = new LinkedHashMap<>();
    /* the commands this registry registered, by label */
    private final Map<String, ActivatorCommand> registered = new HashMap<>();
    private final Map<String, String> prefixByLabel = new HashMap<>();
    /* the labels already reported as taken, so the warning is not repeated at each editor save */
    private final Set<String> conflictWarned = new HashSet<>();

    private final Map<String, Supplier<Collection<String>>> pending = new LinkedHashMap<>();
    private boolean syncScheduled = false;
    private boolean commandMapWarned = false;

    public static synchronized ActivatorCommandsRegistry getInstance() {
        if (instance == null) instance = new ActivatorCommandsRegistry();
        return instance;
    }

    /**
     * Asks for a sync of the commands of a plugin at the next tick; several requests before it are merged, and the
     * supplier is read then, so the objects loaded or edited in between are taken into account.
     *
     * @param owner            the plugin name (ExecutableItems, ExecutableEvents...)
     * @param detailedCommands gives the detailedCommands of all the activators of this plugin with registerCommand: true
     */
    public synchronized void requestSync(String owner, Supplier<Collection<String>> detailedCommands) {
        pending.put(owner, detailedCommands);
        if (syncScheduled) return;
        if (SCore.schedulerHook == null) {
            /* no scheduler (tests, SCore not enabled yet): nothing can be registered now */
            return;
        }
        syncScheduled = true;
        try {
            SCore.schedulerHook.runTask(this::runPending, 1);
        } catch (Throwable t) {
            /* the plugin is disabling: no task can be scheduled anymore */
            syncScheduled = false;
        }
    }

    private void runPending() {
        Map<String, Supplier<Collection<String>>> toRun;
        synchronized (this) {
            toRun = new LinkedHashMap<>(pending);
            pending.clear();
            syncScheduled = false;
        }
        Map<String, Collection<String>> computed = new LinkedHashMap<>();
        for (Map.Entry<String, Supplier<Collection<String>>> entry : toRun.entrySet()) {
            try {
                computed.put(entry.getKey(), entry.getValue().get());
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "[SCore] Could not read the registerCommand activators of " + entry.getKey(), t);
            }
        }
        if (computed.isEmpty()) return;
        synchronized (this) {
            for (Map.Entry<String, Collection<String>> entry : computed.entrySet()) {
                wantedByOwner.put(entry.getKey(), parse(entry.getValue()));
            }
        }
        applyOnServer();
    }

    /** Unregisters at once all the commands only this plugin asked for (plugin disabled). */
    public void unregisterAll(String owner) {
        synchronized (this) {
            pending.remove(owner);
            if (wantedByOwner.remove(owner) == null) return;
        }
        applyOnServer(false);
    }

    private void applyOnServer() {
        applyOnServer(true);
    }

    private void applyOnServer(boolean refreshPlayers) {
        CommandMap commandMap = getServerCommandMap();
        Map<String, Command> known = commandMap == null ? null : getKnownCommands(commandMap);
        if (commandMap == null || known == null) {
            synchronized (this) {
                if (!commandMapWarned && hasWantedCommands()) {
                    commandMapWarned = true;
                    LOGGER.warning("registerCommand: the command map of the server is not reachable, the commands of the activators are not registered.");
                }
            }
            return;
        }
        boolean changed = apply(commandMap, known);
        if (changed && refreshPlayers) refreshPlayers(known);
    }

    private synchronized boolean hasWantedCommands() {
        for (Map<String, List<List<String>>> map : wantedByOwner.values()) if (!map.isEmpty()) return true;
        return false;
    }

    /**
     * Applies the wanted commands on a command map.
     *
     * @return true when a command was registered or unregistered (the players' command tree must be sent again)
     */
    synchronized boolean apply(CommandMap commandMap, Map<String, Command> known) {
        Map<String, List<List<String>>> union = new LinkedHashMap<>();
        Map<String, String> ownerOfLabel = new HashMap<>();
        for (Map.Entry<String, Map<String, List<List<String>>>> ownerEntry : wantedByOwner.entrySet()) {
            for (Map.Entry<String, List<List<String>>> entry : ownerEntry.getValue().entrySet()) {
                List<List<String>> chains = union.get(entry.getKey());
                if (chains == null) {
                    chains = new ArrayList<>();
                    union.put(entry.getKey(), chains);
                    ownerOfLabel.put(entry.getKey(), ownerEntry.getKey());
                }
                for (List<String> chain : entry.getValue()) if (!chains.contains(chain)) chains.add(chain);
            }
        }

        boolean changed = false;

        for (String label : new ArrayList<>(registered.keySet())) {
            if (union.containsKey(label)) continue;
            unregister(commandMap, known, label, registered.remove(label));
            prefixByLabel.remove(label);
            changed = true;
        }

        for (Map.Entry<String, List<List<String>>> entry : union.entrySet()) {
            String label = entry.getKey();
            ActivatorCommand ours = registered.get(label);
            if (ours != null) {
                /* the completions are asked to the server at each tab, no need to send the command tree again */
                ours.setArgumentChains(entry.getValue());
                continue;
            }
            Command existing = known.get(label);
            if (existing != null) {
                if (conflictWarned.add(label)) {
                    LOGGER.warning("registerCommand: the command /" + label + " already exists on the server ("
                            + describe(existing) + "), it is not replaced. The activators still run when it is written.");
                }
                continue;
            }
            String prefix = sanitizePrefix(ownerOfLabel.get(label));
            ActivatorCommand command = new ActivatorCommand(label, entry.getValue());
            command.setDescription("Command of an activator (registerCommand)");
            try {
                commandMap.register(prefix, command);
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "[SCore] registerCommand: could not register the command /" + label, t);
                continue;
            }
            if (known.get(label) != command) {
                /* the label was taken in between: keep nothing of ours */
                unregister(commandMap, known, label, command);
                if (conflictWarned.add(label))
                    LOGGER.warning("registerCommand: the command /" + label + " already exists on the server, it is not replaced.");
                continue;
            }
            registered.put(label, command);
            prefixByLabel.put(label, prefix);
            conflictWarned.remove(label);
            changed = true;
        }

        conflictWarned.retainAll(union.keySet());
        return changed;
    }

    private void unregister(CommandMap commandMap, Map<String, Command> known, String label, ActivatorCommand command) {
        if (command == null) return;
        try {
            command.unregister(commandMap);
        } catch (Throwable ignored) {
        }
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, Command> entry : new ArrayList<>(known.entrySet())) {
            if (entry.getValue() == command) keys.add(entry.getKey());
        }
        for (String key : keys) {
            try {
                known.remove(key);
            } catch (Throwable t) {
                LOGGER.log(Level.WARNING, "[SCore] registerCommand: could not unregister the command /" + key, t);
            }
        }
    }

    private static String describe(Command command) {
        String result = command.getClass().getSimpleName();
        try {
            if (command instanceof org.bukkit.command.PluginIdentifiableCommand)
                result = "plugin " + ((org.bukkit.command.PluginIdentifiableCommand) command).getPlugin().getName();
        } catch (Throwable ignored) {
        }
        return result;
    }

    static String sanitizePrefix(String owner) {
        if (owner == null) return "score";
        String prefix = owner.toLowerCase(Locale.ENGLISH).replaceAll("[^a-z0-9_.-]", "");
        return prefix.isEmpty() ? "score" : prefix;
    }

    /**
     * Reads detailedCommands entries ({@code /promo}, {@code promo summer}...) into label -> argument chains.
     * The label is the first word without the slash, in lower case; the namespaced labels (minecraft:give) are
     * skipped, they are always commands that exist.
     */
    public static Map<String, List<List<String>>> parse(Collection<String> detailedCommands) {
        Map<String, List<List<String>>> result = new LinkedHashMap<>();
        if (detailedCommands == null) return result;
        for (String raw : detailedCommands) {
            if (raw == null) continue;
            String s = raw.trim();
            while (s.startsWith("/")) s = s.substring(1).trim();
            if (s.isEmpty()) continue;
            String[] words = s.split("\\s+");
            String label = words[0].toLowerCase(Locale.ENGLISH);
            if (label.isEmpty() || label.contains(":")) continue;
            List<List<String>> chains = result.get(label);
            if (chains == null) {
                chains = new ArrayList<>();
                result.put(label, chains);
            }
            if (words.length > 1) {
                List<String> chain = new ArrayList<>(Arrays.asList(words).subList(1, words.length));
                if (!chains.contains(chain)) chains.add(chain);
            }
        }
        return result;
    }

    public synchronized Set<String> getRegisteredLabels() {
        return new HashSet<>(registered.keySet());
    }

    /* ---------------------------------------------------------------- server access, by version */

    static CommandMap getServerCommandMap() {
        try {
            Object server = Bukkit.getServer();
            if (server == null) return null;
            /* CraftServer#getCommandMap exists on every version (and Server#getCommandMap on Paper) */
            Method method = server.getClass().getMethod("getCommandMap");
            Object map = method.invoke(server);
            return map instanceof CommandMap ? (CommandMap) map : null;
        } catch (Throwable t) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    static Map<String, Command> getKnownCommands(CommandMap commandMap) {
        try {
            /* SimpleCommandMap#getKnownCommands: recent Spigot and Paper */
            Method method = commandMap.getClass().getMethod("getKnownCommands");
            Object map = method.invoke(commandMap);
            if (map instanceof Map) return (Map<String, Command>) map;
        } catch (Throwable ignored) {
        }
        Class<?> clazz = commandMap.getClass();
        while (clazz != null) {
            try {
                Field field = clazz.getDeclaredField("knownCommands");
                field.setAccessible(true);
                Object map = field.get(commandMap);
                if (map instanceof Map) return (Map<String, Command>) map;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
                continue;
            } catch (Throwable t) {
                return null;
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    /**
     * Sends the command tree again to the players (1.13+, the client colours the commands and completes them from
     * it). Spigot and Paper before 1.20.6 build the tree from a copy of the commands: it is rebuilt first
     * (CraftServer#syncCommands, which also sends it to the players). Paper 1.20.6+ (and Folia) read the
     * command map directly, the tree is only sent again.
     */
    private void refreshPlayers(Map<String, Command> known) {
        if (SCore.is1v12Less()) return;
        boolean brigadierBacked = known.getClass().getName().contains("BrigForwardingMap");
        if (!brigadierBacked) {
            try {
                Object server = Bukkit.getServer();
                Method sync = server.getClass().getMethod("syncCommands");
                sync.invoke(server);
                return;
            } catch (Throwable ignored) {
                /* no syncCommands: send the tree as it is */
            }
        }
        for (Player player : new ArrayList<>(Bukkit.getOnlinePlayers())) {
            Runnable update = () -> {
                try {
                    player.updateCommands();
                } catch (Throwable ignored) {
                }
            };
            try {
                SCore.schedulerHook.runEntityTask(update, null, player, 0);
            } catch (Throwable t) {
                update.run();
            }
        }
    }
}
