package com.kaduvill.capnsbeeaddon.compat.jei.common;

import javax.annotation.Nullable;

/** Static housing-effect behavior, independent of the installed genes and upgrades. */
public enum BeeEffectScaling {
    ATTEMPT_RATE(true, true, "jei.capnsbeeaddon.effect_modifiers.attempt_rate"),
    ENERGY_OUTPUT(true, true, "jei.capnsbeeaddon.effect_modifiers.energy_output"),
    CLOCKWORK_WINDING(true, false, "jei.capnsbeeaddon.effect_modifiers.clockwork_winding"),
    UNAFFECTED(false, false, null);

    final boolean speedAffected;
    final boolean productionAffected;
    @Nullable
    final String explanationKey;

    BeeEffectScaling(boolean speedAffected, boolean productionAffected, @Nullable String explanationKey) {
        this.speedAffected = speedAffected;
        this.productionAffected = productionAffected;
        this.explanationKey = explanationKey;
    }
}
