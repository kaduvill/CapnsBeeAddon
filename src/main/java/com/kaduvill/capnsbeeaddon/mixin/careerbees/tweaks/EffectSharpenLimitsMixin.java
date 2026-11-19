package com.kaduvill.capnsbeeaddon.mixin.careerbees.tweaks;

import com.kaduvill.capnsbeeaddon.compat.careerbees.CareerBeeEffectValues;
import com.kaduvill.capnsbeeaddon.compat.careerbees.EquipmentBeeBonus;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.rwtema.careerbees.effects.EffectSharpen;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.DoubleUnaryOperator;

@Mixin(value = EffectSharpen.class, remap = false)
public abstract class EffectSharpenLimitsMixin {

    @Unique
    private volatile DoubleUnaryOperator capnsbeeaddon$limitedCurve;

    @WrapOperation(
            method = "modifyStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
            at = @At(value = "INVOKE", target = "Lcom/rwtema/careerbees/effects/EffectSharpen;"
                    + "getDoubleUnaryOperator(DDD)Ljava/util/function/DoubleUnaryOperator;"),
            remap = false, require = 1, allow = 1
    )
    private DoubleUnaryOperator capnsbeeaddon$configureCurve(
            double nativeMaximum, double gain, double precision,
            Operation<DoubleUnaryOperator> original
    ) {
        DoubleUnaryOperator curve = capnsbeeaddon$limitedCurve;
        if (curve == null) {
            double maximum = CareerBeeEffectValues.getSharpeningMaxDamageBonus();
            curve = EquipmentBeeBonus.boundedCurve(original.call(maximum, gain, precision), maximum);
            capnsbeeaddon$limitedCurve = curve;
        }
        return curve;
    }

    @WrapOperation(
            method = "modifyStack(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
            at = @At(value = "INVOKE", target = "Lcom/rwtema/careerbees/effects/EffectSharpen;"
                    + "boostStat(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/ai/attributes/IAttribute;"
                    + "Lnet/minecraft/inventory/EntityEquipmentSlot;ILjava/util/function/DoubleUnaryOperator;D)"
                    + "Lnet/minecraft/item/ItemStack;"),
            remap = false, require = 1, allow = 1
    )
    private ItemStack capnsbeeaddon$preserveExistingBonus(
            EffectSharpen effect, ItemStack stack, IAttribute attribute, EntityEquipmentSlot slot,
            int operation, DoubleUnaryOperator curve, double initialValue, Operation<ItemStack> original
    ) {
        if (!EquipmentBeeBonus.SHARPENING.canImprove(
                stack, slot, CareerBeeEffectValues.getSharpeningMaxDamageBonus())) return null;
        return original.call(effect, stack, attribute, slot, operation, curve, initialValue);
    }
}
