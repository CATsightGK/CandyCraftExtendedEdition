package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.block.entity.MarshmallowChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class MarshmallowChestBlock extends FacingModelBlock implements EntityBlock {
    public MarshmallowChestBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide || player.isShiftKeyDown()) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (isBlocked(level, pos)) {
            return InteractionResult.CONSUME;
        }

        if (level.getBlockEntity(pos) instanceof MarshmallowChestBlockEntity blockEntity && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, blockEntity, pos);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MarshmallowChestBlockEntity blockEntity) {
            Containers.dropContents(level, pos, blockEntity);
            level.updateNeighbourForOutputSignal(pos, this);
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MarshmallowChestBlockEntity(pos, state);
    }

    private static boolean isBlocked(LevelAccessor level, BlockPos pos) {
        BlockPos above = pos.above();
        return level.getBlockState(above).isRedstoneConductor(level, above);
    }
}
