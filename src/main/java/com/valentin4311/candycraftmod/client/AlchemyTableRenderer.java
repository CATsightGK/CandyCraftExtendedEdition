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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class AlchemyTableRenderer implements BlockEntityRenderer<AlchemyTableBlockEntity> {
    public static final ModelResourceLocation MIX_MODEL = new ModelResourceLocation(new ResourceLocation(CandyCraft.MODID, "block/alchemy_table_mix"), "");
    private static final ResourceLocation GRENADINE_SPRITE = new ResourceLocation(CandyCraft.MODID, "block/grenadine_flow");
    private static final ResourceLocation CARAMEL_SPRITE = new ResourceLocation(CandyCraft.MODID, "block/caramel_static");
    private static final ResourceLocation CHOCOLATE_SPRITE = new ResourceLocation(CandyCraft.MODID, "block/liquid_chocolate_flow");
    private static final ResourceLocation LIQUID_CANDY_SPRITE = new ResourceLocation(CandyCraft.MODID, "block/liquid_candy_flow");
    private static final ResourceLocation WATER_SPRITE = new ResourceLocation("minecraft", "block/water_flow");
    private static final ResourceLocation MILK_SPRITE = new ResourceLocation("minecraft", "block/quartz_block_bottom");
    private static final ResourceLocation LAVA_SPRITE = new ResourceLocation("minecraft", "block/lava_flow");
    private static final float SYRUP_BOTTOM_Y = 3.05F / 16.0F;
    private static final float MAX_SYRUP_TOP_Y = 12.50F / 16.0F;
    private static final float INNER_MIN = 3.10F / 16.0F;
    private static final float INNER_MAX = 12.90F / 16.0F;
    private static final float MIX_PIVOT_X = 8.0F / 16.0F;
    private static final float MIX_PIVOT_Y = 3.5F / 16.0F;
    private static final float MIX_PIVOT_Z = 8.0F / 16.0F;
    private static final float MIXER_INITIAL_ANGLE = 45.0F;
    private static final float ITEM_SCALE = 0.46F;
    private static final int FLOATING_LIQUID_UNITS = 4;
    private static final float GROUNDED_ITEM_Y = 4.15F / 16.0F;
    private final RandomSource random = RandomSource.create();

    public AlchemyTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AlchemyTableBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float time = renderTime(blockEntity, partialTick);
        renderMixer(blockEntity, time, poseStack, buffer, packedLight, packedOverlay);
        if (blockEntity.isTopFilled() || blockEntity.getLiquidAmount() > 0) {
            renderSyrup(blockEntity, time, poseStack, buffer, packedLight);
        }
        renderIngredients(blockEntity, time, poseStack, buffer, packedLight);
    }

    private void renderMixer(AlchemyTableBlockEntity blockEntity, float time, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float angle = MIXER_INITIAL_ANGLE + blockEntity.getClientMixerAngle(time);
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack stack = new ItemStack(CCItems.ALCHEMY_MIXER_BLADE.get());
        BakedModel model = minecraft.getItemRenderer().getModel(stack, blockEntity.getLevel(), null, 0);
        poseStack.pushPose();
        poseStack.translate(MIX_PIVOT_X, MIX_PIVOT_Y, MIX_PIVOT_Z);
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));
        poseStack.translate(-MIX_PIVOT_X, -MIX_PIVOT_Y, -MIX_PIVOT_Z);
        var modelRenderer = minecraft.getBlockRenderer().getModelRenderer();
        VertexConsumer consumer = buffer.getBuffer(RenderType.cutout());
        if (blockEntity.getLevel() != null) {
            modelRenderer.tesselateBlock(
                blockEntity.getLevel(),
                model,
                blockEntity.getBlockState(),
                blockEntity.getBlockPos(),
                poseStack,
                consumer,
                false,
                random,
                blockEntity.getBlockState().getSeed(blockEntity.getBlockPos()),
                packedOverlay
            );
        } else {
            modelRenderer.renderModel(
                poseStack.last(),
                consumer,
                blockEntity.getBlockState(),
                model,
                1.0F,
                1.0F,
                1.0F,
                packedLight,
                packedOverlay
            );
        }
        poseStack.popPose();
    }

    private void renderSyrup(AlchemyTableBlockEntity blockEntity, float time, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        int amount = blockEntity.getDisplayedSyrupUnits();
        LiquidStyle style = liquidStyle(blockEntity.getLiquidKind());
        if (amount <= 0 || style.spriteId() == null) {
            return;
        }

        float topY = syrupTopY(blockEntity, time);

        poseStack.pushPose();
        VertexConsumer consumer = buffer.getBuffer(style.renderType());
        poseStack.translate(0.0D, 0.003D, 0.0D);
        drawSyrupSurface(poseStack, consumer, SYRUP_BOTTOM_Y, topY, style.packedLight(packedLight), style);
        poseStack.popPose();
    }

    private void drawSyrupSurface(PoseStack poseStack, VertexConsumer consumer, float bottomY, float topY, int packedLight, LiquidStyle style) {
        PoseStack.Pose pose = poseStack.last();
        float min = INNER_MIN;
        float max = INNER_MAX;
        int topAlpha = style.topAlpha();
        int sideAlpha = style.sideAlpha();
        int tint = 255;
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(style.spriteId());
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();
        float liquidDepth = Math.max(0.0F, topY - bottomY);
        float surfaceWidth = max - min;
        float sideBottomV = v1 - Mth.clamp(liquidDepth, 0.0F, 1.0F) * (v1 - v0);

        // Top surface
        consumer.vertex(pose.pose(), min, topY, max).color(tint, tint, tint, topAlpha).uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), max, topY, max).color(tint, tint, tint, topAlpha).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), max, topY, min).color(tint, tint, tint, topAlpha).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), min, topY, min).color(tint, tint, tint, topAlpha).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();

        // Bottom surface, visible through transparent liquids so the basin reads as filled volume.
        int bottomAlpha = Math.max(80, sideAlpha);
        consumer.vertex(pose.pose(), min, bottomY, min).color(tint, tint, tint, bottomAlpha).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), max, bottomY, min).color(tint, tint, tint, bottomAlpha).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), max, bottomY, max).color(tint, tint, tint, bottomAlpha).uv(u1, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), min, bottomY, max).color(tint, tint, tint, bottomAlpha).uv(u0, v1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 1.0F, 0.0F).endVertex();

        // North wall
        consumer.vertex(pose.pose(), min, bottomY, min).color(tint, tint, tint, sideAlpha).uv(u0, sideBottomV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(pose.pose(), max, bottomY, min).color(tint, tint, tint, sideAlpha).uv(u1, sideBottomV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(pose.pose(), max, topY, min).color(tint, tint, tint, sideAlpha).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 0.0F, -1.0F).endVertex();
        consumer.vertex(pose.pose(), min, topY, min).color(tint, tint, tint, sideAlpha).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 0.0F, -1.0F).endVertex();

        // South wall
        consumer.vertex(pose.pose(), min, bottomY, max).color(tint, tint, tint, sideAlpha).uv(u0, sideBottomV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(pose.pose(), min, topY, max).color(tint, tint, tint, sideAlpha).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(pose.pose(), max, topY, max).color(tint, tint, tint, sideAlpha).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 0.0F, 1.0F).endVertex();
        consumer.vertex(pose.pose(), max, bottomY, max).color(tint, tint, tint, sideAlpha).uv(u1, sideBottomV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 0.0F, 0.0F, 1.0F).endVertex();

        // West wall
        consumer.vertex(pose.pose(), min, bottomY, min).color(tint, tint, tint, sideAlpha).uv(u0, sideBottomV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), -1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), min, topY, min).color(tint, tint, tint, sideAlpha).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), -1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), min, topY, max).color(tint, tint, tint, sideAlpha).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), -1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), min, bottomY, max).color(tint, tint, tint, sideAlpha).uv(u1, sideBottomV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), -1.0F, 0.0F, 0.0F).endVertex();

        // East wall
        consumer.vertex(pose.pose(), max, bottomY, min).color(tint, tint, tint, sideAlpha).uv(u0, sideBottomV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), max, bottomY, max).color(tint, tint, tint, sideAlpha).uv(u1, sideBottomV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), max, topY, max).color(tint, tint, tint, sideAlpha).uv(u1, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 1.0F, 0.0F, 0.0F).endVertex();
        consumer.vertex(pose.pose(), max, topY, min).color(tint, tint, tint, sideAlpha).uv(u0, v0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(pose.normal(), 1.0F, 0.0F, 0.0F).endVertex();
    }

    private void renderIngredients(AlchemyTableBlockEntity blockEntity, float time, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        List<ItemStack> ingredients = blockEntity.getIngredientsForRender();
        if (ingredients.isEmpty()) {
            return;
        }

        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        float mixerSpeed = blockEntity.getClientMixerSpeed(time);
        float stirAngle = blockEntity.getClientMixerAngle(time);
        float speedFactor = Mth.clamp(mixerSpeed / 30.0F, 0.0F, 1.75F);
        boolean canFloat = blockEntity.getDisplayedSyrupUnits() >= FLOATING_LIQUID_UNITS;
        float surfaceY = syrupTopY(blockEntity, time);

        for (int i = 0; i < Math.min(4, ingredients.size()); i++) {
            ItemStack stack = ingredients.get(i);
            if (stack.isEmpty()) {
                continue;
            }

            float phase = i * 1.913F + (blockEntity.getBlockPos().asLong() & 255L) * 0.017F;
            // The mixer is X-shaped at its 45-degree model pose, so its open cavities are the
            // cardinal sectors. Minecraft's positive Y rotation maps local +X toward world -Z.
            float cavityAngle = (stirAngle + i * 90.0F) * Mth.DEG_TO_RAD;
            float cavityRadius = canFloat ? 0.205F : 0.195F;
            double cavityX = 0.5D + Mth.cos(cavityAngle) * cavityRadius;
            double cavityZ = 0.5D - Mth.sin(cavityAngle) * cavityRadius;
            double x;
            double z;
            float itemY;
            float bob;
            float spinY;
            float spinX;
            float spinZ;
            if (!canFloat) {
                float push = 0.006F + speedFactor * 0.020F;
                x = cavityX + Mth.cos(cavityAngle + phase) * push;
                z = cavityZ - Mth.sin(cavityAngle + phase) * push;
                itemY = GROUNDED_ITEM_Y + Mth.sin(time * 0.09F + phase) * (0.002F + 0.001F * speedFactor);
                bob = 0.0F;
                spinY = stirAngle + phase * 15.0F;
                float tumbleAmplitude = 8.0F + 34.0F * speedFactor;
                spinX = 8.0F + Mth.sin(time * 0.18F + phase) * tumbleAmplitude;
                spinZ = Mth.cos(time * 0.15F + phase) * tumbleAmplitude;
            } else {
                float drift = Mth.sin(time * 0.11F + phase) * 0.018F
                    + Mth.sin(time * 0.037F + phase * 1.7F) * 0.009F;
                x = cavityX + Mth.cos(cavityAngle + phase * 0.7F) * drift;
                z = cavityZ - Mth.sin(cavityAngle + phase * 0.7F) * drift;
                itemY = Mth.clamp(surfaceY - 0.018F, 0.38F, 0.78F);
                bob = Mth.sin(time * (0.055F + 0.31F * speedFactor) + phase) * (0.016F + 0.019F * speedFactor)
                    + Mth.sin(time * 0.017F + phase * 1.7F) * 0.008F;
                spinY = stirAngle + Mth.sin(time * 0.18F + phase) * 24.0F * speedFactor + phase * 31.0F;
                float tumbleAmplitude = 20.0F + 72.0F * speedFactor;
                spinX = Mth.sin(time * 0.16F + phase) * tumbleAmplitude;
                spinZ = Mth.cos(time * 0.13F + phase * 1.6F) * tumbleAmplitude;
            }

            poseStack.pushPose();
            poseStack.translate(x, itemY + bob, z);
            poseStack.mulPose(Axis.YP.rotationDegrees(spinY));
            poseStack.mulPose(Axis.XP.rotationDegrees(spinX));
            poseStack.mulPose(Axis.ZP.rotationDegrees(spinZ));
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
            itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, blockEntity.getLevel(), 0);
            poseStack.popPose();
        }
    }

    private float syrupTopY(AlchemyTableBlockEntity blockEntity, float time) {
        int amount = blockEntity.getDisplayedSyrupUnits();
        if (amount <= 0) {
            return SYRUP_BOTTOM_Y;
        }

        float baseHeight = Mth.lerp(blockEntity.getLiquidFillFraction(), SYRUP_BOTTOM_Y, MAX_SYRUP_TOP_Y);
        float mixerSpeed = blockEntity.getClientMixerSpeed(time);
        float speedFactor = Mth.clamp(mixerSpeed / 32.0F, 0.0F, 2.625F);
        float motionBlend = Mth.clamp(mixerSpeed / 8.0F, 0.0F, 1.0F);
        if (motionBlend <= 0.0F) {
            return baseHeight;
        }

        float phase = (blockEntity.getBlockPos().asLong() & 1023L) * 0.013F;
        float frequency = 0.18F + 0.09F * speedFactor;
        float wave = Mth.sin(time * frequency + phase) * 0.74F
            + Mth.sin(time * frequency * 0.53F + phase * 1.7F) * 0.26F;
        float amplitude = motionBlend * (0.003F + 0.004F * Mth.clamp(speedFactor, 0.0F, 1.75F));
        return Mth.clamp(baseHeight + wave * amplitude, SYRUP_BOTTOM_Y, MAX_SYRUP_TOP_Y + 0.008F);
    }

    private float renderTime(AlchemyTableBlockEntity blockEntity, float partialTick) {
        return blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() + partialTick : partialTick;
    }

    private static LiquidStyle liquidStyle(AlchemyLiquidKind kind) {
        return switch (kind) {
            case GRENADINE -> new LiquidStyle(GRENADINE_SPRITE, true, false, 170, 115);
            case WATER -> new LiquidStyle(WATER_SPRITE, true, false, 120, 80);
            case MILK -> new LiquidStyle(MILK_SPRITE, false, false, 255, 255);
            case CHOCOLATE -> new LiquidStyle(CHOCOLATE_SPRITE, false, false, 255, 255);
            case LIQUID_CANDY -> new LiquidStyle(LIQUID_CANDY_SPRITE, false, true, 255, 255);
            case LAVA -> new LiquidStyle(LAVA_SPRITE, false, true, 255, 255);
            case CARAMEL -> new LiquidStyle(CARAMEL_SPRITE, true, false, 176, 140);
            case NONE -> new LiquidStyle(null, true, false, 0, 0);
        };
    }

    private record LiquidStyle(ResourceLocation spriteId, boolean translucent, boolean fullBright, int topAlpha, int sideAlpha) {

        private RenderType renderType() {
            return translucent
                ? RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS)
                : RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS);
        }

        private int packedLight(int packedLight) {
            return fullBright ? 0x00F000F0 : packedLight;
        }

    }
}
