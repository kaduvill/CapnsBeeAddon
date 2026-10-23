package com.kaduvill.capnsbeeaddon.compat.jei.DevilBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

/** Fire is an illustration of the world effect, not an indexed item output. */
public final class DevilBeeRecipe extends Gui implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".devil_bee";
    public static final int BASE_TICKS = 4;

    @Override
    public ItemStack getInput() { return ItemStack.EMPTY; }

    @Override
    public boolean acceptsInput(ItemStack input) { return false; }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // No items are consumed or produced.
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        layout.getItemStacks().set(ingredients);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        // Resolve the live atlas sprite each draw so resource reloads are respected.
        // Minecraft advances its animation; no separate texture or animation timer is needed.
        TextureAtlasSprite fire = minecraft.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/fire_layer_0");
        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();
        GlStateManager.color(1, 1, 1, 1);
        minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        drawTexturedModalRect(125, 9, fire, 16, 16);

        String caption = I18n.format("jei.capnsbeeaddon.devil.description");
        minecraft.fontRenderer.drawString(caption,
                (recipeWidth - minecraft.fontRenderer.getStringWidth(caption)) / 2, 32, 0x808080);
    }
}
