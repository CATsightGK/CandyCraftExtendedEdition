package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.item.ForkItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.client.ForgeHooksClient;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ForkItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final ResourceLocation INVENTORY_MODEL = new ResourceLocation(CandyCraft.MODID, "item/fork_inventory");
    private static final ThreadLocal<Player> RENDERED_PLAYER = new ThreadLocal<>();
    private static final float MODEL_CENTER_X = 3.0F / 16.0F;
    private static final float MODEL_CENTER_Y = 13.875F / 16.0F;
    private static final float HELD_BLOCK_CENTER_Y = 36.0F / 16.0F;
    private static final float MAX_HELD_BLOCK_SIZE = 1.12F;
    private static final int SHINE_FRAME_COUNT = 24;
    private static final long SHINE_SWEEP_MILLIS = 2400L;
    private static final ResourceLocation[] SHINE_FRAMES = createShineFrames();
    private final ItemRenderer itemRenderer;

    public ForkItemRenderer(BlockEntityRenderDispatcher dispatcher) {
        super(dispatcher, Minecraft.getInstance().getEntityModels());
        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!isHandContext(context)) {
            BakedModel inventoryModel = Minecraft.getInstance().getModelManager().getModel(INVENTORY_MODEL);
            boolean leftHand = context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
            renderInventoryModel(stack, context, leftHand, inventoryModel, poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        poseStack.pushPose();
        if (context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            ForkClientAnimations.applyThirdPersonItemTransform(
                poseStack,
                stack,
                Minecraft.getInstance().getFrameTime()
            );
        }
        ForkGeometryRenderer.renderRaw(poseStack, buffer, packedLight, stack.hasFoil());
        renderHeldBlock(stack, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private void renderInventoryModel(ItemStack stack, ItemDisplayContext context, boolean leftHand,
            BakedModel model, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        // ItemRenderer has already translated the custom model by -0.5 on every axis.
        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        model = ForgeHooksClient.handleCameraTransforms(poseStack, model, context, leftHand);
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        for (BakedModel renderPass : model.getRenderPasses(stack, true)) {
            for (RenderType renderType : renderPass.getRenderTypes(stack, true)) {
                VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(
                    buffer,
                    renderType,
                    true,
                    stack.hasFoil() && !ForkItem.hasHeldBlock(stack)
                );
                itemRenderer.renderModelLists(
                    renderPass,
                    stack,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    consumer
                );
            }
        }
        if (context == ItemDisplayContext.GUI && ForkItem.hasHeldBlock(stack)) {
            renderInventoryShine(poseStack, buffer);
        }
        poseStack.popPose();
    }

    private static void renderInventoryShine(PoseStack poseStack, MultiBufferSource buffer) {
        int frame = (int)((Util.getMillis() % SHINE_SWEEP_MILLIS) * SHINE_FRAME_COUNT / SHINE_SWEEP_MILLIS);
        ResourceLocation texture = SHINE_FRAMES[frame];
        if (Minecraft.getInstance().getResourceManager().getResource(texture).isEmpty()) {
            return;
        }
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        PoseStack.Pose pose = poseStack.last();
        shineVertex(pose, consumer, 0.0F, 0.0F, 0.6F, 0.0F, 1.0F);
        shineVertex(pose, consumer, 1.0F, 0.0F, 0.6F, 1.0F, 1.0F);
        shineVertex(pose, consumer, 1.0F, 1.0F, 0.6F, 1.0F, 0.0F);
        shineVertex(pose, consumer, 0.0F, 1.0F, 0.6F, 0.0F, 0.0F);
    }

    private static void shineVertex(PoseStack.Pose pose, VertexConsumer consumer,
            float x, float y, float z, float u, float v) {
        consumer.vertex(pose.pose(), x, y, z)
            .color(255, 255, 255, 255)
            .uv(u, v)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(LightTexture.FULL_BRIGHT)
            .normal(pose.normal(), 0.0F, 0.0F, 1.0F)
            .endVertex();
    }

    private static ResourceLocation[] createShineFrames() {
        ResourceLocation[] frames = new ResourceLocation[SHINE_FRAME_COUNT];
        for (int frame = 0; frame < frames.length; frame++) {
            frames[frame] = new ResourceLocation(
                CandyCraft.MODID,
                "textures/item/fork_shine_" + frame + ".png"
            );
        }
        return frames;
    }

    private void renderHeldBlock(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (!ForkItem.hasHeldBlock(stack)) {
            return;
        }
        BlockState state = ForkItem.getHeldBlockState(stack);
        if (state.isAir()) {
            return;
        }

        BlockState lowerState = state;
        BlockState upperState = null;
        boolean plant = state.getBlock() instanceof BushBlock;
        VoxelShape shape = plant ? Shapes.block() : state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        if (state.getBlock() instanceof DoorBlock) {
            lowerState = state.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
            upperState = state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
            VoxelShape lowerShape = lowerState.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            VoxelShape upperShape = upperState.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).move(0.0D, 1.0D, 0.0D);
            shape = Shapes.or(lowerShape, upperShape);
        }
        if (shape.isEmpty()) {
            shape = Shapes.block();
        }

        AABB bounds = shape.bounds();
        double maxDimension = Math.max(bounds.getXsize(), Math.max(bounds.getYsize(), bounds.getZsize()));
        float scale = (float)(MAX_HELD_BLOCK_SIZE / Math.max(1.0D, maxDimension));
        poseStack.pushPose();
        poseStack.translate(
            MODEL_CENTER_X - scale * (bounds.minX + bounds.maxX) * 0.5D,
            HELD_BLOCK_CENTER_Y - scale * (bounds.minY + bounds.maxY) * 0.5D,
            -scale * (bounds.minZ + bounds.maxZ) * 0.5D
        );
        poseStack.scale(scale, scale, scale);
        renderHeldState(lowerState, plant, poseStack, buffer, packedLight);
        if (upperState != null) {
            poseStack.pushPose();
            poseStack.translate(0.0F, 1.0F, 0.0F);
            renderHeldState(upperState, false, poseStack, buffer, packedLight);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private void renderHeldState(BlockState state, boolean plant, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        Minecraft minecraft = Minecraft.getInstance();
        TextureAtlasSprite particle = minecraft.getBlockRenderer().getBlockModel(state).getParticleIcon();
        if (isMissingSprite(particle)) {
            renderHeldItemFallback(state, poseStack, buffer, packedLight);
            return;
        }
        if (plant) {
            renderPlantPlane(state, poseStack, buffer, packedLight);
            return;
        }
        minecraft.getBlockRenderer().renderSingleBlock(
            state,
            poseStack,
            buffer,
            packedLight,
            OverlayTexture.NO_OVERLAY
        );
    }

    private void renderHeldItemFallback(BlockState state, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        ItemStack item = new ItemStack(state.getBlock().asItem());
        if (item.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        itemRenderer.renderStatic(
            item,
            ItemDisplayContext.GROUND,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            poseStack,
            buffer,
            minecraft.level,
            0
        );
    }

    private static boolean isMissingSprite(TextureAtlasSprite sprite) {
        return sprite == null || MissingTextureAtlasSprite.getLocation().equals(sprite.contents().name());
    }

    private static void renderPlantPlane(BlockState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Minecraft minecraft = Minecraft.getInstance();
        TextureAtlasSprite sprite = minecraft.getBlockRenderer().getBlockModel(state).getParticleIcon();
        int tint = minecraft.getBlockColors().getColor(state, null, null, 0);
        if (tint == -1) {
            tint = 0xFFFFFF;
        }
        int red = tint >> 16 & 255;
        int green = tint >> 8 & 255;
        int blue = tint & 255;
        VertexConsumer consumer = buffer.getBuffer(RenderType.cutout());
        PoseStack.Pose pose = poseStack.last();
        float x = 0.5F;
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        plantVertex(pose, consumer, packedLight, x, 0, 0, u0, v1, 1, 0, 0, red, green, blue);
        plantVertex(pose, consumer, packedLight, x, 0, 1, u1, v1, 1, 0, 0, red, green, blue);
        plantVertex(pose, consumer, packedLight, x, 1, 1, u1, v0, 1, 0, 0, red, green, blue);
        plantVertex(pose, consumer, packedLight, x, 1, 0, u0, v0, 1, 0, 0, red, green, blue);
        plantVertex(pose, consumer, packedLight, x, 0, 1, u1, v1, -1, 0, 0, red, green, blue);
        plantVertex(pose, consumer, packedLight, x, 0, 0, u0, v1, -1, 0, 0, red, green, blue);
        plantVertex(pose, consumer, packedLight, x, 1, 0, u0, v0, -1, 0, 0, red, green, blue);
        plantVertex(pose, consumer, packedLight, x, 1, 1, u1, v0, -1, 0, 0, red, green, blue);
    }

    private static void plantVertex(PoseStack.Pose pose, VertexConsumer consumer, int packedLight,
            float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ,
            int red, int green, int blue) {
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        consumer.vertex(matrix, x, y, z)
            .color(red, green, blue, 255)
            .uv(u, v)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(packedLight)
            .normal(normal, normalX, normalY, normalZ)
            .endVertex();
    }

    static void beginPlayerRender(Player player) {
        RENDERED_PLAYER.set(player);
    }

    static void endPlayerRender() {
        RENDERED_PLAYER.remove();
    }

    private static boolean isHandContext(ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
            || context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
            || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
            || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }
}
