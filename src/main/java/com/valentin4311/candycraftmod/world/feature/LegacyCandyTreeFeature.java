package com.valentin4311.candycraftmod.world.feature;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class LegacyCandyTreeFeature extends Feature<NoneFeatureConfiguration> {
    private final Kind kind;

    public LegacyCandyTreeFeature(Codec<NoneFeatureConfiguration> codec, Kind kind) {
        super(codec);
        this.kind = kind;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        BlockPos base = new BlockPos(origin.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ()), origin.getZ());
        return generate(level, context.random(), base, kind);
    }

    public static boolean generate(LevelAccessor level, RandomSource random, BlockPos base, Kind kind) {
        return switch (kind) {
            case CARAMEL -> generateCaramelClassic(level, random, base);
            case CARAMEL_FOREST -> generateCaramel(level, random, base);
            case ENCHANTED -> generateEnchanted(level, random, base);
            case WHITE_CHOCOLATE -> generateSpruceLike(level, random, base, CCBlocks.MARSHMALLOW_LOG_LIGHT.get().defaultBlockState(), CCBlocks.CANDY_LEAVES_LIGHT.get().defaultBlockState());
            case CHOCOLATE -> generateChocolate(level, random, base);
            case CHERRY -> generateCherry(level, random, base);
        };
    }

    private static boolean generateCaramel(LevelAccessor level, RandomSource random, BlockPos base) {
        BlockState trunk = CCBlocks.MARSHMALLOW_LOG_DARK.get().defaultBlockState();
        BlockState leaves = CCBlocks.CANDY_LEAVES_DARK.get().defaultBlockState();
        int height = 9 + random.nextInt(8);
        int bareTrunk = 1 + random.nextInt(2);
        int leafHeight = height - bareTrunk;
        int maxRadius = 2 + random.nextInt(2);
        if (!canGrow(level, base, height + 1, maxRadius)) {
            return false;
        }

        int radius = random.nextInt(3) + 1;
        int radiusLimit = random.nextInt(4) + 1;
        int resetRadius = 0;
        for (int i = 0; i <= leafHeight; ++i) {
            BlockPos layer = base.above(height - i);
            placeCaramelConeLayer(level, layer, leaves, radius);

            if (radius >= radiusLimit) {
                radius = resetRadius;
                resetRadius = 1;
                radiusLimit++;
                if (radiusLimit > maxRadius) {
                    radiusLimit = maxRadius;
                }
            } else {
                radius++;
            }
        }

        int trunkCut = random.nextInt(3);
        for (int y = 0; y < height - trunkCut; y++) {
            set(level, base.above(y), trunk);
        }
        return true;
    }

    private static boolean generateCaramelClassic(LevelAccessor level, RandomSource random, BlockPos base) {
        BlockState trunk = CCBlocks.MARSHMALLOW_LOG_DARK.get().defaultBlockState();
        BlockState leaves = CCBlocks.CANDY_LEAVES_DARK.get().defaultBlockState();
        int height = 3 + random.nextInt(5);
        if (!canGrow(level, base, height + 2, 3)) {
            return false;
        }

        for (int y = 0; y < height; y++) {
            set(level, base.above(y), trunk);
        }

        int crownBase = height - 3;
        for (int y = crownBase; y <= height; y++) {
            int topOffset = y - height;
            int radius = 1 - topOffset / 2;
            placeLegacySmallCaramelLayer(level, base.above(y), leaves, radius, topOffset, random);
        }
        return true;
    }

    private static boolean generateEnchanted(LevelAccessor level, RandomSource random, BlockPos base) {
        BlockState trunk = CCBlocks.MARSHMALLOW_LOG.get().defaultBlockState();
        BlockState leaves = CCBlocks.CANDY_LEAVES_ENCHANT.get().defaultBlockState();
        int height = 10 + random.nextInt(3);
        if (!canGrow(level, base, height + 3, 6)) {
            return false;
        }

        for (int y = 0; y < height; y++) {
            set(level, base.above(y), trunk);
            set(level, base.east().above(y), trunk);
            set(level, base.south().above(y), trunk);
            set(level, base.south().east().above(y), trunk);
        }

        BlockPos crown = base.offset(1, height, 1);
        placeSquareLayer(level, crown.below(3), leaves, 4, random, true);
        placeSquareLayer(level, crown.below(2), leaves, 5, random, true);
        placeSquareLayer(level, crown.below(), leaves, 6, random, true);
        placeSquareLayer(level, crown, leaves, 6, random, true);
        placeSquareLayer(level, crown.above(), leaves, 5, random, true);
        placeSquareLayer(level, crown.above(2), leaves, 3, random, true);

        int branches = 2 + random.nextInt(3);
        Direction[] directions = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);
        for (int i = 0; i < branches; i++) {
            Direction direction = directions[random.nextInt(directions.length)];
            int branchY = height - 5 + random.nextInt(4);
            BlockPos tip = base.offset(1, branchY, 1);
            int length = 4 + random.nextInt(2);
            for (int step = 1; step <= length; step++) {
                tip = tip.relative(direction).above(step % 2 == 0 ? 1 : 0);
                set(level, tip, trunkWithAxis(trunk, direction.getAxis()));
            }
            placeSquareLayer(level, tip, leaves, 3, random, true);
            placeSquareLayer(level, tip.above(), leaves, 2, random, true);
        }
        return true;
    }

    private static boolean generateSpruceLike(LevelAccessor level, RandomSource random, BlockPos base, BlockState trunk, BlockState leaves) {
        int height = 6 + random.nextInt(4);
        if (!canGrow(level, base, height + 1, 3)) {
            return false;
        }

        int bare = 1 + random.nextInt(2);
        int coneHeight = height - bare;
        int radius = random.nextInt(2);
        int radiusLimit = 2 + random.nextInt(2);
        int resetRadius = 0;

        for (int i = 0; i <= coneHeight; i++) {
            BlockPos layer = base.above(height - i);
            placeSpruceLayer(level, layer, leaves, radius);
            if (radius >= radiusLimit) {
                radius = resetRadius;
                resetRadius = 1;
                radiusLimit = Math.min(radiusLimit + 1, 3);
            } else {
                radius++;
            }
        }

        int trunkCut = random.nextInt(3);
        for (int y = 0; y < height - trunkCut; y++) {
            set(level, base.above(y), trunk);
        }
        return true;
    }

    private static boolean generateChocolate(LevelAccessor level, RandomSource random, BlockPos base) {
        BlockState trunk = CCBlocks.MARSHMALLOW_LOG.get().defaultBlockState();
        BlockState leaves = random.nextInt(3) == 0 ? CCBlocks.CANDY_LEAVES_DARK.get().defaultBlockState() : CCBlocks.CANDY_LEAVES.get().defaultBlockState();
        int height = 4 + random.nextInt(3);
        if (!canGrow(level, base, height + 2, 3)) {
            return false;
        }
        for (int y = 0; y < height; y++) {
            set(level, base.above(y), trunk);
        }
        BlockPos crown = base.above(height);
        placeBlobLayer(level, crown.below(2), leaves, 2, random);
        placeBlobLayer(level, crown.below(), leaves, 2, random);
        placeBlobLayer(level, crown, leaves, 1, random);
        return true;
    }

    private static boolean generateCherry(LevelAccessor level, RandomSource random, BlockPos base) {
        BlockState trunk = CCBlocks.MARSHMALLOW_LOG.get().defaultBlockState();
        BlockState leaves = CCBlocks.CANDY_LEAVES_CHERRY.get().defaultBlockState();
        if (!level.getBlockState(base.below()).is(CCBlocks.PUDDING.get()) || (!isReplaceable(level, base) && !level.getBlockState(base).is(CCBlocks.CANDY_SAPLING_CHERRY.get()))) {
            return false;
        }
        if (!isReplaceable(level, base.above())
            || !isReplaceable(level, base.above(2))
            || !isReplaceable(level, base.above().east())
            || !isReplaceable(level, base.above().west())
            || !isReplaceable(level, base.above().north())
            || !isReplaceable(level, base.above().south())) {
            return false;
        }

        set(level, base, trunk);
        int stemBaseY = base.getY() + 1;
        int grown = 0;
        int height = random.nextInt(3) + 5;
        for (int y = 0; y < height; y++) {
            BlockPos stem = new BlockPos(base.getX(), stemBaseY + y, base.getZ());
            boolean diagonals = (y % 2) != 0;
            if (!isCherryLayerReplaceable(level, stem, diagonals)) {
                break;
            }

            set(level, stem, trunk);
            set(level, stem.north(), leaves);
            set(level, stem.south(), leaves);
            set(level, stem.west(), leaves);
            set(level, stem.east(), leaves);
            if (diagonals) {
                set(level, stem.north().east(), leaves);
                set(level, stem.north().west(), leaves);
                set(level, stem.south().east(), leaves);
                set(level, stem.south().west(), leaves);
            }
            grown = y + 1;
        }

        BlockPos top = new BlockPos(base.getX(), stemBaseY + grown, base.getZ());
        if (isReplaceable(level, top)) {
            set(level, top, leaves);
        }
        return true;
    }

    private static boolean isCherryLayerReplaceable(LevelAccessor level, BlockPos stem, boolean diagonals) {
        if (!isReplaceable(level, stem)
            || !isReplaceable(level, stem.north())
            || !isReplaceable(level, stem.south())
            || !isReplaceable(level, stem.west())
            || !isReplaceable(level, stem.east())) {
            return false;
        }
        if (diagonals) {
            return isReplaceable(level, stem.north().east())
                && isReplaceable(level, stem.north().west())
                && isReplaceable(level, stem.south().east())
                && isReplaceable(level, stem.south().west());
        }
        return true;
    }

    private static boolean canGrow(LevelAccessor level, BlockPos base, int height, int radius) {
        if (!isCandySoil(level.getBlockState(base.below()))) {
            return false;
        }
        for (int y = 0; y <= height; y++) {
            int layerRadius = y == 0 ? 0 : radius;
            for (int x = -layerRadius; x <= layerRadius; x++) {
                for (int z = -layerRadius; z <= layerRadius; z++) {
                    BlockPos pos = base.offset(x, y, z);
                    if (level.isOutsideBuildHeight(pos) || !isReplaceable(level, pos)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean isCandySoil(BlockState state) {
        return state.is(CCBlocks.PUDDING.get()) || state.is(CCBlocks.FLOUR.get()) || state.is(CCBlocks.CANDY_FARMLAND.get());
    }

    private static void placeSquareLayer(LevelAccessor level, BlockPos center, BlockState leaves, int radius, RandomSource random, boolean softenCorners) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                boolean corner = Math.abs(x) == radius && Math.abs(z) == radius;
                if (!softenCorners || !corner || random.nextInt(3) != 0) {
                    set(level, center.offset(x, 0, z), leaves);
                }
            }
        }
    }

    private static void placeBlobLayer(LevelAccessor level, BlockPos center, BlockState leaves, int radius, RandomSource random) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                boolean corner = Math.abs(x) == radius && Math.abs(z) == radius;
                if (!corner || random.nextBoolean()) {
                    set(level, center.offset(x, 0, z), leaves);
                }
            }
        }
    }

    private static void placeSpruceLayer(LevelAccessor level, BlockPos center, BlockState leaves, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (Math.abs(x) != radius || Math.abs(z) != radius || radius <= 0) {
                    set(level, center.offset(x, 0, z), leaves);
                }
            }
        }
    }

    private static void placeCaramelConeLayer(LevelAccessor level, BlockPos center, BlockState leaves, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if ((Math.sin(x) != radius || Math.sin(z) != radius || radius <= 0)) {
                    set(level, center.offset(x, 0, z), leaves);
                }
            }
        }
    }

    private static void placeLegacySmallCaramelLayer(LevelAccessor level, BlockPos center, BlockState leaves, int radius, int topOffset, RandomSource random) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                boolean corner = Math.abs(x) == radius && Math.abs(z) == radius;
                if (!corner || random.nextInt(2) != 0 && topOffset != 0) {
                    set(level, center.offset(x, 0, z), leaves);
                }
            }
        }
    }

    private static BlockState trunkWithAxis(BlockState state, Direction.Axis axis) {
        return state.hasProperty(net.minecraft.world.level.block.RotatedPillarBlock.AXIS)
            ? state.setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, axis)
            : state;
    }

    private static boolean isReplaceable(LevelAccessor level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        return current.isAir() || current.canBeReplaced() || current.is(BlockTags.LEAVES);
    }

    private static void set(LevelAccessor level, BlockPos pos, BlockState state) {
        if (!level.isOutsideBuildHeight(pos) && isReplaceable(level, pos)) {
            level.setBlock(pos, state, 2 | 16);
        }
    }

    public enum Kind {
        CARAMEL,
        CARAMEL_FOREST,
        ENCHANTED,
        WHITE_CHOCOLATE,
        CHOCOLATE,
        CHERRY
    }
}
