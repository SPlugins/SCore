package com.ssomar.score.commands.runnable.entity.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.entity.EntityCommand;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TeleportPosition extends EntityCommand {

    public TeleportPosition() {
        CommandSetting x = new CommandSetting("x", 0, Double.class, 0.0);
        CommandSetting y = new CommandSetting("y", 1, Double.class, 0.0);
        CommandSetting z = new CommandSetting("z", 2, Double.class, 0.0);
        List<CommandSetting> settings = getSettings();
        settings.add(x);
        settings.add(y);
        settings.add(z);
        setNewSettingsMode(true);
    }

    @Override
    public void run(Player p, Entity entity, SCommandToExec sCommandToExec) {

        double x = (double) sCommandToExec.getSettingValue("x");
        double y = (double) sCommandToExec.getSettingValue("y");
        double z = (double) sCommandToExec.getSettingValue("z");

        List<String> args = sCommandToExec.getOtherArgs();
        if (args.size() == 3) {
            if (!entity.isDead())
                if (SCore.isFolia())
                    entity.teleportAsync(new Location(entity.getWorld(), x, y, z));
                else
                    entity.teleport(new Location(entity.getWorld(), x, y, z));
        }
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("TELEPORT_POSITION");
        names.add("TELEPORT POSITION");
        return names;
    }

    @Override
    public String getTemplate() {
        return "TELEPORT_POSITION x:5.0 y:64.0 z:98.0";
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
