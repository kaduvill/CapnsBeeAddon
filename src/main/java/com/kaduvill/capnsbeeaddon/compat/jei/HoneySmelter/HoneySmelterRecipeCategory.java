package com.kaduvill.capnsbeeaddon.compat.jei.HoneySmelter;

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

public final class HoneySmelterRecipeCategory implements IRecipeCategory<HoneySmelterRecipeWrapper> {

    public static final String UID = CapnsBeeAddon.MODID + ".honey_smelter_bee";
    private static final int WIDTH = 160;
    private static final int INPUT_X = 18;
    private static final int OUTPUT_X = 124;
    private static final int SLOT_Y = 8;
    private static final int ARROW_X = 68;
    // EffectHoneyCombIngotConvert's base threshold before speed modifiers.
    private static final int BASE_PROCESSING_TICKS = 200;

    private final IDrawable background;
    private final IDrawable slot;
    private final IDrawableStatic arrowBackground;
    private final IDrawableAnimated arrow;
    private final IDrawable icon;
    private final ItemStack pedestal;

    public HoneySmelterRecipeCategory(IGuiHelper helper, ItemStack queen) {
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
        return I18n.format("jei.capnsbeeaddon.honey_smelter.title");
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
                    I18n.format("jei.capnsbeeaddon.base_rate", BASE_PROCESSING_TICKS));
        }
        return Collections.emptyList();
    }

    @Override
    public void setRecipe(IRecipeLayout layout, HoneySmelterRecipeWrapper wrapper, IIngredients ingredients) {
        layout.getItemStacks().init(0, true, INPUT_X, SLOT_Y);
        layout.getItemStacks().init(1, false, OUTPUT_X, SLOT_Y);
        layout.getItemStacks().set(ingredients);
        IFocus<?> focus = layout.getFocus();
        if (focus != null && focus.getMode() == IFocus.Mode.INPUT && focus.getValue() instanceof ItemStack) {
            ItemStack input = (ItemStack) focus.getValue();
            if (wrapper.acceptsInput(input)) {
                layout.getItemStacks().set(0, wrapper.getInputsWithFocus(input));
            }
        }
        if (!pedestal.isEmpty()) {
            // Display-only catalyst: native tooltip/navigation, without a slot background.
            layout.getItemStacks().init(2, true, INPUT_X, SLOT_Y + 20);
            layout.getItemStacks().set(2, pedestal.copy());
        }
    }
}
