package com.kaduvill.capnsbeeaddon.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/** Startup settings shared by mixin selection and optional integrations. */
public final class CapnsBeeAddonConfig {

    private static final String BUGFIXES = "Bugfixes";
    private static final String OPTIMIZATIONS = "Optimizations";
    private static final String JEI = "JEI";

    public final boolean minerBeeGunUnbreakableBlockFix;
    public final boolean honeySmelterBeeGunStackFix;
    public final boolean assassinBeeAutomationFix;
    public final boolean energyBeeOverflowFix;
    public final boolean energyBeeSidedEnergyFix;
    public final boolean businessBeeTradeGenerationFix;
    public final boolean beeCanWorkEnvironmentFastPath;
    public final boolean queenCanWorkCache;
    public final boolean tileApiaryRedstoneLookup;
    public final boolean tileApiaryEmptyCoverTick;
    public final boolean registerJeiPages;
    public final boolean crusherBee;
    public final boolean honeySmelterBee;
    public final boolean chefBee;
    public final boolean lumberBee;
    public final boolean armorerBee;
    public final boolean sharpeningBee;
    public final boolean smelterBee;
    public final boolean taxcollectorBee;
    public final boolean thiefBee;
    public final boolean assassinBee;
    public final boolean repairBee;
    public final boolean retrainingBee;
    public final boolean priestBee;
    public final boolean husbandryBee;
    public final boolean butcherBee;
    public final boolean businessBee;
    public final boolean junkSellerBee;
    public final boolean clockworkBee;
    public final boolean doctorBee;
    public final boolean plagueBee;
    public final boolean electricianBee;

