package com.kaduvill.capnsbeeaddon.compat.jei.CrusherBee;

import com.kaduvill.capnsbeeaddon.compat.careerbees.CareerBeeEffectValues;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CrusherBeeRecipeWrapper implements IRecipeWrapper {

    private final CrusherBeeRecipes.Recipe recipe;

    public CrusherBeeRecipeWrapper(CrusherBeeRecipes.Recipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        // These are alternatives for one consumed item, not simultaneous inputs.
        ingredients.setInputLists(VanillaTypes.ITEM, Collections.singletonList(recipe.getInputs()));
        List<ItemStack> quantities = new ArrayList<>();
        int minOutput = CareerBeeEffectValues.getCrusherMinOutput();
        int maxOutput = CareerBeeEffectValues.getCrusherMaxOutput();
        for (int count = minOutput; count <= maxOutput; count++) {
            ItemStack output = recipe.getOutput();
            output.setCount(count);
            quantities.add(output);
        }
        // One output slot cycles the possible quantities.
        ingredients.setOutputLists(VanillaTypes.ITEM, Collections.singletonList(quantities));
    }
}
