package com.ssomar.score.api.executableblocks.config;

import com.ssomar.score.sobject.InternalData;
import com.ssomar.score.utils.emums.VariableUpdateType;

/**
 * The ExecutableBlock representation of a concrete ItemStack: gives access to the
 * per-item state (usage, variables) stored on the stack, next to its configuration.
 */
public interface ExecutableBlockObjectInterface {

    /**
     * Whether the wrapped ItemStack is a valid ExecutableBlock.
     * Always check this before using the other methods.
     *
     * @return true if the ItemStack is an ExecutableBlock
     */
    boolean isValid();

    /**
     * Get the ExecutableBlock configuration of this item.
     *
     * @return the configuration, or null if {@link #isValid()} is false
     */
    ExecutableBlockInterface getConfig();

    /**
     * Get the internal data of this item (owner, usage, variables, ...).
     *
     * @return the internal data
     */
    InternalData getInternalData();

    /**
     * Update a variable stored on the item.
     *
     * @param variableName the variable name
     * @param value        the new value
     * @param type         how the value is applied
     * @return the resulting value
     */
    String updateVariable(String variableName, String value, VariableUpdateType type);

}
