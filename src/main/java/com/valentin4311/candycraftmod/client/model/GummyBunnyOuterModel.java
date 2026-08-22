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

/**
 * Slightly inflated shell rendered around the swamp gummy bunny, mirroring the
 * two-pass gummy look used by the gummy mouse (colored body + translucent shell).
 */
public class GummyBunnyOuterModel<T extends GummyBunnyEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "gummy_bunny_outer"), "main");

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart tail;

    public GummyBunnyOuterModel(ModelPart root) {
        this.body = root.getChild("body");
        this.head = root.getChild("head");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 9).addBox(0.0F, 0.0F, 0.0F, 7.0F, 7.0F, 9.0F), PartPose.offset(-3.5F, 15.5F, -4.5F));
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(20, 7).addBox(-2.5F, -2.5F, -4.5F, 5.0F, 5.0F, 7.0F), PartPose.offset(0.0F, 19.0F, -4.0F));
        head.addOrReplaceChild("ear_left", CubeListBuilder.create().texOffs(14, 4).addBox(-2.05F, -5.1F, -3.25F, 1.5F, 3.5F, 2.5F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.0872665F));
        head.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(14, 4).addBox(0.55F, -5.1F, -3.25F, 1.5F, 3.5F, 2.5F), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0872665F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(20, 0).addBox(0.0F, 0.0F, 0.0F, 5.0F, 5.0F, 3.5F), PartPose.offset(-2.5F, 16.5F, 4.25F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // The ears are children of the head and inherit its rotation directly.
        head.xRot = headPitch * ((float)Math.PI / 180F);
        head.yRot = netHeadYaw * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        tail.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
