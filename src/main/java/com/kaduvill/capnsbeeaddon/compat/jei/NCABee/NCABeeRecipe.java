package com.kaduvill.capnsbeeaddon.compat.jei.NCABee;

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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Items;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/** Creeper spawning overview using an egg only for navigation; no entities or worlds are created. */
public final class NCABeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".nca_bee";
    public static final int BASE_TICKS = 2;

    private final ItemStack output;
    private final IDrawable face;

    public NCABeeRecipe(IGuiHelper helper) {
        output = new ItemStack(Items.SPAWN_EGG);
        ItemMonsterPlacer.applyEntityIdToItemStack(output, new ResourceLocation("minecraft", "creeper"));
        face = helper.drawableBuilder(new ResourceLocation("minecraft", "textures/entity/creeper/creeper.png"),
                8, 8, 8, 8).setTextureSize(64, 32).build();
    }

    @Override
    public ItemStack getInput() { return ItemStack.EMPTY; }

    @Override
    public boolean acceptsInput(ItemStack input) { return false; }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // A plain Creeper egg provides R navigation; the bee spawns a mob, not an egg item.
        ingredients.setOutput(VanillaTypes.ITEM, output.copy());
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        stacks.init(1, false, new IIngredientRenderer<ItemStack>() {
            @Override
            public void render(Minecraft minecraft, int x, int y, @Nullable ItemStack ingredient) {
                if (ingredient == null || ingredient.isEmpty()) return;
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
            public List<String> getTooltip(Minecraft minecraft, ItemStack ingredient, ITooltipFlag flag) {
                return Arrays.asList(I18n.format("jei.capnsbeeaddon.nca.wither"),
                        I18n.format("jei.capnsbeeaddon.nca.explosion"),
                        I18n.format("jei.capnsbeeaddon.nca.loot"));
            }

            @Override
            public FontRenderer getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
                return minecraft.fontRenderer;
            }
        }, 124, 8, 18, 18, 1, 1);
        stacks.setOverrideDisplayFocus(null);
        stacks.set(ingredients);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String caption = I18n.format("jei.capnsbeeaddon.nca.description");
        minecraft.fontRenderer.drawString(caption,
                (recipeWidth - minecraft.fontRenderer.getStringWidth(caption)) / 2, 32, 0x808080);
    }
}
