package com.ssomar.score.api.myfurniture;

import com.ssomar.score.api.executableitems.ExecutableItemsAPI;
import com.ssomar.score.api.executableitems.config.ExecutableItemsManagerInterface;
import com.ssomar.score.api.myfurniture.config.FurnitureManagerInterface;
import com.ssomar.score.api.myfurniture.config.FurnitureObjectInterface;
import com.ssomar.score.api.myfurniture.config.placed.FurniturePlacedManagerInterface;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Entry point of the public MyFurniture API.
 * <p>
 * The implementation is registered at runtime by the MyFurniture plugin when it enables.
 * To be sure the furniture configurations are loaded, listen to
 * {@link com.ssomar.score.api.myfurniture.load.MyFurniturePostLoadEvent} or check
 * {@link FurnitureManagerInterface#isLoaded()}.
 */
public class MyFurnitureAPI {

    private static FurnitureManagerInterface manager;
    private static FurniturePlacedManagerInterface placedManager;

    /**
     * Internal — called by the MyFurniture plugin on enable. Do not call.
     *
     * @param managerImpl       the manager implementation to expose through this API
     * @param placedManagerImpl the placed-furniture manager implementation to expose through this API
     */
    public static void register(@NotNull FurnitureManagerInterface managerImpl, @NotNull FurniturePlacedManagerInterface placedManagerImpl) {
        manager = managerImpl;
        placedManager = placedManagerImpl;
    }

    /**
     * Check whether the MyFurniture plugin is installed and enabled.
     *
     * @return true if the API is usable
     */
    public static boolean isEnabled() {
        return manager != null;
    }

    /**
     * Get the Furniture Manager.
     * It allows you to get / retrieve the furniture configurations.
     *
     * @return the manager
     * @throws IllegalStateException if the MyFurniture plugin is not installed/enabled yet
     */
    public static @NotNull FurnitureManagerInterface getFurnitureManager() {
        if (manager == null)
            throw new IllegalStateException("MyFurniture is not installed or not enabled yet (add it as a (soft)depend in your plugin.yml)");
        return manager;
    }

    /**
     * Get the FurniturePlaced Manager.
     * It allows you to get / retrieve the furniture placed in the world.
     *
     * @return the placed-furniture manager
     * @throws IllegalStateException if the MyFurniture plugin is not installed/enabled yet
     */
    public static @NotNull FurniturePlacedManagerInterface getFurniturePlacedManager() {
        if (placedManager == null)
            throw new IllegalStateException("MyFurniture is not installed or not enabled yet (add it as a (soft)depend in your plugin.yml)");
        return placedManager;
    }

    /**
     * Get the furniture representation of an ItemStack.
     * Be sure to check if the object is valid with {@link FurnitureObjectInterface#isValid()}.
     *
     * @param itemStack the itemStack to read
     * @return the FurnitureObjectInterface object (invalid if the ItemStack is not a furniture)
     */
    public static @NotNull FurnitureObjectInterface getFurnitureObject(@NotNull ItemStack itemStack) {
        return getFurnitureManager().newFurnitureObject(itemStack);
    }

    /**
     * @deprecated this facade is about MyFurniture — use
     * {@link ExecutableItemsAPI#getExecutableItemsManager()} for ExecutableItems
     */
    @Deprecated
    public static ExecutableItemsManagerInterface getExecutableItemsManager() {
        return ExecutableItemsAPI.getExecutableItemsManager();
    }

    /**
     * @deprecated misnamed — use {@link #getFurnitureObject(ItemStack)}
     */
    @Deprecated
    public static FurnitureObjectInterface getExecutableItemObject(ItemStack itemStack) {
        return getFurnitureObject(itemStack);
    }

}
