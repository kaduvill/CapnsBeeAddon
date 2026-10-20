package com.kaduvill.capnsbeeaddon.compat.jei.common;

import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

public interface BeeEffectRecipe extends IRecipeWrapper {
    ItemStack getInput();

    boolean acceptsInput(ItemStack input);

    void setRecipe(IRecipeLayout layout, IIngredients ingredients);
}
