package com.ssomar.score.api.myfurniture.events;

import com.ssomar.score.api.myfurniture.config.placed.FurniturePlacedInterface;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a placed furniture is broken.
 * Cancelling the event prevents the break.
 */
@Getter
public class FurnitureBreakEvent extends Event implements Cancellable {

    /**
     * How the furniture was broken.
     */
    public enum BreakMethod {
        /**
         * Broken like a vanilla block (mining, explosion, ...).
         */
        NATURAL,
        /**
         * Broken by a MyFurniture mechanic (command, api, ...).
         */
        CUSTOM
    }

    private static final HandlerList handlers = new HandlerList();

    private final @Nullable Player player;

    private final FurniturePlacedInterface furniturePlaced;

    private final @Nullable Event sourceEvent;

    private final BreakMethod breakMethod;

    private boolean cancelled = false;

    /**
     * @param player          the player who broke the furniture, or null
     * @param furniturePlaced the broken furniture
     * @param sourceEvent     the Bukkit event at the origin of the break, if any
     * @param breakMethod     how the furniture was broken
     */
    public FurnitureBreakEvent(@Nullable Player player, @NotNull FurniturePlacedInterface furniturePlaced, @Nullable Event sourceEvent, BreakMethod breakMethod) {
        this.player = player;
        this.furniturePlaced = furniturePlaced;
        this.sourceEvent = sourceEvent;
        this.breakMethod = breakMethod;
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
