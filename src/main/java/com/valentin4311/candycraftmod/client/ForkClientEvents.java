package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.item.ForkItem;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ForkClientEvents {
    private ForkClientEvents() {
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        ItemStack stack = event.getItemStack();
        if (!isFork(stack) || !ForkItem.hasHeldBlock(stack)) {
            return;
        }
        BlockState state = ForkItem.getHeldBlockState(stack);
        if (state.isAir()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        float progress = ForkItem.getEatProgress(stack);
        float bite = (float)Math.sin(progress * Math.PI * 4.0F) * 0.025F;
        int side = event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND ? 1 : -1;
        poseStack.translate(0.28F * side, -0.19F + bite, -0.46F);
        poseStack.mulPose(Axis.YP.rotationDegrees(35.0F * side));
        poseStack.mulPose(Axis.XP.rotationDegrees(-18.0F));
        poseStack.scale(0.18F, 0.18F, 0.18F);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
            state,
            poseStack,
            event.getMultiBufferSource(),
            event.getPackedLight(),
            OverlayTexture.NO_OVERLAY
        );
        poseStack.popPose();
    }

    private static boolean isFork(ItemStack stack) {
        return stack.is(CCItems.FORK.get());
    }
}
