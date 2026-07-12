package com.ssomar.score.api.executableitems.config;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Manager of the ExecutableItems configurations: lookups by id or ItemStack,
 * listing, creation of item representations and load lifecycle.
 */
public interface ExecutableItemsManagerInterface {

    /**
     * Check whether the ExecutableItems configurations have finished loading.
     * Before that, the lookup methods return empty results.
     * Listen to {@link com.ssomar.score.api.executableitems.load.ExecutableItemsPostLoadEvent}
     * to be notified when loading completes.
     *
     * @return true once all ExecutableItems configurations are loaded
     **/
    boolean isLoaded();

    /**
     * Get the ExecutableItem representation of an ItemStack.
     * Check validity with {@link ExecutableItemObjectInterface#isValid()}.
     *
     * @param itemStack the itemStack to read
     * @return the ExecutableItemObject (invalid if the ItemStack is not an ExecutableItem)
     **/
    @NotNull ExecutableItemObjectInterface newExecutableItemObject(@NotNull ItemStack itemStack);

    /**
     * Register a new ExecutableItem configuration created from an ItemStack.
     *
     * @param itemStack the itemStack to convert into an ExecutableItem configuration
     * @param id        the id of the new ExecutableItem
     * @param folder    the folder where the ExecutableItem will be saved (ex: "custom/")
     * @return the created ExecutableItem configuration
     **/
    @NotNull Optional<ExecutableItemInterface> registerNewExecutableItem(@NotNull ItemStack itemStack, @NotNull String id, @NotNull String folder);

    /**
     * Verify if id is a valid ExecutableItem ID
     *
     * @param id The ID to verify
     * @return true if it is a valid ID, false otherwise
     **/
    boolean isValidID(String id);

    /**
     * Get an ExecutableItem from its ID
     *
     * @param id The ID of the ExecutableItem
     * @return The ExecutableItem or an empty optional
     **/
    Optional<ExecutableItemInterface> getExecutableItem(String id);

    /**
     * Get an ExecutableItem from its itemStack form
     *
     * @param itemStack The itemStack to get the ExecutableItem from
     * @return The ExecutableItem or an empty optional
     **/
    Optional<ExecutableItemInterface> getExecutableItem(ItemStack itemStack);

    /**
     * Get an ExecutableItem from its itemStack form, restricted to a whitelist of ids.
     * Faster than {@link #getExecutableItem(ItemStack)} when scanning many items:
     * custom (lore/name based) recognition only runs for the whitelisted ids.
     *
     * @param itemStack     The itemStack to get the ExecutableItem from
     * @param whiteListedID the only ids to consider (empty list = all ids)
     * @return The ExecutableItem or null
     **/
    @Nullable ExecutableItemInterface getExecutableItem(ItemStack itemStack, List<String> whiteListedID);

    /**
     * Get all ExecutableItems Ids
     *
     * @return All ExecutableItems ids
     **/
    List<String> getExecutableItemIdsList();

    /**
     * Get all ExecutableItems
     *
     * @return All ExecutableItems
     **/
    List<ExecutableItemInterface> getAllExecutableItems();
}
