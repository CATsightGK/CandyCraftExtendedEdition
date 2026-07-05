package com.valentin4311.candycraftmod.world.feature;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class MarshmallowWaterlilyPatchFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceKey<Biome> SUGAR_FOREST = biomeKey("sugar_forest");
    private static final ResourceKey<Biome> SUGAR_RIVER = biomeKey("sugar_river");
    private static final ResourceKey<Biome> COTTON_CANDY_PLAINS = biomeKey("cotton_candy_plains");

    public MarshmallowWaterlilyPatchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        if (!isAllowedOriginBiome(level, origin)) {
            return false;
        }

        boolean placed = false;
        for (int i = 0; i < 10; i++) {
            BlockPos target = origin.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(8));
            BlockPos surface = findWaterSurface(level, target);
            if (surface != null && isAllowedWaterlilyBiome(level, surface) && canPlaceLily(level, surface)) {
                placed |= placeLily(level, surface, random);
            }
        }
        return placed;
    }

    private static BlockPos findWaterSurface(WorldGenLevel level, BlockPos origin) {
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(origin.getX(), surfaceY + 1, origin.getZ());
        for (int y = surfaceY + 1; y >= level.getMinBuildHeight(); y--) {
            cursor.setY(y);
            if (isWaterSurface(level, cursor)) {
                return cursor.immutable();
            }
            if (!level.getBlockState(cursor).isAir() && !level.getFluidState(cursor).is(FluidTags.WATER)) {
                return null;
            }
        }
        return null;
    }

    private static boolean canPlaceLily(WorldGenLevel level, BlockPos pos) {
        if (!isWaterSurface(level, pos)) {
            return false;
        }
        return CCBlocks.MARSHMALLOW_SLICE.get().defaultBlockState().canSurvive(level, pos);
    }

    private static boolean isWaterSurface(WorldGenLevel level, BlockPos pos) {
        return level.getBlockState(pos).isAir() && level.getFluidState(pos.below()).is(FluidTags.WATER);
    }

    private static boolean isAllowedOriginBiome(WorldGenLevel level, BlockPos pos) {
        return isMarshmallowWaterlilyBiome(level, pos) || isRiverNearMarshmallowBiome(level, pos) || hasNearbyMarshmallowGrass(level, pos);
    }

    private static boolean isAllowedWaterlilyBiome(WorldGenLevel level, BlockPos pos) {
        return isMarshmallowWaterlilyBiome(level, pos) || isRiverNearMarshmallowBiome(level, pos) || hasNearbyMarshmallowGrass(level, pos);
    }

    private static boolean isMarshmallowWaterlilyBiome(WorldGenLevel level, BlockPos pos) {
        return level.getBiome(pos).is(SUGAR_FOREST) || level.getBiome(pos).is(COTTON_CANDY_PLAINS);
    }

    private static boolean isRiverNearMarshmallowBiome(WorldGenLevel level, BlockPos pos) {
        return level.getBiome(pos).is(SUGAR_RIVER) && hasNearbyMarshmallowBiome(level, pos);
    }

    private static boolean hasNearbyMarshmallowBiome(WorldGenLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dz = -24; dz <= 24; dz += 8) {
            for (int dx = -24; dx <= 24; dx += 8) {
                cursor.set(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
                if (isMarshmallowWaterlilyBiome(level, cursor)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasNearbyMarshmallowGrass(WorldGenLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dz = -5; dz <= 5; dz++) {
            for (int dx = -5; dx <= 5; dx++) {
                if (Math.abs(dx) + Math.abs(dz) > 6) {
                    continue;
                }
                for (int dy = -2; dy <= 1; dy++) {
                    cursor.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    if (level.getBlockState(cursor).is(CCBlocks.CANDY_GRASS_BLOCK.get())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static ResourceKey<Biome> biomeKey(String name) {
        return ResourceKey.create(Registries.BIOME, new ResourceLocation(CandyCraft.MODID, name));
    }

    private static boolean placeLily(WorldGenLevel level, BlockPos pos, RandomSource random) {
        BlockState state = random.nextInt(15) == 0
            ? CCBlocks.MARSHMALLOW_FLOWER_BLOCK.get().defaultBlockState()
            : CCBlocks.MARSHMALLOW_SLICE.get().defaultBlockState();
        level.setBlock(pos, state, 2 | 16);
        return true;
    }
}

