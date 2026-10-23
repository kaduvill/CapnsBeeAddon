package com.kaduvill.capnsbeeaddon.compat.jei.MasonBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.block.BlockStone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Exact in-world conversions from Career Bees 0.4.0; no world access or effect simulation. */
public final class MasonBeeRecipes {
    public static final String UID = CapnsBeeAddon.MODID + ".mason_bee";
    public static final int BASE_TICKS = 10;

    private MasonBeeRecipes() {}

    public static List<Recipe> create() {
        return Collections.unmodifiableList(Arrays.asList(
                new Recipe(new ItemStack(Blocks.COBBLESTONE), stone(BlockStone.EnumType.STONE), true),
                new Recipe(stone(BlockStone.EnumType.STONE), new ItemStack(Blocks.STONEBRICK), false),
                new Recipe(stone(BlockStone.EnumType.ANDESITE), stone(BlockStone.EnumType.ANDESITE_SMOOTH), false),
                new Recipe(stone(BlockStone.EnumType.GRANITE), stone(BlockStone.EnumType.GRANITE_SMOOTH), false),
                new Recipe(stone(BlockStone.EnumType.DIORITE), stone(BlockStone.EnumType.DIORITE_SMOOTH), false)));
    }

    private static ItemStack stone(BlockStone.EnumType type) {
        return new ItemStack(Blocks.STONE, 1, type.getMetadata());
    }

    public static final class Recipe implements BeeEffectRecipe {
        private final ItemStack input;
        private final ItemStack output;
        private final boolean convertsFurther;

        private Recipe(ItemStack input, ItemStack output, boolean convertsFurther) {
            this.input = input.copy();
            this.output = output.copy();
            this.convertsFurther = convertsFurther;
        }

        @Override
        public ItemStack getInput() { return input.copy(); }

        @Override
        public boolean acceptsInput(ItemStack stack) {
            return !stack.isEmpty() && stack.getItem() == input.getItem()
                    && stack.getMetadata() == input.getMetadata();
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            ingredients.setInput(VanillaTypes.ITEM, input.copy());
            ingredients.setOutput(VanillaTypes.ITEM, output.copy());
        }

        @Override
        public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
            layout.getItemStacks().set(ingredients);
            if (convertsFurther) {
                layout.getItemStacks().addTooltipCallback((index, isInput, stack, tooltip) -> {
                    if (index == 1) tooltip.add(I18n.format("jei.capnsbeeaddon.mason.further"));
                });
            }
        }

        @Override
        public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
            String caption = I18n.format("jei.capnsbeeaddon.mason.description");
            minecraft.fontRenderer.drawString(caption,
                    (recipeWidth - minecraft.fontRenderer.getStringWidth(caption)) / 2, 32, 0x808080);
        }
    }
}
