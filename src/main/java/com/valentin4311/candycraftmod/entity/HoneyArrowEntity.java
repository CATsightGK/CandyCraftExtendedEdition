package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCMobEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class HoneyArrowEntity extends AbstractArrow {
    private static final double CROSSBOW_DAMAGE_MULTIPLIER = 1.75D;
    private static final int BOSS_SUGUARD_GROUND_DESPAWN_TICKS = 10 * 20;
    private static final int HONEY_GLUE_DURATION_TICKS = 5 * 20;
    private boolean crossbowDamageApplied;
    private boolean honeyGlue;
    private boolean bossSuguardProjectile;
    private int bossSuguardGroundTicks;

    public HoneyArrowEntity(EntityType<? extends HoneyArrowEntity> entityType, Level level) {
        super(entityType, level);
    }

    public HoneyArrowEntity(Level level, LivingEntity owner) {
        super(CCEntityTypes.HONEY_ARROW.get(), owner, level);
    }

    public HoneyArrowEntity(Level level, double x, double y, double z) {
        super(CCEntityTypes.HONEY_ARROW.get(), x, y, z, level);
    }

    @Override
    protected ItemStack getPickupItem() {
        return new ItemStack(CCItems.HONEY_ARROW.get());
    }

    public void setHoneyGlue(boolean honeyGlue) {
        this.honeyGlue = honeyGlue;
    }

    public void markBossSuguardProjectile() {
        bossSuguardProjectile = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && bossSuguardProjectile) {
            if (inGround) {
                if (++bossSuguardGroundTicks >= BOSS_SUGUARD_GROUND_DESPAWN_TICKS) {
                    discard();
                    return;
                }
            } else {
                bossSuguardGroundTicks = 0;
            }
        }
        if (!inGround && !isInWater() && isInFluidType()) {
            setDeltaMovement(getDeltaMovement().scale(0.6D));
        }
    }

    @Override
    protected float getWaterInertia() {
        return 0.6F;
    }

    @Override
    protected void doPostHurtEffects(LivingEntity living) {
        super.doPostHurtEffects(living);
        if (!level().isClientSide && honeyGlue) {
            living.addEffect(new MobEffectInstance(CCMobEffects.HONEY_GLUE.get(), HONEY_GLUE_DURATION_TICKS), getEffectSource());
        }
        if (!level().isClientSide && getPierceLevel() <= 0 && living instanceof CandyStuckProjectileCarrier carrier) {
            living.setArrowCount(Math.max(0, living.getArrowCount() - 1));
            carrier.candycraft$setHoneyArrowCount(carrier.candycraft$getHoneyArrowCount() + 1);
        }
    }

    @Override
    public void setShotFromCrossbow(boolean shotFromCrossbow) {
        super.setShotFromCrossbow(shotFromCrossbow);
        if (shotFromCrossbow && !crossbowDamageApplied) {
            setBaseDamage(getBaseDamage() * CROSSBOW_DAMAGE_MULTIPLIER);
            crossbowDamageApplied = true;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("HoneyGlue", honeyGlue);
        tag.putBoolean("CrossbowDamageApplied", crossbowDamageApplied);
        tag.putBoolean("BossSuguardProjectile", bossSuguardProjectile);
        tag.putInt("BossSuguardGroundTicks", bossSuguardGroundTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        honeyGlue = tag.getBoolean("HoneyGlue") || tag.getBoolean("Propolis") || tag.getBoolean("Slow");
        crossbowDamageApplied = tag.getBoolean("CrossbowDamageApplied");
        bossSuguardProjectile = tag.getBoolean("BossSuguardProjectile");
        bossSuguardGroundTicks = tag.getInt("BossSuguardGroundTicks");
    }
}
