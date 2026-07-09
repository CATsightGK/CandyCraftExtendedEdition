package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.joml.Vector3f;

public class BasicCandySpiderEntity extends Monster {
    private static final EntityDataAccessor<Boolean> ANGRY = SynchedEntityData.defineId(BasicCandySpiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CHILD = SynchedEntityData.defineId(BasicCandySpiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BOSS_AWAKE = SynchedEntityData.defineId(BasicCandySpiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BOSS_MELEE_MODE = SynchedEntityData.defineId(BasicCandySpiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> BOSS_ATTACK_STATE = SynchedEntityData.defineId(BasicCandySpiderEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BOSS_SHOOT_TICKS = SynchedEntityData.defineId(BasicCandySpiderEntity.class, EntityDataSerializers.INT);
    private static final EntityDimensions BEETLE_DIMENSIONS = EntityDimensions.scalable(1.0F, 0.8F);
    private static final EntityDimensions CHILD_BEETLE_DIMENSIONS = EntityDimensions.scalable(0.5F, 0.4F);
    private static final int BOSS_VOLLEY_CHARGE_DURATION = 45;
    private static final int BOSS_NO_TARGET_SLEEP_DELAY = 20 * 12;
    public static final int BOSS_ATTACK_NONE = 0;
    public static final int BOSS_ATTACK_VOLLEY_CHARGE = 1;
    public static final int BOSS_ATTACK_VOLLEY = 2;
    public static final int BOSS_ATTACK_SPIN = 3;
    private static final DustParticleOptions BOSS_BEETLE_LICORICE_SWIRL = new DustParticleOptions(new Vector3f(0.09F, 0.02F, 0.06F), 1.25F);
    private static final DustParticleOptions BOSS_BEETLE_CANDY_SWIRL = new DustParticleOptions(new Vector3f(0.92F, 0.34F, 0.55F), 0.95F);
    private static final DustParticleOptions BOSS_BEETLE_SUGAR_GLEAM = new DustParticleOptions(new Vector3f(1.0F, 0.74F, 0.88F), 0.65F);
    private boolean bossAwake;
    private boolean bossHealthBarRevealed;
    private int bossCooldown = 100;
    private int bossVolleyChargeTicks;
    private int bossVolleyTicks;
    private int bossSpinTicks;
    private int bossMeleeCooldown;
    private int bossNoTargetTicks;
    private int bossDormantShootCooldown;
    private int bossQueuedForcedVolleys;
    private boolean bossVolley75Triggered;
    private boolean bossVolley45Triggered;
    private final ServerBossEvent bossEvent = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);

    public BasicCandySpiderEntity(EntityType<? extends BasicCandySpiderEntity> type, Level level) {
        super(type, level);
        if (isBossBeetle()) {
            bossEvent.setVisible(false);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ANGRY, false);
        entityData.define(CHILD, false);
        entityData.define(BOSS_AWAKE, false);
        entityData.define(BOSS_MELEE_MODE, false);
        entityData.define(BOSS_ATTACK_STATE, BOSS_ATTACK_NONE);
        entityData.define(BOSS_SHOOT_TICKS, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        if (!isBossBeetle()) {
            goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.3D, false));
            goalSelector.addGoal(3, new RandomStrollGoal(this, 0.3D));
        }
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
            entity -> entity instanceof Player player && CandyTargeting.canAttackPlayer(player)));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    public boolean isAngry() {
        return entityData.get(ANGRY);
    }

    public void setAngry(boolean angry) {
        entityData.set(ANGRY, angry);
        if (angry && getAttribute(Attributes.MOVEMENT_SPEED) != null && getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(isBeetle() ? 1.5D : 0.5D);
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(isBeetle() ? 15.0D : 2.0D);
        }
    }

    public boolean isChildBeetle() {
        return entityData.get(CHILD);
    }

    public boolean isBossAwake() {
        return entityData.get(BOSS_AWAKE);
    }

    public int getBossAttackState() {
        return entityData.get(BOSS_ATTACK_STATE);
    }

    public int getBossShootTicks() {
        return entityData.get(BOSS_SHOOT_TICKS);
    }

    public boolean isBossMeleeMode() {
        return entityData.get(BOSS_MELEE_MODE);
    }

    private void setBossAwake(boolean awake) {
        bossAwake = awake;
        entityData.set(BOSS_AWAKE, awake);
        if (!awake) {
            bossNoTargetTicks = 0;
        }
    }

    public void setChildBeetle(boolean child) {
        entityData.set(CHILD, child);
        if (isBeetle() && getAttribute(Attributes.ATTACK_DAMAGE) != null && !isAngry()) {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(child ? 1.5D : 3.0D);
        }
        refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (!isBeetle()) {
            return super.getDimensions(pose);
        }
        return isChildBeetle() ? CHILD_BEETLE_DIMENSIONS : BEETLE_DIMENSIONS;
    }

    @Override
    public double getPassengersRidingOffset() {
        return isBeetle() ? 0.62D : super.getPassengersRidingOffset();
    }

    @Override
    public void aiStep() {
        if (!CandyTargeting.canAttackEntity(getTarget())) {
            setTarget(null);
        }
        updateBossBar();
        if (isBossBeetle()) {
            setNoGravity(false);
            if (!level().isClientSide && !getActiveEffects().isEmpty()) {
                removeAllEffects();
            }
            tickBossBeetleBehavior();
        } else {
            setNoGravity(false);
            tickBeetleBehavior();
        }
        if (isBeetle() && getVehicle() instanceof BasicCandySpiderEntity beetle) {
            setYRot(beetle.getYRot());
            yRotO = beetle.yRotO;
            yBodyRot = beetle.yBodyRot;
            yHeadRot = beetle.yHeadRot;
            setXRot(beetle.getXRot());
            xRotO = beetle.xRotO;
        }
        super.aiStep();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isBeetle()) {
            setAngry(true);
        }
        if (isBossBeetle()) {
            Entity directEntity = source.getDirectEntity();
            if (!level().isClientSide && source.getEntity() instanceof LivingEntity attacker && canBossTarget(attacker)) {
                bossHealthBarRevealed = true;
                setBossAwake(true);
                setTarget(attacker);
            }
            if (isBossMeleeHealth()) {
                boolean hurt = super.hurt(source, Math.min(amount, 3.0F));
                if (hurt && source.getEntity() instanceof LivingEntity attacker && canBossTarget(attacker)) {
                    setTarget(attacker);
                }
                if (hurt) {
                    checkBossVolleyHealthTriggers();
                }
                return hurt;
            }
            if (directEntity instanceof GummyBallEntity ball && ball.getPower() == 3) {
                setBossAwake(true);
                boolean hurt = super.hurt(source, 8.0F);
                if (hurt) {
                    checkBossVolleyHealthTriggers();
                }
                return hurt;
            }
            if (source.getEntity() instanceof LivingEntity && !level().isClientSide) {
                setBossAwake(true);
            }
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (isBossBeetle()) {
            bossEvent.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!CandyTargeting.canAttackEntity(target)) {
            setTarget(null);
            return false;
        }
        if (isBossBeetle()) {
            return false;
        }
        return super.doHurtTarget(target);
    }

    @Override
    public boolean isPushable() {
        return !isBossBeetle() && super.isPushable();
    }

    @Override
    protected void doPush(Entity entity) {
        if (!isBossBeetle()) {
            super.doPush(entity);
        }
    }

    @Override
    public void knockback(double strength, double x, double z) {
        if (!isBossBeetle()) {
            super.knockback(strength, x, z);
            return;
        }
        if (isBossMeleeMode() && hasSafeKnockbackLanding(strength, x, z)) {
            super.knockback(strength * 0.6D, x, z);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !isBossBeetle() && super.removeWhenFarAway(distanceToClosestPlayer);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !isBossBeetle() && super.canBeAffected(effect);
    }

    @Override
    public boolean isAffectedByPotions() {
        return !isBossBeetle() && super.isAffectedByPotions();
    }

    @Override
    public void die(DamageSource source) {
        if (isChildBeetle() && source.getEntity() instanceof Player) {
            List<BasicCandySpiderEntity> nearby = level().getEntitiesOfClass(BasicCandySpiderEntity.class, getBoundingBox().inflate(32.0D),
                entity -> entity != this && entity.isBeetle());
            for (BasicCandySpiderEntity beetle : nearby) {
                beetle.setAngry(true);
            }
        }
        super.die(source);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
        if (isBeetle() && !isChildBeetle() && level instanceof ServerLevel serverLevel && random.nextInt(10) == 0) {
            BasicCandySpiderEntity child = CCEntityTypes.BEETLE.get().create(serverLevel);
            if (child != null) {
                child.setChildBeetle(true);
                child.moveTo(getX(), getY(), getZ(), getYRot(), 0.0F);
                child.startRiding(this);
                serverLevel.addFreshEntity(child);
            }
        }
        if (isBeetle()) {
            refreshDimensions();
        }
        return data;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (isBossBeetle()) {
            spawnAtLocation(CCItems.RECORD_4.get());
            spawnAtLocation(CCItems.BEETLE_KEY.get());
            spawnAtLocation(CCItems.CHEWING_GUM_EMBLEM.get());
        } else if (isBeetle()) {
            if (!isChildBeetle() && random.nextInt(80) == 0) {
                spawnAtLocation(CCBlocks.BEETLE_EGG_BLOCK.get());
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Angry", isAngry());
        tag.putBoolean("Child", isChildBeetle());
        tag.putBoolean("BossAwake", bossAwake);
        tag.putBoolean("BossHealthBarRevealed", bossHealthBarRevealed);
        tag.putBoolean("BossVolley75Triggered", bossVolley75Triggered);
        tag.putBoolean("BossVolley45Triggered", bossVolley45Triggered);
        tag.putInt("BossNoTargetTicks", bossNoTargetTicks);
        tag.putInt("BossQueuedForcedVolleys", bossQueuedForcedVolleys);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setAngry(tag.getBoolean("Angry"));
        setChildBeetle(tag.getBoolean("Child"));
        setBossAwake(tag.getBoolean("BossAwake"));
        bossHealthBarRevealed = tag.getBoolean("BossHealthBarRevealed");
        bossVolley75Triggered = tag.getBoolean("BossVolley75Triggered");
        bossVolley45Triggered = tag.getBoolean("BossVolley45Triggered");
        bossNoTargetTicks = tag.getInt("BossNoTargetTicks");
        bossQueuedForcedVolleys = tag.getInt("BossQueuedForcedVolleys");
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isBossBeetle() ? null : super.getAmbientSound();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return (isBeetle() || isBossBeetle()) ? null : super.getHurtSound(source);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return (isBeetle() || isBossBeetle()) ? null : super.getDeathSound();
    }

    private void tickBeetleBehavior() {
        if (!level().isClientSide && isBeetle() && !isChildBeetle()) {
            stopNarrowPassageNavigation();
        }

        if (level().isClientSide && isAngry() && random.nextInt(20) == 0) {
            for (int i = 0; i < 2; i++) {
                level().addParticle(ParticleTypes.ANGRY_VILLAGER, getRandomX(1.0D), getRandomY(), getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
            }
        }

        if (!level().isClientSide && shouldPlaceChewingGumTrap() && random.nextInt(60) == 0) {
            BlockPos center = getTarget().blockPosition();
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = center.offset(x, 0, z);
                    if (random.nextBoolean() && canReplaceWithChewingGum(pos) && CCBlocks.CHEWING_GUM_PUDDLE.get().defaultBlockState().canSurvive(level(), pos)) {
                        level().setBlock(pos, CCBlocks.CHEWING_GUM_PUDDLE.get().defaultBlockState(), 3);
                    }
                }
            }
        }
    }

    private void stopNarrowPassageNavigation() {
        Path path = getNavigation().getPath();
        if (path == null || path.isDone()) {
            return;
        }
        Node node = path.getNextNode();
        BlockPos next = new BlockPos(node.x, node.y, node.z);
        BlockPos current = blockPosition();
        int dx = Integer.compare(next.getX(), current.getX());
        int dz = Integer.compare(next.getZ(), current.getZ());
        if (dx == 0 && dz == 0) {
            return;
        }
        boolean wideEnough = dx != 0
            ? hasAdultBeetleClearance(next.north()) && hasAdultBeetleClearance(next.south())
            : hasAdultBeetleClearance(next.east()) && hasAdultBeetleClearance(next.west());
        if (!wideEnough) {
            getNavigation().stop();
        }
    }

    private boolean hasAdultBeetleClearance(BlockPos pos) {
        return level().getBlockState(pos).getCollisionShape(level(), pos).isEmpty()
            && level().getBlockState(pos.above()).getCollisionShape(level(), pos.above()).isEmpty();
    }

    private boolean shouldPlaceChewingGumTrap() {
        if (!isBeetle() || isChildBeetle() || getTarget() == null) {
            return false;
        }
        Entity target = getTarget();
        return target instanceof Player
            && (distanceToSqr(target) > 4.0D || !hasLineOfSight(target));
    }

    private boolean canReplaceWithChewingGum(BlockPos pos) {
        return level().isEmptyBlock(pos)
            || level().getBlockState(pos).is(CCBlocks.SWEET_GRASS.get())
            || level().getBlockState(pos).is(CCBlocks.SWEET_GRASS_PINK.get())
            || level().getBlockState(pos).is(CCBlocks.SWEET_GRASS_PALE.get())
            || level().getBlockState(pos).is(CCBlocks.SWEET_GRASS_YELLOW.get())
            || level().getBlockState(pos).is(CCBlocks.SWEET_GRASS_RED.get());
    }

    private void tickBossBeetleBehavior() {
        if (level().isClientSide) {
            return;
        }

        if (getBossShootTicks() > 0) {
            entityData.set(BOSS_SHOOT_TICKS, getBossShootTicks() - 1);
        }
        if (!bossAwake) {
            entityData.set(BOSS_MELEE_MODE, false);
            entityData.set(BOSS_ATTACK_STATE, BOSS_ATTACK_NONE);
            entityData.set(BOSS_SHOOT_TICKS, 0);
            healBossBeetleWithoutTarget();
            setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
            if (getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0D);
            }
            tickDormantBossBeetleShooting();
            return;
        }
        if (getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35D);
        }

        LivingEntity target = getTarget();
        if (!canBossTarget(target) || distanceToSqr(target) > 48.0D * 48.0D) {
            target = null;
        }
        if (target == null) {
            bossNoTargetTicks++;
            healBossBeetleWithoutTarget();
            bossVolleyChargeTicks = 0;
            bossVolleyTicks = 0;
            bossSpinTicks = 0;
            bossMeleeCooldown = 0;
            entityData.set(BOSS_MELEE_MODE, false);
            entityData.set(BOSS_ATTACK_STATE, BOSS_ATTACK_NONE);
            entityData.set(BOSS_SHOOT_TICKS, 0);
            setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
            getNavigation().stop();
            if (bossNoTargetTicks >= BOSS_NO_TARGET_SLEEP_DELAY) {
                setBossAwake(false);
            }
            return;
        }
        bossNoTargetTicks = 0;
        setTarget(target);
        getLookControl().setLookAt(target, 10.0F, getMaxHeadXRot());
        if (!isBossMeleeHealth()) {
            faceBossBeetleTarget(target);
        }
        checkBossVolleyHealthTriggers();
        if (tryStartForcedBossVolley()) {
            entityData.set(BOSS_MELEE_MODE, false);
            entityData.set(BOSS_ATTACK_STATE, BOSS_ATTACK_VOLLEY_CHARGE);
            return;
        }
        if (bossCooldown > 0) {
            bossCooldown--;
        }
        if (bossVolleyChargeTicks > 0) {
            entityData.set(BOSS_MELEE_MODE, false);
            entityData.set(BOSS_ATTACK_STATE, BOSS_ATTACK_VOLLEY_CHARGE);
            spawnBossBeetleVolleyChargeParticles((ServerLevel) level());
            bossVolleyChargeTicks--;
            if (bossVolleyChargeTicks <= 0) {
                bossVolleyTicks = 50;
            }
            return;
        }
        if (bossVolleyTicks > 0 && tickCount % 2 == 0) {
            entityData.set(BOSS_MELEE_MODE, false);
            entityData.set(BOSS_ATTACK_STATE, BOSS_ATTACK_VOLLEY);
            bossVolleyTicks--;
            shootBossBall(target, 3, true);
        }
        if (bossSpinTicks > 0) {
            entityData.set(BOSS_MELEE_MODE, false);
            entityData.set(BOSS_ATTACK_STATE, BOSS_ATTACK_SPIN);
            bossSpinTicks--;
            if (bossSpinTicks < 100) {
                shootBossBall(target, 2, false);
            }
        }
        if (bossVolleyChargeTicks <= 0 && bossVolleyTicks <= 0 && bossSpinTicks <= 0 && isBossMeleeHealth()) {
            tickBossMeleeMode(target);
            return;
        }
        if (bossVolleyTicks <= 0 && bossSpinTicks <= 0) {
            entityData.set(BOSS_MELEE_MODE, false);
            entityData.set(BOSS_ATTACK_STATE, BOSS_ATTACK_NONE);
        }
        if (bossCooldown <= 0) {
            double healthPercent = getHealth() / getMaxHealth();
            bossCooldown = (int)(40 - (35 - healthPercent * 35));
            if (healthPercent < 0.5D && random.nextInt(6) == 0) {
                beginBossVolley();
            } else if (healthPercent < 0.84D && random.nextInt(10) == 0) {
                bossSpinTicks = 200;
            } else {
                shootBossBall(target, 3, false);
            }
        }
    }

    private boolean tryStartForcedBossVolley() {
        if (bossVolleyChargeTicks > 0 || bossVolleyTicks > 0) {
            return false;
        }
        if (bossQueuedForcedVolleys > 0) {
            bossQueuedForcedVolleys--;
            beginBossVolley();
            return true;
        }
        checkBossVolleyHealthTriggers();
        if (bossQueuedForcedVolleys > 0) {
            bossQueuedForcedVolleys--;
            beginBossVolley();
            return true;
        }
        return false;
    }

    private void checkBossVolleyHealthTriggers() {
        if (!isBossBeetle()) {
            return;
        }
        if (!bossVolley75Triggered && getHealth() <= 75.0F) {
            bossVolley75Triggered = true;
            bossQueuedForcedVolleys++;
        }
        if (!bossVolley45Triggered && getHealth() <= 45.0F) {
            bossVolley45Triggered = true;
            bossQueuedForcedVolleys++;
        }
    }

    private void beginBossVolley() {
        bossVolleyChargeTicks = BOSS_VOLLEY_CHARGE_DURATION;
        bossVolleyTicks = 0;
        bossSpinTicks = 0;
        bossCooldown = 80;
    }

    private void healBossBeetleWithoutTarget() {
        heal(5.0F);
    }

    private void tickBossMeleeMode(LivingEntity target) {
        entityData.set(BOSS_MELEE_MODE, true);
        entityData.set(BOSS_ATTACK_STATE, BOSS_ATTACK_NONE);
        if (getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.46D);
        }
        getLookControl().setLookAt(target, 10.0F, getMaxHeadXRot());
        getNavigation().moveTo(target, 0.46D);
        if (bossCooldown <= 0) {
            shootBossBall(target, 3, false, true);
            double healthPercent = getHealth() / getMaxHealth();
            bossCooldown = (int)(40 - (35 - healthPercent * 35));
        }
        if (bossMeleeCooldown > 0) {
            bossMeleeCooldown--;
        }
        if (distanceToSqr(target) <= 9.0D && hasLineOfSight(target) && bossMeleeCooldown <= 0) {
            target.hurt(damageSources().mobAttack(this), 4.0F);
            target.knockback(1.15D, getX() - target.getX(), getZ() - target.getZ());
            bossMeleeCooldown = 28;
            swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        }
    }

    private boolean isBossMeleeHealth() {
        return isBossBeetle() && getHealth() <= getMaxHealth() * 0.3F;
    }

    private boolean hasSafeKnockbackLanding(double strength, double x, double z) {
        double length = Math.sqrt(x * x + z * z);
        if (length < 1.0E-4D) {
            return true;
        }
        double distance = Math.min(2.5D, Math.max(0.4D, strength * 1.8D));
        double dx = x / length * distance;
        double dz = z / length * distance;
        BlockPos landing = BlockPos.containing(getX() - dx, getY() - 0.1D, getZ() - dz);
        for (int yOffset = 0; yOffset <= 2; yOffset++) {
            BlockPos feet = landing.below(yOffset);
            if (level().getBlockState(feet).isFaceSturdy(level(), feet, net.minecraft.core.Direction.UP)) {
                return true;
            }
        }
        return false;
    }

    private void faceBossBeetleTarget(LivingEntity target) {
        float[] aim = bossBeetleAimTo(target);
        setYRot(aim[0]);
        setXRot(aim[1]);
        yBodyRot = aim[0];
        yHeadRot = aim[0];
    }

    private float[] bossBeetleAimTo(LivingEntity target) {
        double dx = target.getX() - getX();
        double dy = target.getEyeY() - getEyeY();
        double dz = target.getZ() - getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (horizontal < 1.0E-4D) {
            return new float[] { getYRot(), getXRot() };
        }
        float yaw = (float)(Mth.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float)(-(Mth.atan2(dy, horizontal) * 180.0D / Math.PI));
        return new float[] { yaw, pitch };
    }

    private void updateBossBar() {
        if (!isBossBeetle() || level().isClientSide) {
            return;
        }
        Component name = getType().getDescription();
        bossEvent.setName(name);
        bossEvent.setProgress(Math.max(0.0F, Math.min(1.0F, getHealth() / getMaxHealth())));
        bossEvent.setVisible(bossHealthBarRevealed);
    }

    private void spawnBossBeetleVolleyChargeParticles(ServerLevel level) {
        double chargeProgress = 1.0D - (double) bossVolleyChargeTicks / (double) BOSS_VOLLEY_CHARGE_DURATION;
        double centerY = getY() + 3.0D + chargeProgress * 0.2D;
        double spiralSpin = tickCount * 0.38D;
        double maxRadius = 0.55D + chargeProgress * 0.95D;

        for (int i = 0; i < 24; i++) {
            double step = i / 23.0D;
            double radius = 0.12D + maxRadius * step;
            double waveY = Math.sin(spiralSpin + i * 0.5D) * 0.055D;

            for (int arm = 0; arm < 2; arm++) {
                double angle = spiralSpin * (arm == 0 ? 1.0D : -1.0D) + i * 0.42D + arm * Math.PI;
                double x = getX() + Math.cos(angle) * radius;
                double z = getZ() + Math.sin(angle) * radius;
                DustParticleOptions particle = (i + arm) % 3 == 0 ? BOSS_BEETLE_CANDY_SWIRL : BOSS_BEETLE_LICORICE_SWIRL;
                level.sendParticles(particle, x, centerY + waveY, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                if ((i + arm + tickCount) % 4 == 0) {
                    level.sendParticles(ParticleTypes.FLAME, x, centerY + waveY, z, 1, 0.0D, 0.02D, 0.0D, 0.0D);
                }
            }
        }

        for (int i = 0; i < 10; i++) {
            double angle = tickCount * 0.22D + i * Math.PI * 2.0D / 10.0D;
            double petalRadius = maxRadius * (0.72D + Math.sin(angle * 3.0D + tickCount * 0.12D) * 0.18D);
            double x = getX() + Math.cos(angle) * petalRadius;
            double z = getZ() + Math.sin(angle) * petalRadius;
            level.sendParticles(BOSS_BEETLE_SUGAR_GLEAM, x, centerY + 0.05D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        if (tickCount % 3 == 0) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), centerY, getZ(), 3, 0.2D, 0.04D, 0.2D, 0.02D);
        }
    }

    private void shootBossBall(LivingEntity target, int power, boolean lob) {
        shootBossBall(target, power, lob, true);
    }

    private void shootBossBall(LivingEntity target, int power, boolean lob, boolean turnBody) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        float[] aim = bossBeetleAimTo(target);
        if (turnBody) {
            faceBossBeetleTarget(target);
        }
        GummyBallEntity ball = new GummyBallEntity(serverLevel, this, power);
        ball.setBossBeetleProjectile(true);
        if (lob) {
            ball.setAirState(1);
            ball.setPower(3);
            ball.setPos(getX(), getY() + 2.0D, getZ());
            ball.setDeltaMovement(((random.nextBoolean() ? -1.0D : 1.0D) * 3.0D + random.nextDouble() * 6.0D) / 40.0D,
                1.5D,
                ((random.nextBoolean() ? -1.0D : 1.0D) * 3.0D + random.nextDouble() * 6.0D) / 40.0D);
        } else {
            float velocity = power == 3 ? 0.8F : 1.5F;
            ball.shootFromCandySource(this, aim[0], aim[1], velocity);
            if (power == 2) {
                ball.setAirState(3);
            }
        }
        serverLevel.addFreshEntity(ball);
        if (power == 3 && getBossShootTicks() <= 2) {
            entityData.set(BOSS_SHOOT_TICKS, 6);
        }
        playSound(net.minecraft.sounds.SoundEvents.ARROW_SHOOT, 1.0F, 0.8F + random.nextFloat() * 0.4F);
    }

