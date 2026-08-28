package com.kaduvill.capnsbeeaddon.client.territory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

/**
 * Temporary debug overlay for the local player's entity bounding box.
 *
 * Disabled by commenting out its render call in
 * IndustrialApiaryTerritoryClientEvents.
 */
public final class PlayerHitboxDebugOverlay {

    private PlayerHitboxDebugOverlay() {
    }

    public static void render(float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP player = minecraft.player;
        Entity camera = minecraft.getRenderViewEntity();

        if (minecraft.world == null || player == null || camera == null) {
            return;
        }

        double cameraX = camera.lastTickPosX
                + (camera.posX - camera.lastTickPosX) * partialTicks;
        double cameraY = camera.lastTickPosY
                + (camera.posY - camera.lastTickPosY) * partialTicks;
        double cameraZ = camera.lastTickPosZ
                + (camera.posZ - camera.lastTickPosZ) * partialTicks;

        /*
         * Entity#getEntityBoundingBox contains the current tick position.
         * Move it to the player's interpolated render position so the box
         * remains visually aligned with the rendered player while moving.
         */
        double playerX = player.lastTickPosX
                + (player.posX - player.lastTickPosX) * partialTicks;
        double playerY = player.lastTickPosY
                + (player.posY - player.lastTickPosY) * partialTicks;
        double playerZ = player.lastTickPosZ
                + (player.posZ - player.lastTickPosZ) * partialTicks;

        AxisAlignedBB renderedBounds = player.getEntityBoundingBox()
                .offset(
                        playerX - player.posX,
                        playerY - player.posY,
                        playerZ - player.posZ
                )
                .offset(-cameraX, -cameraY, -cameraZ);

        /*
         * Keep GlStateManager's cached state synchronized. Do not restore
         * state through raw glPushAttrib/glPopAttrib.
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

            // Faint complete hitbox visible through blocks.
            GlStateManager.disableDepth();
            GL11.glLineWidth(1.0F);
            RenderGlobal.drawSelectionBoundingBox(
                    renderedBounds,
                    1.00F,
                    0.15F,
                    0.15F,
                    0.28F
            );

            // Bright depth-tested edges for spatial readability.
            GlStateManager.enableDepth();
            GL11.glLineWidth(2.0F);
            RenderGlobal.drawSelectionBoundingBox(
                    renderedBounds,
                    1.00F,
                    0.15F,
                    0.15F,
                    0.95F
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