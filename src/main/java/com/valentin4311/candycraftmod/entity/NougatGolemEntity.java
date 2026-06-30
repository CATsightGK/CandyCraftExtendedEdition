package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

public class NougatGolemEntity extends AbstractGolem {
    private static final EntityDataAccessor<Float> LENGTH = SynchedEntityData.defineId(NougatGolemEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(NougatGolemEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> NATURAL_SEGMENT = SynchedEntityData.defineId(NougatGolemEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ATTACK_SWING_TICKS = SynchedEntityData.defineId(NougatGolemEntity.class, EntityDataSerializers.INT);
    private static final String TAG_LENGTH = "Length";
    private static final String TAG_STACK_CREATED = "StackCreated";
    private static final String TAG_LAUNCHED = "Launched";
    private static final String TAG_LAUNCH_DELAY = "LaunchDelay";
    private static final String TAG_LAUNCH_FUSE = "LaunchFuse";
    private static final String TAG_MODE = "Mode";
    private static final String TAG_NATURAL_SEGMENT = "NaturalSegment";
    private static final String TAG_GROWTH_TICKS = "GrowthTicks";
    private static final String TAG_POWDER_PROGRESS = "PowderProgress";
    private static final int MODE_TURRET = 0;
    private static final int MODE_MOBILE_EXPLODE = 1;
    private static final int MODE_MELEE = 2;
    private static final float SEGMENT_LENGTH = 1.0F;
    private static final float TOP_LENGTH = SEGMENT_LENGTH;
    private static final float MIN_BODY_LENGTH = SEGMENT_LENGTH;
    private static final float BODY_LENGTH_VARIANCE = 0.0F;
    private static final float EXPLOSION_RADIUS = 4.0F;
    private static final float EXPLOSION_DAMAGE = 16.0F;
    private static final float MELEE_BASE_DAMAGE = 4.0F;
    private static final int MAX_STACK_SEGMENTS = 10;
    private static final int GROW_HEAD_TICKS = 30 * 20;
    private static final int LAUNCH_STEP_DELAY = 7;
    private static final int LAUNCH_FUSE_TICKS = 50;
    private static final int LAUNCH_COLLISION_GRACE_TICKS = 5;
    private static final double LAUNCHED_SEGMENT_SPEED = 0.86D;
    private static final double LAUNCHED_SEGMENT_STEER = 0.16D;
    private int attackCooldown;
    private boolean stackCreated;
    private boolean launched;
    private int launchDelay;
    private int launchFuse;
    private int growthTicks;
    private int powderProgress;
    private int targetSearchCooldown;
    @Nullable
    private LivingEntity launchTarget;

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
        entityData.define(MODE, MODE_TURRET);
        entityData.define(NATURAL_SEGMENT, false);
        entityData.define(ATTACK_SWING_TICKS, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
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

    public int getMode() {
        return entityData.get(MODE);
    }

    private void setMode(int mode) {
        entityData.set(MODE, Math.floorMod(mode, 3));
    }

    public float getAttackSwingProgress(float partialTicks) {
        return Math.max(0.0F, entityData.get(ATTACK_SWING_TICKS) - partialTicks) / 10.0F;
    }

    public int getVisualStackIndex() {
        int index = 0;
        Entity vehicle = getVehicle();
        while (vehicle instanceof NougatGolemEntity segment) {
            index++;
            vehicle = segment.getVehicle();
        }
        return index;
    }

    public boolean isStackMoving() {
        NougatGolemEntity base = getBase();
        return base.getMode() != MODE_TURRET && base.getDeltaMovement().horizontalDistanceSqr() > 0.001D;
    }

    @Override
    public boolean canAttackType(EntityType<?> type) {
        return type.getCategory().isFriendly() ? false : super.canAttackType(type);
    }

    @Override
    public boolean canBeCollidedWith() {
        return !launched;
    }

    @Override
    public boolean isPickable() {
        return !launched;
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
        boolean hurt = super.hurt(source, amount);
        if (!level().isClientSide && hurt && source.getEntity() instanceof LivingEntity attacker && canRetaliateAgainst(attacker)) {
            getBase().shareTarget(attacker);
        }
        return hurt;
    }

    @Override
    public void aiStep() {
        if (tickLaunchedSegment()) {
            return;
        }
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        int swing = entityData.get(ATTACK_SWING_TICKS);
        if (swing > 0) {
            entityData.set(ATTACK_SWING_TICKS, swing - 1);
        }
        if (!launched && isTop() && getLength() != TOP_LENGTH) {
            setLength(TOP_LENGTH);
        }
        if (!level().isClientSide && !isBase()) {
            NougatGolemEntity base = getBase();
            setMode(base.getMode());
            LivingEntity baseTarget = base.getTarget();
            if (baseTarget != null && baseTarget.isAlive()) {
                super.setTarget(baseTarget);
            } else {
                super.setTarget(null);
            }
            getNavigation().stop();
        } else if (!level().isClientSide) {
            tickTargeting();
            tickGrowth();
            tickStackCombat();
        }
        spawnMovementTrail();
        super.aiStep();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(0.98F, getLength());
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
        if (entityData.get(NATURAL_SEGMENT)) {
            return;
        }
        spawnAtLocation(CCItems.NOUGAT_POWDER.get(), 2 + random.nextInt(3 + looting));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        NougatGolemEntity base = getBase();
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(CCItems.NOUGAT_POWDER.get())) {
            if (!level().isClientSide) {
                base.feedPowder(player, stack);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }

        if (!level().isClientSide) {
            base.setMode(base.getMode() + 1);
            base.syncModeToStack();
            base.getNavigation().stop();
            player.displayClientMessage(Component.translatable(base.modeTranslationKey()), true);
            level().playSound(null, base.getX(), base.getY(), base.getZ(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.NEUTRAL, 0.65F, 0.85F + base.getMode() * 0.18F);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
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
                setNaturalSegment(true);
                createStack(serverLevel, this, 1 + random.nextInt(3), true);
            }
        }
        return data;
    }

    public static void createStack(ServerLevel level, NougatGolemEntity base, int bodySegments) {
        createStack(level, base, bodySegments, false);
    }

    public static void createStack(ServerLevel level, NougatGolemEntity base, int bodySegments, boolean natural) {
        bodySegments = Math.min(MAX_STACK_SEGMENTS - 1, Math.max(1, bodySegments));
        NougatGolemEntity previous = base;
        base.stackCreated = true;
        base.setNaturalSegment(natural);
        base.setLength(randomBodyLength(base));
        for (int i = 1; i < bodySegments + 1; i++) {
            NougatGolemEntity segment = CCEntityTypes.NOUGAT_GOLEM.get().create(level);
            if (segment == null) {
                continue;
            }
            segment.stackCreated = true;
            segment.setMode(base.getMode());
            segment.setNaturalSegment(natural);
            segment.setLength(i == bodySegments ? TOP_LENGTH : randomBodyLength(segment));
            segment.moveTo(base.getX(), base.getY(), base.getZ(), base.getYRot(), 0.0F);
            level.addFreshEntity(segment);
            segment.startRiding(previous, true);
            previous = segment;
        }
    }

    private static float randomBodyLength(NougatGolemEntity entity) {
        return SEGMENT_LENGTH;
    }

    private boolean launchTopAt(@Nullable LivingEntity target) {
        if (!(level() instanceof ServerLevel serverLevel) || !isBase()) {
            return false;
        }
        List<NougatGolemEntity> segments = stackSegments();
        if (segments.isEmpty()) {
            return false;
        }
        Vec3 aim = target != null ? target.getEyePosition() : position().add(getLookAngle().scale(5.0D));
        NougatGolemEntity segment = selectLaunchSegment(segments, getMode() == MODE_TURRET);
        if (segment == null) {
            return false;
        }
        boolean launchingBase = segment == this;
        double launchY = getY() + getStackHeight() - segment.getLength();
        if (!detachLaunchSegment(segments, segment)) {
            return false;
        }
        segment.setPos(getX(), launchY, getZ());
        segment.superSetTarget(null);
        segment.getNavigation().stop();
        Vec3 from = segment.position().add(0.0D, segment.getLength() * 0.5D, 0.0D);
        Vec3 direction = aim.subtract(from);
        if (direction.lengthSqr() < 0.001D) {
            direction = getLookAngle();
        }
        direction = direction.normalize();
        segment.launched = true;
        segment.launchDelay = 0;
        segment.launchFuse = LAUNCH_FUSE_TICKS;
        segment.launchTarget = target;
        segment.setDeltaMovement(direction.scale(LAUNCHED_SEGMENT_SPEED).add(0.0D, 0.08D, 0.0D));
        segment.hasImpulse = true;
        if (!launchingBase && stackSegments().size() == 1) {
            setLength(TOP_LENGTH);
        }
        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.SNOW_GOLEM_SHOOT, SoundSource.HOSTILE, 1.0F, 0.65F + random.nextFloat() * 0.2F);
        return true;
    }

    private boolean explodeTopNearTarget() {
        if (!(level() instanceof ServerLevel serverLevel) || !isBase()) {
            return false;
        }
        List<NougatGolemEntity> segments = stackSegments();
        if (segments.isEmpty()) {
            return false;
        }
        NougatGolemEntity segment = selectLaunchSegment(segments, getMode() == MODE_TURRET);
        if (segment == null) {
            return false;
        }
        boolean explodingBase = segment == this;
        double explodeY = getY() + Math.max(0.0F, getStackHeight() - segment.getLength());
        if (!detachLaunchSegment(segments, segment)) {
            return false;
        }
        segment.setPos(getX(), explodeY, getZ());
        segment.explodeSegment(serverLevel);
        segment.discard();
        if (!explodingBase && stackSegments().size() == 1) {
            setLength(TOP_LENGTH);
        }
        return true;
    }

    @Nullable
    private NougatGolemEntity selectLaunchSegment(List<NougatGolemEntity> segments, boolean preserveTopHead) {
        if (segments.isEmpty()) {
            return null;
        }
        if (!preserveTopHead) {
            return segments.get(segments.size() - 1);
        }
        if (segments.size() <= 2) {
            return null;
        }
        return segments.get(segments.size() - 2);
    }

    private boolean detachLaunchSegment(List<NougatGolemEntity> segments, NougatGolemEntity segment) {
        int index = segments.indexOf(segment);
        if (index < 0) {
            return false;
        }
        if (index == 0) {
            return true;
        }
        NougatGolemEntity parent = segments.get(index - 1);
        NougatGolemEntity child = index + 1 < segments.size() ? segments.get(index + 1) : null;
        if (child != null) {
            child.stopRiding();
        }
        segment.stopRiding();
        segment.ejectPassengers();
        if (child != null && !child.isRemoved()) {
            child.startRiding(parent, true);
        }
        return true;
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
        serverLevel.sendParticles(
            new BlockParticleOption(ParticleTypes.BLOCK, CCBlocks.NOUGAT_BLOCK.get().defaultBlockState()),
            center.x,
            center.y,
            center.z,
            80,
            0.55D,
            0.45D,
            0.55D,
            0.18D
        );
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

    private boolean tickLaunchedSegment() {
        if (!launched) {
            return false;
        }
        if (launchDelay > 0) {
            launchDelay--;
            setDeltaMovement(Vec3.ZERO);
            return true;
        }
        if (!level().isClientSide) {
            LivingEntity hitTarget = findLaunchedCollisionTarget();
            if (hitTarget != null) {
                if (level() instanceof ServerLevel serverLevel) {
                    explodeSegment(serverLevel);
                }
                discard();
                return true;
            }
            guideLaunchedSegment();
            boolean collisionReady = launchFuse < LAUNCH_FUSE_TICKS - LAUNCH_COLLISION_GRACE_TICKS;
            if (launchFuse-- <= 0 || (collisionReady && (horizontalCollision || verticalCollision))) {
                if (level() instanceof ServerLevel serverLevel) {
                    explodeSegment(serverLevel);
                }
                discard();
                return true;
            }
        }
        if (level().isClientSide && random.nextInt(3) == 0) {
            level().addParticle(nougatPowderParticle(), getX(), getY() + getLength() * 0.5D, getZ(), 0.0D, 0.0D, 0.0D);
        }
        super.aiStep();
        return true;
    }

    private void guideLaunchedSegment() {
        LivingEntity target = launchTarget;
        if (target == null || !target.isAlive() || (!canExplosionDamage(target) && !canRetaliateAgainst(target))) {
            launchTarget = null;
            return;
        }
        Vec3 from = position().add(0.0D, getLength() * 0.5D, 0.0D);
        Vec3 desired = target.getEyePosition().subtract(from);
        if (desired.lengthSqr() < 0.001D) {
            return;
        }
        Vec3 velocity = getDeltaMovement();
        Vec3 steered = velocity.scale(1.0D - LAUNCHED_SEGMENT_STEER)
            .add(desired.normalize().scale(LAUNCHED_SEGMENT_SPEED * LAUNCHED_SEGMENT_STEER));
        if (steered.lengthSqr() > 0.001D) {
            setDeltaMovement(steered.normalize().scale(Math.max(LAUNCHED_SEGMENT_SPEED * 0.75D, steered.length())));
            hasImpulse = true;
        }
    }

    @Nullable
    private LivingEntity findLaunchedCollisionTarget() {
        AABB area = getBoundingBox().inflate(0.35D);
        LivingEntity target = launchTarget;
        if (target != null && target.isAlive() && area.intersects(target.getBoundingBox()) && (canExplosionDamage(target) || canRetaliateAgainst(target))) {
            return target;
        }
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, area, living -> living != this && canExplosionDamage(living));
        return targets.isEmpty() ? null : targets.get(0);
    }

    private boolean canExplosionDamage(LivingEntity target) {
        return canTargetHostile(target);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (!level().isClientSide && !isBase()) {
            getBase().shareTarget(target);
            super.setTarget(target);
            return;
        }
        shareTarget(target);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof LivingEntity living) || !canExplosionDamage(living)) {
            return false;
        }
        float damage = MELEE_BASE_DAMAGE + Math.max(0, getStackSegmentCount() - 1);
        boolean hurt = target.hurt(damageSources().mobAttack(this), damage);
        if (hurt) {
            setStackAttackSwing(10);
            Vec3 knockback = target.position().subtract(position()).normalize().scale(0.45D);
            target.push(knockback.x, 0.18D, knockback.z);
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 0.9F, 0.65F);
        }
        return hurt;
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

    private List<NougatGolemEntity> stackSegments() {
        List<NougatGolemEntity> segments = new java.util.ArrayList<>();
        NougatGolemEntity segment = this;
        while (segment != null) {
            segments.add(segment);
            segment = segment.getTopPassenger();
        }
        return segments;
    }

    private int getStackSegmentCount() {
        int count = 0;
        NougatGolemEntity segment = this;
        while (segment != null) {
            count++;
            segment = segment.getTopPassenger();
        }
        return count;
    }

    private double distanceToStackSqr(LivingEntity target) {
        double best = Double.MAX_VALUE;
        NougatGolemEntity segment = this;
        while (segment != null) {
            best = Math.min(best, segment.distanceToSqr(target));
            segment = segment.getTopPassenger();
        }
        return best;
    }

    private void shareTarget(@Nullable LivingEntity target) {
        if (target != null && !canTargetHostile(target) && !canRetaliateAgainst(target)) {
            target = null;
        }
        super.setTarget(target);
        if (!isBase()) {
            return;
        }
        NougatGolemEntity segment = getTopPassenger();
        while (segment != null) {
            if (segment != this) {
                segment.superSetTarget(target);
            }
            segment = segment.getTopPassenger();
        }
    }

    private void superSetTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
    }

