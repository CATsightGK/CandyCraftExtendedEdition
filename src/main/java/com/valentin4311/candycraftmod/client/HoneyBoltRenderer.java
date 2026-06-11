package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.HoneyBoltEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class HoneyBoltRenderer extends ArrowRenderer<HoneyBoltEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/candybolt.png");

    public HoneyBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(HoneyBoltEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(1.15F, 1.15F, 1.15F);
        super.render(entity, yaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(HoneyBoltEntity entity) {
        return TEXTURE;
    }
}
