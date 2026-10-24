package com.kaduvill.capnsbeeaddon.client;

import com.kaduvill.capnsbeeaddon.compat.careerbees.CareerBeeEffectValues;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

/** Per-effect templates and arguments; the tooltip mixin only routes calls here. */
public final class BeeEffectDescriptions {
    private BeeEffectDescriptions() {}

    public static String format(String key, Object[] arguments, Operation<String> original) {
        // Add an effect's case and addon lang template here; put its shared values
        // in CareerBeeEffectValues so JEI remains independent of the tooltip switch.
        switch (key) {
            case "careerbees.allele.effect.miner.fortune.desc":
                return original.call("effect.capnsbeeaddon.miner.fortune.desc",
                        new Object[]{CareerBeeEffectValues.getFortuneLevel()});
            case "careerbees.allele.effect.steal.mob.desc":
                return original.call("effect.capnsbeeaddon.police.desc", arguments);
            default:
                return original.call(key, arguments);
        }
    }
}
