package com.kaduvill.capnsbeeaddon.compat.careerbees;

/** Effective effect values shared by descriptions, JEI, and future gameplay tweaks. */
public final class CareerBeeEffectValues {
    private CareerBeeEffectValues() {}

    public static int getFortuneLevel() {
        // Career Bees 0.4.0: EffectDigging.digPosition's FORTUNE branch.
        // Keep this independent of the client-only description switch.
        return 2;
    }
}
