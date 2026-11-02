package com.ssomar.score.utils.backward_compatibility;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Inventory.getHolder() and Block.getState() build a full snapshot of the block entity on Paper
 * (copy of the whole NBT: a barrel full of full shulkers can freeze the main thread for seconds).
 * Paper (1.16+) has getHolder(false) / getState(false) that return the live holder / state without the copy.
 *
 * Only use these methods when the holder / state is READ (instanceof, getLocation, getInventory, getCustomName...).
 * If the state is modified then update(), keep the classic getState() snapshot.
 *
 * On Spigot (or old Paper) the methods don't exist: fallback on the classic calls.
 */
public class SnapshotUtils {

    private static final boolean HOLDER_NO_SNAPSHOT = hasMethod(Inventory.class, "getHolder", boolean.class);
    private static final boolean STATE_NO_SNAPSHOT = hasMethod(Block.class, "getState", boolean.class);

    static boolean hasMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            clazz.getMethod(name, parameterTypes);
            return true;
        } catch (NoSuchMethodException | SecurityException e) {
            return false;
        }
    }

    public static boolean isHolderNoSnapshotSupported() {
        return HOLDER_NO_SNAPSHOT;
    }

    public static boolean isStateNoSnapshotSupported() {
        return STATE_NO_SNAPSHOT;
    }

    /**
     * Same as inventory.getHolder() but without the block entity snapshot on Paper.
     */
    public static InventoryHolder getHolderNoSnapshot(Inventory inventory) {
        return getHolder(inventory, HOLDER_NO_SNAPSHOT);
    }

    /**
     * Same as block.getState() but without the block entity snapshot on Paper.
     */
    public static BlockState getStateNoSnapshot(Block block) {
        return getState(block, STATE_NO_SNAPSHOT);
    }

    static InventoryHolder getHolder(Inventory inventory, boolean noSnapshot) {
        if (noSnapshot) {
            try {
                return inventory.getHolder(false);
            } catch (AbstractMethodError e) {
                // Custom Inventory implementation compiled without the Paper method
            }
        }
        return inventory.getHolder();
    }

    static BlockState getState(Block block, boolean noSnapshot) {
        if (noSnapshot) {
            try {
                return block.getState(false);
            } catch (AbstractMethodError e) {
                // Custom Block implementation compiled without the Paper method
            }
        }
        return block.getState();
    }
}
