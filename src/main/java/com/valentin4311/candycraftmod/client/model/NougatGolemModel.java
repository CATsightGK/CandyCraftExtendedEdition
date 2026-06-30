package com.valentin4311.candycraftmod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.NougatGolemEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class NougatGolemModel<T extends NougatGolemEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "nougat_golem"), "main");
    private final ModelPart cube;

    public NougatGolemModel(ModelPart root) {
        this.cube = root.getChild("cube");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("cube",
            CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -16.0F, -8.0F, 16.0F, 16.0F, 16.0F),
            PartPose.offset(0.0F, 24.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        cube.xRot = 0.0F;
        cube.yRot = 0.0F;
        cube.zRot = 0.0F;
        cube.y = 24.0F;

        int stackIndex = entity.getVisualStackIndex();
        boolean top = entity.isTop();
        int mode = entity.getMode();
        if (mode == 0 && !top) {
            float direction = (stackIndex & 1) == 0 ? 1.0F : -1.0F;
            cube.yRot = direction * (ageInTicks * 0.135F + stackIndex * 0.62F);
        } else if (mode != 0 && entity.isStackMoving()) {
            float phase = ageInTicks * 0.34F + stackIndex * 0.72F;
            float stride = Math.min(1.0F, limbSwingAmount * 3.2F + 0.35F);
            cube.xRot = Mth.sin(phase) * 0.075F * stride;
            cube.zRot = Mth.cos(phase * 0.85F) * 0.09F * stride;
            cube.y = 24.0F - Math.abs(Mth.sin(phase)) * 0.65F * stride;
        }

        float swing = entity.getAttackSwingProgress(0.0F);
        if (swing > 0.0F) {
            float progress = 1.0F - Mth.clamp(swing, 0.0F, 1.0F);
            float windup = Mth.sin(progress * (float)Math.PI);
            float impact = Mth.sin(Mth.clamp(progress * 1.35F, 0.0F, 1.0F) * (float)Math.PI);
            float wave = 1.0F - stackIndex * 0.055F;
            cube.xRot += -0.34F * impact * wave;
            cube.zRot += Mth.sin(ageInTicks * 0.9F + stackIndex * 0.55F) * 0.13F * windup;
            cube.y += -1.8F * impact * wave;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        cube.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
