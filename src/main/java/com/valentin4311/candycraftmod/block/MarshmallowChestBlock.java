package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.block.entity.MarshmallowChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.Container;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import com.valentin4311.candycraftmod.menu.MarshmallowChestMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;

public class MarshmallowChestBlock extends FacingModelBlock implements EntityBlock {
    public static final EnumProperty<ChestType> TYPE = net.minecraft.world.level.block.state.properties.BlockStateProperties.CHEST_TYPE;
    private final Theme theme;

    public MarshmallowChestBlock(Theme theme, BlockBehaviour.Properties properties) {
        super(properties);
        this.theme = theme;
        registerDefaultState(defaultBlockState().setValue(TYPE, ChestType.SINGLE));
    }

    public Theme theme() {
        return theme;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide || player.isShiftKeyDown()) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (isBlocked(level, pos) || isOtherHalfBlocked(state, level, pos)) {
            return InteractionResult.CONSUME;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            Container container = getContainer(state, level, pos);
            if (container != null) {
                MenuProvider provider = new SimpleMenuProvider(
                    (id, inventory, menuPlayer) -> new MarshmallowChestMenu(id, inventory, container, theme),
                    Component.translatable("container.candycraftmod." + theme.serializedName())
                );
                int containerSize = container.getContainerSize();
                NetworkHooks.openScreen(serverPlayer, provider, buffer -> {
                    buffer.writeBlockPos(pos);
                    buffer.writeVarInt(containerSize);
                    buffer.writeVarInt(theme.ordinal());
                });
            }
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
        Container container = getContainer(state, level, pos);
        return container == null ? 0 : AbstractContainerMenu.getRedstoneSignalFromContainer(container);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MarshmallowChestBlockEntity(pos, state);
    }

    @Override
    public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int data) {
        super.triggerEvent(state, level, pos, id, data);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity != null && blockEntity.triggerEvent(id, data);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockState state = defaultBlockState().setValue(FACING, facing).setValue(TYPE, ChestType.SINGLE);
        for (Direction direction : new Direction[]{facing.getClockWise(), facing.getCounterClockWise()}) {
            BlockState neighbor = context.getLevel().getBlockState(context.getClickedPos().relative(direction));
            if (canConnect(state, neighbor)) {
                return state.setValue(TYPE, direction == facing.getClockWise() ? ChestType.LEFT : ChestType.RIGHT);
            }
        }
        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
            LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction.getAxis().isHorizontal() && neighborState.is(this)) {
            ChestType type = neighborState.getValue(TYPE);
            if (state.getValue(TYPE) == ChestType.SINGLE && type != ChestType.SINGLE
                    && state.getValue(FACING) == neighborState.getValue(FACING)
                    && connectedDirection(neighborState) == direction.getOpposite()) {
                return state.setValue(TYPE, type.getOpposite());
            }
        }
        if (state.getValue(TYPE) != ChestType.SINGLE && connectedDirection(state) == direction) {
            boolean validPair = neighborState.is(this)
                && neighborState.getValue(FACING) == state.getValue(FACING)
                && neighborState.getValue(TYPE) == state.getValue(TYPE).getOpposite();
            if (!validPair) {
                return state.setValue(TYPE, ChestType.SINGLE);
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TYPE);
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

    private static boolean canConnect(BlockState state, BlockState neighbor) {
        return neighbor.is(state.getBlock())
            && neighbor.getValue(TYPE) == ChestType.SINGLE
            && neighbor.getValue(FACING) == state.getValue(FACING);
    }

    public static Direction connectedDirection(BlockState state) {
        ChestType type = state.getValue(TYPE);
        Direction facing = state.getValue(FACING);
        return type == ChestType.LEFT ? facing.getClockWise()
            : type == ChestType.RIGHT ? facing.getCounterClockWise()
            : Direction.UP;
    }

    private static boolean isOtherHalfBlocked(BlockState state, LevelAccessor level, BlockPos pos) {
        return state.getValue(TYPE) != ChestType.SINGLE && isBlocked(level, pos.relative(connectedDirection(state)));
    }

    public static Container getContainer(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof MarshmallowChestBlockEntity current)) {
            return null;
        }
        ChestType type = state.getValue(TYPE);
        if (type == ChestType.SINGLE) {
            return current;
        }
        BlockPos otherPos = pos.relative(connectedDirection(state));
        BlockState otherState = level.getBlockState(otherPos);
        if (!otherState.is(state.getBlock()) || otherState.getValue(TYPE) != type.getOpposite()
                || !(level.getBlockEntity(otherPos) instanceof MarshmallowChestBlockEntity other)) {
            return null;
        }
        return type == ChestType.LEFT
            ? new CompoundContainer(current, other)
            : new CompoundContainer(other, current);
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
