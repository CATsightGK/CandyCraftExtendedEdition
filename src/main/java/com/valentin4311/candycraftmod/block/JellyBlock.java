package com.valentin4311.candycraftmod.block;

import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JellyBlock extends Block {
    public static final double PURPLE_JUMP_STRENGTH = 2.1D;
    private final double jump;
    private static final ConcurrentHashMap<UUID, Integer> PURPLE_CHARGE_TICKS = new ConcurrentHashMap<>();

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
            entity.resetFallDistance();
            if (!(entity instanceof LivingEntity)) {
                entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());
            }
        } else {
            super.fallOn(level, state, pos, entity, fallDistance);
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (jump != -1.0D && (entity instanceof LivingEntity || entity.isControlledByLocalInstance())) {
            Vec3 movement = entity.getDeltaMovement();
            if (jump == 2.1D) {
                entity.resetFallDistance();
                if (!(entity instanceof Player player)) {
                    return;
                }
                UUID id = player.getUUID();
                PURPLE_CHARGE_TICKS.merge(id, 1, Integer::sum);
                double bob = movement.y <= 0.0D ? Math.sin(player.tickCount * 0.6D) * 0.045D : movement.y;
                entity.setDeltaMovement(movement.x * 0.55D, bob, movement.z * 0.55D);
                entity.hasImpulse = true;
                return;
            }
            if (movement.y <= 0.0D) {
                entity.setDeltaMovement(movement.x, movement.y + jump, movement.z);
                entity.resetFallDistance();
                entity.hasImpulse = true;
            }
        }
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, side);
    }

    public static void releasePurpleJump(Player player) {
        PURPLE_CHARGE_TICKS.remove(player.getUUID());
        Vec3 movement = player.getDeltaMovement();
        player.setDeltaMovement(movement.x * 1.15D, PURPLE_JUMP_STRENGTH, movement.z * 1.15D);
        player.resetFallDistance();
        player.hasImpulse = true;
    }
}
