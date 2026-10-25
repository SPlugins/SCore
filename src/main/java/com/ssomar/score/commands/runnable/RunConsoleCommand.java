package com.ssomar.score.commands.runnable;

import com.ssomar.score.SCore;
import com.ssomar.score.usedapi.AllWorldManager;
import com.ssomar.score.utils.safebreak.BreakChain;
import com.ssomar.score.utils.strings.StringConverter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Optional;


public class RunConsoleCommand {

    static ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

    public static void runConsoleCommand(String command, boolean silenceOutput) {
        FilterManager fM = FilterManager.getInstance();

        if (silenceOutput) {
            //fM.setLogFilterPrior();
            //fM.showDebug();
            fM.incCurrentlyInRun();
            //SsomarDev.testMsg("Currently in run: "+fM.getCurrentlyInRun(), true);

        }

        // By default Bukkit.dispatchCommand() is not sync , so one tick in addition is not a problem, its more to remind me that console commands are not sync
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                try {
                    String newCommand = addWorldCompatibilityForExecute(command);
                    if(!newCommand.isEmpty()) Bukkit.dispatchCommand(console, StringConverter.coloredString(newCommand));
                } catch (Exception e) {
                    e.printStackTrace();
                    SCore.plugin.getLogger().severe(SCore.NAME_COLOR_WITH_BRACKETS + " ERROR WHEN THE CONSOLE COMMAND (" + command + ") IS RUN !");
                }

            }
        };
        /* score run-block-command ... BREAK dispatched here continues the chain of blocks broken by commands */
        SCore.schedulerHook.runTask(BreakChain.wrap(BreakChain.current(), runnable), 1);


        Runnable runnable3 = new Runnable() {

            @Override
            public void run() {
                if (silenceOutput) fM.decrCurrentlyInRun();

            }
        };
        SCore.schedulerHook.runTask(runnable3, 2);
    }

    public static String addWorldCompatibilityForExecute(String command) {
        if (command.contains("in <<")) {
            String[] spliter = command.split("in <<");
            if (spliter.length > 1) {
                String[] spliter2 = spliter[1].split(">>");
                String worldName = spliter2[0];
                Optional<World> worldOptional = AllWorldManager.getWorld(worldName);
                World world = null;
                if (worldOptional.isPresent()) world = worldOptional.get();
                //SsomarDev.testMsg("world: "+world, true);
                if (world != null) {
                    // Need that because getEntities() need to be run in RegionScheduler
                    if(SCore.isFolia()){
                        final World worldFinal = world;
                        final String finalCommand = command;
                        BukkitRunnable runnable = new BukkitRunnable() {
                            @Override
                            public void run() {
                                List<Entity> entities = worldFinal.getEntities();
                                if (!entities.isEmpty()) {
                                    Entity entity = entities.get(0);
                                    //SsomarDev.testMsg("entity: "+entity, true);
                                    final String newCommand = finalCommand.replaceAll("in <<" + worldName + ">>", "at " + entity.getUniqueId());
                                    Bukkit.dispatchCommand(console, StringConverter.coloredString(newCommand));
                                }
                            }
                        };
                        SCore.schedulerHook.runLocationTask(runnable, world.getSpawnLocation(), 1);
                        return "";
                    }
                    else {
                        List<Entity> entities = world.getEntities();
                        if (!entities.isEmpty()) {
                            Entity entity = entities.get(0);
                            //SsomarDev.testMsg("entity: "+entity, true);
                            command = command.replaceAll("in <<" + worldName + ">>", "at " + entity.getUniqueId());
                        } else return "";
                    }
                }
            }
        }
        return command;
    }
}
