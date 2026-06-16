package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BasicCandySlimeEntity extends Slime {
    private int specialAttackCooldown;
    private boolean bossAwake;
    private int bossJumpCooldown;
    private final ServerBossEvent bossEvent = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

    public BasicCandySlimeEntity(EntityType<? extends BasicCandySlimeEntity> type, Level level) {
        super(type, level);
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
        super.aiStep();
        updateBossBar();
        if (specialAttackCooldown > 0) {
            specialAttackCooldown--;
        }
        if (isTornadoJelly() && !onGround() && level().isClientSide) {
            for (int i = 0; i < 2; i++) {
                level().addParticle(ParticleTypes.CLOUD, getRandomX(0.8D), getRandomY(), getRandomZ(0.8D), 0.0D, 0.02D, 0.0D);
            }
        }
        if (isJellyQueen() && tickCount % 80 == 0 && !level().isClientSide) {
            heal(2.0F);
        }
        tickBossAwakeBehavior();
    }

    @Override
    protected int getJumpDelay() {
        if (isYellowJelly()) {
            return 4;
        }
        if (isPezJelly() || isJellyQueen()) {
            return 8;
        }
        return super.getJumpDelay();
    }

    @Override
    protected void jumpFromGround() {
        if (isCandyBoss() && !bossAwake) {
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
            player.hurt(damageSources().mobAttack(this), isKingSlime() ? 12.0F : 8.0F);
        }
        super.playerTouch(player);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof BasicCandySlimeEntity) {
            return false;
        }
        if (isCandyBoss() && source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
            return false;
        }
        if (isCandyBoss() && !level().isClientSide && source.getEntity() != null) {
            bossAwake = true;
            setDeltaMovement(getDeltaMovement().add(0.0D, 0.9D, 0.0D));
        }
        return super.hurt(source, amount);
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
            setSize(1, false);
        }
        super.remove(reason);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (isYellowJelly()) {
            spawnAtLocation(CCItems.GUMMY.get());
        } else if (isRedJelly() || isTornadoJelly()) {
            spawnAtLocation(CCItems.HOT_GUMMY.get());
        } else if (isPezJelly()) {
            spawnAtLocation(CCItems.PEZ.get(), 2);
        } else if (isKingSlime() || isJellyQueen()) {
            spawnAtLocation(CCItems.JELLY_CROWN.get());
            if (isKingSlime()) {
                spawnAtLocation(CCItems.JELLY_BOSS_KEY.get());
            } else {
                spawnAtLocation(CCItems.JELLY_EMBLEM.get());
                spawnAtLocation(CCItems.JELLY_KEY.get());
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("BossAwake", bossAwake);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        bossAwake = tag.getBoolean("BossAwake");
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
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(CCSoundEvents.STEP_JELLY.get(), 0.18F, 0.9F + random.nextFloat() * 0.2F);
    }

    @Override
    public void setSize(int size, boolean resetHealth) {
        super.setSize(size, resetHealth);
        if (getAttribute(Attributes.MAX_HEALTH) != null) {
            if (isKingSlime()) {
                getAttribute(Attributes.MAX_HEALTH).setBaseValue(800.0D);
                setHealth(getMaxHealth());
            } else if (isJellyQueen()) {
                getAttribute(Attributes.MAX_HEALTH).setBaseValue(300.0D);
                setHealth(getMaxHealth());
            } else if (isPezJelly()) {
                getAttribute(Attributes.MAX_HEALTH).setBaseValue(80.0D);
                setHealth(getMaxHealth());
            }
        }
    }

    private void tickBossAwakeBehavior() {
        if (!isCandyBoss() || level().isClientSide) {
            return;
        }
        Player player = level().getNearestPlayer(this, 48.0D);
        AttributeInstance speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (player == null) {
            bossAwake = false;
            if (speed != null) {
                speed.setBaseValue(0.0D);
            }
            setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
            if (tickCount % 20 == 0) {
                heal(5.0F);
            }
            return;
        }
        if (bossAwake && speed != null) {
            speed.setBaseValue(isJellyQueen() ? 0.7D : isKingSlime() ? 0.45D : 0.38D);
        }
        if (bossJumpCooldown > 0) {
            bossJumpCooldown--;
        }
        if (bossAwake && onGround() && bossJumpCooldown <= 0) {
            bossJumpCooldown = isJellyQueen() ? 20 + random.nextInt(25) : 15 + random.nextInt(30);
            getLookControl().setLookAt(player);
            setDeltaMovement(getDeltaMovement().add(
                (player.getX() - getX()) * 0.04D,
                isKingSlime() ? 0.9D : 0.7D,
                (player.getZ() - getZ()) * 0.04D));
        }
    }

    private void updateBossBar() {
        if (!isCandyBoss() || level().isClientSide) {
            return;
        }
        bossEvent.setName(getType().getDescription());
        bossEvent.setColor(isKingSlime() ? BossEvent.BossBarColor.GREEN : BossEvent.BossBarColor.PURPLE);
        bossEvent.setProgress(Math.max(0.0F, Math.min(1.0F, getHealth() / getMaxHealth())));
    }

    private void performBossJumpAttack() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Player player = serverLevel.getNearestPlayer(this, 48.0D);
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
        } else if (isJellyQueen() && getHealth() <= getMaxHealth() * 0.25F) {
            serverLevel.explode(this, getX(), getY(), getZ(), 3.0F, Level.ExplosionInteraction.NONE);
            serverLevel.explode(this, getX(), getY() + 2.0D, getZ(), 3.0F, Level.ExplosionInteraction.NONE);
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
        return !player.getAbilities().instabuild && !player.isSpectator();
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
            setSize(8, true);
        }
    }
}
