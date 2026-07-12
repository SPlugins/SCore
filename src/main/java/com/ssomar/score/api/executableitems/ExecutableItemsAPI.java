package com.ssomar.score.api.executableitems;

import com.ssomar.score.api.executableitems.config.ExecutableItemInterface;
import com.ssomar.score.api.executableitems.config.ExecutableItemObjectInterface;
import com.ssomar.score.api.executableitems.config.ExecutableItemsManagerInterface;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Entry point of the public ExecutableItems API.
 * <p>
 * The implementation is registered at runtime by the ExecutableItems plugin when it enables.
 * To be sure the items are loaded, listen to
 * {@link com.ssomar.score.api.executableitems.load.ExecutableItemsPostLoadEvent} or check
 * {@link ExecutableItemsManagerInterface#isLoaded()}.
 */
public class ExecutableItemsAPI {

    private static ExecutableItemsManagerInterface manager;

    /**
     * Internal — called by the ExecutableItems plugin on enable. Do not call.
     *
     * @param managerImpl the manager implementation to expose through this API
     */
    public static void register(@NotNull ExecutableItemsManagerInterface managerImpl) {
        manager = managerImpl;
    }

    /**
     * Get the ExecutableItems Manager.
     * It allows you to get / retrieve the ExecutableItems configurations.
     *
     * @return the manager
     * @throws IllegalStateException if the ExecutableItems plugin is not installed/enabled yet
     */
    public static @NotNull ExecutableItemsManagerInterface getExecutableItemsManager() {
        if (manager == null)
            throw new IllegalStateException("ExecutableItems is not installed or not enabled yet (add it as a (soft)depend in your plugin.yml)");
        return manager;
    }

    /**
     * Check whether the ExecutableItems plugin is installed and enabled.
     *
     * @return true if the API is usable
     */
    public static boolean isEnabled() {
        return manager != null;
    }

    /**
     * Get the ExecutableItem representation of an ItemStack.
     * Be sure to check if the object is valid with {@link ExecutableItemObjectInterface#isValid()}.
     *
     * @param itemStack the itemStack to read
     * @return the ExecutableItemObjectInterface object (invalid if the ItemStack is not an ExecutableItem)
     */
    public static @NotNull ExecutableItemObjectInterface getExecutableItemObject(@NotNull ItemStack itemStack) {
        return getExecutableItemsManager().newExecutableItemObject(itemStack);
    }

    /**
     * Register a new ExecutableItem configuration created from an ItemStack.
     *
     * @param itemStack the itemStack to convert into an ExecutableItem configuration
     * @param id        the id of the new ExecutableItem
     * @param folder    the folder where the ExecutableItem will be saved (ex: "custom/")
     * @return the created ExecutableItem configuration
     */
    public static @NotNull Optional<ExecutableItemInterface> registerNewExecutableItem(@NotNull ItemStack itemStack, @NotNull String id, @NotNull String folder) {
        return getExecutableItemsManager().registerNewExecutableItem(itemStack, id, folder);
    }

    /**
     * @deprecated misnamed — use {@link #registerNewExecutableItem(ItemStack, String, String)}
     */
    @Deprecated
    public static ExecutableItemInterface registerNewExecutableItemObject(ItemStack itemStack, String id, String folder) {
        return registerNewExecutableItem(itemStack, id, folder).orElse(null);
    }

}
