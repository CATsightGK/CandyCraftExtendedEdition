package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.layer.CottonCandySpiderEyesLayer;
import com.valentin4311.candycraftmod.entity.CottonCandySpiderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.resources.ResourceLocation;

public class CottonCandySpiderRenderer extends SpiderRenderer<CottonCandySpiderEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/cottonspider.png");

    public CottonCandySpiderRenderer(EntityRendererProvider.Context context) {
        super(context);
        layers.removeIf(SpiderEyesLayer.class::isInstance);
        addLayer(new CottonCandySpiderEyesLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(CottonCandySpiderEntity entity) {
        return TEXTURE;
    }
}
