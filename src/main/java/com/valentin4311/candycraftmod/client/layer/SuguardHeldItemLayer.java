package com.valentin4311.candycraftmod.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.client.model.SuguardModel;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
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

        ItemDisplayContext displayContext = ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        if (isBossSuguardCaramelBow(entity, stack)) {
            applyEditorCaramelBowModelTransform(poseStack);
            displayContext = ItemDisplayContext.NONE;
        }
        itemInHandRenderer.renderItem(entity, stack, displayContext, false,
            poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private void translateToLegacySuguardHand(PoseStack poseStack, ItemStack stack, BasicCandyZombieEntity entity, float partialTicks) {
        getParentModel().translateToRightArm(poseStack);
        poseStack.translate(0.0275F, 0.1225F, 0.1425F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        if (isBossSuguardCaramelBow(entity, stack)) {
            applyBossSuguardBowTransform(poseStack);
        } else if (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem) {
            poseStack.translate(-0.0385F, 0.1329F, 0.1183F);
            poseStack.mulPose(Axis.XP.rotationDegrees(21.6F));
            poseStack.mulPose(Axis.YP.rotationDegrees(12.16F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(0.44F));
            poseStack.scale(0.625F, 0.625F, 0.625F);
        } else if (isToolOrWeapon(stack)) {
            applyLegacyToolTransform(poseStack, -0.0038F, 0.0331F, 0.1978F, -105.86F, 0.0F, 180.0F);
        } else {
            poseStack.translate(-0.0822F, 0.1672F, -0.0052F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-86.33F));
            poseStack.mulPose(Axis.YP.rotationDegrees(5.08F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(177.81F));
            poseStack.scale(0.825F, 0.825F, 0.825F);
        }
    }

    private static void applyBossSuguardBowTransform(PoseStack poseStack) {
        poseStack.translate(-0.0385F, 0.1329F, 0.1183F);
        poseStack.mulPose(Axis.XP.rotationDegrees(21.6F));
        poseStack.mulPose(Axis.YP.rotationDegrees(12.16F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(0.44F));
        poseStack.scale(0.625F, 0.625F, 0.625F);
    }

    private static boolean isBossSuguardCaramelBow(BasicCandyZombieEntity entity, ItemStack stack) {
        return entity.getType() == CCEntityTypes.BOSS_SUGUARD.get() && stack.is(CCItems.CARAMEL_BOW.get());
    }

    private static void applyEditorCaramelBowModelTransform(PoseStack poseStack) {
        poseStack.translate(0.75F / 16.0F, 0.0F, 0.25F / 16.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(5.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(80.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-45.0F));
    }

    private static boolean isToolOrWeapon(ItemStack stack) {
        return stack.getItem() instanceof TieredItem
            || stack.is(CCItems.JUMP_WAND.get())
            || stack.is(CCItems.JELLY_WAND.get());
    }

    private static void applyLegacyToolTransform(PoseStack poseStack, float x, float y, float z,
            float xRotation, float yRotation, float zRotation) {
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.XP.rotationDegrees(xRotation));
        poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
        poseStack.mulPose(Axis.ZP.rotationDegrees(zRotation));
        poseStack.scale(0.825F, 0.825F, 0.825F);
    }
}
