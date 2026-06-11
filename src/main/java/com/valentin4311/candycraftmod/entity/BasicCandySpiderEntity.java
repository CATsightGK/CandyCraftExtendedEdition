package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BasicCandySpiderEntity extends Spider {
    private static final EntityDataAccessor<Boolean> ANGRY = SynchedEntityData.defineId(BasicCandySpiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CHILD = SynchedEntityData.defineId(BasicCandySpiderEntity.class, EntityDataSerializers.BOOLEAN);
    private BlockPos flightTarget;
    private int stingCooldown;
    private boolean bossAwake;
    private int bossCooldown = 100;
    private int bossVolleyTicks;
    private int bossSpinTicks;

    public BasicCandySpiderEntity(EntityType<? extends BasicCandySpiderEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ANGRY, false);
        entityData.define(CHILD, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        if (isCaramelBee()) {
            targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
            targetSelector.addGoal(2, new HurtByTargetGoal(this));
            return;
        }

        goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.3D, false));
        goalSelector.addGoal(3, new RandomStrollGoal(this, 0.3D));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    public boolean isAngry() {
        return entityData.get(ANGRY);
    }

    public void setAngry(boolean angry) {
        entityData.set(ANGRY, angry);
        if (angry && getAttribute(Attributes.MOVEMENT_SPEED) != null && getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(isBeetle() ? 1.5D : 0.5D);
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(isChildBeetle() ? 7.0D : isBeetle() ? 15.0D : 2.0D);
        }
    }

    public boolean isChildBeetle() {
        return entityData.get(CHILD);
    }

    public void setChildBeetle(boolean child) {
        entityData.set(CHILD, child);
        refreshDimensions();
    }

    @Override
    public void aiStep() {
        if (isCaramelBee()) {
            setNoGravity(true);
            tickBeeMovement();
        } else if (isBossBeetle()) {
            setNoGravity(false);
            tickBossBeetleBehavior();
        } else {
            setNoGravity(false);
            tickBeetleBehavior();
        }
        super.aiStep();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isCaramelBee() || isBeetle()) {
            setAngry(true);
        }
        if (isBossBeetle()) {
            Entity sourceEntity = source.getEntity();
            if (sourceEntity instanceof GummyBallEntity ball && ball.getPower() == 3) {
                bossAwake = true;
                return super.hurt(source, 8.0F);
            }
            if (sourceEntity instanceof Player && !level().isClientSide) {
                bossAwake = true;
            }
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean success = super.doHurtTarget(target);
        if (success && isCaramelBee() && target instanceof net.minecraft.world.entity.LivingEntity living && random.nextInt(15) == 0) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 400, 0), this);
        }
        return success;
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return !isCaramelBee() && super.causeFallDamage(distance, damageMultiplier, source);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        if (!isCaramelBee()) {
            super.checkFallDamage(y, onGround, state, pos);
        }
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
        return data;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (isBossBeetle()) {
            spawnAtLocation(CCItems.RECORD_4.get());
            spawnAtLocation(CCItems.BEETLE_KEY.get());
            spawnAtLocation(CCItems.CHEWING_GUM_EMBLEM.get());
        } else if (isCaramelBee()) {
            spawnAtLocation(CCItems.HONEY_SHARD.get());
        } else if (isBeetle()) {
            spawnAtLocation(CCItems.CHEWING_GUM.get());
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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setAngry(tag.getBoolean("Angry"));
        setChildBeetle(tag.getBoolean("Child"));
        bossAwake = tag.getBoolean("BossAwake");
    }

    private void tickBeeMovement() {
        Player player = level().getNearestPlayer(this, 8.0D);
        if (stingCooldown > 0) {
            stingCooldown--;
        }
        if (player != null) {
            setAngry(true);
            setTarget(player);
            flightTarget = player.blockPosition();
            if (distanceToSqr(player) <= getBbWidth() * 2.0F * getBbWidth() * 2.0F + player.getBbWidth() && stingCooldown <= 0) {
                stingCooldown = 20;
                doHurtTarget(player);
            }
        } else if (!isAngry()) {
            setTarget(null);
        }

        if (flightTarget == null || !level().isEmptyBlock(flightTarget) || flightTarget.getY() < level().getMinBuildHeight()
            || random.nextInt(100) == 0 || flightTarget.closerToCenterThan(position(), 2.0D)) {
            flightTarget = blockPosition().offset(random.nextInt(14) - random.nextInt(14), random.nextInt(6) - 2, random.nextInt(14) - random.nextInt(14));
        }

        double dx = flightTarget.getX() + 0.5D - getX();
        double dy = flightTarget.getY() + 0.1D - getY();
        double dz = flightTarget.getZ() + 0.5D - getZ();
        if (player != null && isAngry()) {
            dx = player.getX() - getX();
            dy = player.getY() + 1.1D - getY();
            dz = player.getZ() - getZ();
        }

        Vec3 movement = getDeltaMovement();
        setDeltaMovement(
            movement.x + (Math.signum(dx) * 0.5D - movement.x) * 0.10000000149011612D,
            (movement.y + (Math.signum(dy) * 0.699999988079071D - movement.y) * 0.10000000149011612D) * 0.6000000238418579D,
            movement.z + (Math.signum(dz) * 0.5D - movement.z) * 0.10000000149011612D
        );

        float targetYaw = (float)(Math.atan2(getDeltaMovement().z, getDeltaMovement().x) * 180.0D / Math.PI) - 90.0F;
        setYRot(getYRot() + net.minecraft.util.Mth.wrapDegrees(targetYaw - getYRot()));
        yBodyRot = getYRot();
    }

    private void tickBeetleBehavior() {
        if (level().isClientSide && isAngry() && random.nextInt(20) == 0) {
            for (int i = 0; i < 2; i++) {
                level().addParticle(ParticleTypes.ANGRY_VILLAGER, getRandomX(1.0D), getRandomY(), getRandomZ(1.0D), 0.0D, 0.0D, 0.0D);
            }
        }

        if (!level().isClientSide && isBeetle() && !isChildBeetle() && getTarget() != null && random.nextInt(500) == 0) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = blockPosition().offset(x, 0, z);
                    if (random.nextBoolean() && level().isEmptyBlock(pos) && CCBlocks.CHEWING_GUM_PUDDLE.get().defaultBlockState().canSurvive(level(), pos)) {
                        level().setBlockAndUpdate(pos, CCBlocks.CHEWING_GUM_PUDDLE.get().defaultBlockState());
                    }
                }
            }
        }
    }

    private void tickBossBeetleBehavior() {
        if (level().isClientSide) {
            if (bossSpinTicks > 0 || random.nextInt(20) == 0) {
                for (int i = 0; i < 4; i++) {
                    double angle = (tickCount + i * 18) * 0.18D;
                    level().addParticle(ParticleTypes.FLAME, getX() + Math.sin(angle) * 2.5D, getY() + 2.5D, getZ() + Math.cos(angle) * 2.5D, 0.0D, 0.0D, 0.0D);
                }
            }
            return;
        }

        Player close = level().getNearestPlayer(this, 10.0D);
        if (close != null) {
            bossAwake = true;
        }
        if (!bossAwake) {
            heal(5.0F);
            setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
            if (getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0D);
            }
            return;
        }
        if (getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35D);
        }

        Player target = level().getNearestPlayer(this, 48.0D);
        if (target == null) {
            bossAwake = false;
            bossVolleyTicks = 0;
            bossSpinTicks = 0;
            return;
        }
        setTarget(target);
        getLookControl().setLookAt(target, 10.0F, getMaxHeadXRot());
        if (bossCooldown > 0) {
            bossCooldown--;
        }
        if (bossVolleyTicks > 0 && tickCount % 2 == 0) {
            bossVolleyTicks--;
            shootBossBall(target, 3, true);
        }
        if (bossSpinTicks > 0) {
            bossSpinTicks--;
            if (bossSpinTicks < 100 && tickCount % 4 == 0) {
                shootBossBall(target, 2, false);
            }
        }
        if (bossCooldown <= 0) {
            double healthPercent = getHealth() / getMaxHealth();
            bossCooldown = (int)(40 - (35 - healthPercent * 35));
            if (healthPercent < 0.5D && random.nextInt(6) == 0) {
                bossVolleyTicks = 50;
            } else if (healthPercent < 0.84D && random.nextInt(10) == 0) {
                bossSpinTicks = 200;
            } else {
                shootBossBall(target, 3, false);
            }
        }
    }

    private void shootBossBall(Player target, int power, boolean lob) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        GummyBallEntity ball = new GummyBallEntity(serverLevel, this, power);
        ball.setPos(getX(), getY() + 1.2D, getZ());
        if (lob) {
            ball.setDeltaMovement((random.nextBoolean() ? -1 : 1) * (0.075D + random.nextDouble() * 0.15D), 1.5D, (random.nextBoolean() ? -1 : 1) * (0.075D + random.nextDouble() * 0.15D));
        } else {
            ball.shoot(target.getX() - getX(), target.getEyeY() - ball.getY(), target.getZ() - getZ(), 1.25F, 8.0F);
        }
        serverLevel.addFreshEntity(ball);
        playSound(net.minecraft.sounds.SoundEvents.ARROW_SHOOT, 1.0F, 0.8F + random.nextFloat() * 0.4F);
    }

    private boolean isCaramelBee() {
        return getType() == CCEntityTypes.CARAMEL_BEE.get();
    }

    private boolean isBeetle() {
        return getType() == CCEntityTypes.BEETLE.get();
    }

    private boolean isBossBeetle() {
        return getType() == CCEntityTypes.BOSS_BEETLE.get();
    }
}
