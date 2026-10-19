package com.kaduvill.capnsbeeaddon.compat.jei;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.config.CapnsBeeAddonConfig;
import com.kaduvill.capnsbeeaddon.compat.jei.CrusherBee.CrusherBeeRecipeCategory;
import com.kaduvill.capnsbeeaddon.compat.jei.CrusherBee.CrusherBeeRecipeWrapper;
import com.kaduvill.capnsbeeaddon.compat.jei.CrusherBee.CrusherBeeRecipes;
import com.rwtema.careerbees.bees.CareerBeeSpecies;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JEIPlugin
public final class CapnsBeeAddonJeiPlugin implements IModPlugin {

    private ItemStack queen = ItemStack.EMPTY;

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        if (!CapnsBeeAddonConfig.get().isCrusherBeeJeiEnabled()) {
            return;
        }
        try {
            queen = BeeManager.beeRoot.getMemberStack(CareerBeeSpecies.ORE_CRUSHER.get(), EnumBeeType.QUEEN);
        } catch (RuntimeException | LinkageError failure) {
            CapnsBeeAddon.LOGGER.warn("Cannot display Ore Crusher Queen JEI catalyst", failure);
        }
        registry.addRecipeCategories(new CrusherBeeRecipeCategory(registry.getJeiHelpers().getGuiHelper(), queen));
    }

    @Override
    public void register(IModRegistry registry) {
        if (!CapnsBeeAddonConfig.get().isCrusherBeeJeiEnabled()) {
            return;
        }
        // JEI registration runs after normal mod initialization and recipe scripts.
        List<CrusherBeeRecipeWrapper> recipes = new ArrayList<>();
        for (CrusherBeeRecipes.Recipe recipe : CrusherBeeRecipes.read()) {
            recipes.add(new CrusherBeeRecipeWrapper(recipe));
        }
        registry.addRecipes(recipes, CrusherBeeRecipeCategory.UID);
        if (!queen.isEmpty()) registry.addRecipeCatalyst(queen, CrusherBeeRecipeCategory.UID);
        Item pedestal = Item.getByNameOrId("careerbees:flower_pedastal");
        if (pedestal != null) registry.addRecipeCatalyst(new ItemStack(pedestal), CrusherBeeRecipeCategory.UID);
        CapnsBeeAddon.LOGGER.info("Registered {} Crusher Bee JEI recipes", recipes.size());
    }
}
