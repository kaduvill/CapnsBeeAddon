package com.kaduvill.capnsbeeaddon.compat.jei.BensenBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

/** Text-only jazz overview; does not create music, entities or item ingredients. */
public final class BensenBeeRecipe implements BeeEffectRecipe {
    public static final String UID = CapnsBeeAddon.MODID + ".bensen_bee";

    private int captionX;
    private int captionY;
    private int captionWidth;
    private int captionHeight;

    @Override
    public ItemStack getInput() { return ItemStack.EMPTY; }

    @Override
    public boolean acceptsInput(ItemStack input) { return false; }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // Jazz has no consumed items or produced stacks to index in JEI.
    }

    @Override
    public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
        // The dedicated category has no ingredient slots to initialize or populate.
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        String caption = I18n.format("jei.capnsbeeaddon.bensen.description");
        captionWidth = minecraft.fontRenderer.getStringWidth(caption);
        captionHeight = minecraft.fontRenderer.FONT_HEIGHT;
        captionX = (recipeWidth - captionWidth) / 2;
        captionY = (recipeHeight - captionHeight) / 2;
        minecraft.fontRenderer.drawString(caption, captionX, captionY, 0x808080);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        // Use the actual rendered caption bounds, including localization and font changes.
        if (mouseX >= captionX && mouseX < captionX + captionWidth
                && mouseY >= captionY && mouseY < captionY + captionHeight) {
            return Collections.singletonList(I18n.format("jei.capnsbeeaddon.bensen.band"));
        }
        return Collections.emptyList();
    }
}
