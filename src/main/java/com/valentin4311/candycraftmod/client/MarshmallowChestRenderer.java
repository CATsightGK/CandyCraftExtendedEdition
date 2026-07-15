package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.FacingModelBlock;
import com.valentin4311.candycraftmod.block.MarshmallowChestBlock;
import com.valentin4311.candycraftmod.block.entity.MarshmallowChestBlockEntity;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.ChestType;

public class MarshmallowChestRenderer implements BlockEntityRenderer<MarshmallowChestBlockEntity> {
    private static final Box SINGLE_BODY = box(1, 0, 2, 15, 10, 16, 16,
        uv(0, 7, 3.5F, 9.5F), uv(7, 0, 10.5F, 2.5F), uv(7, 2.5F, 10.5F, 5),
        uv(3.5F, 7, 7, 9.5F), uv(3.5F, 3.5F, 0, 0), uv(3.5F, 3.5F, 0, 7));
    private static final Box SINGLE_LID = box(1, 10, 2, 15, 15, 16, 16,
        uv(7, 5, 10.5F, 6.25F), uv(7, 6.25F, 10.5F, 7.5F), uv(7, 7.5F, 10.5F, 8.75F),
        uv(7, 8.75F, 10.5F, 10), uv(7, 3.5F, 3.5F, 0), uv(7, 3.5F, 3.5F, 7));
    private static final Box SINGLE_LOCK = box(7, 8, 1, 9, 12, 2, 16,
        uv(0, 9.5F, 0.5F, 10.5F), uv(1, 9.5F, 1.25F, 10.5F), uv(0.5F, 9.5F, 1, 10.5F),
        uv(1.25F, 9.5F, 1.5F, 10.5F), uv(2, 9.75F, 1.5F, 9.5F), uv(2.5F, 9.5F, 2, 9.75F));

    private static final Box DOUBLE_BODY_VISUAL_LEFT = box(1, 0, 1, 16, 10, 15, 64,
        uv(15, 42, 30, 52), uv(45, 0, 59, 10), uv(0, 42, 15, 52),
        uv(45, 10, 59, 20), uv(30, 14, 15, 0), uv(45, 14, 30, 28));
    private static final Box DOUBLE_LID_VISUAL_LEFT = box(1, 10, 1, 16, 15, 15, 64,
        uv(45, 40, 60, 45), uv(0, 52, 14, 57), uv(45, 45, 60, 50),
        uv(14, 52, 28, 57), uv(45, 28, 30, 14), uv(30, 28, 15, 42));
    private static final Box DOUBLE_LOCK_VISUAL_LEFT = box(15, 8, 0, 16, 12, 1, 64,
        uv(56, 55, 57, 59), uv(0, 57, 1, 61), uv(1, 57, 2, 61),
        uv(2, 57, 3, 61), uv(29, 53, 28, 52), uv(30, 52, 29, 53));

    // The right visual half is authored in the BBModel's 16..31 range. Subtracting 16
    // places it in its own block while keeping the shared hinge exactly on the seam.
    private static final Box DOUBLE_BODY_VISUAL_RIGHT = box(0, 0, 1, 15, 10, 15, 64,
        uv(0, 42, 15, 52), uv(45, 20, 59, 30), uv(15, 42, 30, 52),
        uv(45, 30, 59, 40), uv(15, 14, 0, 0), uv(45, 0, 30, 14));
    private static final Box DOUBLE_LID_VISUAL_RIGHT = box(0, 10, 1, 15, 15, 15, 64,
        uv(30, 48, 45, 53), uv(28, 53, 42, 58), uv(45, 50, 60, 55),
        uv(42, 55, 56, 60), uv(45, 14, 30, 0), uv(45, 14, 30, 28));
    private static final Box DOUBLE_LOCK_VISUAL_RIGHT = box(0, 8, 0, 1, 12, 1, 64,
        uv(3, 57, 4, 61), uv(4, 57, 5, 61), uv(5, 57, 6, 61),
        uv(6, 57, 7, 61), uv(43, 54, 42, 53), uv(44, 53, 43, 54));

    public MarshmallowChestRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MarshmallowChestBlockEntity chest, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Direction facing = chest.getBlockState().getValue(FacingModelBlock.FACING);
        ChestType type = chest.getBlockState().getValue(MarshmallowChestBlock.TYPE);
        float openness = combinedOpenness(chest, type, partialTick);
        openness = 1.0F - (float) Math.pow(1.0F - openness, 3.0D);

