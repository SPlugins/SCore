package com.ssomar.score.sobject;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * A SsomarDev object that can be built into an ItemStack.
 */
public interface SObjectBuildable {

    /**
     * Build the ItemStack of this object with pre-computed internal data.
     *
     * @param quantity     the stack amount
     * @param internalData the internal data (owner, usage, variables...) to apply, or null for defaults
     * @return the built ItemStack
     */
    ItemStack buildItem(int quantity, @Nullable InternalData internalData);

    /**
     * Build the ItemStack of this object.
     *
     * @param quantity   the stack amount
     * @param creatorOpt the optional player considered as the owner/creator of the item
     * @return the built ItemStack
     */
    ItemStack buildItem(int quantity, Optional<Player> creatorOpt);

    /**
     * Build the ItemStack of this object with custom settings.
     *
     * @param quantity   the stack amount
     * @param creatorOpt the optional player considered as the owner/creator of the item
     * @param settings   build settings — supported keys: {@code "Variables"} (Map&lt;String, String&gt;
     *                   of variable id to value) and {@code "Usage"} (Integer)
     * @return the built ItemStack
     */
    ItemStack buildItem(int quantity, Optional<Player> creatorOpt,  Map<String, Object> settings);

    /**
     * Drop the item of this object on the ground.
     *
     * @param location where to drop the item
     * @param amount   the stack amount
     * @return the dropped item entity
     */
    Item dropItem(Location location, int amount);

    /**
     * Drop the item of this object on the ground, with custom settings.
     *
     * @param location   where to drop the item
     * @param amount     the stack amount
     * @param creatorOpt the optional player considered as the owner/creator of the item
     * @param settings   build settings (see {@link #buildItem(int, Optional, Map)})
     * @return the dropped item entity
     */
    Item dropItem(Location location, int amount, Optional<Player> creatorOpt,  Map<String, Object> settings);

    /**
     * Whether the built items can stack together.
     *
     * @return true if the item is stackable
     */
    boolean canBeStacked();

    /**
     * Get the display name of the item of this object.
     *
     * @return the item name
     */
    String getItemName();
}

