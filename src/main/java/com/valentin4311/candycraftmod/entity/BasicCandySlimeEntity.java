package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCFluids;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import org.jetbrains.annotations.Nullable;

public class BasicCandySlimeEntity extends Slime {
    public static final int JELLY_QUEEN_SLEEP_MODE = 0;
    public static final int JELLY_QUEEN_PINK_MODE = 1;
    public static final int JELLY_QUEEN_BLUE_MODE = 2;
    public static final int JELLY_QUEEN_BROWN_MODE = 3;
    private static final int BOSS_SLAM_POSE_TICKS = 52;
    private static final int KING_EXPAND_POSE_TICKS = 36;
    private static final int KING_DASH_POSE_TICKS = 54;
    private static final int KING_DASH_CHARGE_TICKS = 22;
    private static final int BOSS_BOUNCE_POSE_TICKS = 64;
    private static final int BOSS_BOUNCE_CHARGE_TICKS = 40;
    private static final int BOSS_REST_TICKS = 40;
    private static final int PEZ_ROLLING_TICKS = 160;
    private static final int PEZ_ROLL_ATTACH_WAIT_TICKS = 70;
    private static final int PEZ_ROLL_ATTACK_TICKS = 30;
    private static final int PEZ_ROLL_REST_TICKS = 60;
    private static final int PEZ_ROLL_TOTAL_TICKS = PEZ_ROLLING_TICKS + PEZ_ROLL_ATTACH_WAIT_TICKS + PEZ_ROLL_ATTACK_TICKS + PEZ_ROLL_REST_TICKS;
    private static final double PEZ_ROLL_ANIMATION_SECONDS = 0.50251D;
    private static final double PEZ_ROLL_MIN_WAYPOINT_DISTANCE = 18.0D;
    private static final double PEZ_ROLL_MAX_WAYPOINT_DISTANCE = 56.0D;
    private static final int BOSS_LOST_TARGET_TICKS = 200;
    private static final double BOSS_TARGET_RANGE = 64.0D;
    private static final EntityDataAccessor<Boolean> BOSS_AWAKE = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> JELLY_QUEEN_MODE = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BOSS_SLAM_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> KING_EXPAND_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> KING_DASH_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PEZ_ROLL_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PEZ_ATTACH_FACE = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PEZ_ROLL_DIRECTION = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PEZ_ROLL_STEPS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BOSS_BOUNCE_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BOSS_RESTING_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
    private int specialAttackCooldown;
    private boolean dormantRotationInitialized;
    private float dormantYRot;
    private float dormantYHeadRot;
    private int bossJumpCooldown;
    private int bossLostTargetTicks;
    private int bossRangedCooldown;
    private int kingExpandCooldown;
    private int kingDashCooldown;
    private int kingDashChargeTicks;
    private int bossBounceCooldown;
    private int bossTargetSearchCooldown;
    private boolean bossBounceDamageReady;
    private boolean kingExpandDamageReady;
    @Nullable
    private LivingEntity kingDashTarget;
    private int bossMultiTargetSlamCount;
    private int pezSlamCount;
    private int pezRollCooldown;
    private int pezRollDirectionTicks;
    private int pezRollTargetBiasTicks;
    private int pezRollTurnLockTicks;
    private int pezRollBrushDamageCooldown;
    private Vec3 pezRollTangent = Vec3.ZERO;
    private Vec3 pezRollSmoothedTangent = Vec3.ZERO;
    private Vec3 pezRollWaypoint = Vec3.ZERO;
    private Vec3 pezRollNextWaypoint = Vec3.ZERO;
    private boolean pezRollAttackReleased;
    @Nullable
    private LivingEntity pezRollTarget;
    private boolean bossWasOnGround = true;
    private float bossLastFallDistance;
    private boolean bossSlamAttackActive;
    private boolean bossSlamDamageReady;
    private int bossSlamBlockedTicks;
    private int bossSlamStrafeSide = 1;
    private boolean pezDeathSplitSpawned;
    private final ServerBossEvent bossEvent = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    @Nullable
    private LivingEntity bossRetaliationTarget;

