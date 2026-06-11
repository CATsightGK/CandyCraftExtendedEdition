package com.valentin4311.candycraftmod.world.biome.fractal;

public class LayerAddSwampRivers extends Layer {
	private final BiomeInfo river;

	public LayerAddSwampRivers(long seed, Layer parent, BiomeInfo river) {
		super(seed, parent);
		this.river = BiomeInfo.of(river);
	}

	@Override
	protected BiomeInfo[] getNewBiomes(int x, int z, int width, int length) {
		return forEach(x, z, width, length, b ->
			b.is(BiomeIds.SWAMP) && nextInt(6) == 0
			|| b.is(BiomeIds.JUNGLE) && nextInt(8) == 0
			? river : b);
	}
}

