package com.kaduvill.capnsbeeaddon.mixin.careerbees;

import com.rwtema.careerbees.effects.EffectPower;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EffectPower.class, remap = false)
public abstract class EffectPowerOverflowMixin {

    @Redirect(
            method = "getRFRate(Lforestry/api/apiculture/IBeeGenome;"
                    + "Lforestry/api/apiculture/IBeeHousing;)I",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/MathHelper;ceil(F)I",
                    remap = true
            ),
            require = 1,
            allow = 1
    )
    private int capnsbeeaddon$saturateRFRate(float rate) {
        // Java's narrowing conversion saturates excessive output and maps NaN to zero.
        return (int) Math.ceil((double) rate);
    }
}
