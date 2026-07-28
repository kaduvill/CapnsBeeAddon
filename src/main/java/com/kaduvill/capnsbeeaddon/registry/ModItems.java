package com.kaduvill.capnsbeeaddon.registry;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.item.ItemTemporalFocusApiary;
import com.kaduvill.capnsbeeaddon.item.ItemTerritoryRestrictor;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = CapnsBeeAddon.MODID)
public final class ModItems {

    public static final ItemTemporalFocusApiary TEMPORAL_FOCUS_APIARY =
            new ItemTemporalFocusApiary();

    private ModItems() {
    }

    public static final ItemTerritoryRestrictor
            TERRITORY_RESTRICTOR =
            new ItemTerritoryRestrictor();

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                ModItems.TEMPORAL_FOCUS_APIARY,
                ModItems.TERRITORY_RESTRICTOR
        );
    }
}
