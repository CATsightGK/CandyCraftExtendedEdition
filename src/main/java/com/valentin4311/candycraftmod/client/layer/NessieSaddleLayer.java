package com.valentin4311.candycraftmod.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.NessieModel;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class NessieSaddleLayer extends RenderLayer<BasicCandyZombieEntity, NessieModel<BasicCandyZombieEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/nessiesaddle.png");
    private final NessieModel<BasicCandyZombieEntity> model;

    public NessieSaddleLayer(RenderLayerParent<BasicCandyZombieEntity, NessieModel<BasicCandyZombieEntity>> renderer, EntityModelSet modelSet) {
        super(renderer);
        this.model = new NessieModel<>(modelSet.bakeLayer(NessieModel.LAYER));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, BasicCandyZombieEntity entity,
            float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!entity.isNessieSaddled()) {
            return;
        }
        getParentModel().copyPropertiesTo(model);
        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        model.renderToBuffer(poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight,
            OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }
}
