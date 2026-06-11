package com.valentin4311.candycraftmod.world.biome.fractal;

import net.minecraft.resources.ResourceLocation;

public final class BiomeIds {
    public static final ResourceLocation OCEAN = minecraft("ocean");
    public static final ResourceLocation DEEP_OCEAN = minecraft("deep_ocean");
    public static final ResourceLocation PLAINS = minecraft("plains");
    public static final ResourceLocation RIVER = minecraft("river");
    public static final ResourceLocation FROZEN_OCEAN = minecraft("frozen_ocean");
    public static final ResourceLocation FROZEN_RIVER = minecraft("frozen_river");
    public static final ResourceLocation MUSHROOM_FIELDS = minecraft("mushroom_fields");
    public static final ResourceLocation BEACH = minecraft("beach");
    public static final ResourceLocation STONY_SHORE = minecraft("stony_shore");
    public static final ResourceLocation SNOWY_PLAINS = minecraft("snowy_plains");
    public static final ResourceLocation WARM_OCEAN = minecraft("warm_ocean");
    public static final ResourceLocation LUKEWARM_OCEAN = minecraft("lukewarm_ocean");
    public static final ResourceLocation COLD_OCEAN = minecraft("cold_ocean");
    public static final ResourceLocation DEEP_LUKEWARM_OCEAN = minecraft("deep_lukewarm_ocean");
    public static final ResourceLocation DEEP_COLD_OCEAN = minecraft("deep_cold_ocean");
    public static final ResourceLocation DEEP_FROZEN_OCEAN = minecraft("deep_frozen_ocean");
    public static final ResourceLocation FOREST = minecraft("forest");
    public static final ResourceLocation DESERT = minecraft("desert");
    public static final ResourceLocation SWAMP = minecraft("swamp");
    public static final ResourceLocation JUNGLE = minecraft("jungle");
    public static final ResourceLocation SPARSE_JUNGLE = minecraft("sparse_jungle");
    public static final ResourceLocation SNOWY_TAIGA = minecraft("snowy_taiga");
    public static final ResourceLocation WINDSWEPT_FOREST = minecraft("windswept_forest");

    private BiomeIds() {
    }

    private static ResourceLocation minecraft(String path) {
        return new ResourceLocation("minecraft", path);
    }
}

