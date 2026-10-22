package com.kaduvill.capnsbeeaddon.compat.careerbees;

import net.minecraft.entity.passive.EntityVillager;

/** Exact generators whose offers cannot be used by either trading bee. */
public final class TradingBeeTradeExclusions {
    private TradingBeeTradeExclusions() { }

    public static boolean isExcluded(EntityVillager.ITradeList generator) {
        if (generator == null) return false;
        // Names avoid linking optional mods and preserve subclasses that override generation.
        switch (generator.getClass().getName()) {
            case "net.minecraft.entity.passive.EntityVillager$TreasureMapForEmeralds":
            case "forestry.arboriculture.VillagerArboristTrades$GivePollenForEmeralds":
            case "blusunrize.immersiveengineering.common.util.IEVillagerHandler$OreveinMapForEmeralds":
            case "blusunrize.immersiveengineering.common.util.IEVillagerHandler$RevolverPieceForEmeralds":
                return true;
            default:
                return false;
        }
    }
}
