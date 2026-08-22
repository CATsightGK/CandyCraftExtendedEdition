package com.valentin4311.candycraftmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;

/**
 * Marshmallow rope: climbable from all four sides and visually connects to
 * neighbouring marshmallow ropes facing a different axis, so horizontal and
 * vertical sections join without a gap at the corner. Parallel ropes of the
 * same axis never grow connector stubs.
 */
public class MarshmallowRopeBlock extends ChainBlock {
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;

    public MarshmallowRopeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
            .setValue(UP, Boolean.FALSE)
            .setValue(DOWN, Boolean.FALSE)
            .setValue(NORTH, Boolean.FALSE)
            .setValue(SOUTH, Boolean.FALSE)
            .setValue(EAST, Boolean.FALSE)
            .setValue(WEST, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(UP, DOWN, NORTH, SOUTH, EAST, WEST);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withConnections(super.getStateForPlacement(context), context.getLevel(), context.getClickedPos());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        // Directions along the rope's own axis are already covered by the
        // full-length model; only perpendicular faces need a connector stub,
        // and only when the neighbouring rope faces a different axis.
        if (direction.getAxis() == state.getValue(AXIS)) {
            return state;
        }
        return state.setValue(connectionProperty(direction), connectsTo(state, neighborState));
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return true;
    }

    private BlockState withConnections(BlockState state, BlockGetter level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != state.getValue(AXIS)) {
                state = state.setValue(connectionProperty(direction), connectsTo(state, level.getBlockState(pos.relative(direction))));
            }
        }
        return state;
    }

    private static boolean connectsTo(BlockState state, BlockState neighborState) {
        return neighborState.is(state.getBlock()) && neighborState.getValue(AXIS) != state.getValue(AXIS);
    }

    private static BooleanProperty connectionProperty(Direction direction) {
        return switch (direction) {
            case UP -> UP;
            case DOWN -> DOWN;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
        };
    }
}
