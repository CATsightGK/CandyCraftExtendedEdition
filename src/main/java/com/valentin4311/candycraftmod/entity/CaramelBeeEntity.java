package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCMobEffects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CaramelBeeEntity extends Monster {
    private static final int HONEY_GLUE_DURATION_TICKS = 5 * 20;
    private static final int NATURAL_ANGER_DURATION_TICKS = 30 * 20;
    private static final double SUGUARD_WITNESS_RANGE = 16.0D;
    private static final EntityDataAccessor<Boolean> ANGRY = SynchedEntityData.defineId(CaramelBeeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HANGED = SynchedEntityData.defineId(CaramelBeeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final String TAG_ANGRY = "Angry";
    private static final String TAG_ALWAYS_HOSTILE = "AlwaysHostile";
    private static final String TAG_ANGER_TARGET = "AngerTarget";
    private static final String TAG_ANGER_TICKS = "AngerTicks";
    private static final String TAG_HANGED = "Hanged";
    private BlockPos flightTarget;
    private Vec3 hangedOrigin;
    private int attackTick;
    private boolean alwaysHostile;
    @Nullable
    private UUID angerTarget;
    private int angerTicks;

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
    public double getPassengersRidingOffset() {
        // A mounted suguard sits astride the bee's back instead of standing
        // on top of the full bounding box.
        return super.getPassengersRidingOffset() - 0.3D;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ANGRY, false);
        entityData.define(HANGED, false);
    }

    @Override
    protected void registerGoals() {
        targetSelector.addGoal(1, new CaramelBeeHurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new AngryPlayerTargetGoal(this));
    }

    public boolean isAngry() {
        return entityData.get(ANGRY);
    }

    public void setAngry(boolean angry) {
        entityData.set(ANGRY, angry);
    }

    public void setAlwaysHostile(boolean alwaysHostile) {
        this.alwaysHostile = alwaysHostile;
        if (alwaysHostile) {
            angerTarget = null;
            angerTicks = 0;
        }
        setAngry(alwaysHostile || angerTicks > 0 && angerTarget != null);
    }

    public void provoke(Player player) {
        if (alwaysHostile || !CandyTargeting.canAttackPlayer(player)) {
            return;
        }
        angerTarget = player.getUUID();
        angerTicks = NATURAL_ANGER_DURATION_TICKS;
        setAngry(true);
        setTarget(player);
    }

    public static void alertSuguardAttackWitnesses(ServerLevel level, Entity suguard, Player attacker) {
        if (!CandyTargeting.canAttackPlayer(attacker)) {
            return;
        }
        AABB searchBounds = suguard.getBoundingBox().inflate(SUGUARD_WITNESS_RANGE);
        for (CaramelBeeEntity bee : level.getEntitiesOfClass(CaramelBeeEntity.class, searchBounds)) {
            if (bee.alwaysHostile || bee.distanceToSqr(suguard) > SUGUARD_WITNESS_RANGE * SUGUARD_WITNESS_RANGE
                    || !bee.hasLineOfSight(suguard) || !bee.hasLineOfSight(attacker)) {
                continue;
            }
            bee.provoke(attacker);
        }
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
        if (!level().isClientSide) {
            tickAnger();
        }
        if (isHanged()) {
            tickHanged();
            super.aiStep();
            return;
        }
        if (!CandyTargeting.canAttackEntity(getTarget())
                || getTarget() instanceof Player player && !canTargetPlayer(player)) {
            setTarget(null);
        }
        setNoGravity(true);
        if (!level().isClientSide) {
            tickFlight();
        }
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
        Player player = null;
        double followRange = getAttributeValue(Attributes.FOLLOW_RANGE);
        if (isAngry()) {
            if (getTarget() instanceof Player currentTarget && canTargetPlayer(currentTarget)
                    && distanceToSqr(currentTarget) <= followRange * followRange) {
                player = currentTarget;
            } else if (alwaysHostile) {
                player = CandyTargeting.nearestAttackablePlayer(level(), this, followRange);
            } else if (level() instanceof ServerLevel serverLevel && angerTarget != null) {
                Player provoker = serverLevel.getPlayerByUUID(angerTarget);
                if (canTargetPlayer(provoker) && distanceToSqr(provoker) <= followRange * followRange) {
                    player = provoker;
                }
            }
        }
        attackTick = Math.max(attackTick - 1, 0);
        if (player != null) {
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
        Vec3 toTarget = new Vec3(dx, dy, dz);
        if (toTarget.lengthSqr() > 1.0E-4D) {
            Vec3 direction = toTarget.normalize();
            double speed = isAngry() && player != null ? 0.30D : 0.20D;
            Vec3 desired = new Vec3(
                direction.x * speed,
                Mth.clamp(direction.y * speed, -0.16D, 0.20D),
                direction.z * speed
            );
            double blend = isAngry() && player != null ? 0.12D : 0.08D;
            setDeltaMovement(movement.lerp(desired, blend));
        } else {
            setDeltaMovement(movement.scale(0.92D));
        }

        float targetYaw = (float)(Math.atan2(getDeltaMovement().z, getDeltaMovement().x) * 180.0D / Math.PI) - 90.0F;
        setYRot(getYRot() + Mth.wrapDegrees(targetYaw - getYRot()) * 0.22F);
        yBodyRot = getYRot();
    }

    private boolean canAttackPlayer(Player player) {
        return isAngry() && canTargetPlayer(player);
    }

    private boolean canTargetPlayer(@Nullable Player player) {
        return CandyTargeting.canAttackPlayer(player)
            && (alwaysHostile || angerTicks > 0 && angerTarget != null && angerTarget.equals(player.getUUID()));
    }

    private void tickAnger() {
        if (alwaysHostile) {
            if (!isAngry()) {
                setAngry(true);
            }
            return;
        }
        if (angerTicks > 0) {
            --angerTicks;
        }
        if (angerTicks > 0 && angerTarget != null) {
            if (!isAngry()) {
                setAngry(true);
            }
            return;
        }
        angerTicks = 0;
        angerTarget = null;
        setAngry(false);
        if (getTarget() instanceof Player) {
            setTarget(null);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!CandyTargeting.canAttackEntity(target)) {
            setTarget(null);
            return false;
        }
        float damage = level().getDifficulty() == Difficulty.HARD ? 3.0F : 2.0F;
        boolean success = target.hurt(damageSources().mobAttack(this), damage);
        if (success && target instanceof Player player && random.nextBoolean()) {
            player.addEffect(new MobEffectInstance(CCMobEffects.HONEY_GLUE.get(), HONEY_GLUE_DURATION_TICKS), this);
        }
        return success;
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
        int count = random.nextInt(3) + random.nextInt(looting + 1);
        for (int i = 0; i < count; i++) {
            spawnAtLocation(CCItems.HONEY_SHARD.get());
        }
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
        setAlwaysHostile(reason == MobSpawnType.SPAWNER);
        return data;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(TAG_ANGRY, isAngry());
        tag.putBoolean(TAG_ALWAYS_HOSTILE, alwaysHostile);
        tag.putInt(TAG_ANGER_TICKS, angerTicks);
        if (angerTarget != null) {
            tag.putUUID(TAG_ANGER_TARGET, angerTarget);
        }
        tag.putBoolean(TAG_HANGED, isHanged());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        alwaysHostile = tag.getBoolean(TAG_ALWAYS_HOSTILE);
        angerTicks = tag.getInt(TAG_ANGER_TICKS);
        angerTarget = tag.hasUUID(TAG_ANGER_TARGET) ? tag.getUUID(TAG_ANGER_TARGET) : null;
        setAngry(alwaysHostile || angerTicks > 0 && angerTarget != null);
        if (tag.contains(TAG_HANGED)) {
            setHanged(tag.getBoolean(TAG_HANGED));
        }
    }

    private static final class AngryPlayerTargetGoal extends NearestAttackableTargetGoal<Player> {
        private final CaramelBeeEntity bee;

        private AngryPlayerTargetGoal(CaramelBeeEntity bee) {
            super(bee, Player.class, 10, true, false,
                entity -> entity instanceof Player player && bee.canTargetPlayer(player));
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

    private static final class CaramelBeeHurtByTargetGoal extends HurtByTargetGoal {
        private final CaramelBeeEntity bee;

        private CaramelBeeHurtByTargetGoal(CaramelBeeEntity bee) {
            super(bee);
            this.bee = bee;
        }

        @Override
        public void start() {
            LivingEntity attacker = bee.getLastHurtByMob();
            if (attacker instanceof Player player) {
                bee.provoke(player);
            }
            super.start();
        }

        @Override
        protected void alertOther(Mob mob, LivingEntity target) {
            if (mob instanceof CaramelBeeEntity otherBee && target instanceof Player player) {
                otherBee.provoke(player);
                return;
            }
            super.alertOther(mob, target);
        }
    }
}
