package com.ssomar.score.commands.runnable.mixed_player_entity.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.ArgumentChecker;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.mixed_player_entity.MixedCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SetYaw extends MixedCommand {


    @Override
    public void run(Player p, Entity receiver, SCommandToExec sCommandToExec) {
        List<String> args = sCommandToExec.getOtherArgs();
        float yaw = Double.valueOf(args.get(0)).floatValue();
        boolean keepVelocity = false;
        if(args.size() > 1) keepVelocity = Boolean.parseBoolean(args.get(1));

        Vector velocity = receiver.getVelocity();
        Location location = receiver.getLocation();
        location.setYaw(yaw);
        if (SCore.isFolia())
            receiver.teleportAsync(location);
        else
            receiver.teleport(location);
        if(keepVelocity) receiver.setVelocity(velocity);
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        if (args.size() < 1) return Optional.of(notEnoughArgs + getTemplate());

        ArgumentChecker ac = checkDouble(args.get(0), isFinalVerification, getTemplate());
        if (!ac.isValid()) return Optional.of(ac.getError());

        if(args.size() > 1) {
            ArgumentChecker ac2 = checkBoolean(args.get(1), isFinalVerification, getTemplate());
            if (!ac2.isValid()) return Optional.of(ac2.getError());
        }

        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("SET_YAW");
        names.add("SETYAW");
        return names;
    }

    @Override
    public String getTemplate() {
        return "SET_YAW {yaw_number} [keepVelocity]";
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
