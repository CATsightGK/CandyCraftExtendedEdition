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
        float pulse = (float) Math.sin((queen.tickCount + partialTicks) * 0.65F) * 0.5F + 0.5F;
        float airborneBias = queen.onGround() ? 0.35F : 1.0F;
        float squash = slam * (0.08F + 0.07F * pulse);
        float stretch = slam * (0.14F + 0.08F * airborneBias);
        poseStack.translate(0.0F, -0.08F * slam, 0.0F);
        poseStack.scale(1.0F + squash, 1.0F - squash * 0.75F + stretch * 0.12F, 1.0F + squash);
        if (!queen.onGround()) {
            float pitch = queen.getDeltaMovement().y < 0.0D ? 14.0F + 14.0F * slam : -10.0F * slam;
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin((queen.tickCount + partialTicks) * 0.28F) * 6.0F * slam));
        }
    }
}
