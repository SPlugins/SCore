package com.ssomar.score.commands.runnable.block.commands.inline_mineincube;

import com.google.common.base.Preconditions;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Ripped from org.bukkit.util.BoundingBox. This was made to deal with INLINE_MINEINCUBE's issue in lower versions
 * due to BoundingBox being missing in those lower versions.
 */
@SerializableAs("BoundingBox")
public class BoundingBox implements Cloneable, ConfigurationSerializable {


    private double minX;
    private double minY;
    private double minZ;
    private double maxX;
    private double maxY;
    private double maxZ;

    /**
     * Creates a new bounding box from the given corner coordinates.
     *
     * @param x1 the first corner's x value
     * @param y1 the first corner's y value
     * @param z1 the first corner's z value
     * @param x2 the second corner's x value
     * @param y2 the second corner's y value
     * @param z2 the second corner's z value
     */
    public BoundingBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.resize(x1, y1, z1, x2, y2, z2);
    }

    /**
     * Resizes this bounding box.
     *
     * @param x1 the first corner's x value
     * @param y1 the first corner's y value
     * @param z1 the first corner's z value
     * @param x2 the second corner's x value
     * @param y2 the second corner's y value
     * @param z2 the second corner's z value
     * @return this bounding box (resized)
     */
    @NotNull
    public BoundingBox resize(double x1, double y1, double z1, double x2, double y2, double z2) {
        NumberConversions.checkFinite(x1, "x1 not finite");
        NumberConversions.checkFinite(y1, "y1 not finite");
        NumberConversions.checkFinite(z1, "z1 not finite");
        NumberConversions.checkFinite(x2, "x2 not finite");
        NumberConversions.checkFinite(y2, "y2 not finite");
        NumberConversions.checkFinite(z2, "z2 not finite");

        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
        return this;
    }


    @Nullable
    public RayTraceResult rayTrace(@NotNull org.bukkit.util.Vector startArg, @NotNull org.bukkit.util.Vector directionArg, double maxDistance) {
        Vector start = new Vector(startArg.getX(), startArg.getY(), startArg.getZ());
        Vector direction = new Vector(directionArg.getX(), directionArg.getY(), directionArg.getZ());

        Preconditions.checkArgument(start != null, "Start is null!");
        start.checkFinite();
        Preconditions.checkArgument(direction != null, "Direction is null!");
        direction.checkFinite();
        Preconditions.checkArgument(direction.lengthSquared() > 0, "Direction's magnitude is 0!");
        if (maxDistance < 0.0D) return null;

        // ray start:
        double startX = start.getX();
        double startY = start.getY();
        double startZ = start.getZ();

        // ray direction:
        Vector dir = direction.clone().normalizeZeros().normalize();
        double dirX = dir.getX();
        double dirY = dir.getY();
        double dirZ = dir.getZ();

        // saving a few divisions below:
        // Note: If one of the direction vector components is 0.0, these
        // divisions result in infinity. But this is not a problem.
        double divX = 1.0D / dirX;
        double divY = 1.0D / dirY;
        double divZ = 1.0D / dirZ;

        double tMin;
        double tMax;
        BlockFace hitBlockFaceMin;
        BlockFace hitBlockFaceMax;

        // intersections with x planes:
        if (dirX >= 0.0D) {
            tMin = (this.minX - startX) * divX;
            tMax = (this.maxX - startX) * divX;
            hitBlockFaceMin = BlockFace.WEST;
            hitBlockFaceMax = BlockFace.EAST;
        } else {
            tMin = (this.maxX - startX) * divX;
            tMax = (this.minX - startX) * divX;
            hitBlockFaceMin = BlockFace.EAST;
            hitBlockFaceMax = BlockFace.WEST;
        }

        // intersections with y planes:
        double tyMin;
        double tyMax;
        BlockFace hitBlockFaceYMin;
        BlockFace hitBlockFaceYMax;
        if (dirY >= 0.0D) {
            tyMin = (this.minY - startY) * divY;
            tyMax = (this.maxY - startY) * divY;
            hitBlockFaceYMin = BlockFace.DOWN;
            hitBlockFaceYMax = BlockFace.UP;
        } else {
            tyMin = (this.maxY - startY) * divY;
            tyMax = (this.minY - startY) * divY;
            hitBlockFaceYMin = BlockFace.UP;
            hitBlockFaceYMax = BlockFace.DOWN;
        }
        if ((tMin > tyMax) || (tMax < tyMin)) {
            return null;
        }
        if (tyMin > tMin) {
            tMin = tyMin;
            hitBlockFaceMin = hitBlockFaceYMin;
        }
        if (tyMax < tMax) {
            tMax = tyMax;
            hitBlockFaceMax = hitBlockFaceYMax;
        }

        // intersections with z planes:
        double tzMin;
        double tzMax;
        BlockFace hitBlockFaceZMin;
        BlockFace hitBlockFaceZMax;
        if (dirZ >= 0.0D) {
            tzMin = (this.minZ - startZ) * divZ;
            tzMax = (this.maxZ - startZ) * divZ;
            hitBlockFaceZMin = BlockFace.NORTH;
            hitBlockFaceZMax = BlockFace.SOUTH;
        } else {
            tzMin = (this.maxZ - startZ) * divZ;
            tzMax = (this.minZ - startZ) * divZ;
            hitBlockFaceZMin = BlockFace.SOUTH;
            hitBlockFaceZMax = BlockFace.NORTH;
        }
        if ((tMin > tzMax) || (tMax < tzMin)) {
            return null;
        }
        if (tzMin > tMin) {
            tMin = tzMin;
            hitBlockFaceMin = hitBlockFaceZMin;
        }
        if (tzMax < tMax) {
            tMax = tzMax;
            hitBlockFaceMax = hitBlockFaceZMax;
        }

        // intersections are behind the start:
        if (tMax < 0.0D) return null;
        // intersections are too far away:
        if (tMin > maxDistance) {
            return null;
        }

        // find the closest intersection:
        double t;
        BlockFace hitBlockFace;
        if (tMin < 0.0D) {
            t = tMax;
            hitBlockFace = hitBlockFaceMax;
        } else {
            t = tMin;
            hitBlockFace = hitBlockFaceMin;
        }
        // reusing the newly created direction vector for the hit position:
        Vector hitPosition = dir.multiply(t).add(start);
        return new RayTraceResult(hitPosition, hitBlockFace);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(maxX);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxY);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxZ);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minX);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minY);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minZ);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BoundingBox)) return false;
        BoundingBox other = (BoundingBox) obj;
        if (Double.doubleToLongBits(maxX) != Double.doubleToLongBits(other.maxX)) return false;
        if (Double.doubleToLongBits(maxY) != Double.doubleToLongBits(other.maxY)) return false;
        if (Double.doubleToLongBits(maxZ) != Double.doubleToLongBits(other.maxZ)) return false;
        if (Double.doubleToLongBits(minX) != Double.doubleToLongBits(other.minX)) return false;
        if (Double.doubleToLongBits(minY) != Double.doubleToLongBits(other.minY)) return false;
        if (Double.doubleToLongBits(minZ) != Double.doubleToLongBits(other.minZ)) return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BoundingBox [minX=");
        builder.append(minX);
        builder.append(", minY=");
        builder.append(minY);
        builder.append(", minZ=");
        builder.append(minZ);
        builder.append(", maxX=");
        builder.append(maxX);
        builder.append(", maxY=");
        builder.append(maxY);
        builder.append(", maxZ=");
        builder.append(maxZ);
        builder.append("]");
        return builder.toString();
    }

    /**
     * Creates a copy of this bounding box.
     *
     * @return the cloned bounding box
     */
    @NotNull
    @Override
    public BoundingBox clone() {
        try {
            return (BoundingBox) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error(e);
        }
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("minX", minX);
        result.put("minY", minY);
        result.put("minZ", minZ);
        result.put("maxX", maxX);
        result.put("maxY", maxY);
        result.put("maxZ", maxZ);
        return result;
    }

}
