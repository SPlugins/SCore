package com.ssomar.score.api.myfurniture.config;

import com.ssomar.score.sobject.InternalData;

/**
 * The furniture representation of a concrete ItemStack: gives access to the
 * per-item state stored on the stack, next to its configuration.
 */
public interface FurnitureObjectInterface {

    /**
     * Whether the wrapped ItemStack is a valid furniture item.
     * Always check this before using the other methods.
     *
     * @return true if the ItemStack is a furniture item
     */
    boolean isValid();

    /**
     * Get the internal data of this item (owner, usage, variables, ...).
     *
     * @return the internal data
     */
    InternalData getInternalData();

    /**
     * Get the furniture configuration of this item.
     *
     * @return the configuration, or null if {@link #isValid()} is false
     */
    FurnitureInterface getFurnitureConfig();
}
