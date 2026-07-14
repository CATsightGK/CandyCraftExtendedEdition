package com.valentin4311.candycraftmod.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.client.model.MermaidModel;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MermaidHeldItemLayer extends RenderLayer<BasicCandyZombieEntity, MermaidModel<BasicCandyZombieEntity>> {
    private final ItemInHandRenderer itemInHandRenderer;

    public MermaidHeldItemLayer(RenderLayerParent<BasicCandyZombieEntity, MermaidModel<BasicCandyZombieEntity>> parent,
            ItemInHandRenderer itemInHandRenderer) {
        super(parent);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, BasicCandyZombieEntity entity,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack stack = entity.getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        getParentModel().translateToRightArm(poseStack);
        poseStack.translate(-0.015F, 0.175F, 0.115F);
        if (stack.is(CCItems.CARAMEL_BOW.get())) {
            poseStack.translate(-0.055F, 0.07F, -0.13F);
            poseStack.scale(0.625F, 0.625F, 0.625F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(8.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(-7.0F));
        } else {
            poseStack.translate(0.0F, 0.15F, 0.02F);
            poseStack.scale(0.825F, 0.825F, 0.825F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-12.0F));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        itemInHandRenderer.renderItem(entity, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false,
            poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
