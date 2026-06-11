package com.valentin4311.candycraftmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CherryBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    private static final VoxelShape UP_SHAPE = Block.box(4.8D, 9.6D, 4.8D, 11.2D, 16.0D, 11.2D);
    private static final VoxelShape DOWN_SHAPE = Block.box(4.8D, 0.0D, 4.8D, 11.2D, 6.4D, 11.2D);
    private static final VoxelShape NORTH_SHAPE = Block.box(4.8D, 4.8D, 0.0D, 11.2D, 11.2D, 6.4D);
    private static final VoxelShape SOUTH_SHAPE = Block.box(4.8D, 4.8D, 9.6D, 11.2D, 11.2D, 16.0D);
    private static final VoxelShape WEST_SHAPE = Block.box(0.0D, 4.8D, 4.8D, 6.4D, 11.2D, 11.2D);
    private static final VoxelShape EAST_SHAPE = Block.box(9.6D, 4.8D, 4.8D, 16.0D, 11.2D, 11.2D);

    public CherryBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (face.getAxis().isVertical() || !isValidSupport(context.getLevel().getBlockState(context.getClickedPos()))) {
            return null;
        }
        return defaultBlockState().setValue(FACING, face.getOpposite());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction supportDirection = state.getValue(FACING);
        BlockPos supportPos = pos.relative(supportDirection);
        BlockState supportState = level.getBlockState(supportPos);
        return supportDirection.getAxis().isHorizontal() && isValidSupport(supportState);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == state.getValue(FACING) && !canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(FACING));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    private static VoxelShape shapeFor(Direction direction) {
        return switch (direction) {
            case DOWN -> DOWN_SHAPE;
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> UP_SHAPE;
        };
    }

    public static boolean isValidSupport(BlockState state) {
        return state.is(CCBlocks.MARSHMALLOW_LOG.get())
            || state.is(CCBlocks.MARSHMALLOW_LOG_DARK.get())
            || state.is(CCBlocks.MARSHMALLOW_LOG_LIGHT.get());
    }
}
