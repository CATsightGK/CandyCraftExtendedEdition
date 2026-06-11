package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.BeetleModel;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class KingBeetleRenderer extends MobRenderer<BasicCandyZombieEntity, BeetleModel<BasicCandyZombieEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/tamedbeetle.png");

    public KingBeetleRenderer(EntityRendererProvider.Context context) {
        super(context, new BeetleModel<>(context.bakeLayer(BeetleModel.LAYER)), 0.7F);
    }

    @Override
    public ResourceLocation getTextureLocation(BasicCandyZombieEntity entity) {
        return TEXTURE;
    }
}
