package com.kaduvill.capnsbeeaddon.compat.jei;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.config.CapnsBeeAddonConfig;
import com.kaduvill.capnsbeeaddon.compat.jei.AssassinBee.AssassinBeeRecipe;
import com.kaduvill.capnsbeeaddon.compat.jei.ButcherBee.ButcherBeeRecipe;
import com.kaduvill.capnsbeeaddon.compat.jei.ChefBee.ChefBeeRecipes;
import com.kaduvill.capnsbeeaddon.compat.jei.ClockworkBee.ClockworkBeeRecipe;
import com.kaduvill.capnsbeeaddon.compat.jei.ClockworkBee.ClockworkBeeRecipeCategory;
import com.kaduvill.capnsbeeaddon.compat.jei.CrusherBee.CrusherBeeRecipeCategory;
import com.kaduvill.capnsbeeaddon.compat.jei.CrusherBee.CrusherBeeRecipeWrapper;
import com.kaduvill.capnsbeeaddon.compat.jei.CrusherBee.CrusherBeeRecipes;
import com.kaduvill.capnsbeeaddon.compat.jei.HoneySmelter.HoneySmelterRecipeCategory;
import com.kaduvill.capnsbeeaddon.compat.jei.HoneySmelter.HoneySmelterRecipeRegistryPlugin;
import com.kaduvill.capnsbeeaddon.compat.jei.HoneySmelter.HoneySmelterRecipeWrapper;
import com.kaduvill.capnsbeeaddon.compat.jei.HoneySmelter.HoneySmelterRecipes;
import com.kaduvill.capnsbeeaddon.compat.jei.HusbandryBee.HusbandryBeeRecipe;
import com.kaduvill.capnsbeeaddon.compat.jei.LumberBee.LumberBeeRecipes;
import com.kaduvill.capnsbeeaddon.compat.jei.PriestBee.PriestBeeRecipe;
import com.kaduvill.capnsbeeaddon.compat.jei.RepairBee.RepairBeeRecipe;
import com.kaduvill.capnsbeeaddon.compat.jei.RetrainingBee.RetrainingBeeRecipe;
import com.kaduvill.capnsbeeaddon.compat.jei.SmelterBee.SmelterBeeRecipeRegistryPlugin;
import com.kaduvill.capnsbeeaddon.compat.jei.SmelterBee.SmelterBeeRecipes;
import com.kaduvill.capnsbeeaddon.compat.jei.TaxcollectorBee.TaxcollectorBeeRecipe;
import com.kaduvill.capnsbeeaddon.compat.jei.TradingBee.TradingBeeRecipe;
import com.kaduvill.capnsbeeaddon.compat.jei.TradingBee.TradingBeeRecipeCategory;
import com.kaduvill.capnsbeeaddon.compat.jei.TradingBee.TradingBeeRecipes;
import com.kaduvill.capnsbeeaddon.compat.jei.TradingBee.TradingBeeRecipes.Mode;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipeCategory;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeEffectRecipeRegistryPlugin;
import com.kaduvill.capnsbeeaddon.compat.jei.common.BeeOverviewRecipeRegistryPlugin;
import com.kaduvill.capnsbeeaddon.compat.jei.common.EquipmentBeeRecipes;
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
import java.util.Collections;
import java.util.List;

@JEIPlugin
public final class CapnsBeeAddonJeiPlugin implements IModPlugin {

