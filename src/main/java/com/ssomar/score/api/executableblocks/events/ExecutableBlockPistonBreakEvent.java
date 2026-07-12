package com.ssomar.score.api.executableblocks.events;

import com.ssomar.score.api.executableblocks.config.placed.ExecutableBlockPlacedInterface;
import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a piston is about to break a placed ExecutableBlock.
 * Cancelling the event protects the block (the piston move is cancelled).
 */
@Getter
public class ExecutableBlockPistonBreakEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    /**
     * The placed ExecutableBlock about to be broken.
     */
    private final ExecutableBlockPlacedInterface executableBlockPlaced;

    private boolean cancelled = false;

    /**
     * @param executableBlockPlaced the placed ExecutableBlock about to be broken
     */
    public ExecutableBlockPistonBreakEvent(@NotNull ExecutableBlockPlacedInterface executableBlockPlaced) {
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
