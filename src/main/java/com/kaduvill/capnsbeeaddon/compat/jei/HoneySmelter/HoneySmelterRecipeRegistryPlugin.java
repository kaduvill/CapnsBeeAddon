package com.kaduvill.capnsbeeaddon.compat.jei.HoneySmelter;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Exact ore matching on JEI queries, including NBT and non-creative wildcard variants. */
public final class HoneySmelterRecipeRegistryPlugin implements IRecipeRegistryPlugin {

    private static final List<String> CATEGORY_UIDS = Collections.singletonList(HoneySmelterRecipeCategory.UID);

    private final HoneySmelterRecipeWrapper recipe;
    private final IStackHelper stackHelper;
    private final ItemStack output;
    private final List<ItemStack> catalysts;
    private final List<HoneySmelterRecipeWrapper> recipes;

    public HoneySmelterRecipeRegistryPlugin(HoneySmelterRecipeWrapper recipe, IStackHelper stackHelper,
                                          List<ItemStack> catalysts) {
        this.recipe = recipe;
        this.stackHelper = stackHelper;
        this.output = recipe.getOutput();
        List<ItemStack> copies = new ArrayList<>(catalysts.size());
        for (ItemStack catalyst : catalysts) {
            if (!catalyst.isEmpty()) copies.add(catalyst.copy());
        }
        this.catalysts = Collections.unmodifiableList(copies);
        // HEI text-search intersections require the same wrapper identity on every lookup path.
        this.recipes = Collections.singletonList(recipe);
    }

    @Override
    public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
        return matchesFocus(focus) ? CATEGORY_UIDS : Collections.emptyList();
    }

    @Override
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> category, IFocus<V> focus) {
        if (!HoneySmelterRecipeCategory.UID.equals(category.getUid())) return Collections.emptyList();
        return matchesFocus(focus) ? getRecipeWrappers(category) : Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> category) {
        return HoneySmelterRecipeCategory.UID.equals(category.getUid())
                ? (List<T>) (List<?>) recipes : Collections.emptyList();
    }

    private boolean matchesFocus(IFocus<?> focus) {
        if (!(focus.getValue() instanceof ItemStack)) return false;
        ItemStack stack = (ItemStack) focus.getValue();
        if (stack.isEmpty()) return false;
        if (focus.getMode() == IFocus.Mode.OUTPUT) return stackHelper.isEquivalent(output, stack);
        if (focus.getMode() != IFocus.Mode.INPUT) return false;
        if (recipe.acceptsInput(stack)) return true;
        for (ItemStack catalyst : catalysts) {
            if (stackHelper.isEquivalent(catalyst, stack)) return true;
        }
        return false;
    }
}
