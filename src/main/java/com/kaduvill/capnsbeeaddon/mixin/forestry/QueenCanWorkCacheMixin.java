package com.kaduvill.capnsbeeaddon.mixin.forestry;

import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.core.IErrorState;
import net.bdew.gendustry.api.blocks.IIndustrialApiary;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(
        targets = "forestry.apiculture.BeekeepingLogic$QueenCanWorkCache",
        remap = false
)
public abstract class QueenCanWorkCacheMixin {

    @Shadow(remap = false)
    private Set<IErrorState> queenCanWorkCached;

    @Unique
    private long capnsbeeaddon$lastIndustrialApiaryWorldTick = Long.MIN_VALUE;

    @Inject(
            method = "queenCanWork(Lforestry/api/apiculture/IBee;Lforestry/api/apiculture/IBeeHousing;)Ljava/util/Set;",
            at = @At("HEAD"),
            cancellable = true,
            require = 1,
            allow = 1,
            remap = false
    )
    private void capnsbeeaddon$reuseWithinWorldTick(
            IBee queen,
            IBeeHousing beeHousing,
            CallbackInfoReturnable<Set<IErrorState>> cir
    ) {
        if (!(beeHousing instanceof IIndustrialApiary)) {
            return;
        }

        World world = beeHousing.getWorldObj();
        if (world == null || world.isRemote) {
            return;
        }

        long worldTick = world.getTotalWorldTime();
        if (worldTick == capnsbeeaddon$lastIndustrialApiaryWorldTick) {
            cir.setReturnValue(queenCanWorkCached);
            return;
        }

        capnsbeeaddon$lastIndustrialApiaryWorldTick = worldTick;
    }

    @Inject(
            method = "clear()V",
            at = @At("HEAD"),
            require = 1,
            allow = 1,
            remap = false
    )
    private void capnsbeeaddon$clearWorldTickGate(CallbackInfo ci) {
        capnsbeeaddon$lastIndustrialApiaryWorldTick = Long.MIN_VALUE;
    }
}