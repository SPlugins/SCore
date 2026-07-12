package com.ssomar.score.api.executableblocks.config.placed;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Manager of the ExecutableBlocks placed in the world.
 */
public interface ExecutableBlocksPlacedManagerInterface {

    /**
     * Remove a placed ExecutableBlock (no drop, no break event).
     *
     * @param eBP the placed block to remove
     */
    void removeExecutableBlockPlaced(ExecutableBlockPlacedInterface eBP);

    /**
     * Get the placed ExecutableBlock at a location.
     *
     * @param location the location to check
     * @return the placed ExecutableBlock, or an empty optional
     */
    Optional<ExecutableBlockPlacedInterface> getExecutableBlockPlaced(Location location);

    /**
     * Get the placed ExecutableBlock of a block.
     *
     * @param block the block to check
     * @return the placed ExecutableBlock, or an empty optional
     */
    Optional<ExecutableBlockPlacedInterface> getExecutableBlockPlaced(Block block);

    /**
     * Get the placed ExecutableBlock of an interaction entity
     * (blocks with entity-based interaction).
     *
     * @param interaction the interaction entity
     * @return the placed ExecutableBlock, or an empty optional
     */
    Optional<? extends ExecutableBlockPlacedInterface> getExecutableBlockPlaced(Entity interaction);

    /**
     * Get the placed ExecutableBlocks near a location.
     *
     * @param location    the center location
     * @param distanceMax the maximum distance
     * @return the placed ExecutableBlocks in range
     */
    List<? extends ExecutableBlockPlacedInterface> getExecutableBlocksPlacedNear(Location location, double distanceMax);

    /**
     * Get the placed ExecutableBlocks inside a chunk.
     *
     * @param chunk the chunk
     * @return the placed ExecutableBlocks of the chunk
     */
    List<? extends ExecutableBlockPlacedInterface> getExecutableBlocksPlaced(Chunk chunk);

    /**
     * Get all the placed ExecutableBlocks, indexed by location.
     *
     * @return all placed ExecutableBlocks
     */
    Map<Location, ? extends ExecutableBlockPlacedInterface> getAllExecutableBlocksPlaced();

    /**
     * Get all the ExecutableBlocks placed by a player, indexed by location.
     *
     * @param playerUUID the player uuid
     * @return the placed ExecutableBlocks of this player
     */
    Map<Location, ? extends ExecutableBlockPlacedInterface> getAllExecutableBlocksPlacedBy(UUID playerUUID);

    /**
     * Get all the placed ExecutableBlocks of a specific configuration.
     *
     * @param executableBlockId the ExecutableBlock configuration id
     * @return the placed ExecutableBlocks of this configuration
     */
    List<? extends ExecutableBlockPlacedInterface> getAllExecutableBlocksPlacedByConfig(String executableBlockId);

    /**
     * Count the ExecutableBlocks placed by a player.
     *
     * @param playerUUID the player uuid
     * @return the amount of ExecutableBlocks placed by this player
     */
    int getAmountOfExecutableBlocksPlacedBy(UUID playerUUID);

    /**
     * Count the ExecutableBlocks of a specific configuration placed by a player.
     *
     * @param playerUUID the player uuid
     * @param id         the ExecutableBlock configuration id
     * @return the amount of ExecutableBlocks placed by this player for this id
     */
    int getAmountOfExecutableBlocksPlacedBy(UUID playerUUID, String id);
}
