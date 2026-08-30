package com.ssomar.score.commands.runnable;


import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.block.BlockRunCommand;
import com.ssomar.score.commands.runnable.entity.EntityRunCommand;
import com.ssomar.score.commands.runnable.player.PlayerRunCommand;
import com.ssomar.score.data.BlockCommandsQuery;
import com.ssomar.score.data.Database;
import com.ssomar.score.data.EntityCommandsQuery;
import com.ssomar.score.data.PlayerCommandsQuery;
import com.ssomar.score.utils.logging.Utils;
import com.ssomar.score.utils.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
public class CommandsHandler implements Listener {

    private static CommandsHandler instance;

    /* All the structures below are touched from several threads (Folia region threads / global
     * scheduler, async activators): they must be thread-safe, a plain HashMap/ArrayList ends up
     * with null entries and NPEs ("rC" is null) or lost commands. */

    /* DelayedCommands by RunCommand UUID */
    private final Map<UUID, RunCommand> delayedCommandsByRcUuid;

    /* DelayedCommands by receiver UUID (the receiver can be null for server loops -> no ConcurrentHashMap, guarded by receiverLock) */
    private final Map<UUID, List<RunCommand>> delayedCommandsByReceiverUuid;
    private final Object receiverLock = new Object();

    /* DelayedCommands by block UUID */
    List<BlockRunCommand> delayedCommandsByBlockUuid;

    /* for "morph item" timing between delete item and regive item (2 ticks)  player */
    @Setter
    @Getter
    private Map<Player, Long> stopPickup;

    private Map<Player, List<Material>> stopPickupMaterial;

    /* Commands delayed saved that wait to be runned  PLAYER_UUID|PLAYERRUNCOMMAND -> Useful to avoid to call a query at each join*/
    private Map<UUID, List<PlayerRunCommand>> delayedCommandsSaved;

    public CommandsHandler() {
        delayedCommandsByRcUuid = new ConcurrentHashMap<>();
        delayedCommandsByReceiverUuid = Collections.synchronizedMap(new HashMap<>());
        delayedCommandsByBlockUuid = new CopyOnWriteArrayList<>();
        stopPickup = new ConcurrentHashMap<>();
        stopPickupMaterial = new ConcurrentHashMap<>();
        delayedCommandsSaved = new ConcurrentHashMap<>();

        // create new timer task
        /* Bukkit.getScheduler().runTaskTimer(SCore.plugin, () -> {
            // display in console all info of delayedCommandsByReceiverUuid
            for (Map.Entry<UUID, List<RunCommand>> entry : delayedCommandsByReceiverUuid.entrySet()) {
                for(RunCommand runCommand: entry.getValue()) {
                   System.out.println("delayedCommandsByReceiverUuid: " + entry.getKey() + " " + runCommand.getBrutCommand() + " DELAYYYYYYY<>" + runCommand.getDelay() + " " + runCommand.getRunTime());
                }
            }
        }, 0L, 40L);*/
    }

