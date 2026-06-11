package com.valentin4311.candycraftmod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.PingouinEntity;
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

public class PingouinModel<T extends PingouinEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "pingouin"), "main");

    private final ModelPart footLeft;
    private final ModelPart footRight;
    private final ModelPart body;
    private final ModelPart wingLeft;
    private final ModelPart wingRight;
    private final ModelPart head;
    private final ModelPart beak;
    private final ModelPart tail;
    private final ModelPart crest;

    public PingouinModel(ModelPart root) {
        this.footLeft = root.getChild("foot_left");
        this.footRight = root.getChild("foot_right");
        this.body = root.getChild("body");
        this.wingLeft = root.getChild("wing_left");
        this.wingRight = root.getChild("wing_right");
        this.head = root.getChild("head");
        this.beak = root.getChild("beak");
        this.tail = root.getChild("tail");
        this.crest = root.getChild("crest");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("foot_left", CubeListBuilder.create().texOffs(0, 12).addBox(-2.0F, 0.0F, -3.0F, 2.0F, 1.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 23.0F, 0.0F, 0.0F, 0.3490659F, 0.0F));
        root.addOrReplaceChild("foot_right", CubeListBuilder.create().texOffs(0, 12).addBox(0.0F, 0.0F, -3.0F, 2.0F, 1.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 23.0F, 0.0F, 0.0F, -0.3490659F, 0.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(24, 19).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 9.0F, 4.0F), PartPose.offset(0.0F, 14.0F, 0.0F));
        root.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, -2.0F, 1.0F, 7.0F, 4.0F), PartPose.offsetAndRotation(-2.0F, 14.0F, 1.0F, 0.0F, 0.0F, 0.2230717F));
        root.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(24, 0).addBox(0.0F, 0.0F, -2.0F, 1.0F, 7.0F, 4.0F), PartPose.offsetAndRotation(2.0F, 14.0F, 1.0F, 0.0F, 0.0F, -0.2230717F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 20).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 14.0F, 1.0F));
        root.addOrReplaceChild("beak", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -2.0F, -5.0F, 2.0F, 1.0F, 2.0F), PartPose.offset(0.0F, 14.0F, 1.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(10, 0).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 3.0F, 0.0F), PartPose.offsetAndRotation(0.0F, 21.0F, 3.0F, 0.1487144F, 0.0F, 0.0F));
        root.addOrReplaceChild("crest", CubeListBuilder.create().texOffs(12, 9).addBox(-2.5F, 0.0F, 0.0F, 5.0F, 10.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 14.0F, 3.0F, 0.2230717F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float pitch = headPitch * ((float)Math.PI / 180F);
        float yaw = netHeadYaw * ((float)Math.PI / 180F);
        beak.xRot = pitch;
        beak.yRot = yaw;
        head.xRot = pitch;
        head.yRot = yaw;

        footLeft.xRot = ((Mth.sin(limbSwing) + 1.0F) * 0.8F) * -limbSwingAmount;
        footRight.xRot = ((Mth.cos(limbSwing) + 1.0F) * 0.8F) * -limbSwingAmount;
        footLeft.yRot = ((Mth.sin(limbSwing) + 1.0F) * 0.5F) * limbSwingAmount + 0.3490659F;
        footRight.yRot = ((Mth.cos(limbSwing) + 1.0F) * -0.5F) * limbSwingAmount - 0.3490659F;

        crest.xRot = (Mth.cos(entity.tickCount * 0.1F) + 1.0F) * 0.2F;
        if (entity.onGround()) {
            wingLeft.zRot = (Mth.cos(entity.tickCount * 0.1F) + 1.0F) * 0.1F + 0.2230717F;
            wingRight.zRot = -((Mth.cos(entity.tickCount * 0.1F) + 1.0F) * 0.1F) - 0.2230717F;
        } else {
            wingLeft.zRot = (Mth.cos(entity.tickCount * 0.5F) + 1.0F) * 0.6F + 0.2230717F;
            wingRight.zRot = -((Mth.cos(entity.tickCount * 0.5F) + 1.0F) * 0.6F) - 0.2230717F;
        }
        crest.visible = entity.isSuperPingouin();
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        footLeft.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        footRight.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        wingLeft.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        wingRight.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        beak.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        tail.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        crest.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
