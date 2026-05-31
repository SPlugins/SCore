package com.ssomar.score.commands.score;

import com.ssomar.score.SCore;
import com.ssomar.score.sobject.SObject;
import com.ssomar.score.sobject.SObjectBuildable;
import com.ssomar.score.sobject.SObjectManager;
import com.ssomar.score.splugin.SPlugin;
import com.ssomar.score.utils.strings.StringSetting;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DropCommand<X extends SPlugin, Y extends SObjectManager<Z>, Z extends SObject & SObjectBuildable> extends CustomCommandWithManagerAbstract<X, Y, Z>{

    public DropCommand(X sPlugin, Y sObjectManager) {
        super(sPlugin, sObjectManager);
    }

    @Override
    public void run(CommandSender sender, String command, String[] args, String typedCommand) {
        switch (command) {
            case "drop":
                if(args.length == 0){
                    getSm().sendMessage(sender, "&c"+getSPlugin().getNameWithBrackets()+" &c" + getSObjectManager().getObjectName() + " &6" + args[0] + " &cnot found");
                    return;
                }

                List<String> arguments = new ArrayList<>(Arrays.asList(args));
                Map<String, Object> settings = StringSetting.extractSettingsAndRebuildCorrectly(arguments, 0, new ArrayList<>());

                Optional<Z> objectOpt = checkSObject(sender, arguments.get(0));
                if (!objectOpt.isPresent()) return;

                SObjectBuildable droppable = (SObjectBuildable) objectOpt.get();

                AtomicInteger qty = new AtomicInteger();
                if (arguments.size() == 1) qty.set(1);
                else{
                    Optional<Integer> amount = checkAmount(sender, arguments.get(1));
                    if (!amount.isPresent()) {
                        getSm().sendMessage(sender, "&c"+getSPlugin().getNameWithBrackets()+" &cInvalid amount : &6" + arguments.get(1));
                        return;
                    }
                    qty.set(amount.get());
                }

                if (sender instanceof Player) {
                    Player p = (Player) sender;
                    if (arguments.size() <= 2) {
                        objectOpt.get().dropItem(p.getLocation(), qty.get(), Optional.empty(), settings);
                        return;
                    }
                }

                if(arguments.size() < 6) {
                    getSm().sendMessage(sender, "&c"+getSPlugin().getNameWithBrackets()+" &cUsage: &6/" + getSPlugin().getShortName().toLowerCase() + " drop <" + getSObjectManager().getObjectName() + "> <quantity> <world> <x> <y> <z>");
                    return;
                }

                Optional<Optional<World>> worldOptional = checkWorld(sender, arguments.get(2));
                if (!worldOptional.isPresent() || !worldOptional.get().isPresent()){
                    getSm().sendMessage(sender, "&c"+getSPlugin().getNameWithBrackets()+" &c WORLD &6" + arguments.get(2) + " &cis invalid.");
                    return;
                }
                World world = worldOptional.get().get();

                Optional<Double> xOpt = checkDouble(sender, arguments.get(3));
                if (!xOpt.isPresent()){
                    getSm().sendMessage(sender, "&c"+getSPlugin().getNameWithBrackets()+" &c X &6" + arguments.get(3) + " &cis invalid.");
                    return;
                }

                Optional<Double> yOpt = checkDouble(sender, arguments.get(4));
                if (!yOpt.isPresent()){
                    getSm().sendMessage(sender, "&c"+getSPlugin().getNameWithBrackets()+" &c Y &6" + arguments.get(4) + " &cis invalid.");
                    return;
                }

                Optional<Double> zOpt = checkDouble(sender, arguments.get(5));
                if (!zOpt.isPresent()){
                    getSm().sendMessage(sender, "&c"+getSPlugin().getNameWithBrackets()+" &c Z &6" + arguments.get(5) + " &cis invalid.");
                    return;
                }

                SCore.schedulerHook.runLocationTask(()->{
                    objectOpt.get().dropItem(new Location(world, xOpt.get(), yOpt.get(), zOpt.get()), qty.get(), Optional.empty(), settings);
                    getSm().sendMessage(sender, "&c"+getSPlugin().getNameDesign() + " &7Successfully run &e/" + getSPlugin().getShortName().toLowerCase() + " drop " + arguments.get(0) + " " + qty + " " + world.getName() + " " + xOpt.get() + " " + yOpt.get() + " " + zOpt.get());
                },new Location(world, xOpt.get(), yOpt.get(), zOpt.get()),0);

                break;
        }
    }

    @Override
    public List<String> getCommands() {
        List<String> commands = new ArrayList<>();
        commands.add("drop");
        return commands;
    }

    @Override
    public List<String> getArguments(String command, String[] args) {
        ArrayList<String> arguments = new ArrayList<String>();
        switch (args[0]) {
            case "drop":
                if (args.length == 2) {
                    return getSObjectManager().getLoadedObjectsWith(args[1]);
                } else if (args.length == 3) {
                    arguments.addAll(getArgumentsQuantity());

                    return arguments;
                } else if (args.length == 4) {
                    for (World world : Bukkit.getServer().getWorlds()) {
                        arguments.add(world.getName());
                    }

                    return arguments;
                } else if (args.length == 5) {
                    arguments.add("X");

                    return arguments;
                } else if (args.length == 6) {
                    arguments.add("Y");

                    return arguments;
                } else if (args.length == 7) {
                    arguments.add("Z");

                    return arguments;
                }
                break;
        }
        return new ArrayList<>();
    }
}
