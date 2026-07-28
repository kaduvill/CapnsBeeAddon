package com.kaduvill.capnsbeeaddon.client.territory;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * Immutable client-side description of one Career Bee territory overlay.
 *
 * Contains no live World, TileEntity, housing, bee or ItemStack references.
 */
public final class CareerTerritoryInfo {

    private final int dimension;
    private final BlockPos source;
    private final AxisAlignedBB bounds;
    private final String effectName;
    private final String sizeText;

    public CareerTerritoryInfo(
            int dimension,
            @Nonnull BlockPos source,
            @Nonnull AxisAlignedBB bounds,
            @Nonnull String effectName,
            boolean blockAligned
    ) {
        this.dimension = dimension;
        this.source = source.toImmutable();
        this.bounds = bounds;
        this.effectName = effectName;
        this.sizeText = formatSize(bounds, blockAligned);
    }

    public int getDimension() {
        return dimension;
    }

    @Nonnull
    public BlockPos getSource() {
        return source;
    }

    /**
     * Absolute world-coordinate bounds.
     */
    @Nonnull
    public AxisAlignedBB getBounds() {
        return bounds;
    }

    @Nonnull
    public String getEffectName() {
        return effectName;
    }

    /**
     * Preformatted because the tooltip is drawn every frame while hovered.
     */
    @Nonnull
    public String getSizeText() {
        return sizeText;
    }

    public boolean hasSameSource(@Nonnull CareerTerritoryInfo other) {
        return dimension == other.dimension
                && source.equals(other.source);
    }

    @Nonnull
    private static String formatSize(
            @Nonnull AxisAlignedBB bounds,
            boolean blockAligned
    ) {
        double width = bounds.maxX - bounds.minX;
        double height = bounds.maxY - bounds.minY;
        double depth = bounds.maxZ - bounds.minZ;

        if (blockAligned) {
            /*
             Every block-aligned geometry method constructs exact integer
             boundaries. Round instead of ceil so floating-point noise cannot
             turn a 5-block box into 6.
             */
            return Math.round(width)
                    + " × "
                    + Math.round(height)
                    + " × "
                    + Math.round(depth);
        }

        return String.format(
                Locale.ROOT,
                "%.2f × %.2f × %.2f",
                width,
                height,
                depth
        );
    }
}