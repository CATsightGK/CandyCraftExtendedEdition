package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.block.entity.LicoriceFurnaceBlockEntity;
import com.valentin4311.candycraftmod.registry.CCBlockEntities;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class LicoriceFurnaceBlock extends FacingModelBlock implements EntityBlock {
    private final boolean lit;

    public LicoriceFurnaceBlock(boolean lit, BlockBehaviour.Properties properties) {
        super(properties);
        this.lit = lit;
    }

    public boolean isLit() {
        return lit;
    }

    public static void setLit(Level level, BlockPos pos, BlockState state, boolean lit) {
        BlockState newState = (lit ? CCBlocks.LICORICE_FURNACE_ON.get() : CCBlocks.LICORICE_FURNACE.get())
            .defaultBlockState()
            .setValue(FACING, state.getValue(FACING));
        level.setBlock(pos, newState, 3);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide || player.isShiftKeyDown()) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.getBlockEntity(pos) instanceof LicoriceFurnaceBlockEntity blockEntity && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, blockEntity, pos);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (newState.is(CCBlocks.LICORICE_FURNACE.get()) || newState.is(CCBlocks.LICORICE_FURNACE_ON.get())) {
            return;
        }

        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof LicoriceFurnaceBlockEntity blockEntity) {
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
        return new LicoriceFurnaceBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTicker(type, CCBlockEntities.LICORICE_FURNACE.get(), LicoriceFurnaceBlockEntity::serverTick);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!lit || random.nextInt(6) != 0) {
            return;
        }
        Direction direction = state.getValue(FACING);
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + random.nextDouble() * 6.0D / 16.0D;
        double z = pos.getZ() + 0.5D;
        double side = 0.52D;
        double spread = random.nextDouble() * 0.6D - 0.3D;
        level.addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,
            x + direction.getStepX() * side + (direction.getAxis() == Direction.Axis.Z ? spread : 0.0D),
            y,
            z + direction.getStepZ() * side + (direction.getAxis() == Direction.Axis.X ? spread : 0.0D),
            0.0D, 0.0D, 0.0D);
    }

    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTicker(
            BlockEntityType<A> actualType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == actualType ? (BlockEntityTicker<A>) ticker : null;
    }
}
