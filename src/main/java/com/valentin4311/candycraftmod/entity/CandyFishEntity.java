package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

public class CandyFishEntity extends AbstractSchoolingFish {
    public float current;

    public CandyFishEntity(EntityType<? extends CandyFishEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSchoolingFish.createAttributes()
            .add(Attributes.MAX_HEALTH, 10.0D)
            .add(Attributes.MOVEMENT_SPEED, 2.0D);
    }

    public static boolean canSpawn(EntityType<CandyFishEntity> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, net.minecraft.util.RandomSource random) {
        FluidState fluid = level.getFluidState(pos);
        return pos.getY() > 45 && pos.getY() < 60 && fluid.is(FluidTags.WATER) && level.getFluidState(pos.above()).is(FluidTags.WATER);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new StayBelowSurfaceGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        current += 0.2F;
        if (!level().isClientSide && isInWater() && !level().getFluidState(blockPosition().above()).is(FluidTags.WATER)) {
            setDeltaMovement(getDeltaMovement().add(0.0D, -0.04D, 0.0D));
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
    public ItemStack getBucketItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.COD_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.COD_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.COD_DEATH;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.COD_FLOP;
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

    private static final class StayBelowSurfaceGoal extends Goal {
        private final CandyFishEntity fish;
        private Vec3 target;

        private StayBelowSurfaceGoal(CandyFishEntity fish) {
            this.fish = fish;
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!fish.isInWater() || fish.getRandom().nextInt(12) != 0) {
                return false;
            }
            target = findSubmergedTarget();
            return target != null;
        }

        @Override
        public boolean canContinueToUse() {
            return target != null && fish.isInWater() && !fish.getNavigation().isDone();
        }

        @Override
        public void start() {
            fish.getNavigation().moveTo(target.x, target.y, target.z, 1.0D);
        }

        @Override
        public void stop() {
            target = null;
        }

        private Vec3 findSubmergedTarget() {
            BlockPos origin = fish.blockPosition();
            for (int tries = 0; tries < 12; tries++) {
                int x = origin.getX() + fish.getRandom().nextInt(17) - 8;
                int z = origin.getZ() + fish.getRandom().nextInt(17) - 8;
                int y = origin.getY() + fish.getRandom().nextInt(7) - 5;
                BlockPos pos = new BlockPos(x, Math.max(46, Math.min(59, y)), z);
                if (isGoodWater(pos)) {
                    return Vec3.atCenterOf(pos);
                }
            }
            BlockPos down = origin.below(2);
            return isGoodWater(down) ? Vec3.atCenterOf(down) : null;
        }

        private boolean isGoodWater(BlockPos pos) {
            return fish.level().getFluidState(pos).is(FluidTags.WATER)
                && fish.level().getFluidState(pos.above()).is(FluidTags.WATER)
                && fish.level().getFluidState(pos.above(2)).is(FluidTags.WATER);
        }
    }
}
