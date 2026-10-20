package com.kaduvill.capnsbeeaddon.compat.jei.common;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Shared two-slot bee layout with optional pedestal and arrow details. */
public final class BeeEffectRecipeCategory implements IRecipeCategory<BeeEffectRecipe> {
    private final String uid;
    private final String titleKey;
    private final String baseRateKey;
    @Nullable
    private final String intervalNoteKey;
    private final int baseTicks;
    private final IDrawable background;
    private final IDrawable slot;
    private final IDrawableStatic arrowBackground;
    private final IDrawableAnimated arrow;
    private final IDrawable icon;
    private final ItemStack setup;

    public BeeEffectRecipeCategory(IGuiHelper helper, String uid, String titleKey, int baseTicks,
                                   String baseRateKey, @Nullable String intervalNoteKey, ItemStack queen, ItemStack setup) {
        this.uid = uid;
        this.titleKey = titleKey;
        this.baseRateKey = baseRateKey;
        this.baseTicks = baseTicks;
        this.intervalNoteKey = intervalNoteKey;
        this.setup = setup.copy();
        background = helper.createBlankDrawable(160, 50);
        slot = helper.getSlotDrawable();
        ResourceLocation texture = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");
        arrowBackground = helper.createDrawable(texture, 24, 132, 24, 17);
        arrow = helper.drawableBuilder(texture, 82, 128, 24, 17)
                .buildAnimated(baseTicks, IDrawableAnimated.StartDirection.LEFT, false);
        icon = queen.isEmpty() ? null : helper.createDrawableIngredient(queen);
    }

    @Override
    public String getUid() { return uid; }

    @Override
    public String getTitle() { return I18n.format(titleKey); }

    @Override
    public String getModName() { return CapnsBeeAddon.NAME; }

    @Override
    public IDrawable getBackground() { return background; }

    @Override
    public IDrawable getIcon() { return icon; }

    @Override
    public void drawExtras(Minecraft minecraft) {
        slot.draw(minecraft, 18, 8);
        slot.draw(minecraft, 124, 8);
        arrowBackground.draw(minecraft, 68, 8);
        arrow.draw(minecraft, 68, 8);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseX >= 68 && mouseX < 68 + arrow.getWidth() && mouseY >= 8 && mouseY < 8 + arrow.getHeight()) {
            if (intervalNoteKey == null) return Collections.singletonList(I18n.format(baseRateKey, baseTicks));
            return Arrays.asList(I18n.format(baseRateKey, baseTicks), I18n.format(intervalNoteKey));
        }
        return Collections.emptyList();
    }

    @Override
    public void setRecipe(IRecipeLayout layout, BeeEffectRecipe recipe, IIngredients ingredients) {
        layout.getItemStacks().init(0, true, 18, 8);
        layout.getItemStacks().init(1, false, 124, 8);
        recipe.setRecipe(layout, ingredients);
        if (!setup.isEmpty()) {
            layout.getItemStacks().init(2, true, 18, 28);
            layout.getItemStacks().set(2, setup.copy());
        }
    }
}
