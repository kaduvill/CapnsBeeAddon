package com.kaduvill.capnsbeeaddon.mixin.careerbees.tweaks;

import com.kaduvill.capnsbeeaddon.compat.careerbees.CareerBeeEffectValues;
import com.rwtema.careerbees.effects.EffectOreCrushing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = EffectOreCrushing.class, remap = false)
public abstract class EffectOreCrushingOutputMixin {

    @ModifyConstant(
            method = "modifyStack(Lforestry/api/apiculture/IBeeGenome;"
                    + "Lnet/minecraft/item/ItemStack;Lforestry/api/apiculture/IBeeHousing;)"
                    + "Lnet/minecraft/item/ItemStack;",
            constant = @Constant(intValue = 2),
            remap = false,
            require = 1,
            allow = 1
    )
    private int capnsbeeaddon$minimumOutput(int original) {
        return CareerBeeEffectValues.getCrusherMinOutput();
    }

    @ModifyConstant(
            method = "modifyStack(Lforestry/api/apiculture/IBeeGenome;"
                    + "Lnet/minecraft/item/ItemStack;Lforestry/api/apiculture/IBeeHousing;)"
                    + "Lnet/minecraft/item/ItemStack;",
            constant = @Constant(intValue = 3),
            remap = false,
            require = 1,
            allow = 1
    )
    private int capnsbeeaddon$outputRange(int original) {
        return CareerBeeEffectValues.getCrusherMaxOutput() - CareerBeeEffectValues.getCrusherMinOutput() + 1;
    }
}
