package com.ssomar.score.commands.runnable.block.commands;

import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.commands.runnable.ActionInfo;
import com.ssomar.score.commands.runnable.SCommandToExec;
import com.ssomar.score.commands.runnable.block.BlockCommand;
import com.ssomar.score.events.BlockBreakEventExtension;
import com.ssomar.score.utils.ToolsListMaterial;
import com.ssomar.score.utils.safebreak.SafeBreak;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/* FARMINCUBE {radius} {ActiveDrop true or false} {onlyMaxAge true or false} {replant true or false}*/
public class FarmInCube extends BlockCommand {

    private static final boolean DEBUG = false;

    /**
     * Process a single block for destruction and optional replanting.
     * This method is called from the batched task to process each block.
     */
    private static void processBlock(Block toDestroy, boolean onlyMaxAge, boolean drop, boolean replant,
                                     @Nullable UUID playerUuid, @Nullable Player player, boolean event, int slot,
                                     List<Material> validMaterials) {
        BlockData data = toDestroy.getBlockData();
        Material bMat = toDestroy.getType();

        if (onlyMaxAge && data instanceof Ageable) {
            Ageable ageable = (Ageable) data;
            if (ageable.getAge() != ageable.getMaximumAge()) return;
        }

        if (validMaterials.contains(bMat)) {
            // Clone data only when we need to modify it for replanting
            BlockData dataForReplant = replant ? data.clone() : null;
            if (!SafeBreak.breakBlockWithEvent(toDestroy, playerUuid, slot, drop, event, true, BlockBreakEventExtension.BreakCause.MINE_IN_CUBE)) return;
            if (replant && dataForReplant != null) {
                replant(toDestroy, dataForReplant, bMat, player);
            }
        }
    }

    public static void replant(Block block, BlockData oldData, Material material, @Nullable Player player) {
        if (!(oldData instanceof Ageable)) {
            block.setType(Material.AIR);
            return;
        }

        Ageable ageable = (Ageable) oldData;
        Material required = ToolsListMaterial.getInstance().getRealMaterialOfBlock(material);

        boolean needReplant = false;
        if (player != null) {
            // removeItem returns empty map if successful, no need for contains() check
            if (player.getInventory().removeItem(new ItemStack(required)).isEmpty()) {
                needReplant = true;
            } else {
                block.setType(Material.AIR);
            }
        } else {
            needReplant = true;
        }

        if (needReplant) {
            ageable.setAge(0);
            // setBlockData will set the type implicitly, no need for setType() first
            block.setBlockData(oldData);
        }
    }

    @Override
    public void run(Player p, @NotNull Block block, SCommandToExec sCommandToExec) {
        List<String> args = sCommandToExec.getOtherArgs();
        ActionInfo aInfo = sCommandToExec.getActionInfo();
        Material oldMaterial = aInfo.getOldBlockMaterial();

        if (aInfo.isEventFromCustomBreakCommand()) return;

        // Cache the valid materials list once
        final List<Material> validMaterials = ToolsListMaterial.getInstance().getPlantWithGrowth();

        try {
            int radius = Integer.parseInt(args.get(0));

            boolean drop = true;
            if (args.size() >= 2) drop = Boolean.parseBoolean(args.get(1));

            boolean onlyMaxAge = true;
            if (args.size() >= 3) onlyMaxAge = Boolean.parseBoolean(args.get(2));

            boolean replant = false;
            if (args.size() >= 4) replant = Boolean.parseBoolean(args.get(3));

            boolean event = false;
            if (args.size() >= 5) event = Boolean.parseBoolean(args.get(4));

            if (radius >= 10) radius = 9;

            // Cache world reference and coordinates to avoid repeated method calls
            final World world = block.getWorld();
            final int baseX = block.getX();
            final int baseY = block.getY();
            final int baseZ = block.getZ();

            // Collect all blocks to process first (filter early using cached validMaterials)
            final List<Block> blocksToProcess = new ArrayList<>();
            for (int y = -radius; y <= radius; y++) {
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        Block toDestroy = world.getBlockAt(baseX + x, baseY + y, baseZ + z);
                        // Early filter: only add blocks that are valid plant materials
                        if (validMaterials.contains(toDestroy.getType())) {
                            blocksToProcess.add(toDestroy);
                        }
                    }
                }
            }

            // Process all blocks in a single scheduled task instead of thousands of individual tasks
            if (!blocksToProcess.isEmpty()) {
                final boolean dropFinal = drop;
                final boolean onlyMaxAgeFinal = onlyMaxAge;
                final boolean replantFinal = replant;
                final boolean eventFinal = event;
                final int slot = aInfo.getSlot();
                final UUID playerUuid = p != null ? p.getUniqueId() : null;

                Runnable batchTask = () -> {
                    for (Block toDestroy : blocksToProcess) {
                        processBlock(toDestroy, onlyMaxAgeFinal, dropFinal, replantFinal,
                                playerUuid, p, eventFinal, slot, validMaterials);
                    }
                };
                SCore.schedulerHook.runLocationTask(batchTask, block.getLocation(), 1);
            }

            SsomarDev.testMsg("OldMaterial : " + oldMaterial.toString(), DEBUG);
            if (validMaterials.contains(oldMaterial) && replant) {
                final boolean onlyMaxAgeFinal = onlyMaxAge;
                final boolean dropFinal = drop;
                final boolean eventFinal = event;
                final UUID uuidFinal = p != null ? p.getUniqueId() : null;

                Runnable runnable = () -> {
                    BlockData data = block.getBlockData();

                    if (onlyMaxAgeFinal && data instanceof Ageable) {
                        Ageable ageable = (Ageable) data;
                        if (ageable.getAge() != ageable.getMaximumAge()) return;
                    }

                    // Not break (PLAYER_RIGHT_CLICK) so need to break it
                    if (!block.getType().equals(Material.AIR)) {
                        if (!SafeBreak.breakBlockWithEvent(block, uuidFinal, aInfo.getSlot(), dropFinal, eventFinal, true, BlockBreakEventExtension.BreakCause.MINE_IN_CUBE)) return;
                    }
                    block.setType(oldMaterial);
                    BlockData newData = block.getBlockData();
                    if (aInfo.getBlockFace() != null && newData instanceof Directional) {
                        Directional directional = (Directional) newData;
                        directional.setFacing(aInfo.getBlockFace());
                        block.setBlockData(directional);
                    }
                    replant(block, block.getBlockData().clone(), oldMaterial, p);
                };
                SCore.schedulerHook.runLocationTask(runnable, block.getLocation(), 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<String> verify(List<String> args, boolean isFinalVerification) {
        return Optional.empty();
    }

    @Override
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        names.add("FARMINCUBE");
        return names;
    }

    @Override
    public String getTemplate() {
        return "FARMINCUBE {radius} [ActiveDrop true or false] [onlyMaxAge true or false] [replant true or false] [event true or false]";
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
