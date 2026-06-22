package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class NougatGolemEntity extends AbstractGolem {
    private static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(NougatGolemEntity.class, EntityDataSerializers.FLOAT);
    private static final String TAG_LENGTH = "Length";
    private static final String TAG_STACK_CREATED = "StackCreated";
    private static final float TOP_LENGTH = 0.8F;
    private static final float MIN_BODY_LENGTH = 0.65F;
    private static final float BODY_LENGTH_VARIANCE = 0.1F;
    private static final float EXPLOSION_RADIUS = 4.0F;
    private static final float EXPLOSION_DAMAGE = 16.0F;
    private int attackCooldown;
    private boolean stackCreated;

    public NougatGolemEntity(EntityType<? extends NougatGolemEntity> type, Level level) {
        super(type, level);
        setLength(MIN_BODY_LENGTH);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractGolem.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D)
            .add(Attributes.FOLLOW_RANGE, 24.0D)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(LENGTH, MIN_BODY_LENGTH);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new ExplodeNearEnemyGoal(this));
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.6D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, false, true,
            target -> target instanceof Enemy && target.isAlive()));
    }

    public float getLength() {
        return entityData.get(LENGTH);
    }

    public void setLength(float length) {
        entityData.set(LENGTH, length);
        refreshDimensions();
    }

    public boolean isTop() {
        return getPassengers().isEmpty();
    }

    public boolean isBase() {
        return getVehicle() == null;
    }

    @Override
    public boolean canAttackType(EntityType<?> type) {
        return type.getCategory().isFriendly() ? false : super.canAttackType(type);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        if (isTop() && getLength() != TOP_LENGTH) {
            setLength(TOP_LENGTH);
        }
        if (!level().isClientSide && !isBase()) {
            setTarget(null);
            getNavigation().stop();
        }
        super.aiStep();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(0.65F, isBase() ? getStackHeight() : getLength());
    }

    @Override
    public double getPassengersRidingOffset() {
        return getLength();
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        spawnAtLocation(CCItems.NOUGAT_POWDER.get(), 2 + random.nextInt(3 + looting));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
        if (!stackCreated) {
            setLength(TOP_LENGTH);
            if ((reason == MobSpawnType.NATURAL || reason == MobSpawnType.CHUNK_GENERATION)
                && level instanceof ServerLevel serverLevel) {
                createStack(serverLevel, this, 1 + random.nextInt(3));
            }
        }
        return data;
    }

    public static void createStack(ServerLevel level, NougatGolemEntity base, int bodySegments) {
        bodySegments = Math.max(1, bodySegments);
        NougatGolemEntity previous = base;
        base.stackCreated = true;
        base.setLength(randomBodyLength(base));
        for (int i = 1; i < bodySegments + 1; i++) {
            NougatGolemEntity segment = CCEntityTypes.NOUGAT_GOLEM.get().create(level);
            if (segment == null) {
                continue;
            }
            segment.stackCreated = true;
            segment.setLength(i == bodySegments ? TOP_LENGTH : randomBodyLength(segment));
            segment.moveTo(base.getX(), base.getY(), base.getZ(), base.getYRot(), 0.0F);
            level.addFreshEntity(segment);
            segment.startRiding(previous, true);
            previous = segment;
        }
    }

    private static float randomBodyLength(NougatGolemEntity entity) {
        return MIN_BODY_LENGTH + entity.random.nextFloat() * BODY_LENGTH_VARIANCE;
    }

    private void explodeAtStack() {
        if (!(level() instanceof ServerLevel serverLevel) || !isBase()) {
            return;
        }
        NougatGolemEntity segment = this;
        while (segment != null) {
            segment.explodeSegment(serverLevel);
            segment = segment.getTopPassenger();
        }
        discardStack();
    }

    @Nullable
    private NougatGolemEntity getTopPassenger() {
        List<Entity> passengers = getPassengers();
        if (!passengers.isEmpty() && passengers.get(0) instanceof NougatGolemEntity segment) {
            return segment;
        }
        return null;
    }

    private void explodeSegment(ServerLevel serverLevel) {
        Vec3 center = position().add(0.0D, getLength() * 0.5D, 0.0D);
        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 4.0F, 0.7F + random.nextFloat() * 0.2F);
        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION, center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        gameEvent(GameEvent.EXPLODE);

        AABB area = new AABB(center, center).inflate(EXPLOSION_RADIUS);
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, area, this::canExplosionDamage)) {
            double distance = Math.sqrt(target.distanceToSqr(center));
            double scale = Math.max(0.0D, 1.0D - distance / EXPLOSION_RADIUS);
            if (scale <= 0.0D) {
                continue;
            }
            target.hurt(damageSources().explosion(this, this), (float)(EXPLOSION_DAMAGE * scale));
            Vec3 knockback = target.position().subtract(center).normalize().scale(0.65D * scale);
            target.push(knockback.x, 0.25D * scale, knockback.z);
        }
    }

    private boolean canExplosionDamage(LivingEntity target) {
        return target.isAlive() && target instanceof Enemy && !(target instanceof NougatGolemEntity);
    }

    private float getStackHeight() {
        float height = 0.0F;
        NougatGolemEntity segment = this;
        while (segment != null) {
            height += segment.getLength();
            segment = segment.getTopPassenger();
        }
        return Math.max(getLength(), height);
    }

    private void discardStack() {
        NougatGolemEntity segment = this;
        while (segment != null) {
            NougatGolemEntity next = segment.getTopPassenger();
            segment.discard();
            segment = next;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat(TAG_LENGTH, getLength());
        tag.putBoolean(TAG_STACK_CREATED, stackCreated);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setLength(tag.contains(TAG_LENGTH) ? tag.getFloat(TAG_LENGTH) : MIN_BODY_LENGTH);
        stackCreated = tag.getBoolean(TAG_STACK_CREATED);
    }

    private static final class ExplodeNearEnemyGoal extends net.minecraft.world.entity.ai.goal.Goal {
        private final NougatGolemEntity golem;

        private ExplodeNearEnemyGoal(NougatGolemEntity golem) {
            this.golem = golem;
            setFlags(java.util.EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return golem.isBase() && golem.getTarget() != null && golem.getTarget().isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            LivingEntity target = golem.getTarget();
            if (target == null) {
                return;
            }
            golem.getLookControl().setLookAt(target, 30.0F, 30.0F);
            golem.getNavigation().moveTo(target, 1.0D);
            double reach = golem.getBbWidth() * 2.0F * golem.getBbWidth() * 2.0F + target.getBbWidth();
            if (golem.distanceToSqr(target) <= reach && golem.attackCooldown <= 0) {
                golem.attackCooldown = 20;
                golem.explodeAtStack();
            }
        }
    }
}