    public static CommandsHandler getInstance() {
        if (instance == null) instance = new CommandsHandler();
        return instance;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void PlayerJoinEvent(PlayerJoinEvent e) {

        //System.out.println("JOIN EVENT");
        if (!SCore.pluginHolder.isEnabled()) return;

        //System.out.println("JOIN EVENT 2");
        Player p = e.getPlayer();
        /* Atomic take: the commands are consumed here, no containsKey/get/remove sequence */
        List<PlayerRunCommand> saved = getInstance().getDelayedCommandsSaved().remove(p.getUniqueId());
        if (saved == null) return;
        for (PlayerRunCommand command : new ArrayList<>(saved)) {
            if (command == null) continue;
            command.run();
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7SCore will execute the delayed command saved for &a" + p.getName() + " &7: &6" + command.getBrutCommand() + " &7>> delay: &b" + command.getDelay());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void PlayerQuitEvent(PlayerQuitEvent e) {

        if (!SCore.pluginHolder.isEnabled()) return;

        Player p = e.getPlayer();

        List<PlayerRunCommand> commands = getInstance().getDelayedCommandsWithPlayerReceiver(p.getUniqueId());
        //System.out.println("QUIT LIST SIEZ: "+commands.size());
        List<PlayerRunCommand> commandsToSave = new ArrayList<>();
        for (PlayerRunCommand command : commands) {
            if (command == null) continue;
            if (!command.isRunOffline()) {
                //System.out.println("QUTI >> "+command.getBrutCommand());
                if (!command.isClearIfDisconnect()) commandsToSave.add(command);
            }
        }

        getInstance().getDelayedCommandsSaved()
                .computeIfAbsent(p.getUniqueId(), k -> new CopyOnWriteArrayList<>())
                .addAll(commandsToSave);

        for (PlayerRunCommand command : commandsToSave) {
            getInstance().removeDelayedCommand(command.getUuid(), p.getUniqueId());
        }
    }

    public void onEnable() {

        Map<UUID, List<PlayerRunCommand>> loaded = new ConcurrentHashMap<>();
        for (Map.Entry<UUID, List<PlayerRunCommand>> entry : PlayerCommandsQuery.loadSavedCommands(Database.getInstance().connect()).entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) continue;
            loaded.put(entry.getKey(), new CopyOnWriteArrayList<>(entry.getValue()));
        }
        getInstance().setDelayedCommandsSaved(loaded);
        int cpt = 0;
        for (Map.Entry<UUID, List<PlayerRunCommand>> entry : getInstance().getDelayedCommandsSaved().entrySet()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
            for (PlayerRunCommand command : entry.getValue()) {
                if (command == null) continue;
                Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7SCore loaded the delayed command for &a" + player.getName() + " &7: &6" + command.getBrutCommand() + " &7>> delay: &b" + command.getDelay());
                cpt++;
            }
        }
        Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7SCore loaded &6" + cpt + " &7delayed commands saved");

        /* Quite useless because at the start of the server the entities seems not loaded and the Bukkit.getentity return null */
        List<EntityRunCommand> commands = EntityCommandsQuery.selectEntityCommands(Database.getInstance().connect());
        for (EntityRunCommand eCommand : commands) {
            eCommand.run();
        }
        EntityCommandsQuery.deleteEntityCommands(Database.getInstance().connect(true));

        List<BlockRunCommand> commands2 = BlockCommandsQuery.selectAllCommands(Database.getInstance().connect());
        for (BlockRunCommand bCommand : commands2) {
            bCommand.run();
        }
        BlockCommandsQuery.deleteCommands(Database.getInstance().connect(true));
    }

    public void onDisable() {
        List<PlayerRunCommand> savedCommands = new ArrayList<>(getInstance().getDelayedPlayerCommands());

        for (List<PlayerRunCommand> saved : getInstance().getDelayedCommandsSaved().values()) {
            savedCommands.addAll(saved);
        }
        for (PlayerRunCommand command : savedCommands) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(command.getReceiverUUID());
            Utils.sendConsoleMsg(SCore.NAME_COLOR + " &7SCore saved the delayed command for &a" + player.getName() + " &7: &6" + command.getBrutCommand() + " &7>> delay: &b" + command.getDelay());
        }
        PlayerCommandsQuery.deleteCommands(Database.getInstance().connect());
        PlayerCommandsQuery.insertCommand(Database.getInstance().connect(), savedCommands, false);
        getInstance().getDelayedCommandsSaved().clear();

        EntityCommandsQuery.insertCommand(Database.getInstance().connect(), getDelayedEntityCommands());

        BlockCommandsQuery.insertCommand(Database.getInstance().connect(), this.delayedCommandsByBlockUuid);
        this.delayedCommandsByBlockUuid.clear();

        synchronized (receiverLock) {
            this.delayedCommandsByReceiverUuid.clear();
        }
    }

    public void addDelayedCommand(@NotNull RunCommand command) {
        delayedCommandsByRcUuid.put(command.getUuid(), command);
        if (command instanceof PlayerRunCommand) {
            addDelayedCommandByReceiver(((PlayerRunCommand) command).getReceiverUUID(), command);
        } else if (command instanceof EntityRunCommand) {
            addDelayedCommandByReceiver(((EntityRunCommand) command).getEntityUUID(), command);
        } else if (command instanceof BlockRunCommand) {
            this.delayedCommandsByBlockUuid.add((BlockRunCommand) command);
        }

    }

    private void addDelayedCommandByReceiver(@Nullable UUID receiverUUID, RunCommand command) {
        synchronized (receiverLock) {
            List<RunCommand> list = delayedCommandsByReceiverUuid.get(receiverUUID);
            if (list == null) {
                list = new CopyOnWriteArrayList<>();
                delayedCommandsByReceiverUuid.put(receiverUUID, list);
            }
            list.add(command);
        }
    }

    public void removeDelayedCommand(UUID uuid, @Nullable UUID receiverUUID) {
        removeDelayedCommand(uuid, receiverUUID, true);
    }

