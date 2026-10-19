package com.kaduvill.capnsbeeaddon.compat.jei.CrusherBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Collections;
import java.util.List;

public final class CrusherBeeRecipeCategory implements IRecipeCategory<CrusherBeeRecipeWrapper> {

    public static final String UID = CapnsBeeAddon.MODID + ".crusher_bee";
    private static final int WIDTH = 160;
    private static final int INPUT_X = 18;
    private static final int OUTPUT_X = 124;
    private static final int SLOT_Y = 8;
    private static final int ARROW_X = 68;
    // EffectOreCrushing uses a 400-tick threshold, scaled by EffectBase.getSpeed.
    private static final int BASE_PROCESSING_TICKS = 400;

    private final IDrawable background;
    private final IDrawable slot;
    private final IDrawableStatic arrowBackground;
    private final IDrawableAnimated arrow;
    private final IDrawable icon;
    private final ItemStack pedestal;

    public CrusherBeeRecipeCategory(IGuiHelper helper, ItemStack queen) {
        background = helper.createBlankDrawable(WIDTH, 50);
        slot = helper.getSlotDrawable();
        ResourceLocation texture = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");
        arrowBackground = helper.createDrawable(texture, 24, 132, 24, 17);
        arrow = helper.drawableBuilder(texture, 82, 128, 24, 17)
                .buildAnimated(BASE_PROCESSING_TICKS, IDrawableAnimated.StartDirection.LEFT, false);
        icon = queen.isEmpty() ? null : helper.createDrawableIngredient(queen);
        Item pedestalItem = Item.getByNameOrId("careerbees:flower_pedastal");
        pedestal = pedestalItem == null ? ItemStack.EMPTY : new ItemStack(pedestalItem);
    }

    @Override
    public String getUid() {
        return UID;
    }

    @Override
    public String getTitle() {
        return I18n.format("jei.capnsbeeaddon.crusher.title");
    }

    @Override
    public String getModName() {
        return CapnsBeeAddon.NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        slot.draw(minecraft, INPUT_X, SLOT_Y);
        slot.draw(minecraft, OUTPUT_X, SLOT_Y);
        arrowBackground.draw(minecraft, ARROW_X, SLOT_Y);
        arrow.draw(minecraft, ARROW_X, SLOT_Y);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (mouseX >= ARROW_X && mouseX < ARROW_X + arrow.getWidth()
                && mouseY >= SLOT_Y && mouseY < SLOT_Y + arrow.getHeight()) {
            return Collections.singletonList(
                    I18n.format("jei.capnsbeeaddon.crusher.interval", BASE_PROCESSING_TICKS));
        }
        return Collections.emptyList();
    }

    @Override
    public void setRecipe(IRecipeLayout layout, CrusherBeeRecipeWrapper wrapper, IIngredients ingredients) {
        layout.getItemStacks().init(0, true, INPUT_X, SLOT_Y);
        layout.getItemStacks().init(1, false, OUTPUT_X, SLOT_Y);
        IFocus<?> focus = layout.getFocus();
        if (focus != null && focus.getMode() == IFocus.Mode.OUTPUT) {
            // Keep quantity alternatives cycling when the output dust is the focus.
            layout.getItemStacks().setOverrideDisplayFocus(null);
        }
        layout.getItemStacks().set(ingredients);
        if (!pedestal.isEmpty()) {
            // A native JEI display gives this catalyst normal item tooltips/navigation.
            // Set it after the recipe ingredients: it is not in IIngredients' input/output
            // lists, and has no slot background drawn in drawExtras.
            layout.getItemStacks().init(2, true, INPUT_X, SLOT_Y + 20);
            layout.getItemStacks().set(2, pedestal.copy());
        }
        layout.getItemStacks().addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
            if (input || slotIndex != 1) return;
            tooltip.add(I18n.format("jei.capnsbeeaddon.crusher.yield",
                    CrusherBeeRecipes.MIN_OUTPUT, CrusherBeeRecipes.MAX_OUTPUT));
        });
    }
}
