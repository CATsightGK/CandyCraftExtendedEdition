package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.DragonModel;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DragonRenderer extends MobRenderer<BasicCandyZombieEntity, DragonModel<BasicCandyZombieEntity>> {
    private static final ResourceLocation ADULT_TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/dragons.png");
    private static final ResourceLocation BABY_TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/babydragons.png");

    public DragonRenderer(EntityRendererProvider.Context context) {
        super(context, new DragonModel<>(context.bakeLayer(DragonModel.LAYER)), 1.2F);
    }

    @Override
    protected void setupRotations(BasicCandyZombieEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        if (entity.isDragonFlying()) {
            poseStack.mulPose(Axis.XP.rotationDegrees(entity.getXRot() * 0.55F));
        }
    }

    @Override
    protected void scale(BasicCandyZombieEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.translate(-0.0625F, 0.0F, 0.0F);
        if (entity.isDragonFlying()) {
            poseStack.translate(0.0F, Math.sin((entity.tickCount + partialTickTime) * 0.05F) / 6.0D, 0.0F);
        }
        if (entity.isBabyDragon()) {
            poseStack.scale(0.75F, 0.75F, 0.75F);
        } else {
            poseStack.scale(1.5F, 1.5F, 1.5F);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(BasicCandyZombieEntity entity) {
        return entity.isBabyDragon() ? BABY_TEXTURE : ADULT_TEXTURE;
    }
}
