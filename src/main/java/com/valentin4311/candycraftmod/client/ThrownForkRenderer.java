package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.entity.ThrownForkEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class ThrownForkRenderer extends EntityRenderer<ThrownForkEntity> {
    private static final float EMBEDDED_RETRACTION = 1.3828125F;

    public ThrownForkRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(ThrownForkEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        Vec3 direction = entity.getDeltaMovement();
        if (direction.lengthSqr() < 1.0E-7D) {
            float yaw = entity.getYRot() * ((float)Math.PI / 180.0F);
            float pitch = entity.getXRot() * ((float)Math.PI / 180.0F);
            direction = new Vec3(
                -Math.sin(yaw) * Math.cos(pitch),
                -Math.sin(pitch),
                Math.cos(yaw) * Math.cos(pitch)
            );
        }
        direction = direction.normalize();
        float yaw = (float)Math.toDegrees(Math.atan2(direction.x, direction.z));
        float pitch = (float)Math.toDegrees(Math.asin(-direction.y));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F + pitch));
        poseStack.scale(0.45F, 0.45F, 0.45F);
        if (entity.isEmbeddedInBlock()) {
            poseStack.translate(0.0F, -EMBEDDED_RETRACTION, 0.0F);
        }
        ForkGeometryRenderer.renderCentered(poseStack, buffer, packedLight);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownForkEntity entity) {
        return ForkGeometryRenderer.TEXTURE;
    }
}
