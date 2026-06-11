package com.valentin4311.candycraftmod.world.biome.fractal;

import java.util.Map;

public class LayerApplyOceanTemperature extends Layer {
    private final Layer biomes;
    private final Layer ocean;
    private final BiomeInfo trueWarmOcean = BiomeInfo.of(BiomeIds.WARM_OCEAN);
    private final BiomeInfo trueLukewarmOcean = BiomeInfo.of(BiomeIds.LUKEWARM_OCEAN);
    private final BiomeInfo trueOcean = BiomeInfo.of(BiomeIds.OCEAN);
    private final BiomeInfo trueColdOcean = BiomeInfo.of(BiomeIds.COLD_OCEAN);
    private final BiomeInfo trueFrozenOcean = BiomeInfo.of(BiomeIds.FROZEN_OCEAN);
    private final BiomeInfo trueDeepOcean = BiomeInfo.of(BiomeIds.DEEP_OCEAN);
    private final BiomeInfo trueDeepLukewarmOcean = BiomeInfo.of(BiomeIds.DEEP_LUKEWARM_OCEAN);
    private final BiomeInfo trueDeepColdOcean = BiomeInfo.of(BiomeIds.DEEP_COLD_OCEAN);
    private final BiomeInfo trueDeepFrozenOcean = BiomeInfo.of(BiomeIds.DEEP_FROZEN_OCEAN);
    private final Map<BiomeInfo, BiomeInfo> dummyConversionMap;
    private final Map<BiomeInfo, BiomeInfo> dummyDeepConversionMap;

    public LayerApplyOceanTemperature(Layer biomes, Layer ocean, Object ignored) {
        super(0, biomes);
        this.biomes = biomes;
        this.ocean = ocean;
        this.dummyConversionMap = Map.ofEntries(
            Map.entry(DummyBiome.WARM_OCEAN.biomeInfo, this.trueWarmOcean),
            Map.entry(DummyBiome.LUKEWARM_OCEAN.biomeInfo, this.trueLukewarmOcean),
            Map.entry(DummyBiome.OCEAN.biomeInfo, this.trueOcean),
            Map.entry(DummyBiome.COLD_OCEAN.biomeInfo, this.trueColdOcean),
            Map.entry(DummyBiome.FROZEN_OCEAN.biomeInfo, this.trueFrozenOcean)
        );
        this.dummyDeepConversionMap = Map.ofEntries(
            Map.entry(DummyBiome.WARM_OCEAN.biomeInfo, this.trueWarmOcean),
            Map.entry(DummyBiome.LUKEWARM_OCEAN.biomeInfo, this.trueDeepLukewarmOcean),
            Map.entry(DummyBiome.OCEAN.biomeInfo, this.trueDeepOcean),
            Map.entry(DummyBiome.COLD_OCEAN.biomeInfo, this.trueDeepColdOcean),
            Map.entry(DummyBiome.FROZEN_OCEAN.biomeInfo, this.trueDeepFrozenOcean)
        );
    }

    @Override
    public void setWorldSeed(long seed) {
        super.setWorldSeed(seed);
        this.ocean.setWorldSeed(seed);
    }

    @Override
    protected BiomeInfo[] getNewBiomes(int x, int z, int width, int length) {
        int bX = x - 8;
        int bZ = z - 8;
        int bWidth = width + 16;
        int bLength = length + 16;
        BiomeInfo[] biomes = this.biomes.getBiomes(bX, bZ, bWidth, bLength);
        BiomeInfo[] ocean = this.ocean.getBiomes(x, z, width, length);
        BiomeInfo[] output = new BiomeInfo[width * length];

        for (int zz = 0; zz < length; zz++) {
            biomeLoop: for (int xx = 0; xx < width; xx++) {
                BiomeInfo biomeHere = biomes[(xx + 8) + (zz + 8) * bWidth];
                BiomeInfo oceanHere = ocean[xx + zz * width];

                if (!biomeHere.isOcean()) {
                    output[xx + zz * width] = biomeHere;
                    continue;
                }

                if (oceanHere.equals(DummyBiome.WARM_OCEAN.biomeInfo) || oceanHere.equals(DummyBiome.FROZEN_OCEAN.biomeInfo)) {
                    for (int sx = -8; sx <= 8; sx += 4) {
                        for (int sz = -8; sz <= 8; sz += 4) {
                            BiomeInfo biomeThere = biomes[(sx + 8) + (sz + 8) * bWidth];
                            if (!biomeThere.isOcean()) {
                                output[xx + zz * width] = oceanHere.equals(DummyBiome.WARM_OCEAN.biomeInfo) ? this.trueLukewarmOcean : this.trueColdOcean;
                                continue biomeLoop;
                            }
                        }
                    }
                }

                output[xx + zz * width] = (biomeHere.equals(this.trueDeepOcean) ? this.dummyDeepConversionMap : this.dummyConversionMap)
                    .getOrDefault(oceanHere, this.trueOcean);
            }
        }

        return output;
    }
}
