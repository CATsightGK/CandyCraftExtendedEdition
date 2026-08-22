package com.valentin4311.candycraftmod.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.model.SuguardModel;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class SuguardGemGlowLayer
        extends RenderLayer<BasicCandyZombieEntity, SuguardModel<BasicCandyZombieEntity>> {
    private static final ResourceLocation SUGUARD = texture("sugarde_glow.png");
    private static final ResourceLocation SOLDIER = texture("suguardesoldier_glow.png");
    private static final ResourceLocation MAGE = texture("suguardemage_glow.png");
    private static final ResourceLocation BOSS_AWAKE = texture("sugardeboss_glow.png");
    private static final ResourceLocation BOSS_SLEEPING = texture("sugardeboss1_glow.png");
    private static final ResourceLocation BOSS_STAT_1 = texture("sugardeboss2_glow.png");
    private static final ResourceLocation BOSS_STAT_2 = texture("sugardeboss3_glow.png");
    private static final ResourceLocation BOSS_STAT_3 = texture("sugardeboss4_glow.png");

    public SuguardGemGlowLayer(
            RenderLayerParent<BasicCandyZombieEntity, SuguardModel<BasicCandyZombieEntity>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            BasicCandyZombieEntity entity, float limbSwing, float limbSwingAmount,
            float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.eyes(textureFor(entity)));
        getParentModel().renderToBuffer(poseStack, consumer, LightTexture.FULL_BRIGHT,
            OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static ResourceLocation textureFor(BasicCandyZombieEntity entity) {
        if (entity.getType() == CCEntityTypes.MAGE_SUGUARD.get()) {
            return MAGE;
        }
        if (entity.getType() == CCEntityTypes.BOSS_SUGUARD.get()) {
            if (!entity.isBossSuguardAwake()) {
                return BOSS_SLEEPING;
            }
            return switch (entity.getBossSuguardStat()) {
                case 1 -> BOSS_STAT_1;
                case 2 -> BOSS_STAT_2;
                case 3 -> BOSS_STAT_3;
                default -> BOSS_AWAKE;
            };
        }
        return entity.isChocolateForestSuguard() || entity.getMainHandItem().is(CCItems.DYNAMITE.get())
            ? SOLDIER
            : SUGUARD;
    }

    private static ResourceLocation texture(String name) {
        return new ResourceLocation(CandyCraft.MODID, "textures/entity/" + name);
    }
}
