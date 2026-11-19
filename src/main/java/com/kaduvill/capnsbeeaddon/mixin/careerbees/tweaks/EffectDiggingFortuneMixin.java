package com.kaduvill.capnsbeeaddon.mixin.careerbees.tweaks;

import com.kaduvill.capnsbeeaddon.compat.careerbees.CareerBeeEffectValues;
import com.rwtema.careerbees.effects.EffectDigging;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = EffectDigging.class, remap = false)
public abstract class EffectDiggingFortuneMixin {

    @ModifyConstant(
            method = "digPosition(Lforestry/api/apiculture/IBeeHousing;"
                    + "Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;"
                    + "Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/block/Block;)Z",
            constant = @Constant(intValue = 2),
            remap = false,
            require = 1,
            allow = 1
    )
    private int capnsbeeaddon$fortuneLevel(int original) {
        return CareerBeeEffectValues.getFortuneLevel();
    }
}
