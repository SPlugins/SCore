package com.ssomar.score.api.executableblocks;

import com.ssomar.score.api.executableblocks.config.ExecutableBlockObjectInterface;
import com.ssomar.score.api.executableblocks.config.ExecutableBlocksManagerInterface;
import com.ssomar.score.api.executableblocks.config.placed.ExecutableBlocksPlacedManagerInterface;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Entry point of the public ExecutableBlocks API.
 * <p>
 * The implementation is registered at runtime by the ExecutableBlocks plugin when it enables.
 * To be sure the blocks are loaded, listen to
 * {@link com.ssomar.score.api.executableblocks.load.ExecutableBlocksPostLoadEvent} or check
 * {@link ExecutableBlocksManagerInterface#isLoaded()}.
 */
public class ExecutableBlocksAPI {

    private static ExecutableBlocksManagerInterface manager;
    private static ExecutableBlocksPlacedManagerInterface placedManager;

    /**
     * Internal — called by the ExecutableBlocks plugin on enable. Do not call.
     *
     * @param managerImpl       the manager implementation to expose through this API
     * @param placedManagerImpl the placed-blocks manager implementation to expose through this API
     */
    public static void register(@NotNull ExecutableBlocksManagerInterface managerImpl, @NotNull ExecutableBlocksPlacedManagerInterface placedManagerImpl) {
        manager = managerImpl;
        placedManager = placedManagerImpl;
    }

    /**
     * Check whether the ExecutableBlocks plugin is installed and enabled.
     *
     * @return true if the API is usable
     */
    public static boolean isEnabled() {
        return manager != null;
    }

    /**
     * Get the ExecutableBlocks Manager.
     * It allows you to get / retrieve the ExecutableBlocks configurations.
     *
     * @return the manager
     * @throws IllegalStateException if the ExecutableBlocks plugin is not installed/enabled yet
     */
    public static @NotNull ExecutableBlocksManagerInterface getExecutableBlocksManager() {
        if (manager == null)
            throw new IllegalStateException("ExecutableBlocks is not installed or not enabled yet (add it as a (soft)depend in your plugin.yml)");
        return manager;
    }

    /**
     * Get the ExecutableBlocksPlaced Manager.
     * It allows you to get / retrieve the ExecutableBlocks placed in the world.
     *
     * @return the placed-blocks manager
     * @throws IllegalStateException if the ExecutableBlocks plugin is not installed/enabled yet
     */
    public static @NotNull ExecutableBlocksPlacedManagerInterface getExecutableBlocksPlacedManager() {
        if (placedManager == null)
            throw new IllegalStateException("ExecutableBlocks is not installed or not enabled yet (add it as a (soft)depend in your plugin.yml)");
        return placedManager;
    }

    /**
     * Get the ExecutableBlock representation of an ItemStack.
     * Be sure to check if the object is valid with {@link ExecutableBlockObjectInterface#isValid()}.
     *
     * @param itemStack the itemStack to read
     * @return the ExecutableBlockObjectInterface object (invalid if the ItemStack is not an ExecutableBlock)
     */
    public static @NotNull ExecutableBlockObjectInterface getExecutableBlockObject(@NotNull ItemStack itemStack) {
        return getExecutableBlocksManager().newExecutableBlockObject(itemStack);
    }
}
