package com.valentin4311.candycraftmod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.CandyFishEntity;
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

public class CandyFishModel<T extends CandyFishEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "candy_fish"), "main");

    private final ModelPart body;
    private final ModelPart tailTop;
    private final ModelPart tailBottom;
    private final ModelPart finRight;
    private final ModelPart finLeft;

    public CandyFishModel(ModelPart root) {
        this.body = root.getChild("body");
        this.tailTop = root.getChild("tail_top");
        this.tailBottom = root.getChild("tail_bottom");
        this.finRight = root.getChild("fin_right");
        this.finLeft = root.getChild("fin_left");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-1.0F, 0.0F, -4.0F, 2.0F, 5.0F, 9.0F)
            .texOffs(22, 0)
            .addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 4.0F)
            .texOffs(0, 20)
            .addBox(-1.0F, 2.0F, -5.0F, 2.0F, 3.0F, 1.0F), PartPose.offset(0.0F, 16.0F, 0.0F));
        root.addOrReplaceChild("tail_top", CubeListBuilder.create()
            .texOffs(8, 14)
            .addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 4.0F), PartPose.offsetAndRotation(0.0F, 18.23333F, 3.266667F, 0.7807508F, 0.0F, 0.0F));
        root.addOrReplaceChild("tail_bottom", CubeListBuilder.create()
            .texOffs(8, 14)
            .addBox(-0.5F, 0.0F, 0.0F, 1.0F, 2.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 17.0F, 3.866667F, -0.7807508F, 0.0F, 0.0F));
        root.addOrReplaceChild("fin_right", CubeListBuilder.create()
            .texOffs(0, 14)
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 4.0F), PartPose.offset(1.09F, 18.0F, 0.0F));
        root.addOrReplaceChild("fin_left", CubeListBuilder.create()
            .texOffs(0, 14)
            .addBox(0.0F, 0.0F, 0.0F, 0.0F, 2.0F, 4.0F), PartPose.offset(-1.1F, 17.7F, -1.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float wave = (entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D || Math.abs(entity.getDeltaMovement().y) > 1.0E-6D)
            ? Mth.cos(entity.current * 2.3662F)
            : 0.0F;
        tailTop.yRot = wave;
        tailBottom.yRot = wave;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        finRight.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        finLeft.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        tailTop.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        tailBottom.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
