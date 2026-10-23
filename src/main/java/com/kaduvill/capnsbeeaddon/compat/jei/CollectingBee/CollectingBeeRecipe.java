package com.kaduvill.capnsbeeaddon.compat.jei.CollectingBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

/** One collection overview without item ingredients or outputs. */
public final class CollectingBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".collecting_bee";
    public static final int BASE_TICKS = 20;

    @Override
    public ItemStack getInput() { return ItemStack.EMPTY; }

    @Override
    public boolean acceptsInput(ItemStack input) { return false; }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // Collection moves dropped items into the housing; it is not an item recipe.
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        layout.getItemStacks().set(ingredients);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String caption = I18n.format("jei.capnsbeeaddon.collecting.description");
        minecraft.fontRenderer.drawString(caption,
                (recipeWidth - minecraft.fontRenderer.getStringWidth(caption)) / 2, 32, 0x808080);
    }
}
