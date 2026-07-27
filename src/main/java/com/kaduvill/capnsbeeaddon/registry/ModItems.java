package com.kaduvill.capnsbeeaddon.registry;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.item.ItemTemporalFocusUpgrade;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = CapnsBeeAddon.MODID)
public final class ModItems {

    public static final ItemTemporalFocusUpgrade TEMPORAL_FOCUS_UPGRADE =
            new ItemTemporalFocusUpgrade();

    private ModItems() {
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(TEMPORAL_FOCUS_UPGRADE);
    }
}
