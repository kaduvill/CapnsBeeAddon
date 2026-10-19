package com.kaduvill.capnsbeeaddon.compat.jei.HoneySmelter;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

public final class HoneySmelterRecipeWrapper implements IRecipeWrapper {

    private final HoneySmelterRecipes.Recipe recipe;

    public HoneySmelterRecipeWrapper(HoneySmelterRecipes.Recipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getInputs()));
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getOutput());
    }

    public boolean acceptsInput(ItemStack input) {
        return recipe.acceptsInput(input);
    }

    public ItemStack getOutput() {
        return recipe.getOutput();
    }

    public List<ItemStack> getInputsWithFocus(ItemStack focus) {
        List<ItemStack> inputs = recipe.getInputs();
        if (recipe.acceptsInput(focus)) {
            // Include an accepted focus even if JEI's creative-subtype expansion omitted it.
            ItemStack focusedInput = focus.copy();
            focusedInput.setCount(1);
            for (ItemStack input : inputs) {
                if (ItemStack.areItemStacksEqual(input, focusedInput)) return inputs;
            }
            inputs.add(focusedInput);
        }
        return inputs;
    }
}
