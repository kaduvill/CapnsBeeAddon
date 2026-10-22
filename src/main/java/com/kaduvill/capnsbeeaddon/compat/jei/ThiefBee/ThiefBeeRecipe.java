package com.kaduvill.capnsbeeaddon.compat.jei.ThiefBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** One overview with decorative examples that never enter JEI's ingredient index. */
public final class ThiefBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".thief_bee";
    public static final int BASE_TICKS = 10;
    private static final long CYCLE_NANOS = 1_000_000_000L;

    private final IDrawable player;
    private final IDrawable[] examples;
    private long lastDrawNanos = System.nanoTime();
    private long phaseNanos;
    private int exampleIndex;

    public ThiefBeeRecipe(IGuiHelper helper) {
        player = helper.drawableBuilder(new ResourceLocation("minecraft", "textures/entity/steve.png"),
                8, 8, 8, 8).setTextureSize(64, 64).build();
        examples = new IDrawable[] {
                helper.createDrawableIngredient(new ItemStack(Items.APPLE)),
                helper.createDrawableIngredient(new ItemStack(Items.DIAMOND)),
                helper.createDrawableIngredient(new ItemStack(Items.DIAMOND_SWORD)),
                helper.createDrawableIngredient(new ItemStack(Items.IRON_CHESTPLATE))
        };
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // The player and item examples are illustrations, not recipe inputs or outputs.
    }

    @Override
    public ItemStack getInput() { return ItemStack.EMPTY; }

    @Override
    public boolean acceptsInput(ItemStack input) { return false; }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        layout.getItemStacks().set(ingredients);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        advanceCycle(System.nanoTime(), GuiScreen.isShiftKeyDown());
        GlStateManager.disableLighting();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(19, 9, 0);
            GlStateManager.scale(2, 2, 1);
            player.draw(minecraft);
        } finally {
            GlStateManager.popMatrix();
        }
        examples[exampleIndex].draw(minecraft, 125, 9);
        GlStateManager.color(1, 1, 1, 1);

        String caption = I18n.format("jei.capnsbeeaddon.thief.description");
        minecraft.fontRenderer.drawString(caption,
                (recipeWidth - minecraft.fontRenderer.getStringWidth(caption)) / 2, 32, 0x808080);
    }

    private void advanceCycle(long now, boolean paused) {
        if (!paused) phaseNanos = (phaseNanos + now - lastDrawNanos) % (examples.length * CYCLE_NANOS);
        lastDrawNanos = now;
        exampleIndex = (int) (phaseNanos / CYCLE_NANOS);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseY < 8 || mouseY >= 26) return Collections.emptyList();
        if (mouseX >= 18 && mouseX < 36) {
            return Arrays.asList(I18n.format("jei.capnsbeeaddon.thief.scope"),
                    TextFormatting.GRAY + I18n.format("jei.capnsbeeaddon.thief.protection"));
        }
        if (mouseX >= 124 && mouseX < 142) {
            return Collections.singletonList(I18n.format("jei.capnsbeeaddon.thief.item"));
        }
        return Collections.emptyList();
    }
}
