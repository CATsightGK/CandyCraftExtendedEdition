package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class GummyBallEntity extends ThrowableItemProjectile {
    public static final int LEMON_JELLY_VISUAL = 101;
    public static final int RASPBERRY_JELLY_VISUAL = 102;
    public static final int MINT_JELLY_VISUAL = 103;
    public static final int PEZ_JELLY_VISUAL = 104;
    public static final int CARAMEL_KING_JELLY_VISUAL = 105;
    public static final int STRAWBERRY_QUEEN_JELLY_VISUAL = 106;
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
        if (variant >= LEMON_JELLY_VISUAL && variant <= STRAWBERRY_QUEEN_JELLY_VISUAL) {
            entityData.set(VISUAL_VARIANT, variant);
        } else {
            entityData.set(VISUAL_VARIANT, 0);
        }
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
        if (variant == LEMON_JELLY_VISUAL) {
            return new ItemStack(CCItems.LEMON_JELLY_BALL.get());
        }
        if (variant == RASPBERRY_JELLY_VISUAL) {
            return new ItemStack(CCItems.RASPBERRY_JELLY_BALL.get());
        }
        if (variant == MINT_JELLY_VISUAL) {
            return new ItemStack(CCItems.MINT_JELLY_BALL.get());
        }
        if (variant == PEZ_JELLY_VISUAL) {
            return new ItemStack(CCItems.PEZ_JELLY_BALL.get());
        }
        if (variant == CARAMEL_KING_JELLY_VISUAL) {
            return new ItemStack(CCItems.CARAMEL_KING_JELLY_BALL.get());
        }
        if (variant == STRAWBERRY_QUEEN_JELLY_VISUAL) {
            return new ItemStack(CCItems.STRAWBERRY_QUEEN_JELLY_BALL.get());
        }
        return new ItemStack(getDefaultItem());
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && (getPower() == 1 || getPower() >= 3) && tickCount % 2 == 0) {
            spawnBreakParticle();
        } else if (level().isClientSide && getPower() == 2) {
            level().addParticle(ParticleTypes.FLAME, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected float getGravity() {
        return 0.03F;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 1.0F;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (getPower() != 3 || source.getEntity() == null) {
            return false;
        }
        Vec3 look = source.getEntity().getLookAngle();
        setOwner(source.getEntity());
        setDeltaMovement(look.x, look.y, look.z);
        hasImpulse = true;
        markHurt();
        return true;
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.ENTITY && ((EntityHitResult) result).getEntity() instanceof GummyBallEntity) {
            return;
        }
        if (result.getType() == HitResult.Type.ENTITY && ((EntityHitResult) result).getEntity() == getOwner() && tickCount < 8) {
            return;
        }
        super.onHit(result);
        if (result.getType() == HitResult.Type.ENTITY) {
            if (!hitEntity((EntityHitResult) result)) {
                return;
            }
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

    private boolean hitEntity(EntityHitResult result) {
        if (level().isClientSide) {
            return true;
        }
        Entity entity = result.getEntity();
        int power = getPower();
        float damage = getBonusDamage() > 0.0F ? getBonusDamage() : power == 1 ? 6.0F : power == 2 ? 4.0F : power == 3 ? 3.0F : power == 4 ? 8.0F : 0.1F;
        if (entity instanceof BasicCandySlimeEntity candy && (candy.isPezJelly() || candy.isKingSlime() || candy.isJellyQueen())) {
            entity.hurt(damageSources().thrown(this, getOwner()), damage);
            return false;
        }
        entity.hurt(damageSources().thrown(this, getOwner()), damage);
        if (power == 5) {
            return true;
        }
        if (entity instanceof LivingEntity living && (power == 3 || power == 4)) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5 * 20, 0));
        } else if (entity instanceof LivingEntity living && power < 2) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5 * 20, 2));
        } else if (power == 2) {
            entity.setSecondsOnFire(7);
        }
        return true;
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
