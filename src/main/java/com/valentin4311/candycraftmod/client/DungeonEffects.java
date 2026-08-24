package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Void-black sky for the dungeon instance dimensions. */
final class DungeonEffects extends DimensionSpecialEffects {
    DungeonEffects() {
        super(0.0F, false, DimensionSpecialEffects.SkyType.NONE, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 color, float brightness) {
        return Vec3.ZERO;
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }

    @Override
    public boolean renderClouds(ClientLevel level, int ticks, float partialTick, PoseStack poseStack,
            double camX, double camY, double camZ, Matrix4f projectionMatrix) {
        return true;
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, PoseStack poseStack,
            Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        setupFog.run();
        RenderSystem.depthMask(false);
        RenderSystem.disableBlend();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 0.0F, 1.0F);

        poseStack.pushPose();
        drawBlackSkyBox(poseStack.last().pose(), 128.0F);
        poseStack.popPose();

        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        return true;
    }

    private static void drawBlackSkyBox(Matrix4f matrix, float radius) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

        buffer.vertex(matrix, -radius, -radius, -radius).endVertex();
        buffer.vertex(matrix, radius, -radius, -radius).endVertex();
        buffer.vertex(matrix, radius, radius, -radius).endVertex();
        buffer.vertex(matrix, -radius, radius, -radius).endVertex();

        buffer.vertex(matrix, radius, -radius, radius).endVertex();
        buffer.vertex(matrix, -radius, -radius, radius).endVertex();
        buffer.vertex(matrix, -radius, radius, radius).endVertex();
        buffer.vertex(matrix, radius, radius, radius).endVertex();

        buffer.vertex(matrix, -radius, -radius, radius).endVertex();
        buffer.vertex(matrix, -radius, -radius, -radius).endVertex();
        buffer.vertex(matrix, -radius, radius, -radius).endVertex();
        buffer.vertex(matrix, -radius, radius, radius).endVertex();

        buffer.vertex(matrix, radius, -radius, -radius).endVertex();
        buffer.vertex(matrix, radius, -radius, radius).endVertex();
        buffer.vertex(matrix, radius, radius, radius).endVertex();
        buffer.vertex(matrix, radius, radius, -radius).endVertex();

        buffer.vertex(matrix, -radius, radius, -radius).endVertex();
        buffer.vertex(matrix, radius, radius, -radius).endVertex();
        buffer.vertex(matrix, radius, radius, radius).endVertex();
        buffer.vertex(matrix, -radius, radius, radius).endVertex();

        buffer.vertex(matrix, -radius, -radius, radius).endVertex();
        buffer.vertex(matrix, radius, -radius, radius).endVertex();
        buffer.vertex(matrix, radius, -radius, -radius).endVertex();
        buffer.vertex(matrix, -radius, -radius, -radius).endVertex();
        tesselator.end();
    }
}
