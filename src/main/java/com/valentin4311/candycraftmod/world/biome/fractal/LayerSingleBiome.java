package com.valentin4311.candycraftmod.world.biome.fractal;


import java.util.Arrays;

public class LayerSingleBiome extends Layer {
    private final BiomeInfo land;

    public LayerSingleBiome(BiomeInfo land) {
        super(0);
        this.land = BiomeInfo.of(land);
    }

    @Override
    protected BiomeInfo[] getNewBiomes(int x, int z, int width, int length) {
        BiomeInfo[] output = new BiomeInfo[width * length];
        Arrays.fill(output, land);
        return output;
    }
}

