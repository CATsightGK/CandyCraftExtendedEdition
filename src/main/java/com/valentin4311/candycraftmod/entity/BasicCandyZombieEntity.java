package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.item.SugarPillItem;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCSoundEvents;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.phys.Vec3;

public class BasicCandyZombieEntity extends Zombie {
    private static final String TAG_ANGRY = "Angry";
    private static final String TAG_WAITING = "Waiting";
    private static final String TAG_SPAWNED = "Spawned";
    private static final String TAG_COUNTDOWN = "CountDown";
    private boolean angry;
    private boolean waiting;
    private boolean spawnedMinions;
    private int summonCooldown;
    private int rangedCooldown;
    private int power;
    private int variant;
    private boolean saddled;

    public BasicCandyZombieEntity(EntityType<? extends BasicCandyZombieEntity> type, Level level) {
        super(type, level);
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
        if (random.nextInt(50) == 0) {
            angry = true;
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
        tag.putInt("Variant", variant);
        tag.putBoolean("Saddle", saddled);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        angry = tag.getBoolean(TAG_ANGRY);
        waiting = tag.getBoolean(TAG_WAITING);
        spawnedMinions = tag.getBoolean(TAG_SPAWNED);
        summonCooldown = tag.getInt(TAG_COUNTDOWN);
        power = tag.getInt("Power");
        variant = tag.getInt("Variant");
        saddled = tag.getBoolean("Saddle");
    }

    @Override
    public void aiStep() {
        ensureDefaultEquipment();
        if (!isAquatic() && !isDragon() && isInWaterRainOrBubble()) {
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
        super.travel(travelVector);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isNessie()) {
            if (!level().isClientSide && !saddled && stack.is(Items.SADDLE)) {
                saddled = true;
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.SUCCESS;
            }
            if (!level().isClientSide && saddled && player.isShiftKeyDown()) {
                saddled = false;
                spawnAtLocation(Items.SADDLE);
                return InteractionResult.SUCCESS;
            }
            if (!level().isClientSide && saddled && getControllingPassenger() == null) {
                player.startRiding(this);
                return InteractionResult.SUCCESS;
            }
        } else if (isDragon() && !level().isClientSide && getControllingPassenger() == null) {
            player.startRiding(this);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
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
        if (isNougatGolem()) {
            boolean success = super.doHurtTarget(target);
            if (success && !level().isClientSide) {
                level().explode(this, getX(), getY(), getZ(), 2.0F, Level.ExplosionInteraction.NONE);
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
        if (isNougatGolem()) {
            spawnAtLocation(CCItems.NOUGAT_POWDER.get(), 2 + random.nextInt(3 + looting));
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
        if (held.is(CCItems.LICORICE_SPEAR.get()) && random.nextFloat() <= 0.1F) {
            ItemStack drop = held.copy();
            int maxDamage = Math.max(drop.getMaxDamage() - 25, 1);
            int remaining = drop.getMaxDamage() - random.nextInt(random.nextInt(maxDamage) + 1);
            remaining = Math.max(1, Math.min(maxDamage, remaining));
            drop.setDamageValue(remaining);
            spawnAtLocation(drop);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isNessie() ? CCSoundEvents.MOB_NESSIE.get() : isDragon() ? SoundEvents.ENDER_DRAGON_AMBIENT : SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return isNessie() ? CCSoundEvents.MOB_NESSIE_HURT.get() : isDragon() ? SoundEvents.ENDER_DRAGON_HURT : SoundEvents.ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return isNessie() ? CCSoundEvents.MOB_NESSIE_HURT.get() : isDragon() ? SoundEvents.ENDER_DRAGON_DEATH : SoundEvents.ZOMBIE_DEATH;
    }

    private void ensureDefaultEquipment() {
        if (!getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            return;
        }
        if (isMermaid()) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(CCItems.CARAMEL_BOW.get()));
        } else if (isMageSuguard()) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(CCItems.JUMP_WAND.get()));
        } else if (isSuguard() || isBossSuguard()) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(CCItems.LICORICE_SPEAR.get()));
        }
    }

