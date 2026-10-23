package com.kaduvill.capnsbeeaddon.compat.jei.QuantumBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.Collections;
import java.util.List;

/** Draw-only block examples with manual tooltips; no world access or effect simulation. */
public final class QuantumBeeRecipeCategory implements IRecipeCategory<QuantumBeeRecipe> {
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable stone;
    private final IDrawable granite;

    public QuantumBeeRecipeCategory(IGuiHelper helper, ItemStack queen) {
        background = helper.createBlankDrawable(160, 50);
        icon = helper.createDrawableIngredient(queen.copy());
        stone = helper.createDrawableIngredient(new ItemStack(Blocks.STONE));
        granite = helper.createDrawableIngredient(new ItemStack(Blocks.STONE, 1, 1));
    }

    @Override public String getUid() { return QuantumBeeRecipe.UID; }
    @Override public String getTitle() { return I18n.format("jei.capnsbeeaddon.quantum.title"); }
    @Override public String getModName() { return CapnsBeeAddon.NAME; }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void drawExtras(Minecraft minecraft) {
        stone.draw(minecraft, 18, 8);
        granite.draw(minecraft, 124, 8);
        // Static opposite arrows: Teleposition has no fixed processing interval of its own.
        int color = 0xFF808080;
        Gui.drawRect(69, 11, 90, 13, color);
        Gui.drawRect(69, 20, 90, 22, color);
        for (int i = 0; i < 4; i++) {
            Gui.drawRect(85 + i, 8 + i, 87 + i, 16 - i, color);
            Gui.drawRect(73 - i, 17 + i, 75 - i, 25 - i, color);
        }
        String caption = I18n.format("jei.capnsbeeaddon.quantum.description");
        minecraft.fontRenderer.drawString(caption,
                (160 - minecraft.fontRenderer.getStringWidth(caption)) / 2, 32, 0x808080);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseY >= 8 && mouseY < 24
                && ((mouseX >= 18 && mouseX < 34) || (mouseX >= 124 && mouseX < 140))) {
            return Collections.singletonList(TextFormatting.RED
                    + I18n.format("jei.capnsbeeaddon.quantum.eligible"));
        }
        if (mouseX >= 68 && mouseX < 92 && mouseY >= 8 && mouseY < 25) {
            return Collections.singletonList(I18n.format("jei.capnsbeeaddon.quantum.matching"));
        }
        return Collections.emptyList();
    }

    @Override
    public void setRecipe(IRecipeLayout layout, QuantumBeeRecipe recipe, IIngredients ingredients) {
        recipe.setRecipe(layout, ingredients);
    }
}
