package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * DimensionSpecialEffects for the candy world (legacy pastel sky, milk rain)
 * and the dungeon instances (void-black sky).
 */
final class CandyWorldEffects extends DimensionSpecialEffects {
    CandyWorldEffects() {
        super(192.0F, true, DimensionSpecialEffects.SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float brightness) {
        Vec3 fallback = CandyColors.rgbVec(CandyColors.CANDY_WORLD_FOG_FALLBACK);
        return new Vec3(
            color.x * 0.94D + fallback.x * 0.06D,
            color.y * 0.94D + fallback.y * 0.06D,
            color.z * 0.94D + fallback.z * 0.06D
        );
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        return false;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        net.minecraft.core.BlockPos pos = camera.getBlockPosition();
        if (level.hasChunkAt(pos)) {
            return false;
        }

        setupFog.run();
        float day = candySkyDayFactor(level, partialTick);
        float night = 1.0F - day;
        Vec3 sky = CandyColors.rgbVec(CandyColors.lerpColor(0x321326, CandyColors.CANDY_WORLD_SKY_FALLBACK, day));
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor((float)sky.x, (float)sky.y, (float)sky.z, 1.0F);

        poseStack.pushPose();
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        Matrix4f matrix = poseStack.last().pose();
        drawSkyQuad(matrix, 128.0F);
        if (night > 0.25F) {
            drawCandyStars(matrix, Mth.clamp((night - 0.25F) / 0.75F, 0.0F, 1.0F));
        }
        poseStack.popPose();

        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        return true;
    }

    @Override
    public boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick,
            net.minecraft.client.renderer.LightTexture lightTexture, double cameraX, double cameraY, double cameraZ) {
        MilkRainRenderer.render(level, ticks, partialTick, lightTexture, cameraX, cameraY, cameraZ);
        return true;
    }

    @Override
    public boolean tickRain(ClientLevel level, int ticks, Camera camera) {
        MilkRainRenderer.tick(level, ticks, camera);
        return true;
    }

    private static void drawSkyQuad(Matrix4f matrix, float radius) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        buffer.vertex(matrix, -radius, -radius, -radius).endVertex();
        buffer.vertex(matrix, -radius, -radius, radius).endVertex();
        buffer.vertex(matrix, radius, -radius, radius).endVertex();
        buffer.vertex(matrix, radius, -radius, -radius).endVertex();
        tesselator.end();
    }

    private static void drawCandyStars(Matrix4f matrix, float alpha) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < 360; i++) {
            int hash = i * 1103515245 + 12345;
            float x = (((hash >>> 8) & 1023) / 1023.0F - 0.5F) * 240.0F;
            float z = (((hash >>> 20) & 1023) / 1023.0F - 0.5F) * 240.0F;
            if (x * x + z * z < 900.0F) {
                continue;
            }
            float y = -122.0F + ((hash >>> 4) & 15) * 0.12F;
            float size = 0.22F + ((hash >>> 16) & 3) * 0.08F;
            int brightness = 210 + ((hash >>> 12) & 45);
            float red = brightness / 255.0F;
            float green = (brightness * 0.86F) / 255.0F;
            float blue = (brightness * 0.96F) / 255.0F;
            buffer.vertex(matrix, x - size, y, z - size).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrix, x - size, y, z + size).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrix, x + size, y, z + size).color(red, green, blue, alpha).endVertex();
            buffer.vertex(matrix, x + size, y, z - size).color(red, green, blue, alpha).endVertex();
        }
        tesselator.end();
    }

    private static float candySkyDayFactor(Level level, float partialTick) {
        float value = (float)Math.cos(level.getTimeOfDay(partialTick) * ((float)Math.PI * 2.0F)) * 2.0F + 0.5F;
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}
