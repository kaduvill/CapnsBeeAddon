package com.kaduvill.capnsbeeaddon.compat.jei.TaxcollectorBee;

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
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One overview page with indexed loot examples and illustrative mob faces. */
public final class TaxcollectorBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".taxcollector_bee";
    public static final int BASE_TICKS = 100;

    private final IDrawable[] faces;
    private final ItemStack[] loot;
    private final IIngredientRenderer<ItemStack> outputRenderer;

    public TaxcollectorBeeRecipe(IGuiHelper helper, IIngredientRenderer<ItemStack> itemRenderer) {
        faces = new IDrawable[] {
                face(helper, "zombie/zombie", 8, 8, 64),
                face(helper, "spider/spider", 40, 12, 32),
                face(helper, "skeleton/skeleton", 8, 8, 32),
                face(helper, "creeper/creeper", 8, 8, 32),
                face(helper, "skeleton/wither_skeleton", 8, 8, 32)
        };
        loot = new ItemStack[] {
                new ItemStack(Items.ROTTEN_FLESH), new ItemStack(Items.STRING), new ItemStack(Items.BONE),
                new ItemStack(Items.GUNPOWDER), new ItemStack(Items.SKULL, 1, 1)
        };
        outputRenderer = new IIngredientRenderer<ItemStack>() {
            @Override
            public void render(Minecraft minecraft, int x, int y, @Nullable ItemStack ingredient) {
                // The real slot's displayed item drives the face, including native cycling and filtering.
                if (ingredient != null && !ingredient.isEmpty()) {
                    for (int i = 0; i < loot.length; i++) {
                        if (ingredient.getItem() == loot[i].getItem()) {
                            drawFace(minecraft, faces[i], x - 106, y);
                            break;
                        }
                    }
                }
                itemRenderer.render(minecraft, x, y, ingredient);
            }

            @Override
            public List<String> getTooltip(Minecraft minecraft, ItemStack ingredient, ITooltipFlag flag) {
                List<String> tooltip = new ArrayList<>(itemRenderer.getTooltip(minecraft, ingredient, flag));
                tooltip.add(I18n.format("jei.capnsbeeaddon.taxcollector.output"));
                return tooltip;
            }

            @Override
            public FontRenderer getFontRenderer(Minecraft minecraft, ItemStack ingredient) {
                return itemRenderer.getFontRenderer(minecraft, ingredient);
            }
        };
    }

    private static IDrawable face(IGuiHelper helper, String texture, int u, int v, int textureHeight) {
        return helper.drawableBuilder(new ResourceLocation("minecraft", "textures/entity/" + texture + ".png"),
                u, v, 8, 8).setTextureSize(64, textureHeight).build();
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        List<ItemStack> examples = new ArrayList<>(loot.length);
        for (ItemStack stack : loot) examples.add(stack.copy());
        // Alternative examples in one output slot, not guaranteed products.
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(examples));
    }

    @Override
    public ItemStack getInput() { return ItemStack.EMPTY; }

    @Override
    public boolean acceptsInput(ItemStack input) { return false; }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String scope = I18n.format("jei.capnsbeeaddon.taxcollector.scope");
        minecraft.fontRenderer.drawString(scope, (recipeWidth - minecraft.fontRenderer.getStringWidth(scope)) / 2,
                32, 0x808080);
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        stacks.init(1, false, outputRenderer, 124, 8, 18, 18, 1, 1);
        // R should find the page without pinning its cycling examples to the focused item.
        stacks.setOverrideDisplayFocus(null);
        stacks.set(ingredients);
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
        }
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseY >= 8 && mouseY < 26) {
            if (mouseX >= 18 && mouseX < 36) {
                return Collections.singletonList(I18n.format("jei.capnsbeeaddon.taxcollector.input"));
            }
        }
        return Collections.emptyList();
    }
}
