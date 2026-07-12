package com.ssomar.score.api.myfurniture.config.placed;

import com.ssomar.score.api.myfurniture.config.FurnitureInterface;
import com.ssomar.score.api.myfurniture.config.FurnitureObjectInterface;
import com.ssomar.score.sobject.InternalData;
import com.ssomar.score.utils.emums.VariableUpdateType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A furniture placed in the world.
 */
public interface FurniturePlacedInterface {

    /**
     * Get the id of the furniture configuration of this placed furniture.
     *
     * @return the furniture id
     */
    String getFurnitureID();

    /**
     * Get the furniture configuration of this placed furniture.
     *
     * @return the configuration, or null if it is not loaded anymore
     */
    FurnitureInterface getFurniture();

    /**
     * Get the location of the placed furniture.
     *
     * @return the location
     */
    Location getLocation();

    /**
     * Get the internal data of this placed furniture (owner, usage, variables, ...).
     *
     * @return the internal data
     */
    InternalData getInternalData();

    /**
     * Get the remaining usage of the placed furniture.
     *
     * @return the usage count
     */
    int getUsage();

    /**
     * Set the remaining usage of the placed furniture.
     *
     * @param usage the new usage count
     */
    void updateUsage(int usage);

    /**
     * Update a variable stored on the placed furniture.
     *
     * @param variableName the variable name
     * @param value        the new value
     * @param type         how the value is applied
     * @return the resulting value
     */
    String updateVariable(String variableName, String value, VariableUpdateType type);

    /**
     * Break the placed furniture.
     *
     * @param player the player breaking the furniture, or null
     * @param drop   true to drop the furniture item
     */
    void breakFurniture(@Nullable Player player, boolean drop);

    /**
     * Remove the placed furniture without breaking it (no drop, no break event).
     */
    void remove();

    /**
     * Get the description of the placed furniture.
     *
     * @return the description lines
     */
    List<String> getDescription();

    /**
     * Get the icon of this placed furniture (its item form, e.g. for GUIs).
     *
     * @return the icon ItemStack
     */
    ItemStack getIconItem();

    /**
     * Move the placed furniture to another location.
     *
     * @param location the destination
     */
    void moveFurniture(Location location);

    /**
     * Convert this placed furniture back to its item form, keeping its internal data.
     *
     * @return the FurnitureObject of this placed furniture
     */
    FurnitureObjectInterface toObject();

    /**
     * Hide the placed furniture for a specific player (client side only).
     *
     * @param player the player to hide the furniture from
     */
    void hideFurniture(@NotNull Player player);

    /**
     * Show the placed furniture again for a specific player.
     *
     * @param player the player to show the furniture to
     */
    void showFurniture(@NotNull Player player);

    /**
     * Run an animation of the furniture model.
     *
     * @param bbmodelFileName the blockbench model file name
     * @param animationName   the animation name inside the model
     */
    void runAnimation(String bbmodelFileName, String animationName);

    /**
     * Stop the currently running animation of this placed furniture.
     */
    void stopAnimation();

    /**
     * Check whether an animation is currently running on this placed furniture.
     *
     * @return true if an animation is running
     */
    boolean isAnimationRunning();
}
