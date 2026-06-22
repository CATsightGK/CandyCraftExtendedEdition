package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.WaffleSheepModel;
import com.valentin4311.candycraftmod.entity.WaffleSheepEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class WaffleSheepRenderer extends MobRenderer<WaffleSheepEntity, WaffleSheepModel<WaffleSheepEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/sheepcandy0.png");
    private static final ResourceLocation HURT_TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/sheepcandy1.png");
    private static final ResourceLocation FUR_TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/sheepcandy.png");

    public WaffleSheepRenderer(EntityRendererProvider.Context context) {
        super(context, new WaffleSheepModel<>(context.bakeLayer(WaffleSheepModel.LAYER)), 0.7F);
        addLayer(new FurLayer(this, context));
    }

    @Override
    public ResourceLocation getTextureLocation(WaffleSheepEntity entity) {
        return entity.hurtTime > 0 ? HURT_TEXTURE : TEXTURE;
    }

    private static final class FurLayer extends RenderLayer<WaffleSheepEntity, WaffleSheepModel<WaffleSheepEntity>> {
        private final WaffleSheepModel<WaffleSheepEntity> model;

        private FurLayer(WaffleSheepRenderer renderer, EntityRendererProvider.Context context) {
            super(renderer);
            this.model = new WaffleSheepModel<>(context.bakeLayer(WaffleSheepModel.FUR_LAYER));
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, WaffleSheepEntity sheep,
                float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (sheep.isInvisible()) {
                return;
            }

            getParentModel().copyPropertiesTo(model);
            model.prepareMobModel(sheep, limbSwing, limbSwingAmount, partialTick);
            model.setupAnim(sheep, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(FUR_TEXTURE));
            model.renderToBuffer(
                poseStack,
                consumer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(sheep, 0.0F),
                1.0F,
                1.0F,
                1.0F,
                1.0F
            );
        }
    }
}
