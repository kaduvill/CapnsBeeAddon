package com.kaduvill.capnsbeeaddon.client.territory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.nio.FloatBuffer;

/**
 * Client-only local-player hitbox wireframe, enabled by Debug.PlayerHitboxDebugWireframe.
 */
public final class PlayerHitboxDebugOverlay {

    private static final FloatBuffer PREVIOUS_COLOUR = BufferUtils.createFloatBuffer(16);

    private PlayerHitboxDebugOverlay() {
    }

    public static void render(float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP player = minecraft.player;
        Entity camera = minecraft.getRenderViewEntity();

        if (minecraft.world == null || player == null || camera == null
                || player.world != minecraft.world || camera.world != minecraft.world) {
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
        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean textureEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
        boolean depthEnabled = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean depthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int blendSourceRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        int blendDestinationRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int blendSourceAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        int blendDestinationAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
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
            GlStateManager.popMatrix();
            GlStateManager.tryBlendFuncSeparate(blendSourceRgb, blendDestinationRgb,
                    blendSourceAlpha, blendDestinationAlpha);
            GlStateManager.depthMask(depthMask);
            if (blendEnabled) GlStateManager.enableBlend(); else GlStateManager.disableBlend();
            if (textureEnabled) GlStateManager.enableTexture2D(); else GlStateManager.disableTexture2D();
            if (depthEnabled) GlStateManager.enableDepth(); else GlStateManager.disableDepth();
            GL11.glLineWidth(lineWidth);
            GlStateManager.color(red, green, blue, alpha);
        }
    }
}
