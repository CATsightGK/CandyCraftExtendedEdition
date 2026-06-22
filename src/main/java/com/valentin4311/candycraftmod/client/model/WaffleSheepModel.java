package com.valentin4311.candycraftmod.client.model;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.WaffleSheepEntity;
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

public class WaffleSheepModel<T extends WaffleSheepEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "waffle_sheep"), "main");
    public static final ModelLayerLocation FUR_LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "waffle_sheep"), "fur");

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;

    public WaffleSheepModel(ModelPart root) {
        head = root.getChild("head");
        body = root.getChild("body");
        rightHindLeg = root.getChild("right_hind_leg");
        leftHindLeg = root.getChild("left_hind_leg");
        rightFrontLeg = root.getChild("right_front_leg");
        leftFrontLeg = root.getChild("left_front_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-3.0F, -4.0F, -6.0F, 6.0F, 6.0F, 8.0F),
            PartPose.offset(0.0F, 6.0F, -8.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(28, 8)
            .addBox(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F),
            PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, ((float)Math.PI / 2.0F), 0.0F, 0.0F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
            .texOffs(0, 16)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
            PartPose.offset(-3.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
            .texOffs(0, 16)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
            PartPose.offset(3.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
            .texOffs(0, 16)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
            PartPose.offset(-3.0F, 12.0F, -5.0F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
            .texOffs(0, 16)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
            PartPose.offset(3.0F, 12.0F, -5.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition createFurLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-3.0F, -4.0F, -4.0F, 6.0F, 6.0F, 6.0F, new net.minecraft.client.model.geom.builders.CubeDeformation(0.6F)),
            PartPose.offset(0.0F, 6.0F, -8.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(28, 8)
            .addBox(-4.0F, -10.0F, -7.0F, 8.0F, 16.0F, 6.0F, new net.minecraft.client.model.geom.builders.CubeDeformation(1.75F)),
            PartPose.offsetAndRotation(0.0F, 5.0F, 2.0F, ((float)Math.PI / 2.0F), 0.0F, 0.0F));
        root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
            .texOffs(0, 16)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new net.minecraft.client.model.geom.builders.CubeDeformation(0.5F)),
            PartPose.offset(-3.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
            .texOffs(0, 16)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new net.minecraft.client.model.geom.builders.CubeDeformation(0.5F)),
            PartPose.offset(3.0F, 12.0F, 7.0F));
        root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
            .texOffs(0, 16)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new net.minecraft.client.model.geom.builders.CubeDeformation(0.5F)),
            PartPose.offset(-3.0F, 12.0F, -5.0F));
        root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
            .texOffs(0, 16)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 2.0F, 4.0F, new net.minecraft.client.model.geom.builders.CubeDeformation(0.5F)),
            PartPose.offset(3.0F, 12.0F, -5.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        head.xRot = headPitch * ((float)Math.PI / 180.0F);
        head.yRot = netHeadYaw * ((float)Math.PI / 180.0F);
        rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer buffer,
            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightHindLeg.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftHindLeg.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightFrontLeg.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftFrontLeg.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
