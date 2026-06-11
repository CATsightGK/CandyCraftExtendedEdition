package com.valentin4311.candycraftmod.world.feature;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CandySeaweedFeature extends Feature<NoneFeatureConfiguration> {
    public CandySeaweedFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        boolean placed = false;

        for (int i = 0; i < 64; i++) {
            BlockPos target = origin.offset(
                random.nextInt(8) - random.nextInt(8),
                random.nextInt(4) - random.nextInt(4),
                random.nextInt(8) - random.nextInt(8)
            );

            if (random.nextBoolean()) {
                BlockState state = random.nextBoolean()
                    ? CCBlocks.MINT.get().defaultBlockState()
                    : CCBlocks.BANANA_SEAWEED.get().defaultBlockState();
                placed |= tryPlace(level, target, state);
            } else {
                int height = random.nextInt(4) + 1;
                for (int y = 0; y < height; y++) {
                    placed |= tryPlace(level, target.above(y), CCBlocks.ROPE_LICORICE.get().defaultBlockState());
                }
            }
        }

        return placed;
    }

    private static boolean tryPlace(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (level.isOutsideBuildHeight(pos) || !level.getFluidState(pos).is(FluidTags.WATER)) {
            return false;
        }
        if (!state.canSurvive(level, pos)) {
            return false;
        }
        level.setBlock(pos, state, 2 | 16);
        return true;
    }
}
