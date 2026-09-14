package com.ssomar.score.commands.runnable.util.commands;

import com.ssomar.score.commands.runnable.SCommand;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class DelayTick extends SCommand {

    public static final List<String> DELAY_TICK_NAMES =  Arrays.asList("DELAY_TICK", "DELAYTICK");

    @Override
    public List<String> getNames() {
        return DELAY_TICK_NAMES;
    }

    @Override
    public String getTemplate() {
        return "DELAY_TICK {number}";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.YELLOW;
    }

    @Override
    public ChatColor getExtraColor() {
        return ChatColor.GOLD;
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        return Optional.empty();
    }

    @Override
    public String getWikiLink() {
        return null;
    }

    public static boolean checkContains(String command){
        for (String name : DELAY_TICK_NAMES) {
            if (command.contains(name+ " ")) {
                return true;
            }
        }
        return false;
    }

    /**
     * True when the command itself IS a delay: it starts with DELAYTICK/DELAY_TICK followed by a space.
     * {@link #checkContains} also matched a DELAYTICK living inside another command's quoted payload
     * (e.g. {@code cooldowncommand ... "LOCATED_LAUNCH ... ,, DELAYTICK 3 ,, ..."}), which was then parsed
     * as a number and threw out of the builder, killing the whole activator.
     */
    public static boolean checkStartsWith(String command) {
        if (command == null) return false;
        String s = command.trim();
        for (String name : DELAY_TICK_NAMES) {
            if (s.startsWith(name + " ")) return true;
        }
        return false;
    }

    /**
     * Returns the part of the command that is a delay directive, or null: the command itself when it starts
     * with the directive, otherwise the first {@code +++} fragment that starts with it.
     *
     * @param names directive names, each tested as {@code name + " "} at the start of a trimmed fragment
     */
    public static String delayDirective(String command, List<String> names) {
        if (command == null) return null;
        String[] fragments = command.contains("+++") ? command.split("\\+\\+\\+") : new String[]{command};
        for (String fragment : fragments) {
            String s = fragment.trim();
            for (String name : names) {
                if (s.startsWith(name + " ")) return fragment;
            }
        }
        return null;
    }

    public static final List<String> DELAY_SECONDS_NAMES = Arrays.asList("DELAY");

    public static String replaceCommand(String command) {
        for (String name : DELAY_TICK_NAMES) {
            command = command.replaceAll(name + " ", "");
        }
        return command;
    }
}
