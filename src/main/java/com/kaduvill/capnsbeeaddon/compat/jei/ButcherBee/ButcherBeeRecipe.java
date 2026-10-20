package com.kaduvill.capnsbeeaddon.compat.jei.ButcherBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** One cached overview of animal/drop examples; never creates animals or evaluates loot tables. */
public final class ButcherBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".butcher_bee";
    public static final int BASE_TICKS = 20;
    private static final long CYCLE_NANOS = 1_000_000_000L;

    private final IDrawable[] faces;
    private final IDrawable pigSnout;
    private final IDrawable chickenBill;
    private final IDrawable chickenChin;
    private final IDrawable slot;
    private final ItemStack[] loot;
    private final IIngredientRenderer<ItemStack> itemRenderer;
    private long lastDrawNanos = System.nanoTime();
    private long phaseNanos;
    private int animalIndex;

    public ButcherBeeRecipe(IGuiHelper helper, IIngredientRenderer<ItemStack> itemRenderer) {
        this.itemRenderer = itemRenderer;
        faces = new IDrawable[] {
                face(helper, "cow/cow", 6, 6, 8, 8),
                face(helper, "pig/pig", 8, 8, 8, 8),
                face(helper, "chicken", 3, 3, 4, 6),
                face(helper, "sheep/sheep", 8, 8, 6, 6)
        };
        pigSnout = face(helper, "pig/pig", 17, 17, 4, 3);
        chickenBill = face(helper, "chicken", 16, 2, 4, 2);
        chickenChin = face(helper, "chicken", 16, 6, 2, 2);
        slot = helper.getSlotDrawable();
        loot = new ItemStack[] {new ItemStack(Items.BEEF), new ItemStack(Items.PORKCHOP),
                new ItemStack(Items.CHICKEN), new ItemStack(Items.MUTTON)};
    }

    private static IDrawable face(IGuiHelper helper, String texture, int u, int v, int width, int height) {
        return helper.drawableBuilder(new ResourceLocation("minecraft", "textures/entity/" + texture + ".png"),
                u, v, width, height).setTextureSize(64, 32).build();
    }

    @Override
    public ItemStack getInput() { return ItemStack.EMPTY; }

    @Override
    public boolean acceptsInput(ItemStack input) { return false; }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> examples = new ArrayList<>(loot.length);
        for (ItemStack stack : loot) examples.add(stack.copy());
        // Illustrative alternatives in one slot; these are not a complete or guaranteed loot list.
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(examples));
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        stacks.setOverrideDisplayFocus(null);
        stacks.init(1, false, new IIngredientRenderer<ItemStack>() {
            private int displayedAnimal = animalIndex;

            private void update() {
                if (displayedAnimal != animalIndex) {
                    stacks.set(1, loot[animalIndex].copy());
                    displayedAnimal = animalIndex;
                }
            }

            @Override
            public void render(Minecraft minecraft, int x, int y, @Nullable ItemStack ingredient) {
                update();
                // Read the real slot again after a transition so clicks and rendering use the same item.
                ItemStack displayed = stacks.getGuiIngredients().get(1).getDisplayedIngredient();
                drawFace(minecraft, displayedAnimal, x - 106, y);
                itemRenderer.render(minecraft, x, y, displayed);
            }

            @Override
            public List<String> getTooltip(Minecraft minecraft, ItemStack ingredient, ITooltipFlag flag) {
                return itemRenderer.getTooltip(minecraft, ingredient, flag);
            }

            @Override
            public FontRenderer getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
                return itemRenderer.getFontRenderer(minecraft, ingredient);
            }
        }, 124, 8, 18, 18, 1, 1);
        // The four alternatives remain indexed for R. A single displayed value avoids an ore-equivalence
        // tooltip treating the example drops as interchangeable, and keeps the final scope line red.
        stacks.set(1, loot[animalIndex].copy());
        stacks.addTooltipCallback((slotIndex, isInput, ingredient, tooltip) -> {
            if (slotIndex == 1) {
                tooltip.add(I18n.format("jei.capnsbeeaddon.butcher.output"));
                tooltip.add(TextFormatting.RED + I18n.format("jei.capnsbeeaddon.butcher.any_animal"));
            }
        });
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        advanceCycle(System.nanoTime(), GuiScreen.isShiftKeyDown());
        slot.draw(minecraft, 124, 8);
        String caption = I18n.format("jei.capnsbeeaddon.butcher.scope");
        minecraft.fontRenderer.drawString(caption, (recipeWidth - minecraft.fontRenderer.getStringWidth(caption)) / 2,
                32, 0x808080);
    }

    private void advanceCycle(long now, boolean paused) {
        if (!paused) phaseNanos = (phaseNanos + now - lastDrawNanos) % (loot.length * CYCLE_NANOS);
        lastDrawNanos = now;
        animalIndex = (int) (phaseNanos / CYCLE_NANOS);
    }

    private void drawFace(Minecraft minecraft, int animal, int x, int y) {
        GlStateManager.disableLighting();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y, 0);
            GlStateManager.scale(2, 2, 1);
            if (animal == 2) {
                faces[animal].draw(minecraft, 2, 1);
                chickenBill.draw(minecraft, 2, 3);
                chickenChin.draw(minecraft, 3, 5);
            } else if (animal == 3) {
                faces[animal].draw(minecraft, 1, 1);
            } else {
                faces[animal].draw(minecraft);
                if (animal == 1) pigSnout.draw(minecraft, 2, 4);
            }
        } finally {
            GlStateManager.popMatrix();
        }
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseX >= 18 && mouseX < 36 && mouseY >= 8 && mouseY < 26) {
            return Arrays.asList(I18n.format("jei.capnsbeeaddon.butcher.input"),
                    TextFormatting.RED + I18n.format("jei.capnsbeeaddon.butcher.any_animal"));
        }
        return Collections.emptyList();
    }
}
