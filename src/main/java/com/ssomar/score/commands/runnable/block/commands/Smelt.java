package com.ssomar.score.commands.runnable.block.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.block.BlockCommand;
import com.ssomar.score.events.BlockBreakEventExtension;
import com.ssomar.score.utils.safebreak.SafeBreak;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/* SMELT */
public class Smelt extends BlockCommand {

    @Override
    public void run(Player p, @NotNull Block block, SCommandToExec sCommandToExec) {
        List<String> args = sCommandToExec.getOtherArgs();
        ActionInfo aInfo = sCommandToExec.getActionInfo();

        UUID pUUID = null;
        if (p != null) pUUID = p.getUniqueId();

        boolean event = true;
        if (args.size() >= 1) {
            event = Boolean.parseBoolean(args.get(0));
        }
        // if its from an event generated
        if(event && aInfo.isEventFromCustomBreakCommand() && aInfo.getBreakCause() == BlockBreakEventExtension.BreakCause.SMELT) return;

        if (SafeBreak.verifSafeBreak(pUUID, block)) {
            ItemStack itemInHand = p.getInventory().getItemInMainHand();

            ItemStack smelted = getSmeltedItem(block.getType());
            if (smelted == null) return;

            // Determine the amount of smelted item to drop
            int amountToDrop = 1;
            Enchantment fortune = SCore.is1v20v5Plus() ? Enchantment.FORTUNE : Enchantment.getByName("LOOT_BONUS_BLOCKS");
            if (itemInHand.containsEnchantment(fortune)) {
                int fortuneLevel = itemInHand.getEnchantmentLevel(fortune);
                amountToDrop = ThreadLocalRandom.current().nextInt(fortuneLevel + 1) + 1;
            }
            // BREAK
            SafeBreak.breakBlockWithEvent(block, pUUID, aInfo.getSlot(), false, event, false, BlockBreakEventExtension.BreakCause.SMELT);

            // Drop the smelted item
            Location dropLocation = block.getLocation();
            block.getWorld().dropItemNaturally(dropLocation, new ItemStack(smelted.getType(), amountToDrop));
        }
    }

