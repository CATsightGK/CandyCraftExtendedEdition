package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.NessieModel;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class NessieRenderer extends MobRenderer<BasicCandyZombieEntity, NessieModel<BasicCandyZombieEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/nessie0.png");

    public NessieRenderer(EntityRendererProvider.Context context) {
        super(context, new NessieModel<>(context.bakeLayer(NessieModel.LAYER)), 0.8F);
    }

    @Override
    public ResourceLocation getTextureLocation(BasicCandyZombieEntity entity) {
        return TEXTURE;
    }
}
