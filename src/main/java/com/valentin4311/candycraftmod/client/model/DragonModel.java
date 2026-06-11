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

public class DragonModel<T extends BasicCandyZombieEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "dragon"), "main");
    private final ModelPart root;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart tail;

    public DragonModel(ModelPart root) {
        this.root = root;
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 14).addBox(-3.5F, -15.0F, -9.0F, 7.0F, 7.0F, 11.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
        root.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(63, 0).addBox(-2.0F, -24.0F, -17.0F, 4.0F, 15.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.65F, 0.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 32).addBox(-3.5F, -27.0F, -25.0F, 7.0F, 5.0F, 10.0F), PartPose.offset(0.5F, 24.0F, 0.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(62, 21).addBox(-2.5F, -13.0F, 1.0F, 5.0F, 5.0F, 20.0F), PartPose.offset(0.0F, 24.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(64, 32).addBox(0.0F, -1.0F, -1.0F, 3.0F, 9.0F, 3.0F), PartPose.offset(3.0F, 16.0F, -4.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(64, 32).addBox(-3.0F, -1.0F, -1.0F, 3.0F, 9.0F, 3.0F), PartPose.offset(-3.0F, 16.0F, -4.0F));
        root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(72, 0).addBox(0.0F, 0.0F, 0.0F, 21.0F, 0.0F, 14.0F), PartPose.offset(4.0F, 11.0F, -9.0F));
        root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(0, 47).addBox(-21.0F, 0.0F, 0.0F, 21.0F, 0.0F, 14.0F), PartPose.offset(-3.0F, 11.0F, -9.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float flap = (Mth.sin(ageInTicks * 0.3F) + 1.0F) * 0.55F;
        leftWing.zRot = -flap;
        rightWing.zRot = flap;
        leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
        rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * limbSwingAmount;
        tail.yRot = Mth.sin(ageInTicks * 0.13F) * 0.15F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
