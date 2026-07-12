package com.ssomar.score.api.executablecrafting.load;

import com.ssomar.score.api.executablecrafting.config.ExecutableCraftingManagerInterface;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired once all ExecutableCrafting recipes have been loaded and registered.
 * From this point the API lookups return complete results
 * ({@link ExecutableCraftingManagerInterface#isLoaded()} is true).
 */
public class ExecutableCraftingPostLoadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    /**
     * The loaded ExecutableCrafting manager.
     */
    @Getter
    private final ExecutableCraftingManagerInterface manager;

    /**
     * @param manager the loaded ExecutableCrafting manager
     */
    public ExecutableCraftingPostLoadEvent(ExecutableCraftingManagerInterface manager) {
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
