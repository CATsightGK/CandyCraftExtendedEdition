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
        float eased = easeOutBack(slam);
        float pulse = (float) Math.sin((queen.tickCount + partialTicks) * 0.34F) * 0.5F + 0.5F;
        if (!queen.onGround()) {
            boolean falling = queen.getDeltaMovement().y < -0.03D;
            float anticipation = (float) Math.sin(slam * Math.PI);
            float stretch = eased * (falling ? 0.25F : 0.13F);
            float squash = eased * 0.055F;
            poseStack.translate(0.0F, -0.045F * eased + 0.025F * anticipation, 0.0F);
            poseStack.scale(1.0F - squash, 1.0F + stretch, 1.0F - squash);
            float pitch = falling ? 9.0F + 18.0F * eased : -9.0F * anticipation;
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin((queen.tickCount + partialTicks) * 0.18F) * 5.5F * eased));
        } else {
            float rebound = (float) Math.sin(slam * Math.PI * 2.0F) * (1.0F - slam);
            float impact = eased * (0.8F + 0.2F * pulse);
            float squash = 0.2F * impact;
            poseStack.translate(0.0F, -0.09F * impact + 0.035F * rebound, 0.0F);
            poseStack.scale(1.0F + squash, 1.0F - squash * 0.9F, 1.0F + squash);
        }
    }

    private static float easeOutBack(float value) {
        float c1 = 1.70158F;
        float c3 = c1 + 1.0F;
        float t = value - 1.0F;
        return 1.0F + c3 * t * t * t + c1 * t * t;
    }
}