    private NougatGolemEntity getBase() {
        NougatGolemEntity base = this;
        while (base.getVehicle() instanceof NougatGolemEntity parent) {
            base = parent;
        }
        return base;
    }

    private void syncModeToStack() {
        NougatGolemEntity segment = this;
        while (segment != null) {
            segment.setMode(getMode());
            segment = segment.getTopPassenger();
        }
    }

    private void setStackAttackSwing(int ticks) {
        NougatGolemEntity segment = this;
        while (segment != null) {
            segment.entityData.set(ATTACK_SWING_TICKS, ticks);
            segment = segment.getTopPassenger();
        }
    }

    private String modeTranslationKey() {
        return switch (getMode()) {
            case MODE_MOBILE_EXPLODE -> "message.candycraftmod.nougat_golem.mode.mobile_explode";
            case MODE_MELEE -> "message.candycraftmod.nougat_golem.mode.melee";
            default -> "message.candycraftmod.nougat_golem.mode.turret";
        };
    }

    private void tickGrowth() {
        if (!isBase() || launched || getMode() != MODE_TURRET || getStackHeight() <= 0.0F) {
            return;
        }
        if (getStackSegmentCount() >= MAX_STACK_SEGMENTS) {
            growthTicks = 0;
            powderProgress = 0;
            return;
        }
        growthTicks++;
        if (growthTicks >= GROW_HEAD_TICKS) {
            growthTicks = 0;
            addNaturalHead();
        }
    }

