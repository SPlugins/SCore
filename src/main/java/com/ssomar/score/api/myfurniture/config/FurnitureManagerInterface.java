package com.ssomar.score.api.myfurniture.config;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Manager of the MyFurniture configurations.
 */
public interface FurnitureManagerInterface {

    /**
     * Check whether the furniture configurations have finished loading.
     * Before that, the lookup methods return empty results.
     * Listen to {@link com.ssomar.score.api.myfurniture.load.MyFurniturePostLoadEvent}
     * to be notified when loading completes.
     *
     * @return true once all furniture configurations are loaded
     **/
    boolean isLoaded();

    /**
     * Verify if id is a valid furniture ID
     *
     * @param id The ID to verify
     * @return true if it is a valid ID, false otherwise
     **/
    boolean isValidID(String id);

    /**
     * Get a furniture configuration from its ID
     *
     * @param id The ID of the furniture
     * @return The furniture or an empty optional
     **/
    Optional<? extends FurnitureInterface> getFurniture(String id);

    /**
     * Get a furniture configuration from its itemStack form
     *
     * @param itemStack The itemStack to get the furniture from
     * @return The furniture or an empty optional
     **/
    Optional<? extends FurnitureInterface> getFurniture(ItemStack itemStack);

    /**
     * Get all furniture Ids
     *
     * @return All furniture ids
     **/
    List<String> getFurnitureIdsList();

    /**
     * Get all furniture configurations
     *
     * @return All furniture configurations
     **/
    List<? extends FurnitureInterface> getAllFurnitures();

    /**
     * Get the furniture representation of an ItemStack.
     * Check validity with {@link FurnitureObjectInterface#isValid()}.
     *
     * @param itemStack the itemStack to read
     * @return the FurnitureObject (invalid if the ItemStack is not a furniture)
     **/
    @NotNull FurnitureObjectInterface newFurnitureObject(@NotNull ItemStack itemStack);
}