    private void tickDormantBossBeetleShooting() {
        LivingEntity target = nearestDormantBossPlayer(5.0D);
        if (target == null) {
            if (bossDormantShootCooldown > 0) {
                bossDormantShootCooldown--;
            }
            return;
        }
        faceBossBeetleTarget(target);
        getLookControl().setLookAt(target, 10.0F, getMaxHeadXRot());
        if (bossDormantShootCooldown > 0) {
            bossDormantShootCooldown--;
            return;
        }
        shootBossBall(target, 3, false, true);
        bossDormantShootCooldown = 36 + random.nextInt(18);
    }

    @Nullable
    private LivingEntity nearestDormantBossPlayer(double range) {
        return level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(range),
                player -> CandyTargeting.canAttackPlayer(player) && isDormantBossPlayerInFront(player))
            .stream()
            .min(java.util.Comparator.comparingDouble(this::distanceToSqr))
            .orElse(null);
    }

    private boolean isDormantBossPlayerInFront(Player player) {
        if (!hasLineOfSight(player) || Math.abs(player.getY() - getY()) > 2.25D || player.getEyeY() < getY() + 0.45D) {
            return false;
        }
        double dx = player.getX() - getX();
        double dz = player.getZ() - getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (horizontal < 1.0E-4D || horizontal > 5.0D) {
            return false;
        }
        float yawRad = getYRot() * ((float)Math.PI / 180.0F);
        double lookX = -Mth.sin(yawRad);
        double lookZ = Mth.cos(yawRad);
        double dot = lookX * (dx / horizontal) + lookZ * (dz / horizontal);
        return dot >= 0.35D;
    }

    private LivingEntity nearestBossTarget(double range) {
        return level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(range), this::canBossTarget)
            .stream()
            .min(java.util.Comparator.comparingDouble(this::distanceToSqr))
            .orElse(null);
    }

    private boolean canBossTarget(@Nullable Entity entity) {
        if (!(entity instanceof LivingEntity living) || entity == this || !living.isAlive()) {
            return false;
        }
        if (entity instanceof BasicCandySpiderEntity spider && spider.isBossBeetle()) {
            return false;
        }
        return CandyTargeting.canAttackEntity(entity);
    }

    private boolean isBeetle() {
        return getType() == CCEntityTypes.BEETLE.get();
    }

    private boolean isBossBeetle() {
        return getType() == CCEntityTypes.BOSS_BEETLE.get();
    }
}
