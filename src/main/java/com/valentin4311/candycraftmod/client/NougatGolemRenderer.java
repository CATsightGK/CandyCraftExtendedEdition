package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.NougatGolemModel;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class NougatGolemRenderer extends MobRenderer<BasicCandyZombieEntity, NougatGolemModel<BasicCandyZombieEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/nougatgolem2.png");

    public NougatGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new NougatGolemModel<>(context.bakeLayer(NougatGolemModel.LAYER)), 0.8F);
    }

    @Override
    public ResourceLocation getTextureLocation(BasicCandyZombieEntity entity) {
        return TEXTURE;
    }
}
