package com.valentin4311.candycraftmod.mixin;

import com.valentin4311.candycraftmod.entity.CandyStuckProjectileCarrier;
import com.valentin4311.candycraftmod.entity.PropolisSurfaceCarrier;
import com.valentin4311.candycraftmod.registry.CCFluids;
import com.valentin4311.candycraftmod.registry.CCMobEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements CandyStuckProjectileCarrier, PropolisSurfaceCarrier {
    private static final double MAX_CHOCOLATE_SURFACE_DEPTH = 0.35D;
    private static final float MIN_MOVEMENT_INPUT = 0.01F;
    private static final int STUCK_PROJECTILE_LIFETIME = 20 * 60;
    @Unique
    private static final EntityDataAccessor<Integer> CANDYCRAFT_HONEY_ARROWS =
        SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Integer> CANDYCRAFT_HONEY_BOLTS =
        SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.INT);
    @Unique
    private static final EntityDataAccessor<Boolean> CANDYCRAFT_PROPOLIS_SURFACE =
        SynchedEntityData.defineId(LivingEntity.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private int candycraft$removeStuckProjectileTime;
    @Unique
    private boolean candycraft$hasStuckProjectiles;

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void candycraft$defineStuckProjectileData(CallbackInfo callback) {
        LivingEntity entity = (LivingEntity)(Object)this;
        entity.getEntityData().define(CANDYCRAFT_HONEY_ARROWS, 0);
        entity.getEntityData().define(CANDYCRAFT_HONEY_BOLTS, 0);
        entity.getEntityData().define(CANDYCRAFT_PROPOLIS_SURFACE, false);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void candycraft$tickStuckProjectiles(CallbackInfo callback) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (entity.level().isClientSide) {
            return;
        }

        boolean hasPropolis = entity.hasEffect(CCMobEffects.HONEY_GLUE.get());
        if (candycraft$hasPropolisSurface() != hasPropolis) {
            entity.getEntityData().set(CANDYCRAFT_PROPOLIS_SURFACE, hasPropolis);
        }

        if (!candycraft$hasStuckProjectiles) {
            return;
        }
        int arrowCount = candycraft$getHoneyArrowCount();
        int boltCount = candycraft$getHoneyBoltCount();
        if (arrowCount + boltCount <= 0) {
            candycraft$hasStuckProjectiles = false;
            candycraft$removeStuckProjectileTime = 0;
            return;
        }

        if (++candycraft$removeStuckProjectileTime >= STUCK_PROJECTILE_LIFETIME) {
            candycraft$removeStuckProjectileTime = 0;
            if (arrowCount > 0) {
                candycraft$setHoneyArrowCount(arrowCount - 1);
            } else {
                candycraft$setHoneyBoltCount(boltCount - 1);
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void candycraft$saveStuckProjectiles(CompoundTag tag, CallbackInfo callback) {
        int arrowCount = candycraft$getHoneyArrowCount();
        int boltCount = candycraft$getHoneyBoltCount();
        if (arrowCount > 0) {
            tag.putInt("CandyCraftHoneyArrows", arrowCount);
        }
        if (boltCount > 0) {
            tag.putInt("CandyCraftHoneyBolts", boltCount);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void candycraft$loadStuckProjectiles(CompoundTag tag, CallbackInfo callback) {
        candycraft$setHoneyArrowCount(tag.getInt("CandyCraftHoneyArrows"));
        candycraft$setHoneyBoltCount(tag.getInt("CandyCraftHoneyBolts"));
    }

    @Override
    public int candycraft$getHoneyArrowCount() {
        return ((LivingEntity)(Object)this).getEntityData().get(CANDYCRAFT_HONEY_ARROWS);
    }

    @Override
    public void candycraft$setHoneyArrowCount(int count) {
        int clampedCount = Math.max(0, count);
        ((LivingEntity)(Object)this).getEntityData().set(CANDYCRAFT_HONEY_ARROWS, clampedCount);
        candycraft$hasStuckProjectiles = clampedCount > 0 || candycraft$getHoneyBoltCount() > 0;
    }

    @Override
    public int candycraft$getHoneyBoltCount() {
        return ((LivingEntity)(Object)this).getEntityData().get(CANDYCRAFT_HONEY_BOLTS);
    }

    @Override
    public void candycraft$setHoneyBoltCount(int count) {
        int clampedCount = Math.max(0, count);
        ((LivingEntity)(Object)this).getEntityData().set(CANDYCRAFT_HONEY_BOLTS, clampedCount);
        candycraft$hasStuckProjectiles = clampedCount > 0 || candycraft$getHoneyArrowCount() > 0;
    }

    @Override
    public boolean candycraft$hasPropolisSurface() {
        return ((LivingEntity)(Object)this).getEntityData().get(CANDYCRAFT_PROPOLIS_SURFACE);
    }

    @Inject(method = "canStandOnFluid", at = @At("HEAD"), cancellable = true)
    private void candycraft$walkOnChocolateWhileMoving(FluidState fluidState,
            CallbackInfoReturnable<Boolean> callback) {
        if (fluidState.getFluidType() != CCFluids.LIQUID_CHOCOLATE_TYPE.get()) {
            return;
        }

        LivingEntity entity = (LivingEntity)(Object)this;
        if (!(entity instanceof Player player)
            || player.isShiftKeyDown()
            || player.isSwimming()
            || player.getEyeInFluidType() == CCFluids.LIQUID_CHOCOLATE_TYPE.get()
            || player.getFluidTypeHeight(CCFluids.LIQUID_CHOCOLATE_TYPE.get()) > MAX_CHOCOLATE_SURFACE_DEPTH) {
            return;
        }

        if (Math.abs(player.xxa) > MIN_MOVEMENT_INPUT || Math.abs(player.zza) > MIN_MOVEMENT_INPUT) {
            callback.setReturnValue(true);
        }
    }
}
