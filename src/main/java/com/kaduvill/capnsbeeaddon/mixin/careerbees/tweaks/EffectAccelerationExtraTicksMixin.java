package com.kaduvill.capnsbeeaddon.mixin.careerbees.tweaks;

import com.kaduvill.capnsbeeaddon.config.CapnsBeeAddonConfig;
import com.rwtema.careerbees.effects.EffectAcceleration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value = EffectAcceleration.class, remap = false)
public abstract class EffectAccelerationExtraTicksMixin {

    @ModifyConstant(
            method = "worldTick(Lnet/minecraftforge/fml/common/gameevent/TickEvent$WorldTickEvent;)V",
            constant = @Constant(intValue = 4),
            remap = false,
            require = 1,
            allow = 1
    )
    private int capnsbeeaddon$extraTicksPerPhase(int original) {
        return CapnsBeeAddonConfig.get().temporalBeeExtraTicks;
    }
}
