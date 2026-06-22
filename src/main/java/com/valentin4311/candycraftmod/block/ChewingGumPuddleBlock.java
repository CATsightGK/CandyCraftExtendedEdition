package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.util.EmblemHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChewingGumPuddleBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 0.25D, 16.0D);

    public ChewingGumPuddleBlock(BlockBehaviour.Properties properties) {
        super(properties);
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
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), net.minecraft.core.Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !canSurvive(state, level, pos)) {
            if (level instanceof Level realLevel && !realLevel.isClientSide) {
                popResource(realLevel, pos, new ItemStack(CCItems.CHEWING_GUM.get()));
            }
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide && entity.onGround() && entity.getDeltaMovement().horizontalDistanceSqr() > 0.001D) {
            spawnStepParticle(state, level, pos, entity);
        }
        if (entity instanceof Player player && EmblemHelper.has(player, CCItems.CHEWING_GUM_EMBLEM.get())) {
            return;
        }
        Vec3 movement = entity.getDeltaMovement();
        entity.setDeltaMovement(movement.x * 0.2D, Math.min(0.0D, movement.y), movement.z * 0.2D);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(10) == 0) {
            double x = pos.getX() + 0.15D + random.nextDouble() * 0.7D;
            double z = pos.getZ() + 0.15D + random.nextDouble() * 0.7D;
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), x, pos.getY() + 0.04D, z, 0.0D, 0.015D, 0.0D);
        }
    }

    private static void spawnStepParticle(BlockState state, Level level, BlockPos pos, Entity entity) {
        RandomSource random = level.random;
        double x = entity.getX() + (random.nextDouble() - 0.5D) * entity.getBbWidth();
        double z = entity.getZ() + (random.nextDouble() - 0.5D) * entity.getBbWidth();
        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), x, pos.getY() + 0.04D, z, 0.0D, 0.025D, 0.0D);
    }
}
