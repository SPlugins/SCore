package com.ssomar.score.commands.runnable.player.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.block.commands.settempblock.SetTempBlockManager;
import com.ssomar.score.commands.runnable.player.PlayerCommand;
import com.ssomar.score.features.types.list.ListDetailedMaterialFeature;
import com.ssomar.score.utils.safeplace.SafePlace;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Player;

import java.util.*;

public class SetTempBlockPos extends PlayerCommand {

    public SetTempBlockPos() {
        CommandSetting x = new CommandSetting("x", 0, Double.class, 0.0);
        CommandSetting y = new CommandSetting("y", 1, Double.class, 0.0);
        CommandSetting z = new CommandSetting("z", 2, Double.class, 0.0);
        CommandSetting world = new CommandSetting("world", -1, String.class, "");
        CommandSetting material = new CommandSetting("material", 3, Material.class, Material.STONE);
        CommandSetting time = new CommandSetting("time", 4, Integer.class, 10);
        CommandSetting bypassProtection = new CommandSetting("bypassProtection", 5, Boolean.class, false);
        CommandSetting whitelistCurrentBlock = new CommandSetting("whitelistCurrentBlock", 6, String.class, "");
        List<CommandSetting> settings = getSettings();
        settings.add(x);
        settings.add(y);
        settings.add(z);
        settings.add(world);
        settings.add(material);
        settings.add(time);
        settings.add(bypassProtection);
        settings.add(whitelistCurrentBlock);
        setNewSettingsMode(true);
    }

    @Override
    public void run(Player p, Player receiver, SCommandToExec sCommandToExec) {

        double x = (double) sCommandToExec.getSettingValue("x");
        double y = (double) sCommandToExec.getSettingValue("y");
        double z = (double) sCommandToExec.getSettingValue("z");
        String world = (String) sCommandToExec.getSettingValue("world");
        Material material = (Material) sCommandToExec.getSettingValue("material");
        int time = (int) sCommandToExec.getSettingValue("time");
        boolean bypassProtection = (boolean) sCommandToExec.getSettingValue("bypassProtection");
        String whitelistCurrentBlock = (String) sCommandToExec.getSettingValue("whitelistCurrentBlock");

        World w = receiver.getWorld();
        if (!world.isEmpty()) {
            w = SCore.plugin.getServer().getWorld(world);
        }
        Location blockLoc = new Location(w, x, y, z);
        Block block = blockLoc.getBlock();
        BlockData data = block.getBlockData().clone();


        UUID uuid = receiver.getUniqueId();
        SsomarDev.testMsg("DEBUG place 0", true);

        //levelled for lights OK
        if (data instanceof Bisected || data instanceof Orientable || data instanceof Rotatable || data instanceof Slab || data instanceof Directional || verifDependentBlock(block))
            return;

        if (!verifWhitelistCurrentBlock(whitelistCurrentBlock, block)) {
            //SsomarDev.testMsg("DEBUG INVALID BLOCK >> "+data.getMaterial(), true);
            return;
        }

        SsomarDev.testMsg("DEBUG PLACE 1", true);
        SafePlace.placeBlockWithEvent(block, material, Optional.empty(), uuid, false, !bypassProtection);

        SetTempBlockManager.getInstance().runInitTempBlock(block.getLocation(), data, time);

    }

    /**
     * Shared by SET_BLOCK / SET_BLOCK_POS / SET_TEMP_BLOCK_POS.
     * @param whitelistCurrentBlock comma separated list of detailed materials, empty = no filter
     * @return true if the block currently at the position can be replaced
     */
    public static boolean verifWhitelistCurrentBlock(String whitelistCurrentBlock, Block block) {
        if (whitelistCurrentBlock == null || whitelistCurrentBlock.isEmpty()) return true;
        ListDetailedMaterialFeature listDetailedMaterialFeature = new ListDetailedMaterialFeature(true);
        List<String> list = new ArrayList<>();
        if (whitelistCurrentBlock.contains(",")) list = Arrays.asList(whitelistCurrentBlock.split(","));
        else list.add(whitelistCurrentBlock);
        listDetailedMaterialFeature.load(SCore.plugin, list, true);
        return listDetailedMaterialFeature.verifBlock(block);
    }

    private static final BlockFace[] BLOCK_FACES = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};


    public boolean verifDependentBlock(Block block) {
        for (BlockFace blockFace : BLOCK_FACES) {
            Block relative = block.getRelative(blockFace);
            Material mat = relative.getType();
            if (mat == Material.TORCH || mat == Material.LADDER) return true; //Found torch, return true.
        }
        return false; //No torch found, return false
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("SET_TEMP_BLOCK_POS");
        names.add("SETTEMPBLOCKPOS");
        return names;
    }

    @Override
    public String getTemplate() {
        return "SET_TEMP_BLOCK_POS x:0.0 y:0.0 z:0.0 material:STONE time:10 bypassProtection:true whitelistCurrentBlock:SAND,DIRT";
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

