package com.kaduvill.capnsbeeaddon.mixin;

import com.kaduvill.capnsbeeaddon.config.CapnsBeeAddonConfig;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class CapnsBeeAddonMixinLoader implements ILateMixinLoader, IMixinConfigPlugin {

    private static final String MIXIN_PACKAGE = "com.kaduvill.capnsbeeaddon.mixin.";

    @Override
    public List<String> getMixinConfigs() {
        CapnsBeeAddonConfig.get();
        return Collections.singletonList(
                "capnsbeeaddon.mixins.json"
        );
    }

    @Override
    public void onLoad(String mixinPackage) {
        CapnsBeeAddonConfig.get();
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        CapnsBeeAddonConfig config = CapnsBeeAddonConfig.get();
        // Match names without loading mixin classes or their target mods.
        switch (mixinClassName) {
            case MIXIN_PACKAGE + "careerbees.tweaks.CareerBeeEntryRevealMixin":
                return config.revealDireBee || config.revealSoaringBee || config.revealBensenBee;
            case MIXIN_PACKAGE + "careerbees.EffectDiggingMixin":
                return config.minerBeeGunUnbreakableBlockFix;
            case MIXIN_PACKAGE + "careerbees.EntityBeeSwarmHoneySmelterMixin":
                return config.honeySmelterBeeGunStackFix;
            case MIXIN_PACKAGE + "careerbees.EffectAssassinMixin":
                return config.assassinBeeAutomationFix;
            case MIXIN_PACKAGE + "careerbees.EffectPowerOverflowMixin":
                return config.energyBeeOverflowFix;
            case MIXIN_PACKAGE + "careerbees.EffectPowerSidedEnergyMixin":
                return config.energyBeeSidedEnergyFix;
            case MIXIN_PACKAGE + "careerbees.EffectBuisnessTradeGenerationMixin":
                return config.businessBeeTradeGenerationFix;
            case MIXIN_PACKAGE + "forestry.BeeCanWorkEnvironmentFastPathMixin":
                return config.beeCanWorkEnvironmentFastPath;
            case MIXIN_PACKAGE + "forestry.QueenCanWorkCacheMixin":
                return config.queenCanWorkCache;
            case MIXIN_PACKAGE + "gendustry.TileApiaryRedstoneLookupMixin":
            case MIXIN_PACKAGE + "gendustry.TileApiaryAccessor":
            case MIXIN_PACKAGE + "gendustry.TileApiaryServerTickClosureAccessor":
                return config.tileApiaryRedstoneLookup;
            case MIXIN_PACKAGE + "gendustry.TileApiaryEmptyCoverTickMixin":
                return config.tileApiaryEmptyCoverTick;
            default:
                // Feature implementations and the territory overlay accessor stay active.
                return true;
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass,
                         String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass,
                          String mixinClassName, IMixinInfo mixinInfo) {
    }
}
