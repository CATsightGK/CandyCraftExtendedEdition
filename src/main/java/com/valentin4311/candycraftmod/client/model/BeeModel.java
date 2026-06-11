package com.valentin4311.candycraftmod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.BasicCandySpiderEntity;
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

public class BeeModel<T extends BasicCandySpiderEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "caramel_bee"), "main");
    private final ModelPart stinger;
    private final ModelPart tail;
    private final ModelPart body;
    private final ModelPart abdomen;
    private final ModelPart head;
    private final ModelPart wingRight;
    private final ModelPart wingLeft;
    private final ModelPart legRight;
    private final ModelPart legLeft;
    private final ModelPart antennaBack;
    private final ModelPart antennaFront;

    public BeeModel(ModelPart root) {
        stinger = root.getChild("stinger");
        tail = root.getChild("tail");
        body = root.getChild("body");
        abdomen = root.getChild("abdomen");
        head = root.getChild("head");
        wingRight = root.getChild("wing_right");
        wingLeft = root.getChild("wing_left");
        legRight = root.getChild("leg_right");
        legLeft = root.getChild("leg_left");
        antennaBack = root.getChild("antenna_back");
        antennaFront = root.getChild("antenna_front");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("stinger", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 2.0F), PartPose.offset(-0.5F, 19.0F, 7.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(12, 22).addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(-1.0F, 18.5F, 5.2F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 8.0F), PartPose.offset(-2.0F, 17.5F, -3.0F));
        root.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(15, 12).addBox(0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 1.0F), PartPose.offset(-1.5F, 18.0F, 4.6F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 12).addBox(-2.5F, -3.5F, -4.0F, 5.0F, 5.0F, 5.0F), PartPose.offset(0.0F, 19.5F, -3.0F));
        root.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(24, 6).addBox(0.0F, 0.0F, 0.0F, 0.0F, 6.0F, 5.0F), PartPose.offsetAndRotation(1.4F, 18.0F, -1.5F, 0.0F, 0.0F, -1.570796F));
        root.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(24, 0).addBox(0.0F, 1.0F, 0.0F, 0.0F, 6.0F, 5.0F), PartPose.offsetAndRotation(-0.4F, 18.0F, -1.5F, 0.0F, 0.0F, 1.570796F));
        root.addOrReplaceChild("leg_right", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F), PartPose.offset(-2.7F, 20.5F, -1.0F));
        root.addOrReplaceChild("leg_left", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F), PartPose.offset(1.3F, 20.5F, -1.0F));
        root.addOrReplaceChild("antenna_back", CubeListBuilder.create().texOffs(0, 26).addBox(-1.0F, -7.0F, -2.0F, 6.0F, 4.0F, 0.0F), PartPose.offset(-2.0F, 19.5F, -1.0F));
        root.addOrReplaceChild("antenna_front", CubeListBuilder.create().texOffs(0, 22).addBox(-1.0F, -7.0F, -4.0F, 6.0F, 4.0F, 0.0F), PartPose.offset(-2.0F, 19.5F, -1.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float flap = Mth.sin(ageInTicks * 0.9F);
        wingRight.zRot = -1.570796F + flap;
        wingLeft.zRot = 1.570796F - flap;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        stinger.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        tail.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        abdomen.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        wingRight.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        wingLeft.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        legRight.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        legLeft.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        antennaBack.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        antennaFront.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
