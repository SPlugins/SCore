package com.ssomar.score.commands.runnable.block.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.block.BlockCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Mainly used for infinite water and lava buckets.
 */
public class PlaceLiquid extends BlockCommand {

    public PlaceLiquid() {
        CommandSetting type = new CommandSetting("type", 0, String.class, "water");
        List<CommandSetting> settings = getSettings();
        settings.add(type);
        setNewSettingsMode(true);
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("PLACELIQUID");
        return names;
    }

    @Override
    public String getTemplate() {
        return "PLACELIQUID";
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
    public void run(@Nullable Player p, @NotNull Block block, SCommandToExec sCommandToExec) {
        String type = ((String) sCommandToExec.getSettingValue("type")).toUpperCase();
        BlockData blockData = block.getBlockData();

        if (block.getType() == Material.AIR) {
            if (type.equals("WATER")) block.setType(Material.WATER);
            else if (type.equals("LAVA")) block.setType(Material.LAVA);
        }

        else if (type.equals("WATER") && blockData instanceof Waterlogged) {
            Waterlogged waterlogged = (Waterlogged) blockData;
            waterlogged.setWaterlogged(true);
            block.setBlockData(waterlogged);
        }

        else if (block.getType() == Material.CAULDRON) {
            if (type.equals("WATER")) {
                block.setType(Material.WATER_CAULDRON);

                Levelled levelled = (Levelled) block.getBlockData();
                levelled.setLevel(levelled.getMaximumLevel());

                block.setBlockData(levelled);
            } else if (type.equals("LAVA") && SCore.is1v17Plus()) {
                block.setType(Material.LAVA_CAULDRON);
            }
        }
    }
}
