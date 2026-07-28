package com.kaduvill.capnsbeeaddon.client.territory;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nonnull;

/**
 * Immutable client-side description of one Career Bee territory overlay.
 */
public final class CareerTerritoryInfo {

    private final int dimension;
    private final BlockPos source;
    private final AxisAlignedBB bounds;
    private final String effectName;

    public CareerTerritoryInfo(
            int dimension,
            @Nonnull BlockPos source,
            @Nonnull AxisAlignedBB bounds,
            @Nonnull String effectName
    ) {
        this.dimension = dimension;
        this.source = source.toImmutable();
        this.bounds = bounds;
        this.effectName = effectName;
    }

    public int getDimension() {
        return dimension;
    }

    @Nonnull
    public BlockPos getSource() {
        return source;
    }

    @Nonnull
    public AxisAlignedBB getBounds() {
        return bounds;
    }

    @Nonnull
    public String getEffectName() {
        return effectName;
    }

    public int getWidth() {
        return MathHelper.ceil(bounds.maxX - bounds.minX);
    }

    public int getHeight() {
        return MathHelper.ceil(bounds.maxY - bounds.minY);
    }

    public int getDepth() {
        return MathHelper.ceil(bounds.maxZ - bounds.minZ);
    }

    public boolean hasSameSource(@Nonnull CareerTerritoryInfo other) {
        return dimension == other.dimension && source.equals(other.source);
    }
}
