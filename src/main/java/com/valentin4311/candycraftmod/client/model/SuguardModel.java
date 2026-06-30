package com.valentin4311.candycraftmod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
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

public class SuguardModel<T extends BasicCandyZombieEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "suguard"), "main");
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart nose;
    private final ModelPart hatBrim;
    private final ModelPart earRight;
    private final ModelPart earLeft;
    private final ModelPart leftArm;
    public final ModelPart rightArm;
    private final ModelPart shield;
    private final ModelPart hatTop;

    public SuguardModel(ModelPart root) {
        leg1 = root.getChild("leg1");
        leg2 = root.getChild("leg2");
        body = root.getChild("body");
        head = root.getChild("head");
        nose = root.getChild("nose");
        hatBrim = root.getChild("hat_brim");
        earRight = root.getChild("ear_right");
        earLeft = root.getChild("ear_left");
        leftArm = root.getChild("left_arm");
        rightArm = root.getChild("right_arm");
        shield = root.getChild("shield");
        hatTop = root.getChild("hat_top");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 2.0F, 4.0F, 2.0F), PartPose.offset(1.0F, 20.0F, -1.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 2.0F, 4.0F, 2.0F), PartPose.offset(-3.0F, 20.0F, -1.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, 0.0F, 0.0F, 6.0F, 6.0F, 4.0F), PartPose.offset(-3.0F, 14.0F, -2.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(0.0F, 11.0F, 0.0F));
        root.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 22).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("hat_brim", CubeListBuilder.create().texOffs(12, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 1.0F, 4.0F), PartPose.offset(0.0F, 11.0F, 0.0F));
        root.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(4, 22).addBox(1.0F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("ear_left", CubeListBuilder.create().texOffs(4, 22).addBox(-2.0F, 2.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 10.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(20, 6).addBox(0.0F, 0.0F, 0.0F, 1.0F, 4.0F, 2.0F), PartPose.offsetAndRotation(3.0F, 15.0F, 0.0F, -1.570796F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(20, 6).addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 2.0F), PartPose.offsetAndRotation(-4.0F, 15.0F, 0.0F, -1.050296F, 0.0F, 0.0F));
        root.addOrReplaceChild("shield", CubeListBuilder.create().texOffs(8, 16).addBox(0.0F, 0.0F, 0.0F, 5.0F, 5.0F, 1.0F), PartPose.offset(1.0F, 13.5F, -5.0F));
        root.addOrReplaceChild("hat_top", CubeListBuilder.create().texOffs(28, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 1.0F, 3.0F), PartPose.offset(0.0F, 10.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.getType() == CCEntityTypes.BOSS_SUGUARD.get() && !entity.isBossSuguardAwake()) {
            setDormantBossPose();
            return;
        }
        float yaw = netHeadYaw * ((float)Math.PI / 180.0F);
        if (attackTime > 0.0F) {
            float swing = Mth.sin(Mth.sqrt(attackTime) * ((float)Math.PI * 2.0F));
            yaw += swing * ((float)Math.PI * 1.35F);
        } else if (limbSwingAmount > 0.04F) {
            yaw += limbSwing * 0.65F;
        }
        setHeadGroupYaw(yaw);
        leg1.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        leg2.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        leg1.yRot = 0.0F;
        leg2.yRot = 0.0F;
        rightArm.xRot = -1.050296F;
        rightArm.yRot = 0.0F;
        rightArm.zRot = 0.0F;
        leftArm.xRot = -1.570796F;
        leftArm.yRot = 0.0F;
        leftArm.zRot = 0.0F;
        if (entity.isBossSuguardAwake() && entity.getMainHandItem().is(CCItems.CARAMEL_BOW.get())) {
            applyBossBowDraw(entity, ageInTicks);
        }
    }

    private void setDormantBossPose() {
        setHeadGroupYaw(0.0F);
        head.xRot = 0.0F;
        nose.xRot = 0.0F;
        hatBrim.xRot = 0.0F;
        hatTop.xRot = 0.0F;
        earRight.xRot = 0.0F;
        earLeft.xRot = 0.0F;
        leg1.xRot = 0.0F;
        leg1.yRot = 0.0F;
        leg1.zRot = 0.0F;
        leg2.xRot = 0.0F;
        leg2.yRot = 0.0F;
        leg2.zRot = 0.0F;
        rightArm.xRot = -1.050296F;
        rightArm.yRot = 0.0F;
        rightArm.zRot = 0.0F;
        leftArm.xRot = -1.570796F;
        leftArm.yRot = 0.0F;
        leftArm.zRot = 0.0F;
    }

    private void applyBossBowDraw(T entity, float ageInTicks) {
        float draw = entity.getBossBowDrawProgress(0.0F);
        float eased = draw * draw * (3.0F - 2.0F * draw);
        float settle = Mth.sin(eased * (float)Math.PI) * 0.08F;
        float idle = Mth.sin(ageInTicks * 0.34F) * 0.025F;
        rightArm.xRot = -1.48F - 0.22F * eased + idle;
        rightArm.yRot = -0.50F - 0.22F * eased;
        rightArm.zRot = 0.34F + 0.12F * eased + settle;
        leftArm.xRot = -1.52F + 0.10F * eased - idle;
        leftArm.yRot = 0.44F + 0.48F * eased;
        leftArm.zRot = -0.26F - 0.18F * eased - settle;
        float headPull = eased * 0.10F;
        setHeadGroupYaw(head.yRot - headPull);
    }

    private void setHeadGroupYaw(float yaw) {
        head.yRot = yaw;
        nose.yRot = yaw;
        hatBrim.yRot = yaw;
        hatTop.yRot = yaw;
        earRight.yRot = yaw;
        earLeft.yRot = yaw;
    }

    public void translateToRightArm(PoseStack poseStack) {
        rightArm.translateAndRotate(poseStack);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        leg1.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leg2.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        nose.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        hatBrim.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        earRight.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        earLeft.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        shield.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        hatTop.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
