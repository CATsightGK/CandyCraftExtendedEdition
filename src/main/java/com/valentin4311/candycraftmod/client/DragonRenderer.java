package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.DragonModel;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class DragonRenderer extends MobRenderer<BasicCandyZombieEntity, DragonModel<BasicCandyZombieEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/dragons.png");

    public DragonRenderer(EntityRendererProvider.Context context) {
        super(context, new DragonModel<>(context.bakeLayer(DragonModel.LAYER)), 0.8F);
    }

    @Override
    public ResourceLocation getTextureLocation(BasicCandyZombieEntity entity) {
        return TEXTURE;
    }
}
