package com.ssomar.score.api.executablecrafting.events;

import com.ssomar.score.api.executablecrafting.config.RecipeInterface;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a player crafts an item with a custom ExecutableCrafting recipe,
 * before the ingredients are consumed and the result is given.
 * Cancelling the event aborts the craft. The result ItemStack is the exact stack
 * that will be given — modifications to it apply.
 */
@Getter
public class ExecutableCraftingCraftEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    /**
     * The player who crafts.
     */
    private final Player player;

    /**
     * The recipe configuration being used.
     */
    private final RecipeInterface recipe;

    /**
     * The result stack that will be given (amount included). Mutable.
     */
    private final ItemStack result;

    private boolean cancelled = false;

    /**
     * @param player the player who crafts
     * @param recipe the recipe configuration
     * @param result the result stack that will be given
     */
    public ExecutableCraftingCraftEvent(@NotNull Player player, @NotNull RecipeInterface recipe, @NotNull ItemStack result) {
        this.player = player;
        this.recipe = recipe;
        this.result = result;
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

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
