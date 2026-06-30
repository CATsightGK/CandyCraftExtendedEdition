package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.item.SugarPillItem;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCFluids;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCSoundEvents;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class BasicCandyZombieEntity extends Zombie {
    private static final String TAG_ANGRY = "Angry";
    private static final String TAG_WAITING = "Waiting";
    private static final String TAG_SPAWNED = "Spawned";
    private static final String TAG_COUNTDOWN = "CountDown";
    private static final EntityDataAccessor<Integer> LEGACY_VARIANT = SynchedEntityData.defineId(BasicCandyZombieEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SADDLED = SynchedEntityData.defineId(BasicCandyZombieEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BABY_DRAGON = SynchedEntityData.defineId(BasicCandyZombieEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> BOSS_BOW_DRAW_TICKS = SynchedEntityData.defineId(BasicCandyZombieEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> BOSS_SUGUARD_AWAKE = SynchedEntityData.defineId(BasicCandyZombieEntity.class, EntityDataSerializers.BOOLEAN);
    private boolean angry;
    private boolean waiting;
    private boolean spawnedMinions;
    private int summonCooldown;
    private int rangedCooldown;
    private int power;
    private int bossSuguardStat;
    private int bossSuguardCounter = 300;
    private int dragonShootTicks;
    private int kingBeetleExplosionCount;
    private int dragonAgeTicks;
    @Nullable
    private BlockPos nessieSwimTarget;
    private final ServerBossEvent bossEvent = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);

    public BasicCandyZombieEntity(EntityType<? extends BasicCandyZombieEntity> type, Level level) {
        super(type, level);
        setPathfindingMalus(BlockPathTypes.WATER, isNessie() ? 1.0F : -1.0F);
        this.waiting = isMageSuguard();
        this.angry = isMageSuguard();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
            .add(Attributes.MAX_HEALTH, 30.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(LEGACY_VARIANT, 0);
        entityData.define(SADDLED, false);
        entityData.define(BABY_DRAGON, false);
        entityData.define(BOSS_BOW_DRAW_TICKS, 0);
        entityData.define(BOSS_SUGUARD_AWAKE, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(4, new SuguardAttackGoal(this));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.2D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new SuguardTargetGoal(this));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return !effect.getEffect().equals(MobEffects.POISON) && super.canBeAffected(effect);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            net.minecraft.world.entity.MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        ensureDefaultEquipment();
        if (isNessie() && getLegacyVariant() == 0) {
            randomizeNessieVariant();
        }
        if ((isSuguard() || isMageSuguard())
            && reason != net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED
            && level instanceof ServerLevel serverLevel && random.nextInt(100) == 0) {
            CaramelBeeEntity bee = CCEntityTypes.CARAMEL_BEE.get().create(serverLevel);
            if (bee != null) {
                bee.moveTo(getX(), getY(), getZ(), getYRot(), 0.0F);
                bee.setAngry(true);
                serverLevel.addFreshEntity(bee);
                startRiding(bee, true);
            }
        }
        return data;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean(TAG_ANGRY, angry);
        tag.putBoolean(TAG_WAITING, waiting);
        tag.putBoolean(TAG_SPAWNED, spawnedMinions);
        tag.putInt(TAG_COUNTDOWN, summonCooldown);
        tag.putInt("Power", power);
        tag.putInt("BossSuguardStat", bossSuguardStat);
        tag.putInt("BossSuguardCounter", bossSuguardCounter);
        tag.putInt("DragonShootTicks", dragonShootTicks);
        tag.putInt("KingBeetleExplosionCount", kingBeetleExplosionCount);
        tag.putInt("Variant", getLegacyVariant());
        tag.putBoolean("Saddle", isNessieSaddled());
        tag.putBoolean("BabyDragon", isBabyDragon());
        tag.putInt("DragonAgeTicks", dragonAgeTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        angry = tag.getBoolean(TAG_ANGRY);
        setBossSuguardAwake(angry);
        waiting = tag.getBoolean(TAG_WAITING);
        spawnedMinions = tag.getBoolean(TAG_SPAWNED);
        summonCooldown = tag.getInt(TAG_COUNTDOWN);
        power = tag.getInt("Power");
        bossSuguardStat = tag.getInt("BossSuguardStat");
        bossSuguardCounter = tag.contains("BossSuguardCounter") ? tag.getInt("BossSuguardCounter") : 300;
        dragonShootTicks = tag.getInt("DragonShootTicks");
        kingBeetleExplosionCount = tag.getInt("KingBeetleExplosionCount");
        setLegacyVariant(tag.getInt("Variant"));
        setNessieSaddled(tag.getBoolean("Saddle"));
        setBabyDragon(tag.getBoolean("BabyDragon"));
        dragonAgeTicks = tag.getInt("DragonAgeTicks");
        ensureDefaultEquipment();
    }

    @Override
    public void aiStep() {
        ensureDefaultEquipment();
        if (isBossSuguard() && !isBossSuguardAwake()) {
            tickDormantBossSuguard();
            updateBossBar();
            return;
        }
        if (!CandyTargeting.canAttackEntity(getTarget())) {
            setTarget(null);
        }
        updateBossBar();
        if (!isAquatic() && !isDragon() && (isInWaterRainOrBubble() || isInGrenadine())) {
            hurt(damageSources().drown(), 1.0F);
        }

        if (!level().isClientSide && level() instanceof ServerLevel serverLevel) {
            tickMageBehavior(serverLevel);
            tickRangedAndBossBehavior(serverLevel);
        }

        tickMovementAbilities();
        spawnLegacyParticles();
        super.aiStep();
    }

    @Override
    public void travel(Vec3 travelVector) {
        if ((isNessie() || isDragon()) && getControllingPassenger() != null) {
            LivingEntity rider = getControllingPassenger();
            setYRot(rider.getYRot());
            yRotO = getYRot();
            setXRot(rider.getXRot() * 0.5F);
            setRot(getYRot(), getXRot());
            yBodyRot = getYRot();
            yHeadRot = yBodyRot;
            float forward = rider.zza;
            float strafe = rider.xxa * 0.35F;
            if (forward <= 0.0F) {
                forward *= 0.25F;
            }
            double speed = isDragon() ? 0.18D : 0.12D;
            if (isDragon() && !onGround()) {
                setDeltaMovement(getDeltaMovement().add(0.0D, -rider.getXRot() / 1000.0D, 0.0D));
            } else if (isNessie() && isInWater()) {
                setDeltaMovement(getDeltaMovement().add(0.0D, -rider.getXRot() / 900.0D, 0.0D));
            }
            setSpeed((float)speed);
            super.travel(new Vec3(strafe, travelVector.y, forward));
            return;
        }
        if (isNessie() && isEffectiveAi() && isInWater()) {
            moveRelative(0.03F, travelVector);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().multiply(0.8D, 0.8D, 0.8D).add(0.0D, -0.02D, 0.0D));
            return;
        }
        super.travel(travelVector);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isNessie()) {
            if (!level().isClientSide && !isNessieSaddled() && stack.is(Items.SADDLE)) {
                setNessieSaddled(true);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
            if (!level().isClientSide && isNessieSaddled() && player.isShiftKeyDown()) {
                setNessieSaddled(false);
                spawnAtLocation(Items.SADDLE);
                return InteractionResult.SUCCESS;
            }
            if (!level().isClientSide && isNessieSaddled() && getControllingPassenger() == null) {
                player.startRiding(this);
                return InteractionResult.SUCCESS;
            }
        } else if (isDragon() && !isBabyDragon() && !level().isClientSide && getControllingPassenger() == null) {
            player.startRiding(this);
            dragonShootTicks = 0;
            return InteractionResult.SUCCESS;
        } else if (isKingBeetle() && !level().isClientSide && getControllingPassenger() == null) {
            player.startRiding(this);
            kingBeetleExplosionCount = 0;
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isSuguard() && source.getEntity() instanceof LivingEntity) {
            angry = true;
        }
        if (isBossSuguard() && source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)) {
            return false;
        }
        if (isBossSuguard()) {
            LivingEntity attacker = source.getEntity() instanceof LivingEntity living && CandyTargeting.canAttackEntity(living) ? living : null;
            boolean hurt = super.hurt(source, amount);
            if (hurt && !level().isClientSide && attacker != null) {
                setBossSuguardAwake(true);
                setTarget(attacker);
                if (level() instanceof ServerLevel serverLevel) {
                    shootBossSuguardArrow(serverLevel, attacker);
                }
            }
            return hurt;
        }
        if ((isDragon() || isKingBeetle()) && getControllingPassenger() != null && getControllingPassenger().equals(source.getEntity())) {
            return false;
        }
        if (isKingBeetle() && source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (hasBossBar()) {
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
        ItemStack held = getItemBySlot(EquipmentSlot.MAINHAND);
        if (isMageSuguard()) {
            boolean success = super.doHurtTarget(target);
            if (success) {
                target.setSecondsOnFire(6);
            }
            return success;
        }
        if (isBossSuguard()) {
            boolean success = super.doHurtTarget(target);
            if (success && target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0), this);
            }
            return success;
        }
        if (held.is(CCItems.DYNAMITE.get())) {
            if (target.getBoundingBox().maxY > getBoundingBox().minY && target.getBoundingBox().minY < getBoundingBox().maxY) {
                boolean mobGriefing = level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
                level().explode(this, getX(), getY(), getZ(), 2.0F,
                    mobGriefing ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
                discard();
                return true;
            }
        } else if (random.nextInt(10) == 0 && onGround()) {
            double dx = target.getX() - getX();
            double dz = target.getZ() - getZ();
            double distance = Math.max(Math.sqrt(dx * dx + dz * dz), 1.0E-4D);
            setDeltaMovement(
                dx / distance * 0.4D + getDeltaMovement().x * 0.2D,
                0.6D,
                dz / distance * 0.4D + getDeltaMovement().z * 0.2D
            );
        }
        if (isSuguard() && getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(getSuguardAttackDamage());
        }
        return super.doHurtTarget(target);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        if (isMageSuguard()) {
            ItemStack stack = new ItemStack(CCItems.SUGAR_PILL.get());
            SugarPillItem.setData(stack, List.of(new MobEffectInstance(MobEffects.JUMP, 20 * 60, 0)), new int[] { 0x8c5dff });
            spawnAtLocation(stack);
            return;
        }
        if (isNessie()) {
            if (random.nextInt(4) == 0) {
                spawnAtLocation(CCItems.WATER_EMBLEM.get());
            }
            spawnAtLocation(CCItems.CRANBERRY_SCALE.get(), 1 + random.nextInt(2 + looting));
            return;
        }
        if (isDragon()) {
            spawnAtLocation(CCItems.SKY_EMBLEM.get());
            return;
        }
        if (isMermaid()) {
            spawnAtLocation(CCItems.CARAMEL_BOW.get());
            return;
        }
        if (isBossSuguard()) {
            spawnAtLocation(CCItems.SUGUARD_EMBLEM.get());
            spawnAtLocation(CCItems.SUGUARD_BOSS_KEY.get());
            return;
        }
        if (isKingBeetle()) {
            spawnAtLocation(CCItems.CHEWING_GUM_EMBLEM.get());
            return;
        }

        ItemStack held = getItemBySlot(EquipmentSlot.MAINHAND);
        if (isSuguard()) {
            if (held.is(CCItems.DYNAMITE.get())) {
                spawnAtLocation(CCItems.DYNAMITE.get());
                if (random.nextFloat() <= 0.1F) {
                    spawnAtLocation(CCItems.DYNAMITE.get());
                }
                return;
            }
            spawnAtLocation(CCItems.LICORICE_SPEAR.get());
            if (held.is(CCItems.LICORICE_SPEAR.get()) && random.nextFloat() <= 0.1F) {
                ItemStack drop = held.copy();
                int maxDamage = Math.max(drop.getMaxDamage() - 25, 1);
                int remaining = drop.getMaxDamage() - random.nextInt(random.nextInt(maxDamage) + 1);
                remaining = Math.max(1, Math.min(maxDamage, remaining));
                drop.setDamageValue(remaining);
                spawnAtLocation(drop);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (isSuguard() || isBossSuguard() || isMermaid() || isDragon() || isKingBeetle()) {
            return null;
        }
        return isNessie() ? CCSoundEvents.MOB_NESSIE.get() : SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        if (isSuguard() || isBossSuguard() || isMermaid() || isDragon() || isKingBeetle()) {
            return null;
        }
        return isNessie() ? CCSoundEvents.MOB_NESSIE_HURT.get() : SoundEvents.ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (isSuguard() || isBossSuguard() || isMermaid() || isDragon() || isKingBeetle()) {
            return null;
        }
        return isNessie() ? CCSoundEvents.MOB_NESSIE_HURT.get() : SoundEvents.ZOMBIE_DEATH;
    }

    private void ensureDefaultEquipment() {
        if (!getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            return;
        }
        if (isMermaid()) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(CCItems.CARAMEL_BOW.get()));
        } else if (isMageSuguard()) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(CCItems.JUMP_WAND.get()));
        } else if (isBossSuguard()) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(CCItems.CARAMEL_BOW.get()));
        } else if (isSuguard()) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(CCItems.LICORICE_SPEAR.get()));
        }
    }

    private void tickMovementAbilities() {
        if (isNessie()) {
            setAirSupply(getMaxAirSupply());
            if (isInWater() && getControllingPassenger() == null && !level().isClientSide) {
                tickNessieSwimming();
            } else if (!isInWater() && onGround() && tickCount % 10 == 0) {
                nessieSwimTarget = null;
                setDeltaMovement((random.nextDouble() - 0.5D) * 0.5D, 0.34D, (random.nextDouble() - 0.5D) * 0.5D);
            }
        } else if (isDragon()) {
            tickDragonGrowth();
            if (power < getMountMaxPower()) {
                power++;
            }
            tickDragonPower();
            if (!onGround() && getDeltaMovement().y < -0.4D) {
                setDeltaMovement(getDeltaMovement().multiply(0.8D, 0.85D, 0.8D));
            }
        } else if (isKingBeetle()) {
            if (power < getMountMaxPower()) {
                power++;
            }
            tickKingBeetlePower();
        } else if (isMermaid()) {
            setAirSupply(getMaxAirSupply());
            if (isInWater()) {
                Player target = CandyTargeting.nearestAttackablePlayer(level(), this, 16.0D);
                if (target != null) {
                    getLookControl().setLookAt(target);
                    Vec3 to = target.position().subtract(position()).normalize();
                    setDeltaMovement(getDeltaMovement().add(to.x * 0.015D, to.y * 0.01D, to.z * 0.015D));
                }
            } else if (onGround() && tickCount % 10 == 0) {
                playSound(SoundEvents.GUARDIAN_FLOP, 1.0F, 1.0F);
                setDeltaMovement((random.nextDouble() - 0.5D) * 0.5D, 0.34D, (random.nextDouble() - 0.5D) * 0.5D);
            }
        }
    }

    private void tickNessieSwimming() {
        LivingEntity target = getTarget();
        if (target != null && CandyTargeting.canAttackEntity(target) && target.isInWater()) {
            swimToward(target.getX(), target.getY() + target.getBbHeight() * 0.4D, target.getZ(), 0.18D, 0.75D);
            return;
        }

        if (!isValidNessieSwimTarget(nessieSwimTarget)
                || random.nextInt(100) == 0
                || nessieSwimTarget.distToCenterSqr(position()) < 4.0D) {
            nessieSwimTarget = chooseNessieSwimTarget();
        }

        if (nessieSwimTarget != null) {
            swimToward(nessieSwimTarget.getX() + 0.5D, nessieSwimTarget.getY() + 0.1D, nessieSwimTarget.getZ() + 0.5D, 0.15D, 0.7D);
        }
    }

    private void swimToward(double x, double y, double z, double horizontalSpeed, double verticalSpeed) {
        double dx = x - getX();
        double dy = y - getY();
        double dz = z - getZ();
        Vec3 movement = getDeltaMovement();
        setDeltaMovement(
            movement.x + (Math.signum(dx) * horizontalSpeed - movement.x) * 0.1D,
            movement.y + (Math.signum(dy) * verticalSpeed - movement.y) * 0.1D,
            movement.z + (Math.signum(dz) * horizontalSpeed - movement.z) * 0.1D
        );
        float yaw = (float)(Mth.atan2(getDeltaMovement().z, getDeltaMovement().x) * Mth.RAD_TO_DEG) - 90.0F;
        float yawDelta = Mth.wrapDegrees(yaw - getYRot());
        setYRot(getYRot() + yawDelta);
        yBodyRot = getYRot();
        yHeadRot = getYRot();
        zza = 0.5F;
    }

    @Nullable
    private BlockPos chooseNessieSwimTarget() {
        BlockPos origin = blockPosition();
        for (int i = 0; i < 12; i++) {
            BlockPos candidate = origin.offset(random.nextInt(17) - 8, random.nextInt(5) - 2, random.nextInt(17) - 8);
            if (isValidNessieSwimTarget(candidate)) {
                return candidate;
            }
        }
        return isValidNessieSwimTarget(origin) ? origin : null;
    }

    private boolean isValidNessieSwimTarget(@Nullable BlockPos pos) {
        return pos != null
            && pos.getY() > level().getMinBuildHeight()
            && level().getFluidState(pos).is(FluidTags.WATER)
            && level().getFluidState(pos.above()).is(FluidTags.WATER);
    }

    private void tickRangedAndBossBehavior(ServerLevel level) {
        if (rangedCooldown > 0) {
            rangedCooldown--;
        }
        if (isMermaid() && rangedCooldown <= 0) {
            LivingEntity target = getTarget();
            if (target == null && getControllingPassenger() != null) {
                target = CandyTargeting.nearestAttackablePlayer(level, this, 18.0D);
            }
            if (target != null && !CandyTargeting.canAttackEntity(target)) {
                setTarget(null);
                target = null;
            }
            if (target != null) {
                rangedCooldown = 30;
                HoneyArrowEntity ball = new HoneyArrowEntity(level, this);
                double dx = target.getX() - getX();
                double dy = target.getEyeY() - ball.getY();
                double dz = target.getZ() - getZ();
                ball.shoot(dx, dy, dz, 1.4F, 8.0F);
                level.addFreshEntity(ball);
                playSound(SoundEvents.ARROW_SHOOT, 0.6F, 0.9F + random.nextFloat() * 0.2F);
            }
        }
        if (isBossSuguard()) {
            tickBossSuguard(level);
        }
        if (getBossBowDrawTicks() > 0) {
            entityData.set(BOSS_BOW_DRAW_TICKS, getBossBowDrawTicks() - 1);
        }
    }

    private void tickBossSuguard(ServerLevel level) {
        if (!isBossSuguardAwake()) {
            tickDormantBossSuguard();
            return;
        }
        if (getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35D);
        }
        if (--bossSuguardCounter <= 0) {
            bossSuguardCounter = 240 + random.nextInt(80);
            bossSuguardStat = random.nextInt(3) + 1;
        }
        LivingEntity target = getTarget();
        if (target != null && (!CandyTargeting.canAttackEntity(target) || distanceToSqr(target) > 48.0D * 48.0D)) {
            setTarget(null);
            target = null;
        }
        if (target == null) {
            target = CandyTargeting.nearestAttackablePlayer(level, this, 48.0D);
        }
        if (target == null) {
            setBossSuguardAwake(false);
            bossSuguardStat = 0;
            return;
        }
        setTarget(target);
        getLookControl().setLookAt(target, 10.0F, getMaxHeadXRot());
        if (rangedCooldown <= 0) {
            boolean volley = bossSuguardStat == 1 || bossSuguardCounter < 70;
            rangedCooldown = volley ? 5 : 24;
            int shots = distanceTo(target) < 3.0F ? 5 : volley ? 2 + random.nextInt(3) : 1;
            for (int i = 0; i < shots; i++) {
                shootBossSuguardArrow(level, target);
            }
        }
    }

    private void shootBossSuguardArrow(ServerLevel level, LivingEntity target) {
        entityData.set(BOSS_BOW_DRAW_TICKS, 14);
        HoneyArrowEntity arrow = new HoneyArrowEntity(level, this);
        arrow.setPos(getX(), getEyeY() - 0.05D, getZ());
        arrow.setBaseDamage(4.0D + level.getDifficulty().getId() * 0.2D + random.nextGaussian() * 0.25D);
        arrow.setSecondsOnFire(bossSuguardStat == 3 ? 5 : 0);
        arrow.setSlow(bossSuguardStat == 2);
        if (distanceTo(target) < 3.0F) {
            arrow.setKnockback(2);
        }
        arrow.shoot(target.getX() - getX(), target.getEyeY() - arrow.getY(), target.getZ() - getZ(), 1.85F, bossSuguardStat == 1 ? 12.0F : 7.0F);
        level.addFreshEntity(arrow);
        playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
    }

    private int getMountMaxPower() {
        return isKingBeetle() ? 1200 : 1500;
    }

    private int getMountPowerUsed() {
        return isKingBeetle() ? 1200 : 300;
    }

    private void tryUnleashMountPower() {
        if (power < getMountPowerUsed()) {
            return;
        }
        if (isDragon()) {
            power -= getMountPowerUsed();
            dragonShootTicks += 10 + random.nextInt(8);
        } else if (isKingBeetle()) {
            power = 0;
            kingBeetleExplosionCount = 8 + random.nextInt(8);
        }
    }

    private void tickDragonPower() {
        if (isBabyDragon()) {
            dragonShootTicks = 0;
            return;
        }
        LivingEntity controller = getControllingPassenger();
        if (controller == null) {
            dragonShootTicks = 0;
            return;
        }
        if (controller.swinging && dragonShootTicks <= 0) {
            tryUnleashMountPower();
        }
        if (dragonShootTicks > 0 && tickCount % 2 == 0 && !level().isClientSide && level() instanceof ServerLevel serverLevel) {
            setYRot(controller.getYRot());
            GummyBallEntity ball = new GummyBallEntity(serverLevel, controller, 2);
            double yaw = controller.getYRot() * Math.PI / 180.0D;
            double offsetX = Math.sin(yaw) * 3.5D;
            double offsetZ = -Math.cos(yaw) * 3.5D;
            ball.setPos(getX() - offsetX, getY() + controller.getEyeHeight(), getZ() - offsetZ);
            ball.shoot(-offsetX, -controller.getXRot() * 0.05D, -offsetZ, 1.4F, 1.0F);
            serverLevel.addFreshEntity(ball);
            setDeltaMovement(offsetX / 3.4D, getDeltaMovement().y, offsetZ / 3.4D);
            playSound(SoundEvents.ARROW_SHOOT, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
            dragonShootTicks--;
        }
    }

    private void tickKingBeetlePower() {
        LivingEntity controller = getControllingPassenger();
        if (controller == null) {
            kingBeetleExplosionCount = 0;
            return;
        }
        if (controller.swinging && kingBeetleExplosionCount <= 0) {
            tryUnleashMountPower();
        }
        if (!level().isClientSide && kingBeetleExplosionCount > 0 && tickCount % 5 == 0 && level() instanceof ServerLevel serverLevel) {
            double x = getX() + random.nextInt(6) - 3;
            double y = getY() + random.nextInt(5) - 2;
            double z = getZ() + random.nextInt(6) - 3;
            serverLevel.explode(controller, x, y, z, 2.0F, Level.ExplosionInteraction.NONE);
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos pos = blockPosition().offset(dx, 0, dz);
                    if (random.nextBoolean() && serverLevel.getBlockState(pos).isAir()
                        && com.valentin4311.candycraftmod.registry.CCBlocks.CHEWING_GUM_PUDDLE.get().defaultBlockState().canSurvive(serverLevel, pos)) {
                        serverLevel.setBlockAndUpdate(pos, com.valentin4311.candycraftmod.registry.CCBlocks.CHEWING_GUM_PUDDLE.get().defaultBlockState());
                    }
                }
            }
            kingBeetleExplosionCount--;
        }
    }

    private void tickDragonGrowth() {
        if (!isBabyDragon() || level().isClientSide) {
            return;
        }
        dragonAgeTicks++;
        if (dragonAgeTicks >= 24000) {
            setBabyDragon(false);
            dragonAgeTicks = 0;
        }
    }

    private void updateBossBar() {
        if (!hasBossBar() || level().isClientSide) {
            return;
        }
        bossEvent.setName(getBossBarName());
        bossEvent.setColor(getBossBarColor());
        bossEvent.setProgress(Math.max(0.0F, Math.min(1.0F, getHealth() / getMaxHealth())));
        bossEvent.setVisible(!isBossSuguard() || angry);
    }

    private boolean hasBossBar() {
        return isBossSuguard() || isKingBeetle();
    }

    private Component getBossBarName() {
        return getType().getDescription();
    }

    private BossEvent.BossBarColor getBossBarColor() {
        if (isKingBeetle()) {
            return BossEvent.BossBarColor.YELLOW;
        }
        return BossEvent.BossBarColor.WHITE;
    }

    private void tickMageBehavior(ServerLevel level) {
        if (!isMageSuguard()) {
            return;
        }

        LivingEntity currentTarget = getTarget();
        if (currentTarget != null && !CandyTargeting.canAttackEntity(currentTarget)) {
            setTarget(null);
            currentTarget = null;
        }
        Player nearby = CandyTargeting.nearestAttackablePlayer(level, this, 8.0D);
        LivingEntity trigger = currentTarget != null ? currentTarget : nearby;
        waiting = trigger == null;
        if (waiting) {
            getNavigation().stop();
            setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
        }

        if (summonCooldown > 0) {
            summonCooldown--;
            if (summonCooldown == 0) {
                spawnedMinions = false;
            }
        }

        if (trigger != null) {
            setTarget(trigger);
            clearFire();
            if (!spawnedMinions) {
                summonSupportCircle(level, trigger);
                spawnedMinions = true;
                summonCooldown = 2400;
            }
        }
    }

    private void summonSupportCircle(ServerLevel level, LivingEntity target) {
        for (int i = 0; i <= 8; i++) {
            double angle = i / 3.75F * Math.PI;
            double x = -Math.sin(angle) * 2.5D + getX();
            double z = Math.cos(angle) * 2.5D + getZ();
            double y = getY() + 2.0D;

            BasicCandyZombieEntity summoned = CCEntityTypes.SUGUARD.get().create(level);
            if (summoned == null) {
                continue;
            }
            summoned.angry = true;
            summoned.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(CCItems.DYNAMITE.get()));
            summoned.setTarget(target);
            if (summoned.getAttribute(Attributes.MAX_HEALTH) != null) {
                summoned.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10.0D);
            }
            if (summoned.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
                summoned.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(6.0D);
            }
            summoned.setHealth(10.0F);

            BlockPos spawnPos = BlockPos.containing(x, y, z);
            if (!level.getBlockState(spawnPos).isAir() || !level.getBlockState(spawnPos.above()).isAir()) {
                spawnPos = blockPosition();
            }
            summoned.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, getYRot(), 0.0F);
            if (!level.noCollision(summoned)) {
                summoned.moveTo(getX(), getY(), getZ(), getYRot(), 0.0F);
            }
            level.addFreshEntityWithPassengers(summoned);
        }
    }

    private void spawnLegacyParticles() {
        if (random.nextInt(30) != 0) {
            return;
        }
        if (isMageSuguard()) {
            for (int i = 0; i < 5; i++) {
                level().addParticle(ParticleTypes.FLAME,
                    getX() + (random.nextDouble() - 0.5D) * getBbWidth() * 2.0D,
                    getY() + random.nextDouble() * getBbHeight(),
                    getZ() + (random.nextDouble() - 0.5D) * getBbWidth() * 2.0D,
                    0.0D, 0.0D, 0.0D);
            }
        } else {
            for (int i = 0; i < 2; i++) {
                level().addParticle(ParticleTypes.CLOUD,
                    getX() + (random.nextDouble() - 0.5D) * getBbWidth(),
                    getY() + random.nextDouble() * getBbHeight(),
                    getZ() + (random.nextDouble() - 0.5D) * getBbWidth(),
                    0.0D, 0.0D, 0.0D);
            }
        }
    }

    private boolean isMageSuguard() {
        return getType() == CCEntityTypes.MAGE_SUGUARD.get();
    }

    private boolean isSuguard() {
        return getType() == CCEntityTypes.SUGUARD.get();
    }

    private boolean isBossSuguard() {
        return getType() == CCEntityTypes.BOSS_SUGUARD.get();
    }

    public boolean isBossSuguardAwake() {
        return isBossSuguard() && entityData.get(BOSS_SUGUARD_AWAKE);
    }

    private void setBossSuguardAwake(boolean awake) {
        angry = awake;
        entityData.set(BOSS_SUGUARD_AWAKE, awake);
    }

    private void tickDormantBossSuguard() {
        setBossSuguardAwake(false);
        heal(5.0F);
        setTarget(null);
        getNavigation().stop();
        getLookControl().setLookAt(this);
        setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
        if (getAttribute(Attributes.MOVEMENT_SPEED) != null) {
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0D);
        }
        bossSuguardStat = 0;
        entityData.set(BOSS_BOW_DRAW_TICKS, 0);
        yRotO = getYRot();
        yBodyRot = getYRot();
        yHeadRot = getYRot();
        yBodyRotO = yBodyRot;
        yHeadRotO = yHeadRot;
    }

    public int getBossSuguardStat() {
        return bossSuguardStat;
    }

    public int getBossBowDrawTicks() {
        return entityData.get(BOSS_BOW_DRAW_TICKS);
    }

    public float getBossBowDrawProgress(float partialTicks) {
        if (!isBossSuguard()) {
            return 0.0F;
        }
        float ticks = Math.max(0.0F, getBossBowDrawTicks() - partialTicks);
        return Math.min(1.0F, ticks / 14.0F);
    }

    private boolean isNessie() {
        return getType() == CCEntityTypes.NESSIE.get();
    }

    private boolean isDragon() {
        return getType() == CCEntityTypes.DRAGON.get();
    }

    public boolean isBabyDragon() {
        return entityData.get(BABY_DRAGON);
    }

    public void setBabyDragon(boolean babyDragon) {
        entityData.set(BABY_DRAGON, babyDragon);
    }

    public int getLegacyVariant() {
        return entityData.get(LEGACY_VARIANT);
    }

    private void setLegacyVariant(int variant) {
        entityData.set(LEGACY_VARIANT, Math.max(0, variant));
    }

    public boolean isNessieSaddled() {
        return entityData.get(SADDLED);
    }

    private void setNessieSaddled(boolean saddled) {
        entityData.set(SADDLED, saddled);
    }

    private void randomizeNessieVariant() {
        int selected = random.nextInt(4);
        if (random.nextInt(20) == 0) {
            selected = 4;
        }
        if (random.nextInt(20) == 0) {
            selected = 5;
        }
        if (random.nextInt(20) == 0) {
            selected = 6;
        }
        if (random.nextInt(40) == 0) {
            selected = 6;
        }
        setLegacyVariant(selected);
    }

    private boolean isMermaid() {
        return getType() == CCEntityTypes.MERMAID.get();
    }

    private boolean isKingBeetle() {
        return getType() == CCEntityTypes.KING_BEETLE.get();
    }

    private boolean isAquatic() {
        return isNessie() || isMermaid();
    }

    private boolean isInGrenadine() {
        FluidState state = level().getFluidState(blockPosition());
        return state.is(CCFluids.SOURCE_GRENADINE.get()) || state.is(CCFluids.FLOWING_GRENADINE.get());
    }

    private boolean isHostileBiome() {
        ResourceLocation biomeId = level().getBiome(blockPosition()).unwrapKey()
            .map(key -> key.location())
            .orElse(null);
        return biomeId != null
            && CandyCraft.MODID.equals(biomeId.getNamespace())
            && "caramel_forest".equals(biomeId.getPath());
    }

    private boolean shouldActHostile() {
        return angry || isHostileBiome() || level().isNight();
    }

    private double getSuguardAttackDamage() {
        Difficulty difficulty = level().getDifficulty();
        if (difficulty == Difficulty.HARD) {
            return 10.0D;
        }
        if (difficulty == Difficulty.EASY) {
            return 5.0D;
        }
        return 7.0D;
    }

    private static final class SuguardTargetGoal extends NearestAttackableTargetGoal<Player> {
        private final BasicCandyZombieEntity suguard;

        private SuguardTargetGoal(BasicCandyZombieEntity suguard) {
            super(suguard, Player.class, 10, true, false,
                entity -> entity instanceof Player player && CandyTargeting.canAttackPlayer(player));
            this.suguard = suguard;
        }

        @Override
        public boolean canUse() {
            return suguard.shouldActHostile() && super.canUse();
        }
    }

    private static final class SuguardAttackGoal extends MeleeAttackGoal {
        private final BasicCandyZombieEntity suguard;

        private SuguardAttackGoal(BasicCandyZombieEntity suguard) {
            super(suguard, 1.0D, true);
            this.suguard = suguard;
        }

        @Override
        public boolean canContinueToUse() {
            if (suguard.getRandom().nextBoolean()) {
                suguard.setJumping(true);
            }
            return super.canContinueToUse();
        }

        @Override
        protected double getAttackReachSqr(LivingEntity target) {
            return 4.0F + target.getBbWidth();
        }
    }
}
