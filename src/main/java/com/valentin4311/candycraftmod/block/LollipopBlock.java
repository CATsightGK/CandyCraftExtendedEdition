package com.valentin4311.candycraftmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class LollipopBlock extends BushBlock {
    public LollipopBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return !state.isAir();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        BlockPos stemPos = pos.below();
        BlockState stemState = level.getBlockState(stemPos);
        if (stemState.getBlock() instanceof LollipopPlantBlock plant && plant.isMaxAge(stemState)) {
            return plant.use(stemState, level, stemPos, player, hand, hit);
        }
        return super.use(state, level, pos, player, hand, hit);
    }
}
