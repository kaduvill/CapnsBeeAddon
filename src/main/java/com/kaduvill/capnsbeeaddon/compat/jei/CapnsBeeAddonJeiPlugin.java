package com.kaduvill.capnsbeeaddon.compat.jei;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.config.CapnsBeeAddonConfig;
import com.kaduvill.capnsbeeaddon.compat.jei.CrusherBee.CrusherBeeRecipeCategory;
import com.kaduvill.capnsbeeaddon.compat.jei.CrusherBee.CrusherBeeRecipeWrapper;
import com.kaduvill.capnsbeeaddon.compat.jei.CrusherBee.CrusherBeeRecipes;
import com.kaduvill.capnsbeeaddon.compat.jei.HoneySmelter.HoneySmelterRecipeCategory;
import com.kaduvill.capnsbeeaddon.compat.jei.HoneySmelter.HoneySmelterRecipeRegistryPlugin;
import com.kaduvill.capnsbeeaddon.compat.jei.HoneySmelter.HoneySmelterRecipeWrapper;
import com.kaduvill.capnsbeeaddon.compat.jei.HoneySmelter.HoneySmelterRecipes;
import com.rwtema.careerbees.bees.CareerBeeEntry;
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

    private ItemStack crusherQueen = ItemStack.EMPTY;
    private ItemStack honeySmelterQueen = ItemStack.EMPTY;

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        CapnsBeeAddonConfig config = CapnsBeeAddonConfig.get();
        if (config.isCrusherBeeJeiEnabled()) {
            crusherQueen = getQueen(CareerBeeSpecies.ORE_CRUSHER, "Ore Crusher");
            registry.addRecipeCategories(new CrusherBeeRecipeCategory(registry.getJeiHelpers().getGuiHelper(), crusherQueen));
        }
        if (config.isHoneySmelterBeeJeiEnabled()) {
            honeySmelterQueen = getQueen(CareerBeeSpecies.HONEY_SMELTER, "Honey-Smelter");
            registry.addRecipeCategories(new HoneySmelterRecipeCategory(registry.getJeiHelpers().getGuiHelper(), honeySmelterQueen));
        }
    }

    private static ItemStack getQueen(CareerBeeEntry bee, String name) {
        try {
            return BeeManager.beeRoot.getMemberStack(bee.get(), EnumBeeType.QUEEN);
        } catch (RuntimeException | LinkageError failure) {
            CapnsBeeAddon.LOGGER.warn("Cannot display {} Queen JEI catalyst", name, failure);
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void register(IModRegistry registry) {
        CapnsBeeAddonConfig config = CapnsBeeAddonConfig.get();
        if (config.isCrusherBeeJeiEnabled()) {
            registerCrusher(registry);
        }
        if (config.isHoneySmelterBeeJeiEnabled()) {
            registerHoneySmelter(registry);
        }
    }

    private void registerCrusher(IModRegistry registry) {
        // JEI registration runs after normal mod initialization and recipe scripts.
        List<CrusherBeeRecipeWrapper> recipes = new ArrayList<>();
        for (CrusherBeeRecipes.Recipe recipe : CrusherBeeRecipes.read()) {
            recipes.add(new CrusherBeeRecipeWrapper(recipe));
        }
        registry.addRecipes(recipes, CrusherBeeRecipeCategory.UID);
        registerCatalysts(registry, CrusherBeeRecipeCategory.UID, crusherQueen);
        CapnsBeeAddon.LOGGER.info("Registered {} Crusher Bee JEI recipes", recipes.size());
    }

    private void registerHoneySmelter(IModRegistry registry) {
        try {
            List<HoneySmelterRecipes.Recipe> recipes = HoneySmelterRecipes.read();
            for (HoneySmelterRecipes.Recipe recipe : recipes) {
                List<ItemStack> catalysts = registerCatalysts(registry, HoneySmelterRecipeCategory.UID, honeySmelterQueen);
                // This is the sole lookup provider for this recipe, avoiding duplicate pages.
                // Native JEI indexing alone omits some accepted wildcard/NBT variants.
                registry.addRecipeRegistryPlugin(new HoneySmelterRecipeRegistryPlugin(
                        new HoneySmelterRecipeWrapper(recipe), registry.getJeiHelpers().getStackHelper(), catalysts));
            }
            CapnsBeeAddon.LOGGER.info("Registered {} Honey-Smelter Bee JEI recipes", recipes.size());
        } catch (RuntimeException | LinkageError failure) {
            CapnsBeeAddon.LOGGER.warn("Cannot register Honey-Smelter Bee JEI recipe", failure);
        }
    }

    private static List<ItemStack> registerCatalysts(IModRegistry registry, String uid, ItemStack queen) {
        List<ItemStack> catalysts = new ArrayList<>(2);
        if (!queen.isEmpty()) catalysts.add(queen);
        Item pedestal = Item.getByNameOrId("careerbees:flower_pedastal");
        if (pedestal != null) catalysts.add(new ItemStack(pedestal));
        for (ItemStack catalyst : catalysts) registry.addRecipeCatalyst(catalyst, uid);
        return catalysts;
    }
}
