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

public class MermaidModel<T extends BasicCandyZombieEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "mermaid"), "main");
    private final ModelPart root;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart tail5;
    private final ModelPart tail6;
    private final ModelPart fin;

    public MermaidModel(ModelPart root) {
        this.root = root;
        leftArm = root.getChild("shape4");
        rightArm = root.getChild("shape41");
        tail2 = root.getChild("shape21");
        tail3 = root.getChild("shape22");
        tail4 = root.getChild("shape23");
        tail5 = root.getChild("shape24");
        tail6 = root.getChild("shape25");
        fin = root.getChild("shape5");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        add(root, "shape1", 0, 6, -3.0F, 0.0F, -2.0F, 6.0F, 4.0F, 4.0F, 0.0F, 12.0F, -0.5F, 0.0F, 0.0F, 0.0F);
        add(root, "shape3", 0, 1, -1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F, 0.0F, 12.0F, -0.5F, 0.0F, 0.0F, 0.0F);
        add(root, "shape4", 20, 6, 0.0F, 0.0F, -1.0F, 1.0F, 5.0F, 2.0F, 3.0F, 14.0F, -0.5F, -1.570796F, 0.2974289F, 0.0F);
        add(root, "shape41", 20, 6, -1.0F, 0.0F, -1.0F, 1.0F, 5.0F, 2.0F, -3.0F, 14.0F, -0.5F, -1.570796F, -0.6123111F, 0.0F);
        add(root, "shape6", 12, 0, -2.0F, -3.0F, -2.0F, 4.0F, 1.0F, 4.0F, 0.0F, 12.0F, -0.5F, 0.0F, 0.0F, 0.0F);
        add(root, "shape7", 28, 0, -1.5F, -4.0F, -1.5F, 3.0F, 1.0F, 3.0F, 0.0F, 12.0F, -0.5F, 0.0F, 0.0F, 0.0F);
        add(root, "shape8", 4, 22, 1.0F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, 12.0F, -0.5F, 0.0F, 0.0F, 0.0F);
        add(root, "shape81", 4, 22, -2.0F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, 12.0F, -0.5F, 0.0F, 0.0F, 0.0F);
        add(root, "shape9", 0, 22, -0.5F, -2.0F, -2.0F, 1.0F, 1.0F, 1.0F, 0.0F, 12.0F, -0.5F, 0.0F, 0.0F, 0.0F);
        add(root, "shape2", 44, 0, -3.0F, 0.0F, -1.5F, 6.0F, 2.0F, 4.0F, 0.0F, 18.0F, -1.0F, 0.0527999F, 0.0F, 0.0F);
        add(root, "shape21", 48, 6, -2.5F, 1.0F, -2.0F, 5.0F, 2.0F, 3.0F, 0.0F, 18.0F, -0.5F, 0.5279988F, 0.0F, 0.0F);
        add(root, "shape22", 48, 11, -2.5F, 2.0F, -2.0F, 5.0F, 2.0F, 3.0F, 0.0F, 18.0F, -0.58F, 0.844798F, 0.0F, 0.0F);
        add(root, "shape23", 52, 16, -2.0F, 1.0F, -4.5F, 4.0F, 3.0F, 2.0F, 0.0F, 18.0F, -0.5F, 1.900796F, 0.0F, 0.0F);
        add(root, "shape24", 52, 21, -1.5F, 2.0F, -5.0F, 3.0F, 4.0F, 2.0F, 0.0F, 18.0F, -0.5F, 2.111995F, 0.0F, 0.0F);
        add(root, "shape25", 42, 16, -1.0F, 4.0F, -6.0F, 2.0F, 3.0F, 2.0F, 0.0F, 18.0F, -0.5F, 2.373648F, 0.0F, 0.0F);
        add(root, "shape5", 14, 22, -4.5F, 0.0F, 0.0F, 9.0F, 0.0F, 10.0F, 0.0F, 17.0F, 7.5F, 0.5807986F, 0.0F, 0.0F);
        add(root, "shape10", 4, 28, 1.0F, -6.0F, 1.0F, 1.0F, 3.0F, 1.0F, 0.0F, 12.0F, -0.5F, 0.0F, 0.0F, 0.0F);
        add(root, "shape101", 4, 28, -2.0F, -6.0F, 0.5F, 1.0F, 3.0F, 1.0F, 0.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        add(root, "shape102", 0, 26, -0.5F, -7.0F, 2.0F, 1.0F, 5.0F, 1.0F, 0.0F, 12.0F, -0.5F, 0.0F, 0.0F, 0.0F);
        add(root, "shape11", 28, 10, -2.5F, 1.0F, -2.2F, 5.0F, 2.0F, 1.0F, 0.0F, 12.0F, -0.5F, 0.0F, 0.0F, 0.0F);
        add(root, "shape12", 28, 5, -2.5F, 4.0F, -1.6F, 5.0F, 2.0F, 3.0F, 0.0F, 12.0F, -0.5F, 0.0F, 0.0F, 0.0F);
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static void add(PartDefinition root, String name, int u, int v, float x, float y, float z, float dx, float dy, float dz, float px, float py, float pz, float rx, float ry, float rz) {
        root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, dx, dy, dz), PartPose.offsetAndRotation(px, py, pz, rx, ry, rz));
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float sway = Mth.sin(ageInTicks * 0.16F) * 0.08F + Mth.cos(limbSwing * 0.55F) * 0.12F * limbSwingAmount;
        leftArm.xRot = -1.570796F + Mth.cos(limbSwing * 0.6662F) * 0.22F * limbSwingAmount;
        rightArm.xRot = -1.570796F + Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 0.22F * limbSwingAmount;
        tail2.yRot = sway * 0.35F;
        tail3.yRot = sway * 0.55F;
        tail4.yRot = sway * 0.75F;
        tail5.yRot = sway;
        tail6.yRot = sway * 1.25F;
        fin.yRot = sway * 1.55F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
