package com.kaduvill.capnsbeeaddon.compat.jei.MinerBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.careerbees.CareerBeeEffectValues;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

/** Three mining overviews without item ingredients or outputs. */
public final class MinerBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".miner_bees";
    public static final int BASE_TICKS = 40;

    public enum Page {
        MINER("description"),
        FORTUNATE("fortune"),
        SILKY("silky");

        private final String captionKey;

        Page(String caption) {
            captionKey = "jei.capnsbeeaddon.miner." + caption;
        }
    }

    private final Page page;

    public MinerBeeRecipe(Page page) {
        this.page = page;
    }

    @Override
    public ItemStack getInput() { return ItemStack.EMPTY; }

    @Override
    public boolean acceptsInput(ItemStack input) { return false; }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // Placed ores are mined in the world; no items are indexed for navigation.
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        layout.getItemStacks().set(ingredients);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String caption;
        if (page == Page.FORTUNATE) {
            int fortune = CareerBeeEffectValues.getFortuneLevel();
            String levelKey = "enchantment.level." + fortune;
            String level = I18n.hasKey(levelKey) ? I18n.format(levelKey) : Integer.toString(fortune);
            caption = I18n.format(page.captionKey, level);
        } else {
            caption = I18n.format(page.captionKey);
        }
        minecraft.fontRenderer.drawString(caption,
                (recipeWidth - minecraft.fontRenderer.getStringWidth(caption)) / 2, 32, 0x808080);
    }
}