    private void feedPowder(Player player, ItemStack stack) {
        if (getMode() != MODE_TURRET) {
            player.displayClientMessage(Component.translatable(modeTranslationKey()), true);
            return;
        }
        if (getStackSegmentCount() >= MAX_STACK_SEGMENTS) {
            player.displayClientMessage(Component.translatable("message.candycraftmod.nougat_golem.feed", 2, 2), true);
            return;
        }
        powderProgress++;
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.7F, 1.1F + random.nextFloat() * 0.15F);
        player.displayClientMessage(Component.translatable("message.candycraftmod.nougat_golem.feed", Math.min(2, powderProgress), 2), true);
        if (powderProgress >= 2) {
            powderProgress = 0;
            growthTicks = 0;
            addNaturalHead();
        }
    }

    private void addNaturalHead() {
        if (!(level() instanceof ServerLevel serverLevel) || !isBase()) {
            return;
        }
        if (getStackSegmentCount() >= MAX_STACK_SEGMENTS) {
            return;
        }
        NougatGolemEntity top = this;
        while (top.getTopPassenger() != null) {
            top = top.getTopPassenger();
        }
        if (top == this && getLength() == TOP_LENGTH) {
            setLength(randomBodyLength(this));
        }
        NougatGolemEntity segment = CCEntityTypes.NOUGAT_GOLEM.get().create(serverLevel);
        if (segment == null) {
            return;
        }
        segment.stackCreated = true;
        segment.setMode(getMode());
        segment.setNaturalSegment(true);
        segment.setLength(TOP_LENGTH);
        segment.moveTo(getX(), getY(), getZ(), getYRot(), 0.0F);
        serverLevel.addFreshEntity(segment);
        segment.startRiding(top, true);
        refreshDimensions();
        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.SLIME_BLOCK_PLACE, SoundSource.NEUTRAL, 0.8F, 0.8F + random.nextFloat() * 0.2F);
    }

    private void tickTargeting() {
        LivingEntity current = findStackTarget();
        double followRange = getAttributeValue(Attributes.FOLLOW_RANGE);
        if (current != null && canTargetHostile(current) && distanceToStackSqr(current) <= followRange * followRange) {
            shareTarget(current);
            targetSearchCooldown = 0;
            return;
        }
        if (targetSearchCooldown > 0) {
            targetSearchCooldown--;
            return;
        }
        targetSearchCooldown = 10;
        shareTarget(findNearestHostileForStack(followRange));
    }

    @Nullable
    private LivingEntity findStackTarget() {
        NougatGolemEntity segment = this;
        while (segment != null) {
            LivingEntity target = segment.getTarget();
            if (target != null && target.isAlive() && (canTargetHostile(target) || canRetaliateAgainst(target))) {
                return target;
            }
            segment = segment.getTopPassenger();
        }
        return null;
    }

    @Nullable
    private LivingEntity findNearestHostileForStack(double range) {
        AABB area = stackBoundingBox().inflate(range, Math.max(4.0D, range * 0.5D), range);
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : level().getEntitiesOfClass(LivingEntity.class, area, this::canTargetHostile)) {
            double distance = distanceToStackSqr(candidate);
            if (distance < bestDistance && stackHasLineOfSight(candidate)) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }

    private AABB stackBoundingBox() {
        AABB box = getBoundingBox();
        NougatGolemEntity segment = getTopPassenger();
        while (segment != null) {
            box = box.minmax(segment.getBoundingBox());
            segment = segment.getTopPassenger();
        }
        return box;
    }

    private boolean stackHasLineOfSight(LivingEntity target) {
        NougatGolemEntity segment = this;
        while (segment != null) {
            if (segment.hasLineOfSight(target)) {
                return true;
            }
            segment = segment.getTopPassenger();
        }
        return false;
    }

    private void tickStackCombat() {
        if (!isBase() || launched) {
            return;
        }
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        if (!canTargetHostile(target) && !canRetaliateAgainst(target)) {
            shareTarget(null);
            return;
        }
        shareTarget(target);
        getLookControl().setLookAt(target, 30.0F, 30.0F);

        int mode = getMode();
        double distance = distanceToStackSqr(target);
        if (mode == MODE_TURRET) {
            getNavigation().stop();
        } else {
            double speed = mode == MODE_MELEE ? 1.18D : 1.0D;
            getNavigation().moveTo(target, speed);
            getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), speed);
            pushStackToward(target, mode == MODE_MELEE ? 0.06D : 0.045D);
        }

        double reach = Mth.square(getBbWidth() * 1.25F + target.getBbWidth() + 0.35F);
        if (distance <= reach && attackCooldown <= 0) {
            if (mode == MODE_MOBILE_EXPLODE) {
                if (launchTopAt(target)) {
                    attackCooldown = 18;
                }
            } else if (mode == MODE_MELEE) {
                attackCooldown = 16;
                doHurtTarget(target);
            }
        } else if (mode == MODE_MOBILE_EXPLODE && attackCooldown <= 0 && distance <= 16.0D * 16.0D) {
            if (launchTopAt(target)) {
                attackCooldown = 20;
            }
        } else if (mode == MODE_MOBILE_EXPLODE && attackCooldown <= 0 && distance <= 24.0D * 24.0D && !getNavigation().isDone()) {
            if (launchTopAt(target)) {
                attackCooldown = 28;
            }
        } else if (mode == MODE_TURRET && attackCooldown <= 0 && distance <= 24.0D * 24.0D) {
            if (launchTopAt(target)) {
                attackCooldown = 38;
            }
        }
    }

    private void pushStackToward(LivingEntity target, double strength) {
        Vec3 toTarget = target.position().subtract(position());
        if (toTarget.horizontalDistanceSqr() <= 0.001D) {
            return;
        }
        Vec3 push = toTarget.normalize().scale(strength);
        setDeltaMovement(getDeltaMovement().add(push.x, 0.0D, push.z));
        hasImpulse = true;
    }

    private boolean canTargetHostile(LivingEntity target) {
        if (!target.isAlive() || target instanceof NougatGolemEntity) {
            return false;
        }
        if (target instanceof Player) {
            return false;
        }
        if (isAlliedTo(target) || target.isAlliedTo(this)) {
            return false;
        }
        return target instanceof Enemy || target.getType().getCategory() == MobCategory.MONSTER;
    }

    private boolean canRetaliateAgainst(LivingEntity target) {
        if (!target.isAlive() || target instanceof NougatGolemEntity || target instanceof Player) {
            return false;
        }
        if (isAlliedTo(target) || target.isAlliedTo(this) || target.getType().getCategory().isFriendly()) {
            return false;
        }
        return true;
    }

    private void spawnMovementTrail() {
        if (getMode() == MODE_TURRET || !isBase() || launched || getDeltaMovement().horizontalDistanceSqr() < 0.0025D || random.nextInt(2) != 0) {
            return;
        }
        level().addParticle(nougatPowderParticle(),
            getX() - getDeltaMovement().x * 2.0D + (random.nextDouble() - 0.5D) * 0.35D,
            getY() + 0.12D + random.nextDouble() * Math.min(1.2D, getBbHeight()),
            getZ() - getDeltaMovement().z * 2.0D + (random.nextDouble() - 0.5D) * 0.35D,
            -getDeltaMovement().x * 0.1D, 0.02D, -getDeltaMovement().z * 0.1D);
    }

    private static ItemParticleOption nougatPowderParticle() {
        return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(CCItems.NOUGAT_POWDER.get()));
    }

    private void setNaturalSegment(boolean natural) {
        entityData.set(NATURAL_SEGMENT, natural);
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
        tag.putBoolean(TAG_LAUNCHED, launched);
        tag.putInt(TAG_LAUNCH_DELAY, launchDelay);
        tag.putInt(TAG_LAUNCH_FUSE, launchFuse);
        tag.putInt(TAG_MODE, getMode());
        tag.putBoolean(TAG_NATURAL_SEGMENT, entityData.get(NATURAL_SEGMENT));
        tag.putInt(TAG_GROWTH_TICKS, growthTicks);
        tag.putInt(TAG_POWDER_PROGRESS, powderProgress);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setLength(tag.contains(TAG_LENGTH) ? tag.getFloat(TAG_LENGTH) : MIN_BODY_LENGTH);
        stackCreated = tag.getBoolean(TAG_STACK_CREATED);
        launched = tag.getBoolean(TAG_LAUNCHED);
        launchDelay = tag.getInt(TAG_LAUNCH_DELAY);
        launchFuse = tag.getInt(TAG_LAUNCH_FUSE);
        setMode(tag.getInt(TAG_MODE));
        setNaturalSegment(tag.getBoolean(TAG_NATURAL_SEGMENT));
        growthTicks = tag.getInt(TAG_GROWTH_TICKS);
        powderProgress = tag.getInt(TAG_POWDER_PROGRESS);
    }

}
