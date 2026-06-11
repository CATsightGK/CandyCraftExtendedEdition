package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.SuguardModel;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SuguardRenderer extends MobRenderer<BasicCandyZombieEntity, SuguardModel<BasicCandyZombieEntity>> {
    private static final ResourceLocation SUGUARD = texture("sugarde.png");
    private static final ResourceLocation SOLDIER = texture("suguardesoldier.png");
    private static final ResourceLocation MAGE = texture("suguardemage.png");
    private static final ResourceLocation BOSS = texture("sugardeboss.png");

    public SuguardRenderer(EntityRendererProvider.Context context) {
        super(context, new SuguardModel<>(context.bakeLayer(SuguardModel.LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(BasicCandyZombieEntity entity) {
        if (entity.getType() == CCEntityTypes.MAGE_SUGUARD.get()) {
            return MAGE;
        }
        if (entity.getType() == CCEntityTypes.BOSS_SUGUARD.get()) {
            return BOSS;
        }
        return entity.getMainHandItem().is(CCItems.DYNAMITE.get()) ? SOLDIER : SUGUARD;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(CandyCraft.MODID, "textures/entity/" + name);
    }
}
