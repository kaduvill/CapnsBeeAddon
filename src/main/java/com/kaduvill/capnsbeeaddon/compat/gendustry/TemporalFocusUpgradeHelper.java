package com.kaduvill.capnsbeeaddon.compat.gendustry;

import com.kaduvill.capnsbeeaddon.registry.ModItems;
import forestry.api.apiculture.IBeeHousing;
import net.bdew.gendustry.api.blocks.IIndustrialApiary;
import net.minecraft.item.ItemStack;

import java.util.List;

public final class TemporalFocusUpgradeHelper {

    private TemporalFocusUpgradeHelper() {
    }

    public static TemporalFocusMode getFocusMode(IBeeHousing housing) {
        if (!(housing instanceof IIndustrialApiary)) {
            return TemporalFocusMode.NONE;
        }

        List<ItemStack> upgrades =
                ((IIndustrialApiary) housing).getUpgrades();

        if (upgrades == null || upgrades.isEmpty()) {
            return TemporalFocusMode.NONE;
        }

        boolean tileEntityFound = false;
        boolean growthFound = false;

        for (ItemStack stack : upgrades) {
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() == ModItems.TEMPORAL_FOCUS_APIARY) {
                return TemporalFocusMode.APIARY;
            }

            if (stack.getItem() == ModItems.TEMPORAL_FOCUS_TILEENTITY) {
                tileEntityFound = true;
            } else if (stack.getItem() == ModItems.TEMPORAL_FOCUS_GROWTH) {
                growthFound = true;
            }
        }

        if (tileEntityFound) {
            return TemporalFocusMode.TILE_ENTITY;
        }

        return growthFound
                ? TemporalFocusMode.GROWTH
                : TemporalFocusMode.NONE;
    }

    public enum TemporalFocusMode {
        NONE,
        APIARY,
        TILE_ENTITY,
        GROWTH
    }
}