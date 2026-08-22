package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.entity.PropolisSurfaceCarrier;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.monster.Slime;

/** Renders the propolis glint on the slime shell that is actually visible. */
public final class PropolisSlimeGlintLayer extends RenderLayer<Slime, SlimeModel<Slime>> {
    private final SlimeModel<Slime> outerModel;

    public PropolisSlimeGlintLayer(RenderLayerParent<Slime, SlimeModel<Slime>> parent,
            EntityModelSet modelSet) {
        super(parent);
        this.outerModel = new SlimeModel<>(modelSet.bakeLayer(ModelLayers.SLIME_OUTER));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Slime entity,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
            float netHeadYaw, float headPitch) {
        if (!(entity instanceof PropolisSurfaceCarrier carrier)
                || !carrier.candycraft$hasPropolisSurface()
                || entity.isInvisible()) {
            return;
        }

        getParentModel().copyPropertiesTo(outerModel);
        outerModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer consumer = buffer.getBuffer(PropolisRenderTypes.ENTITY_GLINT);
        outerModel.renderToBuffer(
            poseStack,
            consumer,
            LightTexture.FULL_BRIGHT,
            LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
            1.0F,
            1.0F,
            1.0F,
            1.0F
        );
    }
}
