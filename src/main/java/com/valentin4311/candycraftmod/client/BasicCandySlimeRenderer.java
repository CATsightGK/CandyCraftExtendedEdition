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
        float t = 1.0F - slam;
        float eased = smootherStep(t);
        float settle = 1.0F - eased;
        float motionTime = queen.tickCount + partialTicks;
        if (!queen.onGround()) {
            boolean falling = queen.getDeltaMovement().y < -0.03D;
            float hang = smootherStep(1.0F - Math.abs(t * 2.0F - 1.0F));
            float fallCurve = smootherStep(Math.min(1.0F, t * 1.25F));
            float stretchCurve = falling ? fallCurve : hang;
            float stretch = (falling ? 0.16F : 0.07F) * stretchCurve;
            float squash = 0.024F * stretchCurve;
            float bob = (float) Math.sin(motionTime * 0.13F) * 0.006F * settle;
            poseStack.translate(0.0F, -0.03F * stretchCurve + 0.014F * hang + bob, 0.0F);
            poseStack.scale(1.0F - squash, 1.0F + stretch, 1.0F - squash);
            float pitch = falling ? lerp(fallCurve, 2.0F, 10.0F) : -4.0F * hang;
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(motionTime * 0.08F) * 1.7F * settle));
        } else {
            float impact = 1.0F - smootherStep(Math.min(1.0F, t * 3.2F));
            float recover = 1.0F - smootherStep(t);
            float rebound = (float) Math.sin(Math.min(1.0F, t) * Math.PI * 3.0F) * recover;
            float squash = 0.12F * impact + 0.022F * Math.max(0.0F, rebound);
            float yScale = 1.0F - squash * 0.76F;
            float xzScale = 1.0F + squash;
            poseStack.translate(0.0F, -0.052F * impact + 0.012F * rebound, 0.0F);
            poseStack.scale(xzScale, yScale, xzScale);
            poseStack.mulPose(Axis.XP.rotationDegrees(rebound * 1.6F));
        }
    }

    private static float smootherStep(float value) {
        float clamped = Math.max(0.0F, Math.min(1.0F, value));
        return clamped * clamped * clamped * (clamped * (clamped * 6.0F - 15.0F) + 10.0F);
    }

    private static float lerp(float amount, float from, float to) {
        return from + (to - from) * amount;
    }
}
