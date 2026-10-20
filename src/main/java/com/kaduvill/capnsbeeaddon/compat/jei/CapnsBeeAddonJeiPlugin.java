package com.kaduvill.capnsbeeaddon.compat.jei;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.config.CapnsBeeAddonConfig;
import com.kaduvill.capnsbeeaddon.compat.jei.ChefBee.ChefBeeRecipes;
import com.kaduvill.capnsbeeaddon.compat.jei.CrusherBee.CrusherBeeRecipeCategory;
import com.kaduvill.capnsbeeaddon.compat.jei.CrusherBee.CrusherBeeRecipeWrapper;
import com.kaduvill.capnsbeeaddon.compat.jei.CrusherBee.CrusherBeeRecipes;
import com.kaduvill.capnsbeeaddon.compat.jei.HoneySmelter.HoneySmelterRecipeCategory;
import com.kaduvill.capnsbeeaddon.compat.jei.HoneySmelter.HoneySmelterRecipeRegistryPlugin;
import com.kaduvill.capnsbeeaddon.compat.jei.HoneySmelter.HoneySmelterRecipeWrapper;
import com.kaduvill.capnsbeeaddon.compat.jei.HoneySmelter.HoneySmelterRecipes;
import com.kaduvill.capnsbeeaddon.compat.jei.LumberBee.LumberBeeRecipes;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipeCategory;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipeRegistryPlugin;
import com.rwtema.careerbees.bees.CareerBeeEntry;
import com.rwtema.careerbees.bees.CareerBeeSpecies;
import com.rwtema.careerbees.items.ItemIngredients;
import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.EnumBeeType;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

@JEIPlugin
public final class CapnsBeeAddonJeiPlugin implements IModPlugin {

    private ItemStack crusherQueen = ItemStack.EMPTY;
    private ItemStack honeySmelterQueen = ItemStack.EMPTY;
    private ItemStack chefQueen = ItemStack.EMPTY;
    private ItemStack lumberQueen = ItemStack.EMPTY;

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
        if (config.isChefBeeJeiEnabled()) {
            chefQueen = getQueen(CareerBeeSpecies.COOK, "Chef");
            registry.addRecipeCategories(new BeeEffectRecipeCategory(registry.getJeiHelpers().getGuiHelper(),
                    ChefBeeRecipes.UID, "jei.capnsbeeaddon.chef.title", 60,
                    "jei.capnsbeeaddon.base_rate_per_application", "jei.capnsbeeaddon.chef.per_application",
                    chefQueen, getPedestal()));
        }
        if (config.isLumberBeeJeiEnabled()) {
            lumberQueen = getQueen(CareerBeeSpecies.LUMBER, "Lumber");
            registry.addRecipeCategories(new BeeEffectRecipeCategory(registry.getJeiHelpers().getGuiHelper(),
                    LumberBeeRecipes.UID, "jei.capnsbeeaddon.lumber.title", 200,
                    "jei.capnsbeeaddon.base_rate", null, lumberQueen, ItemStack.EMPTY));
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
        if (config.isChefBeeJeiEnabled()) registerChef(registry);
        if (config.isLumberBeeJeiEnabled()) registerLumber(registry);
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

    private void registerChef(IModRegistry registry) {
        try {
            List<ChefBeeRecipes.Recipe> recipes = ChefBeeRecipes.read(
                    registry.getIngredientRegistry().getAllIngredients(VanillaTypes.ITEM));
            if (!recipes.isEmpty()) {
                List<ItemStack> catalysts = registerCatalysts(registry, ChefBeeRecipes.UID, chefQueen);
                registry.addRecipeRegistryPlugin(new BeeEffectRecipeRegistryPlugin(ChefBeeRecipes.UID, recipes,
                        registry.getJeiHelpers().getStackHelper(), catalysts,
                        ChefBeeRecipes::sourceOf, ChefBeeRecipes::withoutGlazing));
            }
            CapnsBeeAddon.LOGGER.info("Registered {} Chef Bee JEI recipes", recipes.size());
        } catch (RuntimeException | LinkageError failure) {
            CapnsBeeAddon.LOGGER.warn("Cannot register Chef Bee JEI recipes", failure);
        }
    }

    private void registerLumber(IModRegistry registry) {
        try {
            ItemStack bark = ItemIngredients.IngredientType.BARK.get();
            List<LumberBeeRecipes.Recipe> recipes = LumberBeeRecipes.read(OreDictionary.getOres("logWood", false),
                    registry.getIngredientRegistry().getIngredientHelper(VanillaTypes.ITEM), bark);
            if (!recipes.isEmpty()) {
                List<ItemStack> catalysts = registerCatalysts(registry, LumberBeeRecipes.UID, lumberQueen, false);
                registry.addRecipeRegistryPlugin(new BeeEffectRecipeRegistryPlugin(LumberBeeRecipes.UID, recipes,
                        registry.getJeiHelpers().getStackHelper(), catalysts, stack -> LumberBeeRecipes.sourceOf(stack, bark)));
            }
            CapnsBeeAddon.LOGGER.info("Registered {} Lumber Bee JEI recipes", recipes.size());
        } catch (RuntimeException | LinkageError failure) {
            CapnsBeeAddon.LOGGER.warn("Cannot register Lumber Bee JEI recipes", failure);
        }
    }

    private static ItemStack getPedestal() {
        Item item = Item.getByNameOrId("careerbees:flower_pedastal");
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static List<ItemStack> registerCatalysts(IModRegistry registry, String uid, ItemStack queen) {
        return registerCatalysts(registry, uid, queen, true);
    }

    private static List<ItemStack> registerCatalysts(IModRegistry registry, String uid, ItemStack queen, boolean needsPedestal) {
        List<ItemStack> catalysts = new ArrayList<>(2);
        if (!queen.isEmpty()) catalysts.add(queen);
        ItemStack pedestal = needsPedestal ? getPedestal() : ItemStack.EMPTY;
        if (!pedestal.isEmpty()) catalysts.add(pedestal);
        for (ItemStack catalyst : catalysts) registry.addRecipeCatalyst(catalyst, uid);
        return catalysts;
    }
}
