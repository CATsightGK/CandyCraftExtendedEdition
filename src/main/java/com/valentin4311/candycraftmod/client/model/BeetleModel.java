package com.valentin4311.candycraftmod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.BasicCandySpiderEntity;
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
    private static final float BODY_BASE_X = -7.0F;
    private static final float BODY_BASE_Y = 11.5F;
    private static final float BODY_BASE_Z = -4.5F;
    private static final float SHELL_BASE_X = -6.666667F;
    private static final float SHELL_BASE_Y = 11.7F;
    private static final float SHELL_BASE_Z = -4.0F;

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
        resetParts();
        if (entity instanceof BasicCandySpiderEntity beetle && beetle.getType() == com.valentin4311.candycraftmod.registry.CCEntityTypes.BOSS_BEETLE.get()) {
            setupBossBeetleAnim(beetle, limbSwing, limbSwingAmount, ageInTicks);
            return;
        }
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

    private void resetParts() {
        leg1.resetPose();
        leg2.resetPose();
        leg3.resetPose();
        leg4.resetPose();
        leg5.resetPose();
        leg6.resetPose();
        belly.resetPose();
        body.resetPose();
        head.resetPose();
        rim.resetPose();
        shell.resetPose();
    }

    private void setupBossBeetleAnim(BasicCandySpiderEntity beetle, float limbSwing, float limbSwingAmount, float ageInTicks) {
        if (!beetle.isBossAwake()) {
            return;
        }

        float walk = Mth.cos(limbSwing * 0.75F) * limbSwingAmount * 0.65F;
        leg1.zRot = walk;
        leg3.zRot = walk;
        leg4.zRot = walk;
        leg5.zRot = -walk;
        leg6.zRot = -walk;
        leg2.zRot = -walk;

        float idleBob = Mth.sin(ageInTicks * 0.12F) * 0.12F;
        body.y += idleBob;
        belly.y += idleBob * 0.7F;
        shell.y += idleBob * 0.8F;
        rim.y += idleBob * 0.8F;

        int state = beetle.getBossAttackState();
        if (beetle.isBossMeleeMode()) {
            float crawl = ageInTicks * 0.62F;
            float lunge = Math.max(0.0F, Mth.sin(ageInTicks * 0.22F));
            body.y += Mth.sin(crawl) * 0.22F;
            belly.y += Mth.sin(crawl + 0.6F) * 0.14F;
            shell.y += Mth.sin(crawl + 0.2F) * 0.18F;
            head.zRot = -0.22F - lunge * 0.2F;
            head.x += lunge * 0.32F;
            leg1.zRot = Mth.sin(crawl + 0.0F) * 0.75F;
            leg2.zRot = Mth.sin(crawl + 1.05F) * 0.75F;
            leg3.zRot = Mth.sin(crawl + 2.1F) * 0.75F;
            leg4.zRot = Mth.sin(crawl + 3.15F) * 0.75F;
            leg5.zRot = Mth.sin(crawl + 4.2F) * 0.75F;
            leg6.zRot = Mth.sin(crawl + 5.25F) * 0.75F;
            body.zRot = Mth.sin(crawl * 0.5F) * 0.045F;
        } else if (state == BasicCandySpiderEntity.BOSS_ATTACK_VOLLEY_CHARGE) {
            float pulse = 0.5F + 0.5F * Mth.sin(ageInTicks * 0.55F);
            float sway = Mth.sin(ageInTicks * 0.22F);
            head.zRot = -0.28F - pulse * 0.18F;
            head.y += 0.25F * pulse;
            body.y -= 0.35F + pulse * 0.35F;
            shell.y -= 0.55F + pulse * 0.45F;
            rim.y -= 0.35F + pulse * 0.25F;
            body.zRot = -sway * 0.035F;
            leg1.zRot += 0.35F;
            leg3.zRot += 0.35F;
            leg4.zRot += 0.2F;
            leg5.zRot -= 0.35F;
            leg6.zRot -= 0.35F;
            leg2.zRot -= 0.2F;
        } else if (state == BasicCandySpiderEntity.BOSS_ATTACK_VOLLEY) {
            float recoil = Math.max(0.0F, Mth.sin(ageInTicks * (float)Math.PI * 0.5F));
            head.zRot = -0.42F - recoil * 0.24F;
            head.x += recoil * 0.35F;
            body.y += recoil * 0.25F;
            shell.y -= recoil * 0.2F;
            shell.xRot = recoil * 0.08F;
            leg1.zRot += recoil * 0.28F;
            leg3.zRot += recoil * 0.28F;
            leg5.zRot -= recoil * 0.28F;
            leg6.zRot -= recoil * 0.28F;
        } else if (state == BasicCandySpiderEntity.BOSS_ATTACK_SPIN) {
            float spray = ageInTicks * 0.42F;
            float recoil = 0.5F + 0.5F * Mth.sin(spray * 2.0F);
            float brace = Mth.sin(spray + 0.35F);
            body.zRot = 0.58F + recoil * 0.06F;
            body.y -= 0.52F + recoil * 0.12F;
            body.x += 0.12F + recoil * 0.08F;
            belly.zRot = body.zRot * 0.72F;
            belly.y -= 0.26F + recoil * 0.06F;
            belly.x += recoil * 0.06F;
            rim.zRot = body.zRot * 0.92F;
            rim.y -= 0.28F + recoil * 0.05F;
            rim.x += recoil * 0.06F;
            head.zRot = -0.08F;
            leg1.zRot += Mth.sin(spray + 0.0F) * 0.35F + 0.22F;
            leg2.zRot += Mth.sin(spray + 1.2F) * 0.28F;
            leg3.zRot += Mth.sin(spray + 2.4F) * 0.35F + 0.22F;
            leg4.zRot += Mth.sin(spray + 3.6F) * 0.28F;
            leg5.zRot -= 0.32F + brace * 0.16F;
            leg6.zRot += 0.32F - brace * 0.16F;
        }
        applyBossLegLife(ageInTicks, limbSwingAmount);
        applyBossShootTap(beetle);
        syncShellToBody();
    }

    private void applyBossLegLife(float ageInTicks, float limbSwingAmount) {
        float idleAmount = 0.06F + (1.0F - Mth.clamp(limbSwingAmount, 0.0F, 1.0F)) * 0.08F;
        float walkAmount = Mth.clamp(limbSwingAmount, 0.0F, 1.0F) * 0.18F;
        leg1.y += Mth.sin(ageInTicks * 0.24F + 0.0F) * idleAmount;
        leg2.y += Mth.sin(ageInTicks * 0.24F + 1.2F) * idleAmount;
        leg3.y += Mth.sin(ageInTicks * 0.24F + 2.4F) * idleAmount;
        leg4.y += Mth.sin(ageInTicks * 0.24F + 3.6F) * idleAmount;
        leg5.y += Mth.sin(ageInTicks * 0.24F + 4.8F) * idleAmount;
        leg6.y += Mth.sin(ageInTicks * 0.24F + 6.0F) * idleAmount;
        leg1.xRot += Mth.sin(ageInTicks * 0.18F + 0.4F) * (0.025F + walkAmount);
        leg2.xRot += Mth.sin(ageInTicks * 0.18F + 1.6F) * (0.025F + walkAmount * 0.8F);
        leg3.xRot += Mth.sin(ageInTicks * 0.18F + 2.8F) * (0.025F + walkAmount);
        leg4.xRot += Mth.sin(ageInTicks * 0.18F + 4.0F) * (0.025F + walkAmount * 0.8F);
        leg5.xRot += Mth.sin(ageInTicks * 0.18F + 5.2F) * (0.025F + walkAmount);
        leg6.xRot += Mth.sin(ageInTicks * 0.18F + 6.4F) * (0.025F + walkAmount);
    }

    private void applyBossShootTap(BasicCandySpiderEntity beetle) {
        int shootTicks = beetle.getBossShootTicks();
        if (shootTicks <= 0) {
            return;
        }
        float progress = 1.0F - shootTicks / 6.0F;
        float tap = Mth.sin(Mth.clamp(progress, 0.0F, 1.0F) * (float)Math.PI);
        head.zRot -= tap * 0.12F;
        leg5.y += tap * 0.42F;
        leg6.y += tap * 0.42F;
        leg5.zRot -= tap * 0.34F;
        leg6.zRot += tap * 0.34F;
        leg5.xRot += tap * 0.08F;
        leg6.xRot -= tap * 0.08F;
    }

    private void syncShellToBody() {
        shell.x = SHELL_BASE_X + (body.x - BODY_BASE_X);
        shell.y = SHELL_BASE_Y + (body.y - BODY_BASE_Y);
        shell.z = SHELL_BASE_Z + (body.z - BODY_BASE_Z);
        shell.xRot = body.xRot;
        shell.yRot = body.yRot;
        shell.zRot = body.zRot;
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
