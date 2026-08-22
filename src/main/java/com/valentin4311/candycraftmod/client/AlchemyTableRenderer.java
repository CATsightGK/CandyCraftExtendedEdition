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
    /** Dummy model that stitches the manual flow-strip sprites into the block atlas. */
    public static final ModelResourceLocation STRIPS_MODEL = new ModelResourceLocation(new ResourceLocation(CandyCraft.MODID, "block/alchemy_fluid_strips"), "");

    // Still sprites (used for the surface and the resting state of the walls).
    private static final ResourceLocation GRENADINE_STILL = new ResourceLocation(CandyCraft.MODID, "block/grenadine_static");
    private static final ResourceLocation CARAMEL_STILL = new ResourceLocation(CandyCraft.MODID, "block/caramel_static");
    private static final ResourceLocation CHOCOLATE_STILL = new ResourceLocation(CandyCraft.MODID, "block/liquid_chocolate_still");
    private static final ResourceLocation LIQUID_CANDY_STILL = new ResourceLocation(CandyCraft.MODID, "block/liquid_candy_still");
    private static final ResourceLocation WATER_STILL = new ResourceLocation("minecraft", "block/water_still");
    private static final ResourceLocation LAVA_STILL = new ResourceLocation("minecraft", "block/lava_still");
    private static final ResourceLocation MILK_SPRITE = new ResourceLocation("minecraft", "block/quartz_block_bottom");

    // Flow strips: copies of the flowing textures WITHOUT animation metadata, so the
    // renderer can address individual frames and drive speed/direction from the mixer.
    private static final ResourceLocation GRENADINE_FLOW_STRIP = new ResourceLocation(CandyCraft.MODID, "block/alchemy_flow_grenadine");
    private static final ResourceLocation CARAMEL_FLOW_STRIP = new ResourceLocation(CandyCraft.MODID, "block/alchemy_flow_caramel");
    private static final ResourceLocation CHOCOLATE_FLOW_STRIP = new ResourceLocation(CandyCraft.MODID, "block/alchemy_flow_chocolate");
    private static final ResourceLocation LIQUID_CANDY_FLOW_STRIP = new ResourceLocation(CandyCraft.MODID, "block/alchemy_flow_liquid_candy");
    private static final ResourceLocation WATER_FLOW_STRIP = new ResourceLocation(CandyCraft.MODID, "block/alchemy_flow_water");
    private static final ResourceLocation LAVA_FLOW_STRIP = new ResourceLocation(CandyCraft.MODID, "block/alchemy_flow_lava");

    private static final float SYRUP_BOTTOM_Y = 3.05F / 16.0F;
    private static final float MAX_SYRUP_TOP_Y = 12.50F / 16.0F;
    private static final float INNER_MIN = 3.10F / 16.0F;
    private static final float INNER_MAX = 12.90F / 16.0F;
    /**
     * Width of one basin wall as a fraction of a full 16px texture: the UV window is
     * cropped to exactly this size so every texture pixel stays square (no stretching).
     */
    private static final float WALL_WINDOW = INNER_MAX - INNER_MIN;
    /**
     * The four inner walls: {x0, z0, x1, z1, nx, ny, nz, liquidDir, phase}.
     * {@code liquidDir} (+1/-1) is the tangential circulation direction along the wall's
     * U axis for the mixer's positive rotation; {@code phase} selects a fixed crop window.
     */
    private static final float[][] WALLS = {
        {INNER_MIN, INNER_MIN, INNER_MAX, INNER_MIN, 0.0F, 0.0F, -1.0F, -1.0F, 0.00F},
        {INNER_MIN, INNER_MAX, INNER_MAX, INNER_MAX, 0.0F, 0.0F, 1.0F, 1.0F, 0.31F},
        {INNER_MIN, INNER_MIN, INNER_MIN, INNER_MAX, -1.0F, 0.0F, 0.0F, 1.0F, 0.52F},
        {INNER_MAX, INNER_MIN, INNER_MAX, INNER_MAX, 1.0F, 0.0F, 0.0F, -1.0F, 0.77F},
    };
    private static final float MIX_PIVOT_X = 8.0F / 16.0F;
    private static final float MIX_PIVOT_Y = 3.5F / 16.0F;
    private static final float MIX_PIVOT_Z = 8.0F / 16.0F;
    private static final float MIXER_INITIAL_ANGLE = 45.0F;
    private static final float ITEM_SCALE = 0.46F;
    private static final int FLOATING_LIQUID_UNITS = 4;
    private static final float GROUNDED_ITEM_Y = 4.15F / 16.0F;
    /** Mixer speed at/above which the walls are considered fully "flowing". */
    private static final float FLOW_FULL_SPEED = 8.0F;
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
        if (amount <= 0 || style.stillId() == null) {
            return;
        }

        float mixerSpeed = blockEntity.getClientMixerSpeed(time);
        float stillness = 1.0F - Mth.clamp(mixerSpeed / FLOW_FULL_SPEED, 0.0F, 1.0F);
        float flowFrame = blockEntity.getClientFlowFrame(time);
        int heightPx = syrupBaseHeightPx(blockEntity);
        float topY = syrupTopY(blockEntity, time);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.003D, 0.0D);
        drawLiquid(blockEntity, poseStack, buffer, heightPx, topY, stillness, flowFrame, style.packedLight(packedLight), style);
        poseStack.popPose();
    }

    private void drawLiquid(AlchemyTableBlockEntity blockEntity, PoseStack poseStack, MultiBufferSource buffer,
            int heightPx, float topY, float stillness, float flowFrame, int packedLight, LiquidStyle style) {
        PoseStack.Pose pose = poseStack.last();
        float min = INNER_MIN;
        float max = INNER_MAX;
        int tint = 255;
        var atlas = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);

        TextureAtlasSprite stillSprite = atlas.apply(style.stillId());
        float su0 = stillSprite.getU0();
        float su1 = stillSprite.getU1();
        float sv0 = stillSprite.getV0();
        float sv1 = stillSprite.getV1();
        // Pixel-quantized fill: the wall band is cropped (never stretched) to whole
        // texture pixels, so each bucket amount has its own discrete slice of the sprite.
        float fill = Mth.clamp(heightPx / 16.0F, 0.0F, 1.0F);
        float stillBandBottom = sv0 + fill * (sv1 - sv0);

        int topAlpha = style.topAlpha();
        int sideAlpha = style.sideAlpha();
        VertexConsumer main = buffer.getBuffer(style.renderType());

        // Top and bottom faces: still texture, cropped to a square 1:1-pixel window
        // (the basin is smaller than a full block, so the full frame would look squeezed).
        float wOff = (1.0F - WALL_WINDOW) * 0.5F;
        float tw0 = su0 + wOff * (su1 - su0);
        float tw1 = su0 + (wOff + WALL_WINDOW) * (su1 - su0);
        float tv0 = sv0 + wOff * (sv1 - sv0);
        float tv1 = sv0 + (wOff + WALL_WINDOW) * (sv1 - sv0);

        vertex(main, pose, min, topY, max, tint, topAlpha, tw0, tv1, packedLight, 0.0F, 1.0F, 0.0F);
        vertex(main, pose, max, topY, max, tint, topAlpha, tw1, tv1, packedLight, 0.0F, 1.0F, 0.0F);
        vertex(main, pose, max, topY, min, tint, topAlpha, tw1, tv0, packedLight, 0.0F, 1.0F, 0.0F);
        vertex(main, pose, min, topY, min, tint, topAlpha, tw0, tv0, packedLight, 0.0F, 1.0F, 0.0F);

        // Bottom surface, visible through transparent liquids so the basin reads as filled volume.
        int bottomAlpha = Math.max(80, sideAlpha);
        vertex(main, pose, min, SYRUP_BOTTOM_Y, min, tint, bottomAlpha, tw0, tv0, packedLight, 0.0F, -1.0F, 0.0F);
        vertex(main, pose, max, SYRUP_BOTTOM_Y, min, tint, bottomAlpha, tw1, tv0, packedLight, 0.0F, -1.0F, 0.0F);
        vertex(main, pose, max, SYRUP_BOTTOM_Y, max, tint, bottomAlpha, tw1, tv1, packedLight, 0.0F, -1.0F, 0.0F);
        vertex(main, pose, min, SYRUP_BOTTOM_Y, max, tint, bottomAlpha, tw0, tv1, packedLight, 0.0F, -1.0F, 0.0F);

        if (stillness >= 0.999F) {
            // Fully at rest: still-textured walls with a fixed 1:1 crop window.
            for (float[] wall : WALLS) {
                drawWallWindow(main, pose, wall, topY, sideAlpha, su0, su1, sv0, stillBandBottom, packedLight, wall[8]);
            }
            return;
        }

        // Stirring: the crop window stays fixed while only the flowing texture frames
        // advance. Mirroring U per wall aligns the internal flow with the mixer's
        // tangential rotation without making the whole side texture orbit around the pot.
        TextureAtlasSprite flowSprite = atlas.apply(style.flowId());
        int frames = Math.max(1, flowSprite.contents().height() / flowSprite.contents().width());
        float fu0 = flowSprite.getU0();
        float fu1 = flowSprite.getU1();
        float fv0 = flowSprite.getV0();
        float frameH = (flowSprite.getV1() - fv0) / frames;
        int frameIndex = (int) (flowFrame % frames + frames) % frames;
        float bandTop = fv0 + frameIndex * frameH;
        float bandBottom = bandTop + fill * frameH;

        for (float[] wall : WALLS) {
            boolean reverseFlow = wall[7] < 0.0F;
            float directionalU0 = reverseFlow ? fu1 : fu0;
            float directionalU1 = reverseFlow ? fu0 : fu1;
            drawWallWindow(main, pose, wall, topY, sideAlpha, directionalU0, directionalU1,
                bandTop, bandBottom, packedLight, wall[8]);
        }

        if (stillness > 0.001F) {
            // Smooth crossfade back to the resting texture while the mixer eases to a stop.
            int fadeAlpha = (int) (sideAlpha * stillness);
            VertexConsumer fade = buffer.getBuffer(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS));
            for (float[] wall : WALLS) {
                drawWallWindow(fade, pose, wall, topY, fadeAlpha, su0, su1, sv0, stillBandBottom, packedLight, wall[8]);
            }
        }
    }

    /**
     * One inner wall with a pixel-square (1:1) crop window into the texture. The window
     * is {@link #WALL_WINDOW} wide, exactly matching the wall width in block pixels, so
     * texture pixels are never stretched. A window that crosses the frame edge is drawn
     * as two wrapped segments, but its position remains fixed while animation frames play.
     */
    private static void drawWallWindow(VertexConsumer consumer, PoseStack.Pose pose, float[] wall, float topY, int alpha,
            float uBase0, float uBase1, float vTop, float vBottom, int packedLight, float windowBase) {
        float w = uBase1 - uBase0;
        float o = ((windowBase % 1.0F) + 1.0F) % 1.0F;
        if (o + WALL_WINDOW <= 1.0F) {
            wallSegment(consumer, pose, wall, 0.0F, 1.0F, topY, alpha,
                uBase0 + o * w, uBase0 + (o + WALL_WINDOW) * w, vTop, vBottom, packedLight);
        } else {
            float split = (1.0F - o) / WALL_WINDOW;
            wallSegment(consumer, pose, wall, 0.0F, split, topY, alpha,
                uBase0 + o * w, uBase1, vTop, vBottom, packedLight);
            wallSegment(consumer, pose, wall, split, 1.0F, topY, alpha,
                uBase0, uBase0 + (o + WALL_WINDOW - 1.0F) * w, vTop, vBottom, packedLight);
        }
    }

    /** A vertical wall quad covering world-fraction [s0,s1] of the wall. */
    private static void wallSegment(VertexConsumer consumer, PoseStack.Pose pose, float[] wall,
            float s0, float s1, float topY, int alpha,
            float uAtS0, float uAtS1, float vTop, float vBottom, int packedLight) {
        float x0 = wall[0];
        float z0 = wall[1];
        float x1 = wall[2];
        float z1 = wall[3];
        float nx = wall[4];
        float ny = wall[5];
        float nz = wall[6];
        float ax = x0 + (x1 - x0) * s0;
        float az = z0 + (z1 - z0) * s0;
        float bx = x0 + (x1 - x0) * s1;
        float bz = z0 + (z1 - z0) * s1;
        int tint = 255;
        vertex(consumer, pose, ax, SYRUP_BOTTOM_Y, az, tint, alpha, uAtS0, vBottom, packedLight, nx, ny, nz);
        vertex(consumer, pose, bx, SYRUP_BOTTOM_Y, bz, tint, alpha, uAtS1, vBottom, packedLight, nx, ny, nz);
        vertex(consumer, pose, bx, topY, bz, tint, alpha, uAtS1, vTop, packedLight, nx, ny, nz);
        vertex(consumer, pose, ax, topY, az, tint, alpha, uAtS0, vTop, packedLight, nx, ny, nz);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float z,
            int tint, int alpha, float u, float v, int packedLight, float nx, float ny, float nz) {
        consumer.vertex(pose.pose(), x, y, z).color(tint, tint, tint, alpha)
            .uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight)
            .normal(pose.normal(), nx, ny, nz).endVertex();
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

    /** Liquid depth in whole texture pixels: one discrete slice per bucket amount. */
    private static int syrupBaseHeightPx(AlchemyTableBlockEntity blockEntity) {
        float baseHeight = Mth.lerp(blockEntity.getLiquidFillFraction(), SYRUP_BOTTOM_Y, MAX_SYRUP_TOP_Y);
        return Mth.clamp(Mth.floor((baseHeight - SYRUP_BOTTOM_Y) * 16.0F + 0.5F), 0, 16);
    }

    private float syrupTopY(AlchemyTableBlockEntity blockEntity, float time) {
        int amount = blockEntity.getDisplayedSyrupUnits();
        if (amount <= 0) {
            return SYRUP_BOTTOM_Y;
        }

        float baseHeight = SYRUP_BOTTOM_Y + syrupBaseHeightPx(blockEntity) / 16.0F;
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
            case GRENADINE -> new LiquidStyle(GRENADINE_STILL, GRENADINE_FLOW_STRIP, true, false, 170, 115);
            case WATER -> new LiquidStyle(WATER_STILL, WATER_FLOW_STRIP, true, false, 120, 80);
            case MILK -> new LiquidStyle(MILK_SPRITE, MILK_SPRITE, false, false, 255, 255);
            case CHOCOLATE -> new LiquidStyle(CHOCOLATE_STILL, CHOCOLATE_FLOW_STRIP, false, false, 255, 255);
            case LIQUID_CANDY -> new LiquidStyle(LIQUID_CANDY_STILL, LIQUID_CANDY_FLOW_STRIP, false, true, 255, 255);
            case LAVA -> new LiquidStyle(LAVA_STILL, LAVA_FLOW_STRIP, false, true, 255, 255);
            case CARAMEL -> new LiquidStyle(CARAMEL_STILL, CARAMEL_FLOW_STRIP, true, false, 176, 140);
            case NONE -> new LiquidStyle(null, null, true, false, 0, 0);
        };
    }

    private record LiquidStyle(ResourceLocation stillId, ResourceLocation flowId, boolean translucent, boolean fullBright, int topAlpha, int sideAlpha) {

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
