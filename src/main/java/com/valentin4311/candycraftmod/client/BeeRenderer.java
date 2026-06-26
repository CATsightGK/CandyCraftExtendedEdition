package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.BeeModel;
import com.valentin4311.candycraftmod.entity.CaramelBeeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BeeRenderer extends MobRenderer<CaramelBeeEntity, BeeModel<CaramelBeeEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/bee.png");

    public BeeRenderer(EntityRendererProvider.Context context) {
        super(context, new BeeModel<>(context.bakeLayer(BeeModel.LAYER)), 0.22F);
    }

    @Override
    public ResourceLocation getTextureLocation(CaramelBeeEntity entity) {
        return TEXTURE;
    }
}
