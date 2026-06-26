package com.valentin4311.candycraftmod.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.client.model.SuguardModel;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class SuguardHeldItemLayer extends RenderLayer<BasicCandyZombieEntity, SuguardModel<BasicCandyZombieEntity>> {
    private final ItemInHandRenderer itemInHandRenderer;

    public SuguardHeldItemLayer(RenderLayerParent<BasicCandyZombieEntity, SuguardModel<BasicCandyZombieEntity>> parent,
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
        translateToLegacySuguardHand(poseStack);
        applyLegacyItemTransform(poseStack, stack);

        itemInHandRenderer.renderItem(entity, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false,
            poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private void translateToLegacySuguardHand(PoseStack poseStack) {
        getParentModel().translateToRightArm(poseStack);
        poseStack.translate(0.0275F, 0.1225F, 0.1425F);
    }

    private static void applyLegacyItemTransform(PoseStack poseStack, ItemStack stack) {
        if (stack.is(CCItems.CARAMEL_BOW.get())) {
            poseStack.translate(-0.05F, 0.12F, -0.12F);
            poseStack.scale(0.625F, 0.625F, 0.625F);
        } else if (stack.is(CCItems.LICORICE_SPEAR.get())) {
            poseStack.translate(0.0F, 0.1875F, 0.0F);
            poseStack.scale(0.825F, -0.825F, 0.825F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-12.0F));
        } else if (stack.is(CCItems.JUMP_WAND.get())) {
            poseStack.translate(0.0F, 0.1575F, 0.10F);
            poseStack.scale(0.825F, -0.825F, 0.825F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-12.0F));
        } else if (stack.is(CCItems.DYNAMITE.get())) {
            poseStack.translate(0.1F, -0.125F, -0.075F);
            poseStack.scale(0.825F, -0.825F, 0.825F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-44.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(93.0F));
        }
    }
}
