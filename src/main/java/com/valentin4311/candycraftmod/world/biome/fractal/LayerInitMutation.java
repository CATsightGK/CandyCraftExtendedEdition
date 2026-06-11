package com.valentin4311.candycraftmod.world.biome.fractal;

public class LayerInitMutation extends Layer {
	public LayerInitMutation(long seed, Layer parent) {
		super(seed, parent);
	}

	@Override
	protected BiomeInfo[] getNewBiomes(int x, int z, int width, int length) {
		return forEach(x, z, width, length, b -> b.equals(DummyBiome.OCEAN.biomeInfo) ? b : DummyBiome.RIVER.biomeInfo.withType(nextInt(299999) + 2));
	}
}

