package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.entity.ThrownForkBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class ThrownForkBlockRenderer extends EntityRenderer<ThrownForkBlockEntity> {
    public ThrownForkBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.18F;
    }

    @Override
    public void render(ThrownForkBlockEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        BlockState state = entity.getBlockState();
        if (state.isAir()) {
            return;
        }

        poseStack.pushPose();
        float spin = (entity.tickCount + partialTick) * 14.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        poseStack.mulPose(Axis.XP.rotationDegrees(spin * 0.65F));
        if (state.getBlock() instanceof DoorBlock) {
            renderDoor(state, poseStack, buffer, packedLight);
        } else {
            poseStack.scale(0.65F, 0.65F, 0.65F);
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            net.minecraft.client.Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                state, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY
            );
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void renderDoor(BlockState state, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        BlockState lower = state.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upper = state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        poseStack.scale(0.42F, 0.42F, 0.42F);
        poseStack.translate(-0.5F, -1.0F, -0.5F);
        net.minecraft.client.Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
            lower, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY
        );
        poseStack.translate(0.0F, 1.0F, 0.0F);
        net.minecraft.client.Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
            upper, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY
        );
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownForkBlockEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
