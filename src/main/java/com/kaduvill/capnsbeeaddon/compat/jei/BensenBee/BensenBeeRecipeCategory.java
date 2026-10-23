package com.kaduvill.capnsbeeaddon.compat.jei.BensenBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

/** Keeps the standard overview size and bee icon without processing arrows or slots. */
public final class BensenBeeRecipeCategory implements IRecipeCategory<BensenBeeRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    public BensenBeeRecipeCategory(IGuiHelper helper, ItemStack queen) {
        background = helper.createBlankDrawable(160, 50);
        icon = queen.isEmpty() ? null : helper.createDrawableIngredient(queen.copy());
    }

    @Override public String getUid() { return BensenBeeRecipe.UID; }
    @Override public String getTitle() { return I18n.format("jei.capnsbeeaddon.bensen.title"); }
    @Override public String getModName() { return CapnsBeeAddon.NAME; }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayout layout, BensenBeeRecipe recipe, IIngredients ingredients) {
        recipe.setRecipe(layout, ingredients);
    }
}
