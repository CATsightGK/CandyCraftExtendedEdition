package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCSweetscapeBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class SeaweedBlock extends LegacyMetadataBlock.Plant implements SimpleWaterloggedBlock {
    private final boolean canStack;

    public SeaweedBlock(boolean canStack, BlockBehaviour.Properties properties) {
        super(properties);
        this.canStack = canStack;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        BlockState above = level.getBlockState(pos.above());
        boolean bottomValid = below.is(CCBlocks.FLOUR.get())
            || below.is(CCBlocks.PUDDING.get())
            || below.is(CCBlocks.CANDY_FARMLAND.get())
            || below.is(CCSweetscapeBlocks.SUGAR_SAND.get())
            || canStack && below.is(this);
        boolean topValid = above.getFluidState().is(FluidTags.WATER) || canStack && above.is(this);
        return bottomValid && topValid && level.getFluidState(pos).is(FluidTags.WATER);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        if (!state.canSurvive(level, pos)) {
            return Blocks.WATER.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return Fluids.WATER.getSource(false);
    }
}
