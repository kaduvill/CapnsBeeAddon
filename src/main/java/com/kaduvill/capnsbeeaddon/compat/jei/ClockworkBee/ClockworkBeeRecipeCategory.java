package com.kaduvill.capnsbeeaddon.compat.jei.ClockworkBee;

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

import java.util.Collections;
import java.util.List;

/** Standard bee layout for automatic winding of existing engines. */
public final class ClockworkBeeRecipeCategory implements IRecipeCategory<ClockworkBeeRecipe> {
    private static final int BASE_TICKS = 1;

    private final IDrawable background;
    private final IDrawable slot;
    private final IDrawableStatic arrowBackground;
    private final IDrawableAnimated arrow;
    private final IDrawable icon;

    public ClockworkBeeRecipeCategory(IGuiHelper helper, ItemStack queen) {
        background = helper.createBlankDrawable(160, 50);
        slot = helper.getSlotDrawable();
        ResourceLocation texture = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");
        arrowBackground = helper.createDrawable(texture, 24, 132, 24, 17);
        // A readable visual cycle; the actual hive effect checks every working tick.
        arrow = helper.drawableBuilder(texture, 82, 128, 24, 17)
                .buildAnimated(20, IDrawableAnimated.StartDirection.LEFT, false);
        icon = queen.isEmpty() ? null : helper.createDrawableIngredient(queen);
    }

    @Override public String getUid() { return ClockworkBeeRecipe.UID; }
    @Override public String getTitle() { return I18n.format("jei.capnsbeeaddon.clockwork.title"); }
    @Override public String getModName() { return CapnsBeeAddon.NAME; }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void drawExtras(Minecraft minecraft) {
        slot.draw(minecraft, 18, 8);
        slot.draw(minecraft, 124, 8);
        arrowBackground.draw(minecraft, 68, 8);
        arrow.draw(minecraft, 68, 8);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseX >= 68 && mouseX < 68 + arrow.getWidth()
                && mouseY >= 8 && mouseY < 8 + arrow.getHeight()) {
            return Collections.singletonList(I18n.format("jei.capnsbeeaddon.base_rate_single", BASE_TICKS));
        }
        return Collections.emptyList();
    }

    @Override
    public void setRecipe(IRecipeLayout layout, ClockworkBeeRecipe recipe, IIngredients ingredients) {
        recipe.setRecipe(layout, ingredients);
    }
}
