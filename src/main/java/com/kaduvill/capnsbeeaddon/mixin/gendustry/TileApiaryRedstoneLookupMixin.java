package com.kaduvill.capnsbeeaddon.mixin.gendustry;

import com.kaduvill.capnsbeeaddon.mixin.gendustry.TileApiaryServerTickClosureAccessor;
import net.bdew.gendustry.gui.rscontrol.DataSlotRSMode;
import net.bdew.gendustry.gui.rscontrol.RSMode$;
import net.bdew.gendustry.machines.apiary.TileApiary;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
        targets =
                "net.bdew.gendustry.machines.apiary."
                        + "TileApiary$$anonfun$2$$anonfun$apply$mcV$sp$1",
        remap = false
)
public abstract class TileApiaryRedstoneLookupMixin {

    /*
     * Gendustry's rsmode is a stable val. Its contained value changes when
     * the player changes redstone mode, so retaining the DataSlot itself
     * does not cache or stale the selected mode.
     *
     * Transient also preserves safe fallback behavior in the unlikely event
     * that this Serializable Scala closure is ever deserialized.
     */
    @Unique
    private transient DataSlotRSMode capnsbeeaddon$redstoneMode;

    @Dynamic(
            "Captures the owner of Gendustry's generated "
                    + "withSuspendedUpdates closure"
    )
    @Inject(
            method =
                    "<init>("
                            + "Lnet/bdew/gendustry/machines/apiary/"
                            + "TileApiary$$anonfun$2;"
                            + ")V",
            at = @At("RETURN"),
            remap = false,
            require = 1,
            allow = 1
    )
    private void capnsbeeaddon$captureRedstoneMode(
            @Coerce Object outer,
            CallbackInfo ci
    ) {
        TileApiary apiary =
                ((TileApiaryServerTickClosureAccessor) outer)
                        .capnsbeeaddon$getApiary();

        if (apiary != null) {
            capnsbeeaddon$redstoneMode =
                    ((TileApiaryAccessor) (Object) apiary)
                            .capnsbeeaddon$getRedstoneMode();
        }
    }

    @Dynamic(
            "Redirects Gendustry's redstone query inside its generated "
                    + "withSuspendedUpdates closure"
    )
    @Redirect(
            method = "apply$mcV$sp",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lnet/minecraft/world/World;"
                                    + "isBlockIndirectlyGettingPowered"
                                    + "(Lnet/minecraft/util/math/BlockPos;)I",
                    remap = true
            ),
            require = 1,
            allow = 1
    )
    private int capnsbeeaddon$skipUnusedRedstoneLookup(
            World world,
            BlockPos pos
    ) {
        DataSlotRSMode mode = capnsbeeaddon$redstoneMode;

        /*
         * Skip only the two modes explicitly known not to consume the
         * powered result. Null and any unexpected future mode retain the
         * original Gendustry query.
         */
        if (mode != null
                && (mode.$colon$eq$eq(RSMode$.MODULE$.ALWAYS())
                || mode.$colon$eq$eq(RSMode$.MODULE$.NEVER()))) {
            return 0;
        }

        return world.isBlockIndirectlyGettingPowered(pos);
    }
}