    public static void dropItemWithFortune(Block b, Player p, Material blockMat) {
        int amountToDrop = 1;
        // Smelt fortune will only work for ores
        ArrayList<Material> whitelist = new ArrayList<>(Arrays.asList(Material.IRON_INGOT, Material.GOLD_INGOT, Material.getMaterial("COPPER_INGOT")));

        if (whitelist.contains(blockMat)) {
            Enchantment fortune = SCore.is1v20v5Plus() ? Enchantment.FORTUNE : Enchantment.getByName("LOOT_BONUS_BLOCKS");

            ItemStack itemInHand = p.getInventory().getItemInMainHand();
            if (!itemInHand.isEmpty() && itemInHand.containsEnchantment(fortune)) {
                int fortuneLevel = itemInHand.getEnchantmentLevel(fortune);
                amountToDrop = ThreadLocalRandom.current().nextInt(fortuneLevel + 1) + 1;
            }
        }
        b.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(blockMat, amountToDrop));
    }

    /**
     * Redstone, Lapis, Diamond and Emerald ores are ignored because
     * when you break those ores, they do not need to be smelt.
     * @param material
     * @return smelt version of a material (returned ItemStack is just a regular material). If there's no
     * smeltable version of the provided Material enum, it will return a null ItemStack.
     */
    public static ItemStack getSmeltedItem(Material material) {
        ItemStack result = null;
        switch (material) {
            case STONE:
                result = new ItemStack(Material.SMOOTH_STONE);
                break;
            case WHITE_TERRACOTTA:
                result = new ItemStack(Material.WHITE_GLAZED_TERRACOTTA);
                break;
            case ORANGE_TERRACOTTA:
                result = new ItemStack(Material.ORANGE_GLAZED_TERRACOTTA);
                break;
            case MAGENTA_TERRACOTTA:
                result = new ItemStack(Material.MAGENTA_GLAZED_TERRACOTTA);
                break;
            case LIGHT_BLUE_TERRACOTTA:
                result = new ItemStack(Material.LIGHT_BLUE_GLAZED_TERRACOTTA);
                break;
            case YELLOW_TERRACOTTA:
                result = new ItemStack(Material.YELLOW_GLAZED_TERRACOTTA);
                break;
            case LIME_TERRACOTTA:
                result = new ItemStack(Material.LIME_GLAZED_TERRACOTTA);
                break;
            case PINK_TERRACOTTA:
                result = new ItemStack(Material.PINK_GLAZED_TERRACOTTA);
                break;
            case GRAY_TERRACOTTA:
                result = new ItemStack(Material.GRAY_GLAZED_TERRACOTTA);
                break;
            case LIGHT_GRAY_TERRACOTTA:
                result = new ItemStack(Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
                break;
            case CYAN_TERRACOTTA:
                result = new ItemStack(Material.CYAN_GLAZED_TERRACOTTA);
                break;
            case PURPLE_TERRACOTTA:
                result = new ItemStack(Material.PURPLE_GLAZED_TERRACOTTA);
                break;
            case BLUE_TERRACOTTA:
                result = new ItemStack(Material.BLUE_GLAZED_TERRACOTTA);
                break;
            case BROWN_TERRACOTTA:
                result = new ItemStack(Material.BROWN_GLAZED_TERRACOTTA);
                break;
            case GREEN_TERRACOTTA:
                result = new ItemStack(Material.GREEN_GLAZED_TERRACOTTA);
                break;
            case RED_TERRACOTTA:
                result = new ItemStack(Material.RED_GLAZED_TERRACOTTA);
                break;
            case BLACK_TERRACOTTA:
                result = new ItemStack(Material.BLACK_GLAZED_TERRACOTTA);
                break;
            case ACACIA_LOG:
            case BIRCH_LOG:
            case DARK_OAK_LOG:
            case JUNGLE_LOG:
            case OAK_LOG:
            case SPRUCE_LOG:
            case ACACIA_WOOD:
            case BIRCH_WOOD:
            case DARK_OAK_WOOD:
            case JUNGLE_WOOD:
            case OAK_WOOD:
            case SPRUCE_WOOD:
                result = new ItemStack(Material.CHARCOAL);
                break;
            case COAL_ORE:
            case DEEPSLATE_COAL_ORE:
                result = new ItemStack(Material.COAL);
                break;
            case NETHERRACK:
                result = new ItemStack(Material.NETHER_BRICK);
                break;
            case SAND:
            case RED_SAND:
                result = new ItemStack(Material.GLASS);
                break;
            case COBBLESTONE:
                result = new ItemStack(Material.STONE);
                break;
            case CLAY:
                result = new ItemStack(Material.TERRACOTTA);
                break;
            case GOLD_ORE:
            case RAW_GOLD:
            case DEEPSLATE_GOLD_ORE:
                result = new ItemStack(Material.GOLD_INGOT);
                break;
            case IRON_ORE:
            case RAW_IRON:
            case DEEPSLATE_IRON_ORE:
                result = new ItemStack(Material.IRON_INGOT);
                break;
            case CACTUS:
                result = new ItemStack(Material.GREEN_DYE);
                break;
            case BAMBOO:
                result = new ItemStack(Material.BAMBOO);
                break;
            case SEA_PICKLE:
                result = new ItemStack(Material.LIME_DYE);
                break;
            case KELP:
                result = new ItemStack(Material.DRIED_KELP);
                break;
            case OBSIDIAN:
                result = new ItemStack(Material.CRYING_OBSIDIAN);
                break;
            case GRAVEL:
                result = new ItemStack(Material.FLINT);
                break;
            case SNOW_BLOCK:
                result = new ItemStack(Material.SNOWBALL);
                break;
            case WARPED_STEM:
            case STRIPPED_WARPED_STEM:
            case WARPED_HYPHAE:
            case STRIPPED_WARPED_HYPHAE:
                result = new ItemStack(Material.WARPED_WART_BLOCK);
                break;
            case COPPER_ORE:
            case DEEPSLATE_COPPER_ORE:
                result = new ItemStack(Material.COPPER_INGOT);
                break;
            case POTATOES:
                result = new ItemStack(Material.BAKED_POTATO);
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("SMELT");
        return names;
    }

    @Override
    public String getTemplate() {
        return "SMELT [generateEvent true or false]";
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
