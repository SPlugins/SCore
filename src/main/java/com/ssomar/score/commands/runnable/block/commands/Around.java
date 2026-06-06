package com.ssomar.score.commands.runnable.block.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.CommmandThatRunsCommand;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.block.BlockCommand;
import com.ssomar.score.usedapi.GriefPreventionAPI;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


/* AROUND {distance} {true or false} {Your commands here} */
public class Around extends BlockCommand {

    private final static Boolean DEBUG = false;

    public Around() {
        CommandSetting distance = new CommandSetting("distance", 0, Double.class, 3d);
        CommandSetting displayMsgIfNoPlayer = new CommandSetting("affectThePlayerThatActivesTheActivator", 1, Boolean.class, true, true);
        CommandSetting throughBlocks = new CommandSetting("throughBlocks", -1, Boolean.class, true);
        CommandSetting limit = new CommandSetting("limit", -1, Integer.class, -1);
        CommandSetting sort = new CommandSetting("sort", -1, String.class, "NEAREST");
        CommandSetting regionCheck = new CommandSetting("regionCheck", -1, Boolean.class, false);
        List<CommandSetting> settings = getSettings();
        settings.add(distance);
        settings.add(displayMsgIfNoPlayer);
        settings.add(throughBlocks);
        settings.add(limit);
        settings.add(sort);
        settings.add(regionCheck);
        setNewSettingsMode(true);
        setCanExecuteCommands(true);
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        String error = "";

        String around = "AROUND {distance} [affectThePlayerThatActivesTheActivator true or false] {Your commands here}";

        if (args.size() < 2) error = notEnoughArgs + around;
        else if (args.size() > 2) {
            try {
                Double.valueOf(args.get(0));
            } catch (NumberFormatException e) {
                error = invalidDistance + args.get(0) + " for command: " + around;
            }
        }

        return error.isEmpty() ? Optional.empty() : Optional.of(error);
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("AROUND");
        return names;
    }

    @Override
    public String getTemplate() {
        return "AROUND {distance} [affectThePlayerThatActivesTheActivator true or false] {Your commands here}";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.LIGHT_PURPLE;
    }

    @Override
    public ChatColor getExtraColor() {
        return ChatColor.DARK_PURPLE;
    }

    @Override
    public void run(Player p, @NotNull Block block, SCommandToExec sCommandToExec) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    double distance = (double) sCommandToExec.getSettingValue("distance");
                    boolean affectThePlayerThatActivesTheActivator = (boolean) sCommandToExec.getSettingValue("affectThePlayerThatActivesTheActivator");
                    boolean throughBlocks = (boolean) sCommandToExec.getSettingValue("throughBlocks");
                    int limit = (int) sCommandToExec.getSettingValue("limit");
                    String sort = (String) sCommandToExec.getSettingValue("sort");
                    boolean regionCheck = (boolean) sCommandToExec.getSettingValue("regionCheck");

                    List<Player> targets = new ArrayList<>();
                    for (Entity e : block.getWorld().getNearbyEntities(block.getLocation().add(0.5, 0.5, 0.5), distance, distance, distance)) {
                        if (e instanceof Player) {

                            Location receiverLoc = e.getLocation();
                            if (p != null && regionCheck && SCore.hasGriefPrevention && !GriefPreventionAPI.playerIsInHisClaim(p, e.getLocation(), true)) continue;

                            if(!throughBlocks){
                                List<Location> centerLocationOfEachFaces = new ArrayList<>();
                                centerLocationOfEachFaces.add(block.getLocation().add(0, 0.5, 0.5));
                                centerLocationOfEachFaces.add(block.getLocation().add(1, 0.5, 0.5));
                                centerLocationOfEachFaces.add(block.getLocation().add(0.5, 0, 0.5));
                                centerLocationOfEachFaces.add(block.getLocation().add(0.5, 1, 0.5));
                                centerLocationOfEachFaces.add(block.getLocation().add(0.5, 0.5, 0));
                                centerLocationOfEachFaces.add(block.getLocation().add(0.5, 0.5, 1));

                                boolean valid = false;
                                for(Location loc : centerLocationOfEachFaces){
                                    double distanceBetween = receiverLoc.distance(loc);
                                    Vector direction = loc.toVector().subtract(receiverLoc.toVector()).normalize();
                                    RayTraceResult rayTraceResult = receiverLoc.getWorld().rayTraceBlocks(receiverLoc, direction, distanceBetween, FluidCollisionMode.NEVER, true);
                                    if(rayTraceResult == null) {
                                        valid = true;
                                        break;
                                    }
                                }
                                if(!valid) continue;
                            }

                            Player target = (Player) e;
                            if (target.hasMetadata("NPC") || (!affectThePlayerThatActivesTheActivator && (p != null && p.equals(target))))
                                continue;
                            targets.add(target);

                        }
                    }

                    if (sort.equalsIgnoreCase("NEAREST")) {
                        targets.sort((p1, p2) -> {
                            double d1 = p1.getLocation().distance(block.getLocation());
                            double d2 = p2.getLocation().distance(block.getLocation());
                            return Double.compare(d1, d2);
                        });
                    } else if (sort.equalsIgnoreCase("RANDOM")) {
                        Collections.shuffle(targets);
                    }

                    if (limit > 0 && targets.size() > limit) {
                        targets = targets.subList(0, limit);
                    }

                    CommmandThatRunsCommand.runPlayerCommands(targets, sCommandToExec.getOtherArgs(), sCommandToExec.getActionInfo());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        SCore.schedulerHook.runLocationTask(runnable, block.getLocation(), 0);
    }
}