    private void tickMovementAbilities() {
        if (isNessie()) {
            setAirSupply(getMaxAirSupply());
            if (isInWater() && getControllingPassenger() == null && !level().isClientSide) {
                Vec3 movement = getDeltaMovement();
                setDeltaMovement(movement.x * 0.9D + (random.nextDouble() - 0.5D) * 0.015D, movement.y + 0.015D, movement.z * 0.9D + (random.nextDouble() - 0.5D) * 0.015D);
            } else if (!isInWater() && onGround() && tickCount % 10 == 0) {
                setDeltaMovement((random.nextDouble() - 0.5D) * 0.5D, 0.34D, (random.nextDouble() - 0.5D) * 0.5D);
            }
        } else if (isDragon()) {
            if (power < 1500) {
                power++;
            }
            if (!onGround() && getDeltaMovement().y < -0.4D) {
                setDeltaMovement(getDeltaMovement().multiply(0.8D, 0.85D, 0.8D));
            }
        } else if (isMermaid()) {
            setAirSupply(getMaxAirSupply());
            if (isInWater()) {
                Player target = level().getNearestPlayer(this, 16.0D);
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

    private void tickRangedAndBossBehavior(ServerLevel level) {
        if (rangedCooldown > 0) {
            rangedCooldown--;
        }
        if ((isMermaid() || isDragon()) && rangedCooldown <= 0) {
            LivingEntity target = getTarget();
            if (target == null && getControllingPassenger() != null) {
                target = level.getNearestPlayer(this, 18.0D);
            }
            if (target != null) {
                rangedCooldown = isDragon() ? 10 : 30;
                GummyBallEntity ball = new GummyBallEntity(level, this, isDragon() ? 2 : 0);
                double dx = target.getX() - getX();
                double dy = target.getEyeY() - ball.getY();
                double dz = target.getZ() - getZ();
                ball.shoot(dx, dy, dz, 1.4F, 8.0F);
                level.addFreshEntity(ball);
                playSound(SoundEvents.ARROW_SHOOT, 0.6F, 0.9F + random.nextFloat() * 0.2F);
            }
        }
        if (isBossSuguard() && tickCount % 160 == 0) {
            summonSupportCircle(level);
        }
    }

    private void tickMageBehavior(ServerLevel level) {
        if (!isMageSuguard()) {
            return;
        }

        Player nearby = level.getNearestPlayer(this, 8.0D);
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

        if (nearby != null) {
            waiting = false;
            if (random.nextFloat() < 0.05F) {
                BlockPos pos = blockPosition();
                if (level.getBlockState(pos).isAir()) {
                    level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.FIRE.defaultBlockState());
                }
            }
            if (!spawnedMinions) {
                summonSupportCircle(level);
                spawnedMinions = true;
                summonCooldown = 2400;
            }
        } else {
            waiting = true;
        }
    }

    private void summonSupportCircle(ServerLevel level) {
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
            if (summoned.getAttribute(Attributes.MAX_HEALTH) != null) {
                summoned.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10.0D);
            }
            summoned.setHealth(10.0F);

            BlockPos spawnPos = BlockPos.containing(x, y, z);
            if (!level.getBlockState(spawnPos).isAir()) {
                spawnPos = blockPosition();
            }
            summoned.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, getYRot(), 0.0F);
            level.addFreshEntity(summoned);
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

    private boolean isNessie() {
        return getType() == CCEntityTypes.NESSIE.get();
    }

    private boolean isDragon() {
        return getType() == CCEntityTypes.DRAGON.get();
    }

    private boolean isMermaid() {
        return getType() == CCEntityTypes.MERMAID.get();
    }

    private boolean isNougatGolem() {
        return getType() == CCEntityTypes.NOUGAT_GOLEM.get();
    }

    private boolean isKingBeetle() {
        return getType() == CCEntityTypes.KING_BEETLE.get();
    }

    private boolean isAquatic() {
        return isNessie() || isMermaid();
    }

    private boolean isHostileBiome() {
        ResourceLocation biomeId = level().getBiome(blockPosition()).unwrapKey()
            .map(key -> key.location())
            .orElse(null);
        return biomeId != null
            && CandyCraft.MODID.equals(biomeId.getNamespace())
            && "caramel_forest".equals(biomeId.getPath());
    }

    private static final class SuguardTargetGoal extends NearestAttackableTargetGoal<Player> {
        private final BasicCandyZombieEntity suguard;

        private SuguardTargetGoal(BasicCandyZombieEntity suguard) {
            super(suguard, Player.class, true);
            this.suguard = suguard;
        }

        @Override
        public boolean canUse() {
            return (suguard.angry || suguard.isHostileBiome()) && super.canUse();
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
