package com.valentin4311.candycraftmod.block;

import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

public final class PlaceableJellyFoodBlock extends HorizontalDirectionalBlock {
    private static final VoxelShape NORTH_SOUTH_SHAPE = box(2.5D, 0.0D, 3.0D, 13.5D, 5.0D, 14.0D);
    private static final VoxelShape EAST_WEST_SHAPE = box(2.0D, 0.0D, 2.5D, 13.0D, 5.0D, 13.5D);
    private final Supplier<? extends Item> item;
    private final Supplier<? extends ParticleOptions> fragmentParticle;

    public PlaceableJellyFoodBlock(Properties properties, Supplier<? extends Item> item,
            Supplier<? extends ParticleOptions> fragmentParticle) {
        super(properties);
        this.item = item;
        this.fragmentParticle = fragmentParticle;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
                for (AABB box : state.getShape(level, pos).toAabbs()) {
                    int xSteps = Math.max(1, (int) Math.ceil((box.maxX - box.minX) * 4.0D));
                    int ySteps = Math.max(1, (int) Math.ceil((box.maxY - box.minY) * 4.0D));
                    int zSteps = Math.max(1, (int) Math.ceil((box.maxZ - box.minZ) * 4.0D));
                    for (int x = 0; x < xSteps; x++) {
                        for (int y = 0; y < ySteps; y++) {
                            for (int z = 0; z < zSteps; z++) {
                                double localX = box.minX + (x + 0.5D) * (box.maxX - box.minX) / xSteps;
                                double localY = box.minY + (y + 0.5D) * (box.maxY - box.minY) / ySteps;
                                double localZ = box.minZ + (z + 0.5D) * (box.maxZ - box.minZ) / zSteps;
                                double velocityX = (localX - 0.5D) * 0.16D + level.getRandom().nextGaussian() * 0.025D;
                                double velocityY = 0.08D + level.getRandom().nextDouble() * 0.08D;
                                double velocityZ = (localZ - 0.5D) * 0.16D + level.getRandom().nextGaussian() * 0.025D;
                                level.addParticle(fragmentParticle.get(), pos.getX() + localX, pos.getY() + localY,
                                    pos.getZ() + localZ, velocityX, velocityY, velocityZ);
                            }
                        }
                    }
                }
                return true;
            }
        });
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? EAST_WEST_SHAPE : NORTH_SOUTH_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos support = pos.below();
        return level.getBlockState(support).isCollisionShapeFullBlock(level, support);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos)
            ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
            : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(item.get());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
