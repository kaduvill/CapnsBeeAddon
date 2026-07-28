package com.kaduvill.capnsbeeaddon.client.territory;

import forestry.api.apiculture.IBeeHousing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

/**
 * Holds only immutable coordinates and refreshes them from the live client
 * world once per second. No World, TileEntity, GUI, genome or ItemStack is
 * retained.
 */
public final class CareerTerritoryOverlay {

    private static final int REFRESH_INTERVAL_TICKS = 20;

    @Nullable
    private static CareerTerritoryInfo selected;

    private static int refreshTicks;

    private CareerTerritoryOverlay() {
    }

    public static void toggle(CareerTerritoryInfo info) {
        if (selected != null && selected.hasSameSource(info)) {
            clear();
        } else {
            selected = info;
            refreshTicks = 0;
        }
    }

    public static boolean isSelected(@Nullable CareerTerritoryInfo info) {
        return info != null
                && selected != null
                && selected.hasSameSource(info);
    }

    public static void clear() {
        selected = null;
        refreshTicks = 0;
    }

    public static void clientTick() {
        Minecraft minecraft = Minecraft.getMinecraft();
        World world = minecraft.world;
        CareerTerritoryInfo current = selected;

        if (current == null) {
            return;
        }

        if (world == null
                || world.provider.getDimension() != current.getDimension()) {
            clear();
            return;
        }

        if (++refreshTicks < REFRESH_INTERVAL_TICKS) {
            return;
        }
        refreshTicks = 0;

        BlockPos source = current.getSource();
        if (!world.isBlockLoaded(source, false)) {
            clear();
            return;
        }

        TileEntity tile = world.getTileEntity(source);
        if (!(tile instanceof IBeeHousing) || tile.isInvalid()) {
            clear();
            return;
        }

        CareerTerritoryInfo refreshed =
                CareerTerritoryResolver.resolve((IBeeHousing) tile);

        if (refreshed == null) {
            clear();
        } else {
            selected = refreshed;
        }
    }

    public static void render(float partialTicks) {
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

        double cameraX = camera.lastTickPosX
                + (camera.posX - camera.lastTickPosX) * partialTicks;
        double cameraY = camera.lastTickPosY
                + (camera.posY - camera.lastTickPosY) * partialTicks;
        double cameraZ = camera.lastTickPosZ
                + (camera.posZ - camera.lastTickPosZ) * partialTicks;

        AxisAlignedBB renderedBounds = current.getBounds()
                .grow(0.002D)
                .offset(-cameraX, -cameraY, -cameraZ);

        /*
         * Do not use raw glPushAttrib/glPopAttrib here. GlStateManager caches
         * OpenGL state in Java, and restoring it behind GlStateManager's back
         * can leave that cache inconsistent for later renderers.
         */
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

            // Faint complete box visible through blocks.
            GlStateManager.disableDepth();
            GL11.glLineWidth(1.0F);
            RenderGlobal.drawSelectionBoundingBox(
                    renderedBounds,
                    0.20F,
                    0.80F,
                    1.00F,
                    0.22F
            );

            // Bright depth-tested edges for spatial readability.
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
}
