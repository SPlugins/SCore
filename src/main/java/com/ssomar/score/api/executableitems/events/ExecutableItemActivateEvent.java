package com.ssomar.score.api.executableitems.events;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Fired when an activator of an ExecutableItem is triggered for a player,
 * before its conditions are evaluated and its commands run.
 * Cancelling the event prevents the activator from running.
 */
public class ExecutableItemActivateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    /**
     * The player who triggered the activator.
     */
    @Getter
    private final Player player;

    /**
     * The ItemStack of the ExecutableItem that triggered.
     */
    @Getter
    private final ItemStack item;

    /**
     * The id of the ExecutableItem configuration.
     */
    @Getter
    private final String executableItemId;

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
     * @param player           the player who triggered the activator
     * @param item             the ItemStack of the ExecutableItem
     * @param executableItemId the id of the ExecutableItem configuration
     * @param activatorId      the id of the triggered activator
     * @param sourceEvent      the Bukkit event at the origin of the trigger, if any
     */
    public ExecutableItemActivateEvent(@NotNull Player player, @NotNull ItemStack item, @NotNull String executableItemId, @NotNull String activatorId, @Nullable Event sourceEvent) {
        super(!org.bukkit.Bukkit.isPrimaryThread());
        this.player = player;
        this.item = item;
        this.executableItemId = executableItemId;
        this.activatorId = activatorId;
        this.sourceEvent = sourceEvent;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
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
