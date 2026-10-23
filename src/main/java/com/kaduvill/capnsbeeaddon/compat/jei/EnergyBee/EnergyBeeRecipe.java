package com.kaduvill.capnsbeeaddon.compat.jei.EnergyBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Energy generation overview; the Forestry symbol is not an item output. */
public final class EnergyBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".energy_bee";
    public static final int BASE_TICKS = 1;
    // Default Slow (0.8) genome with neutral production modifiers.
    private static final int BASE_RF_PER_TICK = 256;

    private final IDrawable energyIcon;

    public EnergyBeeRecipe(IGuiHelper helper) {
        energyIcon = helper.drawableBuilder(
                new ResourceLocation("forestry", "textures/gui/misc/energy.png"), 0, 0, 16, 16)
                .setTextureSize(16, 16).build();
    }

    @Override
    public ItemStack getInput() { return ItemStack.EMPTY; }

    @Override
    public boolean acceptsInput(ItemStack input) { return false; }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // The effect powers adjacent blocks without consuming or producing items.
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        layout.getItemStacks().set(ingredients);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        energyIcon.draw(minecraft, 125, 9);
        String caption = I18n.format("jei.capnsbeeaddon.energy.description");
        minecraft.fontRenderer.drawString(caption,
                (recipeWidth - minecraft.fontRenderer.getStringWidth(caption)) / 2, 32, 0x808080);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseX >= 124 && mouseX < 142 && mouseY >= 8 && mouseY < 26) {
            return Arrays.asList(I18n.format("jei.capnsbeeaddon.energy.base_rate", BASE_RF_PER_TICK),
                    I18n.format("jei.capnsbeeaddon.trading.rate_modifiers"));
        }
        return Collections.emptyList();
    }
}
