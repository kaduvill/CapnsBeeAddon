package com.kaduvill.capnsbeeaddon.compat.jei.TemporalBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

/** One acceleration overview without item ingredients or outputs. */
public final class TemporalBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".temporal_bee";
    // Fixed target scan interval; Temporal does not use speed or production modifiers.
    public static final int BASE_TICKS = 20;

    @Override
    public ItemStack getInput() { return ItemStack.EMPTY; }

    @Override
    public boolean acceptsInput(ItemStack input) { return false; }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // The effect accelerates nearby blocks without consuming or producing items.
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        layout.getItemStacks().set(ingredients);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String caption = I18n.format("jei.capnsbeeaddon.temporal.description");
        minecraft.fontRenderer.drawString(caption,
                (recipeWidth - minecraft.fontRenderer.getStringWidth(caption)) / 2, 32, 0x808080);
    }
}
