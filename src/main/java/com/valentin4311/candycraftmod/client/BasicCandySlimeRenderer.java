package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.BasicCandySlimeEntity;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Slime;

public class BasicCandySlimeRenderer extends SlimeRenderer {
    private static final float PEZ_ROLL_VISUAL_END = 0.5F;
    private static final float PEZ_ATTACH_VISUAL_END = 0.71875F;
    private static final float PEZ_ATTACK_VISUAL_END = 0.8125F;

    public BasicCandySlimeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Slime entity) {
        String texture = "sprinterjelly.png";
        if (entity.getType() == CCEntityTypes.RED_JELLY.get()) texture = "kamikazejelly.png";
        else if (entity.getType() == CCEntityTypes.TORNADO_JELLY.get()) texture = "tornadojelly.png";
        else if (entity instanceof BasicCandySlimeEntity candy) texture = getCandySlimeTexture(candy);
        return new ResourceLocation(CandyCraft.MODID, "textures/entity/" + texture);
    }

    @Override
    protected void setupRotations(Slime entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        if (entity.getType() == CCEntityTypes.TORNADO_JELLY.get() && !entity.onGround()) {
            float flip = (entity.tickCount + partialTicks) * 28.0F;
            poseStack.mulPose(Axis.XP.rotationDegrees(flip));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin((entity.tickCount + partialTicks) * 0.35F) * 14.0F));
        }
        if (entity instanceof BasicCandySlimeEntity candy && (candy.isPezJelly() || candy.isKingSlime() || candy.isJellyQueen())) {
            applyBossSlamPose(candy, poseStack, partialTicks);
            if (candy.isPezJelly() || candy.isKingSlime() || candy.isJellyQueen()) {
                applyKingSpecialPose(candy, poseStack, partialTicks);
            }
            if (candy.isKingSlime() || candy.isJellyQueen()) {
                applyBossBouncePose(candy, poseStack, partialTicks);
            }
            if (candy.isPezJelly()) {
                applyPezRollPose(candy, poseStack, partialTicks);
            }
            applyBossRestPose(candy, poseStack, partialTicks);
            applyJellyHurtWobble(candy, poseStack, partialTicks);
        }
    }

    private static String getJellyQueenTexture(BasicCandySlimeEntity queen) {
        if (!queen.isBossAwake()) {
            return "candyboss4.png";
        }
        return switch (queen.getJellyQueenMode()) {
            case BasicCandySlimeEntity.JELLY_QUEEN_BLUE_MODE -> "candyboss2.png";
            case BasicCandySlimeEntity.JELLY_QUEEN_BROWN_MODE -> "candyboss3.png";
            default -> "candyboss.png";
        };
    }

    private static String getCandySlimeTexture(BasicCandySlimeEntity candy) {
        if (candy.isPezJelly()) {
            return candy.isBossAwake() ? "candyboss5.png" : "candyboss4.png";
        }
        if (candy.isKingSlime()) {
            return candy.isBossAwake() ? "candyboss6.png" : "candyboss4.png";
        }
        if (candy.isJellyQueen()) {
            return getJellyQueenTexture(candy);
        }
        return "sprinterjelly.png";
    }

    private static void applyBossSlamPose(BasicCandySlimeEntity candy, PoseStack poseStack, float partialTicks) {
        float slam = candy.getBossSlamProgress(partialTicks);
        if (slam <= 0.0F) {
            return;
        }
        float t = 1.0F - slam;
        float eased = smootherStep(t);
        float settle = 1.0F - eased;
        float motionTime = candy.tickCount + partialTicks;
        if (!candy.onGround()) {
            boolean falling = candy.getDeltaMovement().y < -0.03D;
            float hang = smootherStep(1.0F - Math.abs(t * 2.0F - 1.0F));
            float fallCurve = smootherStep(Math.min(1.0F, t * 1.25F));
            float stretchCurve = falling ? fallCurve : hang;
            float bossScale = candy.isKingSlime() ? 1.18F : candy.isPezJelly() ? 1.08F : 1.0F;
            float stretch = (falling ? 0.19F : 0.09F) * stretchCurve * bossScale;
            float squash = 0.03F * stretchCurve * bossScale;
            float bob = (float) Math.sin(motionTime * 0.13F) * 0.006F * settle;
            poseStack.translate(0.0F, -0.03F * stretchCurve + 0.014F * hang + bob, 0.0F);
            poseStack.scale(1.0F - squash, 1.0F + stretch, 1.0F - squash);
            float liftPitch = lerp(smootherStep(Math.min(1.0F, t * 2.2F)), 0.0F, -7.0F);
            float pitch = falling ? lerp(fallCurve, liftPitch, 12.0F) : liftPitch * hang;
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin(motionTime * 0.08F) * 2.1F * settle));
        } else {
            float impact = 1.0F - smootherStep(Math.min(1.0F, t * 3.2F));
            float recover = 1.0F - smootherStep(t);
            float rebound = (float) Math.sin(Math.min(1.0F, t) * Math.PI * 3.8F) * recover;
            float bossScale = candy.isKingSlime() ? 1.25F : candy.isPezJelly() ? 1.1F : 1.0F;
            float squash = 0.13F * impact * bossScale + 0.028F * Math.max(0.0F, rebound);
            float yScale = 1.0F - squash * 0.76F;
            float xzScale = 1.0F + squash;
            poseStack.translate(0.0F, -0.052F * impact + 0.012F * rebound, 0.0F);
            poseStack.scale(xzScale, yScale, xzScale);
            poseStack.mulPose(Axis.XP.rotationDegrees(rebound * 1.6F));
        }
    }

    private static void applyKingSpecialPose(BasicCandySlimeEntity king, PoseStack poseStack, float partialTicks) {
        float expand = king.getKingExpandProgress(partialTicks);
        if (expand > 0.0F) {
            float t = 1.0F - expand;
            float shrink = smootherStep(Math.min(1.0F, t / 0.42F));
            float burst = smootherStep(Math.max(0.0F, (t - 0.42F) / 0.14F));
            float recover = smootherStep(Math.max(0.0F, (t - 0.56F) / 0.34F));
            float elastic = (float) Math.sin(Math.max(0.0F, t - 0.48F) / 0.52F * Math.PI * 3.0F) * (1.0F - recover) * 0.08F;
            float scale = lerp(shrink, 1.0F, 0.5F);
            scale = lerp(burst, scale, 1.5F);
            scale = lerp(recover, scale, 1.0F) + elastic;
            float y = scale;
            float xz = scale;
            if (burst > 0.0F && recover < 1.0F) {
                float flatten = (1.0F - recover) * burst;
                xz += 0.18F * flatten;
                y -= 0.16F * flatten;
            }
            poseStack.translate(0.0F, 0.18F * shrink - 0.18F * burst, 0.0F);
            poseStack.scale(xz, y, xz);
        }

        float dash = king.getKingDashProgress(partialTicks);
        if (dash > 0.0F) {
            float charge = king.getKingDashChargeProgress(partialTicks);
            float motion = smootherStep(Math.max(0.0F, 1.0F - dash - 0.35F) / 0.65F);
            float pulse = (float) Math.sin((king.tickCount + partialTicks) * 0.42F) * (1.0F - charge) * 0.025F;
            float amount = king.isJellyQueen() ? 0.62F : 1.0F;
            poseStack.scale(1.0F + (-0.1F * charge + pulse) * amount, 1.0F + 0.16F * charge * amount, 1.0F + (-0.1F * charge + pulse) * amount);
            poseStack.mulPose(Axis.XP.rotationDegrees((-9.0F * charge + 5.0F * motion) * amount));
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) Math.sin((king.tickCount + partialTicks) * 0.26F) * 4.0F * motion * amount));
        }
    }

    private static void applyBossBouncePose(BasicCandySlimeEntity boss, PoseStack poseStack, float partialTicks) {
        float bounce = boss.getBossBounceProgress(partialTicks);
        if (bounce <= 0.0F) {
            return;
        }
        float elapsed = 1.0F - bounce;
        float charge = smootherStep(Math.min(1.0F, elapsed / 0.62F));
        float snap = smootherStep(Math.max(0.0F, (elapsed - 0.62F) / 0.16F));
        float settle = 1.0F - smootherStep(Math.max(0.0F, (elapsed - 0.76F) / 0.24F));
        float amount = boss.isJellyQueen() ? 0.62F : 1.0F;
        float wobble = (float) Math.sin(elapsed * Math.PI * 5.0F) * settle * 0.055F * amount;
        float xz = 1.0F - 0.24F * charge * amount + 0.55F * snap * amount + wobble;
        float y = 1.0F + 0.22F * charge * amount - 0.34F * snap * amount - wobble * 0.5F;
        poseStack.translate(0.0F, 0.05F * charge - 0.08F * snap, 0.0F);
        poseStack.scale(xz, y, xz);
    }

    private static void applyPezRollPose(BasicCandySlimeEntity pez, PoseStack poseStack, float partialTicks) {
        float roll = pez.getPezRollProgress(partialTicks);
        if (roll <= 0.0F) {
            return;
        }
        float elapsed = pez.getPezRollElapsed(partialTicks);
        float time = pez.tickCount + partialTicks;
        if (elapsed < PEZ_ROLL_VISUAL_END) {
            Direction face = pez.getPezAttachFace();
            Direction direction = pez.getPezRollDirection();
            applyPezAttachRotation(face, poseStack);
            applyPezBbmodelRollAnimation(pez, face, direction, poseStack, partialTicks);
            applyPezSurfaceOffset(face, poseStack, 0.22F);
        } else if (elapsed < PEZ_ATTACH_VISUAL_END) {
            Direction face = pez.getPezAttachFace();
            applyPezAttachRotation(face, poseStack);
            applyPezSurfaceOffset(face, poseStack, 0.34F);
            float charge = smootherStep((elapsed - PEZ_ROLL_VISUAL_END) / (PEZ_ATTACH_VISUAL_END - PEZ_ROLL_VISUAL_END));
            float throb = (float)Math.sin(time * 0.62F) * (0.04F + 0.035F * charge);
            poseStack.translate(0.0F, -0.02F + 0.035F * charge, 0.0F);
            poseStack.scale(1.08F + 0.14F * charge + throb, 0.78F - 0.16F * charge - throb * 0.35F, 1.08F + 0.10F * charge - throb * 0.25F);
        } else if (elapsed < PEZ_ATTACK_VISUAL_END) {
            float attack = smootherStep((elapsed - PEZ_ATTACH_VISUAL_END) / (PEZ_ATTACK_VISUAL_END - PEZ_ATTACH_VISUAL_END));
            float snap = (float) Math.sin(attack * Math.PI) * 0.22F;
            poseStack.scale(0.72F + attack * 0.52F, 1.42F - attack * 0.50F, 0.72F + snap);
            poseStack.translate(0.0F, 0.10F * (1.0F - attack) - 0.08F * snap, 0.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-28.0F + attack * 48.0F));
        } else {
            float rest = smootherStep((elapsed - PEZ_ATTACK_VISUAL_END) / (1.0F - PEZ_ATTACK_VISUAL_END));
            float drip = (float) Math.sin(time * 0.48F) * (1.0F - rest) * 0.035F;
            poseStack.translate(0.0F, 0.03F * (1.0F - rest), 0.0F);
            poseStack.scale(1.12F + drip, 0.78F + rest * 0.18F, 1.12F - drip * 0.4F);
        }
    }

    private static void applyPezBbmodelRollAnimation(BasicCandySlimeEntity pez, Direction face, Direction direction, PoseStack poseStack, float partialTicks) {
        float angle = pezBbmodelRollAngle(pez, partialTicks);
        float centerY = pez.getBbHeight() * 0.5F;
        poseStack.translate(0.0F, centerY, 0.0F);
        net.minecraft.world.phys.Vec3 movement = pez.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D);
        if (face == Direction.UP && movement.lengthSqr() > 1.0E-4D) {
            movement = movement.normalize();
            org.joml.Vector3f axis = new org.joml.Vector3f((float)movement.z, 0.0F, (float)-movement.x).normalize();
            poseStack.mulPose(new org.joml.Quaternionf().rotationAxis((float)Math.toRadians(angle), axis.x, axis.y, axis.z));
        } else if (direction == Direction.EAST || direction == Direction.WEST) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(direction == Direction.EAST ? -angle : angle));
        } else if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            poseStack.mulPose(Axis.XP.rotationDegrees(direction == Direction.NORTH ? -angle : angle));
        } else if (face == Direction.EAST || face == Direction.WEST) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(direction == Direction.UP ? angle : -angle));
        } else {
            poseStack.mulPose(Axis.XP.rotationDegrees(direction == Direction.UP ? -angle : angle));
        }
        poseStack.translate(0.0F, -centerY, 0.0F);
    }

    private static float pezBbmodelRollAngle(BasicCandySlimeEntity pez, float partialTicks) {
        double circumference = Math.max(0.25D, pez.getBbWidth()) * Math.PI;
        double predictedDistance = pez.getPezRollDistance() + pez.getDeltaMovement().length() * partialTicks;
        return (float)(predictedDistance / circumference * 360.0D);
    }

    private static void applyBossRestPose(BasicCandySlimeEntity candy, PoseStack poseStack, float partialTicks) {
        float rest = candy.getBossRestingProgress(partialTicks);
        if (rest <= 0.0F) {
            return;
        }
        float elapsed = 1.0F - rest;
        float recover = smootherStep(elapsed);
        float drip = (float) Math.sin((candy.tickCount + partialTicks) * 0.48F) * (1.0F - recover) * 0.035F;
        poseStack.translate(0.0F, 0.03F * (1.0F - recover), 0.0F);
        poseStack.scale(1.12F + drip, 0.78F + recover * 0.18F, 1.12F - drip * 0.4F);
    }

    private static void applyPezAttachRotation(Direction face, PoseStack poseStack) {
        if (face == Direction.DOWN) {
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            poseStack.translate(0.0F, -0.15F, 0.0F);
        } else if (face == Direction.NORTH) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        } else if (face == Direction.SOUTH) {
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        } else if (face == Direction.WEST) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
        } else if (face == Direction.EAST) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        }
    }

    private static void applyPezSurfaceOffset(Direction face, PoseStack poseStack, float amount) {
        if (face == Direction.DOWN) {
            poseStack.translate(0.0F, -amount, 0.0F);
        } else if (face != Direction.UP) {
            poseStack.translate(0.0F, amount, 0.0F);
        }
    }

    private static void applyPezMintRollRotation(BasicCandySlimeEntity pez, Direction face, Direction direction, float partialTicks, PoseStack poseStack) {
        float time = pez.tickCount + partialTicks;
        float angle = time * 28.0F;
        float sway = (float) Math.sin(time * 0.35F) * 10.0F;
        if (face == Direction.UP || face == Direction.DOWN) {
            if (direction == Direction.EAST || direction == Direction.WEST) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(direction == Direction.EAST ? -angle : angle));
            } else {
                poseStack.mulPose(Axis.XP.rotationDegrees(direction == Direction.NORTH ? -angle : angle));
            }
            poseStack.mulPose(Axis.ZP.rotationDegrees(sway));
            return;
        }
        if (face.getAxis() == Direction.Axis.X) {
            if (direction.getAxis() == Direction.Axis.Y) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(direction == Direction.UP ? angle : -angle));
            } else {
                poseStack.mulPose(Axis.YP.rotationDegrees(direction == Direction.NORTH ? angle : -angle));
            }
            poseStack.mulPose(Axis.XP.rotationDegrees(sway));
            return;
        }
        if (direction.getAxis() == Direction.Axis.Y) {
            poseStack.mulPose(Axis.XP.rotationDegrees(direction == Direction.UP ? -angle : angle));
        } else {
            poseStack.mulPose(Axis.YP.rotationDegrees(direction == Direction.EAST ? -angle : angle));
        }
        poseStack.mulPose(Axis.ZP.rotationDegrees(sway));
    }

    private static void applyJellyHurtWobble(BasicCandySlimeEntity candy, PoseStack poseStack, float partialTicks) {
        if (candy.hurtTime <= 0) {
            return;
        }
        float hurt = (candy.hurtTime - partialTicks) / 10.0F;
        hurt = Math.max(0.0F, Math.min(1.0F, hurt));
        float wave = (float) Math.sin(hurt * Math.PI * 4.0F);
        float wobble = wave * hurt;
        poseStack.scale(1.0F + wobble * 0.028F, 1.0F - wobble * 0.022F, 1.0F + wobble * 0.018F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(wobble * 2.2F));
    }

    private static float smootherStep(float value) {
        float clamped = Math.max(0.0F, Math.min(1.0F, value));
        return clamped * clamped * clamped * (clamped * (clamped * 6.0F - 15.0F) + 10.0F);
    }

    private static float lerp(float amount, float from, float to) {
        return from + (to - from) * amount;
    }
}
