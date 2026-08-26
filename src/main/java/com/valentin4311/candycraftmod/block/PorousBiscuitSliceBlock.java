package com.valentin4311.candycraftmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PorousBiscuitSliceBlock extends Block implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape SHAPE = Shapes.or(
        box(1.0D, 0.0D, 1.0D, 15.0D, 1.0D, 15.0D),
        box(0.25D, 1.0D, 0.25D, 15.75D, 2.0D, 15.75D),
        box(1.0D, 2.0D, 1.0D, 15.0D, 3.0D, 15.0D),
        box(0.0D, 0.25D, 1.0D, 1.0D, 2.75D, 3.0D),
        box(0.0D, 0.25D, 5.0D, 1.0D, 2.75D, 7.0D),
        box(0.0D, 0.25D, 9.0D, 1.0D, 2.75D, 11.0D),
        box(0.0D, 0.25D, 13.0D, 1.0D, 2.75D, 15.0D),
        box(1.0D, 0.25D, 15.0D, 3.0D, 2.75D, 16.0D),
        box(5.0D, 0.25D, 15.0D, 7.0D, 2.75D, 16.0D),
        box(9.0D, 0.25D, 15.0D, 11.0D, 2.75D, 16.0D),
        box(13.0D, 0.25D, 15.0D, 15.0D, 2.75D, 16.0D),
        box(15.0D, 0.25D, 13.0D, 16.0D, 2.75D, 15.0D),
        box(15.0D, 0.25D, 9.0D, 16.0D, 2.75D, 11.0D),
        box(15.0D, 0.25D, 5.0D, 16.0D, 2.75D, 7.0D),
        box(15.0D, 0.25D, 1.0D, 16.0D, 2.75D, 3.0D),
        box(13.0D, 0.25D, 0.0D, 15.0D, 2.75D, 1.0D),
        box(9.0D, 0.25D, 0.0D, 11.0D, 2.75D, 1.0D),
        box(5.0D, 0.25D, 0.0D, 7.0D, 2.75D, 1.0D),
        box(1.0D, 0.25D, 0.0D, 3.0D, 2.75D, 1.0D)
    ).optimize();

    public PorousBiscuitSliceBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, net.minecraft.core.Direction direction,
                                  BlockState neighborState, LevelAccessor level, BlockPos pos,
                                  BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                        CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }
}
