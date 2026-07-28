package com.kaduvill.capnsbeeaddon.client.territory;

import com.rwtema.careerbees.effects.EffectAcceleration;
import com.rwtema.careerbees.effects.EffectBase;
import com.rwtema.careerbees.effects.EffectClockwork;
import com.rwtema.careerbees.effects.EffectCrafting;
import com.rwtema.careerbees.effects.EffectCreeper;
import com.rwtema.careerbees.effects.EffectDigging;
import com.rwtema.careerbees.effects.EffectItemModification;
import com.rwtema.careerbees.effects.EffectJazz;
import com.rwtema.careerbees.effects.EffectRandomSwap;
import com.rwtema.careerbees.effects.EffectWorldInteraction;
import com.rwtema.careerbees.effects.ISpecialBeeEffect;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.IAlleleBeeEffect;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeHousingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Resolves the active Career Bee effect and reproduces the geometry used by
 * that effect in Career Bees 0.4.0.
 *
 * This performs only inventory/genome reads and local coordinate math.
 * It never scans blocks, chunks, TileEntities or entities.
 */
public final class CareerTerritoryResolver {

    /*
     * Jazz begins with territory ×0.5, then multiplies it by 1.3 before
     * each of ten intersection checks.
     */
    private static final double JAZZ_MAXIMUM_SCALE =
            0.5D * Math.pow(1.3D, 10);

    private CareerTerritoryResolver() {
    }

    @Nullable
    public static CareerTerritoryInfo resolve(
            @Nullable IBeeHousing housing
    ) {
        if (housing == null) {
            return null;
        }

        World world = housing.getWorldObj();
        IBeeHousingInventory inventory = housing.getBeeInventory();

        if (world == null || inventory == null) {
            return null;
        }

        ItemStack queenStack = inventory.getQueen();
        if (queenStack.isEmpty()) {
            return null;
        }

        IBee bee = BeeManager.beeRoot.getMember(queenStack);
        if (bee == null) {
            return null;
        }

        IBeeGenome genome = bee.getGenome();
        IAlleleBeeEffect allele = genome.getEffect();

        if (!(allele instanceof EffectBase)) {
            return null;
        }

        EffectBase effect = (EffectBase) allele;
        if (!effect.isValidSpecies(genome)) {
            return null;
        }

        BlockPos source = housing.getCoordinates().toImmutable();

        /*
         * Calculate this exactly once. It includes:
         *
         * - the bee's active territory allele;
         * - all housing modifiers, including Gendustry upgrades;
         * - your Territory Restrictor;
         * - the active beekeeping-mode modifier.
         */
        Vec3d territory = EffectBase.getTerritory(genome, housing);

        /*
         * Most Career Bees calculations start with this full-block AABB.
         */
        AxisAlignedBB continuous = new AxisAlignedBB(source).grow(
                territory.x,
                territory.y,
                territory.z
        );

        AxisAlignedBB bounds;
        boolean blockAligned;

        /*
         * Specific implementations must be checked before the broader
         * SpecialEffectEntity category.
         */

        if (effect instanceof EffectAcceleration) {
            /*
             * Temporal floors each radius before its inclusive block loop.
             */
            bounds = createTemporalGrid(source, territory);
            blockAligned = true;

        } else if (effect instanceof EffectItemModification
                || effect instanceof EffectCrafting) {
            /*
             * Honey Smelter, Smelter, Ore Crusher, pedestal crafting, etc.
             */
            bounds = createPedestalGrid(source, territory);
            blockAligned = true;

        } else if (effect instanceof EffectClockwork) {
            /*
             * Clockwork calls EffectBase#getTiles using the continuous AABB.
             */
            bounds = createInclusiveTileScan(continuous);
            blockAligned = true;

        } else if (effect instanceof EffectRandomSwap) {
            /*
             * Teleposition chooses random integer coordinates from the AABB
             * with an exclusive upper bound.
             */
            bounds = createRandomGrid(continuous);
            blockAligned = true;

        } else if (effect instanceof EffectWorldInteraction) {
            /*
             * Lumber, Mason, Burning, Painting, Assassin, etc.
             */
            bounds = createWorldInteractionGrid(continuous);
            blockAligned = true;

        } else if (effect instanceof EffectDigging) {
            /*
             * Digging uses territory only for X/Z and chooses Y from the
             * selected chunk's terrain height. This is the complete possible
             * vertical envelope without scanning chunks from the renderer.
             */
            bounds = createDiggingEnvelope(continuous);
            blockAligned = true;

        } else if (effect instanceof EffectCreeper) {
            /*
             * Creeper uses the continuous AABB for population checks, but
             * spawning always uses source Y ±1. Render the union envelope.
             */
            bounds = createCreeperEnvelope(
                    source,
                    territory,
                    continuous
            );
            blockAligned = false;

        } else if (effect instanceof EffectJazz) {
            /*
             * Jazz expands its audio test box ten times.
             */
            bounds = createJazzEnvelope(source, territory);
            blockAligned = false;

        } else if (effect
                instanceof ISpecialBeeEffect.SpecialEffectEntity) {
            /*
             * Includes EffectBaseEntity subclasses plus direct entity-effect
             * implementations such as Butcher, Husbandry, Pickup and Steal.
             * These query entities through EffectBase#getAABB.
             */
            bounds = continuous;
            blockAligned = false;

        } else {
            /*
             * The remaining Career Bees effects either use adjacent blocks,
             * fixed ranges or no world territory. Hide the button rather than
             * display a plausible-looking but incorrect box.
             */
            return null;
        }

        return new CareerTerritoryInfo(
                world.provider.getDimension(),
                source,
                bounds,
                effect.getAlleleName(),
                blockAligned
        );
    }

