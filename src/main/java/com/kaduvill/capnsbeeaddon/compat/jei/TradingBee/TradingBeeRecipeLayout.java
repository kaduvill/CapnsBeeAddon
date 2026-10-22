package com.kaduvill.capnsbeeaddon.compat.jei.TradingBee;

/** A single emerald above a vertical arrow and a paginated nine-column grid. */
final class TradingBeeRecipeLayout {
    static final int COLUMNS = 9;
    static final int SLOT = 18;
    static final int WIDTH = COLUMNS * SLOT;
    static final int EMERALD_X = (WIDTH - SLOT) / 2;
    static final int ARROW_X = (WIDTH - 17) / 2;
    static final int ARROW_Y = 26;
    static final int LABEL_Y = 55;
    static final int GRID_Y = 68;
    static final int FOOTER_HEIGHT = 13;

    final int pageSize;
    final int footerY;
    final int height;

    TradingBeeRecipeLayout(int entries) {
        int rows = Math.max(1, Math.min(5, (entries + COLUMNS - 1) / COLUMNS));
        pageSize = rows * COLUMNS;
        footerY = entries > pageSize ? GRID_Y + rows * SLOT + 2 : -1;
        height = footerY < 0 ? GRID_Y + rows * SLOT : footerY + FOOTER_HEIGHT;
    }

    int gridX(int index) { return index % COLUMNS * SLOT; }

    int gridY(int index) { return GRID_Y + index / COLUMNS * SLOT; }

    int pages(int entries) { return Math.max(1, (entries + pageSize - 1) / pageSize); }

    static boolean overArrow(int x, int y) {
        return x >= ARROW_X && x < ARROW_X + 18 && y >= ARROW_Y && y < ARROW_Y + 24;
    }
}
