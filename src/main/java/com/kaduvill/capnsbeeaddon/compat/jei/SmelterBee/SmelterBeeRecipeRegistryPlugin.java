package com.kaduvill.capnsbeeaddon.compat.jei.SmelterBee;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Stable wrappers and native input resolution, including hidden wildcard and NBT variants. */
public final class SmelterBeeRecipeRegistryPlugin implements IRecipeRegistryPlugin {
    private static final List<String> UIDS = Collections.singletonList(SmelterBeeRecipes.UID);
    private final SmelterBeeRecipes recipes;
    private final IStackHelper stackHelper;
    private final List<ItemStack> catalysts;

    public SmelterBeeRecipeRegistryPlugin(SmelterBeeRecipes recipes, IStackHelper stackHelper, List<ItemStack> catalysts) {
        this.recipes = recipes;
        this.stackHelper = stackHelper;
        List<ItemStack> copies = new ArrayList<>();
        for (ItemStack catalyst : catalysts) if (!catalyst.isEmpty()) copies.add(catalyst.copy());
        this.catalysts = Collections.unmodifiableList(copies);
    }

    @Override
    public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
        return find(focus).isEmpty() ? Collections.emptyList() : UIDS;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> category, IFocus<V> focus) {
        return SmelterBeeRecipes.UID.equals(category.getUid()) ? (List<T>) (List<?>) find(focus) : Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> category) {
        return SmelterBeeRecipes.UID.equals(category.getUid())
                ? (List<T>) (List<?>) recipes.getRecipes() : Collections.emptyList();
    }

    private List<SmelterBeeRecipes.Recipe> find(IFocus<?> focus) {
        if (!(focus.getValue() instanceof ItemStack)) return Collections.emptyList();
        ItemStack stack = (ItemStack) focus.getValue();
        if (stack.isEmpty()) return Collections.emptyList();
        if (focus.getMode() == IFocus.Mode.OUTPUT) return recipes.findOutputs(stack, stackHelper);
        if (focus.getMode() != IFocus.Mode.INPUT) return Collections.emptyList();
        for (ItemStack catalyst : catalysts) {
            if (stackHelper.isEquivalent(catalyst, stack)) return recipes.getRecipes();
        }
        SmelterBeeRecipes.Recipe recipe = recipes.findInput(stack);
        return recipe == null ? Collections.emptyList() : Collections.singletonList(recipe);
    }
}
