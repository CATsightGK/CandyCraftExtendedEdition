package com.valentin4311.candycraftmod.entity;

import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CottonCandySpiderEntity extends Spider {
    private int extraJumpCooldown;

    public CottonCandySpiderEntity(EntityType<? extends CottonCandySpiderEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!CandyTargeting.canAttackEntity(target)) {
            setTarget(null);
            return false;
        }
        if (super.doHurtTarget(target)) {
            if (target instanceof LivingEntity living) {
                int seconds = level().getDifficulty() == Difficulty.HARD ? 15 : level().getDifficulty() == Difficulty.NORMAL ? 7 : 0;
                if (seconds > 0) {
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, seconds * 20), this);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (extraJumpCooldown > 0) {
            extraJumpCooldown--;
        }
        LivingEntity target = getTarget();
        if (!level().isClientSide && onGround() && extraJumpCooldown <= 0 && target != null && distanceToSqr(target) < 144.0D) {
            Vec3 toward = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
            if (toward.lengthSqr() > 1.0E-4D) {
                toward = toward.normalize().scale(0.28D);
            }
            setDeltaMovement(toward.x, 0.72D, toward.z);
            hasImpulse = true;
            extraJumpCooldown = 12 + random.nextInt(13);
        }
    }

    @Override
    protected void jumpFromGround() {
        super.jumpFromGround();
        setDeltaMovement(getDeltaMovement().multiply(1.0D, 1.65D, 1.0D));
    }
}
