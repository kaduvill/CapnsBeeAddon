package com.kaduvill.capnsbeeaddon.compat.gendustry;

import com.kaduvill.capnsbeeaddon.registry.ModItems;
import forestry.api.apiculture.IBeeHousing;
import net.bdew.gendustry.api.blocks.IIndustrialApiary;
import net.minecraft.item.ItemStack;

import java.util.List;

public final class TemporalFocusUpgradeHelper {

    private TemporalFocusUpgradeHelper() {
    }

    public static boolean hasFocusUpgrade(IBeeHousing housing) {
        if (!(housing instanceof IIndustrialApiary)) {
            return false;
        }

        List<ItemStack> upgrades = ((IIndustrialApiary) housing).getUpgrades();
        if (upgrades == null || upgrades.isEmpty()) {
            return false;
        }

        for (ItemStack stack : upgrades) {
            if (!stack.isEmpty()
                    && stack.getItem() == ModItems.TEMPORAL_FOCUS_APIARY) {
                return true;
            }
        }

        return false;
    }
}
