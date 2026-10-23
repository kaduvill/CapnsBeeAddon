package com.kaduvill.capnsbeeaddon.compat.jei.QuantumBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;

/** Catalyst-only Teleposition overview; example blocks are never JEI ingredients. */
public final class QuantumBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".quantum_bees";

    @Override public ItemStack getInput() { return ItemStack.EMPTY; }
    @Override public boolean acceptsInput(ItemStack input) { return false; }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // Neither consumed items nor produced items: world blocks only exchange positions.
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        // Intentionally no ingredient slots, including for the decorative block examples.
    }
}
