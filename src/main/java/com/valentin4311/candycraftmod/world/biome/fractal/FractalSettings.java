package com.valentin4311.candycraftmod.world.biome.fractal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FractalSettings {
    public final List<BiomeInfo> biomes;
    public final List<ClimaticBiomeList<BiomeInfo>> climaticBiomes;
    public final Map<BiomeInfo, BiomeInfo> hillVariants;
    public final Map<BiomeInfo, BiomeInfo> edgeVariants;
    public final Map<BiomeInfo, BiomeInfo> mutatedVariants;
    public final Map<BiomeInfo, BiomeInfo> veryRareVariants;
    public final Map<Integer, Map<BiomeInfo, List<BiomeInfo>>> subVariants;
    public final BiomeInfo plains;
    public final BiomeInfo icePlains;
    public final int biomeScale;
    public final int hillScale;
    public final int subVariantSeed;
    public final int beachShrink;
    public final int oceanShrink;
    public final TerrainType terrainType;
    public final boolean oceans;
    public final boolean addRivers;
    public final boolean addSnow;
    public final boolean addMushroomIslands;
    public final boolean addBeaches;
    public final boolean addStonyShores;
    public final boolean addHills;
    public final boolean addSwampRivers;
    public final boolean addDeepOceans;
    public final boolean addMutations;
    public final boolean addClimaticOceans;
    public final boolean useClimaticBiomes;

    public FractalSettings(Builder builder) {
        biomes = builder.biomes;
        climaticBiomes = builder.climaticBiomes;
        hillVariants = builder.hillVariants;
        edgeVariants = builder.edgeVariants;
        mutatedVariants = builder.mutatedVariants;
        veryRareVariants = builder.veryRareVariants;
        subVariants = builder.subVariants;
        plains = builder.plains;
        icePlains = builder.icePlains;
        biomeScale = builder.biomeScale;
        hillScale = builder.hillScale;
        subVariantSeed = builder.subVariantSeed;
        beachShrink = builder.beachShrink;
        oceanShrink = builder.oceanShrink;
        terrainType = builder.terrainType;
        oceans = builder.oceans;
        addRivers = builder.addRivers;
        addSnow = builder.addSnow;
        addMushroomIslands = builder.addMushroomIslands;
        addBeaches = builder.addBeaches;
        addStonyShores = builder.addStonyShores;
        addHills = builder.addHills;
        addSwampRivers = builder.addSwampRivers;
        addDeepOceans = builder.addDeepOceans;
        addMutations = builder.addMutations;
        addClimaticOceans = builder.addClimaticOceans;
        useClimaticBiomes = builder.useClimaticBiomes;
    }

    public static class Builder {
        public List<BiomeInfo> biomes = List.of();
        public List<ClimaticBiomeList<BiomeInfo>> climaticBiomes = List.of();
        public Map<BiomeInfo, BiomeInfo> hillVariants = Map.of();
        public Map<BiomeInfo, BiomeInfo> edgeVariants = Map.of();
        public Map<BiomeInfo, BiomeInfo> mutatedVariants = Map.of();
        public Map<BiomeInfo, BiomeInfo> veryRareVariants = Map.of();
        public Map<Integer, Map<BiomeInfo, List<BiomeInfo>>> subVariants = new HashMap<>();
        public BiomeInfo plains = BiomeInfo.of(BiomeIds.PLAINS);
        public BiomeInfo icePlains = BiomeInfo.of(BiomeIds.SNOWY_PLAINS);
        public int biomeScale = 4;
        public int hillScale = 2;
        public int subVariantSeed = 3000;
        public int beachShrink = 1;
        public int oceanShrink = 0;
        public TerrainType terrainType = TerrainType.BETA;
        public boolean oceans = true;
        public boolean addRivers = true;
        public boolean addSnow = false;
        public boolean addMushroomIslands = false;
        public boolean addBeaches = false;
        public boolean addStonyShores = false;
        public boolean addHills = false;
        public boolean addSwampRivers = false;
        public boolean addDeepOceans = false;
        public boolean addMutations = false;
        public boolean addClimaticOceans = false;
        public boolean useClimaticBiomes = false;

        public FractalSettings build() {
            return new FractalSettings(this);
        }
    }

    public enum TerrainType {
        BETA,
        EARLY_RELEASE,
        MAJOR_RELEASE
    }
}

