package com.ssomar.score.api.executableitems.config;

import com.ssomar.score.utils.emums.ResetSetting;
import com.ssomar.score.utils.emums.VariableUpdateType;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The ExecutableItem representation of a concrete ItemStack: gives access to the
 * per-item state (usage, variables) stored on the stack, next to its configuration.
 */
public interface ExecutableItemObjectInterface {

    /**
     * Whether the wrapped ItemStack is a valid ExecutableItem.
     * Always check this before using the other methods.
     *
     * @return true if the ItemStack is an ExecutableItem
     */
    boolean isValid();

    /**
     * Get the ExecutableItem configuration of this item.
     *
     * @return the configuration, or null if {@link #isValid()} is false
     */
    ExecutableItemInterface getConfig();

    /**
     * Get the wrapped ItemStack.
     *
     * @return the ItemStack this object was created from
     */
    ItemStack getItem();

    /**
     * Get the owner of this ExecutableItem (the player it was created for),
     * when the configuration restricts the item to its owner.
     *
     * @return the owner, or an empty optional if the item has no owner
     */
    Optional<OfflinePlayer> getOwnerPlayer();

    /**
     * Get the current values of the item variables.
     *
     * @return variable name to value map
     */
    Map<String, String> getVariablesValues();

    /**
     * Update a variable stored on the item.
     *
     * @param variableName the variable name
     * @param value        the new value
     * @param type         how the value is applied
     * @return the resulting value
     */
    String updateVariable(String variableName, String value, VariableUpdateType type);

    /**
     * Get the remaining usage of the item.
     *
     * @return the usage count
     */
    int getUsage();

    /**
     * Set the remaining usage of the item.
     *
     * @param usage the new usage count
     */
    void updateUsage(int usage);

    /**
     * Refresh the wrapped ItemStack against its configuration (name, lore, ...).
     */
    void refreshItem();

    /**
     * Refresh the wrapped ItemStack and reset the given settings.
     *
     * @param resetSettings the settings to reset
     * @return the refreshed ItemStack
     */
    ItemStack refresh(List<ResetSetting> resetSettings);
}
