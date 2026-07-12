package com.ssomar.score.api.executableevents.load;

import com.ssomar.score.api.executableevents.config.ExecutableEventsManagerInterface;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired once all ExecutableEvents configurations have been loaded.
 * From this point the API lookups return complete results
 * ({@link ExecutableEventsManagerInterface#isLoaded()} is true).
 */
public class ExecutableEventsPostLoadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    /**
     * The loaded ExecutableEvents manager (null when fired through the deprecated constructor).
     */
    @Getter
    private final ExecutableEventsManagerInterface manager;

    /**
     * @param manager the loaded ExecutableEvents manager
     */
    public ExecutableEventsPostLoadEvent(ExecutableEventsManagerInterface manager) {
        this.manager = manager;
    }

    /**
     * @deprecated use {@link #ExecutableEventsPostLoadEvent(ExecutableEventsManagerInterface)}
     */
    @Deprecated
    public ExecutableEventsPostLoadEvent() {
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
