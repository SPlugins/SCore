package com.ssomar.score.api.executableitems.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when an ExecutableItem is removed from a player's inventory
 * (for example when a consumable ExecutableItem runs out of usage).
 */
public class RemoveItemInPlayerInventoryEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    @Getter
    private final ItemStack item;
    @Getter
    private final int slot;


    /**
     * @param player the player whose inventory lost the item
     * @param item   the item removed from the inventory
     * @param slot   the inventory slot the item was removed from
     */
    public RemoveItemInPlayerInventoryEvent(final Player player, ItemStack item, int slot) {
        super(player);
        this.item = item;
        this.slot = slot;
    }

    /**
     * Gets a list of handlers handling this event.
     *
     * @return A list of handlers handling this event.
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Gets a list of handlers handling this event.
     *
     * @return A list of handlers handling this event.
     */
    @Override
    public final @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
