package com.valentin4311.candycraftmod.world.feature;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import java.util.ArrayList;
import java.util.List;
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

public class HardCandyCaneFeature extends Feature<NoneFeatureConfiguration> {
    private static final String HARD_CANDY_PLAINS = "hard_candy_plains";
    private static final int BIOME_EDGE_CLEARANCE = 12;

    public HardCandyCaneFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        BlockPos base = new BlockPos(
            origin.getX(),
            level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ()),
            origin.getZ()
        );

        if (!isBiomeInterior(level, base) || !hasSolidGround(level, base)) {
            return false;
        }

        int stemHeight = 5 + random.nextInt(5);
        int hookWidth = 2 + random.nextInt(2);
        int hookDrop = hookDropForHeight(stemHeight, random);
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockState cane = randomWarmCane(random);
        BlockState vertical = cane.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState horizontal = cane.setValue(RotatedPillarBlock.AXIS, direction.getAxis());

        List<Segment> segments = new ArrayList<>();
        for (int y = 0; y < stemHeight; ++y) {
            segments.add(new Segment(base.above(y), vertical));
        }

        BlockPos top = base.above(stemHeight - 1);
        for (int offset = 1; offset <= hookWidth; ++offset) {
            segments.add(new Segment(top.relative(direction, offset), horizontal));
        }

        BlockPos hookEnd = top.relative(direction, hookWidth);
        for (int drop = 1; drop <= hookDrop; ++drop) {
            segments.add(new Segment(hookEnd.below(drop), vertical));
        }

        if (segments.stream().anyMatch(segment -> !canPlace(level, segment.pos()))) {
            return false;
        }

        segments.forEach(segment -> level.setBlock(segment.pos(), segment.state(), 2 | 16));
        return true;
    }

    private static boolean isHardCandyPlains(WorldGenLevel level, BlockPos pos) {
        return level.getBiome(pos).unwrapKey()
            .map(key -> HARD_CANDY_PLAINS.equals(key.location().getPath()))
            .orElse(false);
    }

    private static boolean isBiomeInterior(WorldGenLevel level, BlockPos base) {
        for (int x = -BIOME_EDGE_CLEARANCE; x <= BIOME_EDGE_CLEARANCE; x += 4) {
            for (int z = -BIOME_EDGE_CLEARANCE; z <= BIOME_EDGE_CLEARANCE; z += 4) {
                if (!isHardCandyPlains(level, base.offset(x, 0, z))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean hasSolidGround(WorldGenLevel level, BlockPos base) {
        if (level.isOutsideBuildHeight(base) || level.isOutsideBuildHeight(base.below())) {
            return false;
        }
        BlockState ground = level.getBlockState(base.below());
        return !ground.isAir() && ground.getFluidState().isEmpty();
    }

    private static boolean canPlace(WorldGenLevel level, BlockPos pos) {
        return !level.isOutsideBuildHeight(pos)
            && level.isEmptyBlock(pos)
            && level.getFluidState(pos).isEmpty();
    }

    private static int hookDropForHeight(int stemHeight, RandomSource random) {
        if (stemHeight <= 6) {
            return 1;
        }
        if (stemHeight <= 8) {
            return 1 + random.nextInt(2);
        }
        return 2 + random.nextInt(2);
    }

    private static BlockState randomWarmCane(RandomSource random) {
        // Only red and white hard candy cane for sugar plains variant
        return CCBlocks.WHITE_RED_HARD_CANDY_BLOCK.get().defaultBlockState();
    }

    private record Segment(BlockPos pos, BlockState state) {
    }
}
