package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.GummyBunnyModel;
import com.valentin4311.candycraftmod.client.model.GummyBunnyOuterModel;
import com.valentin4311.candycraftmod.entity.GummyBunnyEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class GummyBunnyRenderer extends MobRenderer<GummyBunnyEntity, GummyBunnyModel<GummyBunnyEntity>> {
    private static final ResourceLocation FACE = new ResourceLocation(CandyCraft.MODID, "textures/entity/bunny.png");
    private static final ResourceLocation FUR = new ResourceLocation(CandyCraft.MODID, "textures/entity/whitebunny.png");

    public GummyBunnyRenderer(EntityRendererProvider.Context context) {
        super(context, new GummyBunnyModel<>(context.bakeLayer(GummyBunnyModel.LAYER)), 0.3F);
        addLayer(new FurLayer(this, context));
        addLayer(new GummyShellLayer(this, context));
    }

    @Override
    protected void scale(GummyBunnyEntity entity, PoseStack poseStack, float partialTickTime) {
        if (entity.isBaby()) {
            poseStack.scale(0.7F, 0.7F, 0.7F);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(GummyBunnyEntity entity) {
        return FACE;
    }

    @Nullable
    @Override
    protected RenderType getRenderType(GummyBunnyEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (entity.isSwampGummyVariant()) {
            // The swamp variant is drawn entirely by GummyShellLayer so the
            // body can be tinted with the entity's gummy color.
            return glowing ? RenderType.outline(getTextureLocation(entity)) : null;
        }
        if (bodyVisible || translucent) {
            return RenderType.entityTranslucent(getTextureLocation(entity));
        }
        return glowing ? RenderType.outline(getTextureLocation(entity)) : null;
    }

    private static final class FurLayer extends RenderLayer<GummyBunnyEntity, GummyBunnyModel<GummyBunnyEntity>> {
        private final GummyBunnyModel<GummyBunnyEntity> model;

        private FurLayer(GummyBunnyRenderer renderer, EntityRendererProvider.Context context) {
            super(renderer);
            this.model = new GummyBunnyModel<>(context.bakeLayer(GummyBunnyModel.LAYER));
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, GummyBunnyEntity bunny, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (bunny.isInvisible() || bunny.isSwampGummyVariant()) {
                return;
            }

            getParentModel().copyPropertiesTo(model);
            model.prepareMobModel(bunny, limbSwing, limbSwingAmount, partialTick);
            model.setupAnim(bunny, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(FUR));
            model.renderToBuffer(
                poseStack,
                consumer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(bunny, 0.0F),
                bunny.getRed() / 255.0F,
                bunny.getGreen() / 255.0F,
                bunny.getBlue() / 255.0F,
                0.58F
            );
        }
    }

    /**
     * Swamp variant uses the gummy mouse style: a tinted body underneath and
     * an inflated translucent shell on top, instead of the coplanar fur pass.
     */
    private static final class GummyShellLayer extends RenderLayer<GummyBunnyEntity, GummyBunnyModel<GummyBunnyEntity>> {
        private final GummyBunnyModel<GummyBunnyEntity> bodyModel;
        private final GummyBunnyOuterModel<GummyBunnyEntity> shellModel;

        private GummyShellLayer(GummyBunnyRenderer renderer, EntityRendererProvider.Context context) {
            super(renderer);
            this.bodyModel = new GummyBunnyModel<>(context.bakeLayer(GummyBunnyModel.LAYER));
            this.shellModel = new GummyBunnyOuterModel<>(context.bakeLayer(GummyBunnyOuterModel.LAYER));
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, GummyBunnyEntity bunny, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (bunny.isInvisible() || !bunny.isSwampGummyVariant()) {
                return;
            }

            float red = bunny.getRed() / 255.0F;
            float green = bunny.getGreen() / 255.0F;
            float blue = bunny.getBlue() / 255.0F;
            int overlay = LivingEntityRenderer.getOverlayCoords(bunny, 0.0F);

            getParentModel().copyPropertiesTo(bodyModel);
            bodyModel.prepareMobModel(bunny, limbSwing, limbSwingAmount, partialTick);
            bodyModel.setupAnim(bunny, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            bodyModel.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityTranslucent(FACE)),
                packedLight, overlay, red, green, blue, 1.0F);

            shellModel.prepareMobModel(bunny, limbSwing, limbSwingAmount, partialTick);
            shellModel.setupAnim(bunny, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            shellModel.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityTranslucent(FUR)),
                packedLight, overlay, red, green, blue, 0.6F);
        }
    }
}
