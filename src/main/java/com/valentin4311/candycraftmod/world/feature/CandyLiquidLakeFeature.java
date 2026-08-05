package com.valentin4311.candycraftmod.world.feature;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * The 1.12.2 underground lake generator, adapted to the modern feature API.
 * Lower cells are filled with source fluid and the upper cells are open,
 * preserving the old cave/lake shape and solid-boundary checks.
 */
public class CandyLiquidLakeFeature extends Feature<NoneFeatureConfiguration> {
    private final FluidMode fluidMode;

    public CandyLiquidLakeFeature(Codec<NoneFeatureConfiguration> codec) {
        this(codec, FluidMode.WATER_OR_GRENADINE);
    }

    public CandyLiquidLakeFeature(Codec<NoneFeatureConfiguration> codec, FluidMode fluidMode) {
        super(codec);
        this.fluidMode = fluidMode;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int baseX = origin.getX() - 8;
        int baseY = origin.getY();
        int baseZ = origin.getZ() - 8;

        while (baseY > level.getMinBuildHeight() + 5 && level.isEmptyBlock(new BlockPos(baseX, baseY, baseZ))) {
            baseY--;
        }
        baseY -= 4;
        if (baseY <= level.getMinBuildHeight() + 1) {
            return false;
        }

        boolean[] lake = new boolean[16 * 16 * 8];
        int ellipsoids = random.nextInt(4) + 4;
        for (int i = 0; i < ellipsoids; i++) {
            double width = random.nextDouble() * 6.0D + 3.0D;
            double height = random.nextDouble() * 4.0D + 2.0D;
            double depth = random.nextDouble() * 6.0D + 3.0D;
            double centerX = random.nextDouble() * (16.0D - width - 2.0D) + 1.0D + width / 2.0D;
            double centerY = random.nextDouble() * (8.0D - height - 4.0D) + 2.0D + height / 2.0D;
            double centerZ = random.nextDouble() * (16.0D - depth - 2.0D) + 1.0D + depth / 2.0D;

            for (int x = 1; x < 15; x++) {
                for (int z = 1; z < 15; z++) {
                    for (int y = 1; y < 7; y++) {
                        double dx = (x - centerX) / (width / 2.0D);
                        double dy = (y - centerY) / (height / 2.0D);
                        double dz = (z - centerZ) / (depth / 2.0D);
                        if (dx * dx + dy * dy + dz * dz < 1.2D) {
                            lake[index(x, z, y)] = true;
                        }
                    }
                }
            }
        }

        BlockState fluid = chooseFluid(random);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 8; y++) {
                    if (lake[index(x, z, y)] || !touchesLake(lake, x, z, y)) {
                        continue;
                    }
                    BlockPos pos = new BlockPos(baseX + x, baseY + y, baseZ + z);
                    BlockState state = level.getBlockState(pos);
                    if (y >= 4 && !state.getFluidState().isEmpty()) {
                        return false;
                    }
                    if (y < 4 && !state.isSolid() && !state.is(fluid.getBlock())) {
                        return false;
                    }
                }
            }
        }

        boolean placed = false;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 8; y++) {
                    if (lake[index(x, z, y)]) {
                        BlockPos pos = new BlockPos(baseX + x, baseY + y, baseZ + z);
                        level.setBlock(pos, y >= 4 ? Blocks.AIR.defaultBlockState() : fluid, 2 | 16);
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }

    private BlockState chooseFluid(RandomSource random) {
        if (fluidMode == FluidMode.CHOCOLATE) {
            return CCBlocks.LIQUID_CHOCOLATE.get().defaultBlockState();
        }
        return random.nextInt(4) == 0
            ? CCBlocks.GRENADINE.get().defaultBlockState()
            : Blocks.WATER.defaultBlockState();
    }

    private static boolean touchesLake(boolean[] lake, int x, int z, int y) {
        return (x > 0 && lake[index(x - 1, z, y)]) || (x < 15 && lake[index(x + 1, z, y)])
            || (z > 0 && lake[index(x, z - 1, y)]) || (z < 15 && lake[index(x, z + 1, y)])
            || (y > 0 && lake[index(x, z, y - 1)]) || (y < 7 && lake[index(x, z, y + 1)]);
    }

    private static int index(int x, int z, int y) {
        return (x * 16 + z) * 8 + y;
    }

    public enum FluidMode {
        WATER_OR_GRENADINE,
        CHOCOLATE
    }
}
