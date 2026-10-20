package com.kaduvill.capnsbeeaddon.compat.jei.HusbandryBee;

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
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Illustrative vanilla pairs; the real effect puts one eligible adult into love mode. */
public final class HusbandryBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".husbandry_bee";
    public static final int BASE_TICKS = 20;
    private static final long CYCLE_NANOS = 1_000_000_000L;

    private final IDrawable[] faces;
    private final IDrawable chickenBill;
    private final IDrawable chickenChin;
    private final IDrawable pigSnout;
    private long lastDrawNanos = System.nanoTime();
    private long phaseNanos;

    public HusbandryBeeRecipe(IGuiHelper helper) {
        faces = new IDrawable[] {
                face(helper, "sheep/sheep", 8, 8, 6, 6),
                face(helper, "chicken", 3, 3, 4, 6),
                face(helper, "cow/cow", 6, 6, 8, 8),
                face(helper, "pig/pig", 8, 8, 8, 8)
        };
        chickenBill = face(helper, "chicken", 16, 2, 4, 2);
        chickenChin = face(helper, "chicken", 16, 6, 2, 2);
        pigSnout = face(helper, "pig/pig", 17, 17, 4, 3);
    }

    private static IDrawable face(IGuiHelper helper, String texture, int u, int v, int width, int height) {
        return helper.drawableBuilder(new ResourceLocation("minecraft", "textures/entity/" + texture + ".png"),
                u, v, width, height).setTextureSize(64, 32).build();
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // Animals are illustrations, not item inputs or outputs (including spawn eggs).
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
        long now = System.nanoTime();
        if (!GuiScreen.isShiftKeyDown()) {
            phaseNanos = (phaseNanos + now - lastDrawNanos) % (faces.length * CYCLE_NANOS);
        }
        lastDrawNanos = now;
        int animal = (int) (phaseNanos / CYCLE_NANOS);
        // One index drives both parents and the baby, so the examples cannot drift apart.
        drawFace(minecraft, animal, 10, 9, 2);
        drawFace(minecraft, animal, 30, 9, 2);
        drawFace(minecraft, animal, 129, 13, 1);
        String baby = I18n.format("jei.capnsbeeaddon.husbandry.baby");
        minecraft.fontRenderer.drawString(baby, 133 - minecraft.fontRenderer.getStringWidth(baby) / 2, 31, 0x808080);
        GlStateManager.color(1, 1, 1, 1);
    }

    private void drawFace(Minecraft minecraft, int animal, int x, int y, int scale) {
        GlStateManager.disableLighting();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y, 0);
            GlStateManager.scale(scale, scale, 1);
            if (animal == 0) {
                faces[animal].draw(minecraft, 1, 1);
            } else if (animal == 1) {
                faces[animal].draw(minecraft, 2, 1);
                chickenBill.draw(minecraft, 2, 3);
                chickenChin.draw(minecraft, 3, 5);
            } else {
                faces[animal].draw(minecraft);
                if (animal == 3) pigSnout.draw(minecraft, 2, 4);
            }
        } finally {
            GlStateManager.popMatrix();
        }
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseY >= 8 && mouseY < 26) {
            String headingKey;
            if (mouseX >= 10 && mouseX < 46) {
                headingKey = "jei.capnsbeeaddon.husbandry.input";
            } else if (mouseX >= 124 && mouseX < 142) {
                headingKey = "jei.capnsbeeaddon.husbandry.baby";
            } else {
                return Collections.emptyList();
            }
            return Arrays.asList(I18n.format(headingKey),
                    TextFormatting.RED + I18n.format("jei.capnsbeeaddon.husbandry.any_animal"));
        }
        return Collections.emptyList();
    }
}