    /**
     * Honey Smelter and other pedestal effects:
     *
     * min = floor(source - territory)
     * max = ceil(source + territory), inclusive
     */
    @Nonnull
    private static AxisAlignedBB createPedestalGrid(
            @Nonnull BlockPos source,
            @Nonnull Vec3d territory
    ) {
        return fromInclusiveBlocks(
                MathHelper.floor(source.getX() - territory.x),
                MathHelper.floor(source.getY() - territory.y),
                MathHelper.floor(source.getZ() - territory.z),
                MathHelper.ceil(source.getX() + territory.x),
                MathHelper.ceil(source.getY() + territory.y),
                MathHelper.ceil(source.getZ() + territory.z)
        );
    }

    /**
     * Temporal:
     *
     * radius = floor(territory)
     * positions = source-radius through source+radius, inclusive
     */
    @Nonnull
    private static AxisAlignedBB createTemporalGrid(
            @Nonnull BlockPos source,
            @Nonnull Vec3d territory
    ) {
        int radiusX = MathHelper.floor(territory.x);
        int radiusY = MathHelper.floor(territory.y);
        int radiusZ = MathHelper.floor(territory.z);

        return fromInclusiveBlocks(
                source.getX() - radiusX,
                source.getY() - radiusY,
                source.getZ() - radiusZ,
                source.getX() + radiusX,
                source.getY() + radiusY,
                source.getZ() + radiusZ
        );
    }

    /**
     * EffectBase#getTiles, used by Clockwork:
     *
     * min = floor(AABB min)
     * max = ceil(AABB max), inclusive
     */
    @Nonnull
    private static AxisAlignedBB createInclusiveTileScan(
            @Nonnull AxisAlignedBB continuous
    ) {
        return fromInclusiveBlocks(
                MathHelper.floor(continuous.minX),
                MathHelper.floor(continuous.minY),
                MathHelper.floor(continuous.minZ),
                MathHelper.ceil(continuous.maxX),
                MathHelper.ceil(continuous.maxY),
                MathHelper.ceil(continuous.maxZ)
        );
    }

