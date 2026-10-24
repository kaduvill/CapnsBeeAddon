package com.kaduvill.capnsbeeaddon.compat.careerbees;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * Immutable, common-side description of one Career Bee territory boundary.
 *
 * Contains no live World, TileEntity, housing, bee or ItemStack references.
 */
public final class CareerTerritoryInfo {

    private final int dimension;
    private final BlockPos source;
    private final AxisAlignedBB bounds;
    private final String effectTranslationKey;
    private final boolean blockAligned;
    private final String sizeText;
    private final boolean temporal;

    public CareerTerritoryInfo(
            int dimension,
            @Nonnull BlockPos source,
            @Nonnull AxisAlignedBB bounds,
            @Nonnull String effectTranslationKey,
            boolean blockAligned,
            boolean temporal
    ) {
        this.dimension = dimension;
        this.source = source.toImmutable();
        this.bounds = bounds;
        this.effectTranslationKey = effectTranslationKey;
        this.blockAligned = blockAligned;
        this.sizeText = formatSize(bounds, blockAligned);
        this.temporal = temporal;
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
    public String getEffectTranslationKey() {
        return effectTranslationKey;
    }

    public boolean isBlockAligned() {
        return blockAligned;
    }

    /**
     * Preformatted because the tooltip is drawn every frame while hovered.
     */
    @Nonnull
    public String getSizeText() {
        return sizeText;
    }

    public boolean isTemporal() {
        return temporal;
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
