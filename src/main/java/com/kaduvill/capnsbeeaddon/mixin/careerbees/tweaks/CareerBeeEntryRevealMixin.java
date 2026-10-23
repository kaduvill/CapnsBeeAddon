package com.kaduvill.capnsbeeaddon.mixin.careerbees.tweaks;

import com.kaduvill.capnsbeeaddon.config.CapnsBeeAddonConfig;
import com.rwtema.careerbees.bees.CareerBeeEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CareerBeeEntry.class, remap = false)
public abstract class CareerBeeEntryRevealMixin {

    @Shadow
    @Final
    private String name;

    @Inject(
            method = "setIsSecret()Lcom/rwtema/careerbees/bees/CareerBeeEntry;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 1,
            allow = 1
    )
    private void capnsbeeaddon$revealSelectedBee(CallbackInfoReturnable<CareerBeeEntry> cir) {
        CapnsBeeAddonConfig config = CapnsBeeAddonConfig.get();
        boolean reveal;
        // The static species fields are not assigned yet when this method runs.
        switch (name) {
            case "dire":
                reveal = config.revealDireBee;
                break;
            case "soaring":
                reveal = config.revealSoaringBee;
                break;
            case "bensen":
                reveal = config.revealBensenBee;
                break;
            default:
                return;
        }
        if (reveal) {
            // Native secrecy also excludes the built species from discovery totals.
            // Keep that instruction while skipping both secret flags at registration.
            cir.setReturnValue(((CareerBeeEntry) (Object) this).setIsNotCounted());
        }
    }
}
