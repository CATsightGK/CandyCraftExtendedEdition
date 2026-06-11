package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.GingerbreadManEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class GingerbreadManRenderer extends MobRenderer<GingerbreadManEntity, GingerbreadManModel<GingerbreadManEntity>> {
    private static final ResourceLocation[] TEXTURES = {
        new ResourceLocation(CandyCraft.MODID, "textures/entity/gingerbread0.png"),
        new ResourceLocation(CandyCraft.MODID, "textures/entity/gingerbread1.png"),
        new ResourceLocation(CandyCraft.MODID, "textures/entity/gingerbread2.png"),
        new ResourceLocation(CandyCraft.MODID, "textures/entity/gingerbread3.png")
    };

    public GingerbreadManRenderer(EntityRendererProvider.Context context) {
        super(context, new GingerbreadManModel<>(context.bakeLayer(GingerbreadManModel.LAYER)), 0.25F);
    }

    @Override
    protected void scale(GingerbreadManEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(0.5F, 0.5F, 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(GingerbreadManEntity entity) {
        return TEXTURES[Math.max(0, Math.min(TEXTURES.length - 1, entity.getSkinVariant()))];
    }
}
