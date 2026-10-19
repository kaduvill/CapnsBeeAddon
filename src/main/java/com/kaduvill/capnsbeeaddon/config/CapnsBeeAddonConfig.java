package com.kaduvill.capnsbeeaddon.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

/** Startup settings shared by mixin selection and optional integrations. */
public final class CapnsBeeAddonConfig {

    private static final String OPTIMIZATIONS = "Optimizations";
    private static final String JEI = "JEI";

    public final boolean beeCanWorkEnvironmentFastPath;
    public final boolean queenCanWorkCache;
    public final boolean tileApiaryRedstoneLookup;
    public final boolean tileApiaryEmptyCoverTick;
    public final boolean registerJeiPages;
    public final boolean crusherBee;
    public final boolean honeySmelterBee;

    private CapnsBeeAddonConfig(File file) {
        // Preserve the category names, including the capitalization of JEI.
        Configuration config = new Configuration(file, true);
        config.setCategoryComment("general", "Reserved for general settings.");
        config.setCategoryComment(OPTIMIZATIONS, "Optional performance patches. Requires a full game/server restart.");
        config.setCategoryRequiresMcRestart(OPTIMIZATIONS, true);
        config.setCategoryComment(JEI, "Optional addon JEI/HEI pages. Requires a full client restart.");
        config.setCategoryRequiresMcRestart(JEI, true);

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

        // Forge appends unknown properties, so these lists must be mutable.
        config.setCategoryPropertyOrder(OPTIMIZATIONS, new ArrayList<>(Arrays.asList(
                "BeeCanWorkEnvironmentFastPath", "QueenCanWorkCache",
                "TileApiaryRedstoneLookup", "TileApiaryEmptyCoverTick")));
        config.setCategoryPropertyOrder(JEI, new ArrayList<>(Arrays.asList("RegisterJeiPages", "CrusherBee", "HoneySmelterBee")));
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
