package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.BeeModel;
import com.valentin4311.candycraftmod.entity.BasicCandySpiderEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BeeRenderer extends MobRenderer<BasicCandySpiderEntity, BeeModel<BasicCandySpiderEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/bee.png");

    public BeeRenderer(EntityRendererProvider.Context context) {
        super(context, new BeeModel<>(context.bakeLayer(BeeModel.LAYER)), 0.35F);
    }

    @Override
    public ResourceLocation getTextureLocation(BasicCandySpiderEntity entity) {
        return TEXTURE;
    }
}
