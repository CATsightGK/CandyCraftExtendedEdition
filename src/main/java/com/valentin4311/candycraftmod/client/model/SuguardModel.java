package com.valentin4311.candycraftmod.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import com.valentin4311.candycraftmod.entity.CaramelBeeEntity;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
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
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;

public class SuguardModel<T extends BasicCandyZombieEntity> extends EntityModel<T> {
    private static final float DEG_TO_RAD = (float)Math.PI / 180.0F;
    private static final float RIGHT_ARM_REST_X = -1.050296F;
    private static final float LEFT_ARM_REST_X = -1.570796F;
    private static final float DANCE_CYCLE_TICKS = 150.0F;
    private static final float DANCE_JUMP_START = 72.0F;
    private static final float DANCE_JUMP_END = 90.0F;
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(CandyCraft.MODID, "suguard"), "main");
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart nose;
    private final ModelPart hatBrim;
    private final ModelPart earRight;
    private final ModelPart earLeft;
    private final ModelPart leftArm;
    public final ModelPart rightArm;
    private final ModelPart shield;
    private final ModelPart hatTop;

    public SuguardModel(ModelPart root) {
        leg1 = root.getChild("leg1");
        leg2 = root.getChild("leg2");
        body = root.getChild("body");
        head = root.getChild("head");
        nose = root.getChild("nose");
        hatBrim = root.getChild("hat_brim");
        earRight = root.getChild("ear_right");
        earLeft = root.getChild("ear_left");
        leftArm = root.getChild("left_arm");
        rightArm = root.getChild("right_arm");
        shield = leftArm.getChild("shield");
        hatTop = root.getChild("hat_top");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 2.0F, 4.0F, 2.0F), PartPose.offset(1.0F, 20.0F, -1.0F));
        root.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(0, 16).addBox(0.0F, 0.0F, 0.0F, 2.0F, 4.0F, 2.0F), PartPose.offset(-3.0F, 20.0F, -1.0F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, 0.0F, 0.0F, 6.0F, 6.0F, 4.0F), PartPose.offset(-3.0F, 14.0F, -2.0F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(0.0F, 11.0F, 0.0F));
        root.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 22).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("hat_brim", CubeListBuilder.create().texOffs(12, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 1.0F, 4.0F), PartPose.offset(0.0F, 11.0F, 0.0F));
        root.addOrReplaceChild("ear_right", CubeListBuilder.create().texOffs(4, 22).addBox(1.0F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("ear_left", CubeListBuilder.create().texOffs(4, 22).addBox(-2.0F, 2.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 10.0F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
            CubeListBuilder.create().texOffs(20, 6).addBox(0.0F, 0.0F, 0.0F, 1.0F, 4.0F, 2.0F),
            PartPose.offsetAndRotation(3.0F, 15.0F, 0.0F, -1.570796F, 0.0F, 0.0F));
        leftArm.addOrReplaceChild("shield",
            CubeListBuilder.create().texOffs(8, 16).addBox(0.0F, 0.0F, 0.0F, 5.0F, 5.0F, 1.0F),
            PartPose.offsetAndRotation(-2.0F, 5.0F, -1.5F, 1.570796F, 0.0F, 0.0F));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(20, 6).addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 2.0F), PartPose.offsetAndRotation(-4.0F, 15.0F, 0.0F, -1.050296F, 0.0F, 0.0F));
        root.addOrReplaceChild("hat_top", CubeListBuilder.create().texOffs(28, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 1.0F, 3.0F), PartPose.offset(0.0F, 10.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        resetPose();
        if (entity.getType() == CCEntityTypes.BOSS_SUGUARD.get() && !entity.isBossSuguardAwake()) {
            setDormantBossPose();
            return;
        }

        float yaw = Mth.clamp(netHeadYaw, -55.0F, 55.0F) * DEG_TO_RAD;
        float pitch = Mth.clamp(headPitch, -35.0F, 35.0F) * DEG_TO_RAD;
        setHeadGroupRotation(yaw, pitch, 0.0F);

        boolean ridingBee = entity.getVehicle() instanceof CaramelBeeEntity;
        boolean drawingBow = entity.getType() == CCEntityTypes.BOSS_SUGUARD.get()
            && entity.getBossBowDrawTicks() > 0
            && (entity.getMainHandItem().getItem() instanceof BowItem
                || entity.getMainHandItem().getItem() instanceof CrossbowItem);
        float rawMovement = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        boolean moving = rawMovement > 0.0025F;
        if (ridingBee) {
            applyRidingPose();
        } else if (moving) {
            float movement = Mth.clamp(rawMovement * 2.6F, 0.16F, 1.0F);
            animateWalk(limbSwing, movement);
        } else if (!drawingBow && attackTime <= 0.0F) {
            animateIdle(entity, ageInTicks);
            blendIdlePose(getIdleHurtBlend(entity, Mth.clamp(ageInTicks - entity.tickCount, 0.0F, 1.0F)), yaw, pitch);
        }

        if (drawingBow) {
            animateBowAttack(entity, ageInTicks);
        } else if (attackTime > 0.0F) {
            animateMeleeAttack();
        }

        animateHurtHeadSpin(entity, ageInTicks);
    }

    private void resetPose() {
        leg1.resetPose();
        leg2.resetPose();
        body.resetPose();
        head.resetPose();
        nose.resetPose();
        hatBrim.resetPose();
        earRight.resetPose();
        earLeft.resetPose();
        leftArm.resetPose();
        rightArm.resetPose();
        shield.resetPose();
        hatTop.resetPose();
    }

    private void applyRidingPose() {
        // Legs forward and slightly apart, astride the bee like the vanilla
        // humanoid riding pose; arms grip the bee instead of standing rest.
        // Hinge the legs on the bottom edge of the torso's front face (z=-2,
        // same plane as the torso bottom). With the short 4px legs, hinging at
        // the default mid-torso pivot leaves a visible gap and reads as broken.
        leg1.z = -2.0F;
        leg1.xRot = -1.41F;
        leg1.yRot = 0.31F;
        leg1.zRot = 0.08F;
        leg2.z = -2.0F;
        leg2.xRot = -1.41F;
        leg2.yRot = -0.31F;
        leg2.zRot = -0.08F;
        rightArm.xRot = -0.85F;
        rightArm.zRot = -0.10F;
        leftArm.xRot = -1.15F;
        leftArm.zRot = 0.10F;
        body.xRot = 0.12F;
    }

    private void animateWalk(float limbSwing, float movement) {
        float cycle = limbSwing * 0.6662F;
        float leftStep = Mth.cos(cycle);
        float rightStep = Mth.cos(cycle + (float)Math.PI);
        float bob = Mth.abs(Mth.cos(cycle)) * 0.32F * movement;

        leg1.xRot = leftStep * 1.15F * movement;
        leg2.xRot = rightStep * 1.15F * movement;
        leg1.zRot = -leftStep * 0.035F * movement;
        leg2.zRot = -rightStep * 0.035F * movement;

        rightArm.xRot = RIGHT_ARM_REST_X + leftStep * 0.62F * movement;
        leftArm.xRot = LEFT_ARM_REST_X + rightStep * 0.62F * movement;
        rightArm.zRot = -rightStep * 0.055F * movement;
        leftArm.zRot = leftStep * 0.055F * movement;

        float torsoTurn = Mth.sin(cycle) * 0.075F * movement;
        body.yRot = torsoTurn;
        body.zRot = leftStep * 0.025F * movement;
        addHeadGroupYaw(-torsoTurn * 0.45F);
        offsetUpperBodyY(-bob);
    }

    private void animateIdle(T entity, float ageInTicks) {
        float offsetTime = ageInTicks + entity.getId() * 7.13F;
        float breath = Mth.sin(offsetTime * 0.09F);
        float sway = Mth.sin(offsetTime * 0.045F);

        offsetUpperBodyY(-0.055F * (breath + 1.0F));
        body.xRot = breath * 0.012F;
        rightArm.zRot = -0.025F - breath * 0.018F;
        leftArm.zRot = 0.025F + breath * 0.018F;

        switch (Math.floorMod(entity.getId(), 4)) {
            case 0 -> animateLookoutIdle(offsetTime);
            case 1 -> animateWeaponCheckIdle(offsetTime);
            case 2 -> animateWeightShiftIdle(sway);
            default -> {
                if (entity.getType() == CCEntityTypes.BOSS_SUGUARD.get()) {
                    animateBossShieldBowstringIdle(offsetTime);
                } else {
                    animateWeaponDanceIdle(entity, offsetTime);
                }
            }
        }
    }

    private void animateBossShieldBowstringIdle(float time) {
        float cycle = positiveModulo(time, DANCE_CYCLE_TICKS);
        float reach = smoothPulse(cycle, 14.0F, 78.0F);
        float pluckEnvelope = smoothPulse(cycle, 31.0F, 65.0F);
        float pluck = Mth.sin((cycle - 31.0F) * 1.45F) * pluckEnvelope;

        rightArm.xRot = RIGHT_ARM_REST_X - reach * 0.28F;
        rightArm.yRot = -reach * 0.10F;
        rightArm.zRot = -reach * 0.05F;
        leftArm.xRot = LEFT_ARM_REST_X + reach * 0.18F;
        leftArm.yRot = reach * 0.94F + pluck * 0.10F;
        leftArm.zRot = -reach * 0.16F + pluck * 0.055F;
        body.yRot = -reach * 0.08F;
        addHeadGroupYaw(-reach * 0.14F);
        addHeadGroupPitch(reach * 0.11F);
    }

    private void animateLookoutIdle(float time) {
        float look = Mth.sin(time * 0.026F) * 0.28F;
        float nod = Mth.sin(time * 0.053F) * 0.035F;
        addHeadGroupYaw(look);
        addHeadGroupPitch(nod);
        body.yRot = -look * 0.12F;
        rightArm.yRot = -look * 0.08F;
        leftArm.yRot = -look * 0.08F;
    }

    private void animateWeaponCheckIdle(float time) {
        float cycle = positiveModulo(time, 150.0F);
        float inspect = smoothPulse(cycle, 18.0F, 48.0F);
        rightArm.xRot = RIGHT_ARM_REST_X - inspect * 0.48F;
        rightArm.yRot = inspect * 0.22F;
        rightArm.zRot -= inspect * 0.12F;
        leftArm.xRot = LEFT_ARM_REST_X + inspect * 0.16F;
        addHeadGroupYaw(-inspect * 0.18F);
        addHeadGroupPitch(inspect * 0.12F);
    }

    private void animateWeightShiftIdle(float sway) {
        body.yRot = sway * 0.07F;
        body.zRot = sway * 0.035F;
        leg1.zRot = 0.025F + sway * 0.018F;
        leg2.zRot = -0.025F + sway * 0.018F;
        rightArm.xRot = RIGHT_ARM_REST_X + sway * 0.045F;
        leftArm.xRot = LEFT_ARM_REST_X - sway * 0.045F;
        addHeadGroupYaw(-body.yRot * 0.55F);
        addHeadGroupRoll(-body.zRot * 0.4F);
    }

    private void animateWeaponDanceIdle(T entity, float time) {
        float cycle = getDanceCycle(entity, time - entity.getId() * 7.13F);
        float flourish = smoothPulse(cycle, 8.0F, 54.0F);
        float handSweep = Mth.sin((cycle - 8.0F) * 0.40F) * flourish;
        float crouch = smoothPulse(cycle, 52.0F, 73.0F);
        float jump = getDanceJump(entity, time - entity.getId() * 7.13F);
        float landing = smoothPulse(cycle, 90.0F, 108.0F);
        float spinProgress = getDanceSpinProgress(entity, time - entity.getId() * 7.13F);

        rightArm.xRot = RIGHT_ARM_REST_X - flourish * 0.42F;
        rightArm.yRot = handSweep * 0.78F;
        rightArm.zRot = -flourish * 0.18F - handSweep * 0.30F;
        leftArm.xRot = LEFT_ARM_REST_X + flourish * 0.18F;
        leftArm.yRot = -handSweep * 0.16F;
        leftArm.zRot = flourish * 0.12F;
        body.yRot = -handSweep * 0.11F;
        addHeadGroupYaw(handSweep * 0.09F);

        float airbornePose = smootherStep(jump);
        rightArm.xRot = Mth.lerp(airbornePose, rightArm.xRot, -1.22F);
        rightArm.zRot = Mth.lerp(airbornePose, rightArm.zRot, -1.02F);
        leftArm.xRot = Mth.lerp(airbornePose, leftArm.xRot, -1.32F);
        leftArm.zRot = Mth.lerp(airbornePose, leftArm.zRot, 1.02F);
        leg1.xRot = airbornePose * 0.52F;
        leg2.xRot = -airbornePose * 0.52F;
        leg1.zRot = -airbornePose * 0.12F;
        leg2.zRot = airbornePose * 0.12F;
        body.zRot = Mth.sin(spinProgress * (float)Math.PI * 4.0F) * airbornePose * 0.055F;
        addHeadGroupRoll(-body.zRot * 0.6F);

        float compression = crouch * 0.22F + landing * 0.16F;
        offsetUpperBodyY(compression);
        leg1.xRot -= crouch * 0.20F + landing * 0.10F;
        leg2.xRot -= crouch * 0.20F + landing * 0.10F;
    }

    public static boolean hasDanceIdle(BasicCandyZombieEntity entity) {
        return Math.floorMod(entity.getId(), 4) == 3
            && entity.getType() != CCEntityTypes.BOSS_SUGUARD.get();
    }

    public static float getDanceJump(BasicCandyZombieEntity entity, float ageInTicks) {
        float progress = getDanceSpinProgress(entity, ageInTicks);
        return Mth.sin(progress * (float)Math.PI);
    }

    public static float getDanceSpinDegrees(BasicCandyZombieEntity entity, float ageInTicks) {
        return smootherStep(getDanceSpinProgress(entity, ageInTicks)) * 360.0F;
    }

    public static float getIdleHurtBlend(BasicCandyZombieEntity entity, float partialTick) {
        if (entity.hurtTime <= 0 || entity.hurtDuration <= 0) {
            return 1.0F;
        }
        float remaining = Mth.clamp((entity.hurtTime - partialTick) / entity.hurtDuration, 0.0F, 1.0F);
        float hurtEnvelope = Mth.sqrt(Math.max(0.0F, Mth.sin(remaining * (float)Math.PI)));
        return 1.0F - smootherStep(hurtEnvelope) * 0.94F;
    }

    private static float getDanceSpinProgress(BasicCandyZombieEntity entity, float ageInTicks) {
        float cycle = getDanceCycle(entity, ageInTicks);
        return Mth.clamp((cycle - DANCE_JUMP_START) / (DANCE_JUMP_END - DANCE_JUMP_START), 0.0F, 1.0F);
    }

    private static float getDanceCycle(BasicCandyZombieEntity entity, float ageInTicks) {
        return positiveModulo(ageInTicks + entity.getId() * 7.13F, DANCE_CYCLE_TICKS);
    }

    private void animateMeleeAttack() {
        float bodyTurn = Mth.sin(Mth.sqrt(attackTime) * ((float)Math.PI * 2.0F)) * 0.18F;
        float recovery = 1.0F - attackTime;
        recovery *= recovery;
        recovery *= recovery;
        recovery = 1.0F - recovery;
        float strike = Mth.sin(recovery * (float)Math.PI);
        float followThrough = Mth.sin(attackTime * (float)Math.PI);

        body.yRot += bodyTurn;
        rightArm.xRot = RIGHT_ARM_REST_X - strike * 1.25F - followThrough * 0.18F;
        rightArm.yRot += bodyTurn * 2.1F;
        rightArm.zRot -= followThrough * 0.34F;
        leftArm.xRot = LEFT_ARM_REST_X + strike * 0.42F;
        leftArm.yRot += bodyTurn * 1.35F;
        leftArm.zRot += followThrough * 0.16F;
        addHeadGroupYaw(-bodyTurn * 0.7F);
        addHeadGroupPitch(-followThrough * 0.07F);
    }

    private void animateBowAttack(T entity, float ageInTicks) {
        float partialTick = Mth.clamp(ageInTicks - entity.tickCount, 0.0F, 1.0F);
        float draw = entity.getBossBowDrawProgress(partialTick);
        float tension = Mth.sin(draw * (float)Math.PI) * 0.025F;

        body.yRot = -0.08F;
        rightArm.xRot = -1.48F + tension;
        rightArm.yRot = -0.18F;
        rightArm.zRot = -0.07F;
        leftArm.xRot = -1.42F - draw * 0.14F - tension;
        leftArm.yRot = 0.28F + draw * 0.42F;
        leftArm.zRot = 0.08F + draw * 0.08F;
        addHeadGroupYaw(0.06F + draw * 0.06F);
        addHeadGroupPitch(-0.035F);
    }

    private void animateHurtHeadSpin(T entity, float ageInTicks) {
        if (entity.hurtTime <= 0 || entity.hurtDuration <= 0) {
            return;
        }

        float partialTick = Mth.clamp(ageInTicks - entity.tickCount, 0.0F, 1.0F);
        float remaining = Mth.clamp((entity.hurtTime - partialTick) / entity.hurtDuration, 0.0F, 1.0F);
        float progress = smootherStep(1.0F - remaining);
        addHeadGroupYaw(progress * ((float)Math.PI * 2.0F));
    }

    private void setDormantBossPose() {
        setHeadGroupRotation(0.0F, 0.0F, 0.0F);
        rightArm.xRot = RIGHT_ARM_REST_X;
        leftArm.xRot = LEFT_ARM_REST_X;
    }

    private void setHeadGroupRotation(float yaw, float pitch, float roll) {
        head.yRot = yaw;
        nose.yRot = yaw;
        hatBrim.yRot = yaw;
        hatTop.yRot = yaw;
        earRight.yRot = yaw;
        earLeft.yRot = yaw;
        head.xRot = pitch;
        nose.xRot = pitch;
        hatBrim.xRot = pitch;
        hatTop.xRot = pitch;
        earRight.xRot = pitch;
        earLeft.xRot = pitch;
        head.zRot = roll;
        nose.zRot = roll;
        hatBrim.zRot = roll;
        hatTop.zRot = roll;
        earRight.zRot = roll;
        earLeft.zRot = roll;
    }

    private void addHeadGroupYaw(float amount) {
        head.yRot += amount;
        nose.yRot += amount;
        hatBrim.yRot += amount;
        hatTop.yRot += amount;
        earRight.yRot += amount;
        earLeft.yRot += amount;
    }

    private void addHeadGroupPitch(float amount) {
        head.xRot += amount;
        nose.xRot += amount;
        hatBrim.xRot += amount;
        hatTop.xRot += amount;
        earRight.xRot += amount;
        earLeft.xRot += amount;
    }

    private void addHeadGroupRoll(float amount) {
        head.zRot += amount;
        nose.zRot += amount;
        hatBrim.zRot += amount;
        hatTop.zRot += amount;
        earRight.zRot += amount;
        earLeft.zRot += amount;
    }

    private void offsetUpperBodyY(float amount) {
        body.y += amount;
        head.y += amount;
        nose.y += amount;
        hatBrim.y += amount;
        hatTop.y += amount;
        earRight.y += amount;
        earLeft.y += amount;
        leftArm.y += amount;
        rightArm.y += amount;
    }

    private void blendIdlePose(float blend, float baseHeadYaw, float baseHeadPitch) {
        leg1.xRot *= blend;
        leg1.yRot *= blend;
        leg1.zRot *= blend;
        leg2.xRot *= blend;
        leg2.yRot *= blend;
        leg2.zRot *= blend;
        body.xRot *= blend;
        body.yRot *= blend;
        body.zRot *= blend;
        rightArm.xRot = Mth.lerp(blend, RIGHT_ARM_REST_X, rightArm.xRot);
        rightArm.yRot *= blend;
        rightArm.zRot *= blend;
        leftArm.xRot = Mth.lerp(blend, LEFT_ARM_REST_X, leftArm.xRot);
        leftArm.yRot *= blend;
        leftArm.zRot *= blend;

        blendHeadGroupRotation(blend, baseHeadYaw, baseHeadPitch);
        body.y = Mth.lerp(blend, 14.0F, body.y);
        head.y = Mth.lerp(blend, 11.0F, head.y);
        nose.y = Mth.lerp(blend, 12.0F, nose.y);
        hatBrim.y = Mth.lerp(blend, 11.0F, hatBrim.y);
        hatTop.y = Mth.lerp(blend, 10.0F, hatTop.y);
        earRight.y = Mth.lerp(blend, 12.0F, earRight.y);
        earLeft.y = Mth.lerp(blend, 10.0F, earLeft.y);
        leftArm.y = Mth.lerp(blend, 15.0F, leftArm.y);
        rightArm.y = Mth.lerp(blend, 15.0F, rightArm.y);
    }

    private void blendHeadGroupRotation(float blend, float baseYaw, float basePitch) {
        blendHeadPart(head, blend, baseYaw, basePitch);
        blendHeadPart(nose, blend, baseYaw, basePitch);
        blendHeadPart(hatBrim, blend, baseYaw, basePitch);
        blendHeadPart(hatTop, blend, baseYaw, basePitch);
        blendHeadPart(earRight, blend, baseYaw, basePitch);
        blendHeadPart(earLeft, blend, baseYaw, basePitch);
    }

    private static void blendHeadPart(ModelPart part, float blend, float baseYaw, float basePitch) {
        part.xRot = Mth.lerp(blend, basePitch, part.xRot);
        part.yRot = Mth.lerp(blend, baseYaw, part.yRot);
        part.zRot *= blend;
    }

    private static float positiveModulo(float value, float modulus) {
        float result = value % modulus;
        return result < 0.0F ? result + modulus : result;
    }

    private static float smoothPulse(float value, float start, float end) {
        if (value <= start || value >= end) {
            return 0.0F;
        }
        return Mth.sin((value - start) / (end - start) * (float)Math.PI);
    }

    private static float smootherStep(float value) {
        float clamped = Mth.clamp(value, 0.0F, 1.0F);
        return clamped * clamped * clamped * (clamped * (clamped * 6.0F - 15.0F) + 10.0F);
    }

    public void translateToRightArm(PoseStack poseStack) {
        rightArm.translateAndRotate(poseStack);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        leg1.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leg2.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        head.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        nose.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        hatBrim.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        earRight.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        earLeft.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        leftArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        rightArm.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
        hatTop.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
