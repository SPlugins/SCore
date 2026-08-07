package com.ssomar.score.commands.runnable.block.commands.inline_mineincube;

import com.google.common.base.Preconditions;
import java.util.Objects;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Ripped from org.bukkit.util.RayTraceResult. This was made to deal with INLINE_MINEINCUBE's issue in lower versions
 * due to BoundingBox being missing in those lower versions.
 */
public class RayTraceResult {

    private final Vector hitPosition;

    private final Block hitBlock;
    private final BlockFace hitBlockFace;
    private final Entity hitEntity;

    private RayTraceResult(@NotNull Vector hitPosition, @Nullable Block hitBlock, @Nullable BlockFace hitBlockFace, @Nullable Entity hitEntity) {
        Preconditions.checkArgument(hitPosition != null, "Hit position is null!");
        this.hitPosition = hitPosition.clone();
        this.hitBlock = hitBlock;
        this.hitBlockFace = hitBlockFace;
        this.hitEntity = hitEntity;
    }

    /**
     * Creates a RayTraceResult.
     *
     * @param hitPosition the hit position
     * @param hitBlockFace the hit block face
     */
    public RayTraceResult(@NotNull Vector hitPosition, @Nullable BlockFace hitBlockFace) {
        this(hitPosition, null, hitBlockFace, null);
    }
    /**
     * Gets the hit block face.
     *
     * @return the hit block face, or <code>null</code> if not available
     */
    @Nullable
    public BlockFace getHitBlockFace() {
        return hitBlockFace;
    }

}
