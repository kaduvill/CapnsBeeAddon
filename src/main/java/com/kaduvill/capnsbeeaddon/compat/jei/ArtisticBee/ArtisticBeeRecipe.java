package com.kaduvill.capnsbeeaddon.compat.jei.ArtisticBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

/** One wall decoration overview with an indexed Painting item for navigation. */
public final class ArtisticBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".artistic_bee";
    public static final int BASE_TICKS = 600;

    @Override
    public ItemStack getInput() { return ItemStack.EMPTY; }

    @Override
    public boolean acceptsInput(ItemStack input) { return false; }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // Index the Painting item; the tooltip explains that the bee places it in the world.
        ingredients.setOutput(VanillaTypes.ITEM, new ItemStack(Items.PAINTING));
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        stacks.set(ingredients);
        stacks.addTooltipCallback((index, isInput, stack, tooltip) -> {
            if (index == 1) {
                tooltip.add(I18n.format("jei.capnsbeeaddon.artistic.placed"));
                tooltip.add(I18n.format("jei.capnsbeeaddon.artistic.wall"));
            }
        });
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String caption = I18n.format("jei.capnsbeeaddon.artistic.description");
        minecraft.fontRenderer.drawString(caption,
                (recipeWidth - minecraft.fontRenderer.getStringWidth(caption)) / 2, 32, 0x808080);
    }

}
