package com.valentin4311.candycraftmod.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.client.GingerbreadManModel;
import com.valentin4311.candycraftmod.entity.GingerbreadManEntity;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.TieredItem;

public class GingerbreadHeldItemLayer extends RenderLayer<GingerbreadManEntity, GingerbreadManModel<GingerbreadManEntity>> {
    private final ItemInHandRenderer itemInHandRenderer;

    public GingerbreadHeldItemLayer(RenderLayerParent<GingerbreadManEntity, GingerbreadManModel<GingerbreadManEntity>> parent,
            ItemInHandRenderer itemInHandRenderer) {
        super(parent);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, GingerbreadManEntity entity,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack stack = entity.getMainHandItem();
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        getParentModel().rightArm.translateAndRotate(poseStack);
        poseStack.translate(-0.06F, 0.62F, 0.05F);
        if (stack.getItem() instanceof BowItem || stack.getItem() instanceof CrossbowItem) {
            poseStack.translate(0.0333F, 0.0221F, -0.0395F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-85.84F));
            poseStack.mulPose(Axis.YP.rotationDegrees(-9.16F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(179.46F));
            poseStack.scale(0.625F, 0.625F, 0.625F);
        } else if (isToolOrWeapon(stack)) {
            poseStack.translate(0.0F, -0.3468F, 0.204F);
            poseStack.mulPose(Axis.XP.rotationDegrees(10.86F));
            poseStack.scale(1.0F, 1.0F, 1.0F);
        } else {
            poseStack.translate(-0.2101F, -0.0478F, 0.1159F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-85.11F));
            poseStack.mulPose(Axis.YP.rotationDegrees(-1.98F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-88.03F));
            poseStack.scale(0.825F, 0.825F, 0.825F);
        }
        itemInHandRenderer.renderItem(entity, stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false,
            poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private static boolean isToolOrWeapon(ItemStack stack) {
        return stack.getItem() instanceof TieredItem
            || stack.is(CCItems.JUMP_WAND.get())
            || stack.is(CCItems.JELLY_WAND.get());
    }
}
