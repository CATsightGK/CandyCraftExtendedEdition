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
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;

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
        translateToLegacySuguardHand(poseStack, stack, entity, partialTicks);

        itemInHandRenderer.renderItem(entity, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false,
            poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private void translateToLegacySuguardHand(PoseStack poseStack, ItemStack stack, BasicCandyZombieEntity entity, float partialTicks) {
        getParentModel().translateToRightArm(poseStack);
        poseStack.translate(0.0275F, 0.1225F, 0.1425F);
        if (stack.is(CCItems.CARAMEL_BOW.get())) {
            float draw = entity.getBossBowDrawProgress(partialTicks);
            poseStack.translate(-0.075F - 0.028F * draw, 0.105F - 0.026F * draw, -0.145F - 0.045F * draw);
            poseStack.scale(0.625F, 0.625F, 0.625F);
            poseStack.mulPose(Axis.ZP.rotationDegrees(7.0F + 10.0F * draw));
            poseStack.mulPose(Axis.YP.rotationDegrees(-5.0F - 7.0F * draw));
        } else if (stack.is(CCItems.JUMP_WAND.get())) {
            applyLegacyToolTransform(poseStack, 0.0F, 0.1575F, 0.1F, -12.0F);
        } else if (stack.is(CCItems.DYNAMITE.get()) || stack.is(CCItems.GLUE_DYNAMITE.get())) {
            poseStack.translate(0.1F, -0.125F, -0.075F);
            poseStack.scale(0.825F, 0.825F, 0.825F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-44.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(93.0F));
        } else if (isLegacyBladeLike(stack)) {
            applyLegacyToolTransform(poseStack, 0.0F, 0.1875F, 0.0F, -12.0F);
        } else {
            applyLegacyToolTransform(poseStack, 0.0F, 0.1875F, 0.0F, -12.0F);
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
    }

    private static void applyLegacyToolTransform(PoseStack poseStack, float x, float y, float z, float xRotation) {
        poseStack.translate(x, y, z);
        poseStack.scale(0.825F, 0.825F, 0.825F);
        poseStack.mulPose(Axis.XP.rotationDegrees(xRotation));
    }

    private static boolean isLegacyBladeLike(ItemStack stack) {
        return stack.getItem() instanceof SwordItem
            || stack.getItem() instanceof TieredItem
            || stack.is(CCItems.LICORICE_SPEAR.get());
    }
}
