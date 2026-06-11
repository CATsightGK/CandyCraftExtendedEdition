package com.valentin4311.candycraftmod.world.feature;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.registry.CCSweetscapeBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SweetscapeChocolateTreeFeature extends Feature<NoneFeatureConfiguration> {
    public SweetscapeChocolateTreeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return generate(context.level(), context.random(), context.origin());
    }

    public static boolean generate(ServerLevel level, RandomSource random, BlockPos origin) {
        return generate((WorldGenLevel) level, random, origin);
    }

    private static boolean generate(WorldGenLevel level, RandomSource random, BlockPos origin) {
        BlockPos base = new BlockPos(origin.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ()), origin.getZ());
        BlockState below = level.getBlockState(base.below());
        if (!below.is(CCSweetscapeBlocks.CHOCOLATE_COVERED_WHITE_BROWNIE.get()) && !below.is(CCSweetscapeBlocks.WHITE_BROWNIE_BLOCK.get())) {
            return false;
        }

        int trunkHeight = 5 + random.nextInt(3) + random.nextInt(2);
        BlockState trunk = CCSweetscapeBlocks.WAFER_STICK_BLOCK.get().defaultBlockState();
        BlockState leaves = randomLeaves(random);
        for (int y = 0; y < trunkHeight; y++) {
            setReplaceable(level, base.above(y), trunk);
        }

        BlockPos crown = base.above(trunkHeight - 1);
        int height = 3;
        for (int y = crown.getY() - 3 + height; y <= crown.getY() + height; ++y) {
            int yPlusHeight = y - (crown.getY() + height);
            int radius = 1 - yPlusHeight / 2;
            for (int x = crown.getX() - radius; x <= crown.getX() + radius; ++x) {
                int localX = x - crown.getX();
                for (int z = crown.getZ() - radius; z <= crown.getZ() + radius; ++z) {
                    int localZ = z - crown.getZ();
                    if (shouldPlaceLeaf(random, localX, radius, localZ, yPlusHeight)) {
                        setReplaceable(level, new BlockPos(x, y, z), leaves);
                    }
                }
            }
        }

        for (int y = 0; y < height; y++) {
            setReplaceable(level, crown.above(y), trunk);
        }
        return true;
    }

    private static BlockState randomLeaves(RandomSource random) {
        return random.nextBoolean()
            ? CCSweetscapeBlocks.WHITE_CHOCOLATE_LEAVES.get().defaultBlockState()
            : CCSweetscapeBlocks.DARK_CHOCOLATE_LEAVES.get().defaultBlockState();
    }

    private static boolean shouldPlaceLeaf(RandomSource random, int localX, int radius, int localZ, int yPlusHeight) {
        return Math.abs(localX) != radius || Math.abs(localZ) != radius || random.nextInt(2) == 0 || yPlusHeight == 0;
    }

    private static void setReplaceable(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (level.isOutsideBuildHeight(pos)) {
            return;
        }
        BlockState current = level.getBlockState(pos);
        if (current.isAir() || current.canBeReplaced()
            || current.is(CCSweetscapeBlocks.MILK_CHOCOLATE_LEAVES.get())
            || current.is(CCSweetscapeBlocks.WHITE_CHOCOLATE_LEAVES.get())
            || current.is(CCSweetscapeBlocks.DARK_CHOCOLATE_LEAVES.get())) {
            level.setBlock(pos, state, 2 | 16);
        }
    }
}
