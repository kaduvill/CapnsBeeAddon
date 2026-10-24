package com.kaduvill.capnsbeeaddon.compat.careerbees;

import com.kaduvill.capnsbeeaddon.config.CapnsBeeAddonConfig;
import forestry.api.apiculture.IBeeHousing;
import forestry.apiculture.tiles.TileApiary;
import net.bdew.gendustry.api.blocks.IIndustrialApiary;

/** Common-side housing policy shared by the client overlays and server requests. */
public final class CareerTerritorySupport {

    private CareerTerritorySupport() {
    }

    public static boolean isEnabled(IBeeHousing housing) {
        CapnsBeeAddonConfig config = CapnsBeeAddonConfig.get();
        if (housing instanceof IIndustrialApiary) {
            return config.gendustryApiaryTerritory;
        }
        return config.forestryApiaryTerritory && needsServerBoundary(housing);
    }

    public static boolean needsServerBoundary(IBeeHousing housing) {
        // Avoid resolving Forestry's optional BuildCraft interfaces at compile time.
        Object source = housing;
        return source instanceof TileApiary;
    }

    public static boolean isAdvancedEnabled(IBeeHousing housing) {
        return CapnsBeeAddonConfig.get().advancedTemporalOverlay
                && isEnabled(housing);
    }
}
