package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
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

        poseStack.translate(0.0F, animation.bob, 0.0F);
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
        float bob = Mth.sin(elapsed * 2.8F) * 0.035F * amount;
        return new Animation(amount, bob);
    }

    private static float smoothStep(float value) {
        return value * value * (3.0F - 2.0F * value);
    }

    private record Animation(float amount, float bob) {
        private static final Animation NONE = new Animation(0.0F, 0.0F);
    }
}
