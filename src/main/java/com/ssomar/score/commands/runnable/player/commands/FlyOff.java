package com.ssomar.score.commands.runnable.player.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.player.PlayerCommand;
import com.ssomar.score.fly.FlyManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


public class FlyOff extends PlayerCommand {

    public FlyOff() {
        CommandSetting teleportOnTheGround = new CommandSetting("teleportOnTheGround", 0, Boolean.class, true);
        List<CommandSetting> settings = getSettings();
        settings.add(teleportOnTheGround);
        setNewSettingsMode(true);
    }

    @Override
    public void run(Player p, Player receiver, SCommandToExec sCommandToExec) {
        boolean teleport = (boolean) sCommandToExec.getSettingValue("teleportOnTheGround");

        if (teleport) {
            if (!receiver.isOnGround()) {
                Location playerLocation = receiver.getLocation();
                boolean isVoid = false;
                while (playerLocation.getBlock().isEmpty()) {
                    if (playerLocation.getY() <= 1) {
                        isVoid = true;
                        break;
                    }
                    playerLocation.subtract(0, 1, 0);
                }
                if (!isVoid) {
                    playerLocation.add(0, 1, 0);
                    if (SCore.isFolia())
                        receiver.teleportAsync(playerLocation);
                    else
                        receiver.teleport(playerLocation);
                }
            }
        }
        receiver.setAllowFlight(false);
        receiver.setFlying(false);
        FlyManager.getInstance().removePlayerWithFly(receiver);
    }


    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("FLY_OFF");
        names.add("FLY OFF");
        return names;
    }

    @Override
    public String getTemplate() {
        return "FLY_OFF teleportOnTheGround:true";
    }

    @Override
    public ChatColor getColor() {
        return ChatColor.BLUE;
    }

    @Override
    public ChatColor getExtraColor() {
        return ChatColor.AQUA;
    }
}
