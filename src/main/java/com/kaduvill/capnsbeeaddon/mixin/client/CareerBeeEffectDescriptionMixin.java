package com.kaduvill.capnsbeeaddon.mixin.client;

import com.kaduvill.capnsbeeaddon.client.BeeEffectDescriptions;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.rwtema.careerbees.tooltip.TooltipHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = TooltipHandler.class, remap = false)
public abstract class CareerBeeEffectDescriptionMixin {
    @WrapOperation(
            method = "addTooltip("
                    + "Lnet/minecraftforge/client/event/GuiScreenEvent$DrawScreenEvent$Post;"
                    + "Lnet/minecraft/client/gui/inventory/GuiContainer;"
                    + "Lforestry/api/genetics/IAllele;"
                    + "Lforestry/api/apiculture/IBeeGenome;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/resources/I18n;"
                            + "format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;",
                    remap = true
            ),
            remap = false,
            require = 3,
            expect = 3,
            allow = 3
    )
    private String capnsbeeaddon$describeEffect(String key, Object[] arguments, Operation<String> original) {
        return BeeEffectDescriptions.format(key, arguments, original);
    }
}
