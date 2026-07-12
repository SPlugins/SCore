package com.ssomar.score.api.custompiglinstrades.load;

import com.ssomar.score.api.custompiglinstrades.config.CustomPiglinsTradesManagerInterface;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired once all CustomPiglinsTrades trades have been loaded.
 * From this point the API lookups return complete results
 * ({@link CustomPiglinsTradesManagerInterface#isLoaded()} is true).
 */
public class CustomPiglinsTradesPostLoadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    /**
     * The loaded trades manager.
     */
    @Getter
    private final CustomPiglinsTradesManagerInterface manager;

    /**
     * @param manager the loaded trades manager
     */
    public CustomPiglinsTradesPostLoadEvent(CustomPiglinsTradesManagerInterface manager) {
        this.manager = manager;
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
