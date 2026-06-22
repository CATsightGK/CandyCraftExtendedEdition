package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.layer.NessieSaddleLayer;
import com.valentin4311.candycraftmod.client.model.NessieModel;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class NessieRenderer extends MobRenderer<BasicCandyZombieEntity, NessieModel<BasicCandyZombieEntity>> {
    private static final ResourceLocation[] TEXTURES = {
        new ResourceLocation(CandyCraft.MODID, "textures/entity/nessie0.png"),
        new ResourceLocation(CandyCraft.MODID, "textures/entity/nessie1.png"),
        new ResourceLocation(CandyCraft.MODID, "textures/entity/nessie2.png"),
        new ResourceLocation(CandyCraft.MODID, "textures/entity/nessie3.png"),
        new ResourceLocation(CandyCraft.MODID, "textures/entity/nessie4.png"),
        new ResourceLocation(CandyCraft.MODID, "textures/entity/nessie5.png"),
        new ResourceLocation(CandyCraft.MODID, "textures/entity/nessie6.png")
    };

    public NessieRenderer(EntityRendererProvider.Context context) {
        super(context, new NessieModel<>(context.bakeLayer(NessieModel.LAYER)), 0.8F);
        addLayer(new NessieSaddleLayer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(BasicCandyZombieEntity entity) {
        int variant = Math.max(0, Math.min(TEXTURES.length - 1, entity.getLegacyVariant()));
        return TEXTURES[variant];
    }
}
