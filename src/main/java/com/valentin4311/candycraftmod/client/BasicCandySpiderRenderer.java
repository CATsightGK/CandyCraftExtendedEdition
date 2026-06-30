package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.BeetleModel;
import com.valentin4311.candycraftmod.entity.BasicCandySpiderEntity;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BasicCandySpiderRenderer extends MobRenderer<BasicCandySpiderEntity, BeetleModel<BasicCandySpiderEntity>> {
    public BasicCandySpiderRenderer(EntityRendererProvider.Context context) {
        super(context, new BeetleModel<>(context.bakeLayer(BeetleModel.LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(BasicCandySpiderEntity entity) {
        String texture = "beetle.png";
        if (entity.getType() == CCEntityTypes.BOSS_BEETLE.get()) texture = "bossbeetle.png";
        return new ResourceLocation(CandyCraft.MODID, "textures/entity/" + texture);
    }
}
