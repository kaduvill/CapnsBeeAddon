package com.kaduvill.capnsbeeaddon.client.territory;

import com.kaduvill.capnsbeeaddon.compat.careerbees.TemporalTerritorySnapshot;
import com.kaduvill.capnsbeeaddon.compat.gendustry.TemporalFocusUpgradeHelper.TemporalFocusMode;
import com.kaduvill.capnsbeeaddon.network.CapnsBeeAddonNetwork;
import forestry.api.apiculture.IBeeHousing;
import net.bdew.gendustry.api.blocks.IIndustrialApiary;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import javax.annotation.Nullable;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public final class CareerTerritoryOverlay {

    private static final int REFRESH_INTERVAL_TICKS = 20;
    private static final double TERRITORY_EPSILON = 0.002D;
    private static final double TARGET_EPSILON = 0.003D;
    private static final int TERRITORY_COLOUR = 0xFF33CCFF;
    private static final int QUALIFIED_COLOUR = 0xFF44FF66;
    private static final int EXCLUDED_COLOUR = 0xFFFF6655;
    private static final FloatBuffer PREVIOUS_COLOUR = BufferUtils.createFloatBuffer(16);
    private static Mode mode = Mode.OFF;

    @Nullable
    private static CareerTerritoryInfo selected;

    @Nullable
    private static TemporalTerritorySnapshot snapshot;

    private static HudLine[] hudLines = new HudLine[0];
    private static int refreshTicks;

    private CareerTerritoryOverlay() {
    }

    public static void toggle(
            CareerTerritoryInfo info,
            boolean advanced
    ) {
        if (advanced) {
            if (!info.isTemporal()) {
                return;
            }
            if (mode == Mode.ADVANCED_TEMPORAL && sameSource(info)) {
                clear();
                return;
            }
            mode = Mode.ADVANCED_TEMPORAL;
            selected = info;
            snapshot = null;
            hudLines = pendingHud();
            refreshTicks = 0;
            requestSnapshot();
            return;
        }

        if (mode == Mode.BASIC_TERRITORY && sameSource(info)) {
            clear();
        } else {
            mode = Mode.BASIC_TERRITORY;
            selected = info;
            snapshot = null;
            hudLines = new HudLine[0];
            refreshTicks = 0;
        }
    }

    public static boolean isSelected(@Nullable CareerTerritoryInfo info) {
        return info != null && mode != Mode.OFF && sameSource(info);
    }

    public static boolean isBasicSelected(
            @Nullable CareerTerritoryInfo info
    ) {
        return info != null
                && mode == Mode.BASIC_TERRITORY
                && sameSource(info);
    }

    public static boolean isAdvancedSelected(
            @Nullable CareerTerritoryInfo info
    ) {
        return info != null
                && mode == Mode.ADVANCED_TEMPORAL
                && sameSource(info);
    }

    public static void clear() {
        mode = Mode.OFF;
        selected = null;
        snapshot = null;
        hudLines = new HudLine[0];
        refreshTicks = 0;
    }

    public static void acceptSnapshot(
            TemporalTerritorySnapshot incoming
    ) {
        CareerTerritoryInfo current = selected;
        if (mode != Mode.ADVANCED_TEMPORAL
                || current == null
                || incoming.getDimension() != current.getDimension()
                || incoming.getSource() != current.getSource().toLong()) {
            return;
        }

        if (incoming.getStatus()
                == TemporalTerritorySnapshot.Status.SOURCE_UNAVAILABLE) {
            clear();
            return;
        }

        snapshot = incoming;
        hudLines = buildHud(incoming);
    }

    public static void clientTick() {
        CareerTerritoryInfo current = selected;
        if (mode == Mode.OFF || current == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        World world = minecraft.world;
        if (world == null
                || world.provider.getDimension() != current.getDimension()) {
            clear();
            return;
        }

        BlockPos source = current.getSource();
        if (!world.isBlockLoaded(source, false)) {
            clear();
            return;
        }

        TileEntity tile = world.getTileEntity(source);
        if (tile == null || tile.isInvalid()) {
            clear();
            return;
        }

        if (mode == Mode.ADVANCED_TEMPORAL
                && !(tile instanceof IIndustrialApiary)) {
            clear();
            return;
        }
        if (mode == Mode.BASIC_TERRITORY
                && !(tile instanceof IBeeHousing)) {
            clear();
            return;
        }

        if (++refreshTicks < REFRESH_INTERVAL_TICKS) {
            return;
        }
        refreshTicks = 0;

        if (mode == Mode.ADVANCED_TEMPORAL) {
            requestSnapshot();
        } else {
            CareerTerritoryInfo refreshed =
                    CareerTerritoryResolver.resolve((IBeeHousing) tile);
            if (refreshed == null) {
                clear();
            } else {
                selected = refreshed;
            }
        }
    }

    public static void render(float partialTicks) {
        if (mode == Mode.BASIC_TERRITORY) {
            renderBasic(partialTicks);
        } else if (mode == Mode.ADVANCED_TEMPORAL) {
            renderAdvanced(partialTicks);
        }
    }

    public static void renderHud() {
        if (mode != Mode.ADVANCED_TEMPORAL || hudLines.length == 0) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        FontRenderer font = minecraft.fontRenderer;
        int x = 6;
        int y = 6;

        for (HudLine line : hudLines) {
            if (line.swatch != 0) {
                Gui.drawRect(x, y + 2, x + 7, y + 9, line.swatch);
                font.drawStringWithShadow(line.text, x + 11, y, 0xFFFFFF);
            } else {
                font.drawStringWithShadow(line.text, x, y, 0xFFFFFF);
            }
            y += font.FONT_HEIGHT + 2;
        }
    }

    private static void renderBasic(float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        World world = minecraft.world;
        CareerTerritoryInfo current = selected;

        if (current == null
                || world == null
                || world.provider.getDimension() != current.getDimension()) {
            return;
        }

        Entity camera = minecraft.getRenderViewEntity();
        if (camera == null) {
            return;
        }

        double cameraX = interpolate(
                camera.lastTickPosX,
                camera.posX,
                partialTicks
        );
        double cameraY = interpolate(
                camera.lastTickPosY,
                camera.posY,
                partialTicks
        );
        double cameraZ = interpolate(
                camera.lastTickPosZ,
                camera.posZ,
                partialTicks
        );
        AxisAlignedBB renderedBounds = current.getBounds()
                .grow(TERRITORY_EPSILON)
                .offset(-cameraX, -cameraY, -cameraZ);

        GlStateManager.pushMatrix();
        try {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO
            );
            GlStateManager.disableTexture2D();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GL11.glLineWidth(1.0F);
            RenderGlobal.drawSelectionBoundingBox(
                    renderedBounds,
                    0.20F,
                    0.80F,
                    1.00F,
                    0.22F
            );
            GlStateManager.enableDepth();
            GL11.glLineWidth(2.0F);
            RenderGlobal.drawSelectionBoundingBox(
                    renderedBounds,
                    0.20F,
                    0.80F,
                    1.00F,
                    0.90F
            );
        } finally {
            GL11.glLineWidth(1.0F);
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }
    }

    private static void renderAdvanced(float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        World world = minecraft.world;
        TemporalTerritorySnapshot current = snapshot;
        if (current == null
                || world == null
                || world.provider.getDimension() != current.getDimension()) {
            return;
        }

        Entity camera = minecraft.getRenderViewEntity();
        if (camera == null) {
            return;
        }

        double cameraX = interpolate(
                camera.lastTickPosX,
                camera.posX,
                partialTicks
        );
        double cameraY = interpolate(
                camera.lastTickPosY,
                camera.posY,
                partialTicks
        );
        double cameraZ = interpolate(
                camera.lastTickPosZ,
                camera.posZ,
                partialTicks
        );

        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean textureEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int blendSourceRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        int blendDestinationRgb = GL11.glGetInteger(
                GL14.GL_BLEND_DST_RGB
        );
        int blendSourceAlpha = GL11.glGetInteger(
                GL14.GL_BLEND_SRC_ALPHA
        );
        int blendDestinationAlpha = GL11.glGetInteger(
                GL14.GL_BLEND_DST_ALPHA
        );
        float lineWidth = GL11.glGetFloat(GL11.GL_LINE_WIDTH);
        PREVIOUS_COLOUR.clear();
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, PREVIOUS_COLOUR);
        float red = PREVIOUS_COLOUR.get(0);
        float green = PREVIOUS_COLOUR.get(1);
        float blue = PREVIOUS_COLOUR.get(2);
        float alpha = PREVIOUS_COLOUR.get(3);

        GlStateManager.pushMatrix();
        try {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO
            );
            GlStateManager.disableTexture2D();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glLineWidth(2.0F);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            putPackedBoxes(buffer, current.getExcludedTiles(), TARGET_EPSILON, cameraX, cameraY, cameraZ, EXCLUDED_COLOUR);
            putPackedBoxes(buffer, current.getExcludedRandomBlocks(), TARGET_EPSILON, cameraX, cameraY, cameraZ, EXCLUDED_COLOUR);
            putPackedBoxes(buffer, current.getQualifiedTiles(), TARGET_EPSILON, cameraX, cameraY, cameraZ, QUALIFIED_COLOUR);
            putPackedBoxes(buffer, current.getQualifiedRandomBlocks(), TARGET_EPSILON, cameraX, cameraY, cameraZ, QUALIFIED_COLOUR);

            if (current.hasTerritory()) {
                putBox(
                        buffer,
                        current.getMinX() - cameraX - TERRITORY_EPSILON,
                        current.getMinY() - cameraY - TERRITORY_EPSILON,
                        current.getMinZ() - cameraZ - TERRITORY_EPSILON,
                        current.getMaxX() + 1.0D - cameraX
                                + TERRITORY_EPSILON,
                        current.getMaxY() + 1.0D - cameraY
                                + TERRITORY_EPSILON,
                        current.getMaxZ() + 1.0D - cameraZ
                                + TERRITORY_EPSILON,
                        TERRITORY_COLOUR
                );
            }

            tessellator.draw();
        } finally {
            GlStateManager.popMatrix();
            GlStateManager.tryBlendFuncSeparate(blendSourceRgb, blendDestinationRgb, blendSourceAlpha, blendDestinationAlpha);
            GlStateManager.depthMask(depthMask);
            setBlend(blendEnabled);
            setTexture(textureEnabled);
            setDepth(depthEnabled);
            GL11.glLineWidth(lineWidth);
            GlStateManager.color(red, green, blue, alpha);
        }
    }

    private static void requestSnapshot() {
        CareerTerritoryInfo current = selected;
        if (mode == Mode.ADVANCED_TEMPORAL && current != null) {
            CapnsBeeAddonNetwork.requestTemporalSnapshot(
                    current.getDimension(),
                    current.getSource().toLong()
            );
        }
    }

    private static boolean sameSource(CareerTerritoryInfo info) {
        return selected != null && selected.hasSameSource(info);
    }

    private static double interpolate(
            double previous,
            double current,
            float partialTicks
    ) {
        return previous + (current - previous) * partialTicks;
    }

    private static void putPackedBoxes(
            BufferBuilder buffer,
            long[] positions,
            double epsilon,
            double cameraX,
            double cameraY,
            double cameraZ,
            int colour
    ) {
        for (long position : positions) {
            putPackedBox(
                    buffer,
                    position,
                    epsilon,
                    cameraX,
                    cameraY,
                    cameraZ,
                    colour
            );
        }
    }

    private static void putPackedBox(
            BufferBuilder buffer,
            long position,
            double epsilon,
            double cameraX,
            double cameraY,
            double cameraZ,
            int colour
    ) {
        int x = (int) (position >> 38);
        int y = (int) ((position >> 26) & 0xFFFL);
        int z = (int) (position << 38 >> 38);
        if (y >= 0x800) {
            y -= 0x1000;
        }
        putBox(
                buffer,
                x - cameraX - epsilon,
                y - cameraY - epsilon,
                z - cameraZ - epsilon,
                x + 1.0D - cameraX + epsilon,
                y + 1.0D - cameraY + epsilon,
                z + 1.0D - cameraZ + epsilon,
                colour
        );
    }

    private static void putBox(
            BufferBuilder buffer,
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ,
            int colour
    ) {
        putLine(buffer, minX, minY, minZ, maxX, minY, minZ, colour);
        putLine(buffer, maxX, minY, minZ, maxX, minY, maxZ, colour);
        putLine(buffer, maxX, minY, maxZ, minX, minY, maxZ, colour);
        putLine(buffer, minX, minY, maxZ, minX, minY, minZ, colour);
        putLine(buffer, minX, maxY, minZ, maxX, maxY, minZ, colour);
        putLine(buffer, maxX, maxY, minZ, maxX, maxY, maxZ, colour);
        putLine(buffer, maxX, maxY, maxZ, minX, maxY, maxZ, colour);
        putLine(buffer, minX, maxY, maxZ, minX, maxY, minZ, colour);
        putLine(buffer, minX, minY, minZ, minX, maxY, minZ, colour);
        putLine(buffer, maxX, minY, minZ, maxX, maxY, minZ, colour);
        putLine(buffer, maxX, minY, maxZ, maxX, maxY, maxZ, colour);
        putLine(buffer, minX, minY, maxZ, minX, maxY, maxZ, colour);
    }

    private static void putLine(
            BufferBuilder buffer,
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2,
            int colour
    ) {
        float red = ((colour >> 16) & 0xFF) / 255.0F;
        float green = ((colour >> 8) & 0xFF) / 255.0F;
        float blue = (colour & 0xFF) / 255.0F;
        float alpha = ((colour >>> 24) & 0xFF) / 255.0F;
        buffer.pos(x1, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.pos(x2, y2, z2).color(red, green, blue, alpha).endVertex();
    }

    private static void setBlend(boolean enabled) {
        if (enabled) {
            GlStateManager.enableBlend();
        } else {
            GlStateManager.disableBlend();
        }
    }

    private static void setTexture(boolean enabled) {
        if (enabled) {
            GlStateManager.enableTexture2D();
        } else {
            GlStateManager.disableTexture2D();
        }
    }

    private static void setDepth(boolean enabled) {
        if (enabled) {
            GlStateManager.enableDepth();
        } else {
            GlStateManager.disableDepth();
        }
    }

    private static HudLine[] pendingHud() {
        return new HudLine[]{new HudLine(
                I18n.format("gui.capnsbeeaddon.temporal.pending"),
                0
        )};
    }

    private static HudLine[] buildHud(
            TemporalTerritorySnapshot current
    ) {
        List<HudLine> lines = new ArrayList<>(16);
        lines.add(new HudLine(I18n.format("gui.capnsbeeaddon.temporal.title", I18n.format(modeKey(current.getFocusMode()))), 0));
        lines.add(new HudLine(I18n.format(statusKey(current.getStatus())), 0));
        lines.add(new HudLine(I18n.format("gui.capnsbeeaddon.temporal.legend.territory"), TERRITORY_COLOUR));
        lines.add(new HudLine(I18n.format("gui.capnsbeeaddon.temporal.legend.qualified"), QUALIFIED_COLOUR));
        lines.add(new HudLine(I18n.format("gui.capnsbeeaddon.temporal.legend.excluded"), EXCLUDED_COLOUR));

        for (String error : current.getErrors()) {
            lines.add(new HudLine("  " + I18n.format(error), 0));
        }

        if (current.getStatus() == TemporalTerritorySnapshot.Status.ACTIVE) {
            addCount(lines, "gui.capnsbeeaddon.temporal.tiles.qualified", current.getQualifiedTileCount(), current.getQualifiedTiles().length);
            addCount(lines, "gui.capnsbeeaddon.temporal.tiles.excluded", current.getExcludedTileCount(), current.getExcludedTiles().length);
            addCount(lines, "gui.capnsbeeaddon.temporal.random.qualified", current.getQualifiedRandomCount(), current.getQualifiedRandomBlocks().length);
            addCount(lines, "gui.capnsbeeaddon.temporal.random.excluded", current.getExcludedRandomCount(), current.getExcludedRandomBlocks().length);
            lines.add(new HudLine(I18n.format("gui.capnsbeeaddon.temporal.chunks", current.getLoadedChunks(), current.getTotalChunks(), current.getTotalChunks() - current.getLoadedChunks()), 0));
            lines.add(new HudLine(I18n.format("gui.capnsbeeaddon.temporal.inspected", current.getInspectedTileEntries(), current.getInspectedBlockPositions()), 0));
            lines.add(new HudLine(I18n.format("gui.capnsbeeaddon.temporal.global", current.getGloballyRegisteredQualifiedTiles(), current.getQualifiedTileCount()), 0));
            if (current.isSourceRegisteredGlobally()) {
                lines.add(new HudLine(I18n.format("gui.capnsbeeaddon.temporal.source_registered"), 0));
            }
        }
        return lines.toArray(new HudLine[0]);
    }

    private static void addCount(List<HudLine> lines, String key, long total, int rendered) {
        lines.add(new HudLine(I18n.format(key, total, rendered, Math.max(0L, total - rendered)), 0));
    }

    private static String modeKey(TemporalFocusMode focusMode) {
        switch (focusMode) {
            case APIARY:
                return "gui.capnsbeeaddon.temporal.mode.apiary";
            case TILE_ENTITY:
                return "gui.capnsbeeaddon.temporal.mode.tile_entity";
            case GROWTH:
                return "gui.capnsbeeaddon.temporal.mode.growth";
            default:
                return "gui.capnsbeeaddon.temporal.mode.normal";
        }
    }

    private static String statusKey(TemporalTerritorySnapshot.Status status) {
        switch (status) {
            case NO_TEMPORAL_QUEEN:
                return "gui.capnsbeeaddon.temporal.status.no_queen";
            case NOT_TEMPORAL:
                return "gui.capnsbeeaddon.temporal.status.not_temporal";
            case INVALID_TEMPORAL_SPECIES:
                return "gui.capnsbeeaddon.temporal.status.invalid_species";
            case INACTIVE:
                return "gui.capnsbeeaddon.temporal.status.inactive";
            case ACTIVE:
                return "gui.capnsbeeaddon.temporal.status.active";
            default:
                return "gui.capnsbeeaddon.temporal.status.unavailable";
        }
    }

    private static final class HudLine {
        private final String text;
        private final int swatch;
        private HudLine(String text, int swatch) {this.text = text;this.swatch = swatch;}
    }

    private enum Mode {
        OFF,
        BASIC_TERRITORY,
        ADVANCED_TEMPORAL
    }
}