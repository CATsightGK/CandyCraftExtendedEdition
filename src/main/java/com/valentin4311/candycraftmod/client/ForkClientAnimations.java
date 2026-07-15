package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.item.ForkItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public final class ForkClientAnimations {
    private ForkClientAnimations() {
    }

    public static HumanoidModel.ArmPose getArmPose(ItemStack stack) {
        return ForkItem.getEatAnimationTicks(stack) > 0 ? HumanoidModel.ArmPose.TOOT_HORN : null;
    }

    public static boolean applyFirstPersonTransform(PoseStack poseStack, HumanoidArm arm,
            ItemStack stack, float partialTick) {
        Animation animation = animation(stack, partialTick);
        if (animation.amount <= 0.0F) {
            return false;
        }

        float handSign = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float chew = animation.chew * animation.amount;
        poseStack.translate(
            -handSign * 0.22F * animation.amount,
            0.31F * animation.amount + animation.bob,
            0.38F * animation.amount
        );
        poseStack.mulPose(Axis.XP.rotationDegrees(-48.0F * animation.amount + chew * 4.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(handSign * 13.0F * animation.amount));
        poseStack.mulPose(Axis.ZP.rotationDegrees(handSign * (9.0F * animation.amount + chew * 3.0F)));
        return false;
    }

    public static void applyThirdPersonItemTransform(PoseStack poseStack, ItemStack stack, float partialTick) {
        Animation animation = animation(stack, partialTick);
        if (animation.amount > 0.0F) {
            poseStack.translate(0.0F, animation.bob * 1.4F, 0.0F);
        }
    }

    public static float getArmBobDegrees(ItemStack stack, float partialTick) {
        return animation(stack, partialTick).bob * 80.0F;
    }

    private static Animation animation(ItemStack stack, float partialTick) {
        int remaining = ForkItem.getEatAnimationTicks(stack);
        if (remaining <= 0) {
            return Animation.NONE;
        }
        float elapsed = ForkItem.EAT_ANIMATION_TICKS - remaining + partialTick;
        float approach = smoothStep(Mth.clamp(elapsed / 4.0F, 0.0F, 1.0F));
        float retreat = smoothStep(Mth.clamp((ForkItem.EAT_ANIMATION_TICKS - elapsed) / 3.0F, 0.0F, 1.0F));
        float amount = approach * retreat;
        float chew = Mth.sin(elapsed * Mth.PI) * 0.5F + 0.5F;
        float bob = -Mth.abs(Mth.sin(elapsed * Mth.PI)) * 0.028F * amount;
        return new Animation(amount, bob, chew);
    }

    private static float smoothStep(float value) {
        return value * value * (3.0F - 2.0F * value);
    }

    private record Animation(float amount, float bob, float chew) {
        private static final Animation NONE = new Animation(0.0F, 0.0F, 0.0F);
    }
}
