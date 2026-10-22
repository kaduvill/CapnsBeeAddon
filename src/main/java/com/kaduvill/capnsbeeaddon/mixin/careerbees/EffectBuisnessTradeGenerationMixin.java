package com.kaduvill.capnsbeeaddon.mixin.careerbees;

import com.kaduvill.capnsbeeaddon.compat.careerbees.TradingBeeTradeExclusions;
import com.rwtema.careerbees.effects.EffectBuisness;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(value = EffectBuisness.class, remap = false)
public abstract class EffectBuisnessTradeGenerationMixin {

    @Unique
    private ArrayList<EntityVillager.ITradeList> capnsbeeaddon$filteredTrades;

    @Inject(method = "getTrades()Ljava/util/ArrayList;", at = @At("RETURN"), require = 1)
    private void capnsbeeaddon$skipExcludedTrades(
            CallbackInfoReturnable<ArrayList<EntityVillager.ITradeList>> cir) {
        if ((Object) this != EffectBuisness.INSTANCE && (Object) this != EffectBuisness.JUNK) {
            return;
        }
        ArrayList<EntityVillager.ITradeList> trades = cir.getReturnValue();
        if (trades != capnsbeeaddon$filteredTrades) {
            // This is Career Bees' own cached copy, not the villager registry's list.
            // Exact generator names preserve subclasses and avoid optional-mod class loading.
            trades.removeIf(TradingBeeTradeExclusions::isExcluded);
            capnsbeeaddon$filteredTrades = trades;
        }
    }

    @Redirect(method = "performEffect", require = 1, allow = 1,
            at = @At(value = "INVOKE", remap = false,
                    target = "Lnet/minecraftforge/items/IItemHandler;extractItem(IIZ)Lnet/minecraft/item/ItemStack;"))
    private ItemStack capnsbeeaddon$trackJunkSellerEmeralds(
            IItemHandler handler, int slot, int amount, boolean simulate) {
        // The sole extraction here is the initial simulated scan. Payment extraction is separate.
        ItemStack extracted = handler.extractItem(slot, amount, simulate);
        // Reuse the original empty-inventory exit before it shuffles or invokes trade generators.
        // Keeping the existing snapshot also preserves split payments and extraction restrictions.
        return (Object) this == EffectBuisness.JUNK && extracted.getItem() != Items.EMERALD
                ? ItemStack.EMPTY : extracted;
    }
}
