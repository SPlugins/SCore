package com.ssomar.score.api.myfurniture.load;

import com.ssomar.score.api.myfurniture.config.FurnitureManagerInterface;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired once all MyFurniture configurations have been loaded.
 * From this point the API lookups return complete results
 * ({@link FurnitureManagerInterface#isLoaded()} is true).
 */
public class MyFurniturePostLoadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    /**
     * The loaded furniture manager (null when fired through the deprecated constructor).
     */
    @Getter
    private final FurnitureManagerInterface manager;

    /**
     * @param manager the loaded furniture manager
     */
    public MyFurniturePostLoadEvent(FurnitureManagerInterface manager) {
        this.manager = manager;
    }

    /**
     * @deprecated use {@link #MyFurniturePostLoadEvent(FurnitureManagerInterface)}
     */
    @Deprecated
    public MyFurniturePostLoadEvent() {
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
