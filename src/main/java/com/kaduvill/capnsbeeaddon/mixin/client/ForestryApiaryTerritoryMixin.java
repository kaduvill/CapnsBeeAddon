package com.kaduvill.capnsbeeaddon.mixin.client;

import com.kaduvill.capnsbeeaddon.client.territory.CareerTerritoryClientEvents;
import forestry.apiculture.gui.GuiBeeHousing;
import forestry.core.gui.GuiAnalyzerProvider;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiAnalyzerProvider.class)
public abstract class ForestryApiaryTerritoryMixin extends GuiScreen {

    // Forestry also calls initGui directly after clearing buttons when its analyzer changes.
    @Inject(method = "initGui", at = @At("TAIL"))
    private void capnsbeeaddon$addTerritoryButton(CallbackInfo ci) {
        Object screen = this;
        if (screen instanceof GuiBeeHousing<?>) {
            CareerTerritoryClientEvents.onForestryGuiInit(
                    (GuiBeeHousing<?>) screen,
                    buttonList
            );
        }
    }
}
