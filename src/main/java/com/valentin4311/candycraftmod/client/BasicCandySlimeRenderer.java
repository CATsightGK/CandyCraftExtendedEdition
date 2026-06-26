package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.BasicCandySlimeEntity;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Slime;

public class BasicCandySlimeRenderer extends SlimeRenderer {
    public BasicCandySlimeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Slime entity) {
        String texture = "sprinterjelly.png";
        if (entity.getType() == CCEntityTypes.RED_JELLY.get()) texture = "kamikazejelly.png";
        else if (entity.getType() == CCEntityTypes.TORNADO_JELLY.get()) texture = "tornadojelly.png";
        else if (entity instanceof BasicCandySlimeEntity candy) texture = getCandySlimeTexture(candy);
        return new ResourceLocation(CandyCraft.MODID, "textures/entity/" + texture);
    }

    @Override
    protected void setupRotations(Slime entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        if (entity.getType() == CCEntityTypes.TORNADO_JELLY.get() && !entity.onGround()) {
            float flip = (entity.tickCount + partialTicks) * 28.0F;
            poseStack.mulPose(Axis.XP.rotationDegrees(flip));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin((entity.tickCount + partialTicks) * 0.35F) * 14.0F));
        }
        if (entity instanceof BasicCandySlimeEntity candy && candy.isJellyQueen()) {
            applyJellyQueenSlamPose(candy, poseStack, partialTicks);
        }
    }

    private static String getJellyQueenTexture(BasicCandySlimeEntity queen) {
        if (!queen.isBossAwake()) {
            return "candyboss4.png";
        }
        return switch (queen.getJellyQueenMode()) {
            case BasicCandySlimeEntity.JELLY_QUEEN_BLUE_MODE -> "candyboss2.png";
            case BasicCandySlimeEntity.JELLY_QUEEN_BROWN_MODE -> "candyboss3.png";
            default -> "candyboss.png";
        };
    }

    private static String getCandySlimeTexture(BasicCandySlimeEntity candy) {
        if (candy.isPezJelly()) {
            return candy.isBossAwake() ? "candyboss5.png" : "candyboss4.png";
        }
        if (candy.isKingSlime()) {
            return candy.isBossAwake() ? "candyboss6.png" : "candyboss4.png";
        }
        if (candy.isJellyQueen()) {
            return getJellyQueenTexture(candy);
        }
        return "sprinterjelly.png";
    }

    private static void applyJellyQueenSlamPose(BasicCandySlimeEntity queen, PoseStack poseStack, float partialTicks) {
        float slam = queen.getJellyQueenSlamProgress(partialTicks);
        if (slam <= 0.0F) {
            return;
        }
        float eased = smoothStep(slam);
        if (!queen.onGround()) {
            boolean falling = queen.getDeltaMovement().y < -0.03D;
            float anticipation = smoothStep(1.0F - Math.abs(slam * 2.0F - 1.0F));
            float stretch = (falling ? 0.17F : 0.09F) * eased;
            float squash = 0.035F * eased;
            poseStack.translate(0.0F, -0.025F * eased + 0.015F * anticipation, 0.0F);
            poseStack.scale(1.0F - squash, 1.0F + stretch, 1.0F - squash);
            float pitch = falling ? 6.0F + 9.0F * eased : -5.0F * anticipation;
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin((queen.tickCount + partialTicks) * 0.14F) * 2.5F * eased));
        } else {
            float impact = smoothStep(slam);
            float recover = 1.0F - smoothStep(Math.min(1.0F, slam * 1.35F));
            float rebound = (float) Math.sin(slam * Math.PI) * recover;
            float squash = 0.12F * impact;
            poseStack.translate(0.0F, -0.052F * impact + 0.018F * rebound, 0.0F);
            poseStack.scale(1.0F + squash, 1.0F - squash * 0.78F, 1.0F + squash);
        }
    }

    private static float smoothStep(float value) {
        float clamped = Math.max(0.0F, Math.min(1.0F, value));
        return clamped * clamped * (3.0F - 2.0F * clamped);
    }
}
