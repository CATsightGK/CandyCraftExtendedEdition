package com.valentin4311.candycraftmod.world.biome.fractal;

@FunctionalInterface
interface NeighborLayerOperator {
    BiomeInfo apply(BiomeInfo input, int ix, int iz, BiomeInfo... neighbors);
}