    /**
     * Teleposition:
     *
     * getRand(floor(min), ceil(max)) uses an exclusive upper bound.
     */
    @Nonnull
    private static AxisAlignedBB createRandomGrid(
            @Nonnull AxisAlignedBB continuous
    ) {
        return new AxisAlignedBB(
                MathHelper.floor(continuous.minX),
                MathHelper.floor(continuous.minY),
                MathHelper.floor(continuous.minZ),
                MathHelper.ceil(continuous.maxX),
                MathHelper.ceil(continuous.maxY),
                MathHelper.ceil(continuous.maxZ)
        );
    }

    /**
     * EffectWorldInteraction:
     *
     * X/Z use an exclusive random upper bound.
     * Y is looped with <=, so its upper integer coordinate is included.
     */
    @Nonnull
    private static AxisAlignedBB createWorldInteractionGrid(
            @Nonnull AxisAlignedBB continuous
    ) {
        int minimumY = Math.max(
                0,
                MathHelper.floor(continuous.minY)
        );

        int maximumYInclusive = Math.min(
                255,
                MathHelper.ceil(continuous.maxY)
        );

        return new AxisAlignedBB(
                MathHelper.floor(continuous.minX),
                minimumY,
                MathHelper.floor(continuous.minZ),
                MathHelper.ceil(continuous.maxX),
                maximumYInclusive + 1,
                MathHelper.ceil(continuous.maxZ)
        );
    }

    /**
     * Digging:
     *
     * The exact vertical maximum depends on the randomly selected chunk's
     * top-filled segment. Rendering 0..256 is the complete safe envelope and
     * avoids loading or inspecting any chunks.
     */
    @Nonnull
    private static AxisAlignedBB createDiggingEnvelope(
            @Nonnull AxisAlignedBB continuous
    ) {
        return new AxisAlignedBB(
                MathHelper.floor(continuous.minX),
                0,
                MathHelper.floor(continuous.minZ),
                MathHelper.ceil(continuous.maxX),
                256,
                MathHelper.ceil(continuous.maxZ)
        );
    }

    /**
     * Creeper:
     *
     * The continuous AABB is used for the population check. Spawn Y is always
     * source-1, source or source+1, so include those full block levels too.
     */
    @Nonnull
    private static AxisAlignedBB createCreeperEnvelope(
            @Nonnull BlockPos source,
            @Nonnull Vec3d territory,
            @Nonnull AxisAlignedBB continuous
    ) {
        double spawnMinimumX =
                source.getX() + 0.5D - territory.x;
        double spawnMaximumX =
                source.getX() + 0.5D + territory.x;

        double spawnMinimumZ =
                source.getZ() + 0.5D - territory.z;
        double spawnMaximumZ =
                source.getZ() + 0.5D + territory.z;

        return new AxisAlignedBB(
                Math.min(continuous.minX, spawnMinimumX),
                Math.min(continuous.minY, source.getY() - 1.0D),
                Math.min(continuous.minZ, spawnMinimumZ),
                Math.max(continuous.maxX, spawnMaximumX),
                Math.max(continuous.maxY, source.getY() + 2.0D),
                Math.max(continuous.maxZ, spawnMaximumZ)
        );
    }

    /**
     * Jazz:
     *
     * Maximum box tested by its ten-step audio falloff loop.
     */
    @Nonnull
    private static AxisAlignedBB createJazzEnvelope(
            @Nonnull BlockPos source,
            @Nonnull Vec3d territory
    ) {
        return new AxisAlignedBB(source).grow(
                territory.x * JAZZ_MAXIMUM_SCALE,
                territory.y * JAZZ_MAXIMUM_SCALE,
                territory.z * JAZZ_MAXIMUM_SCALE
        );
    }

    /**
     * Converts inclusive integer block coordinates to an AABB whose maximum
     * coordinates are exclusive, as required by AxisAlignedBB rendering.
     */
    @Nonnull
    private static AxisAlignedBB fromInclusiveBlocks(
            int minimumX,
            int minimumY,
            int minimumZ,
            int maximumX,
            int maximumY,
            int maximumZ
    ) {
        return new AxisAlignedBB(
                minimumX,
                minimumY,
                minimumZ,
                maximumX + 1,
                maximumY + 1,
                maximumZ + 1
        );
    }
}