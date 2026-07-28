package com.kaduvill.capnsbeeaddon.registry;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = CapnsBeeAddon.MODID, value = Side.CLIENT)
public final class ClientRegistration {

    private ClientRegistration() {
    }

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(
                ModItems.TEMPORAL_FOCUS_APIARY,
                0,
                new ModelResourceLocation(
                        Objects.requireNonNull(ModItems.TEMPORAL_FOCUS_APIARY.getRegistryName()),
                        "inventory"
                )
        );

        ModelLoader.setCustomModelResourceLocation(
                ModItems.TERRITORY_RESTRICTOR,
                0,
                new ModelResourceLocation(
                        ModItems.TERRITORY_RESTRICTOR.getRegistryName(),
                        "inventory"
                )
        );
    }
}
