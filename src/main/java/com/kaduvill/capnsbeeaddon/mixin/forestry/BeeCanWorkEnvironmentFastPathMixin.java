package com.kaduvill.capnsbeeaddon.mixin.forestry;

import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import forestry.apiculture.genetics.Bee;
import forestry.apiculture.genetics.BeeGenome;
import net.bdew.gendustry.api.blocks.IIndustrialApiary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Bee.class, remap = false)
public abstract class BeeCanWorkEnvironmentFastPathMixin {

    @Shadow(remap = false)
    @Final
    private IBeeGenome genome;

    @Redirect(
            method =
                    "getCanWork"
                            + "(Lforestry/api/apiculture/IBeeHousing;)"
                            + "Ljava/util/Set;",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lforestry/api/apiculture/IBeeHousing;"
                                    + "isRaining()Z",
                    remap = false
            ),
            require = 1,
            allow = 1,
            remap = false
    )
    private boolean capnsbeeaddon$skipIrrelevantRainLookup(
            IBeeHousing housing
    ) {
        if (this.capnsbeeaddon$canUseEnvironmentFastPath(housing)) {
            try {
                if (this.genome.getToleratesRain()) {
                    return false;
                }
            } catch (RuntimeException ignored) {
                /*
                 * The active trait could not be conclusively read.
                 * Execute the original housing query.
                 */
            }
        }

        return housing.isRaining();
    }

    @Redirect(
            method =
                    "getCanWork"
                            + "(Lforestry/api/apiculture/IBeeHousing;)"
                            + "Ljava/util/Set;",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lforestry/api/apiculture/IBeeHousing;"
                                    + "getBlockLightValue()I",
                    remap = false
            ),
            require = 1,
            allow = 1,
            remap = false
    )
    private int capnsbeeaddon$skipIrrelevantBlockLightLookup(
            IBeeHousing housing
    ) {
        if (this.capnsbeeaddon$canUseEnvironmentFastPath(housing)) {
            try {
                if (this.genome.getNeverSleeps()) {
                    /*
                     * Fifteen is a valid maximum block-light value.
                     * Never Sleeps makes either original light branch pass.
                     */
                    return 15;
                }
            } catch (RuntimeException ignored) {
                /*
                 * The active trait could not be conclusively read.
                 * Execute the original housing query.
                 */
            }
        }

        return housing.getBlockLightValue();
    }

    @Redirect(
            method =
                    "getCanWork"
                            + "(Lforestry/api/apiculture/IBeeHousing;)"
                            + "Ljava/util/Set;",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lforestry/api/apiculture/IBeeHousing;"
                                    + "canBlockSeeTheSky()Z",
                    remap = false
            ),
            require = 1,
            allow = 1,
            remap = false
    )
    private boolean capnsbeeaddon$skipIrrelevantSkyLookup(
            IBeeHousing housing
    ) {
        if (this.capnsbeeaddon$canUseEnvironmentFastPath(housing)) {
            try {
                if (this.genome.getCaveDwelling()) {
                    return true;
                }
            } catch (RuntimeException ignored) {
                /*
                 * The active trait could not be conclusively read.
                 * Execute the original housing query.
                 */
            }
        }

        return housing.canBlockSeeTheSky();
    }

    /**
     * Restrict the fast path to the exact pinned housing and genome
     * implementations.
     *
     * Custom Industrial Apiaries, TileApiary subclasses and custom genome
     * implementations execute every original environmental query.
     */
    /**
     * Restrict the fast path to Gendustry Industrial Apiaries and Forestry's
     * known genome implementation.
     *
     * Custom genome implementations execute every original environmental query.
     */
    @Unique
    private boolean capnsbeeaddon$canUseEnvironmentFastPath(
            IBeeHousing housing
    ) {
        IBeeGenome activeGenome = this.genome;

        return housing instanceof IIndustrialApiary
                && activeGenome != null
                && activeGenome.getClass() == BeeGenome.class;
    }
}