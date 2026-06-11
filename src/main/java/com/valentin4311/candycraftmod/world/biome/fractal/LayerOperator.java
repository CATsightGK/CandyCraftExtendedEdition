package com.valentin4311.candycraftmod.world.biome.fractal;

@FunctionalInterface
interface LayerOperator {
    BiomeInfo apply(BiomeInfo input, int ix, int iz);
}
