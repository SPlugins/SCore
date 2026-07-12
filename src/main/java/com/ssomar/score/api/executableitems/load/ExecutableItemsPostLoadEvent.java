package com.ssomar.score.api.executableitems.load;

import com.ssomar.score.api.executableitems.config.ExecutableItemsManagerInterface;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired once all ExecutableItems configurations have been loaded.
 * From this point the API lookups return complete results
 * ({@link ExecutableItemsManagerInterface#isLoaded()} is true).
 */
public class ExecutableItemsPostLoadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    /**
     * The loaded ExecutableItems manager (null when fired through the deprecated constructor).
     */
    @Getter
    private final ExecutableItemsManagerInterface manager;

    /**
     * @param manager the loaded ExecutableItems manager
     */
    public ExecutableItemsPostLoadEvent(ExecutableItemsManagerInterface manager) {
        this.manager = manager;
    }

    /**
     * @deprecated use {@link #ExecutableItemsPostLoadEvent(ExecutableItemsManagerInterface)}
     */
    @Deprecated
    public ExecutableItemsPostLoadEvent() {
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
