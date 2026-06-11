package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.core.Direction;
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
    private static final DustParticleOptions GLUE_PARTICLE = new DustParticleOptions(new Vector3f(1.0F, 0.5F, 0.5F), 1.0F);
    private Entity stuckEntity;
    private boolean glued;

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
    protected float getGravity() {
        return glued ? 0.0F : super.getGravity();
    }

    @Override
    public void tick() {
        if (stuckEntity != null && stuckEntity.isAlive()) {
            Vec3 center = stuckEntity.position().add(0.0D, stuckEntity.getBbHeight() * 0.6D, 0.0D);
            setPos(center.x, center.y, center.z);
            setBoundingBox(makeBoundingBox(center));
            setDeltaMovement(Vec3.ZERO);
        }

        super.tick();

        if (glued) {
            setDeltaMovement(Vec3.ZERO);
            if (level().isClientSide) {
                for (int i = 0; i < 10; i++) {
                    level().addParticle(GLUE_PARTICLE,
                        getX() + (random.nextDouble() * 2.0D - 1.0D) * getBbWidth(),
                        getY() + random.nextDouble() * getBbHeight(),
                        getZ() + (random.nextDouble() * 2.0D - 1.0D) * getBbWidth(),
                        0.0D, 0.0D, 0.0D);
                }
            }
        }
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
            glued = true;
            stickAt(entity.position().add(0.0D, entity.getBbHeight() * 0.6D, 0.0D), Direction.UP);
        }
    }

    @Override
    protected void onBlockImpact(BlockHitResult result) {
        if (level().isClientSide || chocked || level().getBlockState(result.getBlockPos()).getCollisionShape(level(), result.getBlockPos()).isEmpty()) {
            return;
        }
        glued = true;
        stickAt(surfaceCenter(result), result.getDirection());
    }

    protected void stickAt(Vec3 center, Direction face) {
        chocked = true;
        glued = true;
        stuckFace = face;
        noPhysics = true;
        setDeltaMovement(Vec3.ZERO);
        setPos(center.x, center.y, center.z);
        xo = center.x;
        yo = center.y;
        zo = center.z;
        setBoundingBox(makeBoundingBox(center));
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
        tag.putBoolean("Glued", glued);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        glued = tag.getBoolean("Glued");
    }
}
