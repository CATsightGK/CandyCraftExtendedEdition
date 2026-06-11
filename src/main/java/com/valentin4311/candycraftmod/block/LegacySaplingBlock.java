package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.world.feature.LegacyCandyTreeFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LegacySaplingBlock extends CCPlantBlock {
    public static final IntegerProperty METADATA = IntegerProperty.create("metadata", 0, 3);
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 1);
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);

    public LegacySaplingBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(METADATA, 0).setValue(STAGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(METADATA, STAGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
            grow(level, random, pos, state);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.is(Items.SUGAR)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            grow(serverLevel, level.random, pos, state);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void grow(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        if (state.getValue(STAGE) == 0) {
            level.setBlock(pos, state.setValue(STAGE, 1), 4);
            return;
        }

        LegacyCandyTreeFeature.Kind kind = treeKind(state);
        level.removeBlock(pos, false);
        if (!LegacyCandyTreeFeature.generate(level, random, pos, kind)) {
            level.setBlock(pos, state, 4);
        }
    }

    private static LegacyCandyTreeFeature.Kind treeKind(BlockState state) {
        Block block = state.getBlock();
        if (block == CCBlocks.CANDY_SAPLING_DARK.get()) {
            return LegacyCandyTreeFeature.Kind.CARAMEL;
        }
        if (block == CCBlocks.CANDY_SAPLING_LIGHT.get()) {
            return LegacyCandyTreeFeature.Kind.WHITE_CHOCOLATE;
        }
        if (block == CCBlocks.CANDY_SAPLING_CHERRY.get() || state.getValue(METADATA) == 3) {
            return LegacyCandyTreeFeature.Kind.CHERRY;
        }
        if (state.getValue(METADATA) == 1) {
            return LegacyCandyTreeFeature.Kind.CARAMEL;
        }
        if (state.getValue(METADATA) == 2) {
            return LegacyCandyTreeFeature.Kind.WHITE_CHOCOLATE;
        }
        return LegacyCandyTreeFeature.Kind.CHOCOLATE;
    }
}
