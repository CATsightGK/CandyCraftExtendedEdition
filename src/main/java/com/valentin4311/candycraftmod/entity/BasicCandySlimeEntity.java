package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCSoundEvents;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
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
    private static final int JELLY_QUEEN_SLAM_POSE_TICKS = 30;
    private static final int BOSS_LOST_TARGET_TICKS = 200;
    private static final double BOSS_TARGET_RANGE = 64.0D;
    private static final EntityDataAccessor<Boolean> BOSS_AWAKE = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> JELLY_QUEEN_MODE = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> JELLY_QUEEN_SLAM_TICKS = SynchedEntityData.defineId(BasicCandySlimeEntity.class, EntityDataSerializers.INT);
    private int specialAttackCooldown;
    private boolean dormantRotationInitialized;
    private float dormantYRot;
    private float dormantYHeadRot;
    private int bossJumpCooldown;
    private int bossLostTargetTicks;
    private boolean jellyQueenWasOnGround = true;
    private boolean jellyQueenSlamDamageReady;
    private boolean pezDeathSplitSpawned;
    private final ServerBossEvent bossEvent = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    public BasicCandySlimeEntity(EntityType<? extends BasicCandySlimeEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(BOSS_AWAKE, false);
        entityData.define(JELLY_QUEEN_MODE, JELLY_QUEEN_SLEEP_MODE);
        entityData.define(JELLY_QUEEN_SLAM_TICKS, 0);
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
        if (isTornadoJelly() && !onGround() && level().isClientSide && tickCount % 4 == 0) {
            level().addParticle(ParticleTypes.CLOUD, getRandomX(0.8D), getRandomY(), getRandomZ(0.8D), 0.0D, 0.02D, 0.0D);
        }
        tickJellyQueenLandingAndAnimation();
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
        if (!level().isClientSide && isCandyBoss()) {
            performBossJumpAttack();
        }
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
        if (source.getEntity() instanceof BasicCandySlimeEntity) {
            return false;
        }
        if (!level().isClientSide && source.getEntity() instanceof LivingEntity attacker) {
            if (isCandyBoss()) {
                activateBossFromDamage(attacker);
            } else {
                setRetaliationTarget(attacker);
            }
        }
        if (isCandyBoss() && source.is(DamageTypeTags.IS_FALL)) {
            return false;
        }
        if (isCandyBoss() && source.is(DamageTypeTags.IS_PROJECTILE)) {
            reflectProjectile(source);
            return false;
        }
        if (isJellyQueen()) {
            if (!level().isClientSide && source.getEntity() != null) {
                if (!isBossAwake()) {
                    setDeltaMovement(getDeltaMovement().x, 2.0D, getDeltaMovement().z);
                }
                setBossAwake(true);
                updateJellyQueenMode();
                if (source.getEntity() instanceof Player player && amount > 1.0F && !player.getAbilities().instabuild) {
                    knockbackAttackingPlayer(player);
                }
            }
            return super.hurt(source, amount);
        }
        if (isKingSlime() && !level().isClientSide && getSize() > 1) {
            shrinkKingSlimeFromHealth();
        }
        if (isCandyBoss() && !level().isClientSide && source.getEntity() != null) {
            setBossAwake(true);
            setDeltaMovement(getDeltaMovement().add(0.0D, 0.9D, 0.0D));
            if (source.getEntity() instanceof Player player && amount > 1.0F && !player.getAbilities().instabuild) {
                knockbackAttackingPlayer(player);
            }
        }
        return super.hurt(source, amount);
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
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("BossAwake", isBossAwake());
        tag.putInt("BossLostTargetTicks", bossLostTargetTicks);
        if (isJellyQueen()) {
            tag.putInt("JellyQueenMode", getJellyQueenMode());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setBossAwake(tag.getBoolean("BossAwake"));
        bossLostTargetTicks = tag.getInt("BossLostTargetTicks");
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
        bossLostTargetTicks = BOSS_LOST_TARGET_TICKS;
        if (isBossAwake() && speed != null) {
            speed.setBaseValue(isJellyQueen() ? 0.7D : isKingSlime() ? 0.45D : 0.38D);
        }
        if (bossJumpCooldown > 0) {
            bossJumpCooldown--;
        }
        if (isBossAwake() && onGround() && bossJumpCooldown <= 0) {
            bossJumpCooldown = isJellyQueen() ? 20 + random.nextInt(25) : 15 + random.nextInt(30);
            getLookControl().setLookAt(target);
            setDeltaMovement(getDeltaMovement().add(
                (target.getX() - getX()) * 0.04D,
                isKingSlime() ? 0.9D : 0.7D,
                (target.getZ() - getZ()) * 0.04D));
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
        updateJellyQueenMode();
        if (bossJumpCooldown > 0) {
            bossJumpCooldown--;
        }
        if (onGround() && bossJumpCooldown <= 0) {
            bossJumpCooldown = Math.max(2, nextJellyQueenJumpDelay() / 3);
            launchJellyQueenAt(target);
        }
    }

    private void tickJellyQueenLandingAndAnimation() {
        if (!isJellyQueen() || level().isClientSide) {
            return;
        }
        int slamTicks = getJellyQueenSlamTicks();
        if (slamTicks > 0) {
            setJellyQueenSlamTicks(slamTicks - 1);
        }
        boolean grounded = onGround();
        if (isBossAwake() && !jellyQueenWasOnGround && grounded) {
            setJellyQueenSlamTicks(JELLY_QUEEN_SLAM_POSE_TICKS);
            damageJellyQueenSlamTargets();
            jellyQueenSlamDamageReady = false;
        } else if (isBossAwake() && !grounded && getDeltaMovement().y < -0.05D) {
            setJellyQueenSlamTicks(Math.max(getJellyQueenSlamTicks(), JELLY_QUEEN_SLAM_POSE_TICKS));
            damageJellyQueenSlamTargets();
        }
        jellyQueenWasOnGround = grounded;
    }

    private void launchJellyQueenAt(LivingEntity target) {
        getLookControl().setLookAt(target);
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double distance = Math.max(0.1D, Math.sqrt(dx * dx + dz * dz));
        int mode = getJellyQueenMode();
        double horizontalPower = mode == JELLY_QUEEN_BLUE_MODE ? 0.82D : 0.58D;
        double yPower = isInWater() ? 4.0D : 1.5D;
        setJumping(true);
        setDeltaMovement(dx / distance * horizontalPower, yPower, dz / distance * horizontalPower);
        hasImpulse = true;
        jellyQueenSlamDamageReady = true;
        setJellyQueenSlamTicks(JELLY_QUEEN_SLAM_POSE_TICKS - 6);
        if (mode == JELLY_QUEEN_BROWN_MODE && level() instanceof ServerLevel serverLevel) {
            serverLevel.explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.NONE);
            serverLevel.explode(this, getX(), getY() + 2.0D, getZ(), 3.0F, Level.ExplosionInteraction.NONE);
        }
        playSound(getJumpSound(), getSoundVolume(), ((random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
    }

    private void damageJellyQueenSlamTargets() {
        if (!jellyQueenSlamDamageReady || level().isClientSide) {
            return;
        }
        double radius = 0.6D * getSize();
        boolean hitAny = false;
        for (Player player : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(radius, 1.25D, radius), BasicCandySlimeEntity::isSurvivalLike)) {
            if (hurtPlayerWithLegacyBossContact(player)) {
                hitAny = true;
            }
        }
        LivingEntity target = getTarget();
        if (target != null && !(target instanceof Player) && hurtLegacyBossTarget(target)) {
            hitAny = true;
        }
        if (hitAny) {
            jellyQueenSlamDamageReady = false;
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
        if (canBossTarget(attacker)) {
            setTarget(attacker);
            bossLostTargetTicks = BOSS_LOST_TARGET_TICKS;
            setBossAwake(true);
            updateJellyQueenMode();
        }
    }

    private boolean canAttackTarget(Entity target) {
        return isCandyBoss() ? canBossTarget(target) : canRetaliateAgainst(target);
    }

    private boolean canBossTarget(Entity target) {
        return target instanceof LivingEntity
            && target.isAlive()
            && !(target instanceof BasicCandySlimeEntity);
    }

    private boolean canRetaliateAgainst(Entity target) {
        return target instanceof LivingEntity
            && target.isAlive()
            && !(target instanceof BasicCandySlimeEntity)
            && CandyTargeting.canAttackEntity(target);
    }

    private LivingEntity findBossAttackTarget() {
        LivingEntity target = getTarget();
        if (target != null) {
            if (!canBossTarget(target)) {
                setTarget(null);
                return null;
            }
            if (distanceToSqr(target) <= BOSS_TARGET_RANGE * BOSS_TARGET_RANGE) {
                return target;
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
        setJellyQueenSlamTicks(0);
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

    private void setJellyQueenSlamTicks(int ticks) {
        entityData.set(JELLY_QUEEN_SLAM_TICKS, Mth.clamp(ticks, 0, JELLY_QUEEN_SLAM_POSE_TICKS));
    }

    public int getJellyQueenMode() {
        return entityData.get(JELLY_QUEEN_MODE);
    }

    public int getJellyQueenSlamTicks() {
        return entityData.get(JELLY_QUEEN_SLAM_TICKS);
    }

    private ParticleOptions jellyLandingParticle() {
        return new ItemParticleOption(ParticleTypes.ITEM, jellyLandingParticleStack());
    }

    private ItemStack jellyLandingParticleStack() {
        ItemStack stack = new ItemStack(CCItems.GUMMY_BALL.get());
        int variant = jellyLandingParticleVariant();
        if (variant > 0) {
            stack.getOrCreateTag().putInt("CustomModelData", variant);
        }
        return stack;
    }

    private int jellyLandingParticleVariant() {
        if (isRedJelly()) {
            return 1;
        }
        if (isTornadoJelly() || isKingSlime()) {
            return 2;
        }
        if (isJellyQueen()) {
            return switch (getJellyQueenMode()) {
                case JELLY_QUEEN_BLUE_MODE, JELLY_QUEEN_BROWN_MODE -> 2;
                default -> 0;
            };
        }
        return 0;
    }

    public float getJellyQueenSlamProgress(float partialTicks) {
        if (!isJellyQueen()) {
            return 0.0F;
        }
        float ticks = Math.max(0.0F, getJellyQueenSlamTicks() - partialTicks);
        return Mth.clamp(ticks / (float) JELLY_QUEEN_SLAM_POSE_TICKS, 0.0F, 1.0F);
    }

    public boolean isBossAwake() {
        return entityData.get(BOSS_AWAKE);
    }

    private void setBossAwake(boolean awake) {
        if (isBossAwake() != awake) {
            dormantRotationInitialized = false;
        }
        entityData.set(BOSS_AWAKE, awake);
        if (isJellyQueen()) {
            setJellyQueenMode(awake ? getJellyQueenMode() == JELLY_QUEEN_SLEEP_MODE ? JELLY_QUEEN_PINK_MODE : getJellyQueenMode() : JELLY_QUEEN_SLEEP_MODE);
        }
    }

    public void prepareDungeonBossSpawn() {
        applyLegacySpawnSize();
        putBossToSleep();
        bossJumpCooldown = 0;
        bossLostTargetTicks = 0;
        setDeltaMovement(0.0D, 0.0D, 0.0D);
        if (isJellyQueen()) {
            setJellyQueenMode(JELLY_QUEEN_SLEEP_MODE);
            setJellyQueenSlamTicks(0);
        }
    }

    private void putBossToSleep() {
        setBossAwake(false);
        bossLostTargetTicks = 0;
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

    private void performBossJumpAttack() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Player player = CandyTargeting.nearestAttackablePlayer(serverLevel, this, 48.0D);
        if (player == null) {
            return;
        }
        if (isPezJelly() && random.nextInt(5) == 0) {
            BasicCandySlimeEntity tornado = CCEntityTypes.TORNADO_JELLY.get().create(serverLevel);
            if (tornado != null) {
                tornado.moveTo(getX(), getY() + 0.5D, getZ(), random.nextFloat() * 360.0F, 0.0F);
                serverLevel.addFreshEntity(tornado);
            }
        } else if (isKingSlime()) {
            if (random.nextInt(5) == 0) {
                serverLevel.explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.NONE);
            }
            if (random.nextInt(3) == 0) {
                BasicCandySlimeEntity jelly = CCEntityTypes.YELLOW_JELLY.get().create(serverLevel);
                if (jelly != null) {
                    jelly.moveTo(getX(), getY() + 0.5D, getZ(), random.nextFloat() * 360.0F, 0.0F);
                    serverLevel.addFreshEntity(jelly);
                }
            }
            if (random.nextInt(10) == 0) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 2), this);
            }
        }
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
