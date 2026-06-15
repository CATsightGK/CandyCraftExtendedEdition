package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class CandyFishEntity extends WaterAnimal {
    public float current;
    private BlockPos currentFlightTarget;

    public CandyFishEntity(EntityType<? extends CandyFishEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return WaterAnimal.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 10.0D)
            .add(Attributes.MOVEMENT_SPEED, 2.0D);
    }

    public static boolean canSpawn(EntityType<CandyFishEntity> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, net.minecraft.util.RandomSource random) {
        FluidState fluid = level.getFluidState(pos);
        return pos.getY() > 45 && pos.getY() < 63 && fluid.is(FluidTags.WATER);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        current += 0.2F;

        if (isInWater()) {
            if (currentFlightTarget != null) {
                FluidState state = level().getFluidState(currentFlightTarget);
                if (!state.is(FluidTags.WATER) || currentFlightTarget.getY() < level().getMinBuildHeight()) {
                    currentFlightTarget = null;
                }
            }

            if (currentFlightTarget == null || random.nextInt(100) == 0 || currentFlightTarget.closerToCenterThan(position(), 2.0D)) {
                currentFlightTarget = blockPosition().offset(random.nextInt(14) - random.nextInt(14), random.nextInt(3) - 1, random.nextInt(14) - random.nextInt(14));
            }

            double dx = currentFlightTarget.getX() + 0.5D - getX();
            double dy = currentFlightTarget.getY() + 0.1D - getY();
            double dz = currentFlightTarget.getZ() + 0.5D - getZ();

            setDeltaMovement(
                getDeltaMovement().x + (Math.signum(dx) * 0.5D - getDeltaMovement().x) * 0.10000000149011612D,
                getDeltaMovement().y + (Math.signum(dy) * 0.699999988079071D - getDeltaMovement().y) * 0.10000000149011612D - 0.002D,
                getDeltaMovement().z + (Math.signum(dz) * 0.5D - getDeltaMovement().z) * 0.10000000149011612D
            );

            float targetYaw = (float)(Mth.atan2(getDeltaMovement().z, getDeltaMovement().x) * (180F / Math.PI)) - 90.0F;
            float wrapped = Mth.wrapDegrees(targetYaw - getYRot());
            zza = 0.5F;
            setYRot(getYRot() + wrapped);
            yBodyRot = getYRot();
            yHeadRot = getYRot();
        } else if (onGround()) {
            setDeltaMovement((random.nextFloat() * 2.0F - 1.0F) * 0.08F, 0.4D, (random.nextFloat() * 2.0F - 1.0F) * 0.08F);
            setYRot(random.nextFloat() * 360.0F);
            hasImpulse = true;
            if (tickCount % 10 == 0) {
                level().playSound(null, blockPosition(), SoundEvents.COD_FLOP, SoundSource.NEUTRAL, getSoundVolume(), 1.0F);
            }
        }
    }

    @Override
    public void travel(net.minecraft.world.phys.Vec3 travelVector) {
        if (isEffectiveAi() && isInWater()) {
            moveRelative(0.01F, travelVector);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.9D));
        } else {
            super.travel(travelVector);
        }
    }

    @Override
    public int getMaxAirSupply() {
        return 300;
    }

    @Override
    protected int increaseAirSupply(int currentAir) {
        return getMaxAirSupply();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        spawnAtLocation(CCItems.CRANBERRY_FISH.get());
        int scales = random.nextInt(3);
        for (int i = 0; i < scales; i++) {
            spawnAtLocation(CCItems.CRANBERRY_SCALE.get());
        }
        if (random.nextInt(50) == 0) {
            spawnAtLocation(CCItems.CRANBERRY_EMBLEM.get());
        }
    }
}
