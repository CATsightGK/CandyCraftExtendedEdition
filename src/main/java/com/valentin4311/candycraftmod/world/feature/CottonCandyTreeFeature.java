package com.valentin4311.candycraftmod.world.feature;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.registry.CCSweetscapeBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CottonCandyTreeFeature extends Feature<NoneFeatureConfiguration> {
    public CottonCandyTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        BlockPos base = new BlockPos(origin.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ()), origin.getZ());
        return generate(level, context.random(), base);
    }

    public static boolean generate(LevelAccessor level, RandomSource random, BlockPos base) {
        BlockState below = level.getBlockState(base.below());
        if (!below.is(CCSweetscapeBlocks.CANDY_GRASS_BLOCK.get()) && !below.is(CCSweetscapeBlocks.MILK_BROWNIE_BLOCK.get())) {
            return false;
        }

        int trunkHeight = 5 + random.nextInt(3);
        BlockState trunk = CCSweetscapeBlocks.WHITE_CANDY_CANE_BLOCK.get().defaultBlockState();
        BlockState leaves = CCSweetscapeBlocks.COTTON_CANDY_LEAVES.get().defaultBlockState();
        for (int y = 0; y < trunkHeight; y++) {
            setReplaceable(level, base.above(y), trunk);
        }

        BlockPos crown = base.above(trunkHeight - 1);
        int currentY = -2;
        placeLayer1(level, crown.above(currentY++), leaves);
        placeLayer2(level, crown.above(currentY++), leaves);
        placeLayer3(level, crown.above(currentY++), leaves);
        placeLayer4(level, crown.above(currentY++), leaves);
        placeLayer4(level, crown.above(currentY++), leaves);
        placeLayer3(level, crown.above(currentY++), leaves);
        placeLayer2(level, crown.above(currentY++), leaves);
        placeLayer1(level, crown.above(currentY), leaves);
        return true;
    }

    private static void placeLayer1(LevelAccessor level, BlockPos pos, BlockState leaves) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                setReplaceable(level, pos.offset(x, 0, z), leaves);
            }
        }
    }

    private static void placeLayer2(LevelAccessor level, BlockPos pos, BlockState leaves) {
        placeLayerSquare(level, pos, leaves, 2);
        clear(level, pos.offset(2, 0, 2));
        clear(level, pos.offset(2, 0, -2));
        clear(level, pos.offset(-2, 0, 2));
        clear(level, pos.offset(-2, 0, -2));
    }

    private static void placeLayer3(LevelAccessor level, BlockPos pos, BlockState leaves) {
        placeLayerSquare(level, pos, leaves, 2);
        setReplaceable(level, pos.offset(3, 0, 0), leaves);
        setReplaceable(level, pos.offset(-3, 0, 0), leaves);
        setReplaceable(level, pos.offset(0, 0, -3), leaves);
        setReplaceable(level, pos.offset(0, 0, 3), leaves);
    }

    private static void placeLayer4(LevelAccessor level, BlockPos pos, BlockState leaves) {
        placeLayerSquare(level, pos, leaves, 2);
        for (int i = -1; i <= 1; i++) {
            setReplaceable(level, pos.offset(i, 0, 3), leaves);
            setReplaceable(level, pos.offset(i, 0, -3), leaves);
            setReplaceable(level, pos.offset(3, 0, i), leaves);
            setReplaceable(level, pos.offset(-3, 0, i), leaves);
        }
    }

    private static void placeLayerSquare(LevelAccessor level, BlockPos pos, BlockState leaves, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                setReplaceable(level, pos.offset(x, 0, z), leaves);
            }
        }
    }

    private static void clear(LevelAccessor level, BlockPos pos) {
        if (!level.isOutsideBuildHeight(pos) && (level.isEmptyBlock(pos) || level.getBlockState(pos).is(CCSweetscapeBlocks.COTTON_CANDY_LEAVES.get()))) {
            level.removeBlock(pos, false);
        }
    }

    private static void setReplaceable(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level.isOutsideBuildHeight(pos)) {
            return;
        }
        BlockState current = level.getBlockState(pos);
        if (current.isAir() || current.canBeReplaced() || current.is(CCSweetscapeBlocks.COTTON_CANDY_LEAVES.get())) {
            level.setBlock(pos, state, 2 | 16);
        }
    }
}
