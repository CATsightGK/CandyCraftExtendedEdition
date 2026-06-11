package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.CandyFishModel;
import com.valentin4311.candycraftmod.entity.CandyFishEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CandyFishRenderer extends MobRenderer<CandyFishEntity, CandyFishModel<CandyFishEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/fish.png");

    public CandyFishRenderer(EntityRendererProvider.Context context) {
        super(context, new CandyFishModel<>(context.bakeLayer(CandyFishModel.LAYER)), 0.3F);
    }

    @Override
    protected void setupRotations(CandyFishEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTick);
        if (!entity.isInWater()) {
            poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(90.0F));
            poseStack.translate(-0.0625F, 0.40F, 0.0F);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(CandyFishEntity entity) {
        return TEXTURE;
    }
}