    private ItemStack crusherQueen = ItemStack.EMPTY;
    private ItemStack honeySmelterQueen = ItemStack.EMPTY;
    private ItemStack chefQueen = ItemStack.EMPTY;
    private ItemStack lumberQueen = ItemStack.EMPTY;
    private ItemStack armorerQueen = ItemStack.EMPTY;
    private ItemStack sharpeningQueen = ItemStack.EMPTY;
    private ItemStack smelterQueen = ItemStack.EMPTY;
    private ItemStack taxcollectorQueen = ItemStack.EMPTY;
    private ItemStack assassinQueen = ItemStack.EMPTY;
    private ItemStack repairQueen = ItemStack.EMPTY;
    private ItemStack graduateQueen = ItemStack.EMPTY;
    private ItemStack phdQueen = ItemStack.EMPTY;
    private ItemStack priestQueen = ItemStack.EMPTY;
    private ItemStack husbandryQueen = ItemStack.EMPTY;
    private ItemStack butcherQueen = ItemStack.EMPTY;
    private ItemStack businessQueen = ItemStack.EMPTY;
    private ItemStack junkSellerQueen = ItemStack.EMPTY;
    private ItemStack clockworkQueen = ItemStack.EMPTY;
    private ClockworkBeeRecipe clockworkRecipe;
    private TradingBeeRecipes.Snapshot tradingRecipes;

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
        if (config.isArmorerBeeJeiEnabled()) {
            armorerQueen = getQueen(CareerBeeSpecies.ARMORER, "Armorer");
            registerEquipmentCategory(registry, EquipmentBeeRecipes.ARMORER, armorerQueen);
        }
        if (config.isSharpeningBeeJeiEnabled()) {
            sharpeningQueen = getQueen(CareerBeeSpecies.SHARPENER, "Sharpening");
            registerEquipmentCategory(registry, EquipmentBeeRecipes.SHARPENING, sharpeningQueen);
        }
        if (config.isSmelterBeeJeiEnabled()) {
            smelterQueen = getQueen(CareerBeeSpecies.SMELTER, "Smelter");
            registry.addRecipeCategories(new BeeEffectRecipeCategory(registry.getJeiHelpers().getGuiHelper(),
                    SmelterBeeRecipes.UID, "jei.capnsbeeaddon.smelter.title", 20,
                    "jei.capnsbeeaddon.base_rate", null, smelterQueen, getPedestal()));
        }
        if (config.isTaxcollectorBeeJeiEnabled()) {
            taxcollectorQueen = getQueen(CareerBeeSpecies.TAXCOLLECTOR, "Taxcollector");
            registry.addRecipeCategories(new BeeEffectRecipeCategory(registry.getJeiHelpers().getGuiHelper(),
                    TaxcollectorBeeRecipe.UID, "jei.capnsbeeaddon.taxcollector.title", TaxcollectorBeeRecipe.BASE_TICKS,
                    "jei.capnsbeeaddon.base_rate", null, taxcollectorQueen, ItemStack.EMPTY));
        }
        if (config.isAssassinBeeJeiEnabled()) {
            assassinQueen = getQueen(CareerBeeSpecies.ASSASSIN, "Assassin");
            registry.addRecipeCategories(new BeeEffectRecipeCategory(registry.getJeiHelpers().getGuiHelper(),
                    AssassinBeeRecipe.UID, "jei.capnsbeeaddon.assassin.title", AssassinBeeRecipe.BASE_TICKS,
                    "jei.capnsbeeaddon.base_rate", null, assassinQueen, ItemStack.EMPTY));
        }
        if (config.isRepairBeeJeiEnabled()) {
            repairQueen = getQueen(CareerBeeSpecies.REPAIR, "Repair");
            registry.addRecipeCategories(new BeeEffectRecipeCategory(registry.getJeiHelpers().getGuiHelper(),
                    RepairBeeRecipe.UID, "jei.capnsbeeaddon.repair.title", RepairBeeRecipe.BASE_TICKS,
                    "jei.capnsbeeaddon.base_rate", "jei.capnsbeeaddon.processing_chance", repairQueen, getPedestal()));
        }
        if (config.isRetrainingBeeJeiEnabled()) {
            graduateQueen = getQueen(CareerBeeSpecies.VOCATIONAL, "Graduate");
            phdQueen = getQueen(CareerBeeSpecies.PHD, "PHD");
            registry.addRecipeCategories(new BeeEffectRecipeCategory(registry.getJeiHelpers().getGuiHelper(),
                    RetrainingBeeRecipe.UID, "jei.capnsbeeaddon.retraining.title", RetrainingBeeRecipe.BASE_TICKS,
                    "jei.capnsbeeaddon.base_rate", null, graduateQueen, getPedestal()));
        }
        if (config.isPriestBeeJeiEnabled()) {
            priestQueen = getQueen(CareerBeeSpecies.PRIEST, "Priest");
            registry.addRecipeCategories(new BeeEffectRecipeCategory(registry.getJeiHelpers().getGuiHelper(),
                    PriestBeeRecipe.UID, "jei.capnsbeeaddon.priest.title", PriestBeeRecipe.BASE_TICKS,
                    "jei.capnsbeeaddon.base_rate", null, priestQueen, getPedestal()));
        }
        if (config.isHusbandryBeeJeiEnabled()) {
            husbandryQueen = getQueen(CareerBeeSpecies.HUSBANDRYIST, "Husbandry");
            registry.addRecipeCategories(new BeeEffectRecipeCategory(registry.getJeiHelpers().getGuiHelper(),
                    HusbandryBeeRecipe.UID, "jei.capnsbeeaddon.husbandry.title", HusbandryBeeRecipe.BASE_TICKS,
                    "jei.capnsbeeaddon.base_rate", "jei.capnsbeeaddon.husbandry.limit",
                    husbandryQueen, ItemStack.EMPTY, false));
        }
        if (config.isButcherBeeJeiEnabled()) {
            butcherQueen = getQueen(CareerBeeSpecies.BUTCHER, "Butcher");
            registry.addRecipeCategories(new BeeEffectRecipeCategory(registry.getJeiHelpers().getGuiHelper(),
                    ButcherBeeRecipe.UID, "jei.capnsbeeaddon.butcher.title", ButcherBeeRecipe.BASE_TICKS,
                    "jei.capnsbeeaddon.base_rate", null, butcherQueen, ItemStack.EMPTY, false));
        }
        if (config.isClockworkBeeJeiEnabled()) {
            Item engine = Item.getByNameOrId("forestry:engine_clockwork");
            if (engine != null) {
                clockworkQueen = getQueen(CareerBeeSpecies.CLOCKWORK, "Clockwork");
                clockworkRecipe = new ClockworkBeeRecipe(new ItemStack(engine));
                registry.addRecipeCategories(new ClockworkBeeRecipeCategory(
                        registry.getJeiHelpers().getGuiHelper(), clockworkQueen));
            } else {
                CapnsBeeAddon.LOGGER.warn("Cannot display Clockwork Bee JEI page: Clockwork Engine is unavailable");
            }
        }
        if (config.isBusinessBeeJeiEnabled() || config.isJunkSellerBeeJeiEnabled()) {
            try {
                tradingRecipes = TradingBeeRecipes.read();
                if (config.isBusinessBeeJeiEnabled()) {
                    businessQueen = getQueen(CareerBeeSpecies.BUISNESS, "Business");
                    registry.addRecipeCategories(new TradingBeeRecipeCategory(registry.getJeiHelpers().getGuiHelper(),
                            Mode.BUSINESS, businessQueen, tradingRecipes.get(Mode.BUSINESS).size()));
                }
                if (config.isJunkSellerBeeJeiEnabled()) {
                    junkSellerQueen = getQueen(CareerBeeSpecies.JUNK, "Junk Seller");
                    registry.addRecipeCategories(new TradingBeeRecipeCategory(registry.getJeiHelpers().getGuiHelper(),
                            Mode.JUNK_SELLER, junkSellerQueen, tradingRecipes.get(Mode.JUNK_SELLER).size()));
                }
            } catch (RuntimeException | LinkageError failure) {
                CapnsBeeAddon.LOGGER.warn("Cannot register trading bee JEI categories", failure);
            }
        }
    }

    private void registerEquipmentCategory(IRecipeCategoryRegistration registry,
                                           EquipmentBeeRecipes effect, ItemStack queen) {
        registry.addRecipeCategories(new BeeEffectRecipeCategory(registry.getJeiHelpers().getGuiHelper(),
                effect.getUid(), effect.getTitleKey(), effect.getBaseTicks(),
                "jei.capnsbeeaddon.base_rate", "jei.capnsbeeaddon.processing_chance", queen, getPedestal()));
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
        if (config.isArmorerBeeJeiEnabled()) registerEquipment(registry, EquipmentBeeRecipes.ARMORER, armorerQueen);
        if (config.isSharpeningBeeJeiEnabled()) registerEquipment(registry, EquipmentBeeRecipes.SHARPENING, sharpeningQueen);
        if (config.isSmelterBeeJeiEnabled()) registerSmelter(registry);
        if (config.isTaxcollectorBeeJeiEnabled()) registerTaxcollector(registry);
        if (config.isAssassinBeeJeiEnabled()) registerAssassin(registry);
        if (config.isRepairBeeJeiEnabled()) registerRepair(registry);
        if (config.isRetrainingBeeJeiEnabled()) registerRetraining(registry);
        if (config.isPriestBeeJeiEnabled()) registerPriest(registry);
        if (config.isHusbandryBeeJeiEnabled()) registerHusbandry(registry);
        if (config.isButcherBeeJeiEnabled()) registerButcher(registry);
        if (config.isClockworkBeeJeiEnabled() && clockworkRecipe != null) {
            registry.addRecipes(Collections.singletonList(clockworkRecipe), ClockworkBeeRecipe.UID);
            registerCatalysts(registry, ClockworkBeeRecipe.UID, clockworkQueen, false);
            CapnsBeeAddon.LOGGER.info("Registered Clockwork Bee JEI overview page");
        }
        if (tradingRecipes != null) {
            if (config.isBusinessBeeJeiEnabled()) registerTrading(registry, Mode.BUSINESS, businessQueen);
            if (config.isJunkSellerBeeJeiEnabled()) registerTrading(registry, Mode.JUNK_SELLER, junkSellerQueen);
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

    private void registerEquipment(IModRegistry registry, EquipmentBeeRecipes effect, ItemStack queen) {
        try {
            List<EquipmentBeeRecipes.Recipe> recipes = effect.read(
                    registry.getIngredientRegistry().getAllIngredients(VanillaTypes.ITEM));
            if (!recipes.isEmpty()) {
                List<ItemStack> catalysts = registerCatalysts(registry, effect.getUid(), queen);
                registry.addRecipeRegistryPlugin(new BeeEffectRecipeRegistryPlugin(effect.getUid(), recipes,
                        registry.getJeiHelpers().getStackHelper(), catalysts, effect::sourceOf, effect::withoutBonus));
            }
            CapnsBeeAddon.LOGGER.info("Registered {} {} JEI recipes", recipes.size(), effect.getUid());
        } catch (RuntimeException | LinkageError failure) {
            CapnsBeeAddon.LOGGER.warn("Cannot register {} JEI recipes", effect.getUid(), failure);
        }
    }

    private void registerSmelter(IModRegistry registry) {
        try {
            SmelterBeeRecipes recipes = SmelterBeeRecipes.read(registry.getJeiHelpers().getStackHelper());
            if (!recipes.getRecipes().isEmpty()) {
                List<ItemStack> catalysts = registerCatalysts(registry, SmelterBeeRecipes.UID, smelterQueen);
                // Sole provider: input matching follows the furnace, while outputs can have many source recipes.
                registry.addRecipeRegistryPlugin(new SmelterBeeRecipeRegistryPlugin(recipes,
                        registry.getJeiHelpers().getStackHelper(), catalysts));
            }
            CapnsBeeAddon.LOGGER.info("Registered {} Smelter Bee JEI recipes", recipes.getRecipes().size());
        } catch (RuntimeException | LinkageError failure) {
            CapnsBeeAddon.LOGGER.warn("Cannot register Smelter Bee JEI recipes", failure);
        }
    }

    private static ItemStack getPedestal() {
        Item item = Item.getByNameOrId("careerbees:flower_pedastal");
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    private void registerTaxcollector(IModRegistry registry) {
        try {
            registry.addRecipes(Collections.singletonList(new TaxcollectorBeeRecipe(
                    registry.getJeiHelpers().getGuiHelper(),
                    registry.getIngredientRegistry().getIngredientRenderer(VanillaTypes.ITEM))), TaxcollectorBeeRecipe.UID);
            registerCatalysts(registry, TaxcollectorBeeRecipe.UID, taxcollectorQueen, false);
            CapnsBeeAddon.LOGGER.info("Registered Taxcollector Bee JEI information page");
        } catch (RuntimeException | LinkageError failure) {
            CapnsBeeAddon.LOGGER.warn("Cannot register Taxcollector Bee JEI information page", failure);
        }
    }

    private void registerAssassin(IModRegistry registry) {
        try {
            AssassinBeeRecipe recipe = AssassinBeeRecipe.read(registry.getJeiHelpers().getGuiHelper());
            List<ItemStack> catalysts = registerCatalysts(registry, AssassinBeeRecipe.UID, assassinQueen, false);
            // One stable overview wrapper also provides navigation from every princess and drone.
            registry.addRecipeRegistryPlugin(new BeeOverviewRecipeRegistryPlugin(AssassinBeeRecipe.UID, recipe,
                    registry.getJeiHelpers().getStackHelper(), catalysts,
                    recipe::acceptsInput, BeeOverviewRecipeRegistryPlugin::isDroneOrPrincess));
            CapnsBeeAddon.LOGGER.info("Registered Assassin Bee JEI information page");
        } catch (RuntimeException | LinkageError failure) {
            CapnsBeeAddon.LOGGER.warn("Cannot register Assassin Bee JEI information page", failure);
        }
    }

    private void registerRepair(IModRegistry registry) {
        try {
            RepairBeeRecipe recipe = new RepairBeeRecipe();
            if (!recipe.getInput().isEmpty()) {
                registry.addRecipes(Collections.singletonList(recipe), RepairBeeRecipe.UID);
                registerCatalysts(registry, RepairBeeRecipe.UID, repairQueen);
                CapnsBeeAddon.LOGGER.info("Registered Repair Bee JEI overview page");
            }
        } catch (RuntimeException | LinkageError failure) {
            CapnsBeeAddon.LOGGER.warn("Cannot register Repair Bee JEI overview page", failure);
        }
    }

    private void registerPriest(IModRegistry registry) {
        try {
            PriestBeeRecipe recipe = new PriestBeeRecipe(
                    registry.getIngredientRegistry().getIngredientRenderer(VanillaTypes.ITEM));
            if (!recipe.getInput().isEmpty()) {
                registry.addRecipes(Collections.singletonList(recipe), PriestBeeRecipe.UID);
                registerCatalysts(registry, PriestBeeRecipe.UID, priestQueen);
                CapnsBeeAddon.LOGGER.info("Registered Priest Bee JEI overview page");
            }
        } catch (RuntimeException | LinkageError failure) {
            CapnsBeeAddon.LOGGER.warn("Cannot register Priest Bee JEI overview page", failure);
        }
    }

    private void registerRetraining(IModRegistry registry) {
        try {
            RetrainingBeeRecipe recipe = RetrainingBeeRecipe.read(
                    registry.getIngredientRegistry().getIngredientRenderer(VanillaTypes.ITEM));
            List<ItemStack> catalysts = registerCatalysts(registry, RetrainingBeeRecipe.UID, graduateQueen, false);
            if (!phdQueen.isEmpty()) {
                registry.addRecipeCatalyst(phdQueen, RetrainingBeeRecipe.UID);
                catalysts.add(phdQueen);
            }
            ItemStack pedestal = getPedestal();
            if (!pedestal.isEmpty()) {
                registry.addRecipeCatalyst(pedestal, RetrainingBeeRecipe.UID);
                catalysts.add(pedestal);
            }
            registry.addRecipeRegistryPlugin(new BeeOverviewRecipeRegistryPlugin(RetrainingBeeRecipe.UID, recipe,
                    registry.getJeiHelpers().getStackHelper(), catalysts,
                    BeeOverviewRecipeRegistryPlugin::isDroneOrPrincess,
                    BeeOverviewRecipeRegistryPlugin::isDroneOrPrincess));
            CapnsBeeAddon.LOGGER.info("Registered Graduate/PHD Bee retraining JEI overview page");
        } catch (RuntimeException | LinkageError failure) {
            CapnsBeeAddon.LOGGER.warn("Cannot register Graduate/PHD Bee retraining JEI overview page", failure);
        }
    }

    private void registerHusbandry(IModRegistry registry) {
        try {
            registry.addRecipes(Collections.singletonList(new HusbandryBeeRecipe(
                    registry.getJeiHelpers().getGuiHelper())), HusbandryBeeRecipe.UID);
            registerCatalysts(registry, HusbandryBeeRecipe.UID, husbandryQueen, false);
            CapnsBeeAddon.LOGGER.info("Registered Husbandry Bee JEI information page");
        } catch (RuntimeException | LinkageError failure) {
            CapnsBeeAddon.LOGGER.warn("Cannot register Husbandry Bee JEI information page", failure);
        }
    }

    private void registerButcher(IModRegistry registry) {
        try {
            registry.addRecipes(Collections.singletonList(new ButcherBeeRecipe(
                    registry.getJeiHelpers().getGuiHelper(),
                    registry.getIngredientRegistry().getIngredientRenderer(VanillaTypes.ITEM))), ButcherBeeRecipe.UID);
            registerCatalysts(registry, ButcherBeeRecipe.UID, butcherQueen, false);
            CapnsBeeAddon.LOGGER.info("Registered Butcher Bee JEI overview page");
        } catch (RuntimeException | LinkageError failure) {
            CapnsBeeAddon.LOGGER.warn("Cannot register Butcher Bee JEI overview page", failure);
        }
    }

    private void registerTrading(IModRegistry registry, Mode mode, ItemStack queen) {
        try {
            TradingBeeRecipe recipe = new TradingBeeRecipe(mode, tradingRecipes.get(mode));
            List<ItemStack> catalysts = registerCatalysts(registry, mode.uid, queen, false);
            registry.addRecipeRegistryPlugin(new BeeOverviewRecipeRegistryPlugin(mode.uid, recipe,
                    registry.getJeiHelpers().getStackHelper(), catalysts, recipe::acceptsInput, recipe::acceptsOutput));
            CapnsBeeAddon.LOGGER.info("Registered {} trading bee JEI grid entries for {}",
                    tradingRecipes.get(mode).size(), mode.uid);
        } catch (RuntimeException | LinkageError failure) {
            CapnsBeeAddon.LOGGER.warn("Cannot register {} JEI overview", mode.uid, failure);
        }
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
