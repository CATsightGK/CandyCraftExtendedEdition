package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.EasterChickenEntity;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class EasterChickenRenderer extends MobRenderer<EasterChickenEntity, ChickenModel<EasterChickenEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/easter_chicken/easter_chicken.png");

    public EasterChickenRenderer(EntityRendererProvider.Context context) {
        super(context, new ChickenModel<>(context.bakeLayer(ModelLayers.CHICKEN)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(EasterChickenEntity entity) {
        return TEXTURE;
    }

    @Override
    protected float getBob(EasterChickenEntity entity, float partialTicks) {
        float flap = Mth.lerp(partialTicks, entity.oFlap, entity.wingRotation);
        float flapSpeed = Mth.lerp(partialTicks, entity.oFlapSpeed, entity.destPos);
        return (Mth.sin(flap) + 1.0F) * flapSpeed;
    }
}
