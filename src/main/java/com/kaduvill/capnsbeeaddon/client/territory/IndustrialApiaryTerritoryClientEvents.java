package com.kaduvill.capnsbeeaddon.client.territory;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.network.CapnsBeeAddonNetwork.TemporalSnapshotEvent;
import forestry.api.apiculture.IBeeHousing;
import net.bdew.gendustry.machines.apiary.GuiApiary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * Adds one vanilla GUI button over Gendustry's Industrial Apiary GUI and owns
 * the client-only overlay events.
 */
@Mod.EventBusSubscriber(
        modid = CapnsBeeAddon.MODID,
        value = Side.CLIENT
)
public final class IndustrialApiaryTerritoryClientEvents {

    private static final int BUTTON_ID = 0xCA71;

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    // left of redstone-mode widget at (137, 5).
    private static final int BUTTON_RELATIVE_X = 119;
    private static final int BUTTON_RELATIVE_Y = 5;

    @Nullable
    private static GuiApiary activeGui;

    @Nullable
    private static GuiButtonCareerTerritory activeButton;

    private IndustrialApiaryTerritoryClientEvents() {
    }

    @SubscribeEvent
    public static void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!(event.getGui() instanceof GuiApiary)) {
            return;
        }

        GuiApiary gui = (GuiApiary) event.getGui();

        // Be defensive against another init pass on the same GUI.
        Iterator<GuiButton> iterator = event.getButtonList().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().id == BUTTON_ID) {
                iterator.remove();
            }
        }

        int guiLeft = (gui.width - GUI_WIDTH) / 2;
        int guiTop = (gui.height - GUI_HEIGHT) / 2;

        /*
         GuiApiary#te() has the concrete return type TileApiary.
         Resolving TileApiary's full inheritance tree at compile time pulls
         in Gendustry's optional Redstone Flux API.

         Erase the concrete type first, then use the Forestry interface that
         this feature actually needs.
         */
        Object apiary = gui.te();
        if (!(apiary instanceof IBeeHousing)) {
            return;
        }

        GuiButtonCareerTerritory button = new GuiButtonCareerTerritory(
                BUTTON_ID,
                guiLeft + BUTTON_RELATIVE_X,
                guiTop + BUTTON_RELATIVE_Y,
                (IBeeHousing) apiary
        );

        event.getButtonList().add(button);
        activeGui = gui;
        activeButton = button;
    }

    @SubscribeEvent
    public static void onButtonPressed(
            GuiScreenEvent.ActionPerformedEvent.Post event
    ) {
        if (event.getButton() instanceof GuiButtonCareerTerritory) {
            Minecraft minecraft = Minecraft.getMinecraft();
            boolean advanced = GameSettings.isKeyDown(
                    minecraft.gameSettings.keyBindSneak
            );
            ((GuiButtonCareerTerritory) event.getButton())
                    .toggleOverlay(advanced);
        }
    }

    @SubscribeEvent
    public static void onTemporalSnapshot(TemporalSnapshotEvent event) {
        CareerTerritoryOverlay.acceptSnapshot(event.getSnapshot());
    }

    /**
     * GuiScreen draws vanilla buttons, but it does not automatically invoke
     * GuiButton#drawButtonForegroundLayer. Draw the tooltip after the complete
     * Gendustry screen so slots and widgets cannot cover it.
     */
    @SubscribeEvent
    public static void onGuiDrawn(GuiScreenEvent.DrawScreenEvent.Post event) {
        GuiButtonCareerTerritory button = activeButton;

        if (button != null && event.getGui() == activeGui) {
            button.drawButtonForegroundLayer(
                    event.getMouseX(),
                    event.getMouseY()
            );
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (activeGui != null
                && Minecraft.getMinecraft().currentScreen != activeGui) {
            activeGui = null;
            activeButton = null;
        }

        CareerTerritoryOverlay.clientTick();
    }

    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        CareerTerritoryOverlay.render(event.getPartialTicks());
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            CareerTerritoryOverlay.renderHud();
        }


        // Temporary player-hitbox visualization for territory testing.
        //PlayerHitboxDebugOverlay.render(event.getPartialTicks());
    }
}