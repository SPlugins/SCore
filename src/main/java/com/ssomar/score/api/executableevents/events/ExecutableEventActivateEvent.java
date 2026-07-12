package com.ssomar.score.api.executableevents.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when an activator of an ExecutableEvent is triggered,
 * before its conditions are evaluated and its commands run.
 * Cancelling the event prevents the activator from running.
 */
public class ExecutableEventActivateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    /**
     * The player who triggered the activator, or null when the trigger is not player-bound
     * (world events, server events...).
     */
    @Getter
    @Nullable
    private final Player player;

    /**
     * The id of the ExecutableEvent configuration.
     */
    @Getter
    private final String executableEventId;

    /**
     * The id of the activator being triggered.
     */
    @Getter
    private final String activatorId;

    /**
     * The Bukkit event that triggered the activator, if any.
     */
    @Getter
    @Nullable
    private final Event sourceEvent;

    private boolean cancelled = false;

    /**
     * @param player            the player who triggered the activator, or null
     * @param executableEventId the id of the ExecutableEvent configuration
     * @param activatorId       the id of the triggered activator
     * @param sourceEvent       the Bukkit event at the origin of the trigger, if any
     */
    public ExecutableEventActivateEvent(@Nullable Player player, @NotNull String executableEventId, @NotNull String activatorId, @Nullable Event sourceEvent) {
        super(!org.bukkit.Bukkit.isPrimaryThread());
        this.player = player;
        this.executableEventId = executableEventId;
        this.activatorId = activatorId;
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
