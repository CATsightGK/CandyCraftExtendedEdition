package com.valentin4311.candycraftmod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class NessieModel<T extends BasicCandyZombieEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "nessie"), "main");
    private final ModelPart root;
    private final ModelPart flipperLeft;
    private final ModelPart flipperRight;
    private final ModelPart tail;

    public NessieModel(ModelPart root) {
        this.root = root;
        this.flipperLeft = root.getChild("flipper_left");
        this.flipperRight = root.getChild("flipper_right");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 33).addBox(-5.0F, -9.0F, -7.0F, 9.0F, 9.0F, 12.0F), PartPose.offset(0.0F, 23.0F, 0.0F));
        root.addOrReplaceChild("neck_low", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -12.0F, -10.0F, 5.0F, 5.0F, 5.0F), PartPose.offset(0.0F, 23.0F, 0.0F));
        root.addOrReplaceChild("neck_mid", CubeListBuilder.create().texOffs(0, 10).addBox(-2.5F, -18.0F, -14.0F, 5.0F, 6.0F, 5.0F), PartPose.offset(0.0F, 23.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(44, 1).addBox(-4.0F, -24.0F, -20.0F, 7.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 23.0F, 0.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 69).addBox(-3.0F, -7.0F, 4.0F, 5.0F, 5.0F, 14.0F), PartPose.offset(0.0F, 23.0F, 0.0F));
        root.addOrReplaceChild("flipper_left", CubeListBuilder.create().texOffs(24, 23).addBox(0.0F, -1.0F, -1.0F, 9.0F, 2.0F, 3.0F), PartPose.offsetAndRotation(3.0F, 22.0F, -5.0F, 0.0F, 0.58F, 0.19F));
        root.addOrReplaceChild("flipper_right", CubeListBuilder.create().texOffs(24, 18).addBox(-9.0F, -1.0F, -1.0F, 9.0F, 2.0F, 3.0F), PartPose.offsetAndRotation(-4.0F, 22.0F, -5.0F, 0.0F, -0.58F, -0.19F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        flipperLeft.yRot = 0.58F + Mth.cos(ageInTicks * 0.25F) * 0.25F;
        flipperRight.yRot = -0.58F - Mth.cos(ageInTicks * 0.25F) * 0.25F;
        tail.yRot = Mth.sin(ageInTicks * 0.18F) * 0.18F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
