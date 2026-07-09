package com.valentin4311.candycraftmod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.GummyBearEntity;
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

public class GummyBearModel<T extends GummyBearEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "gummy_bear"), "main");

    private final ModelPart head;
    private final ModelPart headOuter;
    private final ModelPart body;
    private final ModelPart bodyOuter;
    private final ModelPart leg0;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg1Outer;
    private final ModelPart leg2Outer;
    private final ModelPart leg3Outer;
    private final ModelPart leg4Outer;

    public GummyBearModel(ModelPart root) {
        head = root.getChild("head");
        headOuter = root.getChild("head_outer");
        body = root.getChild("body");
        bodyOuter = root.getChild("body_outer");
        leg0 = root.getChild("leg0");
        leg1 = root.getChild("leg1");
        leg2 = root.getChild("leg2");
        leg3 = root.getChild("leg3");
        leg1Outer = root.getChild("leg1_outer");
        leg2Outer = root.getChild("leg2_outer");
        leg3Outer = root.getChild("leg3_outer");
        leg4Outer = root.getChild("leg4_outer");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head",
            CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -3.0F, -3.0F, 7.0F, 7.0F, 7.0F),
            PartPose.offset(0.0F, 10.0F, -16.0F));
        root.addOrReplaceChild("head_outer",
            CubeListBuilder.create()
                .texOffs(13, 49).addBox(-4.5F, -4.0F, -4.0F, 9.0F, 9.0F, 9.0F)
                .texOffs(0, 44).addBox(-2.5F, 1.0F, -6.0F, 5.0F, 3.0F, 3.0F)
                .texOffs(26, 0).addBox(3.0F, -4.5F, -1.0F, 2.0F, 2.0F, 1.0F)
                .texOffs(26, 0).mirror().addBox(-5.0F, -4.5F, -1.0F, 2.0F, 2.0F, 1.0F),
            PartPose.offset(0.0F, 10.0F, -16.0F));
        root.addOrReplaceChild("body",
            CubeListBuilder.create()
                .texOffs(0, 19).addBox(-9.0F, -13.0F, -7.0F, 14.0F, 14.0F, 11.0F)
                .texOffs(39, 0).addBox(-8.0F, -25.0F, -7.0F, 12.0F, 12.0F, 10.0F),
            PartPose.offset(2.0F, 9.0F, 12.0F));
        root.addOrReplaceChild("body_outer",
            CubeListBuilder.create()
                .texOffs(58, 70).addBox(-9.0F, -26.0F, -8.0F, 14.0F, 13.0F, 12.0F)
                .texOffs(0, 67).addBox(-10.0F, -14.0F, -8.0F, 16.0F, 15.0F, 13.0F),
            PartPose.offset(2.0F, 9.0F, 12.0F));
        root.addOrReplaceChild("leg0",
            CubeListBuilder.create().texOffs(50, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 8.0F),
            PartPose.offset(-4.5F, 14.0F, 6.0F));
        root.addOrReplaceChild("leg1",
            CubeListBuilder.create().texOffs(50, 22).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 8.0F),
            PartPose.offset(4.5F, 14.0F, 6.0F));
        root.addOrReplaceChild("leg2",
            CubeListBuilder.create().texOffs(50, 40).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 6.0F),
            PartPose.offset(-3.5F, 14.0F, -8.0F));
        root.addOrReplaceChild("leg3",
            CubeListBuilder.create().texOffs(50, 40).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 6.0F),
            PartPose.offset(3.5F, 14.0F, -8.0F));
        root.addOrReplaceChild("leg1_outer",
            CubeListBuilder.create().texOffs(100, 32).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 11.0F, 9.0F),
            PartPose.offset(-4.5F, 14.0F, 6.0F));
        root.addOrReplaceChild("leg2_outer",
            CubeListBuilder.create().texOffs(100, 32).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 11.0F, 9.0F),
            PartPose.offset(4.5F, 14.0F, 6.0F));
        root.addOrReplaceChild("leg3_outer",
            CubeListBuilder.create().texOffs(104, 52).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 11.0F, 7.0F),
            PartPose.offset(-3.5F, 14.0F, -8.0F));
        root.addOrReplaceChild("leg4_outer",
            CubeListBuilder.create().texOffs(104, 52).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 11.0F, 7.0F),
            PartPose.offset(3.5F, 14.0F, -8.0F));
        return LayerDefinition.create(mesh, 128, 96);
    }

    @Override
    public void setupAnim(T bear, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        resetPose();
        head.xRot = headPitch * ((float)Math.PI / 180.0F);
        head.yRot = netHeadYaw * ((float)Math.PI / 180.0F);
        body.xRot = (float)Math.PI / 2.0F;
        leg1.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        leg0.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        leg3.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount;
        leg2.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

        float partial = ageInTicks - (float)bear.tickCount;
        float stand = bear.getStandingAnimationScale(partial);
        stand *= stand;
        float grounded = 1.0F - stand;

        body.xRot = ((float)Math.PI / 2.0F) - stand * (float)Math.PI * 0.35F;
        body.y = 9.0F * grounded + 11.0F * stand;
        leg2.z = -8.0F * grounded - 4.0F * stand;
        leg2.xRot -= stand * (float)Math.PI * 0.45F;
        leg3.y = leg2.y;
        leg3.z = leg2.z;
        leg3.xRot -= stand * (float)Math.PI * 0.45F;

        if (bear.isBaby()) {
            head.y = 10.0F * grounded - 9.0F * stand;
            head.z = -16.0F * grounded - 7.0F * stand;
        } else {
            head.y = 10.0F * grounded - 14.0F * stand;
            head.z = -16.0F * grounded - 3.0F * stand;
        }
        head.xRot += stand * (float)Math.PI * 0.15F;
        head.y += Mth.sin(ageInTicks * 0.04F) * 0.3F;
        body.y += Mth.sin(ageInTicks * 0.04F + (float)Math.PI / 2.0F) * 0.3F;

        syncOuterParts();
    }

    private void resetPose() {
        head.resetPose();
        headOuter.resetPose();
        body.resetPose();
        bodyOuter.resetPose();
        leg0.resetPose();
        leg1.resetPose();
        leg2.resetPose();
        leg3.resetPose();
        leg1Outer.resetPose();
        leg2Outer.resetPose();
        leg3Outer.resetPose();
        leg4Outer.resetPose();
    }

    private void syncOuterParts() {
        headOuter.copyFrom(head);
        bodyOuter.copyFrom(body);
        leg1Outer.copyFrom(leg0);
        leg2Outer.copyFrom(leg1);
        leg3Outer.copyFrom(leg3);
        leg4Outer.copyFrom(leg2);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leg0.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leg1.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leg2.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leg3.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        bodyOuter.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, 0.8F);
        leg1Outer.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, 0.8F);
        leg2Outer.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, 0.8F);
        leg3Outer.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, 0.8F);
        leg4Outer.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, 0.8F);
        headOuter.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, 0.8F);
    }
}
