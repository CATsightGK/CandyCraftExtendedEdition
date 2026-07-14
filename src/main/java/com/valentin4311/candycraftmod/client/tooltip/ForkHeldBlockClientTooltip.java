package com.valentin4311.candycraftmod.client.tooltip;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.inventory.tooltip.ForkHeldBlockTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public final class ForkHeldBlockClientTooltip implements ClientTooltipComponent {
    private final ForkHeldBlockTooltip tooltip;

    public ForkHeldBlockClientTooltip(ForkHeldBlockTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public int getWidth(Font font) {
        return 20 + font.width(tooltip.name());
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        BlockState state = tooltip.state();
        if (state.getBlock() instanceof DoorBlock) {
            renderDoorModel(state, x, y, graphics);
        } else {
            ItemStack icon = tooltip.iconStack();
            if (!icon.isEmpty()) {
                graphics.renderItem(icon, x, y + 1);
            }
        }
        graphics.drawString(font, tooltip.name(), x + 20, y + 6, 0xFFFFFF, false);
    }

    private static void renderDoorModel(BlockState state, int x, int y, GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        BlockState lower = state.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
        BlockState upper = state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        graphics.flush();
        Lighting.setupFor3DItems();

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();
        poseStack.translate(x + 8.0F, y + 18.0F, 400.0F);
        poseStack.scale(7.0F, -7.0F, 7.0F);
        poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(225.0F));
        poseStack.translate(-0.5F, -1.0F, -0.5F);

        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        minecraft.getBlockRenderer().renderSingleBlock(
            lower, poseStack, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY
        );
        poseStack.translate(0.0F, 1.0F, 0.0F);
        minecraft.getBlockRenderer().renderSingleBlock(
            upper, poseStack, buffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY
        );
        buffer.endBatch();
        poseStack.popPose();
        Lighting.setupForFlatItems();
    }
}
