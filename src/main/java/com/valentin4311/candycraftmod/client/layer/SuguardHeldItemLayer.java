package com.valentin4311.candycraftmod.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.client.model.SuguardModel;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
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
        getParentModel().translateToRightArm(poseStack);
        poseStack.translate(0.0275F, 0.1225F, 0.1425F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(0.0625F, 0.125F, -0.625F);

        itemInHandRenderer.renderItem(entity, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false,
            poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}
