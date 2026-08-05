package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.valentin4311.candycraftmod.entity.CandyStuckProjectileCarrier;
import com.valentin4311.candycraftmod.entity.HoneyArrowEntity;
import com.valentin4311.candycraftmod.entity.HoneyBoltEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.StuckInBodyLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;

public class CandyProjectileStuckLayer
        extends StuckInBodyLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private final EntityRenderDispatcher dispatcher;
    private int honeyArrowsToRender;
    private int renderedItems;

    public CandyProjectileStuckLayer(EntityRenderDispatcher dispatcher,
            LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
        this.dispatcher = dispatcher;
    }

    @Override
    protected int numStuck(AbstractClientPlayer player) {
        CandyStuckProjectileCarrier carrier = (CandyStuckProjectileCarrier)player;
        honeyArrowsToRender = carrier.candycraft$getHoneyArrowCount();
        renderedItems = 0;
        return honeyArrowsToRender + carrier.candycraft$getHoneyBoltCount();
    }

    @Override
    protected void renderStuckItem(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
            Entity target, float x, float y, float z, float partialTick) {
        AbstractArrow projectile;
        if (renderedItems++ < honeyArrowsToRender) {
            projectile = new HoneyArrowEntity(target.level(), target.getX(), target.getY(), target.getZ());
        } else {
            projectile = new HoneyBoltEntity(target.level(), target.getX(), target.getY(), target.getZ());
        }

        float horizontal = Mth.sqrt(x * x + z * z);
        projectile.setYRot((float)(Math.atan2(x, z) * Mth.RAD_TO_DEG));
        projectile.setXRot((float)(Math.atan2(y, horizontal) * Mth.RAD_TO_DEG));
        projectile.yRotO = projectile.getYRot();
        projectile.xRotO = projectile.getXRot();
        dispatcher.render(projectile, 0.0D, 0.0D, 0.0D, 0.0F, partialTick,
            poseStack, buffer, packedLight);
    }
}
