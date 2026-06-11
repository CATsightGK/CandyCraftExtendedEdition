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

public class MermaidModel<T extends BasicCandyZombieEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "mermaid"), "main");
    private final ModelPart root;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart tail;
    private final ModelPart fin;

    public MermaidModel(ModelPart root) {
        this.root = root;
        this.leftArm = root.getChild("left_arm");
        this.rightArm = root.getChild("right_arm");
        this.tail = root.getChild("tail");
        this.fin = root.getChild("fin");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 6).addBox(-3.0F, -12.0F, -2.0F, 6.0F, 4.0F, 4.0F), PartPose.offset(0.0F, 24.0F, -0.5F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(12, 0).addBox(-2.0F, -16.0F, -2.0F, 4.0F, 4.0F, 4.0F), PartPose.offset(0.0F, 24.0F, -0.5F));
        root.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(28, 0).addBox(-1.5F, -17.0F, -1.5F, 3.0F, 1.0F, 3.0F), PartPose.offset(0.0F, 24.0F, -0.5F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(20, 6).addBox(0.0F, -1.0F, -1.0F, 1.0F, 5.0F, 2.0F), PartPose.offsetAndRotation(3.0F, 14.0F, -0.5F, -1.570796F, 0.3F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(20, 6).addBox(-1.0F, -1.0F, -1.0F, 1.0F, 5.0F, 2.0F), PartPose.offsetAndRotation(-3.0F, 14.0F, -0.5F, -1.570796F, -0.6F, 0.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(44, 0).addBox(-3.0F, -1.0F, -1.5F, 6.0F, 8.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 18.0F, -1.0F, 0.35F, 0.0F, 0.0F));
        root.addOrReplaceChild("fin", CubeListBuilder.create().texOffs(14, 22).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 0.0F, 10.0F), PartPose.offsetAndRotation(0.0F, 17.0F, 7.5F, 0.58F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float sway = Mth.sin(ageInTicks * 0.18F) * 0.18F;
        tail.yRot = sway;
        fin.yRot = sway * 1.6F;
        leftArm.xRot = -1.45F + Mth.cos(limbSwing * 0.6662F) * 0.25F * limbSwingAmount;
        rightArm.xRot = -1.45F + Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 0.25F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
