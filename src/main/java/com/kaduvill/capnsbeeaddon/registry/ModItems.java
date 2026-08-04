package com.kaduvill.capnsbeeaddon.registry;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.item.ItemProductNullifierUpgrade;
import com.kaduvill.capnsbeeaddon.item.ItemTemporalApiaryFocusUpgrade;
import com.kaduvill.capnsbeeaddon.item.ItemTemporalTileEntityFocusUpgrade;
import com.kaduvill.capnsbeeaddon.item.ItemTemporalGrowthFocusUpgrade;
import com.kaduvill.capnsbeeaddon.item.ItemTerritoryRestrictor;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = CapnsBeeAddon.MODID)
public final class ModItems {

    public static final ItemTemporalApiaryFocusUpgrade TEMPORAL_FOCUS_APIARY =
            new ItemTemporalApiaryFocusUpgrade();
    public static final ItemTemporalTileEntityFocusUpgrade TEMPORAL_FOCUS_TILEENTITY =
            new ItemTemporalTileEntityFocusUpgrade();
    public static final ItemTemporalGrowthFocusUpgrade TEMPORAL_FOCUS_GROWTH =
            new ItemTemporalGrowthFocusUpgrade();
    public static final ItemTerritoryRestrictor TERRITORY_RESTRICTOR =
            new ItemTerritoryRestrictor();
    public static final ItemProductNullifierUpgrade PRODUCT_NULLIFIER =
            new ItemProductNullifierUpgrade();
    private ModItems() {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                TEMPORAL_FOCUS_APIARY,
                TEMPORAL_FOCUS_TILEENTITY,
                TEMPORAL_FOCUS_GROWTH,
                TERRITORY_RESTRICTOR,
                PRODUCT_NULLIFIER
        );
    }
}