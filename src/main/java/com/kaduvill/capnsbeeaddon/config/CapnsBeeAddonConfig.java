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
    private static final String CAREER_BEES_TWEAKS = "Career Bees Tweaks";
    private static final String CAREER_BEE_TERRITORY = "Career Bee Territory";
    private static final String DEBUG = "Debug";
    private static final String JEI = "JEI";
    private static final String TOP = "TOP";

    public final boolean topIntegration;
    public final boolean topYenteBee;
    public final boolean topStudentBee;
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
    public final boolean accurateEffectDescriptions;
    public final boolean revealDireBee;
    public final boolean revealSoaringBee;
    public final boolean revealBensenBee;
    public final int temporalBeeExtraTicks;
    public final boolean forestryApiaryTerritory;
    public final boolean gendustryApiaryTerritory;
    public final boolean advancedTemporalOverlay;
    public final boolean playerHitboxDebugWireframe;
    public final boolean registerJeiPages;
    public final boolean crusherBee;
    public final boolean honeySmelterBee;
    public final boolean chefBee;
    public final boolean lumberBee;
    public final boolean masonBee;
    public final boolean minerBees;
    public final boolean armorerBee;
    public final boolean sharpeningBee;
    public final boolean smelterBee;
    public final boolean policeBee;
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
    public final boolean devilBee;
    public final boolean madScientistBee;
    public final boolean quantumBees;
    public final boolean collectingBee;
    public final boolean energyBee;
    public final boolean temporalBee;
    public final boolean bensenBee;
    public final boolean direBee;
    public final boolean artisticBee;
    public final boolean ncaBee;
    public final boolean yenteBee;
    public final boolean studentBee;

    private CapnsBeeAddonConfig(File file) {
        // Preserve the category names, including the capitalization of JEI and TOP.
        Configuration config = new Configuration(file, true);
        config.setCategoryComment(TOP, "Optional The One Probe integration. Requires The One Probe and a full game/server restart.");
        config.setCategoryRequiresMcRestart(TOP, true);
        config.setCategoryComment(BUGFIXES, "Optional bug fixes. Requires a full game/server restart.");
        config.setCategoryRequiresMcRestart(BUGFIXES, true);
        config.setCategoryComment(OPTIMIZATIONS, "Optional performance patches. Requires a full game/server restart.");
        config.setCategoryRequiresMcRestart(OPTIMIZATIONS, true);
        config.setCategoryComment(CAREER_BEES_TWEAKS, "Optional Career Bees tweaks. Requires a full game/server restart.");
        config.setCategoryRequiresMcRestart(CAREER_BEES_TWEAKS, true);
        config.setCategoryComment(CAREER_BEE_TERRITORY, "Career Bee territory visualization. Client settings control buttons and overlays; server settings control Forestry boundary replies and advanced Temporal snapshots. Requires a full game/server restart.");
        config.setCategoryRequiresMcRestart(CAREER_BEE_TERRITORY, true);
        config.setCategoryComment(DEBUG, "Client-side debug visuals. Requires a full client restart. Has no effect on dedicated servers.");
        config.setCategoryRequiresMcRestart(DEBUG, true);
        config.setCategoryComment(JEI, "Optional addon JEI/HEI pages. Requires a full client restart.");
        config.setCategoryRequiresMcRestart(JEI, true);

        playerHitboxDebugWireframe = readBoolean(config, DEBUG, "PlayerHitboxDebugWireframe", false,
                "Draw the local player's actual hitbox as a red wireframe. Default: false.");
        topIntegration = readBoolean(config, TOP, "TopIntegration",
                "Master switch for this addon's The One Probe integration.");
        topYenteBee = readBoolean(config, TOP, "YenteBee",
                "Show an attached Yente princess in The One Probe's extended view. Requires TopIntegration.");
        topStudentBee = readBoolean(config, TOP, "StudentBee",
                "Show an attached Student drone in The One Probe's extended view. Requires TopIntegration.");
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
                "Skip unused Industrial Apiary redstone queries. Controls its supporting accessors too. Automatically skipped when using Gendustry CEu.");
        tileApiaryEmptyCoverTick = readBoolean(config, OPTIMIZATIONS,
                "TileApiaryEmptyCoverTick",
                "Skip cover traversal for Industrial Apiaries known to have no covers. Automatically skipped when using Gendustry CEu.");
        accurateEffectDescriptions = readBoolean(config, CAREER_BEES_TWEAKS, "AccurateEffectDescriptions",
                "Correct supported Career Bee analyzer effect descriptions. Currently corrects Fortunate Miner, Police, and Ore Crusher descriptions. Client-only; JEI always uses the shared accurate effect values, even when this is disabled. Default: true.");
        revealDireBee = readBoolean(config, CAREER_BEES_TWEAKS, "RevealDireBee", false,
                "Remove the Dire Bee's secret status while retaining its exclusion from discovery totals. Default: false.");
        revealSoaringBee = readBoolean(config, CAREER_BEES_TWEAKS, "RevealSoaringBee", false,
                "Remove the Soaring Bee's secret status while retaining its exclusion from discovery totals. Default: false.");
        revealBensenBee = readBoolean(config, CAREER_BEES_TWEAKS, "RevealBensenBee", false,
                "Remove the Bensen Bee's secret status while retaining its exclusion from discovery totals. Default: false.");
        temporalBeeExtraTicks = Math.max(0, config.get(
                CAREER_BEES_TWEAKS, "TemporalBeeExtraTicks", 4,
                "Extra TileEntity updates per world-tick phase (START and END) for Temporal Bee. "
                        + "Default: 4, giving 8 extra updates per full world tick. 4 leaves Career Bees unchanged. "
                        + "0 disables these extra updates. Random-ticking blocks are unchanged.",
                0, Integer.MAX_VALUE).setRequiresMcRestart(true).getInt());
        forestryApiaryTerritory = readBoolean(config, CAREER_BEE_TERRITORY, "ForestryApiary",
                "Enable the Career Bee territory button and overlays for Forestry Apiaries. Does not include Bee Houses or Alvearies.");
        gendustryApiaryTerritory = readBoolean(config, CAREER_BEE_TERRITORY, "GendustryIndustrialApiary",
                "Enable the Career Bee territory button and overlays for Gendustry Industrial Apiaries.");
        advancedTemporalOverlay = readBoolean(config, CAREER_BEE_TERRITORY, "AdvancedTemporalOverlay",
                "Enable sneak-click Temporal target boxes and diagnostics for enabled apiaries. Requests server snapshots every 20 client ticks while active, inspecting loaded chunks.");
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
        masonBee = readBoolean(config, JEI, "MasonBee",
                "Register Mason Bee in-world block conversion recipes. Requires RegisterJeiPages.");
        minerBees = readBoolean(config, JEI, "MinerBees",
                "Register Miner, Fortunate Miner and Silky Miner Bee overview pages and catalysts. Requires RegisterJeiPages.");
        armorerBee = readBoolean(config, JEI, "ArmorerBee",
                "Register Armorer Bee equipment improvement recipes. Requires RegisterJeiPages.");
        sharpeningBee = readBoolean(config, JEI, "SharpeningBee",
                "Register Sharpening Bee equipment improvement recipes. Requires RegisterJeiPages.");
        smelterBee = readBoolean(config, JEI, "SmelterBee",
                "Register Smelter Bee furnace recipes. Requires RegisterJeiPages.");
        policeBee = readBoolean(config, JEI, "PoliceBee",
                "Register the Police Bee hostile mob loot and incident report overview page. Requires RegisterJeiPages.");
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
        devilBee = readBoolean(config, JEI, "DevilBee",
                "Register the Devil Bee fire overview page. Requires RegisterJeiPages.");
        madScientistBee = readBoolean(config, JEI, "MadScientistBee",
                "Register the Mad Scientist Bee explosion overview page. Requires RegisterJeiPages.");
        quantumBees = readBoolean(config, JEI, "QuantumBees",
                "Register the shared Quantum Charming/Strange Bees Teleposition overview page. Requires RegisterJeiPages.");
        collectingBee = readBoolean(config, JEI, "CollectingBee",
                "Register the Collecting Bee dropped-item collection overview page. Requires RegisterJeiPages.");
        energyBee = readBoolean(config, JEI, "EnergyBee",
                "Register the Energy Bee power generation overview page. Requires RegisterJeiPages.");
        bensenBee = readBoolean(config, JEI, "BensenBee",
                "Register the Bensen Bee jazz overview page. Requires RegisterJeiPages.");
        temporalBee = readBoolean(config, JEI, "TemporalBee",
                "Register the Temporal Bee block acceleration overview page. Requires RegisterJeiPages.");
        direBee = readBoolean(config, JEI, "DireBee",
                "Register the Dire Bee house-building overview and placed block outputs. Requires RegisterJeiPages.");
        artisticBee = readBoolean(config, JEI, "ArtisticBee",
                "Register the Artistic Bee wall decoration overview page. Requires RegisterJeiPages.");
        ncaBee = readBoolean(config, JEI, "NCABee",
                "Register the N.C.A. Bee Creeper spawning overview page. Requires RegisterJeiPages.");
        yenteBee = readBoolean(config, JEI, "YenteBee",
                "Register the Yente Bee overview page. Requires RegisterJeiPages.");
        studentBee = readBoolean(config, JEI, "StudentBee",
                "Register the Student Bee overview page. Requires RegisterJeiPages.");

        // Forge appends unknown properties, so these lists must be mutable.
        config.setCategoryPropertyOrder(TOP, new ArrayList<>(Arrays.asList("TopIntegration", "YenteBee", "StudentBee")));
        config.setCategoryPropertyOrder(BUGFIXES, new ArrayList<>(Arrays.asList(
                "MinerBeeGunUnbreakableBlockFix", "HoneySmelterBeeGunStackFix", "AssassinBeeAutomationFix", "EnergyBeeOverflowFix",
                "EnergyBeeSidedEnergyFix")));
        config.setCategoryPropertyOrder(OPTIMIZATIONS, new ArrayList<>(Arrays.asList(
                "BusinessBeeTradeGenerationFix", "BeeCanWorkEnvironmentFastPath", "QueenCanWorkCache",
                "TileApiaryRedstoneLookup", "TileApiaryEmptyCoverTick")));
        config.setCategoryPropertyOrder(CAREER_BEES_TWEAKS, new ArrayList<>(Arrays.asList(
                "AccurateEffectDescriptions", "RevealDireBee", "RevealSoaringBee", "RevealBensenBee", "TemporalBeeExtraTicks")));
        config.setCategoryPropertyOrder(CAREER_BEE_TERRITORY, new ArrayList<>(Arrays.asList(
                "ForestryApiary", "GendustryIndustrialApiary", "AdvancedTemporalOverlay")));
        config.setCategoryPropertyOrder(DEBUG, new ArrayList<>(Arrays.asList("PlayerHitboxDebugWireframe")));
        config.setCategoryPropertyOrder(JEI, new ArrayList<>(Arrays.asList(
                "RegisterJeiPages", "CrusherBee", "HoneySmelterBee", "ChefBee", "LumberBee", "MasonBee", "MinerBees",
                "ArmorerBee", "SharpeningBee", "SmelterBee", "PoliceBee", "TaxcollectorBee", "ThiefBee", "AssassinBee", "RepairBee",
                "RetrainingBee", "PriestBee", "HusbandryBee", "ButcherBee", "BusinessBee", "JunkSellerBee",
                "ClockworkBee", "DoctorBee", "PlagueBee", "ElectricianBee", "DevilBee", "MadScientistBee", "CollectingBee", "EnergyBee", "BensenBee", "TemporalBee", "DireBee", "ArtisticBee", "NCABee",
                "QuantumBees", "YenteBee", "StudentBee")));
        if (config.hasChanged()) {
            config.save();
        }
    }

    /** Called by the late mixin loader, before any mod mixins are selected. */
    public static CapnsBeeAddonConfig get() {
        return Holder.INSTANCE;
    }

    public boolean isTopIntegrationEnabled() {
        return topIntegration && (topYenteBee || topStudentBee);
    }

    public boolean isAdvancedTemporalOverlayEnabled() {
        return advancedTemporalOverlay
                && (forestryApiaryTerritory || gendustryApiaryTerritory);
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

    public boolean isMasonBeeJeiEnabled() {
        return registerJeiPages && masonBee;
    }

    public boolean isMinerBeesJeiEnabled() {
        return registerJeiPages && minerBees;
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

    public boolean isPoliceBeeJeiEnabled() {
        return registerJeiPages && policeBee;
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

    public boolean isDevilBeeJeiEnabled() {
        return registerJeiPages && devilBee;
    }

    public boolean isMadScientistBeeJeiEnabled() {
        return registerJeiPages && madScientistBee;
    }

    public boolean isQuantumBeesJeiEnabled() {
        return registerJeiPages && quantumBees;
    }

    public boolean isCollectingBeeJeiEnabled() {
        return registerJeiPages && collectingBee;
    }

    public boolean isEnergyBeeJeiEnabled() {
        return registerJeiPages && energyBee;
    }

    public boolean isBensenBeeJeiEnabled() {
        return registerJeiPages && bensenBee;
    }

    public boolean isTemporalBeeJeiEnabled() {
        return registerJeiPages && temporalBee;
    }

    public boolean isDireBeeJeiEnabled() {
        return registerJeiPages && direBee;
    }

    public boolean isArtisticBeeJeiEnabled() {
        return registerJeiPages && artisticBee;
    }

    public boolean isNCABeeJeiEnabled() {
        return registerJeiPages && ncaBee;
    }

    public boolean isYenteBeeJeiEnabled() {
        return registerJeiPages && yenteBee;
    }

    public boolean isStudentBeeJeiEnabled() {
        return registerJeiPages && studentBee;
    }

    private static boolean readBoolean(Configuration config, String category,
                                       String name, String comment) {
        return readBoolean(config, category, name, true, comment);
    }

    private static boolean readBoolean(Configuration config, String category,
                                       String name, boolean defaultValue, String comment) {
        return config.get(category, name, defaultValue, comment)
                .setRequiresMcRestart(true).getBoolean();
    }

    // JVM initialization publishes a single immutable snapshot for this process.
    private static final class Holder {
        private static final CapnsBeeAddonConfig INSTANCE = new CapnsBeeAddonConfig(
                new File(Loader.instance().getConfigDir(), "capnsbeeaddon.cfg"));
    }
}
