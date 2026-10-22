package com.kaduvill.capnsbeeaddon.compat.jei.ClockworkBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Before/after examples of existing engines; never reads or modifies a world. */
public final class ClockworkBeeRecipe implements IRecipeWrapper {
    public static final String UID = CapnsBeeAddon.MODID + ".clockwork_bee";

    private final ItemStack engine;

    public ClockworkBeeRecipe(ItemStack engine) {
        if (engine.isEmpty()) throw new IllegalArgumentException("Missing Clockwork Engine");
        this.engine = engine.copy();
        this.engine.setCount(1);
    }

    private List<ItemStack> quantities() {
        ItemStack one = engine.copy();
        ItemStack two = engine.copy();
        two.setCount(2);
        return Arrays.asList(one, two);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(quantities()));
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(quantities()));
    }

    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        stacks.init(0, true, 18, 8);
        stacks.init(1, false, 124, 8);
        // Preserve paired 1/2 alternatives even when opened from the engine's R/U lookup.
        stacks.setOverrideDisplayFocus(null);
        stacks.set(ingredients);
        stacks.addTooltipCallback((index, isInput, stack, tooltip) ->
                tooltip.add(I18n.format("jei.capnsbeeaddon.clockwork.engine")));
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        int y = 30;
        for (String line : minecraft.fontRenderer.listFormattedStringToWidth(
                I18n.format("jei.capnsbeeaddon.clockwork.description"), recipeWidth)) {
            minecraft.fontRenderer.drawString(line,
                    (recipeWidth - minecraft.fontRenderer.getStringWidth(line)) / 2, y, 0x808080);
            y += minecraft.fontRenderer.FONT_HEIGHT;
        }
    }
}
