package com.kaduvill.capnsbeeaddon.compat.jei.PoliceBee;

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

/** One cached overview of hostile mobs, example drops and reports; never evaluates entities or loot tables. */
public final class PoliceBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".police_bee";
    public static final int BASE_TICKS = 10;
    private static final long CYCLE_NANOS = 1_000_000_000L;
    private static final int TARGET_X = 18;
    private static final int LOOT_X = 115;
    private static final int REPORT_X = 133;

    private final IDrawable[] faces;
    private final IDrawable slot;
    private final ItemStack[] loot;
    private final ItemStack report;
    private final IIngredientRenderer<ItemStack> itemRenderer;
    private long lastDrawNanos = System.nanoTime();
    private long phaseNanos;
    private int mobIndex;

    public PoliceBeeRecipe(IGuiHelper helper, IIngredientRenderer<ItemStack> itemRenderer, ItemStack report) {
        this.itemRenderer = itemRenderer;
        this.report = report.copy();
        faces = new IDrawable[] {
                face(helper, "zombie/zombie", 8, 8, 64),
                face(helper, "skeleton/skeleton", 8, 8, 32),
                face(helper, "spider/spider", 40, 12, 32),
                face(helper, "creeper/creeper", 8, 8, 32)
        };
        slot = helper.getSlotDrawable();
        loot = new ItemStack[] {new ItemStack(Items.ROTTEN_FLESH), new ItemStack(Items.BONE),
                new ItemStack(Items.STRING), new ItemStack(Items.GUNPOWDER)};
    }

    private static IDrawable face(IGuiHelper helper, String texture, int u, int v, int textureHeight) {
        return helper.drawableBuilder(new ResourceLocation("minecraft", "textures/entity/" + texture + ".png"),
                u, v, 8, 8).setTextureSize(64, textureHeight).build();
    }

    @Override
    public ItemStack getInput() { return ItemStack.EMPTY; }

    @Override
    public boolean acceptsInput(ItemStack input) { return false; }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> examples = new ArrayList<>(loot.length);
        for (ItemStack stack : loot) examples.add(stack.copy());
        // Loot examples are alternatives, not guaranteed quantities or an exhaustive drop list.
        // The report is a separate real output; no incident NBT is invented for the preview.
        ingredients.setOutputLists(VanillaTypes.ITEM, Arrays.asList(examples, Collections.singletonList(report.copy())));
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        stacks.setOverrideDisplayFocus(null);
        stacks.init(1, false, new IIngredientRenderer<ItemStack>() {
            private int displayedMob = mobIndex;

            private void update() {
                if (displayedMob != mobIndex) {
                    stacks.set(1, loot[mobIndex].copy());
                    displayedMob = mobIndex;
                }
            }

            @Override
            public void render(Minecraft minecraft, int x, int y, @Nullable ItemStack ingredient) {
                update();
                // Drawing and clicks read the same real slot after a cycle transition.
                ItemStack displayed = stacks.getGuiIngredients().get(1).getDisplayedIngredient();
                drawFace(minecraft, faces[displayedMob], x - (LOOT_X - TARGET_X), y);
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
        }, LOOT_X, 8, 18, 18, 1, 1);
        stacks.init(2, false, REPORT_X, 8);
        // Keep R navigation from all four drops without pinning the cycling slot to the focus.
        stacks.set(1, loot[mobIndex].copy());
        stacks.set(2, report.copy());
        stacks.addTooltipCallback((slotIndex, isInput, ingredient, tooltip) -> {
            if (slotIndex == 1) {
                tooltip.add(I18n.format("jei.capnsbeeaddon.police.output"));
                tooltip.add(TextFormatting.GRAY + I18n.format("jei.capnsbeeaddon.police.collected"));
                tooltip.add(TextFormatting.RED + I18n.format("jei.capnsbeeaddon.police.any_mob"));
            } else if (slotIndex == 2) {
                tooltip.add(I18n.format("jei.capnsbeeaddon.police.report"));
                tooltip.add(TextFormatting.GRAY + I18n.format("jei.capnsbeeaddon.police.report_details"));
            }
        });
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        advanceCycle(System.nanoTime(), GuiScreen.isShiftKeyDown());
        slot.draw(minecraft, LOOT_X, 8);
        slot.draw(minecraft, REPORT_X, 8);
        String caption = I18n.format("jei.capnsbeeaddon.police.description");
        minecraft.fontRenderer.drawString(caption, (recipeWidth - minecraft.fontRenderer.getStringWidth(caption)) / 2,
                32, 0x808080);
    }

    private void advanceCycle(long now, boolean paused) {
        if (!paused) phaseNanos = (phaseNanos + now - lastDrawNanos) % (loot.length * CYCLE_NANOS);
        lastDrawNanos = now;
        mobIndex = (int) (phaseNanos / CYCLE_NANOS);
    }

    private static void drawFace(Minecraft minecraft, IDrawable face, int x, int y) {
        GlStateManager.disableLighting();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.translate(x, y, 0);
            GlStateManager.scale(2, 2, 1);
            face.draw(minecraft);
        } finally {
            GlStateManager.popMatrix();
            GlStateManager.color(1, 1, 1, 1);
        }
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseX >= TARGET_X && mouseX < TARGET_X + 18 && mouseY >= 8 && mouseY < 26) {
            return Arrays.asList(I18n.format("jei.capnsbeeaddon.police.target"),
                    TextFormatting.GRAY + I18n.format("jei.capnsbeeaddon.police.neutral"),
                    TextFormatting.RED + I18n.format("jei.capnsbeeaddon.police.any_mob"));
        }
        return Collections.emptyList();
    }
}
