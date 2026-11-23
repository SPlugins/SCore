package com.ssomar.score.commands.runnable.block.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.CommandSetting;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.block.BlockCommand;
import com.ssomar.score.utils.safebreak.SafeBreak;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DrainInCube extends BlockCommand {

    /* Removed with the water when waterlogged:true, like a sponge does. By name: they don't exist before 1.13 */
    private static final List<Material> WATER_PLANTS = new ArrayList<>();

    static {
        for (String name : new String[]{"KELP", "KELP_PLANT", "SEAGRASS", "TALL_SEAGRASS", "BUBBLE_COLUMN"}) {
            Material material = Material.matchMaterial(name);
            if (material != null) WATER_PLANTS.add(material);
        }
    }

    public DrainInCube() {
        /* Opt-in: also dry the waterlogged blocks (slabs, stairs...) and remove the water plants */
        CommandSetting waterlogged = new CommandSetting("waterlogged", -1, Boolean.class, false);
        List<CommandSetting> settings = getSettings();
        settings.add(waterlogged);
        setNewSettingsMode(true);
    }

    @Override
    public void run(Player p, @NotNull Block block, SCommandToExec sCommandToExec) {
        List<String> args = sCommandToExec.getOtherArgs();


        try {
            boolean waterlogged = (boolean) sCommandToExec.getSettingValue("waterlogged");
            String radiusString = args.get(0);
            boolean byPassMaxRadius = false;
            if (radiusString.contains("*")) {
                radiusString = radiusString.replace("*", "").trim();
                byPassMaxRadius = true;
            }
            int radius = Integer.parseInt(radiusString);

            List<Material> whiteList = new ArrayList<>();
            if (args.size() <= 1) {
                whiteList.add(Material.WATER);
                whiteList.add(Material.LAVA);
            } else if (args.get(1).toUpperCase().equals("LAVA")) {
                whiteList.add(Material.LAVA);
            } else if (args.get(1).toUpperCase().equals("WATER")) {
                whiteList.add(Material.WATER);
            }

            /* Waterlogged blocks only exist in 1.13+ */
            boolean drainWaterlogged = waterlogged && whiteList.contains(Material.WATER) && !SCore.is1v12Less();

            if (radius < 10 || byPassMaxRadius) {
                for (int y = -radius; y < radius + 1; y++) {
                    for (int x = -radius; x < radius + 1; x++) {
                        for (int z = -radius; z < radius + 1; z++) {

                            Block toBreak = block.getWorld().getBlockAt(block.getX() + x, block.getY() + y, block.getZ() + z);

                            if (whiteList.contains(toBreak.getType()) || (drainWaterlogged && WATER_PLANTS.contains(toBreak.getType()))) {
                                if(p == null || SafeBreak.verifSafeBreak(p.getUniqueId(), toBreak))
                                    toBreak.setType(Material.AIR);
                            } else if (drainWaterlogged && Waterlog.isWaterlogged(toBreak)) {
                                if(p == null || SafeBreak.verifSafeBreak(p.getUniqueId(), toBreak))
                                    Waterlog.dry(toBreak);
                            }
                        }
                    }
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("DRAININCUBE");
        return names;
    }

    @Override
    public String getTemplate() {
        return "DRAININCUBE {radius} [LAVA or WATER (no need if you want both)] [waterlogged:false]";
    }

    @Override
    public ChatColor getColor() {
        return null;
    }

    @Override
    public ChatColor getExtraColor() {
        return null;
    }

    /* Separate class: BlockData and Waterlogged must not be loaded in 1.12 and less */
    private static class Waterlog {

        static boolean isWaterlogged(Block block) {
            BlockData data = block.getBlockData();
            return data instanceof Waterlogged && ((Waterlogged) data).isWaterlogged();
        }

        static void dry(Block block) {
            BlockData data = block.getBlockData();
            if (data instanceof Waterlogged) {
                ((Waterlogged) data).setWaterlogged(false);
                block.setBlockData(data);
            }
        }
    }
}
