package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.CandyWolfEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Wolf;

public class CandyWolfRenderer extends WolfRenderer {
    private static final ResourceLocation WILD = new ResourceLocation(CandyCraft.MODID, "textures/entity/wolfcandy.png");
    private static final ResourceLocation ANGRY = new ResourceLocation(CandyCraft.MODID, "textures/entity/wolf_angrycandy.png");
    private static final ResourceLocation TAME = new ResourceLocation(CandyCraft.MODID, "textures/entity/wolf_tamecandy.png");
    private static final ResourceLocation TAME_READY = new ResourceLocation(CandyCraft.MODID, "textures/entity/wolf_tamecandy2.png");

    public CandyWolfRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Wolf entity) {
        if (!(entity instanceof CandyWolfEntity wolf)) {
            return WILD;
        }
        if (wolf.isTame()) {
            return wolf.getFurTime() < 1 ? TAME_READY : TAME;
        }
        return wolf.isAngry() ? ANGRY : WILD;
    }
}