    public void removeDelayedCommand(UUID uuid, @Nullable UUID receiverUUID, boolean canceltask) {
        //SsomarDev.testMsg("removeDelayedCommand >> "+uuid, true);
        /* Atomic: removeDelayedCommand runs concurrently (PlayerQuitEvent on the player's region
         * thread vs insideDelayedCommand() on the global scheduler), a containsKey/get pair lets
         * the other thread remove the entry in between and get() returns null. */
        RunCommand removed = delayedCommandsByRcUuid.remove(uuid);
        if (removed != null && canceltask) {
            ScheduledTask task = removed.getTask();
            if (task != null) task.cancel();
        }

        /* ==================================== */
        RunCommand toDelete = null;

        for (RunCommand rC : delayedCommandsByBlockUuid) {
            if (rC == null) continue;
            if (rC.getUuid().equals(uuid)) {
                toDelete = rC;
                ScheduledTask task;
                if ((task = rC.getTask()) != null && canceltask) task.cancel();
            }
        }
        if (toDelete != null) delayedCommandsByBlockUuid.remove(toDelete);


        if (receiverUUID != null) {
            synchronized (receiverLock) {
                List<RunCommand> runCommands = delayedCommandsByReceiverUuid.get(receiverUUID);
                if (runCommands != null) {
                    toDelete = null;
                    for (RunCommand rC : runCommands) {
                        if (rC == null) continue;
                        if (rC.getUuid().equals(uuid)) {
                            toDelete = rC;
                            ScheduledTask task;
                            if ((task = rC.getTask()) != null && canceltask) task.cancel();
                        }
                    }
                    if (toDelete != null) runCommands.remove(toDelete);

                    if (runCommands.isEmpty()) {
                        delayedCommandsByReceiverUuid.remove(receiverUUID);
                    }
                }
            }
        }

        //System.out.println(">>>>>> Yess remove :: "+delayedCommandsByReceiverUuid.size());
    }

    public void removeAllDelayedCommands(UUID receiverUUID) {
        List<RunCommand> runCommands;
        synchronized (receiverLock) {
            runCommands = delayedCommandsByReceiverUuid.remove(receiverUUID);
        }
        if (runCommands == null) return;
        for (RunCommand rC : runCommands) {
            if (rC == null) continue;
            this.removeDelayedCommand(rC.getUuid(), null);
        }
    }

    static int i = 0;

    public List<PlayerRunCommand> getDelayedCommandsWithPlayerReceiver(UUID receiverUUID) {
        List<PlayerRunCommand> commands = new ArrayList<>();
        List<RunCommand> runCommands;
        synchronized (receiverLock) {
            runCommands = delayedCommandsByReceiverUuid.get(receiverUUID);
        }
        if (runCommands != null) {
            for (RunCommand rC : runCommands) {
                if (rC instanceof PlayerRunCommand) commands.add((PlayerRunCommand) rC);
            }
        }
        i++;
        return commands;
    }

    private List<List<RunCommand>> snapshotDelayedCommandsByReceiver() {
        synchronized (receiverLock) {
            return new ArrayList<>(delayedCommandsByReceiverUuid.values());
        }
    }

    public List<PlayerRunCommand> getDelayedPlayerCommands() {
        List<PlayerRunCommand> commands = new ArrayList<>();
        for (List<RunCommand> runCommands : snapshotDelayedCommandsByReceiver()) {
            for (RunCommand rC : runCommands) {
                if (rC instanceof PlayerRunCommand) commands.add((PlayerRunCommand) rC);
            }
        }
        return commands;
    }

    public List<EntityRunCommand> getDelayedEntityCommands() {
        List<EntityRunCommand> commands = new ArrayList<>();
        for (List<RunCommand> runCommands : snapshotDelayedCommandsByReceiver()) {
            for (RunCommand rC : runCommands) {
                if (rC instanceof EntityRunCommand) commands.add((EntityRunCommand) rC);
            }
        }
        return commands;
    }

    public void addStopPickup(Player p, Integer delay) {
        if (p == null) return;

        long time = System.currentTimeMillis() + (delay * 50);
        //System.out.println("ADD "+p.getDisplayName()+ " time: "+time);
        stopPickup.put(p, time);
        Runnable runnable = () -> stopPickup.remove(p, time);
        SCore.schedulerHook.runEntityTask(runnable, null, p, delay);
    }

    public void addStopPickup(Player p, Integer delay, Material material) {
        if (p == null) return;

        stopPickupMaterial.computeIfAbsent(p, k -> new CopyOnWriteArrayList<>()).add(material);
        Runnable runnable = () -> {
            List<Material> materials = stopPickupMaterial.get(p);
            if (materials != null) materials.remove(material);
        };
        SCore.schedulerHook.runEntityTask(runnable, null, p, delay);
    }

    //FAIRE AVEC LHEURE DE FIN CEST MIEUX

    public boolean hasStopPickup(@NotNull Player p) {
        long time = System.currentTimeMillis();
        //System.out.println("pickup "+CommandsHandler.getInstance().getStopPickup().get(p)+" actual "+time);
        Long until = stopPickup.get(p);
        if (until == null) return false;
        boolean stop = until > time;
        if (!stop) stopPickup.remove(p, until);
        return stop;
    }

    public boolean hasStopPickup(@NotNull Player p, Material material) {
        List<Material> materials = stopPickupMaterial.get(p);
        return materials != null && materials.contains(material);
    }

}
