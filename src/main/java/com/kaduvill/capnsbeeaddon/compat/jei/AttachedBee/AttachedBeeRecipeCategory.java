package com.kaduvill.capnsbeeaddon.compat.jei.AttachedBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Compact bee + apiary illustration with one shared attachment tooltip. */
public final class AttachedBeeRecipeCategory implements IRecipeCategory<AttachedBeeRecipe> {
    private static final long CYCLE_NANOS = 1_000_000_000L;

    private final AttachedBeeRecipe.Kind kind;
    private final IDrawable background;
    private final IDrawable bee;
    private final IDrawable[] apiaries;
    private long lastDrawNanos = System.nanoTime();
    private long phaseNanos;

    public AttachedBeeRecipeCategory(IGuiHelper helper, AttachedBeeRecipe recipe, ItemStack... apiaries) {
        kind = recipe.getKind();
        background = helper.createBlankDrawable(160, 50);
        bee = helper.createDrawableIngredient(recipe.getInput());
        List<IDrawable> icons = new ArrayList<>(apiaries.length);
        for (ItemStack apiary : apiaries) {
            if (!apiary.isEmpty()) icons.add(helper.createDrawableIngredient(apiary.copy()));
        }
        if (icons.isEmpty()) throw new IllegalArgumentException("Missing apiary examples");
        this.apiaries = icons.toArray(new IDrawable[0]);
    }

    @Override public String getUid() { return kind.uid; }
    @Override public String getTitle() { return I18n.format(kind.titleKey); }
    @Override public String getModName() { return CapnsBeeAddon.NAME; }
    @Override public IDrawable getBackground() { return background; }
    @Override public IDrawable getIcon() { return bee; }

    @Override
    public void drawExtras(Minecraft minecraft) {
        long now = System.nanoTime();
        if (!GuiScreen.isShiftKeyDown()) {
            phaseNanos = (phaseNanos + now - lastDrawNanos) % (apiaries.length * CYCLE_NANOS);
        }
        lastDrawNanos = now;
        bee.draw(minecraft, 48, 8);
        minecraft.fontRenderer.drawString("+", 80 - minecraft.fontRenderer.getStringWidth("+") / 2,
                12, 0x808080);
        apiaries[(int) (phaseNanos / CYCLE_NANOS)].draw(minecraft, 96, 8);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseX >= 48 && mouseX < 112 && mouseY >= 8 && mouseY < 24) {
            return Arrays.asList(I18n.format("jei.capnsbeeaddon.attached.attach"),
                    TextFormatting.GRAY + I18n.format(kind.requirementKey));
        }
        return Collections.emptyList();
    }

    @Override
    public void setRecipe(IRecipeLayout layout, AttachedBeeRecipe recipe, IIngredients ingredients) {
        recipe.setRecipe(layout, ingredients);
    }
}
