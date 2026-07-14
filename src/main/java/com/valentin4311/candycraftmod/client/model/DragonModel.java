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
    private final ModelPart leftScaleWing;
    private final ModelPart rightScaleWing;
    private final ModelPart leftFrontLegTop;
    private final ModelPart rightFrontLegTop;
    private final ModelPart leftBackLegTop;
    private final ModelPart rightBackLegTop;
    private final ModelPart leftFrontLegBase;
    private final ModelPart rightFrontLegBase;
    private final ModelPart leftBackLegBase;
    private final ModelPart rightBackLegBase;

    public DragonModel(ModelPart root) {
        this.root = root;
        this.leftWing = root.getChild("left_wing");
        this.rightWing = root.getChild("right_wing");
        this.leftScaleWing = root.getChild("left_scale_wing");
        this.rightScaleWing = root.getChild("right_scale_wing");
        this.leftFrontLegTop = root.getChild("left_front_leg_top");
        this.rightFrontLegTop = root.getChild("right_front_leg_top");
        this.leftBackLegTop = root.getChild("left_back_leg_top");
        this.rightBackLegTop = root.getChild("right_back_leg_top");
        this.leftFrontLegBase = leftFrontLegTop.getChild("left_front_leg_base");
        this.rightFrontLegBase = rightFrontLegTop.getChild("right_front_leg_base");
        this.leftBackLegBase = leftBackLegTop.getChild("left_back_leg_base");
        this.rightBackLegBase = rightBackLegTop.getChild("right_back_leg_base");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body1", CubeListBuilder.create().texOffs(0, 14).addBox(0.0F, 0.0F, 0.0F, 7.0F, 7.0F, 11.0F), PartPose.offset(-3.0F, 9.0F, -11.0F));
        root.addOrReplaceChild("neck1", CubeListBuilder.create().texOffs(63, 0).addBox(0.0F, 0.0F, 0.0F, 4.0F, 15.0F, 3.0F), PartPose.offsetAndRotation(-1.5F, 4.0F, -19.0F, 0.6457718F, 0.0F, 0.0F));
        root.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(36, 19).addBox(0.0F, 0.0F, 0.0F, 6.0F, 6.0F, 7.0F), PartPose.offsetAndRotation(-2.5F, 9.5F, 0.0F, -0.1396263F, 0.0F, 0.0F));
        root.addOrReplaceChild("body3", CubeListBuilder.create().texOffs(62, 21).addBox(0.0F, 0.0F, 0.0F, 5.0F, 5.0F, 6.0F), PartPose.offsetAndRotation(-2.0F, 11.0F, 6.8F, -0.2617994F, 0.0F, 0.0F));
        root.addOrReplaceChild("body4", CubeListBuilder.create().texOffs(99, 14).addBox(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 5.0F), PartPose.offsetAndRotation(-1.5F, 13.0F, 12.0F, -0.3839724F, 0.0F, 0.0F));
        root.addOrReplaceChild("body5", CubeListBuilder.create().texOffs(26, 18).addBox(0.0F, 0.0F, 0.0F, 3.0F, 3.0F, 4.0F), PartPose.offsetAndRotation(-1.0F, 15.0F, 16.0F, -0.2094395F, 0.0F, 0.0F));
        root.addOrReplaceChild("body6", CubeListBuilder.create().texOffs(99, 23).addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 5.0F), PartPose.offsetAndRotation(-0.5F, 16.5F, 19.0F, -0.0371786F, 0.0F, 0.0F));
        root.addOrReplaceChild("body7", CubeListBuilder.create().texOffs(56, 21).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 17.2F, 23.5F, 0.2602503F, 0.0F, 0.0F));
        root.addOrReplaceChild("neck2", CubeListBuilder.create().texOffs(85, 14).addBox(0.0F, 0.0F, 0.0F, 5.0F, 10.0F, 2.0F), PartPose.offsetAndRotation(-2.0F, 3.0F, -17.0F, 0.6457718F, 0.0F, 0.0F));

        PartDefinition rightFrontLegTop = root.addOrReplaceChild("right_front_leg_top", CubeListBuilder.create().texOffs(64, 32).addBox(0.0F, 0.0F, 0.0F, 3.0F, 7.0F, 3.0F), PartPose.offsetAndRotation(-5.5F, 12.0F, -10.0F, 0.4059698F, 0.0F, 0.0F));
        PartDefinition leftFrontLegTop = root.addOrReplaceChild("left_front_leg_top", CubeListBuilder.create().texOffs(64, 32).addBox(0.0F, 0.0F, 0.0F, 3.0F, 7.0F, 3.0F), PartPose.offsetAndRotation(3.5F, 12.0F, -10.0F, 0.4059698F, 0.0F, 0.0F));
        PartDefinition rightBackLegTop = root.addOrReplaceChild("right_back_leg_top", CubeListBuilder.create().texOffs(64, 32).addBox(0.0F, 0.0F, 0.0F, 3.0F, 7.0F, 3.0F), PartPose.offsetAndRotation(-5.0F, 13.0F, 3.0F, 0.4059698F, 0.0F, 0.0F));
        PartDefinition leftBackLegTop = root.addOrReplaceChild("left_back_leg_top", CubeListBuilder.create().texOffs(64, 32).addBox(0.0F, 0.0F, 0.0F, 3.0F, 7.0F, 3.0F), PartPose.offsetAndRotation(3.0F, 13.0F, 3.0F, 0.4059698F, 0.0F, 0.0F));

        PartDefinition rightFrontLegBase = rightFrontLegTop.addOrReplaceChild("right_front_leg_base", CubeListBuilder.create().texOffs(117, 14).addBox(0.0F, 0.0F, 0.0F, 2.0F, 7.0F, 2.0F), PartPose.offsetAndRotation(0.9F, 4.3F, 1.4F, -0.7550357F, 0.0F, 0.0F));
        PartDefinition leftFrontLegBase = leftFrontLegTop.addOrReplaceChild("left_front_leg_base", CubeListBuilder.create().texOffs(117, 14).addBox(0.0F, 0.0F, 0.0F, 2.0F, 7.0F, 2.0F), PartPose.offsetAndRotation(0.1F, 4.3F, 1.4F, -0.7550357F, 0.0F, 0.0F));
        PartDefinition rightBackLegBase = rightBackLegTop.addOrReplaceChild("right_back_leg_base", CubeListBuilder.create().texOffs(117, 14).addBox(0.0F, 0.0F, 0.0F, 2.0F, 6.0F, 2.0F), PartPose.offsetAndRotation(0.9F, 5.3F, 1.4F, -0.7550357F, 0.0F, 0.0F));
        PartDefinition leftBackLegBase = leftBackLegTop.addOrReplaceChild("left_back_leg_base", CubeListBuilder.create().texOffs(117, 14).addBox(0.0F, 0.0F, 0.0F, 2.0F, 6.0F, 2.0F), PartPose.offsetAndRotation(0.1F, 5.3F, 1.4F, -0.7550357F, 0.0F, 0.0F));

        rightFrontLegBase.addOrReplaceChild("front_right_feet", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, -4.0F, 3.0F, 1.0F, 4.0F), PartPose.offsetAndRotation(-0.5F, 6.0F, 2.5F, 0.3490659F, 0.0F, 0.0F));
        leftFrontLegBase.addOrReplaceChild("front_left_feet", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, -4.0F, 3.0F, 1.0F, 4.0F), PartPose.offsetAndRotation(-0.5F, 6.0F, 2.5F, 0.3490659F, 0.0F, 0.0F));
        rightBackLegBase.addOrReplaceChild("back_right_feet", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, -4.0F, 3.0F, 1.0F, 4.0F), PartPose.offsetAndRotation(-0.5F, 5.0F, 2.5F, 0.3490659F, 0.0F, 0.0F));
        leftBackLegBase.addOrReplaceChild("back_left_feet", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, -4.0F, 3.0F, 1.0F, 4.0F), PartPose.offsetAndRotation(-0.5F, 5.0F, 2.5F, 0.3490659F, 0.0F, 0.0F));

        root.addOrReplaceChild("scale0", CubeListBuilder.create().texOffs(76, 26).addBox(0.0F, 0.0F, 0.0F, 0.0F, 3.0F, 6.0F), PartPose.offset(0.5F, 6.0F, -8.0F));
        root.addOrReplaceChild("scale1", CubeListBuilder.create().texOffs(88, 21).addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 5.0F), PartPose.offsetAndRotation(0.5F, 7.5F, 1.0F, -0.1396263F, 0.0F, 0.0F));
        root.addOrReplaceChild("scale2", CubeListBuilder.create().texOffs(24, -4).addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 4.0F), PartPose.offsetAndRotation(0.5F, 9.5F, 8.0F, -0.2617994F, 0.0F, 0.0F));
        root.addOrReplaceChild("scale3", CubeListBuilder.create().texOffs(57, 9).addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 3.0F), PartPose.offsetAndRotation(0.5F, 12.0F, 14.0F, -0.3839724F, 0.0F, 0.0F));
        root.addOrReplaceChild("scale4", CubeListBuilder.create().texOffs(20, -2).addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(0.5F, 14.0F, 17.5F, -0.2094395F, 0.0F, 0.0F));

        root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(88, 28).addBox(-1.9F, -20.0F, 0.0F, 2.0F, 21.0F, 2.0F), PartPose.offsetAndRotation(4.0F, 11.0F, -9.0F, 0.0F, 0.0F, 1.570796F));
        root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(88, 28).addBox(-0.1F, -20.0F, 0.0F, 2.0F, 21.0F, 2.0F), PartPose.offsetAndRotation(-3.0F, 11.0F, -9.0F, 0.0F, 0.0F, -1.570796F));
        root.addOrReplaceChild("left_scale_wing", CubeListBuilder.create().texOffs(72, 0).addBox(-1.0F, -1.0F, 0.0F, 21.0F, 0.0F, 14.0F), PartPose.offset(4.0F, 11.0F, -9.0F));
        root.addOrReplaceChild("right_scale_wing", CubeListBuilder.create().texOffs(-14, 47).addBox(-20.0F, -1.0F, 0.0F, 21.0F, 0.0F, 14.0F), PartPose.offset(-3.0F, 11.0F, -9.0F));

        root.addOrReplaceChild("horn2", CubeListBuilder.create().texOffs(25, 0).addBox(-3.6F, -5.0F, 0.0F, 1.0F, 1.0F, 7.0F), PartPose.offsetAndRotation(0.5F, 3.0F, -17.0F, 0.4712389F, 0.0F, 0.0F));
        root.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(33, 0).addBox(-3.0F, -1.0F, -7.0F, 6.0F, 2.0F, 9.0F), PartPose.offsetAndRotation(0.5F, 3.0F, -17.0F, 0.0371786F, 0.0F, 0.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 32).addBox(-3.5F, -5.0F, -8.0F, 7.0F, 5.0F, 10.0F), PartPose.offsetAndRotation(0.5F, 3.0F, -17.0F, 0.2268928F, 0.0F, 0.0F));
        root.addOrReplaceChild("head2", CubeListBuilder.create().texOffs(34, 32).addBox(-3.0F, -5.5F, -6.8F, 6.0F, 2.0F, 9.0F), PartPose.offsetAndRotation(0.5F, 3.0F, -17.0F, 0.2974289F, 0.0F, 0.0F));
        root.addOrReplaceChild("horn1", CubeListBuilder.create().texOffs(25, 0).addBox(2.6F, -5.0F, 0.0F, 1.0F, 1.0F, 7.0F), PartPose.offsetAndRotation(0.5F, 3.0F, -17.0F, 0.4712389F, 0.0F, 0.0F));
        root.addOrReplaceChild("crystal", CubeListBuilder.create().texOffs(0, 5).addBox(2.9F, -4.5F, -3.9F, 1.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(0.5F, 3.0F, -17.0F, 0.0F, 0.7853982F, 0.0F));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean flying = entity.isDragonFlying();
        leftWing.visible = flying;
        rightWing.visible = flying;
        leftScaleWing.visible = flying;
        rightScaleWing.visible = flying;
        if (flying) {
            if (entity.isDragonFalling()) {
                float glideSway = Mth.sin(ageInTicks * 0.16F) * 0.04F;
                rightWing.zRot = -0.35F + glideSway;
                leftWing.zRot = 0.35F - glideSway;
                rightScaleWing.zRot = 1.15F + glideSway;
                leftScaleWing.zRot = -1.15F - glideSway;
            } else {
                float flap = (Mth.sin(ageInTicks * 0.85F) + 1.0F) * 0.68F;
                rightWing.zRot = flap - 1.570796F;
                leftWing.zRot = -flap + 1.570796F;
                rightScaleWing.zRot = flap;
                leftScaleWing.zRot = -flap;
            }
            float tuckedLeg = entity.isDragonFalling() ? 1.05F : 0.785398F;
            leftFrontLegTop.xRot = tuckedLeg;
            rightFrontLegTop.xRot = tuckedLeg;
            leftBackLegTop.xRot = tuckedLeg;
            rightBackLegTop.xRot = tuckedLeg;
            leftFrontLegBase.xRot = 0.0F;
            rightFrontLegBase.xRot = 0.0F;
            leftBackLegBase.xRot = 0.0F;
            rightBackLegBase.xRot = 0.0F;
        } else {
            leftFrontLegTop.xRot = ((Mth.sin(limbSwing) + 1.0F) * -limbSwingAmount) + 0.4059698F;
            rightFrontLegTop.xRot = ((Mth.cos(limbSwing) + 1.0F) * -limbSwingAmount) + 0.4059698F;
            rightBackLegTop.xRot = ((Mth.sin(limbSwing) + 1.0F) * -limbSwingAmount) + 0.4059698F;
            leftBackLegTop.xRot = ((Mth.cos(limbSwing) + 1.0F) * -limbSwingAmount) + 0.4059698F;
            rightBackLegBase.xRot = -((Mth.sin(limbSwing) + 1.0F) * -limbSwingAmount) - 0.7550357F;
            rightFrontLegBase.xRot = -((Mth.cos(limbSwing) + 1.0F) * -limbSwingAmount) - 0.7550357F;
            leftBackLegBase.xRot = -((Mth.cos(limbSwing) + 1.0F) * -limbSwingAmount) - 0.7550357F;
            leftFrontLegBase.xRot = -((Mth.sin(limbSwing) + 1.0F) * -limbSwingAmount) - 0.7550357F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
