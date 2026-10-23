package com.kaduvill.capnsbeeaddon.compat.jei.DireBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.Arrays;

/** One house-building overview with three indexed, world-placed block outputs. */
public final class DireBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".dire_bee";
    public static final int BASE_TICKS = 2;
    private static final int[] OUTPUT_X = {104, 122, 140};
    private static final int SLOT_Y = 8;

    private final IDrawable slot;

    public DireBeeRecipe(IGuiHelper helper) {
        slot = helper.getSlotDrawable();
    }

    @Override
    public ItemStack getInput() { return ItemStack.EMPTY; }

    @Override
    public boolean acceptsInput(ItemStack input) { return false; }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // These blocks are placed in the house, not delivered together as an item batch.
        ingredients.setOutputs(VanillaTypes.ITEM, Arrays.asList(
                new ItemStack(Blocks.COBBLESTONE), new ItemStack(Blocks.GLASS), new ItemStack(Blocks.TORCH)));
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        for (int i = 0; i < OUTPUT_X.length; i++) {
            stacks.init(i + 1, false, OUTPUT_X[i], SLOT_Y);
        }
        stacks.set(ingredients);
        stacks.addTooltipCallback((index, isInput, stack, tooltip) -> {
            if (!isInput) tooltip.add(I18n.format("jei.capnsbeeaddon.dire.placed"));
        });
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        for (int x : OUTPUT_X) slot.draw(minecraft, x, SLOT_Y);
        String caption = I18n.format("jei.capnsbeeaddon.dire.description");
        minecraft.fontRenderer.drawString(caption,
                (recipeWidth - minecraft.fontRenderer.getStringWidth(caption)) / 2, 32, 0x808080);
    }
}
