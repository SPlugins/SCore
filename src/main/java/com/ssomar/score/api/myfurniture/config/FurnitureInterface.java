package com.ssomar.score.api.myfurniture.config;

import com.ssomar.score.api.myfurniture.config.placed.FurniturePlacedInterface;
import com.ssomar.score.features.types.UncoloredStringFeature;
import com.ssomar.score.sobject.*;
import com.ssomar.score.utils.place.OverrideMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A MyFurniture configuration: everything defined in the furniture's configuration file,
 * with helpers to build its item, check placement permissions and place it in the world.
 * <p>
 * Obtain instances through
 * {@link com.ssomar.score.api.myfurniture.MyFurnitureAPI#getFurnitureManager()}.
 */
public interface FurnitureInterface extends SObjectInterface, SObjectWithActivators, SObjectBuildable, SObjectWithVariables {
    /**
     * @param player    The player to whom you want to check the possession of the permission
     * @param showError true if you want to show an error message to the player if he doesn't have the permission
     * @return true if the player has the permission of this furniture
     */
    boolean hasFurniturePerm(@NotNull Player player, boolean showError);

    /**
     * Check whether a player is allowed to place this furniture at a location
     * (protection plugins, limits, ...).
     *
     * @param player                the player to check
     * @param location              the target location
     * @param showErrorToThePLayer  true to send an error message to the player when not allowed
     * @return true if the player can place the furniture there
     */
    boolean checkIfPlayerCanPlaceAt(@NotNull Player player, @NotNull Location location, boolean showErrorToThePLayer);

    /**
     * Check whether a player is allowed to break this furniture at a location.
     *
     * @param player                the player to check
     * @param location              the target location
     * @param showErrorToThePLayer  true to send an error message to the player when not allowed
     * @return true if the player can break the furniture there
     */
    boolean checkIfPlayerCanBreakAt(@NotNull Player player, @NotNull Location location, boolean showErrorToThePLayer);

    /**
     * Put this furniture in cooldown for a player (all activators).
     *
     * @param player    the player to apply the cooldown to
     * @param cooldown  the cooldown duration
     * @param isInTicks true if the duration is in ticks, false for seconds
     */
    void addCooldown(Player player, int cooldown, boolean isInTicks);

    /**
     * Put a specific activator of this furniture in cooldown for a player.
     *
     * @param player      the player to apply the cooldown to
     * @param cooldown    the cooldown duration
     * @param isInTicks   true if the duration is in ticks, false for seconds
     * @param activatorID the activator to put in cooldown
     */
    void addCooldown(Player player, int cooldown, boolean isInTicks, String activatorID);

    /**
     * Get the item model of this furniture.
     *
     * @return the item model feature
     */
    UncoloredStringFeature getItemModel();

    /**
     * Get the icon of this furniture (its built item form, e.g. for GUIs).
     *
     * @return the icon ItemStack
     */
    ItemStack getIconItem();

    /**
     * Get the description of the furniture configuration.
     *
     * @return the description lines
     */
    List<String> getDescription();

    /**
     * Place this furniture in the world.
     *
     * @param location             where to place the furniture
     * @param override             how to handle an existing block/furniture at the location
     * @param placer               the entity placing the furniture, or null
     * @param overrideInternalData internal data to apply to the placed furniture, or null
     * @return the placed furniture, or an empty optional if the placement failed
     */
    Optional<? extends FurniturePlacedInterface> place(@NotNull Location location, OverrideMode override, @Nullable Entity placer, @Nullable InternalData overrideInternalData);
}
