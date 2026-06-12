package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.entity.AlchemyTableBlockEntity;
import com.valentin4311.candycraftmod.block.entity.AlchemyLiquidKind;
import com.valentin4311.candycraftmod.registry.CCItems;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class AlchemyTableRenderer implements BlockEntityRenderer<AlchemyTableBlockEntity> {
    public static final ModelResourceLocation MIX_MODEL = new ModelResourceLocation(new ResourceLocation(CandyCraft.MODID, "block/alchemy_table_mix"), "");
    private static final ResourceLocation GRENADINE_TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/block/grenadine_flow.png");
    private static final ResourceLocation CARAMEL_TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/block/caramel_static.png");
    private static final ResourceLocation CHOCOLATE_TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/block/liquid_chocolate_flow.png");
    private static final ResourceLocation LIQUID_CANDY_TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/block/liquid_candy_flow.png");
    private static final ResourceLocation WATER_TEXTURE = new ResourceLocation("minecraft", "textures/block/water_flow.png");
    private static final ResourceLocation MILK_TEXTURE = new ResourceLocation("minecraft", "textures/block/quartz_block_bottom.png");
    private static final ResourceLocation LAVA_TEXTURE = new ResourceLocation("minecraft", "textures/block/lava_flow.png");
    private static final float SYRUP_BOTTOM_Y = 3.05F / 16.0F;
    private static final float MIN_SYRUP_TOP_Y = 3.00F / 16.0F;
    private static final float MAX_SYRUP_TOP_Y = 10.80F / 16.0F;
    private static final float INNER_MIN = 3.10F / 16.0F;
    private static final float INNER_MAX = 12.90F / 16.0F;
    private static final float MIX_PIVOT_X = 8.0F / 16.0F;
    private static final float MIX_PIVOT_Y = 3.5F / 16.0F;
    private static final float MIX_PIVOT_Z = 8.0F / 16.0F;
    private static final Vec3[] ITEM_POSITIONS = {
        new Vec3(0.34D, 0.0D, 0.34D),
        new Vec3(0.66D, 0.0D, 0.34D),
        new Vec3(0.34D, 0.0D, 0.66D),
        new Vec3(0.66D, 0.0D, 0.66D)
    };

    public AlchemyTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AlchemyTableBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float time = renderTime(blockEntity, partialTick);
        boolean mixing = blockEntity.isMixing();
        renderMixer(blockEntity, time, mixing, poseStack, buffer, packedLight, packedOverlay);
        if (blockEntity.isTopFilled() || blockEntity.getLiquidAmount() > 0) {
            renderSyrup(blockEntity, time, mixing, poseStack, buffer, packedLight);
        }
        renderIngredients(blockEntity, time, mixing, poseStack, buffer, packedLight);
    }

    private void renderMixer(AlchemyTableBlockEntity blockEntity, float time, boolean mixing, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float angle = mixing ? time * (blockEntity.isFastMixing() ? 36.0F : 18.0F) : 0.0F;
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack stack = new ItemStack(CCItems.ALCHEMY_MIXER_BLADE.get());
        BakedModel model = minecraft.getItemRenderer().getModel(stack, blockEntity.getLevel(), null, 0);
        poseStack.pushPose();
        poseStack.translate(MIX_PIVOT_X, MIX_PIVOT_Y, MIX_PIVOT_Z);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.translate(-MIX_PIVOT_X, -MIX_PIVOT_Y, -MIX_PIVOT_Z);
        minecraft.getBlockRenderer().getModelRenderer().renderModel(
            poseStack.last(),
            buffer.getBuffer(RenderType.cutout()),
            blockEntity.getBlockState(),
            model,
            1.0F,
            1.0F,
            1.0F,
            packedLight,
            packedOverlay
        );
        poseStack.popPose();
    }

    private void renderSyrup(AlchemyTableBlockEntity blockEntity, float time, boolean mixing, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        int amount = blockEntity.getDisplayedSyrupUnits();
        LiquidStyle style = liquidStyle(blockEntity.getLiquidKind());
        if (amount <= 0 || style.texture() == null) {
            return;
        }

        float topY = syrupTopY(blockEntity, time, mixing);

        poseStack.pushPose();
        VertexConsumer consumer = buffer.getBuffer(style.renderType());
        poseStack.translate(0.0D, 0.003D, 0.0D);
        drawSyrupSurface(poseStack, consumer, SYRUP_BOTTOM_Y, topY, style.packedLight(packedLight), time, mixing, blockEntity.isFastMixing(), style);
        poseStack.popPose();
    }

    private void drawSyrupSurface(PoseStack poseStack, VertexConsumer consumer, float bottomY, float topY, int packedLight, float time, boolean mixing, boolean fast, LiquidStyle style) {
        PoseStack.Pose pose = poseStack.last();
        float min = INNER_MIN;
        float max = INNER_MAX;
        int topAlpha = style.topAlpha();
        int sideAlpha = style.sideAlpha();
        int tint = 255;
        float speed = mixing ? 0.08F * (fast ? 1.7F : 1.0F) : 0.035F;
        float topScroll = -(time * speed) % 1.0F;
        float sideScroll = -(time * speed * 1.6F) % 1.0F;

        // Top surface
        consumer.vertex(pose.pose(), min, topY, max).color(tint, tint, tint, topAlpha).uv(0.0F, 1.0F + topScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), max, topY, max).color(tint, tint, tint, topAlpha).uv(1.0F, 1.0F + topScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), max, topY, min).color(tint, tint, tint, topAlpha).uv(1.0F, 0.0F + topScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), min, topY, min).color(tint, tint, tint, topAlpha).uv(0.0F, 0.0F + topScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();

        // Bottom surface, visible through transparent liquids so the basin reads as filled volume.
        int bottomAlpha = Math.max(80, sideAlpha);
        consumer.vertex(pose.pose(), min, bottomY, min).color(tint, tint, tint, bottomAlpha).uv(0.0F, 0.0F + topScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), max, bottomY, min).color(tint, tint, tint, bottomAlpha).uv(1.0F, 0.0F + topScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), max, bottomY, max).color(tint, tint, tint, bottomAlpha).uv(1.0F, 1.0F + topScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), min, bottomY, max).color(tint, tint, tint, bottomAlpha).uv(0.0F, 1.0F + topScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();

        // North wall
        consumer.vertex(pose.pose(), min, bottomY, min).color(tint, tint, tint, sideAlpha).uv(0.0F, 1.0F + sideScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(pose.pose(), max, bottomY, min).color(tint, tint, tint, sideAlpha).uv(1.0F, 1.0F + sideScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(pose.pose(), max, topY, min).color(tint, tint, tint, sideAlpha).uv(1.0F, 0.0F + sideScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(pose.pose(), min, topY, min).color(tint, tint, tint, sideAlpha).uv(0.0F, 0.0F + sideScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 0.0F, -1.0F).endVertex();

        // South wall
        consumer.vertex(pose.pose(), min, bottomY, max).color(tint, tint, tint, sideAlpha).uv(0.0F, 1.0F + sideScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(pose.pose(), min, topY, max).color(tint, tint, tint, sideAlpha).uv(0.0F, 0.0F + sideScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(pose.pose(), max, topY, max).color(tint, tint, tint, sideAlpha).uv(1.0F, 0.0F + sideScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(pose.pose(), max, bottomY, max).color(tint, tint, tint, sideAlpha).uv(1.0F, 1.0F + sideScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 0.0F, 1.0F).endVertex();

        // West wall
        consumer.vertex(pose.pose(), min, bottomY, min).color(tint, tint, tint, sideAlpha).uv(0.0F, 1.0F + sideScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), -1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), min, topY, min).color(tint, tint, tint, sideAlpha).uv(0.0F, 0.0F + sideScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), -1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), min, topY, max).color(tint, tint, tint, sideAlpha).uv(1.0F, 0.0F + sideScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), -1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), min, bottomY, max).color(tint, tint, tint, sideAlpha).uv(1.0F, 1.0F + sideScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), -1.0F, 0.0F, 0.0F).endVertex();

        // East wall
        consumer.vertex(pose.pose(), max, bottomY, min).color(tint, tint, tint, sideAlpha).uv(0.0F, 1.0F + sideScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), max, bottomY, max).color(tint, tint, tint, sideAlpha).uv(1.0F, 1.0F + sideScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), max, topY, max).color(tint, tint, tint, sideAlpha).uv(1.0F, 0.0F + sideScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), max, topY, min).color(tint, tint, tint, sideAlpha).uv(0.0F, 0.0F + sideScroll).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 1.0F, 0.0F, 0.0F).endVertex();
    }

    private void renderIngredients(AlchemyTableBlockEntity blockEntity, float time, boolean mixing, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        List<ItemStack> ingredients = blockEntity.getIngredientsForRender();
        if (ingredients.isEmpty()) {
            return;
        }

        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        float stirAngle = mixing ? -time * 18.0F : 0.0F;
        float surfaceY = syrupTopY(blockEntity, time, mixing);
        float itemY = Mth.clamp(surfaceY - 0.015F, 0.32F, 0.70F);

        for (int i = 0; i < Math.min(4, ingredients.size()); i++) {
            ItemStack stack = ingredients.get(i);
            if (stack.isEmpty()) {
                continue;
            }

            Vec3 base = ITEM_POSITIONS[i];
            double x = base.x;
            double z = base.z;
            if (mixing) {
                float orbit = (stirAngle + i * 90.0F) * Mth.DEG_TO_RAD;
                float radius = (blockEntity.isFastMixing() ? 0.23F : 0.19F) + (i % 2) * 0.025F + (blockEntity.isFastMixing() ? Mth.sin(time * 0.7F + i) * 0.025F : 0.0F);
                x = 0.5D + Mth.cos(orbit) * radius;
                z = 0.5D + Mth.sin(orbit + (blockEntity.isFastMixing() ? Mth.sin(time * 0.3F + i) * 0.35F : 0.0F)) * radius;
            }
            float bob = Mth.sin(time * (mixing ? (blockEntity.isFastMixing() ? 0.9F : 0.45F) : 0.1F) + i * 1.7F) * (mixing ? (blockEntity.isFastMixing() ? 0.035F : 0.018F) : 0.03F);
            float spin = (mixing ? stirAngle * (blockEntity.isFastMixing() ? 1.8F : 1.0F) : -time * 2.0F) + i * 37.0F;

            poseStack.pushPose();
            poseStack.translate(x, itemY + bob, z);
            poseStack.mulPose(Axis.YP.rotationDegrees(spin));
            poseStack.scale(0.46F, 0.46F, 0.46F);
            itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, blockEntity.getLevel(), 0);
            poseStack.popPose();
        }
    }

    private float syrupTopY(AlchemyTableBlockEntity blockEntity, float time, boolean mixing) {
        int amount = blockEntity.getDisplayedSyrupUnits();
        if (amount <= 0) {
            return MIN_SYRUP_TOP_Y;
        }
        float topY = Mth.lerp(amount / 6.0F, MIN_SYRUP_TOP_Y, MAX_SYRUP_TOP_Y);
        if (mixing) {
            topY += Mth.sin(time * 0.45F) * 0.006F;
        }
        return Mth.clamp(topY, MIN_SYRUP_TOP_Y, MAX_SYRUP_TOP_Y);
    }

    private float renderTime(AlchemyTableBlockEntity blockEntity, float partialTick) {
        return blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() + partialTick : partialTick;
    }

    private static LiquidStyle liquidStyle(AlchemyLiquidKind kind) {
        return switch (kind) {
            case GRENADINE -> new LiquidStyle(GRENADINE_TEXTURE, true, false, 170, 115);
            case WATER -> new LiquidStyle(WATER_TEXTURE, true, false, 120, 80);
            case MILK -> new LiquidStyle(MILK_TEXTURE, false, false, 255, 255);
            case CHOCOLATE -> new LiquidStyle(CHOCOLATE_TEXTURE, false, false, 255, 255);
            case LIQUID_CANDY -> new LiquidStyle(LIQUID_CANDY_TEXTURE, false, true, 255, 255);
            case LAVA -> new LiquidStyle(LAVA_TEXTURE, false, true, 255, 255);
            case CARAMEL -> new LiquidStyle(CARAMEL_TEXTURE, true, false, 176, 140);
            case NONE -> new LiquidStyle(null, true, false, 0, 0);
        };
    }

    private record LiquidStyle(ResourceLocation texture, boolean translucent, boolean fullBright, int topAlpha, int sideAlpha) {
        private RenderType renderType() {
            return translucent ? RenderType.entityTranslucent(texture) : RenderType.entityCutoutNoCull(texture);
        }

        private int packedLight(int packedLight) {
            return fullBright ? 0x00F000F0 : packedLight;
        }

    }
}
