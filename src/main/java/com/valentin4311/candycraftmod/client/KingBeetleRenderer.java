package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.BeetleModel;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class KingBeetleRenderer extends MobRenderer<BasicCandyZombieEntity, BeetleModel<BasicCandyZombieEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/tamedbeetle.png");

    public KingBeetleRenderer(EntityRendererProvider.Context context) {
        super(context, new BeetleModel<>(context.bakeLayer(BeetleModel.LAYER)), 1.6F);
    }

    @Override
    public ResourceLocation getTextureLocation(BasicCandyZombieEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(BasicCandyZombieEntity entity, PoseStack poseStack, float partialTickTime) {
        super.scale(entity, poseStack, partialTickTime);
        poseStack.scale(3.0F, 3.0F, 3.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
    }
}
