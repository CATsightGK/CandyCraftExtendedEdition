package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class GummyMouseEntity extends Animal {
    private static final EntityDataAccessor<Byte> COLOR = SynchedEntityData.defineId(GummyMouseEntity.class, EntityDataSerializers.BYTE);

    public GummyMouseEntity(EntityType<? extends GummyMouseEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(COLOR, (byte)0);
    }

    public SweetscapeGummyColor getColor() {
        return SweetscapeGummyColor.byId(entityData.get(COLOR));
    }

    public void setColor(SweetscapeGummyColor color) {
        entityData.set(COLOR, (byte)color.id());
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.2D));
        goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Ocelot.class, 8.0F, 0.85D, 1.33D));
        goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, 1.2F, 0.85D, 1.33D));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 8;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.RABBIT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RABBIT_DEATH;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
        setColor(colorFromGround(level.getBlockState(blockPosition().below()), random));
        return data;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        spawnAtLocation(new ItemStack(itemForColor(getColor()), 1 + random.nextInt(2 + looting)));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("Color", (byte)getColor().id());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setColor(SweetscapeGummyColor.byId(tag.getByte("Color")));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 2.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    public static boolean canSpawn(EntityType<? extends GummyMouseEntity> type, ServerLevelAccessor level,
            MobSpawnType reason, BlockPos pos, net.minecraft.util.RandomSource random) {
        return level.getRawBrightness(pos, 0) > 8 && isGummySurface(level.getBlockState(pos.below()));
    }

    public static boolean isGummySurface(BlockState state) {
        return state.is(CCBlocks.RED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.ORANGE_GUMMY_BLOCK.get())
            || state.is(CCBlocks.YELLOW_GUMMY_BLOCK.get())
            || state.is(CCBlocks.WHITE_GUMMY_BLOCK.get())
            || state.is(CCBlocks.GREEN_GUMMY_BLOCK.get())
            || state.is(CCBlocks.RED_HARDENED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.ORANGE_HARDENED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.YELLOW_HARDENED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.WHITE_HARDENED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.GREEN_HARDENED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.RED_GUMMY_WORM_BLOCK.get())
            || state.is(CCBlocks.ORANGE_GUMMY_WORM_BLOCK.get())
            || state.is(CCBlocks.YELLOW_GUMMY_WORM_BLOCK.get())
            || state.is(CCBlocks.WHITE_GUMMY_WORM_BLOCK.get())
            || state.is(CCBlocks.GREEN_GUMMY_WORM_BLOCK.get());
    }

    static SweetscapeGummyColor colorFromGround(BlockState state, net.minecraft.util.RandomSource random) {
        if (state.is(CCBlocks.ORANGE_GUMMY_BLOCK.get())
            || state.is(CCBlocks.ORANGE_HARDENED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.ORANGE_GUMMY_WORM_BLOCK.get())) {
            return SweetscapeGummyColor.ORANGE;
        }
        if (state.is(CCBlocks.YELLOW_GUMMY_BLOCK.get())
            || state.is(CCBlocks.YELLOW_HARDENED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.YELLOW_GUMMY_WORM_BLOCK.get())) {
            return SweetscapeGummyColor.YELLOW;
        }
        if (state.is(CCBlocks.WHITE_GUMMY_BLOCK.get())
            || state.is(CCBlocks.WHITE_HARDENED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.WHITE_GUMMY_WORM_BLOCK.get())) {
            return SweetscapeGummyColor.WHITE;
        }
        if (state.is(CCBlocks.GREEN_GUMMY_BLOCK.get())
            || state.is(CCBlocks.GREEN_HARDENED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.GREEN_GUMMY_WORM_BLOCK.get())) {
            return SweetscapeGummyColor.GREEN;
        }
        if (state.is(CCBlocks.RED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.RED_HARDENED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.RED_GUMMY_WORM_BLOCK.get())) {
            return SweetscapeGummyColor.RED;
        }
        return SweetscapeGummyColor.random(random);
    }

    static net.minecraft.world.item.Item itemForColor(SweetscapeGummyColor color) {
        return switch (color) {
            case ORANGE -> CCItems.ORANGE_GUMMY.get();
            case YELLOW -> CCItems.YELLOW_GUMMY.get();
            case WHITE -> CCItems.WHITE_GUMMY.get();
            case GREEN -> CCItems.GREEN_GUMMY.get();
            case RED -> CCItems.RED_GUMMY.get();
        };
    }
}

