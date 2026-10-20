package com.kaduvill.capnsbeeaddon.compat.jei.common;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/** Broad bee navigation to one cached example, without enumerating species or genomes. */
public final class BeeOverviewRecipeRegistryPlugin implements IRecipeRegistryPlugin {
    private final String uid;
    private final List<String> uids;
    private final List<BeeEffectRecipe> recipes;
    private final IStackHelper stackHelper;
    private final List<ItemStack> catalysts;
    private final Predicate<ItemStack> inputNavigation;
    private final Predicate<ItemStack> outputNavigation;

    public BeeOverviewRecipeRegistryPlugin(String uid, BeeEffectRecipe recipe, IStackHelper stackHelper,
                                          List<ItemStack> catalysts, Predicate<ItemStack> inputNavigation,
                                          Predicate<ItemStack> outputNavigation) {
        this.uid = Objects.requireNonNull(uid, "Category UID");
        this.uids = Collections.singletonList(uid);
        this.recipes = Collections.singletonList(Objects.requireNonNull(recipe, "Overview recipe"));
        this.stackHelper = Objects.requireNonNull(stackHelper, "Stack helper");
        this.inputNavigation = Objects.requireNonNull(inputNavigation, "Input navigation");
        this.outputNavigation = Objects.requireNonNull(outputNavigation, "Output navigation");
        List<ItemStack> copies = new ArrayList<>();
        for (ItemStack catalyst : catalysts) if (!catalyst.isEmpty()) copies.add(catalyst.copy());
        this.catalysts = Collections.unmodifiableList(copies);
    }

    @Override
    public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
        return matches(focus) ? uids : Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> category, IFocus<V> focus) {
        return category != null && uid.equals(category.getUid()) && matches(focus)
                ? (List<T>) (List<?>) recipes : Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> category) {
        return category != null && uid.equals(category.getUid())
                ? (List<T>) (List<?>) recipes : Collections.emptyList();
    }

    private boolean matches(IFocus<?> focus) {
        if (focus == null || !(focus.getValue() instanceof ItemStack)) return false;
        ItemStack stack = (ItemStack) focus.getValue();
        if (stack.isEmpty()) return false;
        try {
            if (focus.getMode() == IFocus.Mode.INPUT) {
                for (ItemStack catalyst : catalysts) {
                    if (stackHelper.isEquivalent(catalyst, stack)) return true;
                }
                return inputNavigation.test(stack);
            }
            return focus.getMode() == IFocus.Mode.OUTPUT && outputNavigation.test(stack);
        } catch (RuntimeException | LinkageError failure) {
            return false;
        }
    }

    public static boolean isDroneOrPrincess(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        try {
            // getType checks item identity; it does not parse or copy genetic NBT.
            EnumBeeType type = BeeManager.beeRoot.getType(stack);
            return type == EnumBeeType.DRONE || type == EnumBeeType.PRINCESS;
        } catch (RuntimeException | LinkageError failure) {
            return false;
        }
    }
}
