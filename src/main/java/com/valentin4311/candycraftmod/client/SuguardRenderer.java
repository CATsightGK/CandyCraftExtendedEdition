package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.SuguardModel;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import com.valentin4311.candycraftmod.client.layer.SuguardHeldItemLayer;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class SuguardRenderer extends MobRenderer<BasicCandyZombieEntity, SuguardModel<BasicCandyZombieEntity>> {
    private static final ResourceLocation SUGUARD = texture("sugarde.png");
    private static final ResourceLocation SOLDIER = texture("suguardesoldier.png");
    private static final ResourceLocation MAGE = texture("suguardemage.png");
    private static final ResourceLocation BOSS_AWAKE = texture("sugardeboss.png");
    private static final ResourceLocation BOSS_SLEEPING = texture("sugardeboss1.png");
    private static final ResourceLocation BOSS_STAT_1 = texture("sugardeboss2.png");
    private static final ResourceLocation BOSS_STAT_2 = texture("sugardeboss3.png");
    private static final ResourceLocation BOSS_STAT_3 = texture("sugardeboss4.png");

    public SuguardRenderer(EntityRendererProvider.Context context) {
        super(context, new SuguardModel<>(context.bakeLayer(SuguardModel.LAYER)), 0.5F);
        addLayer(new SuguardHeldItemLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public void render(BasicCandyZombieEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        shadowRadius = entity.getType() == CCEntityTypes.BOSS_SUGUARD.get() ? 0.55F : 0.26F;
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    protected void scale(BasicCandyZombieEntity entity, PoseStack poseStack, float partialTickTime) {
        if (entity.getType() == CCEntityTypes.BOSS_SUGUARD.get()) {
            poseStack.scale(2.0F, 2.0F, 2.0F);
        }
        super.scale(entity, poseStack, partialTickTime);
    }

    @Override
    public ResourceLocation getTextureLocation(BasicCandyZombieEntity entity) {
        if (entity.getType() == CCEntityTypes.MAGE_SUGUARD.get()) {
            return MAGE;
        }
        if (entity.getType() == CCEntityTypes.BOSS_SUGUARD.get()) {
            if (!entity.isBossSuguardAwake()) {
                return BOSS_SLEEPING;
            }
            return BOSS_AWAKE;
        }
        return entity.getMainHandItem().is(CCItems.DYNAMITE.get()) ? SOLDIER : SUGUARD;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(CandyCraft.MODID, "textures/entity/" + name);
    }
}
