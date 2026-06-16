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
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JellyBlock extends Block {
    private final double jump;
    private static final ConcurrentHashMap<UUID, Integer> PURPLE_CHARGE_TICKS = new ConcurrentHashMap<>();
    private static Field jumpingField;

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
                int charge = PURPLE_CHARGE_TICKS.merge(id, 1, Integer::sum);
                boolean wantsJump = isPressingJump(player) || player.getDeltaMovement().y > 0.08D;
                if (!wantsJump) {
                    double bob = level.isClientSide ? Math.sin((player.tickCount + charge) * 0.7D) * 0.015D : 0.0D;
                    entity.setDeltaMovement(movement.x * 0.70D, Math.min(movement.y, 0.0D) + bob, movement.z * 0.70D);
                    entity.hasImpulse = true;
                    return;
                }
                PURPLE_CHARGE_TICKS.remove(id);
                entity.setDeltaMovement(movement.x * 1.15D, Math.max(1.15D, movement.y + 1.05D), movement.z * 1.15D);
                entity.resetFallDistance();
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

    private static boolean isPressingJump(Player player) {
        try {
            if (jumpingField == null) {
                jumpingField = LivingEntity.class.getDeclaredField("jumping");
                jumpingField.setAccessible(true);
            }
            return jumpingField.getBoolean(player);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }
}
