package com.ssomar.score.api.executableblocks.config;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Manager of the ExecutableBlocks configurations: lookups by id or ItemStack,
 * listing, creation of block-item representations and load lifecycle.
 */
public interface ExecutableBlocksManagerInterface {

    /**
     * Check whether the ExecutableBlocks configurations have finished loading.
     * Before that, the lookup methods return empty results.
     * Listen to {@link com.ssomar.score.api.executableblocks.load.ExecutableBlocksPostLoadEvent}
     * to be notified when loading completes.
     *
     * @return true once all ExecutableBlocks configurations are loaded
     **/
    boolean isLoaded();

    /**
     * Get the ExecutableBlock representation of an ItemStack.
     * Check validity with {@link ExecutableBlockObjectInterface#isValid()}.
     *
     * @param itemStack the itemStack to read
     * @return the ExecutableBlockObject (invalid if the ItemStack is not an ExecutableBlock)
     **/
    @NotNull ExecutableBlockObjectInterface newExecutableBlockObject(@NotNull ItemStack itemStack);

    /**
     * Verify if id is a valid ExecutableBlock ID
     *
     * @param id The ID to verify
     * @return true if it is a valid ID, false otherwise
     **/
    boolean isValidID(String id);

    /**
     * Get an ExecutableBlock from its ID
     *
     * @param id The ID of the ExecutableBlock
     * @return The ExecutableBlock or an empty optional
     **/
    Optional<ExecutableBlockInterface> getExecutableBlock(String id);

    /**
     * Get an ExecutableBlock from its itemStack form
     *
     * @param itemStack The itemStack to get the ExecutableItem from
     * @return The ExecutableBlock or an empty optional
     **/
    Optional<ExecutableBlockInterface> getExecutableBlock(ItemStack itemStack);

    /**
     * Get all ExecutableBlocks Ids
     *
     * @return All ExecutableBlocks ids
     **/
    List<String> getExecutableBlockIdsList();

    /**
     * Get all ExecutableBlocks
     *
     * @return All ExecutableBlocks
     **/
    List<ExecutableBlockInterface> getAllExecutableBlocks();
}