    public BasicCandySlimeEntity(EntityType<? extends BasicCandySlimeEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(BOSS_AWAKE, false);
        entityData.define(JELLY_QUEEN_MODE, JELLY_QUEEN_SLEEP_MODE);
        entityData.define(BOSS_SLAM_TICKS, 0);
        entityData.define(KING_EXPAND_TICKS, 0);
        entityData.define(KING_DASH_TICKS, 0);
        entityData.define(PEZ_ROLL_TICKS, 0);
        entityData.define(PEZ_ATTACH_FACE, Direction.UP.ordinal());
        entityData.define(PEZ_ROLL_DIRECTION, Direction.NORTH.ordinal());
        entityData.define(PEZ_ROLL_STEPS, 0);
        entityData.define(BOSS_BOUNCE_TICKS, 0);
        entityData.define(BOSS_RESTING_TICKS, 0);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData, tag);
        applyLegacySpawnSize();
        return data;
    }

    @Override
    public void aiStep() {
        if (isCandyBoss() && !isBossAwake()) {
            freezeSleepingBoss();
        }
        super.aiStep();
        updateBossBar();
        if (specialAttackCooldown > 0) {
            specialAttackCooldown--;
        }
        if (bossTargetSearchCooldown > 0) {
            bossTargetSearchCooldown--;
        }
        if (isTornadoJelly() && !onGround() && level().isClientSide && tickCount % 4 == 0) {
            level().addParticle(ParticleTypes.CLOUD, getRandomX(0.8D), getRandomY(), getRandomZ(0.8D), 0.0D, 0.02D, 0.0D);
        }
        tickBossSlamLandingAndAnimation();
        tickKingBossSpecialAnimation();
        tickBossBounceAndRest();
        tickPezRollSkill();
        tickBossAwakeBehavior();
        if (isCandyBoss() && !isBossAwake()) {
            freezeSleepingBoss();
        }
        tickRetaliationTarget();
    }

    @Override
    protected int getJumpDelay() {
        if (isYellowJelly()) {
            return 4;
        }
        if (isPezJelly()) {
            return 8;
        }
        if (isJellyQueen()) {
            return nextJellyQueenJumpDelay();
        }
        return super.getJumpDelay();
    }

    @Override
    protected void jumpFromGround() {
        if (isCandyBoss() && !isBossAwake()) {
            return;
        }
        super.jumpFromGround();
    }

    @Override
    public void playerTouch(Player player) {
        if (!isSurvivalLike(player)) {
            super.playerTouch(player);
            return;
        }
        if (!isAlive() || specialAttackCooldown > 0) {
            super.playerTouch(player);
            return;
        }
        if (isCandyBoss() && !isBossAwake()) {
            return;
        }
        if (isYellowJelly()) {
            specialAttackCooldown = 10;
            player.hurt(damageSources().mobAttack(this), 6.0F);
            playSound(SoundEvents.SLIME_ATTACK, 1.0F, 1.0F);
        } else if (isRedJelly()) {
            specialAttackCooldown = 20;
            if (!level().isClientSide) {
                player.hurt(damageSources().mobAttack(this), 6.0F);
                level().explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.NONE);
                discard();
            }
        } else if (isTornadoJelly()) {
            specialAttackCooldown = 20;
            if (!level().isClientSide) {
                player.hurt(damageSources().mobAttack(this), 6.0F);
                level().explode(this, getX(), getY(), getZ(), 1.0F, Level.ExplosionInteraction.NONE);
                discard();
            }
        } else if (isPezJelly() || isKingSlime() || isJellyQueen()) {
            specialAttackCooldown = 15;
            hurtPlayerWithLegacyBossContact(player);
        }
        super.playerTouch(player);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        LivingEntity livingAttacker = getLivingAttacker(source);
        if (isInGrenadine() && source.getEntity() == null && source.getDirectEntity() == null) {
            return false;
        }
        if (isCandyBoss() && source.is(DamageTypeTags.IS_PROJECTILE)) {
            boolean wasAwake = isBossAwake();
            if (!level().isClientSide && livingAttacker != null && (wasAwake || shouldDormantBossWakeFromMobAttack(livingAttacker))) {
                activateBossFromDamage(livingAttacker);
            }
            reflectProjectile(source);
            if (!wasAwake) {
                return false;
            }
            if (isBossAwake() && random.nextInt(4) != 0) {
                return false;
            }
            return super.hurt(source, amount * 0.5F);
        }
        if (source.getEntity() instanceof BasicCandySlimeEntity slimeAttacker && !isCandyBoss() && !isPezJelly() && !isForcedJellyConflict(slimeAttacker, this)) {
            return false;
        }
        if (!level().isClientSide && livingAttacker != null) {
            if (isCandyBoss()) {
                activateBossFromDamage(livingAttacker);
            } else {
                setRetaliationTarget(livingAttacker);
            }
        }
        if (isCandyBoss() && source.is(DamageTypeTags.IS_FALL)) {
            return false;
        }
        if (isJellyQueen()) {
            if (!level().isClientSide && livingAttacker != null) {
                activateBossFromDamage(livingAttacker);
                if (isBossAwake()) {
                    updateJellyQueenMode();
                }
                if (source.getEntity() instanceof Player player && amount > 1.0F && !player.getAbilities().instabuild) {
                    knockbackAttackingPlayer(player);
                }
            }
            boolean hurt = super.hurt(source, amount);
            if (!level().isClientSide && hurt && livingAttacker != null) {
                activateBossFromDamage(livingAttacker);
            }
            return hurt;
        }
        if (isKingSlime() && !level().isClientSide && getSize() > 1) {
            shrinkKingSlimeFromHealth();
        }
        if (isCandyBoss() && !level().isClientSide && source.getEntity() != null) {
            if (source.getEntity() instanceof Player player && amount > 1.0F && !player.getAbilities().instabuild) {
                knockbackAttackingPlayer(player);
            }
        }
        boolean hurt = super.hurt(source, amount);
        if (!level().isClientSide && hurt && isCandyBoss() && livingAttacker != null) {
            activateBossFromDamage(livingAttacker);
        }
        return hurt;
    }

    @Nullable
    private static LivingEntity getLivingAttacker(DamageSource source) {
        if (source.getEntity() instanceof LivingEntity attacker) {
            return attacker;
        }
        if (source.getDirectEntity() instanceof LivingEntity attacker) {
            return attacker;
        }
        return null;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!canAttackTarget(target)) {
            setTarget(null);
            return false;
        }
        if (!isAlive() || specialAttackCooldown > 0) {
            return false;
        }
        if (isCandyBoss() && !isBossAwake()) {
            return false;
        }
        if (isYellowJelly()) {
            specialAttackCooldown = 10;
            return hurtCandyTarget(target, 6.0F);
        }
        if (isRedJelly()) {
            specialAttackCooldown = 20;
            if (!level().isClientSide) {
                hurtCandyTarget(target, 6.0F);
                level().explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.NONE);
                discard();
            }
            return true;
        }
        if (isTornadoJelly()) {
            specialAttackCooldown = 20;
            if (!level().isClientSide) {
                hurtCandyTarget(target, 6.0F);
                level().explode(this, getX(), getY(), getZ(), 1.0F, Level.ExplosionInteraction.NONE);
                discard();
            }
            return true;
        }
        if (isPezJelly() || isKingSlime() || isJellyQueen()) {
            specialAttackCooldown = 15;
            return hurtLegacyBossTarget(target);
        }
        return super.doHurtTarget(target);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (isCandyBoss()) {
            bossEvent.addPlayer(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide && reason == RemovalReason.KILLED && getSize() > 1) {
            splitPezJellyOnDeath();
            setSize(1, false);
        }
        super.remove(reason);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        if (isJellyQueen()) {
            spawnAtLocation(CCItems.RECORD_1.get());
            spawnAtLocation(CCItems.JELLY_KEY.get());
            spawnAtLocation(CCItems.JELLY_EMBLEM.get());
        } else if (isPezJelly() && getSize() <= 1) {
            spawnAtLocation(CCItems.JELLY_SENTRY_KEY.get());
        } else if (isKingSlime() && getSize() <= 1) {
            spawnAtLocation(CCItems.JELLY_BOSS_KEY.get());
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("BossAwake", isBossAwake());
        tag.putInt("BossLostTargetTicks", bossLostTargetTicks);
        tag.putInt("BossRangedCooldown", bossRangedCooldown);
        tag.putInt("KingExpandCooldown", kingExpandCooldown);
        tag.putInt("KingDashCooldown", kingDashCooldown);
        tag.putInt("BossMultiTargetSlamCount", bossMultiTargetSlamCount);
        tag.putInt("PezSlamCount", pezSlamCount);
        tag.putInt("PezRollCooldown", pezRollCooldown);
        if (isJellyQueen()) {
            tag.putInt("JellyQueenMode", getJellyQueenMode());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setBossAwake(tag.getBoolean("BossAwake"));
        bossLostTargetTicks = tag.getInt("BossLostTargetTicks");
        bossRangedCooldown = tag.getInt("BossRangedCooldown");
        kingExpandCooldown = tag.getInt("KingExpandCooldown");
        kingDashCooldown = tag.getInt("KingDashCooldown");
        bossMultiTargetSlamCount = tag.getInt("BossMultiTargetSlamCount");
        pezSlamCount = tag.getInt("PezSlamCount");
        pezRollCooldown = tag.getInt("PezRollCooldown");
        if (isJellyQueen()) {
            setJellyQueenMode(tag.contains("JellyQueenMode") ? tag.getInt("JellyQueenMode") : isBossAwake() ? JELLY_QUEEN_PINK_MODE : JELLY_QUEEN_SLEEP_MODE);
        }
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SLIME_DEATH;
    }

    @Override
    protected SoundEvent getSquishSound() {
        return SoundEvents.SLIME_SQUISH;
    }

    @Override
    protected SoundEvent getJumpSound() {
        return SoundEvents.SLIME_JUMP;
    }

    @Override
    protected ParticleOptions getParticleType() {
        return jellyLandingParticle();
    }

    @Override
    protected boolean spawnCustomParticles() {
        if (!level().isClientSide) {
            return true;
        }
        int size = getSize();
        ParticleOptions particle = getParticleType();
        for (int i = 0; i < size * 8; i++) {
            float angle = random.nextFloat() * ((float) Math.PI * 2.0F);
            float radius = random.nextFloat() * 0.5F + 0.5F;
            double dx = Mth.sin(angle) * size * 0.5F * radius;
            double dz = Mth.cos(angle) * size * 0.5F * radius;
            level().addParticle(particle, getX() + dx, getY(), getZ() + dz, 0.0D, 0.0D, 0.0D);
        }
        return true;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(CCSoundEvents.STEP_JELLY.get(), 0.18F, 0.9F + random.nextFloat() * 0.2F);
    }

    @Override
    public void setSize(int size, boolean resetHealth) {
        super.setSize(size, resetHealth);
        if (getAttribute(Attributes.MAX_HEALTH) != null) {
            if (isKingSlime()) {
                getAttribute(Attributes.MAX_HEALTH).setBaseValue(800.0D);
                if (resetHealth) {
                    setHealth(getMaxHealth());
                }
            } else if (isJellyQueen()) {
                getAttribute(Attributes.MAX_HEALTH).setBaseValue(300.0D);
                if (resetHealth) {
                    setHealth(getMaxHealth());
                }
            } else if (isPezJelly()) {
                getAttribute(Attributes.MAX_HEALTH).setBaseValue(size * 20.0D);
                if (resetHealth) {
                    setHealth(getMaxHealth());
                }
            }
        }
    }

    private void tickBossAwakeBehavior() {
        if (!isCandyBoss() || level().isClientSide) {
            return;
        }
        if (isJellyQueen()) {
            tickJellyQueenBossBehavior();
            return;
        }
        LivingEntity target = findBossAttackTarget();
        AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (target == null) {
            tickBossLostTarget(speed);
            return;
        }
        bossLostTargetTicks = isPezJelly() ? BOSS_LOST_TARGET_TICKS * 2 : BOSS_LOST_TARGET_TICKS;
        if (isBossResting()) {
            if (speed != null) {
                speed.setBaseValue(0.0D);
            }
            setDeltaMovement(0.0D, Math.min(0.0D, getDeltaMovement().y), 0.0D);
            getNavigation().stop();
            return;
        }
        if (isBossAwake() && speed != null) {
            speed.setBaseValue(isJellyQueen() ? 0.7D : isKingSlime() ? 0.45D : 0.38D);
        }
        tickBossRangedAttack(target);
        if (isKingSlime() || isJellyQueen()) {
            tickKingSlimeSpecials(target);
        }
        if (isPezJelly()) {
            tickPezExpandSpecial(target);
            tickPezRollTrigger(target);
            if (getPezRollTicks() > 0) {
                if (speed != null) {
                    speed.setBaseValue(0.0D);
                }
                return;
            }
        }
        if (bossJumpCooldown > 0) {
            bossJumpCooldown--;
        }
        if (isBossAwake() && onGround() && bossJumpCooldown <= 0) {
            bossJumpCooldown = isKingSlime() ? 18 + random.nextInt(28) : 12 + random.nextInt(22);
            launchBossSlamAt(target);
        }
    }

    private void tickJellyQueenBossBehavior() {
        LivingEntity target = findBossAttackTarget();
        AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (target == null) {
            tickBossLostTarget(speed);
            return;
        }
        bossLostTargetTicks = BOSS_LOST_TARGET_TICKS;

        if (!isBossAwake()) {
            setJellyQueenMode(JELLY_QUEEN_SLEEP_MODE);
            if (speed != null) {
                speed.setBaseValue(0.0D);
            }
            setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
            heal(5.0F);
            return;
        }

        if (speed != null) {
            speed.setBaseValue(0.699999988079071D);
        }
        if (isBossResting()) {
            if (speed != null) {
                speed.setBaseValue(0.0D);
            }
            setDeltaMovement(0.0D, Math.min(0.0D, getDeltaMovement().y), 0.0D);
            getNavigation().stop();
            return;
        }
        updateJellyQueenMode();
        tickBossRangedAttack(target);
        tickKingSlimeSpecials(target);
        if (bossJumpCooldown > 0) {
            bossJumpCooldown--;
        }
        if (onGround() && bossJumpCooldown <= 0) {
            bossJumpCooldown = Math.max(2, nextJellyQueenJumpDelay() / 3);
            launchBossSlamAt(target);
        }
    }

    private void tickBossSlamLandingAndAnimation() {
        if (!isCandyBoss() || level().isClientSide) {
            return;
        }
        int slamTicks = getBossSlamTicks();
        if (slamTicks > 0) {
            setBossSlamTicks(slamTicks - 1);
        }
        boolean grounded = onGround();
        if (bossSlamAttackActive && !bossWasOnGround && grounded) {
            bossLastFallDistance = fallDistance;
            setBossSlamTicks(BOSS_SLAM_POSE_TICKS / 2);
            int hits = damageBossSlamTargets();
            trackBossSlamCombo(hits);
            triggerBossSlamLandingEffects();
            tryStartHeightBonusDash();
            bossSlamAttackActive = false;
            bossSlamDamageReady = false;
        } else if (bossSlamAttackActive && !grounded && getDeltaMovement().y < -0.05D) {
            damageBossSlamTargets();
        }
        if (bossSlamAttackActive && !grounded && horizontalCollision) {
            bossSlamBlockedTicks = Math.min(20, bossSlamBlockedTicks + 1);
        } else if (!bossSlamAttackActive) {
            bossSlamBlockedTicks = Math.max(0, bossSlamBlockedTicks - 1);
        }
        bossWasOnGround = grounded;
    }

    private void tickKingBossSpecialAnimation() {
        if (!(isPezJelly() || isKingSlime() || isJellyQueen()) || level().isClientSide) {
            return;
        }
        if (kingExpandCooldown > 0) {
            kingExpandCooldown--;
        }
        if (kingDashCooldown > 0) {
            kingDashCooldown--;
        }
        int expandTicks = getKingExpandTicks();
        if (expandTicks > 0) {
            setKingExpandTicks(expandTicks - 1);
            setDeltaMovement(0.0D, Math.min(0.0D, getDeltaMovement().y), 0.0D);
            getNavigation().stop();
            if (kingExpandDamageReady && expandTicks <= 10) {
                kingExpandDamageReady = false;
                damageKingExpandTargets();
            }
            if (expandTicks - 1 <= 0) {
                startBossResting();
            }
        }
        int dashTicks = getKingDashTicks();
        if (dashTicks > 0) {
            setKingDashTicks(dashTicks - 1);
            spawnKingDashTrail();
            if (kingDashChargeTicks > 0) {
                kingDashChargeTicks--;
                if (kingDashChargeTicks == 0 && kingDashTarget != null && kingDashTarget.isAlive()) {
                    releaseKingDash(kingDashTarget);
                }
            } else if (dashTicks < KING_DASH_POSE_TICKS - KING_DASH_CHARGE_TICKS) {
                damageKingDashTargets();
            }
        } else {
            kingDashTarget = null;
            kingDashChargeTicks = 0;
        }
    }

    private void tickBossBounceAndRest() {
        if (!isCandyBoss() || level().isClientSide) {
            return;
        }
        int restTicks = getBossRestingTicks();
        if (restTicks > 0) {
            setBossRestingTicks(restTicks - 1);
            setDeltaMovement(0.0D, Math.min(0.0D, getDeltaMovement().y), 0.0D);
            getNavigation().stop();
            if (level() instanceof ServerLevel serverLevel && tickCount % 4 == 0) {
                serverLevel.sendParticles(ParticleTypes.FALLING_WATER, getX(), getY() + getBbHeight() * 0.7D, getZ(),
                    10, getBbWidth() * 0.32D, getBbHeight() * 0.22D, getBbWidth() * 0.32D, 0.035D);
            }
        }
        if (bossBounceCooldown > 0) {
            bossBounceCooldown--;
        }
        int bounceTicks = getBossBounceTicks();
        if (bounceTicks <= 0) {
            return;
        }
        setBossBounceTicks(bounceTicks - 1);
        setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
        getNavigation().stop();
        if (bossBounceDamageReady && bounceTicks <= BOSS_BOUNCE_POSE_TICKS - BOSS_BOUNCE_CHARGE_TICKS) {
            bossBounceDamageReady = false;
            damageBossBounceTargets();
        }
        if (bounceTicks - 1 <= 0) {
            startBossResting();
        }
    }

    private void launchBossSlamAt(LivingEntity target) {
        getLookControl().setLookAt(target);
        double horizontalPower = bossSlamHorizontalPower();
        double yPower = isInWater() ? 4.0D : 1.5D;
        Vec3 horizontal = chooseBossSlamVector(target, horizontalPower);
        setJumping(true);
        setDeltaMovement(horizontal.x, yPower, horizontal.z);
        hasImpulse = true;
        bossSlamAttackActive = true;
        bossSlamDamageReady = true;
        bossWasOnGround = false;
        setBossSlamTicks(BOSS_SLAM_POSE_TICKS);
        performBossSlamSpecials(target);
        playSound(getJumpSound(), getSoundVolume(), ((random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
    }

    private Vec3 chooseBossSlamVector(LivingEntity target, double horizontalPower) {
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double distance = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
        Vec3 direct = new Vec3(dx / distance, 0.0D, dz / distance);
        boolean shouldStrafe = bossSlamBlockedTicks >= 3 || !hasLineOfSight(target);
        if (!shouldStrafe || isBossSlamPathOpen(direct, horizontalPower)) {
            return direct.scale(horizontalPower);
        }

        double[] angles = {24.0D, -24.0D, 42.0D, -42.0D, 62.0D, -62.0D};
        Vec3 best = direct;
        double bestScore = -999.0D;
        for (double angle : angles) {
            double signedAngle = angle * bossSlamStrafeSide;
            Vec3 candidate = rotateHorizontal(direct, signedAngle);
            if (!isBossSlamPathOpen(candidate, horizontalPower)) {
                continue;
            }
            Vec3 projected = position().add(candidate.scale(Math.max(2.0D, distance * 0.55D)));
            double targetScore = -projected.distanceToSqr(target.position());
            double sideScore = Math.abs(angle) < 45.0D ? 0.35D : 0.0D;
            double score = targetScore + sideScore;
            if (score > bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        bossSlamStrafeSide *= -1;
        return best.scale(horizontalPower);
    }

    private boolean isBossSlamPathOpen(Vec3 direction, double horizontalPower) {
        if (direction.lengthSqr() < 1.0E-4D) {
            return false;
        }
        Vec3 normalized = direction.normalize();
        double step = Math.max(0.45D, horizontalPower * 1.15D);
        for (int i = 1; i <= 4; i++) {
            Vec3 offset = normalized.scale(step * i);
            if (!level().noCollision(this, getBoundingBox().move(offset.x, 0.35D + i * 0.12D, offset.z))) {
                return false;
            }
        }
        return true;
    }

    private static Vec3 rotateHorizontal(Vec3 direction, double degrees) {
        double radians = Math.toRadians(degrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(direction.x * cos - direction.z * sin, 0.0D, direction.x * sin + direction.z * cos).normalize();
    }

    private void tickBossRangedAttack(LivingEntity target) {
        if (!isBossAwake() || !(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (bossRangedCooldown > 0) {
            bossRangedCooldown--;
            return;
        }
        double distanceSqr = distanceToSqr(target);
        boolean highOrFar = target.getY() - getY() > 4.0D || distanceSqr > 144.0D;
        if (!highOrFar || !hasLineOfSight(target)) {
            return;
        }
        shootBossJellyBall(serverLevel, target);
        bossRangedCooldown = isJellyQueen() ? 48 + random.nextInt(28) : isKingSlime() ? 64 + random.nextInt(36) : 58 + random.nextInt(32);
    }

    private void shootBossJellyBall(ServerLevel level, LivingEntity target) {
        GummyBallEntity ball = new GummyBallEntity(level, this, 5);
        ball.setVisualVariant(bossJellyBallVisual());
        ball.setBonusDamage(5.0F);
        ball.setPos(getX(), getEyeY() - 0.1D, getZ());
        double dx = target.getX() - getX();
        double dy = target.getEyeY() - ball.getY();
        double dz = target.getZ() - getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        ball.shoot(dx, dy + distance * 0.08D, dz, 1.25F, 6.0F);
        level.addFreshEntity(ball);
        playSound(SoundEvents.SNOWBALL_THROW, 1.0F, 0.85F + random.nextFloat() * 0.25F);
    }

    private int bossJellyBallVisual() {
        if (isPezJelly()) {
            return GummyBallEntity.PEZ_JELLY_VISUAL;
        }
        if (isKingSlime()) {
            return GummyBallEntity.CARAMEL_KING_JELLY_VISUAL;
        }
        if (isJellyQueen()) {
            return GummyBallEntity.STRAWBERRY_QUEEN_JELLY_VISUAL;
        }
        if (isYellowJelly()) {
            return GummyBallEntity.LEMON_JELLY_VISUAL;
        }
        if (isRedJelly()) {
            return GummyBallEntity.RASPBERRY_JELLY_VISUAL;
        }
        if (isTornadoJelly()) {
            return GummyBallEntity.MINT_JELLY_VISUAL;
        }
        return 0;
    }

    private void tickKingSlimeSpecials(LivingEntity target) {
        if (!(isKingSlime() || isJellyQueen())) {
            return;
        }
        if (getKingExpandTicks() == 0 && kingExpandCooldown <= 0 && onGround() && Math.abs(getDeltaMovement().y) < 0.05D && distanceToSqr(target) < 196.0D && random.nextInt(28) == 0) {
            startKingExpandAttack();
            return;
        }
        if (getBossBounceTicks() == 0 && bossBounceCooldown <= 0 && onGround() && distanceToSqr(target) < 81.0D && random.nextInt(isJellyQueen() ? 48 : 36) == 0) {
            startBossBounceAttack();
            return;
        }
        if (getKingDashTicks() == 0 && kingDashCooldown <= 0 && distanceToSqr(target) > 49.0D && random.nextInt(28) == 0) {
            startKingDashAttack(target);
        }
    }

    private void startBossBounceAttack() {
        setBossBounceTicks(BOSS_BOUNCE_POSE_TICKS);
        bossBounceCooldown = isJellyQueen() ? 210 + random.nextInt(100) : 170 + random.nextInt(90);
        bossBounceDamageReady = true;
        setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
        getNavigation().stop();
        playSound(SoundEvents.SLIME_SQUISH, 1.35F, 0.5F);
    }

    private void damageBossBounceTargets() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        double radius = Math.max(isJellyQueen() ? 4.25D : 5.25D, getSize() * 0.58D);
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius, 1.8D, radius), this::canBossTarget)) {
            if (target == this) {
                continue;
            }
            if (target.hurt(damageSources().mobAttack(this), isJellyQueen() ? 4.0F : 8.0F)) {
                double dx = target.getX() - getX();
                double dz = target.getZ() - getZ();
                double length = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                double push = isJellyQueen() ? 1.35D : 2.0D;
                target.push(dx / length * push, isJellyQueen() ? 0.52D : 0.8D, dz / length * push);
                target.hurtMarked = true;
            }
        }
        serverLevel.sendParticles(jellyLandingParticle(), getX(), getY() + 0.2D, getZ(), 42, radius * 0.22D, 0.22D, radius * 0.22D, 0.09D);
        playSound(SoundEvents.SLIME_ATTACK, 1.55F, 0.62F);
    }

    private void startBossResting() {
        setBossRestingTicks(BOSS_REST_TICKS);
        setDeltaMovement(0.0D, Math.min(0.0D, getDeltaMovement().y), 0.0D);
        getNavigation().stop();
    }

    private void startKingExpandAttack() {
        setKingExpandTicks(KING_EXPAND_POSE_TICKS);
        kingExpandCooldown = isPezJelly() ? 180 + random.nextInt(80) : isJellyQueen() ? 190 + random.nextInt(90) : 150 + random.nextInt(80);
        kingExpandDamageReady = true;
        setDeltaMovement(0.0D, 0.0D, 0.0D);
        getNavigation().stop();
        playSound(SoundEvents.SLIME_SQUISH, 1.2F, 0.55F);
    }

    private void tryStartHeightBonusDash() {
        LivingEntity target = findBossAttackTarget();
        if (target == null || !(isKingSlime() || isJellyQueen()) || getKingDashTicks() > 0 || kingDashCooldown > 0) {
            return;
        }
        float fall = Math.max(bossLastFallDistance, fallDistance);
        if (fall < 5.0F) {
            return;
        }
        int chance = Mth.clamp(10 - (int)(fall * 0.9F), 2, 10);
        if (random.nextInt(chance) == 0) {
            startKingDashAttack(target);
        }
    }

    private void damageKingExpandTargets() {
        double radius = Math.max(isPezJelly() ? 5.25D : isJellyQueen() ? 7.5D : 9.0D, getSize() * 1.05D);
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius, 3.2D, radius), this::canBossTarget)) {
            if (target == this) {
                continue;
            }
            if (target.hurt(damageSources().mobAttack(this), isPezJelly() ? 5.5F : isJellyQueen() ? 4.5F : 9.0F)) {
                double dx = target.getX() - getX();
                double dz = target.getZ() - getZ();
                double length = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
                double push = isPezJelly() ? 1.65D : isJellyQueen() ? 1.8D : 2.7D;
                target.push(dx / length * push, isPezJelly() ? 0.62D : isJellyQueen() ? 0.7D : 1.05D, dz / length * push);
                target.hurtMarked = true;
            }
        }
        serverLevel.sendParticles(jellyLandingParticle(), getX(), getY() + 0.2D, getZ(), 76, radius * 0.33D, 0.38D, radius * 0.33D, 0.12D);
        playSound(SoundEvents.SLIME_ATTACK, 1.9F, 0.54F);
    }

    private void startKingDashAttack(LivingEntity target) {
        kingDashTarget = target;
        kingDashChargeTicks = KING_DASH_CHARGE_TICKS;
        kingDashCooldown = isJellyQueen() ? 220 + random.nextInt(100) : 170 + random.nextInt(90);
        setKingDashTicks(KING_DASH_POSE_TICKS);
        setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
        getNavigation().stop();
        getLookControl().setLookAt(target);
        playSound(SoundEvents.SLIME_SQUISH, 1.3F, 0.72F);
    }

    private void releaseKingDash(LivingEntity target) {
        Vec3 desired = target.position().subtract(position());
        Vec3 current = getDeltaMovement();
        if (desired.lengthSqr() < 1.0E-4D) {
            desired = getLookAngle();
        }
        Vec3 corrected = desired.normalize().scale(isJellyQueen() ? 1.65D : 2.15D).add(current.scale(0.15D));
        setDeltaMovement(corrected.x, Math.max(0.08D, corrected.y * 0.15D + 0.28D), corrected.z);
        hasImpulse = true;
        playSound(SoundEvents.SLIME_JUMP, 1.45F, 0.82F);
    }

    private void damageKingDashTargets() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.8D), this::canBossTarget)) {
            if (target == this) {
                continue;
            }
            if (target.hurt(damageSources().mobAttack(this), isJellyQueen() ? 7.0F : 14.0F)) {
                Vec3 velocity = getDeltaMovement();
                double horizontal = Math.max(0.1D, velocity.horizontalDistance());
                double push = isJellyQueen() ? 0.72D : 1.1D;
                target.push(velocity.x / horizontal * push, isJellyQueen() ? 0.35D : 0.55D, velocity.z / horizontal * push);
                target.hurtMarked = true;
                setKingDashTicks(Math.min(getKingDashTicks(), 10));
            }
        }
    }

    private void spawnKingDashTrail() {
        if (!(level() instanceof ServerLevel serverLevel) || kingDashChargeTicks > 0) {
            return;
        }
        serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new net.minecraft.world.item.ItemStack(isJellyQueen() ? CCItems.STRAWBERRY_QUEEN_JELLY_BALL.get() : CCItems.CARAMEL_KING_JELLY_BALL.get())),
            getX() - getDeltaMovement().x * 0.7D, getY() + getBbHeight() * 0.45D, getZ() - getDeltaMovement().z * 0.7D,
            4, getBbWidth() * 0.2D, getBbHeight() * 0.15D, getBbWidth() * 0.2D, 0.03D);
    }

    private void tickPezRollTrigger(LivingEntity target) {
        if (!isPezJelly()) {
            return;
        }
        if (pezRollCooldown > 0) {
            pezRollCooldown--;
        }
        if (pezSlamCount >= 5 && pezRollCooldown <= 0 && getPezRollTicks() == 0) {
            int chance = Math.max(1, 11 - pezSlamCount);
            if (pezSlamCount < 10 && random.nextInt(chance) != 0) {
                return;
            }
            startPezRollSkill(target);
            pezSlamCount = 0;
        }
    }

    private void tickPezExpandSpecial(LivingEntity target) {
        if (!isPezJelly() || getPezRollTicks() > 0 || getKingExpandTicks() > 0 || kingExpandCooldown > 0 || !onGround() || Math.abs(getDeltaMovement().y) >= 0.05D) {
            return;
        }
        if (distanceToSqr(target) < 81.0D && random.nextInt(42) == 0) {
            startKingExpandAttack();
        }
    }

    private void tickPezRollSkill() {
        if (!isPezJelly() || level().isClientSide) {
            return;
        }
        int ticks = getPezRollTicks();
        if (ticks <= 0) {
            setNoGravity(false);
            return;
        }
        setPezRollTicks(ticks - 1);
        int elapsed = PEZ_ROLL_TOTAL_TICKS - ticks;
        LivingEntity target = pezRollTarget != null && pezRollTarget.isAlive() ? pezRollTarget : findBossAttackTarget();
        if (target == null) {
            setPezRollTicks(0);
            setNoGravity(false);
            return;
        }
        pezRollTarget = target;

        if (elapsed < PEZ_ROLLING_TICKS) {
            rollPezThroughRoom(target, elapsed);
        } else if (elapsed < PEZ_ROLLING_TICKS + PEZ_ROLL_ATTACH_WAIT_TICKS) {
            stickPezToFace(target);
        } else if (elapsed < PEZ_ROLLING_TICKS + PEZ_ROLL_ATTACH_WAIT_TICKS + PEZ_ROLL_ATTACK_TICKS) {
            if (!pezRollAttackReleased) {
                releasePezRollAttack(target);
            }
            damagePezRollAttackTargets();
        } else {
            restPezAfterRoll();
        }

        if (ticks - 1 <= 0) {
            setNoGravity(false);
            pezRollAttackReleased = false;
            pezRollTarget = null;
            pezRollCooldown = 120;
        }
    }

    private void startPezRollSkill(LivingEntity target) {
        setPezRollTicks(PEZ_ROLL_TOTAL_TICKS);
        setPezAttachFace(Direction.UP);
        setPezRollDirection(Direction.NORTH);
        setPezRollSteps(0);
        pezRollDirectionTicks = 0;
        pezRollTargetBiasTicks = 0;
        pezRollTurnLockTicks = 0;
        pezRollTangent = Vec3.ZERO;
        pezRollSmoothedTangent = Vec3.ZERO;
        pezRollWaypoint = Vec3.ZERO;
        pezRollNextWaypoint = Vec3.ZERO;
        pezRollTarget = target;
        pezRollAttackReleased = false;
        pezRollCooldown = 240;
        bossSlamAttackActive = false;
        bossSlamDamageReady = false;
        getNavigation().stop();
        playSound(SoundEvents.SLIME_SQUISH, 1.4F, 0.62F);
    }

    public boolean debugStartPezRoll(LivingEntity target) {
        if (!isPezJelly() || level().isClientSide || target == null || !target.isAlive()) {
            return false;
        }
        setBossAwake(true);
        setTarget(target);
        startPezRollSkill(target);
        return true;
    }

    private void rollPezThroughRoom(LivingEntity target, int elapsed) {
        Direction face = getPezAttachFace();
        if (face == null || !hasAttachSurface(face)) {
            face = choosePezRollFace();
            setPezAttachFace(face);
            pezRollDirectionTicks = 0;
            pezRollTangent = Vec3.ZERO;
            pezRollSmoothedTangent = Vec3.ZERO;
            pezRollWaypoint = Vec3.ZERO;
            pezRollNextWaypoint = Vec3.ZERO;
        }

        updatePezRollWaypoints(face, target);
        Direction rollDirection = getPezRollDirection();
        if (pezRollDirectionTicks <= 0 || !isPezRollDirectionValid(face, rollDirection)) {
            rollDirection = choosePezRollDirection(face, target, true);
            applyPezRollDirection(rollDirection);
            pezRollDirectionTicks = 38 + random.nextInt(25);
        } else {
            pezRollDirectionTicks--;
        }

        setNoGravity(face != Direction.UP);
        Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());
        Vec3 targetTangent = Vec3.atLowerCornerOf(rollDirection.getNormal());
        double speed = pezRollMatchedSpeed();
        if (!canPezSlide(face, targetTangent)) {
            Direction climbFace = findPezClimbFace(face, rollDirection, speed);
            if (climbFace != null && climbFace != face) {
                face = climbFace;
                setPezAttachFace(face);
                pezRollWaypoint = Vec3.ZERO;
                pezRollNextWaypoint = Vec3.ZERO;
                updatePezRollWaypoints(face, target);
                rollDirection = choosePezRollDirection(face, target, true);
                applyPezRollDirection(rollDirection);
                targetTangent = Vec3.atLowerCornerOf(rollDirection.getNormal());
                normal = Vec3.atLowerCornerOf(face.getNormal());
                pezRollDirectionTicks = 38 + random.nextInt(25);
            }
        }
        if (!canPezSlide(face, targetTangent)) {
            Direction escape = choosePezOpenDirection(face, targetTangent);
            if (escape != null) {
                applyPezRollDirection(escape);
                targetTangent = Vec3.atLowerCornerOf(escape.getNormal());
                pezRollDirectionTicks = 30 + random.nextInt(21);
            } else {
                Direction newFace = choosePezRollFace();
                if (newFace != face && hasAttachSurface(newFace)) {
                    face = newFace;
                    setPezAttachFace(face);
                    pezRollWaypoint = Vec3.ZERO;
                    pezRollNextWaypoint = Vec3.ZERO;
                    updatePezRollWaypoints(face, target);
                    rollDirection = choosePezRollDirection(face, target, true);
                    applyPezRollDirection(rollDirection);
                    targetTangent = Vec3.atLowerCornerOf(rollDirection.getNormal());
                    normal = Vec3.atLowerCornerOf(face.getNormal());
                    pezRollDirectionTicks = 38 + random.nextInt(25);
                } else {
                    targetTangent = Vec3.ZERO;
                }
            }
        }

        Vec3 tangent = targetTangent.lengthSqr() > 1.0E-4D ? targetTangent.normalize() : Vec3.ZERO;
        if (tangent.lengthSqr() > 1.0E-4D) {
            if (pezRollSmoothedTangent.lengthSqr() < 1.0E-4D) {
                pezRollSmoothedTangent = tangent;
            } else {
                Vec3 blended = pezRollSmoothedTangent.scale(0.86D).add(tangent.scale(0.14D));
                pezRollSmoothedTangent = blended.lengthSqr() > 1.0E-4D ? blended.normalize() : tangent;
            }
        }
        Vec3 moveTangent = pezRollSmoothedTangent.lengthSqr() > 1.0E-4D ? pezRollSmoothedTangent.normalize() : tangent;
        Vec3 desired = moveTangent.scale(speed).add(normal.scale(-0.045D));
        if (!level().noCollision(this, getBoundingBox().move(desired.scale(0.65D)))) {
            spawnPezRollBreakParticles(normal);
            Direction escape = choosePezOpenDirection(face, tangent);
            if (escape != null) {
                applyPezRollDirection(escape);
                pezRollDirectionTicks = 24 + random.nextInt(18);
                Vec3 escapeTangent = Vec3.atLowerCornerOf(escape.getNormal());
                pezRollSmoothedTangent = escapeTangent;
                desired = escapeTangent.scale(speed * 0.9D).add(normal.scale(-0.04D));
            } else {
                desired = normal.scale(-0.04D);
                pezRollDirectionTicks = 8;
            }
        }
        Vec3 beforeRollPosition = position();
        if (face == Direction.UP && !onGround() && elapsed % 13 < 4) {
            desired = desired.add(0.0D, -0.12D, 0.0D);
        }
        move(MoverType.SELF, desired);
        setDeltaMovement(desired.scale(0.75D));
        hasImpulse = true;
        damagePezRollBrushTargets(tangent, beforeRollPosition);
    }

    private double pezRollMatchedSpeed() {
        double cycleTicks = PEZ_ROLL_ANIMATION_SECONDS * 20.0D;
        double circumference = Math.max(0.25D, getBbWidth()) * Math.PI;
        return circumference / cycleTicks;
    }

    private boolean canPezSlide(Direction face, Vec3 tangent) {
        if (tangent.lengthSqr() < 1.0E-4D || tangent.normalize().dot(Vec3.atLowerCornerOf(face.getNormal())) != 0.0D) {
            return false;
        }
        Vec3 step = tangent.normalize().scale(1.05D);
        return level().noCollision(this, getBoundingBox().move(step))
            && hasAttachSurfaceAt(face, position().add(step));
    }

    private boolean canPezSlideFrom(Direction face, Vec3 center, Vec3 tangent) {
        if (tangent.lengthSqr() < 1.0E-4D || tangent.normalize().dot(Vec3.atLowerCornerOf(face.getNormal())) != 0.0D) {
            return false;
        }
        Vec3 step = tangent.normalize().scale(1.05D);
        AABB box = getBoundingBox().move(center.subtract(position())).move(step);
        return level().noCollision(this, box)
            && hasAttachSurfaceAt(face, center.add(step));
    }

    private boolean isPezRollDirectionValid(Direction face, Direction direction) {
        return direction != null && direction.getAxis() != face.getAxis();
    }

    private Direction findPezClimbFace(Direction currentFace, Direction rollDirection, double speed) {
        if (rollDirection == null || !isPezRollDirectionValid(currentFace, rollDirection)) {
            return null;
        }
        Direction wallFace = rollDirection.getOpposite();
        Vec3 ahead = position().add(Vec3.atLowerCornerOf(rollDirection.getNormal()).scale(Math.max(0.4D, speed * 1.4D)));
        if (wallFace != currentFace && hasAttachSurfaceAt(wallFace, ahead)) {
            return wallFace;
        }
        if (currentFace != Direction.DOWN && hasAttachSurfaceAt(Direction.DOWN, ahead)
            && (rollDirection.getAxis() == Direction.Axis.X || rollDirection.getAxis() == Direction.Axis.Z)) {
            return Direction.DOWN;
        }
        if (currentFace != Direction.UP && !hasAttachSurfaceAt(wallFace, ahead) && hasAttachSurfaceAt(Direction.UP, ahead)) {
            return Direction.UP;
        }
        return null;
    }

    private void applyPezRollDirection(Direction direction) {
        setPezRollDirection(direction);
        pezRollTangent = Vec3.atLowerCornerOf(direction.getNormal());
        if (pezRollSmoothedTangent.lengthSqr() < 1.0E-4D || Math.abs(pezRollSmoothedTangent.normalize().dot(pezRollTangent.normalize())) < 0.05D) {
            pezRollSmoothedTangent = pezRollTangent;
        }
        setPezRollSteps(getPezRollSteps() + 1);
    }

    private Direction findPezAttachFace() {
        Direction[] preferred = {Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP};
        for (Direction direction : preferred) {
            if (hasAttachSurface(direction)) {
                return direction;
            }
        }
        return Direction.UP;
    }

    private Direction choosePezRollFace() {
        Direction current = getPezAttachFace();
        if (current != null && hasAttachSurface(current)) {
            return current;
        }
        Direction[] preferred = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP};
        for (Direction candidate : preferred) {
            if (hasAttachSurface(candidate)) {
                return candidate;
            }
        }
        return findPezAttachFace();
    }

    private Direction choosePezStrikeAttachFace() {
        Direction[] preferred = {Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP};
        Direction best = Direction.UP;
        double bestScore = -Double.MAX_VALUE;
        for (Direction candidate : preferred) {
            if (!hasAttachSurface(candidate)) {
                continue;
            }
            double score = random.nextDouble();
            if (candidate == Direction.DOWN) {
                score += 9.0D;
            } else if (candidate != Direction.UP) {
                score += 6.0D;
            }
            if (candidate == getPezAttachFace()) {
                score += 1.5D;
            }
            if (score > bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        return best;
    }

    private boolean hasAttachSurface(Direction face) {
        if (face == Direction.UP && onGround()) {
            return true;
        }
        return hasAttachSurfaceAt(face, position());
    }

    private boolean hasAttachSurfaceAt(Direction face, Vec3 center) {
        if (face == Direction.UP && onGround()) {
            return true;
        }
        Vec3 probe = Vec3.atLowerCornerOf(face.getOpposite().getNormal()).scale(0.08D);
        AABB box = getBoundingBox().move(center.subtract(position())).move(probe).deflate(0.05D);
        return !level().noCollision(this, box);
    }

    private void updatePezRollWaypoints(Direction face, LivingEntity target) {
        boolean reached = pezRollWaypoint.lengthSqr() < 1.0E-4D || position().distanceToSqr(pezRollWaypoint) < 9.0D;
        boolean tooClose = pezRollWaypoint.lengthSqr() > 1.0E-4D && position().distanceToSqr(pezRollWaypoint) < PEZ_ROLL_MIN_WAYPOINT_DISTANCE * PEZ_ROLL_MIN_WAYPOINT_DISTANCE;
        if (!reached && !tooClose && canPezRollTowardWaypoint(face, pezRollWaypoint)) {
            return;
        }
        Vec3 previous = pezRollWaypoint;
        if (pezRollNextWaypoint.lengthSqr() > 1.0E-4D
            && position().distanceToSqr(pezRollNextWaypoint) >= PEZ_ROLL_MIN_WAYPOINT_DISTANCE * PEZ_ROLL_MIN_WAYPOINT_DISTANCE
            && canPezRollTowardWaypoint(face, pezRollNextWaypoint)) {
            pezRollWaypoint = pezRollNextWaypoint;
        } else {
            pezRollWaypoint = choosePezDistantRollWaypoint(face, position(), previous, target);
        }
        pezRollNextWaypoint = choosePezDistantRollWaypoint(face, pezRollWaypoint, position(), target);
        pezRollDirectionTicks = 0;
    }

    private boolean canPezRollTowardWaypoint(Direction face, Vec3 waypoint) {
        Direction direction = directionTowardPezWaypoint(face, waypoint);
        return direction != null && canPezSlide(face, Vec3.atLowerCornerOf(direction.getNormal()));
    }

    private Vec3 choosePezDistantRollWaypoint(Direction face, Vec3 origin, @Nullable Vec3 avoid, LivingEntity target) {
        Vec3 best = Vec3.ZERO;
        double bestScore = -Double.MAX_VALUE;
        double minDistanceSqr = PEZ_ROLL_MIN_WAYPOINT_DISTANCE * PEZ_ROLL_MIN_WAYPOINT_DISTANCE;
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == face.getAxis()) {
                continue;
            }
            Vec3 tangent = Vec3.atLowerCornerOf(direction.getNormal());
            Vec3 cursor = origin;
            for (int step = 1; step <= (int)PEZ_ROLL_MAX_WAYPOINT_DISTANCE; step++) {
                if (!canPezSlideFrom(face, cursor, tangent)) {
                    break;
                }
                cursor = cursor.add(tangent);
                double distanceSqr = origin.distanceToSqr(cursor);
                if (distanceSqr < minDistanceSqr) {
                    continue;
                }
                double score = Math.sqrt(distanceSqr) * 3.0D + random.nextDouble() * 4.0D;
                if (avoid != null && avoid.lengthSqr() > 1.0E-4D) {
                    score += Math.sqrt(cursor.distanceToSqr(avoid)) * 0.35D;
                }
                if (target != null) {
                    score += Math.sqrt(cursor.distanceToSqr(target.position())) * 0.55D;
                }
                if (score > bestScore) {
                    best = cursor;
                    bestScore = score;
                }
            }
        }
        if (best.lengthSqr() > 1.0E-4D) {
            return best;
        }
        Direction fallback = choosePezOpenDirection(face, Vec3.ZERO);
        if (fallback == null) {
            for (Direction direction : Direction.values()) {
                if (direction.getAxis() != face.getAxis()) {
                    fallback = direction;
                    break;
                }
            }
        }
        Vec3 fallbackStep = fallback == null ? Vec3.ZERO : Vec3.atLowerCornerOf(fallback.getNormal()).scale(PEZ_ROLL_MIN_WAYPOINT_DISTANCE);
        return origin.add(fallbackStep);
    }

    private Direction directionTowardPezWaypoint(Direction face, Vec3 waypoint) {
        if (waypoint.lengthSqr() < 1.0E-4D) {
            return null;
        }
        Vec3 toWaypoint = waypoint.subtract(position());
        Direction best = null;
        double bestScore = 0.35D;
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == face.getAxis()) {
                continue;
            }
            Vec3 candidate = Vec3.atLowerCornerOf(direction.getNormal());
            double score = candidate.dot(toWaypoint);
            if (score > bestScore) {
                best = direction;
                bestScore = score;
            }
        }
        return best;
    }

    private Direction choosePezRollDirection(Direction face, LivingEntity target, boolean preferWaypoint) {
        Direction best = Direction.NORTH;
        double bestScore = -Double.MAX_VALUE;
        Vec3 toTarget = target.position().subtract(position());
        Vec3 toWaypoint = pezRollWaypoint.lengthSqr() > 1.0E-4D ? pezRollWaypoint.subtract(position()) : Vec3.ZERO;
        boolean forceTowardTarget = pezRollTargetBiasTicks > 0;
        boolean biasTowardTarget = forceTowardTarget || (!preferWaypoint && random.nextInt(7) == 0);
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == face.getAxis()) {
                continue;
            }
            Vec3 candidate = Vec3.atLowerCornerOf(direction.getNormal());
            double score = random.nextDouble() * 2.0D;
            if (preferWaypoint && toWaypoint.lengthSqr() > 1.0E-4D) {
                score += candidate.dot(toWaypoint.normalize()) * 30.0D;
            }
            if (canPezSlide(face, candidate)) {
                score += 6.0D;
            }
            if (direction == getPezRollDirection()) {
                score += 1.5D;
            }
            if (biasTowardTarget && toTarget.lengthSqr() > 1.0E-4D) {
                score += candidate.dot(toTarget.normalize()) * (forceTowardTarget ? 12.0D : 2.3D);
            }
            if (score > bestScore) {
                best = direction;
                bestScore = score;
            }
        }
        if (forceTowardTarget) {
            pezRollTargetBiasTicks--;
        }
        return best;
    }

    private Direction choosePezOpenDirection(Direction face, Vec3 previousTangent) {
        Direction best = null;
        double bestScore = -Double.MAX_VALUE;
        Vec3 previous = previousTangent.lengthSqr() > 1.0E-4D ? previousTangent.normalize() : Vec3.ZERO;
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == face.getAxis()) {
                continue;
            }
            Vec3 candidate = Vec3.atLowerCornerOf(direction.getNormal());
            if (!canPezSlide(face, candidate)) {
                continue;
            }
            double score = random.nextDouble();
            if (previous.lengthSqr() > 1.0E-4D) {
                score += candidate.dot(previous) * 0.7D;
            }
            if (score > bestScore) {
                best = direction;
                bestScore = score;
            }
        }
        return best;
    }

    private void stickPezToFace(LivingEntity target) {
        Direction face = getPezAttachFace();
        if (face == null || face == Direction.UP || !hasAttachSurface(face)) {
            face = choosePezStrikeAttachFace();
            setPezAttachFace(face);
        }
        setNoGravity(face != Direction.UP);
        Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());
        setDeltaMovement(normal.scale(-0.035D));
        hasImpulse = true;
        getLookControl().setLookAt(target);
    }

    private void releasePezRollAttack(LivingEntity target) {
        Direction face = getPezAttachFace();
        setNoGravity(false);
        Vec3 toTarget = target.getEyePosition().subtract(position());
        if (toTarget.lengthSqr() < 1.0E-4D) {
            toTarget = getLookAngle();
        }
        Vec3 faceKick = face == null ? Vec3.ZERO : Vec3.atLowerCornerOf(face.getNormal()).scale(0.38D);
        Vec3 dash = toTarget.normalize().scale(2.65D).add(faceKick);
        setDeltaMovement(dash);
        hasImpulse = true;
        pezRollAttackReleased = true;
        playSound(SoundEvents.SLIME_JUMP, 1.5F, 0.7F);
    }

    private void damagePezRollAttackTargets() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.9D), this::canBossTarget)) {
            if (target == this) {
                continue;
            }
            if (target.isBlocking()) {
                Vec3 away = position().subtract(target.position());
                target.push(-away.x * 0.08D, 0.12D, -away.z * 0.08D);
                target.hurtMarked = true;
                setPezRollTicks(Math.min(getPezRollTicks(), PEZ_ROLL_REST_TICKS));
                continue;
            }
            if (target.hurt(damageSources().mobAttack(this), getSize() * 2.35F)) {
                Vec3 velocity = getDeltaMovement();
                double horizontal = Math.max(0.1D, velocity.horizontalDistance());
                target.push(velocity.x / horizontal * 1.35D, 0.58D, velocity.z / horizontal * 1.35D);
                target.hurtMarked = true;
                setPezRollTicks(Math.min(getPezRollTicks(), PEZ_ROLL_REST_TICKS));
            }
        }
    }

    private void damagePezRollBrushTargets(Vec3 tangent, Vec3 previousPosition) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (pezRollBrushDamageCooldown > 0) {
            pezRollBrushDamageCooldown--;
            return;
        }
        AABB sweptBox = getBoundingBox().minmax(getBoundingBox().move(previousPosition.subtract(position()))).inflate(0.35D);
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, sweptBox, this::canBossTarget)) {
            if (target == this) {
                continue;
            }
            Vec3 contact = target.position().subtract(position());
            if (contact.lengthSqr() < 1.0E-4D) {
                contact = tangent;
            }
            spawnPezRollBreakParticles(contact.normalize());
            if (target.hurt(damageSources().mobAttack(this), getSize() * 0.9F)) {
                target.push(tangent.x * 0.65D, 0.22D, tangent.z * 0.65D);
                pezRollBrushDamageCooldown = 4;
            }
            target.hurtMarked = true;
        }
    }

    private void spawnPezRollBreakParticles(Vec3 normal) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 direction = normal.lengthSqr() > 1.0E-4D ? normal.normalize() : Vec3.ZERO;
        serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new net.minecraft.world.item.ItemStack(CCItems.PEZ_JELLY_BALL.get())),
            getX() + direction.x * getBbWidth() * 0.55D,
            getY() + getBbHeight() * 0.5D + direction.y * getBbHeight() * 0.45D,
            getZ() + direction.z * getBbWidth() * 0.55D,
            12, 0.12D, 0.12D, 0.12D, 0.08D);
    }

    private void restPezAfterRoll() {
        setNoGravity(false);
        setDeltaMovement(0.0D, Math.min(0.0D, getDeltaMovement().y), 0.0D);
        getNavigation().stop();
        if (level() instanceof ServerLevel serverLevel && tickCount % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.FALLING_WATER, getX(), getY() + getBbHeight() * 0.75D, getZ(),
                10, getBbWidth() * 0.35D, getBbHeight() * 0.25D, getBbWidth() * 0.35D, 0.04D);
        }
    }

    private static float smootherStep(float value) {
        float clamped = Mth.clamp(value, 0.0F, 1.0F);
        return clamped * clamped * clamped * (clamped * (clamped * 6.0F - 15.0F) + 10.0F);
    }

    private double bossSlamHorizontalPower() {
        if (isJellyQueen()) {
            return getJellyQueenMode() == JELLY_QUEEN_BLUE_MODE ? 0.82D : 0.58D;
        }
        if (isKingSlime()) {
            return 0.52D;
        }
        return 0.58D;
    }

    private int damageBossSlamTargets() {
        if (!bossSlamDamageReady || level().isClientSide) {
            return 0;
        }
        double radius = 0.6D * getSize();
        int hits = 0;
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius, 1.25D, radius), this::canBossTarget)) {
            if (target == this) {
                continue;
            }
            if (!hasLineOfSight(target) || distanceToSqr(target) >= radius * radius) {
                continue;
            }
            if (target.hurt(damageSources().mobAttack(this), bossSlamDamage())) {
                playSound(SoundEvents.SLIME_ATTACK, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
                hits++;
            }
        }
        if (hits > 0) {
            bossSlamDamageReady = false;
        }
        return hits;
    }

    private float bossSlamDamage() {
        int size = getSize();
        if (isJellyQueen()) {
            return size * 2.0F;
        }
        if (isKingSlime()) {
            return size * 2.5F;
        }
        return size;
    }

    private void trackBossSlamCombo(int hits) {
        if (isPezJelly()) {
            pezSlamCount++;
            if (pezSlamCount >= 10 && pezRollCooldown <= 0 && getPezRollTicks() == 0) {
                LivingEntity target = findBossAttackTarget();
                if (target != null) {
                    startPezRollSkill(target);
                }
                pezSlamCount = 0;
            }
            return;
        }
        if (!(isKingSlime() || isJellyQueen())) {
            return;
        }
        if (hits >= 2) {
            bossMultiTargetSlamCount++;
        }
        if (bossMultiTargetSlamCount >= 3 && getKingExpandTicks() == 0 && kingExpandCooldown <= 0) {
            startKingExpandAttack();
            bossMultiTargetSlamCount = 0;
        }
    }

    private void triggerBossSlamLandingEffects() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (isJellyQueen() && getJellyQueenMode() == JELLY_QUEEN_BROWN_MODE) {
            serverLevel.explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.NONE);
            serverLevel.explode(this, getX(), getY() + 2.0D, getZ(), 3.0F, Level.ExplosionInteraction.NONE);
        } else if (isKingSlime() && random.nextInt(5) == 0) {
            serverLevel.explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.NONE);
        }
    }

    private void performBossSlamSpecials(LivingEntity target) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (isPezJelly() && random.nextInt(5) == 0) {
            BasicCandySlimeEntity tornado = CCEntityTypes.TORNADO_JELLY.get().create(serverLevel);
            if (tornado != null) {
                tornado.moveTo(getX(), getY() + 0.5D, getZ(), random.nextFloat() * 360.0F, 0.0F);
                serverLevel.addFreshEntity(tornado);
            }
        } else if (isKingSlime()) {
            if (random.nextInt(3) == 0) {
                BasicCandySlimeEntity jelly = CCEntityTypes.YELLOW_JELLY.get().create(serverLevel);
                if (jelly != null) {
                    jelly.moveTo(getX(), getY() + 0.5D, getZ(), random.nextFloat() * 360.0F, 0.0F);
                    serverLevel.addFreshEntity(jelly);
                }
            }
            if (target instanceof Player player && random.nextInt(10) == 0) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 2), this);
            }
        }
    }

    private boolean hurtPlayerWithLegacyBossContact(Player player) {
        int size = getSize();
        double range = 0.6D * size;
        if (!hasLineOfSight(player) || distanceToSqr(player) >= range * range) {
            return false;
        }
        float damage = isJellyQueen() ? size * 2.0F : isKingSlime() ? size * 2.5F : size;
        if (player.hurt(damageSources().mobAttack(this), damage)) {
            playSound(SoundEvents.SLIME_ATTACK, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
            return true;
        }
        return false;
    }

    private void tickRetaliationTarget() {
        if (level().isClientSide) {
            return;
        }
        LivingEntity target = getTarget();
        if (target == null) {
            return;
        }
        if (!canAttackTarget(target)) {
            setTarget(null);
            return;
        }
        if (getBoundingBox().inflate(0.15D).intersects(target.getBoundingBox())) {
            doHurtTarget(target);
        }
    }

    private boolean hurtLegacyBossTarget(Entity target) {
        int size = getSize();
        double range = 0.6D * size;
        if (!canAttackTarget(target) || !hasLineOfSight(target) || distanceToSqr(target) >= range * range) {
            return false;
        }
        float damage = isJellyQueen() ? size * 2.0F : isKingSlime() ? size * 2.5F : size;
        return hurtCandyTarget(target, damage);
    }

    private boolean hurtCandyTarget(Entity target, float damage) {
        if (!canAttackTarget(target)) {
            return false;
        }
        if (target.hurt(damageSources().mobAttack(this), damage)) {
            playSound(SoundEvents.SLIME_ATTACK, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
            return true;
        }
        return false;
    }

    private void setRetaliationTarget(LivingEntity attacker) {
        if (canRetaliateAgainst(attacker)) {
            setTarget(attacker);
            if (isCandyBoss()) {
                setBossAwake(true);
            }
        }
    }

    private void activateBossFromDamage(LivingEntity attacker) {
        if (canBossWakeFrom(attacker)) {
            bossLostTargetTicks = BOSS_LOST_TARGET_TICKS;
            bossRetaliationTarget = attacker;
            setBossAwake(true);
            updateJellyQueenMode();
            if (CandyTargeting.canAttackEntity(attacker)) {
                setTarget(attacker);
                getNavigation().stop();
                if (getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                    getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(isJellyQueen() ? 0.7D : isKingSlime() ? 0.45D : 0.38D);
                }
            }
        }
    }

    private boolean canAttackTarget(Entity target) {
        return isCandyBoss() ? canBossTarget(target) : canRetaliateAgainst(target);
    }

    private boolean canBossTarget(Entity target) {
        return target instanceof LivingEntity
            && target.isAlive()
            && (!(target instanceof BasicCandySlimeEntity slimeTarget) || isBossRetaliationTarget(slimeTarget) || isForcedJellyConflict(this, slimeTarget))
            && CandyTargeting.canAttackEntity(target);
    }

    private boolean isBossRetaliationTarget(Entity target) {
        return bossRetaliationTarget != null && bossRetaliationTarget == target;
    }

    private boolean canBossWakeFrom(Entity source) {
        return source instanceof LivingEntity
            && source.isAlive()
            && (!(source instanceof BasicCandySlimeEntity slimeSource) || isCandyBoss() || isForcedJellyConflict(slimeSource, this));
    }

    private boolean canRetaliateAgainst(Entity target) {
        return target instanceof LivingEntity
            && target.isAlive()
            && (!(target instanceof BasicCandySlimeEntity slimeTarget) || isForcedJellyConflict(this, slimeTarget))
            && CandyTargeting.canAttackEntity(target);
    }

    private static boolean isForcedJellyConflict(BasicCandySlimeEntity attacker, BasicCandySlimeEntity victim) {
        return attacker.getTarget() == victim || victim.getTarget() == attacker;
    }

    private boolean shouldDormantBossWakeFromMobAttack(LivingEntity attacker) {
        return isCandyBoss()
            && !isBossAwake()
            && !(attacker instanceof Player)
            && !isPlayerAlliedEntity(attacker)
            && canBossWakeFrom(attacker)
            && CandyTargeting.canAttackEntity(attacker);
    }

    private static boolean isPlayerAlliedEntity(Entity entity) {
        if (entity instanceof TamableAnimal tamable && tamable.isTame() && tamable.getOwner() instanceof Player) {
            return true;
        }
        return false;
    }

    private LivingEntity findBossAttackTarget() {
        LivingEntity target = getTarget();
        if (target != null) {
            if (!canBossTarget(target)) {
                setTarget(null);
                return null;
            }
            double range = isPezJelly() ? 96.0D : BOSS_TARGET_RANGE;
            if (distanceToSqr(target) <= range * range) {
                return target;
            }
        }
        LivingEntity retaliationTarget = bossRetaliationTarget;
        if (retaliationTarget != null) {
            if (!canBossTarget(retaliationTarget)) {
                bossRetaliationTarget = null;
            } else {
                double range = isPezJelly() ? 96.0D : BOSS_TARGET_RANGE;
                if (distanceToSqr(retaliationTarget) <= range * range) {
                    setTarget(retaliationTarget);
                    return retaliationTarget;
                }
            }
        }
        if (isPezJelly() && level() instanceof ServerLevel serverLevel) {
            if (bossTargetSearchCooldown > 0) {
                return null;
            }
            bossTargetSearchCooldown = 10;
            LivingEntity nearest = serverLevel.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(96.0D), this::canBossTarget)
                .stream()
                .filter(entity -> entity != this)
                .min((a, b) -> Double.compare(distanceToSqr(a), distanceToSqr(b)))
                .orElse(null);
            if (nearest != null) {
                bossTargetSearchCooldown = 4;
                setTarget(nearest);
                return nearest;
            }
        }
        return null;
    }

    private void tickBossLostTarget(@Nullable AttributeInstance speed) {
        if (speed != null) {
            speed.setBaseValue(0.0D);
        }
        setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
        getNavigation().stop();
        setJumping(false);
        if (tickCount % 20 == 0) {
            heal(5.0F);
        }
        if (!isBossAwake()) {
            if (isJellyQueen()) {
                setJellyQueenMode(JELLY_QUEEN_SLEEP_MODE);
            }
            return;
        }
        if (bossLostTargetTicks > 0) {
            bossLostTargetTicks--;
            updateJellyQueenMode();
            return;
        }
        putBossToSleep();
        setBossSlamTicks(0);
        setKingExpandTicks(0);
        setKingDashTicks(0);
        if (isJellyQueen()) {
            setJellyQueenMode(JELLY_QUEEN_SLEEP_MODE);
        }
    }

    private void updateJellyQueenMode() {
        if (!isBossAwake()) {
            setJellyQueenMode(JELLY_QUEEN_SLEEP_MODE);
        } else if (getHealth() <= getMaxHealth() * 0.25F) {
            setJellyQueenMode(JELLY_QUEEN_BROWN_MODE);
        } else if (getHealth() <= getMaxHealth() * 0.5F) {
            setJellyQueenMode(JELLY_QUEEN_BLUE_MODE);
        } else {
            setJellyQueenMode(JELLY_QUEEN_PINK_MODE);
        }
    }

    private int nextJellyQueenJumpDelay() {
        return getJellyQueenMode() == JELLY_QUEEN_BLUE_MODE ? random.nextInt(10) + 5 : random.nextInt(40) + 5;
    }

    private void setJellyQueenMode(int mode) {
        entityData.set(JELLY_QUEEN_MODE, Mth.clamp(mode, JELLY_QUEEN_SLEEP_MODE, JELLY_QUEEN_BROWN_MODE));
    }

    private void setBossSlamTicks(int ticks) {
        entityData.set(BOSS_SLAM_TICKS, Mth.clamp(ticks, 0, BOSS_SLAM_POSE_TICKS));
    }

    private void setKingExpandTicks(int ticks) {
        entityData.set(KING_EXPAND_TICKS, Mth.clamp(ticks, 0, KING_EXPAND_POSE_TICKS));
    }

    private void setKingDashTicks(int ticks) {
        entityData.set(KING_DASH_TICKS, Mth.clamp(ticks, 0, KING_DASH_POSE_TICKS));
    }

    private void setPezRollTicks(int ticks) {
        entityData.set(PEZ_ROLL_TICKS, Mth.clamp(ticks, 0, PEZ_ROLL_TOTAL_TICKS));
    }

    private void setPezAttachFace(Direction face) {
        entityData.set(PEZ_ATTACH_FACE, face.ordinal());
    }

    private void setPezRollDirection(Direction direction) {
        entityData.set(PEZ_ROLL_DIRECTION, direction.ordinal());
    }

    private void setPezRollSteps(int steps) {
        entityData.set(PEZ_ROLL_STEPS, Math.max(0, steps));
    }

    private void setBossBounceTicks(int ticks) {
        entityData.set(BOSS_BOUNCE_TICKS, Mth.clamp(ticks, 0, BOSS_BOUNCE_POSE_TICKS));
    }

    private void setBossRestingTicks(int ticks) {
        entityData.set(BOSS_RESTING_TICKS, Mth.clamp(ticks, 0, BOSS_REST_TICKS));
    }

    public int getJellyQueenMode() {
        return entityData.get(JELLY_QUEEN_MODE);
    }

    public int getBossSlamTicks() {
        return entityData.get(BOSS_SLAM_TICKS);
    }

    public int getKingExpandTicks() {
        return entityData.get(KING_EXPAND_TICKS);
    }

    public int getKingDashTicks() {
        return entityData.get(KING_DASH_TICKS);
    }

    public int getPezRollTicks() {
        return entityData.get(PEZ_ROLL_TICKS);
    }

    public Direction getPezAttachFace() {
        int ordinal = Mth.clamp(entityData.get(PEZ_ATTACH_FACE), 0, Direction.values().length - 1);
        return Direction.values()[ordinal];
    }

    public Direction getPezRollDirection() {
        int ordinal = Mth.clamp(entityData.get(PEZ_ROLL_DIRECTION), 0, Direction.values().length - 1);
        return Direction.values()[ordinal];
    }

    public int getPezRollSteps() {
        return entityData.get(PEZ_ROLL_STEPS);
    }

    public int getBossBounceTicks() {
        return entityData.get(BOSS_BOUNCE_TICKS);
    }

    public int getBossRestingTicks() {
        return entityData.get(BOSS_RESTING_TICKS);
    }

    private ParticleOptions jellyLandingParticle() {
        return new ItemParticleOption(ParticleTypes.ITEM, new net.minecraft.world.item.ItemStack(jellyLandingParticleItem()));
    }

    private net.minecraft.world.item.Item jellyLandingParticleItem() {
        if (isYellowJelly()) {
            return CCItems.LEMON_JELLY_BALL.get();
        }
        if (isRedJelly()) {
            return CCItems.RASPBERRY_JELLY_BALL.get();
        }
        if (isTornadoJelly()) {
            return CCItems.MINT_JELLY_BALL.get();
        }
        if (isPezJelly()) {
            return CCItems.PEZ_JELLY_BALL.get();
        }
        if (isKingSlime()) {
            return CCItems.CARAMEL_KING_JELLY_BALL.get();
        }
        if (isJellyQueen()) {
            return CCItems.STRAWBERRY_QUEEN_JELLY_BALL.get();
        }
        return CCItems.GUMMY_BALL.get();
    }

    public float getJellyQueenSlamProgress(float partialTicks) {
        return getBossSlamProgress(partialTicks);
    }

    public float getBossSlamProgress(float partialTicks) {
        if (!isCandyBoss()) {
            return 0.0F;
        }
        float ticks = Math.max(0.0F, getBossSlamTicks() - partialTicks);
        return Mth.clamp(ticks / (float) BOSS_SLAM_POSE_TICKS, 0.0F, 1.0F);
    }

    public float getKingExpandProgress(float partialTicks) {
        if (!(isPezJelly() || isKingSlime() || isJellyQueen())) {
            return 0.0F;
        }
        float ticks = Math.max(0.0F, getKingExpandTicks() - partialTicks);
        return Mth.clamp(ticks / (float) KING_EXPAND_POSE_TICKS, 0.0F, 1.0F);
    }

    public float getKingDashProgress(float partialTicks) {
        if (!(isKingSlime() || isJellyQueen())) {
            return 0.0F;
        }
        float ticks = Math.max(0.0F, getKingDashTicks() - partialTicks);
        return Mth.clamp(ticks / (float) KING_DASH_POSE_TICKS, 0.0F, 1.0F);
    }

    public float getKingDashChargeProgress(float partialTicks) {
        float dashProgress = getKingDashProgress(partialTicks);
        if (dashProgress <= 0.0F) {
            return 0.0F;
        }
        float elapsed = (1.0F - dashProgress) * KING_DASH_POSE_TICKS;
        return Mth.clamp(elapsed / (float) KING_DASH_CHARGE_TICKS, 0.0F, 1.0F);
    }

    public float getPezRollProgress(float partialTicks) {
        if (!isPezJelly()) {
            return 0.0F;
        }
        float ticks = Math.max(0.0F, getPezRollTicks() - partialTicks);
        return Mth.clamp(ticks / (float) PEZ_ROLL_TOTAL_TICKS, 0.0F, 1.0F);
    }

    public float getPezRollElapsed(float partialTicks) {
        if (!isPezJelly()) {
            return 0.0F;
        }
        return Mth.clamp(1.0F - getPezRollProgress(partialTicks), 0.0F, 1.0F);
    }

    public float getBossBounceProgress(float partialTicks) {
        if (!(isKingSlime() || isJellyQueen())) {
            return 0.0F;
        }
        float ticks = Math.max(0.0F, getBossBounceTicks() - partialTicks);
        return Mth.clamp(ticks / (float) BOSS_BOUNCE_POSE_TICKS, 0.0F, 1.0F);
    }

    public float getBossRestingProgress(float partialTicks) {
        if (!isCandyBoss()) {
            return 0.0F;
        }
        float ticks = Math.max(0.0F, getBossRestingTicks() - partialTicks);
        return Mth.clamp(ticks / (float) BOSS_REST_TICKS, 0.0F, 1.0F);
    }

    public boolean isBossResting() {
        return getBossRestingTicks() > 0;
    }

    public boolean isBossAwake() {
        return entityData.get(BOSS_AWAKE);
    }

    private void setBossAwake(boolean awake) {
        if (isBossAwake() != awake) {
            dormantRotationInitialized = false;
        }
        entityData.set(BOSS_AWAKE, awake);
        if (!level().isClientSide) {
            bossEvent.setVisible(awake);
        }
        if (isJellyQueen()) {
            setJellyQueenMode(awake ? getJellyQueenMode() == JELLY_QUEEN_SLEEP_MODE ? JELLY_QUEEN_PINK_MODE : getJellyQueenMode() : JELLY_QUEEN_SLEEP_MODE);
        }
    }

    public void prepareDungeonBossSpawn() {
        applyLegacySpawnSize();
        putBossToSleep();
        bossJumpCooldown = 0;
        bossLostTargetTicks = 0;
        bossRangedCooldown = 0;
        kingExpandCooldown = 0;
        kingDashCooldown = 0;
        kingDashChargeTicks = 0;
        kingDashTarget = null;
        kingExpandDamageReady = false;
        bossMultiTargetSlamCount = 0;
        pezSlamCount = 0;
        pezRollCooldown = 0;
        pezRollTurnLockTicks = 0;
        pezRollTarget = null;
        pezRollAttackReleased = false;
        bossSlamAttackActive = false;
        bossSlamDamageReady = false;
        setBossSlamTicks(0);
        setKingExpandTicks(0);
        setKingDashTicks(0);
        setPezRollTicks(0);
        setPezAttachFace(Direction.UP);
        setPezRollSteps(0);
        setBossBounceTicks(0);
        setBossRestingTicks(0);
        setDeltaMovement(0.0D, 0.0D, 0.0D);
        if (isJellyQueen()) {
            setJellyQueenMode(JELLY_QUEEN_SLEEP_MODE);
        }
    }

    private void putBossToSleep() {
        setBossAwake(false);
        bossRetaliationTarget = null;
        bossLostTargetTicks = 0;
        bossSlamAttackActive = false;
        bossSlamDamageReady = false;
        kingDashTarget = null;
        kingDashChargeTicks = 0;
        pezRollTarget = null;
        pezRollAttackReleased = false;
        pezRollTurnLockTicks = 0;
        pezRollBrushDamageCooldown = 0;
        setPezRollTicks(0);
        setPezRollSteps(0);
        setBossBounceTicks(0);
        setBossRestingTicks(0);
        setNoGravity(false);
        setTarget(null);
    }

    private void shrinkKingSlimeFromHealth() {
        double percent = (double) (getHealth() / getMaxHealth()) * 12.0D;
        int targetSize = Math.max(1, (int) percent + 1);
        if (getSize() > targetSize) {
            setSize(targetSize, false);
        }
    }

    private void splitPezJellyOnDeath() {
        if (pezDeathSplitSpawned || !isPezJelly() || getHealth() > 0.0F || !(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        pezDeathSplitSpawned = true;
        serverLevel.explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.NONE);
        BasicCandySlimeEntity slime = CCEntityTypes.PEZ_JELLY.get().create(serverLevel);
        if (slime != null) {
            slime.setSize(getSize() - 1, true);
            slime.setBossAwake(false);
            slime.moveTo(getX(), getY() + 0.5D, getZ(), random.nextFloat() * 360.0F, 0.0F);
            serverLevel.addFreshEntity(slime);
        }
    }

    private void knockbackAttackingPlayer(Player player) {
        double dx = getX() - player.getX();
        double dz = getZ() - player.getZ();
        while (dx * dx + dz * dz < 1.0E-4D) {
            dx = (random.nextDouble() - random.nextDouble()) * 0.01D;
            dz = (random.nextDouble() - random.nextDouble()) * 0.01D;
        }
        player.knockback(2.0D, dx, dz);
    }

    private void freezeSleepingBoss() {
        if (!dormantRotationInitialized) {
            dormantYRot = getYRot();
            dormantYHeadRot = getYHeadRot();
            dormantRotationInitialized = true;
        }
        setTarget(null);
        getNavigation().stop();
        setJumping(false);
        setYRot(dormantYRot);
        yRotO = dormantYRot;
        yBodyRot = dormantYRot;
        yBodyRotO = dormantYRot;
        yHeadRot = dormantYHeadRot;
        yHeadRotO = dormantYHeadRot;
        setDeltaMovement(0.0D, Math.min(0.0D, getDeltaMovement().y), 0.0D);
        AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            speed.setBaseValue(0.0D);
        }
    }

    private void reflectProjectile(DamageSource source) {
        if (level().isClientSide || !(source.getDirectEntity() instanceof Projectile projectile)) {
            return;
        }
        Vec3 direction = projectile.getDeltaMovement().scale(-1.0D);
        Entity attacker = source.getEntity();
        if (attacker != null) {
            direction = attacker.getEyePosition().subtract(projectile.position());
        }
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = projectile.position().subtract(position());
        }
        if (direction.lengthSqr() < 1.0E-4D) {
            direction = new Vec3(0.0D, 0.15D, 1.0D);
        }
        double speed = Math.max(1.2D, projectile.getDeltaMovement().length());
        Vec3 reflected = direction.normalize().scale(speed * 1.25D);
        projectile.setDeltaMovement(reflected);
        projectile.setYRot((float)(Mth.atan2(reflected.x, reflected.z) * (180.0D / Math.PI)));
        projectile.setXRot((float)(Mth.atan2(reflected.y, reflected.horizontalDistance()) * (180.0D / Math.PI)));
        projectile.hasImpulse = true;
        if (projectile instanceof AbstractArrow arrow) {
            arrow.setOwner(this);
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        }
    }

    private void updateBossBar() {
        if (!isCandyBoss() || level().isClientSide) {
            return;
        }
        bossEvent.setName(getType().getDescription());
        bossEvent.setColor(getBossBarColor());
        bossEvent.setProgress(Math.max(0.0F, Math.min(1.0F, getHealth() / getMaxHealth())));
        bossEvent.setVisible(isBossAwake());
    }

    private BossEvent.BossBarColor getBossBarColor() {
        if (isKingSlime()) {
            return BossEvent.BossBarColor.YELLOW;
        }
        if (isJellyQueen()) {
            return switch (getJellyQueenMode()) {
                case JELLY_QUEEN_BLUE_MODE -> BossEvent.BossBarColor.BLUE;
                case JELLY_QUEEN_BROWN_MODE -> BossEvent.BossBarColor.YELLOW;
                default -> BossEvent.BossBarColor.PINK;
            };
        }
        return BossEvent.BossBarColor.PURPLE;
    }

    public boolean isYellowJelly() {
        return getType() == CCEntityTypes.YELLOW_JELLY.get();
    }

    public boolean isRedJelly() {
        return getType() == CCEntityTypes.RED_JELLY.get();
    }

    public boolean isTornadoJelly() {
        return getType() == CCEntityTypes.TORNADO_JELLY.get();
    }

    public boolean isPezJelly() {
        return getType() == CCEntityTypes.PEZ_JELLY.get();
    }

    public boolean isKingSlime() {
        return getType() == CCEntityTypes.KING_SLIME.get();
    }

    public boolean isJellyQueen() {
        return getType() == CCEntityTypes.JELLY_QUEEN.get();
    }

    private boolean isCandyBoss() {
        return isPezJelly() || isKingSlime() || isJellyQueen();
    }

    private boolean isInGrenadine() {
        FluidState feet = level().getFluidState(blockPosition());
        FluidState eye = level().getFluidState(BlockPos.containing(getX(), getEyeY(), getZ()));
        return isGrenadine(feet) || isGrenadine(eye);
    }

    private static boolean isGrenadine(FluidState state) {
        return state.is(CCFluids.SOURCE_GRENADINE.get()) || state.is(CCFluids.FLOWING_GRENADINE.get());
    }

    private static boolean isSurvivalLike(Player player) {
        return CandyTargeting.canAttackPlayer(player);
    }

    private void applyLegacySpawnSize() {
        if (isRedJelly()) {
            setSize(2, true);
        } else if (isYellowJelly() || isTornadoJelly()) {
            setSize(1, true);
        } else if (isPezJelly()) {
            setSize(10, true);
        } else if (isKingSlime()) {
            setSize(13, true);
        } else if (isJellyQueen()) {
            setSize(6, true);
        }
    }
}
