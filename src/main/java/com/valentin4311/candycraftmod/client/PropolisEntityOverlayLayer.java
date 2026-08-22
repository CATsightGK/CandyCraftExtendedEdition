package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.entity.PropolisSurfaceCarrier;
import com.valentin4311.candycraftmod.mixin.client.VillagerModelAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;

/** CandyCraft's final surface pass, rendered after an entity's body and clothing layers. */
public final class PropolisEntityOverlayLayer<T extends LivingEntity, M extends EntityModel<T>>
        extends RenderLayer<T, M> {
    public PropolisEntityOverlayLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
            float netHeadYaw, float headPitch) {
        if (!(entity instanceof PropolisSurfaceCarrier carrier)
                || !carrier.candycraft$hasPropolisSurface()
                || entity.isInvisible()) {
            return;
        }

        EntityModel<T> model = getParentModel();
        ModelPart villagerHat = null;
        ModelPart villagerHatRim = null;
        boolean hatVisible = false;
        boolean hatRimVisible = false;
        if (model instanceof VillagerModel<?> villagerModel) {
            VillagerModelAccessor accessor = (VillagerModelAccessor)(Object)villagerModel;
            villagerHat = accessor.candycraft$getHat();
            villagerHatRim = accessor.candycraft$getHatRim();
            hatVisible = villagerHat.visible;
            hatRimVisible = villagerHatRim.visible;
            villagerHat.visible = false;
            villagerHatRim.visible = false;
        }
        try {
            VertexConsumer consumer = buffer.getBuffer(PropolisRenderTypes.ENTITY_GLINT);
            model.renderToBuffer(
                poseStack,
                consumer,
                LightTexture.FULL_BRIGHT,
                LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                1.0F,
                1.0F,
                1.0F,
                1.0F
            );
        } finally {
            if (villagerHat != null) {
                villagerHat.visible = hatVisible;
                villagerHatRim.visible = hatRimVisible;
            }
        }
    }
}
