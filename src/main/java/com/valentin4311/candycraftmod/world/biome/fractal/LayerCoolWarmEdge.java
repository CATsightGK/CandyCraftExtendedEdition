package com.valentin4311.candycraftmod.world.biome.fractal;

public class LayerCoolWarmEdge extends Layer {
    public LayerCoolWarmEdge(long seed, Layer parent) {
        super(seed, parent);
    }

    @Override
    protected BiomeInfo[] getNewBiomes(int x, int z, int width, int length) {
        return forEachWithNeighbors(x, z, width, length, (input, ix, iz, neighbors) -> {
            if (!input.equals(DummyBiome.CLIMATE.biomeInfo.withType(1))) return input;
            if (neighborsContain(neighbors, DummyBiome.CLIMATE.biomeInfo.withType(3))
                || neighborsContain(neighbors, DummyBiome.CLIMATE.biomeInfo.withType(4)))
                return input.withType(2);
            return input;
        });
    }
}

