package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.registry.CCMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpikesBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 9.6D, 16.0D);
    private final int damage;
    private final boolean appliesCloying;

    public SpikesBlock(int damage, BlockBehaviour.Properties properties) {
        this(damage, false, properties);
    }

    public SpikesBlock(int damage, boolean appliesCloying, BlockBehaviour.Properties properties) {
        super(properties);
        this.damage = damage;
        this.appliesCloying = appliesCloying;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        hurtEntity(level, entity);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        hurtEntity(level, entity);
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        hurtEntity(level, entity);
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    private void hurtEntity(Level level, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            entity.hurt(level.damageSources().generic(), damage / 2.0F);
            if (appliesCloying) {
                living.addEffect(new MobEffectInstance(CCMobEffects.CLOYING.get(), 5 * 20));
            }
        }
    }
}
