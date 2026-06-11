package com.valentin4311.candycraftmod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.GummyBunnyEntity;
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

public class GummyBunnyModel<T extends GummyBunnyEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "gummy_bunny"), "main");

    private final ModelPart frontLegRight;
    private final ModelPart frontLegLeft;
    private final ModelPart backLegRight;
    private final ModelPart backLegLeft;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart tail;
    private final ModelPart whiskers;
    private final ModelPart earLeft;
    private final ModelPart earRight;

    public GummyBunnyModel(ModelPart root) {
        this.frontLegRight = root.getChild("front_leg_right");
        this.frontLegLeft = root.getChild("front_leg_left");
        this.backLegRight = root.getChild("back_leg_right");
        this.backLegLeft = root.getChild("back_leg_left");
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.tail = root.getChild("tail");
        this.whiskers = root.getChild("whiskers");
        this.earLeft = root.getChild("ear_left");
        this.earRight = root.getChild("ear_right");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("front_leg_right", CubeListBuilder.create().texOffs(12, 0).addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(1.0F, 22.0F, -3.0F));
        root.addOrReplaceChild("front_leg_left", CubeListBuilder.create().texOffs(12, 0).addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(-3.0F, 22.0F, -3.0F));
        root.addOrReplaceChild("back_leg_right", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, -3.0F, 2.0F, 2.0F, 3.0F), PartPose.offset(1.0F, 22.0F, 4.0F));
        root.addOrReplaceChild("back_leg_left", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, -3.0F, 2.0F, 2.0F, 3.0F), PartPose.offset(-3.0F, 22.0F, 4.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 9).addBox(0.0F, 0.0F, 0.0F, 6.0F, 6.0F, 8.0F), PartPose.offset(-3.0F, 16.0F, -4.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(20, 7).addBox(-2.0F, -2.0F, -4.0F, 4.0F, 4.0F, 6.0F), PartPose.offset(0.0F, 19.0F, -4.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(20, 0).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 3.0F), PartPose.offset(-2.0F, 17.0F, 4.0F));
        root.addOrReplaceChild("whiskers", CubeListBuilder.create().texOffs(0, 6).addBox(-3.5F, -1.0F, -3.0F, 7.0F, 3.0F, 0.0F), PartPose.offset(0.0F, 19.0F, -4.0F));
        root.addOrReplaceChild("ear_left", CubeListBuilder.create().texOffs(14, 4).addBox(-1.8F, -4.8F, -3.0F, 1.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 19.0F, -4.0F, 0.0F, 0.0F, -0.0872665F));
        root.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(14, 4).addBox(0.8F, -4.8F, -3.0F, 1.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 19.0F, -4.0F, 0.0F, 0.0F, 0.0872665F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float pitch = headPitch * ((float)Math.PI / 180F);
        float yaw = netHeadYaw * ((float)Math.PI / 180F);
        head.xRot = pitch;
        head.yRot = yaw;
        whiskers.xRot = pitch;
        whiskers.yRot = yaw;
        earLeft.xRot = pitch;
        earLeft.yRot = yaw;
        earRight.xRot = pitch;
        earRight.yRot = yaw;

        if (!entity.onGround()) {
            backLegRight.xRot = 0.4F;
            backLegLeft.xRot = 0.4F;
        } else {
            backLegRight.xRot = 0.0F;
            backLegLeft.xRot = 0.0F;
        }

        frontLegRight.xRot = Mth.cos(limbSwing * 0.6662F) * 0.9F * limbSwingAmount;
        frontLegLeft.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 0.9F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        frontLegRight.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        frontLegLeft.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        backLegRight.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        backLegLeft.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        tail.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        whiskers.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        earLeft.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        earRight.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
