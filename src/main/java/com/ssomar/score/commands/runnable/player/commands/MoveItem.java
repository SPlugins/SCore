package com.ssomar.score.commands.runnable.player.commands;

import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.player.PlayerCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MoveItem extends PlayerCommand {

    public MoveItem() {
        CommandSetting yourSlot = new CommandSetting("yourSlot", 0, Integer.class, 0);
        CommandSetting theirSlot = new CommandSetting("theirSlot", 1, Integer.class, -1);
        CommandSetting who = new CommandSetting("who", -1, String.class, "null");
        List<CommandSetting> settings = getSettings();
        settings.add(yourSlot);
        settings.add(theirSlot);
        settings.add(who);
        setNewSettingsMode(true);
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("MOVE_ITEM");
        return names;

    }

    @Override
    public String getTemplate() {
        return "MOVE_ITEM yourSlot:0 theirSlot:1 who:IGN";
    }

    @Override
    public ChatColor getColor() {
        return null;
    }

    @Override
    public ChatColor getExtraColor() {
        return null;
    }

    @Override
    public void run(@Nullable Player launcher, Player receiver, SCommandToExec sCommandToExec) {
        int yourSlot = (int) sCommandToExec.getSettingValue("yourSlot");
        int theirSlot = (int) sCommandToExec.getSettingValue("theirSlot"); // optional
        String who = (String) sCommandToExec.getSettingValue("who"); // optional

        // try to get the Player instance of the target player

        Player targetPlayer;

        if (receiver == null) {
            targetPlayer = Bukkit.getPlayer(who);
            if (targetPlayer == null) return;
        } else {
            targetPlayer = receiver;
        }

        // if ever the user wants to specify where the item should land if possible

        ItemStack itemFromLauncher = launcher.getInventory().getItem(yourSlot);
        if (itemFromLauncher == null) return;

        if (theirSlot >= 0) {
            ItemStack slotItemFromTarget = targetPlayer.getInventory().getItem(theirSlot);
            if (slotItemFromTarget == null ||  slotItemFromTarget.getType() == Material.AIR) {
                targetPlayer.getInventory().setItem(theirSlot, itemFromLauncher);
                launcher.getInventory().setItem(yourSlot, new ItemStack(Material.AIR));
            } else {
                targetPlayer.getInventory().addItem(itemFromLauncher);
                launcher.getInventory().setItem(yourSlot, new ItemStack(Material.AIR));
            }
        } else {
            targetPlayer.getInventory().addItem(itemFromLauncher);
            launcher.getInventory().setItem(yourSlot, new ItemStack(Material.AIR));
        }

    }
}
