package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCSoundEvents;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class NessieEntity extends Animal {
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(NessieEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SADDLED = SynchedEntityData.defineId(NessieEntity.class, EntityDataSerializers.BOOLEAN);
    @Nullable
    private BlockPos swimTarget;

    public NessieEntity(EntityType<? extends NessieEntity> type, Level level) {
        super(type, level);
        setPathfindingMalus(BlockPathTypes.WATER, 1.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 60.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.4D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(VARIANT, 0);
        entityData.define(SADDLED, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new BreedGoal(this, 1.0D));
        goalSelector.addGoal(2, new TemptGoal(this, 1.0D, Ingredient.of(CCItems.CRANBERRY_FISH.get()), false));
        goalSelector.addGoal(3, new FollowParentGoal(this, 1.0D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(CCItems.CRANBERRY_FISH.get());
    }

    @Override
    public void aiStep() {
        setAirSupply(getMaxAirSupply());
        if (!level().isClientSide && isInWater() && getControllingPassenger() == null) {
            tickSwimming();
        } else if (!isInWater() && onGround()) {
            swimTarget = null;
            zza = 0.0F;
            setDeltaMovement(getDeltaMovement().multiply(0.15D, 1.0D, 0.15D).add(0.0D, 0.01D, 0.0D));
        }
        super.aiStep();
    }

    @Override
    public void travel(Vec3 travelVector) {
        LivingEntity rider = getControllingPassenger();
        if (rider != null) {
            getNavigation().stop();
            setYRot(rider.getYRot());
            yRotO = getYRot();
            setXRot(rider.getXRot() * 0.5F);
            yBodyRot = getYRot();
            yHeadRot = getYRot();
            float forward = rider.zza;
            float strafe = rider.xxa * 0.35F;
            if (forward <= 0.0F) {
                forward *= 0.25F;
            }
            if (isInWater()) {
                moveRelative(0.055F, new Vec3(strafe, -rider.getXRot() / 45.0F, forward));
                move(MoverType.SELF, getDeltaMovement());
                setDeltaMovement(getDeltaMovement().multiply(0.86D, 0.86D, 0.86D));
            } else {
                setSpeed(0.12F);
                super.travel(new Vec3(strafe, travelVector.y, forward));
            }
            calculateEntityAnimation(false);
            return;
        }
        if (isEffectiveAi() && isInWater()) {
            moveRelative(0.03F, travelVector);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().multiply(0.8D, 0.8D, 0.8D).add(0.0D, -0.02D, 0.0D));
            return;
        }
        super.travel(travelVector);
    }

    private void tickSwimming() {
        Player temptedBy = level().getNearestPlayer(this, 10.0D);
        if (temptedBy != null && (temptedBy.getMainHandItem().is(CCItems.CRANBERRY_FISH.get())
                || temptedBy.getOffhandItem().is(CCItems.CRANBERRY_FISH.get()))) {
            swimToward(temptedBy.getX(), temptedBy.getY() + temptedBy.getBbHeight() * 0.5D, temptedBy.getZ());
            return;
        }
        if (isInLove()) {
            NessieEntity mate = level().getNearestEntity(
                level().getEntitiesOfClass(NessieEntity.class, getBoundingBox().inflate(8.0D), this::canMate),
                net.minecraft.world.entity.ai.targeting.TargetingConditions.forNonCombat(), this,
                getX(), getY(), getZ());
            if (mate != null) {
                swimToward(mate.getX(), mate.getY(), mate.getZ());
                return;
            }
        }
        if (isBaby()) {
            NessieEntity parent = level().getNearestEntity(
                level().getEntitiesOfClass(NessieEntity.class, getBoundingBox().inflate(8.0D), entity -> !entity.isBaby()),
                net.minecraft.world.entity.ai.targeting.TargetingConditions.forNonCombat(), this,
                getX(), getY(), getZ());
            if (parent != null) {
                swimToward(parent.getX(), parent.getY(), parent.getZ());
                return;
            }
        }
        if (!isValidSwimTarget(swimTarget) || random.nextInt(100) == 0 || swimTarget.distToCenterSqr(position()) < 4.0D) {
            swimTarget = chooseSwimTarget();
        }
        if (swimTarget == null) {
            return;
        }
        swimToward(swimTarget.getX() + 0.5D, swimTarget.getY() + 0.1D, swimTarget.getZ() + 0.5D);
    }

    private void swimToward(double x, double y, double z) {
        double dx = x - getX();
        double dy = y - getY();
        double dz = z - getZ();
        Vec3 movement = getDeltaMovement();
        setDeltaMovement(
            movement.x + (Math.signum(dx) * 0.15D - movement.x) * 0.1D,
            movement.y + (Math.signum(dy) * 0.7D - movement.y) * 0.1D,
            movement.z + (Math.signum(dz) * 0.15D - movement.z) * 0.1D
        );
        float yaw = (float)(Mth.atan2(getDeltaMovement().z, getDeltaMovement().x) * Mth.RAD_TO_DEG) - 90.0F;
        setYRot(getYRot() + Mth.wrapDegrees(yaw - getYRot()));
        yBodyRot = getYRot();
        yHeadRot = getYRot();
        zza = 0.5F;
    }

    @Nullable
    private BlockPos chooseSwimTarget() {
        BlockPos origin = blockPosition();
        for (int i = 0; i < 12; i++) {
            BlockPos candidate = origin.offset(random.nextInt(17) - 8, random.nextInt(5) - 2, random.nextInt(17) - 8);
            if (isValidSwimTarget(candidate)) {
                return candidate;
            }
        }
        return isValidSwimTarget(origin) ? origin : null;
    }

    private boolean isValidSwimTarget(@Nullable BlockPos pos) {
        return pos != null && pos.getY() > level().getMinBuildHeight()
            && level().getFluidState(pos).is(FluidTags.WATER)
            && level().getFluidState(pos.above()).is(FluidTags.WATER);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isFood(stack)) {
            return super.mobInteract(player, hand);
        }
        if (!isBaby() && !isNessieSaddled() && stack.is(Items.SADDLE)) {
            if (!level().isClientSide) {
                setNessieSaddled(true);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (isNessieSaddled() && player.isShiftKeyDown()) {
            if (!level().isClientSide) {
                setNessieSaddled(false);
                spawnAtLocation(Items.SADDLE);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (isNessieSaddled() && getFirstPassenger() == null) {
            if (!level().isClientSide) {
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Nullable
    @Override
    public NessieEntity getBreedOffspring(ServerLevel level, AgeableMob partner) {
        NessieEntity child = CCEntityTypes.NESSIE.get().create(level);
        if (child != null) {
            child.randomizeVariant();
            if (random.nextInt(4) == 0) {
                spawnAtLocation(CCItems.WATER_EMBLEM.get());
            }
        }
        return child;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
        randomizeVariant();
        return data;
    }

    private void randomizeVariant() {
        int selected = random.nextInt(4);
        if (random.nextInt(20) == 0) selected = 4;
        if (random.nextInt(20) == 0) selected = 5;
        if (random.nextInt(20) == 0) selected = 6;
        if (random.nextInt(40) == 0) selected = 6;
        entityData.set(VARIANT, selected);
    }

    public int getLegacyVariant() {
        return entityData.get(VARIANT);
    }

    public boolean isNessieSaddled() {
        return entityData.get(SADDLED);
    }

    private void setNessieSaddled(boolean saddled) {
        entityData.set(SADDLED, saddled);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity passenger = getFirstPassenger();
        return isNessieSaddled() && passenger instanceof Player player ? player : null;
    }

    @Override
    public double getPassengersRidingOffset() {
        return getBbHeight() * 0.5D - 0.55D;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return getControllingPassenger() != source.getEntity() && super.hurt(source, amount);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", getLegacyVariant());
        tag.putBoolean("Saddle", isNessieSaddled());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(VARIANT, Mth.clamp(tag.getInt("Variant"), 0, 6));
        setNessieSaddled(tag.getBoolean("Saddle"));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return CCSoundEvents.MOB_NESSIE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return CCSoundEvents.MOB_NESSIE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return CCSoundEvents.MOB_NESSIE_HURT.get();
    }
}
