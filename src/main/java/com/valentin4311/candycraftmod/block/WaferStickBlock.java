package com.valentin4311.candycraftmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WaferStickBlock extends RotatedPillarBlock {
    private static final VoxelShape SHAPE = Shapes.join(
        Shapes.join(
            box(3.0D, 0.0D, 13.0D, 13.0D, 16.0D, 16.0D),
            box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D),
            BooleanOp.OR
        ),
        Shapes.join(
            box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D),
            box(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 3.0D),
            BooleanOp.OR
        ),
        BooleanOp.OR
    );

    public WaferStickBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, side);
    }
}
