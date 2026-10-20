package com.kaduvill.capnsbeeaddon.compat.jei.ChefBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipe;
import com.rwtema.careerbees.effects.EffectHoneyGlaze;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ChefBeeRecipes {
    public static final String UID = CapnsBeeAddon.MODID + ".chef_bee";
    private static final String FOOD_EFFECTS = "BEE_FOOD_EFFECTS";
    private static final String GLAZING = "glazing";
    // EffectHoneyGlaze.getMax caps the strength at 20, adding one point each application.
    private static final int MAX_APPLICATIONS = 20;

    private ChefBeeRecipes() {}

    public static List<Recipe> read(Iterable<ItemStack> ingredients) {
        Map<NBTTagCompound, Recipe> recipes = new LinkedHashMap<>();
        for (ItemStack stack : ingredients) {
            if (!(stack.getItem() instanceof ItemFood)) continue;
            ItemStack input = one(stack);
            Completion completion = finish(input);
            if (completion != null) recipes.putIfAbsent(input.serializeNBT(), new Recipe(input, completion));
        }
        return Collections.unmodifiableList(new ArrayList<>(recipes.values()));
    }

    /** The installed helper is pure: getMax ignores the genome; no world/housing is read. */
    public static ItemStack step(ItemStack input) {
        if (input.isEmpty() || !(input.getItem() instanceof ItemFood)) return ItemStack.EMPTY;
        try {
            ItemStack copy = one(input);
            if (!EffectHoneyGlaze.INSTANCE.acceptItemStack(copy)) return ItemStack.EMPTY;
            ItemStack output = EffectHoneyGlaze.INSTANCE.modifyStack(null, copy, null);
            return output == null || output.isEmpty() ? ItemStack.EMPTY : one(output);
        } catch (RuntimeException | LinkageError failure) {
            // A broken custom food must not prevent the other recipes from loading or opening.
            return ItemStack.EMPTY;
        }
    }

    /** Only a verified, fully glazed product is a recipe output. Preserve all other NBT. */
    public static ItemStack sourceOf(ItemStack output) {
        if (!isFullyGlazed(output)) return ItemStack.EMPTY;
        ItemStack input = withoutGlazing(output);
        Completion completion = finish(input);
        return completion != null && ItemStack.areItemStacksEqual(completion.output, one(output)) ? input : ItemStack.EMPTY;
    }

    /** Lookup-only copy: remove glazing and its empty containers, retaining other food data. */
    public static ItemStack withoutGlazing(ItemStack stack) {
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack input = one(stack);
        NBTTagCompound tag = input.getTagCompound();
        if (tag != null) {
            if (tag.hasKey(FOOD_EFFECTS, 10)) {
                NBTTagCompound effects = tag.getCompoundTag(FOOD_EFFECTS);
                effects.removeTag(GLAZING);
                if (effects.hasNoTags()) tag.removeTag(FOOD_EFFECTS);
            }
            if (tag.hasNoTags()) input.setTagCompound(null);
        }
        return input;
    }

    private static Completion finish(ItemStack input) {
        ItemStack current = input;
        for (int applications = 1; applications <= MAX_APPLICATIONS; applications++) {
            current = step(current);
            if (current.isEmpty()) return null;
            if (isFullyGlazed(current)) return new Completion(current, applications);
        }
        // Fail closed for malformed data or a custom food that never reaches a stable limit.
        return null;
    }

    private static boolean isFullyGlazed(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemFood) || !stack.hasTagCompound()) return false;
        NBTTagCompound data = stack.getTagCompound().getCompoundTag(FOOD_EFFECTS).getCompoundTag(GLAZING);
        float current = data.getFloat("current");
        float max = data.getFloat("max");
        return Float.isFinite(current) && Float.isFinite(max) && max > 0 && current == max;
    }

    private static ItemStack one(ItemStack stack) {
        ItemStack copy = stack.copy();
        copy.setCount(1);
        return copy;
    }

    private static final class Completion {
        private final ItemStack output;
        private final int applications;

        private Completion(ItemStack output, int applications) {
            this.output = output;
            this.applications = applications;
        }
    }

    /** Per-layout text avoids mutating a wrapper shared by multiple JEI pages/lookups. */
    private static final class ApplicationsLabel implements IDrawable {
        private final int applications;

        private ApplicationsLabel(int applications) { this.applications = applications; }

        @Override
        public int getWidth() { return 18; }

        @Override
        public int getHeight() { return 31; }

        @Override
        public void draw(Minecraft minecraft, int xOffset, int yOffset) {
            String text = I18n.format("jei.capnsbeeaddon.chef.applications", applications);
            // Align with the product's right edge so the label stays inside the recipe.
            minecraft.fontRenderer.drawString(text, xOffset + 18 - minecraft.fontRenderer.getStringWidth(text),
                    yOffset + 22, 0x808080);
        }
    }

    public static final class Recipe implements BeeEffectRecipe {
        private final ItemStack input;
        private final Completion completion;

        private Recipe(ItemStack input, Completion completion) {
            this.input = input.copy();
            this.completion = completion;
        }

        @Override
        public ItemStack getInput() { return input.copy(); }

        @Override
        public boolean acceptsInput(ItemStack stack) {
            return stack.getItem() == input.getItem() && finish(stack) != null;
        }

        @Override
        public void getIngredients(IIngredients ingredients) {
            ingredients.setInput(VanillaTypes.ITEM, input.copy());
            ingredients.setOutput(VanillaTypes.ITEM, completion.output.copy());
        }

        @Override
        public void setRecipe(IRecipeLayout layout, IIngredients ingredients) {
            ItemStack displayedInput = input;
            Completion displayed = completion;
            IFocus<?> focus = layout.getFocus();
            if (focus != null && focus.getValue() instanceof ItemStack) {
                ItemStack stack = (ItemStack) focus.getValue();
                ItemStack source = focus.getMode() == IFocus.Mode.INPUT ? stack : sourceOf(stack);
                if (source.getItem() == input.getItem() && !ItemStack.areItemStacksEqual(one(source), input)) {
                    Completion focused = finish(source);
                    if (focused != null) {
                        displayedInput = one(source);
                        displayed = focused;
                    }
                }
            }
            // Bind the actual input ourselves; an output focus must never pin a partial glaze.
            layout.getItemStacks().setOverrideDisplayFocus(null);
            layout.getItemStacks().set(ingredients);
            layout.getItemStacks().set(0, displayedInput.copy());
            layout.getItemStacks().set(1, displayed.output.copy());
            layout.getItemStacks().setBackground(1, new ApplicationsLabel(displayed.applications));
        }
    }
}
