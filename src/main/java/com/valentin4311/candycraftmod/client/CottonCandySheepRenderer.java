package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.CottonCandySheepEntity;
import net.minecraft.client.model.SheepFurModel;
import net.minecraft.client.model.SheepModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

public class CottonCandySheepRenderer extends MobRenderer<CottonCandySheepEntity, SheepModel<CottonCandySheepEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/candy_sheep/candy_sheep.png");
    private static final ResourceLocation FUR_TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/candy_sheep/candy_sheep_fur.png");

    public CottonCandySheepRenderer(EntityRendererProvider.Context context) {
        super(context, new SheepModel<>(context.bakeLayer(ModelLayers.SHEEP)), 0.7F);
        addLayer(new CottonFurLayer(this, context));
    }

    @Override
    public ResourceLocation getTextureLocation(CottonCandySheepEntity entity) {
        return TEXTURE;
    }

    private static final class CottonFurLayer extends RenderLayer<CottonCandySheepEntity, SheepModel<CottonCandySheepEntity>> {
        private final SheepFurModel<CottonCandySheepEntity> model;

        private CottonFurLayer(CottonCandySheepRenderer renderer, EntityRendererProvider.Context context) {
            super(renderer);
            this.model = new SheepFurModel<>(context.bakeLayer(ModelLayers.SHEEP_FUR));
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, CottonCandySheepEntity sheep,
                float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (sheep.isInvisible() || sheep.isSheared()) {
                return;
            }
            getParentModel().copyPropertiesTo(model);
            model.prepareMobModel(sheep, limbSwing, limbSwingAmount, partialTick);
            model.setupAnim(sheep, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(FUR_TEXTURE));
            model.renderToBuffer(poseStack, consumer, packedLight, LivingEntityRenderer.getOverlayCoords(sheep, 0.0F),
                1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
