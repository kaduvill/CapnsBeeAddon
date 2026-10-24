package com.kaduvill.capnsbeeaddon.client.territory;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.kaduvill.capnsbeeaddon.config.CapnsBeeAddonConfig;
import com.kaduvill.capnsbeeaddon.network.CapnsBeeAddonNetwork.TemporalSnapshotEvent;
import com.kaduvill.capnsbeeaddon.network.CapnsBeeAddonNetwork.BasicBoundaryEvent;
import forestry.api.apiculture.IBeeHousing;
import forestry.apiculture.gui.ContainerBeeHousing;
import forestry.apiculture.gui.GuiBeeHousing;
import forestry.apiculture.tiles.TileApiary;
import net.bdew.gendustry.machines.apiary.GuiApiary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

/**
 * Shares the territory button and client-only overlay events between supported apiaries.
 */
@Mod.EventBusSubscriber(
        modid = CapnsBeeAddon.MODID,
        value = Side.CLIENT
)
public final class CareerTerritoryClientEvents {

    private static final int BUTTON_ID = 0xCA71;

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    // left of redstone-mode widget at (137, 5).
    private static final int BUTTON_RELATIVE_X = 119;
    private static final int BUTTON_RELATIVE_Y = 5;

    @Nullable
    private static GuiScreen activeGui;

    @Nullable
    private static GuiButtonCareerTerritory activeButton;

    private CareerTerritoryClientEvents() {
    }

    @SubscribeEvent
    public static void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (!CapnsBeeAddonConfig.get().gendustryApiaryTerritory
                || !(event.getGui() instanceof GuiApiary)) {
            return;
        }

        GuiApiary gui = (GuiApiary) event.getGui();

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

        addButton(gui, event.getButtonList(), (IBeeHousing) apiary,
                guiLeft + BUTTON_RELATIVE_X, guiTop + BUTTON_RELATIVE_Y);
    }

    /** Called after every Forestry analyzer initialization, including internal rebuilds. */
    public static void onForestryGuiInit(GuiBeeHousing<?> gui, List<GuiButton> buttons) {
        if (!CapnsBeeAddonConfig.get().forestryApiaryTerritory
                || !(gui.inventorySlots instanceof ContainerBeeHousing)) {
            return;
        }

        // Slots retain the housing tile. Inspect them only during GUI initialization,
        // avoiding another accessor and any dependence on analyzer-dependent slot indices.
        for (Slot slot : gui.inventorySlots.inventorySlots) {
            if (slot.inventory instanceof TileApiary) {
                // Use the interface directly without resolving Forestry's optional BuildCraft API.
                addButton(gui, buttons, (IBeeHousing) slot.inventory,
                        gui.getGuiLeft() + 151, gui.getGuiTop() + 2);
                return;
            }
        }
    }

    private static void addButton(GuiScreen gui, List<GuiButton> buttons,
                                  IBeeHousing housing, int x, int y) {
        Iterator<GuiButton> iterator = buttons.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() instanceof GuiButtonCareerTerritory) {
                iterator.remove();
            }
        }

        GuiButtonCareerTerritory button = new GuiButtonCareerTerritory(
                BUTTON_ID,
                x,
                y,
                housing
        );

        buttons.add(button);
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

    @SubscribeEvent
    public static void onBasicBoundary(BasicBoundaryEvent event) {
        CareerTerritoryOverlay.acceptBasicBoundary(event.dimension, event.source,
                event.generation, event.boundary);
    }

    /**
     * GuiScreen draws vanilla buttons, but it does not automatically invoke
     * GuiButton#drawButtonForegroundLayer. Draw the tooltip after the complete
     * screen so slots and widgets cannot cover it.
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
        if (CapnsBeeAddonConfig.get().playerHitboxDebugWireframe) {
            PlayerHitboxDebugOverlay.render(event.getPartialTicks());
        }
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            CareerTerritoryOverlay.renderHud();
        }
    }
}
