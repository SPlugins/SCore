package com.ssomar.score.api.myfurniture.config.placed;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Manager of the furniture placed in the world.
 */
public interface FurniturePlacedManagerInterface {

    /**
     * Get the placed furniture at a location.
     *
     * @param location the location to check
     * @return the placed furniture, or an empty optional
     */
    Optional<? extends FurniturePlacedInterface> getFurniturePlaced(Location location);

    /**
     * Get the placed furniture of a block (for furniture with a hitbox block).
     *
     * @param block the block to check
     * @return the placed furniture, or an empty optional
     */
    Optional<? extends FurniturePlacedInterface> getFurniturePlaced(Block block);

    /**
     * Get the placed furniture of a display entity.
     *
     * @param byDisplay the display entity of the furniture
     * @return the placed furniture, or an empty optional
     */
    Optional<? extends FurniturePlacedInterface> getFurniturePlaced(Entity byDisplay);

    /**
     * Get a placed furniture from its unique id.
     *
     * @param id the unique id of the placed furniture
     * @return the placed furniture, or an empty optional
     */
    Optional<? extends FurniturePlacedInterface> getFurniturePlaced(String id);

    /**
     * Get the placed furniture near a location.
     *
     * @param location    the center location
     * @param distanceMax the maximum distance
     * @return the placed furniture in range
     */
    List<? extends FurniturePlacedInterface> getFurniturePlacedNear(Location location, double distanceMax);

    /**
     * Count the furniture placed by a player.
     *
     * @param playerUUID the player uuid
     * @return the amount of furniture placed by this player
     */
    int getAmountOfFurniturePlacedBy(UUID playerUUID);

    /**
     * Count the furniture of a specific configuration placed by a player.
     *
     * @param playerUUID the player uuid
     * @param id         the furniture configuration id
     * @return the amount of furniture placed by this player for this id
     */
    int getAmountOfFurniturePlacedBy(UUID playerUUID, String id);

    /**
     * Remove a placed furniture without breaking it (no drop, no break event).
     *
     * @param furniturePlaced the placed furniture to remove
     */
    void removeFurniturePlaced(FurniturePlacedInterface furniturePlaced);
}
