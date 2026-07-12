package com.ssomar.score.api.executableblocks.load;

import com.ssomar.score.api.executableblocks.config.ExecutableBlocksManagerInterface;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired once all ExecutableBlocks configurations have been loaded.
 * From this point the API lookups return complete results
 * ({@link ExecutableBlocksManagerInterface#isLoaded()} is true).
 */
public class ExecutableBlocksPostLoadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    /**
     * The loaded ExecutableBlocks manager (null when fired through the deprecated constructor).
     */
    @Getter
    private final ExecutableBlocksManagerInterface manager;

    /**
     * @param manager the loaded ExecutableBlocks manager
     */
    public ExecutableBlocksPostLoadEvent(ExecutableBlocksManagerInterface manager) {
        this.manager = manager;
    }

    /**
     * @deprecated use {@link #ExecutableBlocksPostLoadEvent(ExecutableBlocksManagerInterface)}
     */
    @Deprecated
    public ExecutableBlocksPostLoadEvent() {
        this(null);
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
