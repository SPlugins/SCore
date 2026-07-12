package com.ssomar.score.api.executableblocks.events;

import com.ssomar.score.api.executableblocks.config.placed.ExecutableBlockPlacedInterface;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when an ExecutableBlock is placed in the world.
 * Cancelling the event prevents the placement.
 */
@Getter
public class ExecutableBlockPlaceEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final @Nullable Entity placer;

    private final ExecutableBlockPlacedInterface executableBlockPlaced;


    private boolean cancelled = false;

    /**
     * @param placer                the entity that placed the block, or null
     * @param executableBlockPlaced the placed ExecutableBlock
     */
    public ExecutableBlockPlaceEvent(@Nullable Entity placer, ExecutableBlockPlacedInterface executableBlockPlaced) {
        this.placer = placer;
        this.executableBlockPlaced = executableBlockPlaced;
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
    public final @NotNull
    HandlerList getHandlers() {
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
