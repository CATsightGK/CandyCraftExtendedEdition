package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.PingouinModel;
import com.valentin4311.candycraftmod.entity.PingouinEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class PingouinRenderer extends MobRenderer<PingouinEntity, PingouinModel<PingouinEntity>> {
    private static final ResourceLocation[] TEXTURES = {
        new ResourceLocation(CandyCraft.MODID, "textures/entity/pingouin0.png"),
        new ResourceLocation(CandyCraft.MODID, "textures/entity/pingouin1.png"),
        new ResourceLocation(CandyCraft.MODID, "textures/entity/pingouin2.png")
    };

    public PingouinRenderer(EntityRendererProvider.Context context) {
        super(context, new PingouinModel<>(context.bakeLayer(PingouinModel.LAYER)), 0.5F);
    }

    @Override
    protected void scale(PingouinEntity entity, PoseStack poseStack, float partialTickTime) {
        if (entity.isBaby()) {
            poseStack.scale(0.5F, 0.5F, 0.5F);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(PingouinEntity entity) {
        return TEXTURES[Math.max(0, Math.min(TEXTURES.length - 1, entity.getColor()))];
    }
}
