package com.kaduvill.capnsbeeaddon.mixin.careerbees;

import com.rwtema.careerbees.effects.EffectDigging;
import forestry.api.apiculture.IBeeGenome;
import forestry.api.apiculture.IBeeHousing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EffectDigging.class, remap = false)
public abstract class EffectDiggingMixin {

    @Inject(
            method = "handleBlock(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;"
                    + "Lnet/minecraft/util/EnumFacing;Lforestry/api/apiculture/IBeeGenome;"
                    + "Lforestry/api/apiculture/IBeeHousing;)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1
    )
    private void capnsbeeaddon$preventUnbreakableGunMining(
            World world,
            BlockPos pos,
            EnumFacing facing,
            IBeeGenome genome,
            IBeeHousing housing,
            CallbackInfoReturnable<Boolean> cir
    ) {
        // handleBlock is the Bee Gun path; apiaries call processPosition instead.
        // Reject before digPosition generates drops or changes capture state.
        IBlockState state = world.getBlockState(pos);
        if (state.getBlockHardness(world, pos) < 0.0F) {
            cir.setReturnValue(false);
        }
    }
}
