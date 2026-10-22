package com.kaduvill.capnsbeeaddon.compat.jei.common;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Cached illustrations of Doctor and Plague effects; never creates or queries entities. */
public final class MedicalBeeRecipe implements BeeEffectRecipe {
    public static final String DOCTOR_UID = CapnsBeeAddon.MODID + ".doctor_bee";
    public static final String PLAGUE_UID = CapnsBeeAddon.MODID + ".plague_bee";
    public static final int BASE_TICKS = 10;
    private static final long CYCLE_NANOS = 1_000_000_000L;
    private static final String[] TARGET_KEYS = {
            "jei.capnsbeeaddon.medical.players", "jei.capnsbeeaddon.medical.hostile_mobs",
            "jei.capnsbeeaddon.medical.animals"
    };

    public enum Page { DOCTOR, PLAGUE, CURING }

    private final Page page;
    private final Face[] targets;
    @Nullable
    private final Face villager;
    @Nullable
    private final IDrawable effect;
    private long lastDrawNanos = System.nanoTime();
    private long phaseNanos;
    private int targetIndex;

    public MedicalBeeRecipe(IGuiHelper helper, Page page) {
        this.page = page;
        if (page == Page.CURING) {
            targets = new Face[] {villagerFace(helper, "zombie_villager/zombie_villager")};
            villager = villagerFace(helper, "villager/villager");
            effect = null;
        } else {
            targets = new Face[] {
                    new Face(face(helper, "steve", 64, 8, 8, 8, 8), null, 0, 0),
                    new Face(face(helper, "zombie/zombie", 64, 8, 8, 8, 8), null, 0, 0),
                    new Face(face(helper, "pig/pig", 32, 8, 8, 8, 8),
                            face(helper, "pig/pig", 32, 17, 17, 4, 3), 2, 4)
            };
            villager = null;
            // Only the vanilla appearance is used. Our tooltip supplies Doctor's actual 4-second duration.
            ItemStack icon = page == Page.DOCTOR
                    ? PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.REGENERATION)
                    : new ItemStack(Items.MILK_BUCKET);
            effect = helper.createDrawableIngredient(icon);
        }
    }

    private static IDrawable face(IGuiHelper helper, String texture, int textureHeight,
                                  int u, int v, int width, int height) {
        return helper.drawableBuilder(new ResourceLocation("minecraft", "textures/entity/" + texture + ".png"),
                u, v, width, height).setTextureSize(64, textureHeight).build();
    }

    private static Face villagerFace(IGuiHelper helper, String texture) {
        return new Face(face(helper, texture, 64, 8, 8, 8, 10),
                face(helper, texture, 64, 26, 2, 2, 4), 3, 7);
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // Faces, potion and milk are illustrations, not consumed or produced item stacks.
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
        targets[targetIndex].draw(minecraft, 19, page == Page.CURING ? 5 : 9);
        if (villager != null) villager.draw(minecraft, 125, 5);
        if (effect != null) effect.draw(minecraft, 125, 9);
        GlStateManager.color(1, 1, 1, 1);

        String captionKey = page == Page.DOCTOR ? "jei.capnsbeeaddon.doctor.description"
                : page == Page.PLAGUE ? "jei.capnsbeeaddon.plague.effect"
                : "jei.capnsbeeaddon.plague.curing.effect";
        drawCaption(minecraft, recipeWidth, 32, I18n.format(captionKey));
    }

    private void advanceCycle(long now, boolean paused) {
        if (!paused) phaseNanos = (phaseNanos + now - lastDrawNanos) % (targets.length * CYCLE_NANOS);
        lastDrawNanos = now;
        targetIndex = (int) (phaseNanos / CYCLE_NANOS);
    }

    private static void drawCaption(Minecraft minecraft, int width, int y, String text) {
        minecraft.fontRenderer.drawString(text, (width - minecraft.fontRenderer.getStringWidth(text)) / 2,
                y, 0x808080);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseY < 5 || mouseY >= 27) return Collections.emptyList();
        if (mouseX >= 18 && mouseX < 36) {
            if (page == Page.CURING) {
                return Collections.singletonList(I18n.format("jei.capnsbeeaddon.plague.curing.target"));
            }
            if (page == Page.PLAGUE && targetIndex == 1) {
                return Arrays.asList(I18n.format("jei.capnsbeeaddon.medical.target"),
                        I18n.format("jei.capnsbeeaddon.plague.undead"),
                        I18n.format("jei.capnsbeeaddon.plague.undead_immunity"),
                        TextFormatting.RED + I18n.format(TARGET_KEYS[targetIndex]));
            }
            return Arrays.asList(I18n.format("jei.capnsbeeaddon.medical.target"),
                    TextFormatting.RED + I18n.format(TARGET_KEYS[targetIndex]));
        }
        if (mouseX >= 124 && mouseX < 142) {
            if (page == Page.DOCTOR) {
                return Arrays.asList(I18n.format("jei.capnsbeeaddon.doctor.effect"),
                        I18n.format("jei.capnsbeeaddon.doctor.healing"));
            }
            if (page == Page.PLAGUE) {
                return Arrays.asList(I18n.format("jei.capnsbeeaddon.plague.cleansing"),
                        I18n.format("jei.capnsbeeaddon.plague.beneficial"),
                        I18n.format("jei.capnsbeeaddon.plague.healing"),
                        TextFormatting.GRAY + I18n.format("jei.capnsbeeaddon.plague.scope"));
            }
            return Arrays.asList(I18n.format("entity.Villager.name"),
                    I18n.format("jei.capnsbeeaddon.plague.curing.timer"));
        }
        return Collections.emptyList();
    }

    private static final class Face {
        private final IDrawable base;
        @Nullable
        private final IDrawable detail;
        private final int detailX;
        private final int detailY;

        private Face(IDrawable base, @Nullable IDrawable detail, int detailX, int detailY) {
            this.base = base;
            this.detail = detail;
            this.detailX = detailX;
            this.detailY = detailY;
        }

        private void draw(Minecraft minecraft, int x, int y) {
            GlStateManager.disableLighting();
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.pushMatrix();
            try {
                GlStateManager.translate(x, y, 0);
                GlStateManager.scale(2, 2, 1);
                base.draw(minecraft);
                if (detail != null) detail.draw(minecraft, detailX, detailY);
            } finally {
                GlStateManager.popMatrix();
            }
        }
    }
}
