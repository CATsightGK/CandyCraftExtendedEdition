package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CherryLeavesBlock extends LegacyLeavesBlock {
    public CherryLeavesBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos fruitPos = pos.below();
        if (level.isEmptyBlock(fruitPos) && random.nextInt(100) == 0) {
            level.setBlockAndUpdate(fruitPos, CCBlocks.CHERRY_BLOCK.get().defaultBlockState().setValue(CherryBlock.FACING, Direction.UP));
        }
    }
}
