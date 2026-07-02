package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.BeetleModel;
import com.valentin4311.candycraftmod.entity.BasicCandySpiderEntity;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BeetleRenderer extends MobRenderer<BasicCandySpiderEntity, BeetleModel<BasicCandySpiderEntity>> {
    private static final ResourceLocation BEETLE = texture("beetle.png");
    private static final ResourceLocation ANGRY = texture("angrybeetle.png");
    private static final ResourceLocation BOSS = texture("bossbeetle.png");
    private static final ResourceLocation BOSS_SLEEPING = texture("bossbeetle2.png");

    public BeetleRenderer(EntityRendererProvider.Context context) {
        super(context, new BeetleModel<>(context.bakeLayer(BeetleModel.LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(BasicCandySpiderEntity entity) {
        if (entity.getType() == CCEntityTypes.BOSS_BEETLE.get()) {
            return entity.isBossAwake() ? BOSS : BOSS_SLEEPING;
        }
        return entity.isAngry() ? ANGRY : BEETLE;
    }

    @Override
    protected void scale(BasicCandySpiderEntity entity, PoseStack poseStack, float partialTickTime) {
        super.scale(entity, poseStack, partialTickTime);
        if (entity.getType() == CCEntityTypes.BOSS_BEETLE.get()) {
            poseStack.scale(2.0F, 2.0F, 2.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
            return;
        }
        if (entity.isChildBeetle()) {
            poseStack.scale(0.5F, 0.5F, 0.5F);
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(CandyCraft.MODID, "textures/entity/" + name);
    }
}
