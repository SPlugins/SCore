package com.ssomar.score.commands.runnable;

import com.ssomar.score.splugin.SPlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class CommandManager<T extends SCommand> {

    @Getter
    @Setter
    private List<T> commands;

    /** Get the associated custom command of the entry if there is one.
     * The name must end on a word boundary and the longest matching name wins, so the result no longer
     * depends on registration order (SETBLOCK vs SETBLOCKPOS, TELEPORT vs TELEPORT POSITION) and another
     * plugin's command that merely starts with an SCore name (launchprojectile, breakinradius) is left alone. **/
    public Optional<T> getCommand(String entry) {
        if (entry == null) return Optional.empty();
        NameIndex<T> index = nameIndex;
        if (index == null || index.commands != commands || index.size != commands.size()) nameIndex = index = new NameIndex<>(commands);
        T best = null;
        int bestLen = -1;
        List<Map.Entry<String, T>> candidates = index.byFirstWord.get(firstWord(entry));
        if (candidates != null) {
            /* Same order as the commands list: the first command with the longest name wins, as before */
            for (Map.Entry<String, T> candidate : candidates) {
                String name = candidate.getKey();
                if (name.length() > bestLen && SCommand.startsWithName(entry, name)) {
                    bestLen = name.length();
                    best = candidate.getValue();
                }
            }
        }
        return Optional.ofNullable(best);
    }

    /* getCommand runs for each command line: it asked every command (several hundreds) to match the entry, and each getNames()
     * builds a new list. A name can only match an entry that starts with the same first word, so the names are indexed by it. */
    private volatile NameIndex<T> nameIndex;

    private static final class NameIndex<T extends SCommand> {
        private final List<T> commands;
        private final int size;
        private final Map<String, List<Map.Entry<String, T>>> byFirstWord = new HashMap<>();

        private NameIndex(List<T> commands) {
            this.commands = commands;
            this.size = commands.size();
            for (T command : commands) {
                for (String name : command.getNames()) {
                    if (name == null || name.isEmpty()) continue;
                    byFirstWord.computeIfAbsent(firstWord(name), k -> new ArrayList<>()).add(new AbstractMap.SimpleImmutableEntry<>(name, command));
                }
            }
        }
    }

    /** The leading letters, digits and '_' (the boundary of SCommand.startsWithName), each char folded the way
     * String.regionMatches(true, ...) compares them: there 'İ' matches 'i' and the Kelvin sign 'k' (MİNEİNCUBE typed with a
     * Turkish caps lock), which String.toUpperCase keeps apart. */
    static String firstWord(String s) {
        StringBuilder word = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') break;
            word.append(Character.toLowerCase(Character.toUpperCase(c)));
        }
        return word.toString();
    }

    /** Extract the arguments of the entry for a specific custom command **/
    public List<String> getArgs(T command, String entry) {
        // Same (case-insensitive, boundary-aware) comparison as getCommand: a case-sensitive strip here
        // left the command word in the arguments for lower-case entries.
        entry = command.stripName(entry);
        if(entry.trim().equals("")) return new ArrayList<>();
        return Arrays.asList(entry.trim().split(" "));
    }

    /** Verify if the args are correct for a specific command **/
    public Optional<String> verifArgs(@NotNull T command, List<String> args) {
        return command.verify(args, false);
    }

    public Optional<String> verifCommand(String entry) {
        Optional<T> commandOpt = getCommand(entry);
        if (commandOpt.isPresent()) {
            return verifCommand(commandOpt.get(), entry);
        }
        return Optional.empty();
    }

    public Optional<String> verifCommand(T command, String entry) {
        List<String> args = getArgs(command, entry);

        Optional<String> error = this.verifArgs(command, args);
        if (error.isPresent()) {
            return Optional.of("&6>>" + " " + error.get());
        }

        return Optional.empty();
    }

    public List<String> getCommandsVerified(SPlugin sPlugin, @NotNull List<String> entries, List<String> errorList, String id) {
        List<String> result = new ArrayList<>();

        for (String entry : entries) {
            Optional<T> commandOpt = getCommand(entry);
            if (commandOpt.isPresent() && !entry.contains("+++")) {
                T command = commandOpt.get();
                Optional<String> error = verifCommand(command, entry);
                error.ifPresent(value -> errorList.add("&cERROR, Invalid command &7&o(Command: "+entry+")  &7&o(ID: " + id+ ") "+value));
            }
            result.add(entry);
        }
        return result;
    }

    public Map<String, String> getCommandsDisplay() {
        Map<String, String> result = new HashMap<>();
        for (SCommand c : this.commands) {

            ChatColor extra = c.getExtraColor();
            if (extra == null) extra = ChatColor.DARK_PURPLE;

            ChatColor color = c.getColor();
            if (color == null) color = ChatColor.LIGHT_PURPLE;

            result.put(extra + "[" + color + "&l" + c.getNames().get(0) + extra + "]", c.getTemplate());
        }
        return result;
    }
}