        String textureName = chest.theme().textureName()
            + (type == ChestType.SINGLE ? "_custom_single.png" : "_custom_double.png");
        ResourceLocation texture = new ResourceLocation(CandyCraft.MODID, "textures/entity/chest/" + textureName);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        if (type == ChestType.SINGLE) {
            renderBox(poseStack, consumer, SINGLE_BODY, null, true, packedLight, packedOverlay);
            renderAnimatedGroup(poseStack, consumer, SINGLE_LID, SINGLE_LOCK,
                8, 10, 16, null, openness, packedLight, packedOverlay);
        } else if (type == ChestType.RIGHT) {
            renderBox(poseStack, consumer, DOUBLE_BODY_VISUAL_LEFT, Direction.EAST, true, packedLight, packedOverlay);
            renderAnimatedGroup(poseStack, consumer, DOUBLE_LID_VISUAL_LEFT, DOUBLE_LOCK_VISUAL_LEFT,
                16, 10, 15, Direction.EAST, openness, packedLight, packedOverlay);
        } else {
            renderBox(poseStack, consumer, DOUBLE_BODY_VISUAL_RIGHT, Direction.WEST, true, packedLight, packedOverlay);
            renderAnimatedGroup(poseStack, consumer, DOUBLE_LID_VISUAL_RIGHT, DOUBLE_LOCK_VISUAL_RIGHT,
                0, 10, 15, Direction.WEST, openness, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    private static void renderAnimatedGroup(PoseStack poseStack, VertexConsumer consumer, Box lid, Box lock,
            float pivotX, float pivotY, float pivotZ, Direction seam, float openness,
            int packedLight, int packedOverlay) {
        poseStack.pushPose();
        poseStack.translate(pivotX / 16.0F, pivotY / 16.0F, pivotZ / 16.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(openness * 90.0F));
        poseStack.translate(-pivotX / 16.0F, -pivotY / 16.0F, -pivotZ / 16.0F);

        // At rest the lid bottom and body top occupy the same plane. Omitting only that
        // hidden face while fully closed removes the flashing without shifting the model.
        renderBox(poseStack, consumer, lid, seam, openness > 0.001F, packedLight, packedOverlay);
        renderBox(poseStack, consumer, lock, seam, true, packedLight, packedOverlay);
        poseStack.popPose();
    }

    private static void renderBox(PoseStack poseStack, VertexConsumer consumer, Box box,
            Direction skippedFace, boolean renderDown, int packedLight, int packedOverlay) {
        for (Direction face : Direction.values()) {
            if (face == skippedFace || (face == Direction.DOWN && !renderDown)) {
                continue;
            }
            renderFace(poseStack.last(), consumer, box, face, packedLight, packedOverlay);
        }
    }

    private static void renderFace(PoseStack.Pose pose, VertexConsumer consumer, Box box, Direction face,
            int packedLight, int packedOverlay) {
        float x0 = box.x0 / 16.0F;
        float y0 = box.y0 / 16.0F;
        float z0 = box.z0 / 16.0F;
        float x1 = box.x1 / 16.0F;
        float y1 = box.y1 / 16.0F;
        float z1 = box.z1 / 16.0F;
        float[][] vertices = switch (face) {
            case DOWN -> new float[][]{{x0, y0, z1}, {x0, y0, z0}, {x1, y0, z0}, {x1, y0, z1}};
            case UP -> new float[][]{{x0, y1, z0}, {x0, y1, z1}, {x1, y1, z1}, {x1, y1, z0}};
            case NORTH -> new float[][]{{x1, y1, z0}, {x1, y0, z0}, {x0, y0, z0}, {x0, y1, z0}};
            case SOUTH -> new float[][]{{x0, y1, z1}, {x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}};
            case WEST -> new float[][]{{x0, y1, z0}, {x0, y0, z0}, {x0, y0, z1}, {x0, y1, z1}};
            case EAST -> new float[][]{{x1, y1, z1}, {x1, y0, z1}, {x1, y0, z0}, {x1, y1, z0}};
        };
        Uv uv = box.uvs.get(face);
        float[][] texture = {
            {uv.u0 / box.textureSize, uv.v0 / box.textureSize},
            {uv.u0 / box.textureSize, uv.v1 / box.textureSize},
            {uv.u1 / box.textureSize, uv.v1 / box.textureSize},
            {uv.u1 / box.textureSize, uv.v0 / box.textureSize},
        };
        for (int index = 0; index < 4; index++) {
            consumer.vertex(pose.pose(), vertices[index][0], vertices[index][1], vertices[index][2])
                .color(255, 255, 255, 255)
                .uv(texture[index][0], texture[index][1])
                .overlayCoords(packedOverlay)
                .uv2(packedLight)
                .normal(pose.normal(), face.getStepX(), face.getStepY(), face.getStepZ())
                .endVertex();
        }
    }

    private static float combinedOpenness(MarshmallowChestBlockEntity chest, ChestType type, float partialTick) {
        float openness = chest.getOpenNess(partialTick);
        if (type == ChestType.SINGLE || chest.getLevel() == null) {
            return openness;
        }
        Direction connection = MarshmallowChestBlock.connectedDirection(chest.getBlockState());
        if (chest.getLevel().getBlockEntity(chest.getBlockPos().relative(connection))
                instanceof MarshmallowChestBlockEntity other) {
            openness = Math.max(openness, other.getOpenNess(partialTick));
        }
        return openness;
    }

    private static Uv uv(float u0, float v0, float u1, float v1) {
        return new Uv(u0, v0, u1, v1);
    }

    private static Box box(float x0, float y0, float z0, float x1, float y1, float z1, float textureSize,
            Uv north, Uv east, Uv south, Uv west, Uv up, Uv down) {
        Map<Direction, Uv> uvs = new EnumMap<>(Direction.class);
        uvs.put(Direction.NORTH, north);
        uvs.put(Direction.EAST, east);
        uvs.put(Direction.SOUTH, south);
        uvs.put(Direction.WEST, west);
        uvs.put(Direction.UP, up);
        uvs.put(Direction.DOWN, down);
        return new Box(x0, y0, z0, x1, y1, z1, textureSize, uvs);
    }

    private record Uv(float u0, float v0, float u1, float v1) {
    }

    private record Box(float x0, float y0, float z0, float x1, float y1, float z1,
            float textureSize, Map<Direction, Uv> uvs) {
    }
}
