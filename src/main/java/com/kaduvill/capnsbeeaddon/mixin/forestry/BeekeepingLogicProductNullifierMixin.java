package com.kaduvill.capnsbeeaddon.mixin.forestry;

import com.kaduvill.capnsbeeaddon.registry.ModItems;
import forestry.api.apiculture.IBee;
import forestry.api.apiculture.IBeeHousing;
import net.bdew.gendustry.api.blocks.IIndustrialApiary;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(
        targets = "forestry.apiculture.BeekeepingLogic",
        remap = false
)
public abstract class BeekeepingLogicProductNullifierMixin {

    @Redirect(
            method =
                    "doProduction"
                            + "(Lforestry/api/apiculture/IBee;"
                            + "Lforestry/api/apiculture/IBeeHousing;"
                            + "Lforestry/api/apiculture/IBeeListener;)V",
            at = @At(
                    value = "INVOKE",
                    target =
                            "Lforestry/api/apiculture/IBee;"
                                    + "produceStacks"
                                    + "(Lforestry/api/apiculture/IBeeHousing;)"
                                    + "Lnet/minecraft/util/NonNullList;",
                    remap = false
            ),
            require = 1,
            allow = 1
    )
    private static NonNullList<ItemStack> capnsbeeaddon$nullifyProducts(
            IBee queen,
            IBeeHousing housing
    ) {
        if (housing instanceof IIndustrialApiary) {
            List<ItemStack> upgrades =
                    ((IIndustrialApiary) housing).getUpgrades();

            if (upgrades != null) {
                for (ItemStack stack : upgrades) {
                    if (!stack.isEmpty()
                            && stack.getItem()
                            == ModItems.PRODUCT_NULLIFIER) {
                        return NonNullList.create();
                    }
                }
            }
        }
        return queen.produceStacks(housing);
    }
}