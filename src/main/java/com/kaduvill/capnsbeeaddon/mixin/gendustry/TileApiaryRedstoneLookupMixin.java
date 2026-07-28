package com.kaduvill.capnsbeeaddon.mixin.gendustry;

import net.bdew.gendustry.gui.rscontrol.DataSlotRSMode;
import net.bdew.gendustry.gui.rscontrol.RSMode$;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(
        targets =
                "net.bdew.gendustry.machines.apiary."
                        + "TileApiary$$anonfun$2$$anonfun$apply$mcV$sp$1",
        remap = false
)
public abstract class TileApiaryRedstoneLookupMixin {

    /*
     * This server-tick closure belongs to exactly one TileApiary for its
     * entire lifetime. Cache its accessor after the first successful lookup.
     *
     * The closure already retains the same apiary through its generated
     * $outer chain, so this does not extend the tile's lifetime.
     */
    @Unique
    private TileApiaryAccessor capnsbeeaddon$apiary;

    @Dynamic("Targets Gendustry's Scala-generated server-tick closure")
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
        TileApiaryAccessor apiary = capnsbeeaddon$apiary;

        if (apiary == null) {
            TileEntity tile = world.getTileEntity(pos);

            /*
             Preserve original behavior if something unexpected changed
             the tile or prevented the accessor from being applied.

             Do not cache failure: retry on a later update.
             */
            if (!(tile instanceof TileApiaryAccessor)) {
                return world.isBlockIndirectlyGettingPowered(pos);
            }

            apiary = (TileApiaryAccessor) tile;
            capnsbeeaddon$apiary = apiary;
        }

        DataSlotRSMode mode =
                apiary.capnsbeeaddon$getRedstoneMode();

        if (mode == null) {
            return world.isBlockIndirectlyGettingPowered(pos);
        }

        boolean usesRedstone =
                mode.$colon$eq$eq(RSMode$.MODULE$.RS_ON())
                        || mode.$colon$eq$eq(
                        RSMode$.MODULE$.RS_OFF()
                );

        return usesRedstone
                ? world.isBlockIndirectlyGettingPowered(pos)
                : 0;
    }
}