package com.ssomar.score.api.custompiglinstrades.events;

import com.ssomar.score.api.custompiglinstrades.config.TradeInterface;
import lombok.Getter;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a player triggers a custom piglin trade, before its result runs.
 * Cancelling the event prevents the trade (the input item is not consumed).
 */
@Getter
public class PiglinTradeEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    /**
     * The player who triggered the trade, or null if not triggered by a player.
     */
    private final @Nullable Player player;

    /**
     * The piglin the trade is made with.
     */
    private final Piglin piglin;

    /**
     * The trade configuration being executed.
     */
    private final TradeInterface trade;

    /**
     * The item given to the piglin (the trade input).
     */
    private final ItemStack input;

    private boolean cancelled = false;

    /**
     * @param player the player who triggered the trade, or null
     * @param piglin the piglin the trade is made with
     * @param trade  the trade configuration
     * @param input  the input item of the trade
     */
    public PiglinTradeEvent(@Nullable Player player, @NotNull Piglin piglin, @NotNull TradeInterface trade, @NotNull ItemStack input) {
        this.player = player;
        this.piglin = piglin;
        this.trade = trade;
        this.input = input;
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
