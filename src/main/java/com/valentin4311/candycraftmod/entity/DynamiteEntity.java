package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class DynamiteEntity extends ThrowableItemProjectile {
    protected static final float EXPLOSION_RADIUS = 3.0F;
    protected int fuse = 65;
    protected boolean chocked;
    protected Direction stuckFace = Direction.UP;

    public DynamiteEntity(EntityType<? extends DynamiteEntity> entityType, Level level) {
        super(entityType, level);
    }

    public DynamiteEntity(Level level, LivingEntity owner) {
        super(CCEntityTypes.DYNAMITE.get(), owner, level);
    }

    public DynamiteEntity(Level level, double x, double y, double z) {
        super(CCEntityTypes.DYNAMITE.get(), x, y, z, level);
    }

    public void setFuse(int fuse) {
        this.fuse = fuse;
    }

    public int getFuse() {
        return fuse;
    }

    @Override
    protected Item getDefaultItem() {
        return CCItems.DYNAMITE.get();
    }

    @Override
    protected float getGravity() {
        return super.getGravity();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            for (int i = 0; i < 2; i++) {
                level().addParticle(ParticleTypes.LARGE_SMOKE,
                    getX() + (random.nextDouble() - 0.5D) * getBbWidth(),
                    getY() + random.nextDouble() * getBbHeight(),
                    getZ() + (random.nextDouble() - 0.5D) * getBbWidth(),
                    0.0D, 0.0D, 0.0D);
            }
        }

        if (!level().isClientSide && --fuse <= 0) {
            explode();
        }
    }

    protected void explode() {
        spawnExplosionParticles();
        boolean mobGriefing = level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        level().explode(this, getX(), getY(), getZ(), EXPLOSION_RADIUS,
            mobGriefing ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
        discard();
    }

    protected void spawnExplosionParticles() {
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new net.minecraft.world.item.ItemStack(CCItems.NOUGAT_POWDER.get())),
                getX(), getY(), getZ(), 60, 0.9D, 0.7D, 0.9D, 0.18D);
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.ENTITY) {
            onEntityImpact((EntityHitResult) result);
        } else if (result.getType() == HitResult.Type.BLOCK) {
            onBlockImpact((BlockHitResult) result);
        }
    }

    protected void onEntityImpact(EntityHitResult result) {
        if (level().isClientSide || chocked) {
            return;
        }
        Entity entity = result.getEntity();
        Entity owner = getOwner();
        entity.hurt(damageSources().thrown(this, owner), 0.0F);
        if (entity instanceof LivingEntity) {
            setDeltaMovement(Vec3.ZERO);
            chocked = true;
        }
    }

    protected void onBlockImpact(BlockHitResult result) {
        if (level().isClientSide) {
            return;
        }
        BlockState state = level().getBlockState(result.getBlockPos());
        if (state.getCollisionShape(level(), result.getBlockPos()).isEmpty()) {
            return;
        }
        chockAt(result);
    }

    protected void chockAt(BlockHitResult result) {
        Vec3 motion = getDeltaMovement();
        Direction face = result.getDirection();
        Vec3 center = surfaceCenter(result);
        chocked = true;
        stuckFace = face;
        noPhysics = false;
        setPos(center.x, center.y, center.z);
        xo = center.x;
        yo = center.y;
        zo = center.z;
        setBoundingBox(makeBoundingBox(center));
        if (face == Direction.NORTH || face == Direction.SOUTH) {
            setDeltaMovement(motion.x * 0.1D, 0.0D, 0.0D);
        } else if (face == Direction.EAST || face == Direction.WEST) {
            setDeltaMovement(0.0D, 0.0D, motion.z * 0.1D);
        } else if (face == Direction.UP) {
            setDeltaMovement(motion.x * 0.1D, 0.0D, motion.z * 0.1D);
        } else {
            setDeltaMovement(motion.x * 0.2D, 0.0D, motion.z * 0.2D);
        }
    }

    protected Vec3 surfaceCenter(BlockHitResult result) {
        Vec3 hit = result.getLocation();
        double inset = getBbWidth() * 0.5D + 0.002D;
        return hit.add(
            result.getDirection().getStepX() * inset,
            result.getDirection().getStepY() * inset,
            result.getDirection().getStepZ() * inset
        );
    }

    protected AABB makeBoundingBox(Vec3 center) {
        double halfWidth = getBbWidth() * 0.5D;
        double halfHeight = getBbHeight() * 0.5D;
        return new AABB(
            center.x - halfWidth,
            center.y - halfHeight,
            center.z - halfWidth,
            center.x + halfWidth,
            center.y + halfHeight,
            center.z + halfWidth
        );
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Fuse", fuse);
        tag.putBoolean("Chocked", chocked);
        tag.putInt("StuckFace", stuckFace.get3DDataValue());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        fuse = tag.getInt("Fuse");
        chocked = tag.getBoolean("Chocked");
        if (tag.contains("Stopped")) {
            chocked = tag.getBoolean("Stopped");
        }
        noPhysics = false;
        stuckFace = Direction.from3DDataValue(tag.getInt("StuckFace"));
    }
}
