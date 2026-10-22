package com.kaduvill.capnsbeeaddon.compat.jei.TradingBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.TradingBee.TradingBeeRecipes.Mode;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class TradingBeeRecipeCategory implements IRecipeCategory<TradingBeeRecipe> {
    public static final int BASE_TICKS = 100;
    private final Mode mode;
    private final TradingBeeRecipeLayout layout;
    private final IDrawable background;
    private final IDrawable slot;
    private final IDrawableStatic emptyArrow;
    private final IDrawableAnimated arrow;
    private final IDrawable icon;

    public TradingBeeRecipeCategory(IGuiHelper helper, Mode mode, ItemStack queen, int entries) {
        this.mode = mode;
        layout = new TradingBeeRecipeLayout(entries);
        background = helper.createBlankDrawable(TradingBeeRecipeLayout.WIDTH, layout.height);
        slot = helper.getSlotDrawable();
        emptyArrow = new TradingBeeArrow(0xFF8B8B8B);
        arrow = helper.createAnimatedDrawable(new TradingBeeArrow(0xFFFFFFFF),
                BASE_TICKS, IDrawableAnimated.StartDirection.LEFT, false);
        icon = queen.isEmpty() ? null : helper.createDrawableIngredient(queen);
    }

    @Override public String getUid() { return mode.uid; }
    @Override public String getTitle() { return I18n.format(mode.translation + ".title"); }
    @Override public String getModName() { return CapnsBeeAddon.NAME; }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void drawExtras(Minecraft minecraft) {
        GlStateManager.color(1, 1, 1, 1);
        slot.draw(minecraft, TradingBeeRecipeLayout.EMERALD_X, 0);
        for (int i = 0; i < layout.pageSize; i++) slot.draw(minecraft, layout.gridX(i), layout.gridY(i));
        String label = I18n.format(mode.translation + ".pool");
        minecraft.fontRenderer.drawString(label,
                (TradingBeeRecipeLayout.WIDTH - minecraft.fontRenderer.getStringWidth(label)) / 2,
                TradingBeeRecipeLayout.LABEL_Y, 0x404040);
        GlStateManager.pushMatrix();
        try {
            // Rotate the outline and fill together: Business flows up, Junk Seller flows down.
            GlStateManager.translate((TradingBeeRecipeLayout.WIDTH + (mode.isBusiness() ? -17 : 17)) / 2.0F,
                    TradingBeeRecipeLayout.ARROW_Y + (mode.isBusiness() ? 24 : 0), 0);
            GlStateManager.rotate(mode.isBusiness() ? -90 : 90, 0, 0, 1);
            emptyArrow.draw(minecraft, 0, 0);
            arrow.draw(minecraft, 0, 0);
        } finally {
            GlStateManager.popMatrix();
            GlStateManager.color(1, 1, 1, 1);
        }
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (!TradingBeeRecipeLayout.overArrow(mouseX, mouseY)) return Collections.emptyList();
        return Arrays.asList(I18n.format("jei.capnsbeeaddon.base_rate", BASE_TICKS),
                I18n.format("jei.capnsbeeaddon.trading.rate_modifiers"),
                I18n.format(mode.translation + ".attempts"));
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, TradingBeeRecipe recipe, IIngredients ingredients) {
        recipe.setRecipe(recipeLayout, ingredients);
    }
}
