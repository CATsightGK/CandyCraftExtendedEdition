package com.valentin4311.candycraftmod.entity;

import javax.annotation.Nullable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

/**
 * Shared server-side mob behaviors extracted from the candy slime/zombie/spider
 * families, which previously carried byte-identical private copies.
 */
public final class CandyMobHelper {
    public static final int DORMANT_BOSS_HEAL_INTERVAL_TICKS = 20;
    public static final float DORMANT_BOSS_HEAL_AMOUNT = 5.0F;

    private CandyMobHelper() {
    }

    @Nullable
    public static LivingEntity getLivingAttacker(DamageSource source) {
        if (source.getEntity() instanceof LivingEntity attacker) {
            return attacker;
        }
        if (source.getDirectEntity() instanceof LivingEntity attacker) {
            return attacker;
        }
        return null;
    }

    public static void stopHorizontalMovement(Entity entity, boolean clampUpwardMovement) {
        Vec3 movement = entity.getDeltaMovement();
        double verticalMovement = clampUpwardMovement ? Math.min(0.0D, movement.y) : movement.y;
        if (movement.x != 0.0D || movement.z != 0.0D || movement.y != verticalMovement) {
            entity.setDeltaMovement(0.0D, verticalMovement, 0.0D);
        }
    }

    public static void setMovementSpeedBase(Mob mob, double value) {
        setBaseValueIfChanged(mob.getAttribute(Attributes.MOVEMENT_SPEED), value);
    }

    public static void setBaseValueIfChanged(@Nullable AttributeInstance attribute, double value) {
        if (attribute != null && attribute.getBaseValue() != value) {
            attribute.setBaseValue(value);
        }
    }

    /** Heals a dormant (sleeping) boss on the legacy interval; returns true when a heal fired this tick. */
    public static boolean tickDormantBossRegeneration(LivingEntity boss) {
        if (boss.tickCount % DORMANT_BOSS_HEAL_INTERVAL_TICKS == 0) {
            boss.heal(DORMANT_BOSS_HEAL_AMOUNT);
            return true;
        }
        return false;
    }
}
