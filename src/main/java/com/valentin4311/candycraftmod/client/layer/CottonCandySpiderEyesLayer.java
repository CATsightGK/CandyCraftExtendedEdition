package com.valentin4311.candycraftmod.client.layer;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.CottonCandySpiderEntity;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class CottonCandySpiderEyesLayer extends EyesLayer<CottonCandySpiderEntity, SpiderModel<CottonCandySpiderEntity>> {
    private static final RenderType PINK_GLOWING_EYES = RenderType.eyes(
        new ResourceLocation(CandyCraft.MODID, "textures/entity/cottonspider_eyes.png"));

    public CottonCandySpiderEyesLayer(
            RenderLayerParent<CottonCandySpiderEntity, SpiderModel<CottonCandySpiderEntity>> parent) {
        super(parent);
    }

    @Override
    public RenderType renderType() {
        return PINK_GLOWING_EYES;
    }
}
