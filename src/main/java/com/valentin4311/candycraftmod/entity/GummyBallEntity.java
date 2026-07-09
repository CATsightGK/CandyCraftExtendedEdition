package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
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
    private static final EntityDataAccessor<Boolean> BOSS_BEETLE_PROJECTILE = SynchedEntityData.defineId(GummyBallEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> AIR_STATE = SynchedEntityData.defineId(GummyBallEntity.class, EntityDataSerializers.INT);

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

    public void setBossBeetleProjectile(boolean bossBeetleProjectile) {
        entityData.set(BOSS_BEETLE_PROJECTILE, bossBeetleProjectile);
    }

    public boolean isBossBeetleProjectile() {
        Entity owner = getOwner();
        return entityData.get(BOSS_BEETLE_PROJECTILE)
            || owner != null && owner.getType() == CCEntityTypes.BOSS_BEETLE.get();
    }

    public void setAirState(int airState) {
        entityData.set(AIR_STATE, airState);
    }

    public int getAirState() {
        return entityData.get(AIR_STATE);
    }

    public void shootFromCandySource(LivingEntity owner, float velocityOverride) {
        shootFromCandySource(owner, owner.getYRot(), owner.getXRot(), velocityOverride);
    }

    public void shootFromCandySource(LivingEntity owner, float yaw, float pitch, float velocityOverride) {
        float yawRad = yaw / 180.0F * (float)Math.PI;
        float pitchRad = pitch / 180.0F * (float)Math.PI;
        float offsetX = random.nextFloat() / 20.0F - 0.05F;
        float offsetZ = random.nextFloat() / 20.0F - 0.05F;
        if (getPower() == 0 || getPower() == 3) {
            offsetX = 0.0F;
            offsetZ = 0.0F;
        }

        setPos(owner.getX() - Mth.cos(yawRad) * 0.16D,
            owner.getY() + owner.getEyeHeight() - 0.10000000149011612D,
            owner.getZ() - Mth.sin(yawRad) * 0.16D);

        float baseVelocity = getPower() == 3 ? 0.002F : 0.4F;
        double motionX = -Mth.sin(yawRad) * Mth.cos(pitchRad) * baseVelocity;
        double motionZ = Mth.cos(yawRad) * Mth.cos(pitchRad) * baseVelocity;
        double motionY = -Mth.sin((pitch + 1.0F) / 180.0F * (float)Math.PI) * baseVelocity;
        shoot(motionX + offsetX, motionY, motionZ + offsetZ, velocityOverride, 1.0F);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(POWER, 0);
        entityData.define(VISUAL_VARIANT, 0);
        entityData.define(BONUS_DAMAGE, 0.0F);
        entityData.define(BOSS_BEETLE_PROJECTILE, false);
        entityData.define(AIR_STATE, 0);
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
        if (getPower() == 2) {
            return gummyBallVariant(1);
        }
        if (getPower() == 3) {
            return gummyBallVariant(2);
        }
        return new ItemStack(getDefaultItem());
    }

    private ItemStack gummyBallVariant(int customModelData) {
        ItemStack stack = new ItemStack(CCItems.GUMMY_BALL.get());
        stack.getOrCreateTag().putInt("CustomModelData", customModelData);
        return stack;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide && (getPower() == 1 || getPower() >= 3)) {
            spawnBreakParticle();
        } else if (level().isClientSide && getPower() == 2) {
            Vec3 motion = getDeltaMovement();
            for (int i = 0; i < 2; i++) {
                double step = i * 0.45D + random.nextDouble() * 0.2D;
                level().addParticle(ParticleTypes.FLAME,
                    getX() - motion.x * step + (random.nextDouble() - 0.5D) * 0.12D,
                    getY() - motion.y * step + (random.nextDouble() - 0.5D) * 0.12D,
                    getZ() - motion.z * step + (random.nextDouble() - 0.5D) * 0.12D,
                    0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected float getGravity() {
        if (isBossBeetleProjectile() && getAirState() != 1) {
            return 0.0F;
        }
        return getAirState() == 1 ? 0.05F : 0.03F;
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 direction = new Vec3(x, y, z).normalize();
        if (getPower() > 0 && getPower() != 3) {
            direction = direction.add(
                random.nextGaussian() * 0.007499999832361937D * inaccuracy,
                random.nextGaussian() * 0.007499999832361937D * inaccuracy,
                random.nextGaussian() * 0.007499999832361937D * inaccuracy
            ).normalize();
        }
        setDeltaMovement(direction.scale(velocity));
        hasImpulse = true;
        double horizontal = direction.horizontalDistance();
        setYRot((float)(Mth.atan2(direction.x, direction.z) * 180.0D / Math.PI));
        setXRot((float)(Mth.atan2(direction.y, horizontal) * 180.0D / Math.PI));
        yRotO = getYRot();
        xRotO = getXRot();
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
        boolean bossBeetleProjectile = isBossBeetleProjectile();
        float damage = getBonusDamage() > 0.0F ? getBonusDamage() : getSourceDamage(power, bossBeetleProjectile);
        if (entity instanceof BasicCandySlimeEntity candy && (candy.isPezJelly() || candy.isKingSlime() || candy.isJellyQueen())) {
            entity.hurt(damageSources().thrown(this, getOwner()), damage);
            return false;
        }
        entity.hurt(damageSources().thrown(this, getOwner()), damage);
        if (power == 5) {
            return true;
        }
        if (power == 3 && bossBeetleProjectile && !(entity instanceof BasicCandySpiderEntity beetle && beetle.getType() == CCEntityTypes.BOSS_BEETLE.get())) {
            Vec3 motion = getDeltaMovement();
            double horizontal = motion.horizontalDistance();
            if (horizontal > 1.0E-4D) {
                entity.push(motion.x * 0.6000000238418579D / horizontal, 0.1D, motion.z * 0.6000000238418579D / horizontal);
            }
        } else if (entity instanceof LivingEntity living && (power == 3 || power == 4)) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5 * 20, 0));
        } else if (entity instanceof LivingEntity living && power < 2) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5 * 20, 2));
        } else if (power == 2) {
            entity.setSecondsOnFire(7);
        }
        return true;
    }

    private float getSourceDamage(int power, boolean bossBeetleProjectile) {
        if (power == 1) {
            return 6.0F;
        }
        if (power == 2) {
            return 4.0F;
        }
        if (power == 3) {
            return bossBeetleProjectile ? 3.0F : 3.0F;
        }
        if (power == 4) {
            return 8.0F;
        }
        return 0.1F;
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
        tag.putBoolean("BossBeetleProjectile", entityData.get(BOSS_BEETLE_PROJECTILE));
        tag.putInt("AirState", getAirState());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setPower(tag.getInt("Power"));
        setVisualVariant(tag.getInt("VisualVariant"));
        setBonusDamage(tag.getFloat("BonusDamage"));
        setBossBeetleProjectile(tag.getBoolean("BossBeetleProjectile"));
        setAirState(tag.getInt("AirState"));
    }
}