    private CapnsBeeAddonConfig(File file) {
        // Preserve the category names, including the capitalization of JEI.
        Configuration config = new Configuration(file, true);
        config.setCategoryComment("general", "Reserved for general settings.");
        config.setCategoryComment(BUGFIXES, "Optional bug fixes. Requires a full game/server restart.");
        config.setCategoryRequiresMcRestart(BUGFIXES, true);
        config.setCategoryComment(OPTIMIZATIONS, "Optional performance patches. Requires a full game/server restart.");
        config.setCategoryRequiresMcRestart(OPTIMIZATIONS, true);
        config.setCategoryComment(JEI, "Optional addon JEI/HEI pages. Requires a full client restart.");
        config.setCategoryRequiresMcRestart(JEI, true);

        minerBeeGunUnbreakableBlockFix = readBoolean(config, BUGFIXES,
                "MinerBeeGunUnbreakableBlockFix",
                "Prevent Miner, Fortunate Miner and Silky Miner Bees fired from the Bee Gun from mining blocks with negative hardness (such as bedrock). Does not change apiary mining.");
        honeySmelterBeeGunStackFix = readBoolean(config, BUGFIXES,
                "HoneySmelterBeeGunStackFix",
                "Convert one iron ingot per Honey-Smelter Bee Gun action while preserving the remaining dropped stack. Does not change apiary processing. Disabling restores original Career Bees behavior, including voiding.");
        assassinBeeAutomationFix = readBoolean(config, BUGFIXES,
                "AssassinBeeAutomationFix",
                "Fix Assassin Bee disappearing and crashing when used in bee housing. Does not change the Bee Gun. Disabling restores original Career Bees behavior, including its known bugs.");
        energyBeeOverflowFix = readBoolean(config, BUGFIXES,
                "EnergyBeeOverflowFix",
                "Cap excessive Energy Bee output at 2,147,483,647 RF to prevent negative transfers.");
        energyBeeSidedEnergyFix = readBoolean(config, BUGFIXES,
                "EnergyBeeSidedEnergyFix",
                "Use the touching or hit face when the null-side energy handler is missing or cannot receive.");
        businessBeeTradeGenerationFix = readBoolean(config, OPTIMIZATIONS,
                "BusinessBeeTradeGenerationFix",
                "Skip vanilla treasure-map and Immersive Engineering ore-vein map/revolver-part trade generation for Business and Junk Seller Bees, and skip Junk Seller trade generation without extractable emeralds in adjacent inventories. Immersive Engineering is optional.");
        beeCanWorkEnvironmentFastPath = readBoolean(config, OPTIMIZATIONS,
                "BeeCanWorkEnvironmentFastPath",
                "Skip irrelevant rain, light and sky queries for Industrial Apiaries.");
        queenCanWorkCache = readBoolean(config, OPTIMIZATIONS,
                "QueenCanWorkCache",
                "Reuse the Industrial Apiary queen's canWork result within one world tick.");
        tileApiaryRedstoneLookup = readBoolean(config, OPTIMIZATIONS,
                "TileApiaryRedstoneLookup",
                "Skip unused Industrial Apiary redstone queries. Controls its supporting accessors too.");
        tileApiaryEmptyCoverTick = readBoolean(config, OPTIMIZATIONS,
                "TileApiaryEmptyCoverTick",
                "Skip cover traversal for Industrial Apiaries known to have no covers.");
        registerJeiPages = readBoolean(config, JEI, "RegisterJeiPages",
                "Master switch for all JEI/HEI pages added by this addon.");
        crusherBee = readBoolean(config, JEI, "CrusherBee",
                "Register the Ore Crusher Bee category, recipes and catalysts. Requires RegisterJeiPages.");
        honeySmelterBee = readBoolean(config, JEI, "HoneySmelterBee",
                "Register the Honey-Smelter Bee category, recipe and catalysts. Requires RegisterJeiPages.");
        chefBee = readBoolean(config, JEI, "ChefBee",
                "Register Chef Bee honey-glazing recipes. Requires RegisterJeiPages.");
        lumberBee = readBoolean(config, JEI, "LumberBee",
                "Register Lumber Bee bark recipes and tree requirements. Requires RegisterJeiPages.");
        armorerBee = readBoolean(config, JEI, "ArmorerBee",
                "Register Armorer Bee equipment improvement recipes. Requires RegisterJeiPages.");
        sharpeningBee = readBoolean(config, JEI, "SharpeningBee",
                "Register Sharpening Bee equipment improvement recipes. Requires RegisterJeiPages.");
        smelterBee = readBoolean(config, JEI, "SmelterBee",
                "Register Smelter Bee furnace recipes. Requires RegisterJeiPages.");
        taxcollectorBee = readBoolean(config, JEI, "TaxcollectorBee",
                "Register the Taxcollector Bee information page. Requires RegisterJeiPages.");
        thiefBee = readBoolean(config, JEI, "ThiefBee",
                "Register the Thief Bee player-inventory stealing overview. Requires RegisterJeiPages.");
        assassinBee = readBoolean(config, JEI, "AssassinBee",
                "Register the Assassin Bee information page. Requires RegisterJeiPages.");
        repairBee = readBoolean(config, JEI, "RepairBee",
                "Register the Repair Bee vanilla repair overview page. Requires RegisterJeiPages.");
        retrainingBee = readBoolean(config, JEI, "RetrainingBee",
                "Register the Graduate/PHD Bee retraining overview page. Requires RegisterJeiPages.");
        priestBee = readBoolean(config, JEI, "PriestBee",
                "Register the Priest Bee random enchanting overview page. Requires RegisterJeiPages.");
        husbandryBee = readBoolean(config, JEI, "HusbandryBee",
                "Register the Husbandry Bee animal breeding overview page. Requires RegisterJeiPages.");
        butcherBee = readBoolean(config, JEI, "ButcherBee",
                "Register the Butcher Bee animal loot overview page. Requires RegisterJeiPages.");
        businessBee = readBoolean(config, JEI, "BusinessBee",
                "Register the Business Bee villager trade overview. Requires RegisterJeiPages.");
        junkSellerBee = readBoolean(config, JEI, "JunkSellerBee",
                "Register the Junk Seller Bee villager trade overview. Requires RegisterJeiPages.");
        clockworkBee = readBoolean(config, JEI, "ClockworkBee",
                "Register the Clockwork Bee engine-winding overview. Requires RegisterJeiPages.");
        doctorBee = readBoolean(config, JEI, "DoctorBee",
                "Register the Doctor Bee healing overview. Requires RegisterJeiPages.");
        plagueBee = readBoolean(config, JEI, "PlagueBee",
                "Register the Plague Bee cleansing and zombie-villager curing overviews. Requires RegisterJeiPages.");
        electricianBee = readBoolean(config, JEI, "ElectricianBee",
                "Register Electrician Bee damage and mob conversion pages. Requires RegisterJeiPages.");

        // Forge appends unknown properties, so these lists must be mutable.
        config.setCategoryPropertyOrder(BUGFIXES, new ArrayList<>(Arrays.asList(
                "MinerBeeGunUnbreakableBlockFix", "HoneySmelterBeeGunStackFix", "AssassinBeeAutomationFix", "EnergyBeeOverflowFix",
                "EnergyBeeSidedEnergyFix")));
        config.setCategoryPropertyOrder(OPTIMIZATIONS, new ArrayList<>(Arrays.asList(
                "BusinessBeeTradeGenerationFix", "BeeCanWorkEnvironmentFastPath", "QueenCanWorkCache",
                "TileApiaryRedstoneLookup", "TileApiaryEmptyCoverTick")));
        config.setCategoryPropertyOrder(JEI, new ArrayList<>(Arrays.asList(
                "RegisterJeiPages", "CrusherBee", "HoneySmelterBee", "ChefBee", "LumberBee",
                "ArmorerBee", "SharpeningBee", "SmelterBee", "TaxcollectorBee", "ThiefBee", "AssassinBee", "RepairBee",
                "RetrainingBee", "PriestBee", "HusbandryBee", "ButcherBee", "BusinessBee", "JunkSellerBee",
                "ClockworkBee", "DoctorBee", "PlagueBee", "ElectricianBee")));
        if (config.hasChanged()) {
            config.save();
        }
    }

