package com.kaduvill.capnsbeeaddon.mixin.gendustry;

import net.bdew.lib.covers.TileCoverable;
import net.bdew.lib.covers.TileCoverable$class;
import net.bdew.lib.data.DataSlotOption;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import scala.Option;
import scala.collection.immutable.Map;

@Mixin(
        targets = "net.bdew.gendustry.machines.apiary.TileApiary",
        remap = false
)
public abstract class TileApiaryEmptyCoverTickMixin {

    @Unique
    private static final byte CAPNSBEEADDON$COVERS_UNKNOWN = 0;

    @Unique
    private static final byte CAPNSBEEADDON$COVERS_EMPTY = 1;

    /**
     * At least one cover exists, or the cover structure could not be
     * conclusively validated. In both cases, run BDLib's original code.
     */
    @Unique
    private static final byte CAPNSBEEADDON$COVERS_RUN_ORIGINAL = 2;

    /**
     * EnumFacing.values() clones its array. Keep one private copy rather
     * than cloning it after every cover invalidation.
     */
    @Unique
    private static final EnumFacing[] CAPNSBEEADDON$COVER_SIDES =
            EnumFacing.values();

    /**
     * JVM zero-initialization intentionally represents UNKNOWN.
     *
     * This state is transient and is not written to NBT.
     */
    @Unique
    private byte capnsbeeaddon$coverState;

    /**
     * Redirect the single Scala trait-helper invocation in the concrete
     * TileApiary.tickCovers()V forwarder.
     *
     * This avoids cancellable @Inject callback allocation in the hot path.
     */
    @Redirect(
            method = "tickCovers()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/bdew/lib/covers/TileCoverable$class;"
                            + "tickCovers("
                            + "Lnet/bdew/lib/covers/TileCoverable;"
                            + ")V",
                    remap = false
            ),
            require = 1,
            allow = 1,
            remap = false
    )
    private void capnsbeeaddon$skipKnownEmptyCoverTraversal(
            TileCoverable tile
    ) {
        byte state = this.capnsbeeaddon$coverState;

        if (state == CAPNSBEEADDON$COVERS_EMPTY) {
            return;
        }

        if (state == CAPNSBEEADDON$COVERS_UNKNOWN
                && this.capnsbeeaddon$scanAndCacheCoverState(tile)) {
            return;
        }

        TileCoverable$class.tickCovers(tile);
    }

    /**
     * Redirect the single helper invocation in onCoversChanged()V.
     *
     * The current BDLib helper is empty, but it is still called unchanged
     * so the patch preserves the exact original method structure.
     */
    @Redirect(
            method = "onCoversChanged()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/bdew/lib/covers/TileCoverable$class;"
                            + "onCoversChanged("
                            + "Lnet/bdew/lib/covers/TileCoverable;"
                            + ")V",
                    remap = false
            ),
            require = 1,
            allow = 1,
            remap = false
    )
    private void capnsbeeaddon$invalidateCoverState(
            TileCoverable tile
    ) {
        this.capnsbeeaddon$coverState =
                CAPNSBEEADDON$COVERS_UNKNOWN;

        TileCoverable$class.onCoversChanged(tile);
    }

    /**
     * Returns true only when all six canonical cover slots are conclusively
     * present and empty.
     *
     * Any cover or unexpected structure permanently selects the original
     * implementation until onCoversChanged() invalidates the cache.
     */
    @Unique
    private boolean capnsbeeaddon$scanAndCacheCoverState(
            TileCoverable tile
    ) {
        try {
            Map<EnumFacing, DataSlotOption<ItemStack>> covers =
                    tile.covers();

            if (covers == null
                    || covers.size()
                    != CAPNSBEEADDON$COVER_SIDES.length) {
                this.capnsbeeaddon$coverState =
                        CAPNSBEEADDON$COVERS_RUN_ORIGINAL;
                return false;
            }

            for (EnumFacing side : CAPNSBEEADDON$COVER_SIDES) {
                /*
                 * Map.apply throws when the key is absent. That is caught
                 * below and falls back to BDLib's original implementation.
                 */
                DataSlotOption<ItemStack> slot = covers.apply(side);

                if (slot == null) {
                    this.capnsbeeaddon$coverState =
                            CAPNSBEEADDON$COVERS_RUN_ORIGINAL;
                    return false;
                }

                Object rawValue = slot.value();

                if (!(rawValue instanceof Option)) {
                    this.capnsbeeaddon$coverState =
                            CAPNSBEEADDON$COVERS_RUN_ORIGINAL;
                    return false;
                }

                Option<?> value = (Option<?>) rawValue;

                if (value.isDefined()) {
                    this.capnsbeeaddon$coverState =
                            CAPNSBEEADDON$COVERS_RUN_ORIGINAL;
                    return false;
                }
            }

            this.capnsbeeaddon$coverState =
                    CAPNSBEEADDON$COVERS_EMPTY;
            return true;
        } catch (RuntimeException ignored) {
            /*
             The optimization could not conclusively prove emptiness.
             Run BDLib's original implementation from now on.
             */
            this.capnsbeeaddon$coverState =
                    CAPNSBEEADDON$COVERS_RUN_ORIGINAL;
            return false;
        }
    }
}