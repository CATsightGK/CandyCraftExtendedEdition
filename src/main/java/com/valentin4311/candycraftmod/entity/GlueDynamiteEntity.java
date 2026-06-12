package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class GlueDynamiteEntity extends DynamiteEntity {
    private static final int STUCK_FUSE_TICKS = 65;
    private static final DustParticleOptions GLUE_PARTICLE = new DustParticleOptions(new Vector3f(0.64F, 0.32F, 0.86F), 1.0F);
    private static final EntityDataAccessor<Boolean> GLUED = SynchedEntityData.defineId(GlueDynamiteEntity.class, EntityDataSerializers.BOOLEAN);
    private Entity stuckEntity;

    public GlueDynamiteEntity(EntityType<? extends GlueDynamiteEntity> entityType, Level level) {
        super(entityType, level);
    }

    public GlueDynamiteEntity(Level level, LivingEntity owner) {
        super(CCEntityTypes.GLUE_DYNAMITE.get(), level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
    }

    public GlueDynamiteEntity(Level level, double x, double y, double z) {
        super(CCEntityTypes.GLUE_DYNAMITE.get(), level);
        setPos(x, y, z);
    }

    @Override
    protected Item getDefaultItem() {
        return CCItems.GLUE_DYNAMITE.get();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(GLUED, false);
    }

    @Override
    protected float getGravity() {
        return isGlued() ? 0.0F : super.getGravity();
    }

    @Override
    public void tick() {
        if (isGlued()) {
            noPhysics = true;
            if (stuckEntity != null && stuckEntity.isAlive()) {
                Vec3 center = stuckEntity.position().add(0.0D, stuckEntity.getBbHeight() * 0.6D, 0.0D);
                setPos(center.x, center.y, center.z);
                setBoundingBox(makeBoundingBox(center));
            }
            setDeltaMovement(Vec3.ZERO);

            if (level().isClientSide) {
                for (int i = 0; i < 2; i++) {
                    level().addParticle(ParticleTypes.LARGE_SMOKE,
                        getX() + (random.nextDouble() - 0.5D) * getBbWidth(),
                        getY() + random.nextDouble() * getBbHeight(),
                        getZ() + (random.nextDouble() - 0.5D) * getBbWidth(),
                        0.0D, 0.0D, 0.0D);
                }
                for (int i = 0; i < 10; i++) {
                    level().addParticle(GLUE_PARTICLE,
                        getX() + (random.nextDouble() * 2.0D - 1.0D) * getBbWidth(),
                        getY() + random.nextDouble() * getBbHeight(),
                        getZ() + (random.nextDouble() * 2.0D - 1.0D) * getBbWidth(),
                        0.0D, 0.0D, 0.0D);
                }
            }

            if (!level().isClientSide && --fuse <= 0) {
                explode();
            }
            return;
        }

        super.tick();
    }

    @Override
    protected void onEntityImpact(EntityHitResult result) {
        if (level().isClientSide || chocked) {
            return;
        }
        Entity entity = result.getEntity();
        entity.hurt(damageSources().thrown(this, getOwner()), 0.0F);
        if (entity instanceof LivingEntity) {
            stuckEntity = entity;
            stickAt(entity.position().add(0.0D, entity.getBbHeight() * 0.6D, 0.0D), Direction.UP);
        }
    }

    @Override
    protected void onBlockImpact(BlockHitResult result) {
        if (chocked || level().getBlockState(result.getBlockPos()).getCollisionShape(level(), result.getBlockPos()).isEmpty()) {
            return;
        }
        stickAt(surfaceCenter(result), result.getDirection());
    }

    protected void stickAt(Vec3 center, Direction face) {
        chocked = true;
        entityData.set(GLUED, true);
        stuckFace = face;
        fuse = Math.max(fuse, STUCK_FUSE_TICKS);
        noPhysics = true;
        setDeltaMovement(Vec3.ZERO);
        setPos(center.x, center.y, center.z);
        xo = center.x;
        yo = center.y;
        zo = center.z;
        setBoundingBox(makeBoundingBox(center));
    }

    private boolean isGlued() {
        return entityData.get(GLUED);
    }

    @Override
    protected void spawnExplosionParticles() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new net.minecraft.world.item.ItemStack(CCItems.CHEWING_GUM.get())),
                getX(), getY(), getZ(), 70, 0.9D, 0.7D, 0.9D, 0.18D);
            serverLevel.sendParticles(GLUE_PARTICLE, getX(), getY(), getZ(), 50, 0.9D, 0.7D, 0.9D, 0.08D);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Glued", isGlued());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(GLUED, tag.getBoolean("Glued"));
        noPhysics = isGlued();
    }
}
