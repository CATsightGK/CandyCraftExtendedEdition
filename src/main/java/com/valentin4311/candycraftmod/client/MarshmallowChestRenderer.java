package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.FacingModelBlock;
import com.valentin4311.candycraftmod.block.entity.MarshmallowChestBlockEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MarshmallowChestRenderer implements BlockEntityRenderer<MarshmallowChestBlockEntity> {
    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;

    public MarshmallowChestRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(ModelLayers.CHEST);
        lid = root.getChild("lid");
        bottom = root.getChild("bottom");
        lock = root.getChild("lock");
    }

    @Override
    public void render(MarshmallowChestBlockEntity chest, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Direction direction = chest.getBlockState().getValue(FacingModelBlock.FACING);
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(-direction.toYRot()));
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        float openness = 1.0F - chest.getOpenNess(partialTick);
        openness = 1.0F - openness * openness * openness;
        lid.xRot = -(openness * Mth.HALF_PI);
        lock.xRot = lid.xRot;

        ResourceLocation texture = new ResourceLocation(CandyCraft.MODID,
            "textures/entity/chest/" + chest.theme().textureName() + ".png");
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(texture));
        bottom.render(poseStack, consumer, packedLight, packedOverlay);
        lid.render(poseStack, consumer, packedLight, packedOverlay);
        lock.render(poseStack, consumer, packedLight, packedOverlay);
        poseStack.popPose();
    }
}
