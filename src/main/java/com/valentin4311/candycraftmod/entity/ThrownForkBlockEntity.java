package com.valentin4311.candycraftmod.entity;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class ThrownForkBlockEntity extends ThrowableProjectile {
    private static final float ENTITY_HIT_DAMAGE = 4.0F;
    private static final EntityDataAccessor<BlockState> BLOCK_STATE = SynchedEntityData.defineId(
        ThrownForkBlockEntity.class,
        EntityDataSerializers.BLOCK_STATE
    );

    public ThrownForkBlockEntity(EntityType<? extends ThrownForkBlockEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownForkBlockEntity(Level level, LivingEntity owner, BlockState state) {
        super(CCEntityTypes.THROWN_FORK_BLOCK.get(), owner, level);
        setBlockState(state);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(BLOCK_STATE, Blocks.AIR.defaultBlockState());
    }

    public BlockState getBlockState() {
        return entityData.get(BLOCK_STATE);
    }

    private void setBlockState(BlockState state) {
        entityData.set(BLOCK_STATE, state);
    }

    @Override
    protected float getGravity() {
        return 0.02F;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount > 100) {
            shatter();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        if (level().isClientSide) {
            return;
        }
        BlockState state = getBlockState();
        if (state.getBlock() instanceof BushBlock || !placeBlock(state, hitResult)) {
            shatter();
            return;
        }
        discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (!level().isClientSide) {
            hitResult.getEntity().hurt(damageSources().thrown(this, getOwner()), ENTITY_HIT_DAMAGE);
            shatter();
        }
    }

    private boolean placeBlock(BlockState state, BlockHitResult hitResult) {
        if (!(state.getBlock().asItem() instanceof BlockItem blockItem) || !(getOwner() instanceof Player player)) {
            return false;
        }
        ItemStack placementStack = new ItemStack(blockItem);
        BlockPlaceContext context = new BlockPlaceContext(
            level(),
            player,
            InteractionHand.MAIN_HAND,
            placementStack,
            hitResult
        );
        InteractionResult result = blockItem.place(context);
        return result.consumesAction();
    }

    private void shatter() {
        BlockState state = getBlockState();
        if (level() instanceof ServerLevel serverLevel && !state.isAir()) {
            serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, state),
                getX(), getY(), getZ(),
                28,
                0.28D, 0.28D, 0.28D,
                0.08D
            );
            level().playSound(
                null,
                blockPosition(),
                state.getSoundType(level(), blockPosition(), this).getBreakSound(),
                SoundSource.BLOCKS,
                0.8F,
                1.1F
            );
        }
        discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("BlockState", NbtUtils.writeBlockState(getBlockState()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("BlockState", Tag.TAG_COMPOUND)) {
            setBlockState(NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("BlockState")));
        }
    }
}
