package com.valentin4311.candycraftmod.world.feature;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.registry.CCSweetscapeBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class GummyWormFeature extends Feature<NoneFeatureConfiguration> {
    public GummyWormFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        BlockPos surfacePos = new BlockPos(
            origin.getX(),
            level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ()),
            origin.getZ()
        );

        if (isWormBlock(level.getBlockState(surfacePos.below()))) {
            return false;
        }

        BlockState state = randomWormState(random);
        return switch (random.nextInt(3)) {
            case 0 -> generateWormFlat(level, surfacePos, random.nextInt(10) + 7, state, random);
            case 1 -> generateWormStraight(level, surfacePos, random.nextInt(12) + 6, random.nextInt(4) + 3, state);
            default -> generateWormArc(level, surfacePos, state, random);
        };
    }

    private static boolean generateWormStraight(WorldGenLevel level, BlockPos position, int below, int above, BlockState state) {
        BlockState yState = state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        boolean placed = false;
        for (int i = -below; i < above; i++) {
            placed |= place(level, position.above(i), yState);
        }
        return placed;
    }

    private static boolean generateWormArc(WorldGenLevel level, BlockPos position, BlockState state, RandomSource random) {
        int height = random.nextInt(2) + 2;
        int startDepth = random.nextInt(4) + 4;
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockState vertical = state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockPos pos = position.below(startDepth);
        boolean placed = false;

        for (int i = 0; i <= height + startDepth; i++) {
            pos = pos.above();
            placed |= place(level, pos, vertical);
        }

        BlockState horizontal = state.setValue(RotatedPillarBlock.AXIS, direction.getAxis());
        for (int i = 0; i <= 2 + random.nextInt(2); i++) {
            pos = pos.relative(direction);
            placed |= place(level, pos, horizontal);
        }

        while (isAirOrLiquid(level, pos.below())) {
            pos = pos.below();
            placed |= place(level, pos, vertical);
        }
        for (int i = 0; i <= 4 + random.nextInt(4); i++) {
            pos = pos.below();
            placed |= place(level, pos, vertical);
        }
        return placed;
    }

    private static boolean generateWormFlat(WorldGenLevel level, BlockPos position, int length, BlockState state, RandomSource random) {
        BlockPos pos = position.above();
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int lastTurnDir = 0;
        boolean hasTurned = false;
        boolean placed = false;

        for (int i = 0; i <= length; i++) {
            placed |= place(level, pos, state.setValue(RotatedPillarBlock.AXIS, direction.getAxis()));

            if (hasTurned) {
                hasTurned = false;
            } else if (random.nextInt(3) == 0) {
                direction = direction.getClockWise();
                if (lastTurnDir == 1 || lastTurnDir == 0 && random.nextBoolean()) {
                    direction = direction.getOpposite();
                    lastTurnDir = -1;
                } else {
                    lastTurnDir = 1;
                }
                hasTurned = true;
            }

            while (isAirOrLiquid(level, pos.below())) {
                pos = pos.below();
                placed |= place(level, pos, state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                i++;
            }

            while (!isAirOrLiquid(level, pos.relative(direction))) {
                pos = pos.above();
                if (!isAirOrLiquid(level, pos)) {
                    return placed;
                }
                placed |= place(level, pos, state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
                i++;
            }

            pos = pos.relative(direction);
        }
        return placed;
    }

    private static boolean place(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (level.isOutsideBuildHeight(pos)) {
            return false;
        }
        level.setBlock(pos, state, 2 | 16);
        return true;
    }

    private static boolean isAirOrLiquid(WorldGenLevel level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos) && (level.isEmptyBlock(pos) || !level.getFluidState(pos).isEmpty());
    }

    private static boolean isWormBlock(BlockState state) {
        return state.is(CCSweetscapeBlocks.RED_GUMMY_WORM_BLOCK.get())
            || state.is(CCSweetscapeBlocks.ORANGE_GUMMY_WORM_BLOCK.get())
            || state.is(CCSweetscapeBlocks.YELLOW_GUMMY_WORM_BLOCK.get())
            || state.is(CCSweetscapeBlocks.WHITE_GUMMY_WORM_BLOCK.get())
            || state.is(CCSweetscapeBlocks.GREEN_GUMMY_WORM_BLOCK.get());
    }

    private static BlockState randomWormState(RandomSource random) {
        return switch (random.nextInt(5)) {
            case 1 -> CCSweetscapeBlocks.ORANGE_GUMMY_WORM_BLOCK.get().defaultBlockState();
            case 2 -> CCSweetscapeBlocks.YELLOW_GUMMY_WORM_BLOCK.get().defaultBlockState();
            case 3 -> CCSweetscapeBlocks.WHITE_GUMMY_WORM_BLOCK.get().defaultBlockState();
            case 4 -> CCSweetscapeBlocks.GREEN_GUMMY_WORM_BLOCK.get().defaultBlockState();
            default -> CCSweetscapeBlocks.RED_GUMMY_WORM_BLOCK.get().defaultBlockState();
        };
    }
}
