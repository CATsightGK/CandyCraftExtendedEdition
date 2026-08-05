package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.entity.PurpleJellyStuckEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class JellyBlock extends Block {
    public static final double PURPLE_JUMP_STRENGTH = 2.1D;
    private final double jump;

    public JellyBlock(double jump, BlockBehaviour.Properties properties) {
        super(properties);
        this.jump = jump;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return jump == -1.0D ? super.getCollisionShape(state, level, pos, context) : box(0.0D, 0.0D, 0.0D, 16.0D, 15.92D, 16.0D);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (jump == -1.0D || jump == 2.1D) {
            if (entity instanceof LivingEntity) {
                entity.resetFallDistance();
                ((PurpleJellyStuckEntity)entity).candycraft$setPurpleJellyStuck();
            } else {
                super.fallOn(level, state, pos, entity, fallDistance);
            }
        } else {
            super.fallOn(level, state, pos, entity, fallDistance);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (jump != -1.0D && entity instanceof LivingEntity && !entity.isShiftKeyDown()) {
            Vec3 movement = entity.getDeltaMovement();
            if (movement.y <= 0.0D) {
                entity.setDeltaMovement(movement.x, movement.y + jump, movement.z);
                if (jump == 1.0D) {
                    Vec3 bounced = entity.getDeltaMovement();
                    entity.setDeltaMovement(
                        Math.abs(bounced.x) < 0.25D ? bounced.x * 4.0D : bounced.x,
                        bounced.y,
                        Math.abs(bounced.z) < 0.25D ? bounced.z * 4.0D : bounced.z
                    );
                }
                entity.resetFallDistance();
                entity.hasImpulse = true;
            }
        }
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, side);
    }

}
