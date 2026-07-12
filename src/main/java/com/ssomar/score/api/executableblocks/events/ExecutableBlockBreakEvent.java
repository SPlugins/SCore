package com.ssomar.score.api.executableblocks.events;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when a placed ExecutableBlock is broken.
 * Cancelling the event prevents the break.
 */
@Getter
public class ExecutableBlockBreakEvent extends Event implements Cancellable {

    /**
     * How the ExecutableBlock was broken.
     */
    public enum BreakMethod {
        /**
         * Broken like a vanilla block (mining, explosion, ...).
         */
        NATURAL,
        /**
         * Broken by an ExecutableBlocks mechanic (command, api, ...).
         */
        CUSTOM
    }

    private static final HandlerList handlers = new HandlerList();

    private final @Nullable Player player;

    private final Block block;

    private final BreakMethod breakMethod;

    private final Event sourceEvent;

    private boolean cancelled = false;

    /**
     * @param player      the player who broke the block, or null if not broken by a player
     * @param block       the broken block
     * @param sourceEvent the Bukkit event at the origin of the break, if any
     * @param breakMethod how the block was broken
     */
    public ExecutableBlockBreakEvent(@Nullable Player player, Block block, @Nullable Event sourceEvent, BreakMethod breakMethod) {
        this.player = player;
        this.block = block;
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
