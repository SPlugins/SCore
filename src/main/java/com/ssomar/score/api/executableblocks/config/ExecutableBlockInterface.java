package com.ssomar.score.api.executableblocks.config;

import com.ssomar.score.api.executableblocks.config.placed.ExecutableBlockPlacedInterface;
import com.ssomar.score.sobject.*;
import com.ssomar.score.utils.place.OverrideMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * An ExecutableBlock configuration: everything defined in the block's configuration file,
 * with helpers to build its item, manage cooldowns and place it in the world.
 * <p>
 * Obtain instances through
 * {@link com.ssomar.score.api.executableblocks.ExecutableBlocksAPI#getExecutableBlocksManager()}.
 */
public interface ExecutableBlockInterface extends SObjectInterface, SObjectWithActivators, SObjectBuildable, SObjectWithVariables {


    /**
     * @param player    The player to whom you want to check the possession of the permission
     * @param showError true if you want to show an error message to the player if he doesn't have the permission
     * @return The name of the ExecutableBlock
     */
    boolean hasBlockPerm(@NotNull Player player, boolean showError);


    /**
     * Put this ExecutableBlock in cooldown for a player (all activators).
     *
     * @param player    the player to apply the cooldown to
     * @param cooldown  the cooldown duration
     * @param isInTicks true if the duration is in ticks, false for seconds
     */
    void addCooldown(Player player, int cooldown, boolean isInTicks);

    /**
     * Put a specific activator of this ExecutableBlock in cooldown for a player.
     *
     * @param player      the player to apply the cooldown to
     * @param cooldown    the cooldown duration
     * @param isInTicks   true if the duration is in ticks, false for seconds
     * @param activatorID the activator to put in cooldown
     */
    void addCooldown(Player player, int cooldown, boolean isInTicks, String activatorID);

    /**
     * Get the description (lore) of the ExecutableBlock configuration.
     *
     * @return the description lines
     */
    List<String> getDescription();

    /**
     * Get the display name of the ExecutableBlock configuration.
     *
     * @return the display name
     */
    String getDisplayName();

    /**
     * Get the icon of this ExecutableBlock (its built item form, e.g. for GUIs).
     *
     * @return the icon ItemStack
     */
    ItemStack getIconItem();

    /**
     * Drop the item of this ExecutableBlock on the ground.
     *
     * @param location where to drop the item
     * @param amount   the amount to drop
     * @return the dropped item entity
     */
    Item dropItem(Location location, int amount);

    Optional<ExecutableBlockPlacedInterface> place(@NotNull Location location, boolean placeBlock, OverrideMode overrideMode, @Nullable Entity placer, @Nullable InternalData overrideInternalData);

}
