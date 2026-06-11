package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class GummyBunnyEntity extends Rabbit {
    private static final EntityDataAccessor<Integer> RED = SynchedEntityData.defineId(GummyBunnyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> GREEN = SynchedEntityData.defineId(GummyBunnyEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BLUE = SynchedEntityData.defineId(GummyBunnyEntity.class, EntityDataSerializers.INT);

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

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(CCItems.LICORICE.get());
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

    @Nullable
    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType spawnType, @Nullable net.minecraft.world.entity.SpawnGroupData spawnGroupData, @Nullable CompoundTag tag) {
        net.minecraft.world.entity.SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, tag);
        randomizeColor();
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
}
