package com.kaduvill.capnsbeeaddon.compat.jei.AssassinBee;

import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipeRegistryPlugin;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IStackHelper;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;

/** Recipe lookups for any princess or drone link to the cached overview. */
public final class AssassinBeeRecipeRegistryPlugin implements IRecipeRegistryPlugin {
    private static final List<String> CATEGORY_UIDS = Collections.singletonList(AssassinBeeRecipe.UID);

    private final BeeEffectRecipeRegistryPlugin delegate;

    public AssassinBeeRecipeRegistryPlugin(AssassinBeeRecipe recipe, IStackHelper stackHelper,
                                          List<ItemStack> catalysts) {
        delegate = new BeeEffectRecipeRegistryPlugin(AssassinBeeRecipe.UID,
                Collections.singletonList(recipe), stackHelper, catalysts, recipe::sourceOf);
    }

    @Override
    public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
        return isOffspringNavigation(focus) ? CATEGORY_UIDS : delegate.getRecipeCategoryUids(focus);
    }

    @Override
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> category, IFocus<V> focus) {
        return isOffspringNavigation(focus)
                ? delegate.getRecipeWrappers(category) : delegate.getRecipeWrappers(category, focus);
    }

    @Override
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> category) {
        return delegate.getRecipeWrappers(category);
    }

    private static boolean isOffspringNavigation(IFocus<?> focus) {
        if (focus.getMode() != IFocus.Mode.OUTPUT || !(focus.getValue() instanceof ItemStack)) return false;
        ItemStack stack = (ItemStack) focus.getValue();
        if (stack.isEmpty()) return false;
        try {
            // getType checks the item identity; no genetic NBT is parsed or copied.
            EnumBeeType type = BeeManager.beeRoot.getType(stack);
            return type == EnumBeeType.PRINCESS || type == EnumBeeType.DRONE;
        } catch (RuntimeException | LinkageError failure) {
            return false;
        }
    }
}
