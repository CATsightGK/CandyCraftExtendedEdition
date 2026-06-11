package com.valentin4311.candycraftmod.world.biome.fractal;

public class LayerInitRiver extends Layer {
	private final boolean wideRandom;

	public LayerInitRiver(long seed, Layer parent, boolean wideRandom) {
		super(seed, parent);
		this.wideRandom = wideRandom;
	}

	@Override
	protected BiomeInfo[] getNewBiomes(int x, int z, int width, int length) {
		return forEach(x, z, width, length, b -> b.equals(DummyBiome.OCEAN.biomeInfo) ? b :
				(wideRandom ? DummyBiome.RIVER.biomeInfo.withType(nextInt(299999) + 2) :
						DummyBiome.RIVER.biomeInfo.asSpecial(nextInt(2) == 1)));
	}
}

