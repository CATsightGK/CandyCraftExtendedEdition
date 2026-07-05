package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.CandyCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class GummyBunnyEntity extends Rabbit {
    private static final EntityDataAccessor<Integer> RED = SynchedEntityData.defineId(GummyBunnyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GREEN = SynchedEntityData.defineId(GummyBunnyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BLUE = SynchedEntityData.defineId(GummyBunnyEntity.class, EntityDataSerializers.INT);
    private static final int[][] GUMMY_SWAMP_COLORS = {
        { 224, 52, 72 },
        { 244, 151, 42 },
        { 247, 228, 68 },
        { 230, 248, 221 },
        { 107, 221, 93 }
    };
    private float lastJumpYaw;
    private int jumpDelay;
    private boolean jumpLocked;
    private boolean legacyJumping;

    public GummyBunnyEntity(EntityType<? extends GummyBunnyEntity> type, Level level) {
        super(type, level);
        setVariant(Variant.WHITE);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(RED, 255);
        entityData.define(GREEN, 255);
        entityData.define(BLUE, 255);
    }

    public int getRed() {
        return entityData.get(RED);
    }

    public int getGreen() {
        return entityData.get(GREEN);
    }

    public int getBlue() {
        return entityData.get(BLUE);
    }

    public void setColor(int red, int green, int blue) {
        entityData.set(RED, red);
        entityData.set(GREEN, green);
        entityData.set(BLUE, blue);
    }

    public void randomizeColor() {
        setColor(random.nextInt(230) + 20, random.nextInt(230) + 20, random.nextInt(230) + 20);
    }

    public void randomizeSwampColor() {
        int[] color = GUMMY_SWAMP_COLORS[random.nextInt(GUMMY_SWAMP_COLORS.length)];
        setColor(color[0], color[1], color[2]);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(CCItems.LICORICE.get());
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 6;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.SLIME_JUMP_SMALL, 0.15F, 1.0F);
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
        return false;
    }

    @Override
    public void aiStep() {
        if (!level().isClientSide && !isInWaterOrBubble()) {
            if (jumpDelay > 0 && onGround()) {
                jumpDelay--;
                setSpeed(0.0F);
                setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
            }
            if (jumpDelay <= 0 && getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.20000000298023224D);
            }
            if (onGround()) {
                jumpLocked = false;
            }
            if (hasHorizontalMovement() && !jumpLocked && jumpDelay <= 0) {
                legacyJumping = true;
                setJumping(true);
                jumpDelay = 10;
            }
            if (legacyJumping) {
                setDeltaMovement(getDeltaMovement().x, isInLove() ? 0.45D : 0.55D, getDeltaMovement().z);
                setJumping(false);
                legacyJumping = false;
                jumpLocked = true;
                playSound(SoundEvents.SLIME_JUMP_SMALL, 0.15F, 1.0F);
            }
            if (jumpLocked && !onGround()) {
                setYRot(lastJumpYaw);
                if (!isInLove() && getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                    getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.40000000298023224D);
                }
            }
            lastJumpYaw = getYRot();
        }
        super.aiStep();
    }

    @Nullable
    @Override
    public Rabbit getBreedOffspring(ServerLevel level, AgeableMob partner) {
        GummyBunnyEntity bunny = CCEntityTypes.GUMMY_BUNNY.get().create(level);
        if (bunny != null) {
            bunny.randomizeColor();
            bunny.setVariant(Variant.WHITE);
        }
        return bunny;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (isBaby()) {
            return;
        }
        int count = 1 + random.nextInt(3) + random.nextInt(looting + 1);
        spawnAtLocation(new ItemStack(CCItems.GUMMY.get(), count));
    }

    @Nullable
    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType spawnType, @Nullable net.minecraft.world.entity.SpawnGroupData spawnGroupData, @Nullable CompoundTag tag) {
        net.minecraft.world.entity.SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag);
        if (isGummySwamp(level)) {
            randomizeSwampColor();
        } else {
            randomizeColor();
        }
        setVariant(Variant.WHITE);
        return data;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("red", getRed());
        tag.putInt("green", getGreen());
        tag.putInt("blue", getBlue());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setColor(tag.getInt("red"), tag.getInt("green"), tag.getInt("blue"));
        setVariant(Variant.WHITE);
    }

    private boolean hasHorizontalMovement() {
        return Math.abs(getDeltaMovement().x) > 0.003D || Math.abs(getDeltaMovement().z) > 0.003D;
    }

    private boolean isGummySwamp(ServerLevelAccessor level) {
        return level.getBiome(blockPosition()).unwrapKey()
            .map(key -> key.location())
            .filter(id -> CandyCraft.MODID.equals(id.getNamespace()))
            .map(ResourceLocation::getPath)
            .filter("gummy_swamp"::equals)
            .isPresent();
    }
}
