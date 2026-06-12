package com.valentin4311.candycraftmod.world.feature;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class CandyGrassFeature extends Feature<NoneFeatureConfiguration> {
    public CandyGrassFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        String originBiome = level.getBiome(origin).unwrapKey()
            .map(key -> key.location().getPath())
            .orElse("");
        if ("ice_cream_plains".equals(originBiome) || "ice_cream_sky_mountains".equals(originBiome)) {
            return false;
        }

        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ());
        int startY = nextInt(random, surfaceY * 2);
        BlockPos base = new BlockPos(origin.getX(), startY, origin.getZ());

        while (base.getY() > 55 && base.getY() < 128) {
            BlockState state = level.getBlockState(base);
            if (!state.isAir() && !state.is(BlockTags.LEAVES)) {
                break;
            }
            base = base.below();
        }

        boolean placed = false;
        for (int i = 0; i < 128; ++i) {
            int x = base.getX() + random.nextInt(8) - random.nextInt(8);
            int y = base.getY() + random.nextInt(4) - random.nextInt(4);
            int z = base.getZ() + random.nextInt(8) - random.nextInt(8);
            BlockPos target = new BlockPos(x, y, z);

            if (target.getY() <= 58 || !level.isEmptyBlock(target) || !canPlaceOnCandyDirt(level, target.below())) {
                continue;
            }

            level.setBlock(target, randomPlant(level, target, random), 2);
            placed = true;
        }

        return placed;
    }

    private static int nextInt(RandomSource random, int bound) {
        return bound <= 1 ? 0 : random.nextInt(bound);
    }

    private static boolean canPlaceOnCandyDirt(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(CCBlocks.PUDDING.get()) || state.is(CCBlocks.FLOUR.get()) || state.is(CCBlocks.CANDY_FARMLAND.get());
    }

    private static BlockState randomGrass(RandomSource random) {
        return switch (random.nextInt(4)) {
            case 0 -> CCBlocks.SWEET_GRASS_PINK.get().defaultBlockState();
            case 1 -> CCBlocks.SWEET_GRASS_PALE.get().defaultBlockState();
            case 2 -> CCBlocks.SWEET_GRASS_YELLOW.get().defaultBlockState();
            default -> CCBlocks.SWEET_GRASS_RED.get().defaultBlockState();
        };
    }

    private static BlockState randomPlant(WorldGenLevel level, BlockPos pos, RandomSource random) {
        String biome = level.getBiome(pos).unwrapKey()
            .map(key -> key.location().getPath())
            .orElse("");

        if (isRareSugarEssenceBiome(biome) && random.nextInt(600) == 4) {
            return CCBlocks.SUGAR_ESSENCE_FLOWER.get().defaultBlockState();
        }

        if (random.nextInt(32) == 31 && random.nextBoolean()) {
            return "caramel_forest".equals(biome)
                ? CCBlocks.ACID_MINT_FLOWER.get().defaultBlockState()
                : CCBlocks.FRAISE_TAGADA_FLOWER.get().defaultBlockState();
        }

        return randomGrass(random);
    }

    private static boolean isRareSugarEssenceBiome(String biome) {
        return "sugar_enchanted_forest".equals(biome);
    }
}
