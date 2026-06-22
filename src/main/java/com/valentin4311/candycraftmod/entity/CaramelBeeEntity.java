package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCItems;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CaramelBeeEntity extends Monster {
    private static final EntityDataAccessor<Boolean> ANGRY = SynchedEntityData.defineId(CaramelBeeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HANGED = SynchedEntityData.defineId(CaramelBeeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final String TAG_ANGRY = "Angry";
    private static final String TAG_HANGED = "Hanged";
    private BlockPos flightTarget;
    private Vec3 hangedOrigin;
    private int attackTick;

    public CaramelBeeEntity(EntityType<? extends CaramelBeeEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 15.0D)
            .add(Attributes.MOVEMENT_SPEED, 2.0D)
            .add(Attributes.ATTACK_DAMAGE, 2.0D)
            .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ANGRY, false);
        entityData.define(HANGED, false);
    }

    @Override
    protected void registerGoals() {
        targetSelector.addGoal(1, new AngryPlayerTargetGoal(this));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    public boolean isAngry() {
        return entityData.get(ANGRY);
    }

    public void setAngry(boolean angry) {
        entityData.set(ANGRY, angry);
    }

    public boolean isHanged() {
        return entityData.get(HANGED);
    }

    public void setHanged(boolean hanged) {
        entityData.set(HANGED, hanged);
        if (hanged) {
            hangedOrigin = position();
            setTarget(null);
        }
    }

    @Override
    public void aiStep() {
        if (isHanged()) {
            tickHanged();
            super.aiStep();
            return;
        }
        setNoGravity(true);
        tickFlight();
        super.aiStep();
    }

    private void tickHanged() {
        setNoGravity(false);
        getNavigation().stop();
        setTarget(null);
        setDeltaMovement(getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
        if (!level().isClientSide && hangedOrigin != null && position().distanceToSqr(hangedOrigin) > 0.04D) {
            setHanged(false);
        }
    }

    private void tickFlight() {
        Player player = level().getNearestPlayer(this, 8.0D);
        attackTick = Math.max(attackTick - 1, 0);
        if (isAngry() && player != null) {
            setTarget(player);
        } else if (!isAngry()) {
            setTarget(null);
        }

        if (player != null && canAttackPlayer(player)) {
            double reach = getBbWidth() * 2.0F * getBbWidth() * 2.0F + player.getBbWidth();
            if (distanceToSqr(player.getX(), player.getBoundingBox().minY, player.getZ()) <= reach && attackTick <= 0) {
                attackTick = 20;
                doHurtTarget(player);
            }
        }

        if (flightTarget == null || !level().isEmptyBlock(flightTarget) || flightTarget.getY() < level().getMinBuildHeight()
            || random.nextInt(100) == 0 || flightTarget.closerToCenterThan(position(), 2.0D)) {
            flightTarget = blockPosition().offset(random.nextInt(14) - random.nextInt(14), random.nextInt(6) - 2, random.nextInt(14) - random.nextInt(14));
        }

        double dx = flightTarget.getX() + 0.5D - getX();
        double dy = flightTarget.getY() + 0.1D - getY();
        double dz = flightTarget.getZ() + 0.5D - getZ();
        if (isAngry() && player != null) {
            dx = player.getX() - getX();
            dy = player.getY() + 1.1D - getY();
            dz = player.getZ() - getZ();
            flightTarget = player.blockPosition();
        }

        Vec3 movement = getDeltaMovement();
        setDeltaMovement(
            movement.x + (Math.signum(dx) * 0.5D - movement.x) * 0.10000000149011612D,
            (movement.y + (Math.signum(dy) * 0.699999988079071D - movement.y) * 0.10000000149011612D) * 0.6000000238418579D,
            movement.z + (Math.signum(dz) * 0.5D - movement.z) * 0.10000000149011612D
        );

        float targetYaw = (float)(Math.atan2(getDeltaMovement().z, getDeltaMovement().x) * 180.0D / Math.PI) - 90.0F;
        setYRot(getYRot() + Mth.wrapDegrees(targetYaw - getYRot()));
        yBodyRot = getYRot();
    }

    private boolean canAttackPlayer(Player player) {
        return isAngry() && !player.isCreative() && !player.isSpectator();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        float damage = level().getDifficulty() == Difficulty.HARD ? 3.0F : 2.0F;
        boolean success = target.hurt(damageSources().mobAttack(this), damage);
        if (success && target instanceof net.minecraft.world.entity.LivingEntity living && random.nextInt(15) == 0) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 400, 0), this);
        }
        return success;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!isInvulnerableTo(source)) {
            setAngry(true);
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (isHanged()) {
            super.travel(travelVector);
            return;
        }
        move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        spawnAtLocation(CCItems.HONEY_SHARD.get());
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
        if (reason != MobSpawnType.COMMAND) {
            setAngry(true);
        }
        return data;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(TAG_ANGRY, isAngry());
        tag.putBoolean(TAG_HANGED, isHanged());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setAngry(tag.getBoolean(TAG_ANGRY));
        if (tag.contains(TAG_HANGED)) {
            setHanged(tag.getBoolean(TAG_HANGED));
        }
    }

    private static final class AngryPlayerTargetGoal extends NearestAttackableTargetGoal<Player> {
        private final CaramelBeeEntity bee;

        private AngryPlayerTargetGoal(CaramelBeeEntity bee) {
            super(bee, Player.class, true);
            this.bee = bee;
        }

        @Override
        public boolean canUse() {
            return bee.isAngry() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return bee.isAngry() && super.canContinueToUse();
        }
    }
}
