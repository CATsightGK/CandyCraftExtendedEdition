package com.valentin4311.candycraftmod.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

public class GummyBearEntity extends PolarBear {
    private static final EntityDataAccessor<Byte> COLOR = SynchedEntityData.defineId(GummyBearEntity.class, EntityDataSerializers.BYTE);

    public GummyBearEntity(EntityType<? extends GummyBearEntity> type, Level level) {
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

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            @Nullable SpawnGroupData spawnData, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, tag);
        setColor(SweetscapeGummyColor.random(random));
        return data;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        GummyBearEntity child = (GummyBearEntity)getType().create(level);
        if (child != null) {
            child.setColor(partner instanceof GummyBearEntity bear ? bear.getColor() : getColor());
        }
        return child;
    }

    @Override
    protected void dropCustomDeathLoot(net.minecraft.world.damagesource.DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        int count = 2 + random.nextInt(3) + random.nextInt(looting + 1);
        spawnAtLocation(new ItemStack(GummyMouseEntity.itemForColor(getColor()), count));
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
        return PolarBear.createAttributes();
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 5;
    }

    public static boolean canSpawn(EntityType<? extends Animal> type, ServerLevelAccessor level,
            MobSpawnType reason, BlockPos pos, net.minecraft.util.RandomSource random) {
        return level.getRawBrightness(pos, 0) > 8 && GummyMouseEntity.isGummySurface(level.getBlockState(pos.below()));
    }
}
