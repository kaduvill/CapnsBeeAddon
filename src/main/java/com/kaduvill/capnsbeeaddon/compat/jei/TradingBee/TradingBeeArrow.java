package com.kaduvill.capnsbeeaddon.compat.jei.TradingBee;

import mezz.jei.api.gui.IDrawableStatic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

/** Pixel arrow with no backing rectangle; supports JEI's animated clipping masks. */
final class TradingBeeArrow implements IDrawableStatic {
    private final int color;

    TradingBeeArrow(int color) { this.color = color; }

    @Override public int getWidth() { return 24; }
    @Override public int getHeight() { return 17; }

    @Override
    public void draw(Minecraft minecraft, int xOffset, int yOffset) {
        draw(minecraft, xOffset, yOffset, 0, 0, 0, 0);
    }

    @Override
    public void draw(Minecraft minecraft, int xOffset, int yOffset,
                     int maskTop, int maskBottom, int maskLeft, int maskRight) {
        for (int y = Math.max(1, maskTop); y < Math.min(16, getHeight() - maskBottom); y++) {
            int left = Math.max(y >= 7 && y <= 9 ? 1 : 15, maskLeft);
            int right = Math.min(23 - Math.abs(y - 8), getWidth() - maskRight);
            if (left < right) Gui.drawRect(xOffset + left, yOffset + y, xOffset + right, yOffset + y + 1, color);
        }
    }
}
