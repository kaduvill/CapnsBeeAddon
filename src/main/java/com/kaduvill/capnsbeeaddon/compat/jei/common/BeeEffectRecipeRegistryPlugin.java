package com.kaduvill.capnsbeeaddon.compat.jei.common;

import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Immutable item index; only the focused item's candidates are tested on a JEI query. */
public final class BeeEffectRecipeRegistryPlugin implements IRecipeRegistryPlugin {
    private final String uid;
    private final List<String> uids;
    private final List<BeeEffectRecipe> recipes;
    private final Map<Item, List<IndexedRecipe>> byItem;
    private final List<ItemStack> catalysts;
    private final IStackHelper stackHelper;
    private final Function<ItemStack, ItemStack> outputSource;
    private final Function<ItemStack, ItemStack> inputNormalizer;

    public BeeEffectRecipeRegistryPlugin(String uid, List<? extends BeeEffectRecipe> recipes,
                                        IStackHelper stackHelper, List<ItemStack> catalysts,
                                        Function<ItemStack, ItemStack> outputSource) {
        this(uid, recipes, stackHelper, catalysts, outputSource, Function.identity());
    }

    public BeeEffectRecipeRegistryPlugin(String uid, List<? extends BeeEffectRecipe> recipes,
                                        IStackHelper stackHelper, List<ItemStack> catalysts,
                                        Function<ItemStack, ItemStack> outputSource,
                                        Function<ItemStack, ItemStack> inputNormalizer) {
        this.uid = uid;
        this.uids = Collections.singletonList(uid);
        this.recipes = Collections.unmodifiableList(new ArrayList<>(recipes));
        this.stackHelper = stackHelper;
        this.outputSource = outputSource;
        this.inputNormalizer = inputNormalizer;
        Map<Item, List<IndexedRecipe>> index = new IdentityHashMap<>();
        for (BeeEffectRecipe recipe : this.recipes) {
            ItemStack input = recipe.getInput();
            // Cache a separate lookup key; the recipe keeps its original input and effect state.
            index.computeIfAbsent(input.getItem(), item -> new ArrayList<>())
                    .add(new IndexedRecipe(recipe, inputNormalizer.apply(input).copy()));
        }
        index.replaceAll((item, entries) -> Collections.unmodifiableList(entries));
        this.byItem = Collections.unmodifiableMap(index);
        List<ItemStack> copies = new ArrayList<>();
        for (ItemStack catalyst : catalysts) if (!catalyst.isEmpty()) copies.add(catalyst.copy());
        this.catalysts = Collections.unmodifiableList(copies);
    }

    @Override
    public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
        return find(focus).isEmpty() ? Collections.emptyList() : uids;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> category, IFocus<V> focus) {
        return uid.equals(category.getUid()) ? (List<T>) (List<?>) find(focus) : Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> category) {
        return uid.equals(category.getUid()) ? (List<T>) (List<?>) recipes : Collections.emptyList();
    }

    private List<BeeEffectRecipe> find(IFocus<?> focus) {
        if (!(focus.getValue() instanceof ItemStack)) return Collections.emptyList();
        ItemStack stack = (ItemStack) focus.getValue();
        if (stack.isEmpty()) return Collections.emptyList();
        if (focus.getMode() == IFocus.Mode.INPUT) {
            for (ItemStack catalyst : catalysts) {
                if (stackHelper.isEquivalent(catalyst, stack)) return recipes;
            }
        } else if (focus.getMode() == IFocus.Mode.OUTPUT) {
            stack = outputSource.apply(stack);
        } else return Collections.emptyList();
        if (stack.isEmpty()) return Collections.emptyList();
        List<IndexedRecipe> candidates = byItem.get(stack.getItem());
        if (candidates == null) return Collections.emptyList();
        ItemStack lookupInput = inputNormalizer.apply(stack);
        BeeEffectRecipe fallback = null;
        for (IndexedRecipe candidate : candidates) {
            BeeEffectRecipe recipe = candidate.recipe;
            if (!recipe.acceptsInput(stack)) continue;
            if (stackHelper.isEquivalent(candidate.lookupInput, lookupInput)) return Collections.singletonList(recipe);
            if (fallback == null) fallback = recipe;
        }
        // Keep the same wrapper identity for hidden metadata/NBT variants too.
        return fallback == null ? Collections.emptyList() : Collections.singletonList(fallback);
    }

    private static final class IndexedRecipe {
        private final BeeEffectRecipe recipe;
        private final ItemStack lookupInput;

        private IndexedRecipe(BeeEffectRecipe recipe, ItemStack lookupInput) {
            this.recipe = recipe;
            this.lookupInput = lookupInput;
        }
    }
}
