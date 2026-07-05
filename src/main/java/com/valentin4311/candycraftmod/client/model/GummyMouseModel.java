package com.valentin4311.candycraftmod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.GummyMouseEntity;
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

public class GummyMouseModel<T extends GummyMouseEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "gummy_mouse"), "main");

    private final ModelPart body;
    private final ModelPart tail;

    public GummyMouseModel(ModelPart root) {
        this.body = root.getChild("body");
        this.tail = body.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body",
            CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 3.0F, 6.0F)
                .texOffs(16, 15).addBox(-1.5F, 0.5F, 0.5F, 3.0F, 2.0F, 5.0F),
            PartPose.offset(0.0F, 21.0F, -3.0F));
        PartDefinition head = body.addOrReplaceChild("head",
            CubeListBuilder.create()
                .texOffs(0, 9).addBox(-1.5F, 0.0F, -3.0F, 3.0F, 2.0F, 3.0F)
                .texOffs(22, 4).addBox(-1.0F, 0.5F, -3.1F, 2.0F, 1.0F, 3.0F),
            PartPose.offset(0.0F, 1.0F, 0.0F));
        head.addOrReplaceChild("left_ear",
            CubeListBuilder.create().texOffs(0, 14).addBox(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 2.0F),
            PartPose.offset(0.3F, -0.6F, -2.5F));
        head.addOrReplaceChild("right_ear",
            CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, 0.0F, 1.0F, 1.0F, 2.0F),
            PartPose.offset(-0.3F, -0.6F, -2.5F));
        body.addOrReplaceChild("tail",
            CubeListBuilder.create()
                .texOffs(0, 14).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 2.0F, 6.0F)
                .texOffs(18, 8).addBox(-0.5F, 0.5F, -1.0F, 1.0F, 1.0F, 6.0F),
            PartPose.offset(0.0F, 1.0F, 6.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        body.y = 21.0F + Mth.sin(ageInTicks * 0.05F) * 0.04F;
        body.x = Mth.sin(ageInTicks * 0.6F + 0.3F) * 0.004F;
        tail.yRot = Mth.sin(ageInTicks * 0.6F + 0.3F) * (float)Math.PI * 0.04F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
            float red, float green, float blue, float alpha) {
        body.render(poseStack, consumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
