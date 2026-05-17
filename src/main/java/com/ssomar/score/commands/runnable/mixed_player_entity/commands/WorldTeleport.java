package com.ssomar.score.commands.runnable.mixed_player_entity.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.ArgumentChecker;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.mixed_player_entity.MixedCommand;
import com.ssomar.score.usedapi.MultiverseAPI;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WorldTeleport extends MixedCommand {

    @Override
    public void run(Player p, Entity receiver, SCommandToExec sCommandToExec) {
        List<String> args = sCommandToExec.getOtherArgs();

        if (args.size() == 1) {
            Location locP = receiver.getLocation();
            World world;
            if (SCore.hasMultiverse) world = MultiverseAPI.getWorld(args.get(0));
            else world = Bukkit.getWorld(args.get(0));


            int i = 0;
            boolean teleport = false;
            while (i < 999 && !teleport) {

                Location newLoc = new Location(world, locP.getX() + i, locP.getY(), locP.getZ());

                if (world.getEnvironment().equals(Environment.NETHER)) {
                    int y = receiver.getWorld().getHighestBlockYAt((int) newLoc.getX(), (int) newLoc.getZ());
                    newLoc.setY(y);

                    boolean block1 = false;
                    boolean block2 = false;

                    while (newLoc.getY() > 120) {
                        newLoc.subtract(0, 1, 0);
                    }

                    while (newLoc.getY() > 0) {
                        if (block1 && newLoc.getBlock().isEmpty()) {
                            block2 = true;
                        } else if (newLoc.getBlock().isEmpty()) {
                            block1 = true;
                        } else if (block1 && block2) {
                            if (newLoc.getBlock().getType().equals(Material.LAVA)) {
                                block1 = false;
                                block2 = false;
                                break;
                            }
                            newLoc.add(0, 1, 0);
                            if (SCore.isFolia())
                                receiver.teleportAsync(newLoc);
                            else
                                receiver.teleport(newLoc);
                            teleport = true;
                            break;
                        } else {
                            block1 = false;
                            block2 = false;
                        }
                        newLoc.subtract(0, 1, 0);
                    }
                } else {
                    int y = receiver.getWorld().getHighestBlockYAt((int) newLoc.getX(), (int) newLoc.getZ());
                    newLoc.setY(y);
                    while (newLoc.getBlock().isEmpty() && newLoc.getY() > 0) {
                        newLoc.subtract(0, 1, 0);
                    }
                    if (newLoc.getBlock().getType().equals(Material.LAVA)) {
                        continue;
                    }
                    if (newLoc.getBlock().isEmpty() && newLoc.getY() > 0) continue;
                    else if (!newLoc.getBlock().isEmpty()) {
                        newLoc.add(0, 1, 0);
                        if (SCore.isFolia())
                            receiver.teleportAsync(newLoc);
                        else
                            receiver.teleport(newLoc);
                        teleport = true;
                    }
                }
                i++;
            }
            if (!teleport) receiver.sendMessage("ERROR NO POS FOUND");
        }
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {

        if (args.size() < 1) return Optional.of(notEnoughArgs + getTemplate());

        ArgumentChecker ac = checkWorld(args.get(0), isFinalVerification, getTemplate());
        if (!ac.isValid()) return Optional.of(ac.getError());

        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("WORLD_TELEPORT");
        names.add("WORLDTELEPORT");
        return names;
    }

    @Override
    public String getTemplate() {
        return "WORLD_TELEPORT {world}";
    }

    @Override
    public ChatColor getColor() {
        return null;
    }

    @Override
    public ChatColor getExtraColor() {
        return null;
    }

}
