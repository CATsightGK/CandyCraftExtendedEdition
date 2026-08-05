package com.valentin4311.candycraftmod.world;

import com.valentin4311.candycraftmod.CandyCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;

public final class CandyPrecipitation {
    private static final ResourceLocation CHOCOLATE_FOREST = candyBiome("chocolate_forest");
    private static final ResourceLocation ICE_CREAM_PLAINS = candyBiome("ice_cream_plains");
    private static final ResourceLocation ICE_CREAM_SKY_MOUNTAINS = candyBiome("ice_cream_sky_mountains");
    private static final ResourceLocation SUGAR_COLD_FOREST = candyBiome("sugar_cold_forest");

    private CandyPrecipitation() {
    }

    public static Biome.Precipitation at(LevelReader level, BlockPos pos) {
        ResourceLocation biome = level.getBiome(pos).unwrapKey()
            .map(key -> key.location())
            .orElse(null);
        if (CHOCOLATE_FOREST.equals(biome)) {
            return Biome.Precipitation.RAIN;
        }
        if (ICE_CREAM_PLAINS.equals(biome)
            || ICE_CREAM_SKY_MOUNTAINS.equals(biome)
            || SUGAR_COLD_FOREST.equals(biome)) {
            return Biome.Precipitation.SNOW;
        }
        return Biome.Precipitation.NONE;
    }

    private static ResourceLocation candyBiome(String path) {
        return new ResourceLocation(CandyCraft.MODID, path);
    }
}
