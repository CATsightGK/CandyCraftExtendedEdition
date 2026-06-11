package com.valentin4311.candycraftmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import com.valentin4311.candycraftmod.registry.CCFluids;

public class FragileGrenadineBlock extends Block {
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);

    public FragileGrenadineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        tick(state, level, pos, random);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (hasGrenadineNearby(level, pos)) {
            if (state.getValue(AGE) != 0) {
                level.setBlock(pos, state.setValue(AGE, 0), Block.UPDATE_CLIENTS);
            }
            level.destroyBlockProgress(crackId(pos), pos, -1);
            level.scheduleTick(pos, this, 40 + random.nextInt(40));
            return;
        }

        int age = state.getValue(AGE);
        if (age >= 3) {
            level.destroyBlockProgress(crackId(pos), pos, -1);
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        } else {
            level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
            level.destroyBlockProgress(crackId(pos), pos, (age + 1) * 3);
            level.scheduleTick(pos, this, 40 + random.nextInt(40));
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (!level.isClientSide) {
            level.scheduleTick(pos, this, 40 + level.random.nextInt(40));
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        super.playerWillDestroy(level, pos, state, player);
        if (!level.isClientSide) {
            level.setBlock(pos, Blocks.WATER.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    private static boolean hasGrenadineNearby(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getFluidState(pos.relative(direction)).is(CCFluids.SOURCE_GRENADINE.get())
                || level.getFluidState(pos.relative(direction)).is(CCFluids.FLOWING_GRENADINE.get())) {
                return true;
            }
        }
        return false;
    }

    private static int crackId(BlockPos pos) {
        return pos.hashCode();
    }
}
