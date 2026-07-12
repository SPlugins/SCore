package com.ssomar.score.api.executableblocks.config.placed;

import com.ssomar.score.api.executableblocks.config.ExecutableBlockInterface;
import com.ssomar.score.api.executableblocks.config.ExecutableBlockObjectInterface;
import com.ssomar.score.sobject.InternalData;
import com.ssomar.score.utils.emums.VariableUpdateType;
import com.ssomar.score.utils.placeholders.StringPlaceholder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A placed ExecutableBlock in the world.
 */
public interface ExecutableBlockPlacedInterface {

    /**
     * Get the location of the placed block.
     *
     * @return the block location
     */
    Location getLocation();

    /**
     * Get the ExecutableBlock configuration of this placed block.
     *
     * @return the configuration
     */
    ExecutableBlockInterface getExecutableBlockConfig();

    /**
     * Get the internal data of this placed block (owner, usage, variables, ...).
     *
     * @return the internal data
     */
    InternalData getInternalData();

    /**
     * Break the placed block.
     *
     * @param player the player breaking the block, or null
     * @param drop   true to drop the block item
     */
    void breakBlock(@Nullable Player player, boolean drop);

    /**
     * Remove the placed block without breaking it (no drop, no break event).
     */
    void remove();

    /**
     * Update a variable stored on the placed block.
     *
     * @param variableName the variable name
     * @param value        the new value
     * @param type         how the value is applied
     * @return the resulting value
     */
    String updateVariable(String variableName, String value, VariableUpdateType type);

    /**
     * Set the remaining usage of the placed block.
     *
     * @param usage the new usage count
     */
    void updateUsage(int usage);

    /**
     * Get the remaining usage of the placed block.
     *
     * @return the usage count (-1 means infinite)
     */
    int getUsage();

    /**
     * Add or remove usage of the placed block.
     *
     * @param usageModification the modification to apply (can be negative)
     */
    void changeUsage(int usageModification);

    /**
     * Convert this placed block back to its item form, keeping its internal
     * data (usage, variables, owner).
     *
     * @return the ExecutableBlockObject of this placed block
     */
    ExecutableBlockObjectInterface toObject();

    /**
     * Move the placed block to another location.
     *
     * @param location             the destination
     * @param withDeleteAndReplace true to delete the physical block at the old location
     *                             and place it at the new one, false to only move the data
     */
    void moveBlock(Location location, boolean withDeleteAndReplace);

    /**
     * Check whether an entity is currently standing on the placed block.
     *
     * @return true if an entity is on the block
     */
    boolean hasEntityOn();

    /**
     * Get the icon of this placed block (its item form, e.g. for GUIs).
     *
     * @return the icon ItemStack
     */
    ItemStack getIconItem();

    /**
     * Get the description of this placed block (id, location, usage...).
     *
     * @return the description lines
     */
    List<String> getDescription();

    /**
     * Get the placeholders of this placed block (usable in SCore commands/texts).
     *
     * @return the placeholders
     */
    StringPlaceholder getPlaceholders();

    /**
     * Get the id of the ExecutableBlock configuration of this placed block.
     *
     * @return the ExecutableBlock id
     */
    default String getExecutableBlockId() {
        return getEB_ID();
    }

    /**
     * @deprecated use {@link #getExecutableBlockId()}
     */
    @Deprecated
    String getEB_ID();

}
