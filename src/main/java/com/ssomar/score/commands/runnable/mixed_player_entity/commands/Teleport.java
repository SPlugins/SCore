package com.ssomar.score.commands.runnable.mixed_player_entity.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.ArgumentChecker;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.mixed_player_entity.MixedCommand;
import com.ssomar.score.usedapi.AllWorldManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/* CUSTOMDASH1 {x} {y} {z} */
public class Teleport extends MixedCommand {

    @Override
    public void run(Player p, Entity receiver, SCommandToExec sCommandToExec) {
        List<String> args = sCommandToExec.getOtherArgs();
        World world = AllWorldManager.getWorld(args.get(0)).get();

        double x = Double.parseDouble(args.get(1));
        double y = Double.parseDouble(args.get(2));
        double z = Double.parseDouble(args.get(3));

        float pitch = receiver.getLocation().getPitch();
        float yaw = receiver.getLocation().getYaw();
        Vector velocity = receiver.getVelocity();
        boolean keepVelocity = false;

        if(args.size() >= 5) pitch = Float.parseFloat(args.get(4));
        if(args.size() >= 6) yaw = Float.parseFloat(args.get(5));
        if(args.size() >= 7) keepVelocity = Boolean.parseBoolean(args.get(6));


        Location loc = new Location(world, x, y, z);
        loc.setPitch(pitch);
        loc.setYaw(yaw);
        if (SCore.isFolia())
            receiver.teleportAsync(loc);
        else
            receiver.teleport(loc);
        if(keepVelocity) receiver.setVelocity(velocity);
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        return staticVerif(args, isFinalVerification, getTemplate());
    }

    public static Optional<String> staticVerif(List<String> args, boolean isFinalVerification, String template){
        if (args.size() < 4) return Optional.of(notEnoughArgs + template);

        ArgumentChecker ac = checkWorld(args.get(0), isFinalVerification, template);
        if (!ac.isValid()) return Optional.of(ac.getError());

        for (int i = 1; i < 4; i++) {
            ArgumentChecker ac2 = checkDouble(args.get(i), isFinalVerification, template);
            if (!ac2.isValid()) return Optional.of(ac2.getError());
        }

        if (args.size() >= 5) {
            String value = args.get(4);
            ArgumentChecker ac3 = checkDouble(value, isFinalVerification, template);
            if (!ac3.isValid()) return Optional.of(ac3.getError());
        }

        if (args.size() >= 6) {
            String value = args.get(5);
            ArgumentChecker ac3 = checkDouble(value, isFinalVerification, template);
            if (!ac3.isValid()) return Optional.of(ac3.getError());
        }

        if (args.size() >= 7) {
            String value = args.get(6);
            ArgumentChecker ac3 = checkBoolean(value, isFinalVerification, template);
            if (!ac3.isValid()) return Optional.of(ac3.getError());
        }

        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("TELEPORT");
        return names;
    }

    @Override
    public String getTemplate() {
        return "TELEPORT {world} {x} {y} {z} [pitch] [yaw] [keepVelocity]";
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
