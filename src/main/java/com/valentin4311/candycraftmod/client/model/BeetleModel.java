package com.valentin4311.candycraftmod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.client.animation.LicoriceBeetleBossAnimations;
import com.valentin4311.candycraftmod.entity.BasicCandySpiderEntity;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
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
import org.joml.Vector3f;

public class BeetleModel<T extends Entity> extends HierarchicalModel<T> {
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
    private final ModelPart root;
    private final Vector3f animationVector = new Vector3f();
    private static final int ANIMATION_DORMANT = -4;
    private static final int ANIMATION_IDLE = -3;
    private static final int ANIMATION_WALK = -2;
    private static final int ANIMATION_SINGLE_SHOT = -1;
    private static final int ANIMATION_MELEE = 4;
    private static final float BODY_BASE_X = -7.0F;
    private static final float BODY_BASE_Y = 11.5F;
    private static final float BODY_BASE_Z = -4.5F;
    private static final float SHELL_BASE_X = -6.666667F;
    private static final float SHELL_BASE_Y = 11.7F;
    private static final float SHELL_BASE_Z = -4.0F;
    private static final float RIM_BASE_X = -7.5F;
    private static final float RIM_BASE_Y = 13.5F;
    private static final float RIM_BASE_Z = -4.0F;

    public BeetleModel(ModelPart root) {
        this.root = root;
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

    @Override
    public ModelPart root() {
        return root;
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
        float walkAmount = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        if (entity instanceof BasicCandySpiderEntity beetle && beetle.isBossBeetle()) {
            applyBossAnimation(beetle, ageInTicks, walkAmount);
            head.zRot += -headPitch * ((float)Math.PI / 270.0F);
            head.yRot += netHeadYaw * ((float)Math.PI / 270.0F);
            return;
        }

        head.zRot = -headPitch * ((float)Math.PI / 270.0F);
        head.yRot = netHeadYaw * ((float)Math.PI / 270.0F);
        float walk = Mth.cos(limbSwing * 0.6662F) * walkAmount;
        float idle = ageInTicks * 0.085F;
        float idleWave = Mth.sin(idle);
        float idleWaveOffset = Mth.sin(idle + 1.7278761F);
        float bodyBob = idleWave * (0.08F + 0.06F * (1.0F - walkAmount));
        body.y += bodyBob;
        belly.y += bodyBob * 0.55F;
        head.y += idleWaveOffset * 0.2F;
        body.zRot += idleWave * 0.006F;
        head.xRot += idleWaveOffset * 0.035F;
        leg1.zRot = walk + idleWaveOffset * 0.055F;
        leg3.zRot = walk + idleWaveOffset * 0.045F;
        leg4.zRot = walk - idleWaveOffset * 0.045F;
        leg5.zRot = -walk - idleWaveOffset * 0.055F;
        leg6.zRot = -walk - idleWaveOffset * 0.045F;
        leg2.zRot = -walk + idleWaveOffset * 0.04F;
        syncOuterBodyToTorso();
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

    private void applyBossAnimation(BasicCandySpiderEntity beetle, float ageInTicks, float walkAmount) {
        int state = beetle.getBossAttackState();
        int animationKey;
        AnimationDefinition animation;
        float weight = 1.0F;

        if (state == BasicCandySpiderEntity.BOSS_ATTACK_NONE
            && !beetle.isBossMeleeMode()
            && beetle.getBossShootTicks() > 0) {
            animationKey = ANIMATION_SINGLE_SHOT;
            animation = LicoriceBeetleBossAnimations.SINGLE_SHOT;
        } else if (!beetle.isBossAwake()) {
            animationKey = ANIMATION_DORMANT;
            animation = LicoriceBeetleBossAnimations.DORMANT;
        } else if (beetle.isBossMeleeMode()) {
            animationKey = ANIMATION_MELEE;
            animation = LicoriceBeetleBossAnimations.MELEE;
        } else if (state == BasicCandySpiderEntity.BOSS_ATTACK_VOLLEY_CHARGE) {
            animationKey = BasicCandySpiderEntity.BOSS_ATTACK_VOLLEY_CHARGE;
            animation = LicoriceBeetleBossAnimations.VOLLEY_CHARGE;
        } else if (state == BasicCandySpiderEntity.BOSS_ATTACK_VOLLEY) {
            animationKey = BasicCandySpiderEntity.BOSS_ATTACK_VOLLEY;
            animation = LicoriceBeetleBossAnimations.VOLLEY_FIRE;
        } else if (state == BasicCandySpiderEntity.BOSS_ATTACK_SPIN) {
            animationKey = BasicCandySpiderEntity.BOSS_ATTACK_SPIN;
            animation = LicoriceBeetleBossAnimations.SPIN_FIRE;
        } else if (walkAmount > 0.05F) {
            animationKey = ANIMATION_WALK;
            animation = LicoriceBeetleBossAnimations.WALK;
            weight = Mth.clamp(walkAmount * 4.0F, 0.0F, 1.0F);
        } else {
            animationKey = ANIMATION_IDLE;
            animation = LicoriceBeetleBossAnimations.IDLE;
        }

        float animationSeconds = beetle.getClientBossAnimationSeconds(animationKey, ageInTicks);
        KeyframeAnimations.animate(this, animation, (long)(animationSeconds * 1000.0F), weight, animationVector);
    }

    private void syncOuterBodyToTorso() {
        shell.x = SHELL_BASE_X + (body.x - BODY_BASE_X);
        shell.y = SHELL_BASE_Y + (body.y - BODY_BASE_Y);
        shell.z = SHELL_BASE_Z + (body.z - BODY_BASE_Z);
        shell.xRot = body.xRot;
        shell.yRot = body.yRot;
        shell.zRot = body.zRot;

        rim.x = RIM_BASE_X + (body.x - BODY_BASE_X);
        rim.y = RIM_BASE_Y + (body.y - BODY_BASE_Y);
        rim.z = RIM_BASE_Z + (body.z - BODY_BASE_Z);
        rim.xRot = body.xRot;
        rim.yRot = body.yRot;
        rim.zRot = body.zRot;
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
