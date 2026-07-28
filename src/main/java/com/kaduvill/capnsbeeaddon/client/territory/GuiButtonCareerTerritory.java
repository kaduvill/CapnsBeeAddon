package com.kaduvill.capnsbeeaddon.client.territory;

import forestry.api.apiculture.IBeeHousing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Compact 16x16 button using the vanilla button texture and a code-drawn box
 * icon, so no additional GUI texture is required.
 */
public final class GuiButtonCareerTerritory extends GuiButton {

    private final IBeeHousing housing;

    @Nullable
    private CareerTerritoryInfo cachedInfo;

    private long lastResolvedTick = Long.MIN_VALUE;

    public GuiButtonCareerTerritory(
            int buttonId,
            int x,
            int y,
            IBeeHousing housing
    ) {
        super(buttonId, x, y, 16, 16, "");
        this.housing = housing;
    }

    @Override
    public void drawButton(
            Minecraft minecraft,
            int mouseX,
            int mouseY,
            float partialTicks
    ) {
        refresh(minecraft, false);
        visible = cachedInfo != null;

        if (!visible) {
            hovered = false;
            return;
        }

        hovered = mouseX >= x
                && mouseY >= y
                && mouseX < x + width
                && mouseY < y + height;

        minecraft.getTextureManager().bindTexture(BUTTON_TEXTURES);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int hoverState = getHoverState(hovered);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
        );

        int textureY = 46 + hoverState * 20;

        int leftWidth = width / 2;
        int rightWidth = width - leftWidth;
        int topHeight = height / 2;
        int bottomHeight = height - topHeight;

        drawTexturedModalRect(
                x,
                y,
                0,
                textureY,
                leftWidth,
                topHeight
        );

        drawTexturedModalRect(
                x + leftWidth,
                y,
                200 - rightWidth,
                textureY,
                rightWidth,
                topHeight
        );

        drawTexturedModalRect(
                x,
                y + topHeight,
                0,
                textureY + 20 - bottomHeight,
                leftWidth,
                bottomHeight
        );

        drawTexturedModalRect(
                x + leftWidth,
                y + topHeight,
                200 - rightWidth,
                textureY + 20 - bottomHeight,
                rightWidth,
                bottomHeight
        );

        mouseDragged(minecraft, mouseX, mouseY);
        drawTerritoryIcon();
    }

    @Override
    public void drawButtonForegroundLayer(int mouseX, int mouseY) {
        if (!visible || !hovered || cachedInfo == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.currentScreen == null) {
            return;
        }

        List<String> tooltip = new ArrayList<>(3);
        tooltip.add(I18n.format(
                CareerTerritoryOverlay.isSelected(cachedInfo)
                        ? "gui.capnsbeeaddon.territory.hide"
                        : "gui.capnsbeeaddon.territory.show"
        ));
        tooltip.add(I18n.format(
                "gui.capnsbeeaddon.territory.effect",
                cachedInfo.getEffectName()
        ));
        tooltip.add(I18n.format(
                "gui.capnsbeeaddon.territory.size",
                cachedInfo.getWidth(),
                cachedInfo.getHeight(),
                cachedInfo.getDepth()
        ));

        GuiUtils.drawHoveringText(
                tooltip,
                mouseX,
                mouseY,
                minecraft.currentScreen.width,
                minecraft.currentScreen.height,
                -1,
                minecraft.fontRenderer
        );
    }

    public void toggleOverlay() {
        refresh(Minecraft.getMinecraft(), true);
        if (cachedInfo != null) {
            CareerTerritoryOverlay.toggle(cachedInfo);
        }
    }

    private void refresh(Minecraft minecraft, boolean force) {
        if (minecraft.world == null) {
            cachedInfo = null;
            lastResolvedTick = Long.MIN_VALUE;
            return;
        }

        long worldTime = minecraft.world.getTotalWorldTime();
        if (!force && worldTime == lastResolvedTick) {
            return;
        }

        lastResolvedTick = worldTime;
        cachedInfo = CareerTerritoryResolver.resolve(housing);
    }

    private void drawTerritoryIcon() {
        boolean selected = CareerTerritoryOverlay.isSelected(cachedInfo);
        int colour;

        if (selected) {
            colour = 0xFF55FFFF;
        } else if (hovered) {
            colour = 0xFFFFFFA0;
        } else {
            colour = 0xFFE0E0E0;
        }

        // Two offset rectangles form a tiny wireframe-box icon.
        drawHorizontalLine(x + 4, x + 10, y + 6, colour);
        drawHorizontalLine(x + 4, x + 10, y + 12, colour);
        drawVerticalLine(x + 4, y + 6, y + 12, colour);
        drawVerticalLine(x + 10, y + 6, y + 12, colour);

        drawHorizontalLine(x + 6, x + 12, y + 4, colour);
        drawVerticalLine(x + 12, y + 4, y + 10, colour);

        drawHorizontalLine(x + 4, x + 6, y + 6, colour);
        drawHorizontalLine(x + 10, x + 12, y + 10, colour);
        drawVerticalLine(x + 6, y + 4, y + 6, colour);
        drawVerticalLine(x + 12, y + 10, y + 12, colour);
    }
}
