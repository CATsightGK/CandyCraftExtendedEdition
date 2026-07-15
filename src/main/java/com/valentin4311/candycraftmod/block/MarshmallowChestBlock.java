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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class MarshmallowChestBlock extends FacingModelBlock implements EntityBlock {
    private final Theme theme;

    public MarshmallowChestBlock(Theme theme, BlockBehaviour.Properties properties) {
        super(properties);
        this.theme = theme;
    }

    public Theme theme() {
        return theme;
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

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != com.valentin4311.candycraftmod.registry.CCBlockEntities.MARSHMALLOW_CHEST.get()) {
            return null;
        }
        return (tickerLevel, pos, tickerState, blockEntity) ->
            MarshmallowChestBlockEntity.lidAnimateTick(tickerLevel, pos, tickerState, (MarshmallowChestBlockEntity) blockEntity);
    }

    private static boolean isBlocked(LevelAccessor level, BlockPos pos) {
        BlockPos above = pos.above();
        return level.getBlockState(above).isRedstoneConductor(level, above);
    }

    public enum Theme {
        NORMAL("marshmallow_chest", "marshmallow_chest_normal"),
        DARK("marshmallow_chest_dark", "marshmallow_chest_dark"),
        LIGHT("marshmallow_chest_light", "marshmallow_chest_light");

        private final String serializedName;
        private final String textureName;

        Theme(String serializedName, String textureName) {
            this.serializedName = serializedName;
            this.textureName = textureName;
        }

        public String serializedName() {
            return serializedName;
        }

        public String textureName() {
            return textureName;
        }
    }
}
