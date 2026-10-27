package com.ssomar.score.usedapi;

import com.ssomar.executableblocks.executableblocks.placedblocks.ExecutableBlockPlaced;
import com.ssomar.score.SCore;
import com.ssomar.score.SsomarDev;
import com.ssomar.score.api.executableblocks.ExecutableBlocksAPI;
import com.ssomar.score.api.executableblocks.config.placed.ExecutableBlockPlacedInterface;
import com.ssomar.score.utils.safebreak.SafeBreak;
import dev.lone.itemsadder.api.CustomBlock;
import dev.lone.itemsadder.api.CustomFurniture;
import dev.lone.itemsadder.api.CustomStack;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemsAdderAPI {

    /**
     * These following methods are not allowed for compilation by using JUnit testing because
     * using these methods will lead to potential recursion.<br/>
     * - {@link SafeBreak#breakEB(Player, Block, boolean)}<br/>
     * - {@link ExecutableBlockPlaced#breakBlock(Player, boolean, Event, ExecutableBlockPlaced.BreakMethod)}<br/>
     * - {@link ExecutableBlockPlaced#runBreakBlockAnimation()}<br/><br/>
     * <hr/><br/>
     * This method is used to try breaking an ItemsAdder ExecutableBlock properly.
     * @param block
     * @param item
     * @param drop
     * @return boolean value whether the breaking of the custom block succeeded.
     */
    public static boolean breakCustomBlock(Block block, ItemStack item, boolean drop) {
        SsomarDev.testMsg(ChatColor.GOLD+"[#s0003] breakCustomBlock() method call from ItemsAdderAPI.java", true);
        if (SCore.hasItemsAdder && block != null && !block.isEmpty()) {
            // https://discord.com/channels/701066025516531753/1386807735009677493 remove for custom IA build
            if (SCore.hasClass("dev.lone.itemsadder.api.CustomBlock")) {
                SsomarDev.testMsg("> [#s0004] Attempted to break ItemsAdder block", true);
                CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);

                // This checks if the provided block location is a solid ItemsAdder block.
                // For some reason, customBlock value is null if an Armor Stand is in that said location
                if (customBlock != null) {
                    SsomarDev.testMsg("> > [#s0005] Custom Block is not null", true);
                    if (drop) {
                        List<ItemStack> loots = customBlock.getLoot(item, false);
                        for (ItemStack loot : loots) {
                            block.getWorld().dropItemNaturally(block.getLocation(), loot);
                        }
                    }
                    customBlock.playBreakSound();
                    customBlock.playBreakEffect();
                    customBlock.playBreakParticles();
                    customBlock.remove();
                    Runnable runnable = new Runnable() {
                        public void run() {
                            block.setType(org.bukkit.Material.AIR);
                        }
                    };

                    if (SCore.hasExecutableBlocks) { // For proper practice
                        Optional<ExecutableBlockPlacedInterface> eBPOpt = ExecutableBlocksAPI.getExecutableBlocksPlacedManager().getExecutableBlockPlaced(block);
                        if (eBPOpt.isPresent()) {
                            SsomarDev.testMsg("> > > > [#s0031] EXECUTABLE_BLOCK detected on location, attempting to drop & delete", true);
                            ExecutableBlockPlaced eBP = (ExecutableBlockPlaced) eBPOpt.get();
                            tryToDropExecutableBlock(eBP, block, drop);
                            eBP.remove();
                        }
                    }

                    SCore.schedulerHook.runTask(runnable, 1);
                    return true;
                }
            }

            ArmorStand armorStand;
            SsomarDev.testMsg("> [#s0025] Start iterating through nearby entities "+block.getLocation().getWorld().getNearbyEntities(block.getLocation(), 0.5, 0.5, 0.5).size()+"; Block: "+block.getLocation(), true);
            for (Entity e : block.getLocation().getWorld().getNearbyEntities(block.getLocation(), 0.5, 0.5, 0.5)) {
                if (e instanceof ArmorStand) {
                    SsomarDev.testMsg("> [#s0006] Custom Block instanceof ArmorStand", true);
                    armorStand = (ArmorStand) e;
                    //SsomarDev.testMsg("ITEM ADDER DETECTED >> "+armorStand.getCustomName(), true);
                    if (armorStand.getCustomName() != null && armorStand.getCustomName().equals("ItemsAdder_furniture")) {
                        SsomarDev.testMsg("> > [#s0007] Armorstand has a name and ArmorStand name is \"ItemsAdder_furniture\". Target at: "+e.getLocation() , false);

                        // The getNearbyEntities() has the chance to select nearby furniture, and we do not want that.
                        // Instead, we will compare their XYZ location to check if their position is also at the dead center
                        // of the broken block.
                        if (e.getX() == block.getX()+0.5 && e.getY() == block.getY() && e.getZ() == block.getZ()+0.5) {
                            SsomarDev.testMsg("> > > [#s0027] Block finally removed", true);

                            CustomFurniture furniture = CustomFurniture.byAlreadySpawned(armorStand);
                            furniture.remove(false);
                            // To double-tap it. For some reason, in Paper 1.21.8, the remove() method from CustomFurniture didn't remove the furniture.
                            // Haven't fully figured out why but this serves as "Plan B".
                            // (Source: @raphael599 reported this weird behavior during the fix for this)
                            if (e != null) e.remove();

                            if (SCore.hasExecutableBlocks) {
                                // SsomarDev.testMsg("DEBUG SAFE BREAK has EB", DEBUG);
                                Optional<ExecutableBlockPlacedInterface> eBPOpt = ExecutableBlocksAPI.getExecutableBlocksPlacedManager().getExecutableBlockPlaced(block);
                                if (eBPOpt.isPresent()) {
                                    SsomarDev.testMsg("> > > > [#s0029] EXECUTABLE_BLOCK detected on location, attempting to drop:"+drop+" & delete", true);
                                    ExecutableBlockPlaced eBP = (ExecutableBlockPlaced) eBPOpt.get();
                                    tryToDropExecutableBlock(eBP, block, drop);
                                    eBP.remove();
                                }
                            }

                            return true;
                        } else {
                            SsomarDev.testMsg(ChatColor.RED+"> > [#s0028] FAIL: "+(e.getX() == block.getX()+0.5)+" "+(e.getY() == block.getY())+" "+(e.getZ() == block.getZ()+0.5), false);
                        }
                    } else {
                        if (armorStand.getCustomName() == null) {
                            SsomarDev.testMsg("[#s0008] Armorstand has no name", true);
                        } else {
                            SsomarDev.testMsg("[#s0009] Armorstand is not IA; name is: " + armorStand.getCustomName(), true);
                        }
                    }
                }
            }

        }
        return false;

    }

    /**
    * Handles the logic for dropping the ExecutableBlock block ItemStack to the world
    * <br/><br/>
    * By checking the option's value for <code>dropBlockIfItIsBroken</code> in the block config and
    * checking the boolean value of the drop argument provided by its method calls,
    * it can properly decide whether to drop the block or not when the player breaks
    * the block, uses <code>BREAK</code>/<code>REMOVE</code> block command and whatever the value of
    * <code>dropBlockIfItIsBroken</code> is.
     * <br/><br/>
     * (Side note: This was made because I don't want to explain why is this here twice in {@link ItemsAdderAPI#breakCustomBlock(Block, ItemStack, boolean)}. Made it as a
     * private static method because I don't see myself using it elsewhere -Special70)
     */
    private static void tryToDropExecutableBlock(ExecutableBlockPlaced eBP, Block block, boolean drop) {
        if (eBP.getExecutableBlock().getDropBlockIfItIsBroken().getValue() && drop)
            eBP.dropAtLocation(block.getLocation(), eBP.getExecutableBlock().getResetInternalDatasWhenBroken().getValue());
    }

    public static Optional<String> getCustomBlockID(Block block) {
        if (SCore.hasItemsAdder && block != null && !block.isEmpty()) {
            // https://discord.com/channels/701066025516531753/1386807735009677493 remove for custom IA build
            if (SCore.hasClass("dev.lone.itemsadder.api.CustomBlock")) {
                CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
                //SsomarDev.testMsg("ITEM ADDER DETECTED >> "+(customBlock != null), true);
                if (customBlock != null) {
                    return Optional.of(customBlock.getId());
                }
            }
            ArmorStand armorStand;
            for (Entity e : block.getLocation().getWorld().getNearbyEntities(block.getLocation(), 0.5, 0.5, 0.5)) {
                if (e instanceof ArmorStand) {
                    armorStand = (ArmorStand) e;
                    if (armorStand.getCustomName() != null && armorStand.getCustomName().equals("ItemsAdder_furniture")) {
                        return Optional.of(CustomFurniture.byAlreadySpawned(armorStand).getId());
                    }
                }
            }

        }
        return Optional.empty();

    }

    public static Optional<String> getCustomItemID(ItemStack item) {
        if (SCore.hasItemsAdder && item != null) {
            CustomStack customStack = CustomStack.byItemStack(item);
            //SsomarDev.testMsg("ITEM ADDER DETECTED >> "+(customBlock != null), true);
            if (customStack != null) {
                return Optional.of(customStack.getId());
            }
        }
        return Optional.empty();

    }

    public static boolean isCustomBlock(Block block) {
        return getCustomBlockID(block).isPresent();
    }


    public static boolean isCustomItem(ItemStack item) {
        return getCustomItemID(item).isPresent();
    }

    public static boolean placeItemAdder(Location location, String id) {
        try {
            CustomBlock customBlock = CustomBlock.getInstance(id);
            if (customBlock != null) {
                //SsomarDev.testMsg("placeItemsAdder Block: " + id, DEBUG);
                customBlock.place(location);
                return true;
            }
        } catch (Exception | Error e) {
            try {
                //SsomarDev.testMsg("placeItemsAdder is Furniture " + id, DEBUG);
                CustomFurniture.spawnPreciseNonSolid(id, location);
                return true;
            } catch (Exception e1) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
