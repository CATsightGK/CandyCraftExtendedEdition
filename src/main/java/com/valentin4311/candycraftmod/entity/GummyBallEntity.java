package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCSweetscapeItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class GummyBallEntity extends ThrowableItemProjectile {
    public static final int RED_JELLY_VISUAL = 100;
    private static final EntityDataAccessor<Integer> POWER = SynchedEntityData.defineId(GummyBallEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> VISUAL_VARIANT = SynchedEntityData.defineId(GummyBallEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> BONUS_DAMAGE = SynchedEntityData.defineId(GummyBallEntity.class, EntityDataSerializers.FLOAT);

    public GummyBallEntity(EntityType<? extends GummyBallEntity> entityType, Level level) {
        super(entityType, level);
    }

    public GummyBallEntity(Level level, LivingEntity owner, int power) {
        super(CCEntityTypes.GUMMY_BALL.get(), owner, level);
        setPower(power);
    }

    public GummyBallEntity(Level level, double x, double y, double z) {
        super(CCEntityTypes.GUMMY_BALL.get(), x, y, z, level);
    }

    public void setPower(int power) {
        entityData.set(POWER, power);
    }

    public int getPower() {
        return entityData.get(POWER);
    }

    public void setVisualVariant(int variant) {
        entityData.set(VISUAL_VARIANT, variant == RED_JELLY_VISUAL ? RED_JELLY_VISUAL : Math.max(0, Math.min(2, variant)));
    }

    public int getVisualVariant() {
        return entityData.get(VISUAL_VARIANT);
    }

    public void setBonusDamage(float damage) {
        entityData.set(BONUS_DAMAGE, Math.max(0.0F, damage));
    }

    public float getBonusDamage() {
        return entityData.get(BONUS_DAMAGE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(POWER, 0);
        entityData.define(VISUAL_VARIANT, 0);
        entityData.define(BONUS_DAMAGE, 0.0F);
    }

    @Override
    protected Item getDefaultItem() {
        return CCItems.GUMMY_BALL.get();
    }

    @Override
    public ItemStack getItem() {
        int variant = getVisualVariant();
        if (variant == RED_JELLY_VISUAL) {
            return new ItemStack(CCSweetscapeItems.RED_GUMMY.get());
        }
        ItemStack stack = new ItemStack(getDefaultItem());
        if (variant > 0) {
            stack.getOrCreateTag().putInt("CustomModelData", variant);
        }
        return stack;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && getPower() == 1) {
            spawnBreakParticle();
        } else if (level().isClientSide && getPower() == 2) {
            level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        } else if (level().isClientSide && getPower() >= 3 && tickCount % 2 == 0) {
            spawnBreakParticle();
        }
    }

    @Override
    protected float getGravity() {
        return 0.03F;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (result.getType() == HitResult.Type.ENTITY) {
            hitEntity((EntityHitResult) result);
        }

        if (level().isClientSide) {
            for (int i = 0; i < 8; i++) {
                spawnBreakParticle();
            }
        } else {
            level().broadcastEntityEvent(this, (byte)3);
        }

        if (!level().isClientSide) {
            discard();
        }
    }

    private void hitEntity(EntityHitResult result) {
        if (level().isClientSide) {
            return;
        }
        Entity entity = result.getEntity();
        int power = getPower();
        float damage = getBonusDamage() > 0.0F ? getBonusDamage() : power == 1 ? 6.0F : power == 2 ? 4.0F : power == 3 ? 3.0F : power == 4 ? 8.0F : 0.1F;
        entity.hurt(damageSources().thrown(this, getOwner()), damage);
        if (entity instanceof LivingEntity living && (power == 3 || power == 4)) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5 * 20, 0));
        } else if (entity instanceof LivingEntity living && power < 2) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5 * 20, 2));
        } else if (power == 2) {
            entity.setSecondsOnFire(7);
        }
    }

    private void spawnBreakParticle() {
        if (!level().isClientSide) {
            return;
        }
        level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, getItem()),
            getX(), getY(), getZ(),
            (random.nextDouble() - 0.5D) * 0.1D,
            (random.nextDouble() - 0.5D) * 0.1D,
            (random.nextDouble() - 0.5D) * 0.1D);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; i++) {
                spawnBreakParticle();
            }
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Power", getPower());
        tag.putInt("VisualVariant", getVisualVariant());
        tag.putFloat("BonusDamage", getBonusDamage());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setPower(tag.getInt("Power"));
        setVisualVariant(tag.getInt("VisualVariant"));
        setBonusDamage(tag.getFloat("BonusDamage"));
    }
}
