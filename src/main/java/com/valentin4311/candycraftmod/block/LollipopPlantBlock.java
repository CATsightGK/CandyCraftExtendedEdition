package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LollipopPlantBlock extends CropBlock implements BonemealableBlock {
    private static final VoxelShape[] SHAPES = {
        Block.box(6.0D, 0.0D, 6.0D, 10.0D, 2.0D, 10.0D),
        Block.box(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D),
        Block.box(6.0D, 0.0D, 6.0D, 10.0D, 6.0D, 10.0D),
        Block.box(6.0D, 0.0D, 6.0D, 10.0D, 8.0D, 10.0D),
        Block.box(6.0D, 0.0D, 6.0D, 10.0D, 10.0D, 10.0D),
        Block.box(6.0D, 0.0D, 6.0D, 10.0D, 12.0D, 10.0D),
        Block.box(6.0D, 0.0D, 6.0D, 10.0D, 14.0D, 10.0D),
        Block.box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D)
    };

    public LollipopPlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected Item getBaseSeedId() {
        return CCItems.LOLLIPOP_SEEDS.get();
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(CCBlocks.CANDY_FARMLAND.get());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[Mth.clamp(getAge(state), 0, getMaxAge())];
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getRawBrightness(pos.above(), 0) < 9) {
            return;
        }

        int age = getAge(state);
        if (random.nextInt((int)(25.0F / getGrowthSpeed(this, level, pos)) + 1) != 0) {
            return;
        }

        if (age >= getMaxAge()) {
            BlockPos top = pos.above();
            if (level.isEmptyBlock(top)) {
                level.setBlock(top, CCBlocks.LOLLIPOP_BLOCK.get().defaultBlockState(), 2);
            }
        } else {
            level.setBlock(pos, getStateForAge(age + 1), 2);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean clientSide) {
        return getAge(state) < getMaxAge();
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = Math.min(getMaxAge(), getAge(state) + Mth.nextInt(random, 2, 5));
        level.setBlock(pos, getStateForAge(age), 2);
    }
}
