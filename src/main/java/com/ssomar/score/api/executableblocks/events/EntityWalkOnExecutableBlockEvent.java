package com.ssomar.score.api.executableblocks.events;

import com.ssomar.score.api.executableblocks.config.placed.ExecutableBlockPlacedInterface;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when an entity walks on a placed ExecutableBlock that has an
 * ENTITY_WALK_ON activator. Cancelling the event prevents the activator from running.
 * <p>
 * This event can fire frequently (it is checked periodically while entities move
 * on such blocks) — keep listeners lightweight.
 */
@Getter
public class EntityWalkOnExecutableBlockEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    /**
     * The entity walking on the block.
     */
    private final Entity entity;

    /**
     * The placed ExecutableBlock being walked on.
     */
    private final ExecutableBlockPlacedInterface executableBlockPlaced;

    private boolean cancelled = false;

    /**
     * @param entity                the entity walking on the block
     * @param executableBlockPlaced the placed ExecutableBlock being walked on
     */
    public EntityWalkOnExecutableBlockEvent(@NotNull Entity entity, @NotNull ExecutableBlockPlacedInterface executableBlockPlaced) {
        super(!org.bukkit.Bukkit.isPrimaryThread());
        this.entity = entity;
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
