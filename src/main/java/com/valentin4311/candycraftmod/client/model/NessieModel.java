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

public class NessieModel<T extends BasicCandyZombieEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "nessie"), "main");
    private final ModelPart root;
    private final ModelPart leg7;
    private final ModelPart leg1;
    private final ModelPart leg8;
    private final ModelPart leg2;
    private final float leg7BaseYRot;
    private final float leg1BaseYRot;
    private final float leg8BaseYRot;
    private final float leg2BaseYRot;

    public NessieModel(ModelPart root) {
        this.root = root;
        leg7 = root.getChild("leg7");
        leg1 = root.getChild("leg1");
        leg8 = root.getChild("leg8");
        leg2 = root.getChild("leg2");
        leg7BaseYRot = leg7.yRot;
        leg1BaseYRot = leg1.yRot;
        leg8BaseYRot = leg8.yRot;
        leg2BaseYRot = leg2.yRot;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        add(root, "head", 44, 1, -4.0F, -4.0F, -8.0F, 7.0F, 8.0F, 8.0F, 0.0F, 0.0F, -9.0F, 0.0F, 0.0F, 0.0F);
        add(root, "body", 0, 0, -2.0F, 0.0F, -3.0F, 5.0F, 5.0F, 5.0F, -1.0F, 4.0F, -11.0F, -0.1858931F, 0.0F, 0.0F);
        add(root, "rear_end", 0, 33, -5.0F, -4.0F, -12.0F, 9.0F, 9.0F, 9.0F, 0.0F, 19.0F, 2.0F, 0.0F, 0.0F, 0.0F);
        add(root, "leg8", 24, 23, 0.0F, -1.0F, -1.0F, 9.0F, 2.0F, 3.0F, 3.0F, 22.0F, -8.0F, 0.0F, 0.5759587F, 0.1919862F);
        add(root, "leg2", 24, 23, 0.0F, -1.0F, -1.0F, 9.0F, 2.0F, 3.0F, 2.0F, 22.0F, 6.0F, 0.0F, -0.5759587F, 0.1919862F);
        add(root, "leg7", 24, 18, -9.0F, -1.0F, -1.0F, 9.0F, 2.0F, 3.0F, -4.0F, 22.0F, -8.0F, 0.0F, -0.5759587F, -0.1919862F);
        add(root, "leg1", 24, 18, -9.0F, -1.0F, -2.0F, 9.0F, 2.0F, 3.0F, -3.0F, 22.0F, 7.0F, 0.0F, 0.5759587F, -0.1919862F);
        add(root, "body1", 0, 0, -2.0F, 0.0F, -2.0F, 5.0F, 5.0F, 5.0F, -1.0F, 9.0F, -13.0F, 0.1115358F, 0.0F, 0.0F);
        add(root, "body2", 0, 0, -2.0F, 0.0F, -2.0F, 5.0F, 5.0F, 5.0F, -1.0F, 14.0F, -12.0F, 0.4461433F, 0.0F, 0.0F);
        add(root, "body3", 22, 5, -3.0F, 0.0F, -2.0F, 5.0F, 3.0F, 3.0F, 0.0F, 18.0F, -10.0F, 1.226894F, 0.0F, 0.0F);
        add(root, "rear_end1", 0, 52, -5.0F, -5.0F, 0.0F, 7.0F, 7.0F, 9.0F, 1.0F, 21.0F, -1.0F, 0.0F, 0.0F, 0.0F);
        add(root, "rear_end2", 0, 69, -3.0F, -3.0F, 0.0F, 5.0F, 5.0F, 7.0F, 0.0F, 20.0F, 8.0F, 0.0F, 0.0F, 0.0F);
        add(root, "rear_end3", 0, 89, -1.0F, -1.0F, 0.0F, 3.0F, 3.0F, 5.0F, -1.0F, 19.0F, 15.0F, 0.0F, 0.0F, 0.0F);
        add(root, "rear_end4", 0, 82, 0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 3.0F, -1.0F, 19.0F, 20.0F, 0.0F, 0.0F, 0.0F);
        add(root, "body4", 0, 10, -3.0F, -2.0F, -5.0F, 5.0F, 5.0F, 5.0F, 0.0F, -2.0F, -17.0F, 0.0F, 0.0F, 0.0F);
        add(root, "body5", 0, 99, -0.5F, -6.0F, 0.0F, 0.0F, 10.0F, 9.0F, 0.0F, -3.0F, -12.0F, 0.0F, 0.0F, 0.0F);
        add(root, "body6", 38, 32, 0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 5.0F, -4.0F, -2.0F, -11.0F, 0.0F, -0.418879F, 0.0F);
        add(root, "body7", 38, 32, 0.0F, 0.0F, 0.0F, 0.0F, 5.0F, 5.0F, 3.0F, -2.0F, -11.0F, 0.0F, 0.418879F, 0.0F);
        add(root, "body8", 42, 47, -0.5F, 0.0F, 0.0F, 0.0F, 5.0F, 5.0F, 0.0F, 5.0F, -9.733334F, -0.185895F, 0.0F, 0.0F);
        add(root, "body9", 21, 91, -0.5F, -10.0F, 0.0F, 0.0F, 10.0F, 9.0F, 0.0F, 21.0F, -6.0F, 0.0F, 0.0F, 0.0F);
        add(root, "body10", 0, 0, -2.0F, 0.0F, -5.0F, 5.0F, 1.0F, 5.0F, -1.0F, 1.0F, -16.0F, 0.0F, 0.0F, 0.0F);
        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void add(PartDefinition root, String name, int u, int v, float x, float y, float z, float dx, float dy, float dz, float px, float py, float pz, float rx, float ry, float rz) {
        root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, dx, dy, dz), PartPose.offsetAndRotation(px, py, pz, rx, ry, rz));
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float current = ageInTicks * 0.2F;
        float rearSwing = Mth.cos(current * 0.8F) * 0.42F;
        float frontSwing = Mth.cos((current + 4.0F) * 0.8F) * 0.42F;
        leg7.yRot = leg7BaseYRot + rearSwing;
        leg1.yRot = leg1BaseYRot + rearSwing;
        leg8.yRot = leg8BaseYRot + frontSwing;
        leg2.yRot = leg2BaseYRot + frontSwing;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
