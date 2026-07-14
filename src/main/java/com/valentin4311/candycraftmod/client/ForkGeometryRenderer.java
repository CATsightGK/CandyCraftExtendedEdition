package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Renders the full-size fork without passing its out-of-range cubes through ModelBakery. */
public final class ForkGeometryRenderer {
    public static final ResourceLocation TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/item/fork_3d.png");
    private static final float TEXTURE_SIZE = 64.0F;
    private static final float CENTER_X = 3.0F;
    private static final float CENTER_Y = 13.875F;

    private static final Cuboid[] CUBOIDS = {
        cuboid(0.5F, 23.5F, -1.25F, 5.5F, 27.5F, 1.5F,
            uv(18, 7, 23, 11), uv(24, 25, 27, 29), uv(18, 11, 23, 15),
            uv(14, 26, 17, 30), uv(19, 26, 14, 23), uv(24, 23, 19, 26)),
        cuboid(1.5F, -14.5F, -1.0F, 4.5F, 5.5F, 1.0F,
            uv(4, 16, 7, 36), uv(10, 16, 12, 36), uv(7, 16, 10, 36),
            uv(12, 16, 14, 36), uv(23, 31, 20, 29), uv(32, 20, 29, 22)),
        cuboid(1.0F, 5.5F, -1.5F, 5.0F, 9.5F, 1.5F,
            uv(23, 7, 27, 11), uv(26, 15, 29, 19), uv(23, 11, 27, 15),
            uv(17, 26, 20, 30), uv(24, 29, 20, 26), uv(31, 7, 27, 10)),
        cuboid(1.5F, 9.5F, -1.0F, 4.5F, 23.5F, 1.0F,
            uv(7, 19, 10, 33), uv(5, 2, 7, 16), uv(7, 2, 10, 16),
            uv(5, 2, 7, 16), uv(5, 2, 2, 0), uv(8, 0, 5, 2)),
        cuboid(0.5F, -16.0F, -1.5F, 5.5F, -12.0F, 1.5F,
            uv(14, 19, 19, 23), uv(27, 10, 30, 14), uv(19, 19, 24, 23),
            uv(27, 25, 30, 29), uv(29, 22, 24, 19), uv(29, 22, 24, 25)),
        cuboid(-2.75F, 25.75F, -0.75F, 8.75F, 28.25F, 0.75F,
            uv(14, 16, 26, 19), uv(29, 14, 31, 17), uv(18, 0, 30, 3),
            uv(29, 17, 31, 20), uv(30, 5, 18, 3), uv(30, 5, 18, 7)),
        cuboid(-2.75F, 28.25F, -0.75F, -1.25F, 43.75F, 0.75F,
            uv(11, 1, 12, 16), uv(10, 1, 11, 16), uv(13, 1, 14, 16),
            uv(12, 1, 13, 16), uv(12, 1, 11, 0), uv(13, 0, 12, 1)),
        cuboid(2.25F, 28.25F, -0.75F, 3.75F, 43.75F, 0.75F,
            uv(15, 1, 16, 16), uv(14, 1, 15, 16), uv(17, 1, 18, 16),
            uv(16, 1, 17, 16), uv(16, 1, 15, 0), uv(17, 0, 16, 1)),
        cuboid(7.25F, 28.25F, -0.75F, 8.75F, 43.75F, 0.75F,
            uv(1, 17, 2, 32), uv(0, 17, 1, 32), uv(3, 17, 4, 32),
            uv(2, 17, 3, 32), uv(2, 17, 1, 16), uv(3, 16, 2, 17))
    };

    private ForkGeometryRenderer() {
    }

    public static void renderCentered(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        render(poseStack, buffer, packedLight, true);
    }

    public static void renderRaw(PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        render(poseStack, buffer, packedLight, false);
    }

    private static void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, boolean centered) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        for (Cuboid cuboid : CUBOIDS) {
            renderCuboid(pose, consumer, packedLight, cuboid, centered);
        }
    }

    private static void renderCuboid(PoseStack.Pose pose, VertexConsumer consumer, int light, Cuboid cube, boolean centered) {
        float offsetX = centered ? CENTER_X : 0.0F;
        float offsetY = centered ? CENTER_Y : 0.0F;
        float x1 = (cube.x1 - offsetX) / 16.0F;
        float y1 = (cube.y1 - offsetY) / 16.0F;
        float z1 = cube.z1 / 16.0F;
        float x2 = (cube.x2 - offsetX) / 16.0F;
        float y2 = (cube.y2 - offsetY) / 16.0F;
        float z2 = cube.z2 / 16.0F;

        quad(pose, consumer, light, cube.north, 0, 0, -1,
            x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1);
        quad(pose, consumer, light, cube.east, 1, 0, 0,
            x2, y1, z2, x2, y1, z1, x2, y2, z1, x2, y2, z2);
        quad(pose, consumer, light, cube.south, 0, 0, 1,
            x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2);
        quad(pose, consumer, light, cube.west, -1, 0, 0,
            x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1);
        quad(pose, consumer, light, cube.up, 0, 1, 0,
            x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1);
        quad(pose, consumer, light, cube.down, 0, -1, 0,
            x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer consumer, int light, Uv uv,
            float normalX, float normalY, float normalZ, float... vertices) {
        float u1 = uv.u1 / TEXTURE_SIZE;
        float v1 = uv.v1 / TEXTURE_SIZE;
        float u2 = uv.u2 / TEXTURE_SIZE;
        float v2 = uv.v2 / TEXTURE_SIZE;
        vertex(pose, consumer, light, vertices[0], vertices[1], vertices[2], u1, v2, normalX, normalY, normalZ);
        vertex(pose, consumer, light, vertices[3], vertices[4], vertices[5], u2, v2, normalX, normalY, normalZ);
        vertex(pose, consumer, light, vertices[6], vertices[7], vertices[8], u2, v1, normalX, normalY, normalZ);
        vertex(pose, consumer, light, vertices[9], vertices[10], vertices[11], u1, v1, normalX, normalY, normalZ);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, int light,
            float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ) {
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        consumer.vertex(matrix, x, y, z)
            .color(255, 255, 255, 255)
            .uv(u, v)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(light)
            .normal(normal, normalX, normalY, normalZ)
            .endVertex();
    }

    private static Uv uv(float u1, float v1, float u2, float v2) {
        return new Uv(u1, v1, u2, v2);
    }

    private static Cuboid cuboid(float x1, float y1, float z1, float x2, float y2, float z2,
            Uv north, Uv east, Uv south, Uv west, Uv up, Uv down) {
        return new Cuboid(x1, y1, z1, x2, y2, z2, north, east, south, west, up, down);
    }

    private record Uv(float u1, float v1, float u2, float v2) {
    }

    private record Cuboid(float x1, float y1, float z1, float x2, float y2, float z2,
            Uv north, Uv east, Uv south, Uv west, Uv up, Uv down) {
    }
}
