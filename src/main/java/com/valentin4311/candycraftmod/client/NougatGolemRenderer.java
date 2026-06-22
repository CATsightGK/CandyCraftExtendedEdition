package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.NougatGolemModel;
import com.valentin4311.candycraftmod.entity.NougatGolemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class NougatGolemRenderer extends MobRenderer<NougatGolemEntity, NougatGolemModel<NougatGolemEntity>> {
    private static final ResourceLocation BODY_TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/nougatgolem2.png");
    private static final ResourceLocation HEAD_TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/nougatgolem.png");

    public NougatGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new NougatGolemModel<>(context.bakeLayer(NougatGolemModel.LAYER)), 0.8F);
    }

    @Override
    protected void scale(NougatGolemEntity entity, PoseStack poseStack, float partialTickTime) {
        float length = entity.getLength();
        poseStack.scale(length, length, length);
    }

    @Override
    public ResourceLocation getTextureLocation(NougatGolemEntity entity) {
        return entity.isTop() ? HEAD_TEXTURE : BODY_TEXTURE;
    }
}
