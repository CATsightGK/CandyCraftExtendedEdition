package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.layer.MermaidHeldItemLayer;
import com.valentin4311.candycraftmod.client.model.MermaidModel;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class MermaidRenderer extends MobRenderer<BasicCandyZombieEntity, MermaidModel<BasicCandyZombieEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/entity/mermaid.png");

    public MermaidRenderer(EntityRendererProvider.Context context) {
        super(context, new MermaidModel<>(context.bakeLayer(MermaidModel.LAYER)), 0.5F);
        addLayer(new MermaidHeldItemLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(BasicCandyZombieEntity entity) {
        return TEXTURE;
    }
}