    /** Called by the late mixin loader, before any mod mixins are selected. */
    public static CapnsBeeAddonConfig get() {
        return Holder.INSTANCE;
    }

    public boolean isCrusherBeeJeiEnabled() {
        return registerJeiPages && crusherBee;
    }

    public boolean isHoneySmelterBeeJeiEnabled() {
        return registerJeiPages && honeySmelterBee;
    }

    public boolean isChefBeeJeiEnabled() {
        return registerJeiPages && chefBee;
    }

    public boolean isLumberBeeJeiEnabled() {
        return registerJeiPages && lumberBee;
    }

    public boolean isArmorerBeeJeiEnabled() {
        return registerJeiPages && armorerBee;
    }

    public boolean isSharpeningBeeJeiEnabled() {
        return registerJeiPages && sharpeningBee;
    }

    public boolean isSmelterBeeJeiEnabled() {
        return registerJeiPages && smelterBee;
    }

    public boolean isTaxcollectorBeeJeiEnabled() {
        return registerJeiPages && taxcollectorBee;
    }

    public boolean isThiefBeeJeiEnabled() {
        return registerJeiPages && thiefBee;
    }

    public boolean isAssassinBeeJeiEnabled() {
        return registerJeiPages && assassinBee;
    }

    public boolean isRepairBeeJeiEnabled() {
        return registerJeiPages && repairBee;
    }

    public boolean isRetrainingBeeJeiEnabled() {
        return registerJeiPages && retrainingBee;
    }

    public boolean isPriestBeeJeiEnabled() {
        return registerJeiPages && priestBee;
    }

    public boolean isHusbandryBeeJeiEnabled() {
        return registerJeiPages && husbandryBee;
    }

    public boolean isButcherBeeJeiEnabled() {
        return registerJeiPages && butcherBee;
    }

    public boolean isBusinessBeeJeiEnabled() {
        return registerJeiPages && businessBee;
    }

    public boolean isJunkSellerBeeJeiEnabled() {
        return registerJeiPages && junkSellerBee;
    }

    public boolean isClockworkBeeJeiEnabled() {
        return registerJeiPages && clockworkBee;
    }

    public boolean isDoctorBeeJeiEnabled() {
        return registerJeiPages && doctorBee;
    }

    public boolean isPlagueBeeJeiEnabled() {
        return registerJeiPages && plagueBee;
    }

    public boolean isElectricianBeeJeiEnabled() {
        return registerJeiPages && electricianBee;
    }

    private static boolean readBoolean(Configuration config, String category,
                                       String name, String comment) {
        return config.get(category, name, true, comment)
                .setRequiresMcRestart(true).getBoolean();
    }

    // JVM initialization publishes a single immutable snapshot for this process.
    private static final class Holder {
        private static final CapnsBeeAddonConfig INSTANCE = new CapnsBeeAddonConfig(
                new File(Loader.instance().getConfigDir(), "capnsbeeaddon.cfg"));
    }
}
