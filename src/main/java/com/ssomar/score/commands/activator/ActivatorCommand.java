package com.ssomar.score.commands.activator;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A command registered for the activators that listen to a written command (PLAYER_WRITE_COMMAND with
 * {@code registerCommand: true}). The activator still runs from the command event, before the server dispatches
 * the command: this command does nothing by itself, it only makes the server and the client know it
 * (no "Unknown command", no red command in the chat bar, tab completion).
 */
public class ActivatorCommand extends Command {

    /* each entry: the arguments of one detailedCommands entry, after the label */
    private volatile List<List<String>> argumentChains;

    public ActivatorCommand(String label, List<List<String>> argumentChains) {
        super(label);
        setArgumentChains(argumentChains);
    }

    public void setArgumentChains(List<List<String>> argumentChains) {
        this.argumentChains = argumentChains == null ? Collections.<List<String>>emptyList() : argumentChains;
    }

    public List<List<String>> getArgumentChains() {
        return argumentChains;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        /* nothing to do, the activators ran from the command event */
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return complete(argumentChains, args);
    }

    /**
     * The suggestions for the argument being typed: the next word of each chain whose previous words match
     * what is already written (case insensitive), that starts with the word being written.
     */
    public static List<String> complete(List<List<String>> chains, String[] args) {
        List<String> result = new ArrayList<>();
        if (chains == null || args == null || args.length == 0) return result;
        int index = args.length - 1;
        String current = args[index].toLowerCase(java.util.Locale.ENGLISH);
        for (List<String> chain : chains) {
            if (chain.size() <= index) continue;
            boolean match = true;
            for (int i = 0; i < index; i++) {
                if (!chain.get(i).equalsIgnoreCase(args[i])) {
                    match = false;
                    break;
                }
            }
            if (!match) continue;
            String next = chain.get(index);
            if (next.toLowerCase(java.util.Locale.ENGLISH).startsWith(current) && !result.contains(next)) result.add(next);
        }
        return result;
    }
}
