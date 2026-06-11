package com.valentin4311.candycraftmod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
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
import net.minecraft.world.entity.Entity;

public class BeetleModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "beetle"), "main");
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart leg3;
    private final ModelPart leg4;
    private final ModelPart leg5;
    private final ModelPart leg6;
    private final ModelPart belly;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rim;
    private final ModelPart shell;

    public BeetleModel(ModelPart root) {
        leg1 = root.getChild("leg1");
        leg2 = root.getChild("leg2");
        leg3 = root.getChild("leg3");
        leg4 = root.getChild("leg4");
        leg5 = root.getChild("leg5");
        leg6 = root.getChild("leg6");
        belly = root.getChild("belly");
        body = root.getChild("body");
        head = root.getChild("head");
        rim = root.getChild("rim");
        shell = root.getChild("shell");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 24).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 5.0F, 3.0F), PartPose.offset(-5.0F, 19.0F, 4.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 24).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 5.0F, 3.0F), PartPose.offset(0.0F, 19.0F, 4.0F));
        root.addOrReplaceChild("leg3", CubeListBuilder.create().texOffs(0, 24).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 5.0F, 3.0F), PartPose.offset(5.0F, 19.0F, 4.0F));
        root.addOrReplaceChild("leg4", CubeListBuilder.create().texOffs(0, 16).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 5.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 19.0F, -4.0F, 0.0F, 3.141593F, 0.0F));
        root.addOrReplaceChild("leg5", CubeListBuilder.create().texOffs(0, 16).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 5.0F, 3.0F), PartPose.offsetAndRotation(-5.0F, 19.0F, -4.0F, 0.0F, 3.141593F, 0.0F));
        root.addOrReplaceChild("leg6", CubeListBuilder.create().texOffs(0, 16).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 5.0F, 3.0F), PartPose.offsetAndRotation(5.0F, 19.0F, -4.0F, 0.0F, 3.141593F, 0.0F));
        root.addOrReplaceChild("belly", CubeListBuilder.create().texOffs(46, 0).addBox(0.0F, 0.0F, 0.0F, 13.0F, 3.0F, 8.0F), PartPose.offset(-6.5F, 18.0F, -4.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, 0.0F, 14.0F, 7.0F, 9.0F), PartPose.offset(-7.0F, 11.5F, -4.5F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(12, 19).addBox(-7.0F, -3.0F, -3.5F, 8.0F, 6.0F, 7.0F), PartPose.offset(-7.0F, 17.0F, 0.0F));
        root.addOrReplaceChild("rim", CubeListBuilder.create().texOffs(42, 17).addBox(0.0F, 0.0F, 0.0F, 1.0F, 7.0F, 8.0F), PartPose.offset(-7.5F, 13.5F, -4.0F));
        root.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(60, 18).addBox(0.0F, 0.0F, 0.0F, 13.0F, 6.0F, 8.0F), PartPose.offset(-6.666667F, 11.7F, -4.0F));
        return LayerDefinition.create(mesh, 128, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        head.zRot = -headPitch * ((float)Math.PI / 270.0F);
        head.yRot = netHeadYaw * ((float)Math.PI / 270.0F);
        float walk = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
        leg1.zRot = walk;
        leg3.zRot = walk;
        leg4.zRot = walk;
        leg5.zRot = -walk;
        leg6.zRot = -walk;
        leg2.zRot = -walk;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        leg1.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leg2.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leg3.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leg4.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leg5.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leg6.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        belly.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rim.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        shell.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
