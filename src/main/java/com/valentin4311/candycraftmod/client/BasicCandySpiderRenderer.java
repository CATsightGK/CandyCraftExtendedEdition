package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.BasicCandySpiderEntity;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.resources.ResourceLocation;

public class BasicCandySpiderRenderer extends SpiderRenderer<BasicCandySpiderEntity> {
    public BasicCandySpiderRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(BasicCandySpiderEntity entity) {
        String texture = "beetle.png";
        if (entity.getType() == CCEntityTypes.BOSS_BEETLE.get()) texture = "bossbeetle.png";
        return new ResourceLocation(CandyCraft.MODID, "textures/entity/" + texture);
    }
}
