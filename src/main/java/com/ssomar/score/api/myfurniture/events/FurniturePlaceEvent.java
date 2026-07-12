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
 * Fired when a furniture is placed in the world.
 * Cancelling the event prevents the placement.
 */
@Getter
public class FurniturePlaceEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final @Nullable Player player;

    private final FurniturePlacedInterface furniturePlaced;

    private final @Nullable Event sourceEvent;

    private boolean cancelled = false;

    /**
     * @param player          the player who placed the furniture, or null
     * @param furniturePlaced the placed furniture
     * @param sourceEvent     the Bukkit event at the origin of the placement, if any
     */
    public FurniturePlaceEvent(@Nullable Player player, @NotNull FurniturePlacedInterface furniturePlaced, @Nullable Event sourceEvent) {
        this.player = player;
        this.furniturePlaced = furniturePlaced;
        this.sourceEvent = sourceEvent;
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
