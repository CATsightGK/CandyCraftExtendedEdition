package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.entity.HoneyBoltEntity;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class HoneyBoltRenderer extends EntityRenderer<HoneyBoltEntity> {
    private static final float EXPOSED_LENGTH_OFFSET = -0.25F;
    private final ItemRenderer itemRenderer;
    private final ItemStack boltStack = new ItemStack(CCItems.HONEY_BOLT.get());

    public HoneyBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(HoneyBoltEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        poseStack.scale(1.35F, 1.35F, 1.35F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-45.0F));
        poseStack.translate(EXPOSED_LENGTH_OFFSET, 0.0F, 0.0F);
        itemRenderer.renderStatic(boltStack, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY,
            poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(HoneyBoltEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
