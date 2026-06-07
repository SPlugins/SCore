package com.ssomar.score.commands.runnable.player.commands;

import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.player.PlayerCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class OpenAnvil extends PlayerCommand {

    public OpenAnvil() {
        CommandSetting world = new CommandSetting("world", 0, String.class, "");
        CommandSetting x = new CommandSetting("x", 1, Double.class, 0.0);
        CommandSetting y = new CommandSetting("y", 2, Double.class, 0.0);
        CommandSetting z = new CommandSetting("z", 3, Double.class, 0.0);
        CommandSetting force = new CommandSetting("force", 4, Boolean.class, true);
        List<CommandSetting> settings = getSettings();
        settings.add(world);
        settings.add(x);
        settings.add(y);
        settings.add(z);
        settings.add(force);
        setNewSettingsMode(true);
    }

    @Override
    public void run(Player p, Player receiver, SCommandToExec sCommandToExec) {
        boolean force = (boolean) sCommandToExec.getSettingValue("force");
        String worldName = (String) sCommandToExec.getSettingValue("world");

        // No world provided -> virtual anvil
        if (worldName == null || worldName.isEmpty()) {
            receiver.openAnvil(null, force);
            return;
        }

        // World provided -> open the real anvil at the coords so vanilla anvil-damage mechanics apply
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            receiver.openAnvil(null, force);
            return;
        }

        double x = (double) sCommandToExec.getSettingValue("x");
        double y = (double) sCommandToExec.getSettingValue("y");
        double z = (double) sCommandToExec.getSettingValue("z");
        receiver.openAnvil(new Location(world, x, y, z), force);
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("OPEN_ANVIL");
        return names;
    }

    @Override
    public String getTemplate() {
        return "OPEN_ANVIL world: x:0 y:0 z:0 force:true";
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
