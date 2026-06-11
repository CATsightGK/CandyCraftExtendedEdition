package com.valentin4311.candycraftmod.world.biome.fractal;

public class LayerComputeRiver extends Layer {
	private final boolean convertOceans;

	public LayerComputeRiver(long seed, Layer parent, boolean convertOceans) {
		super(seed, parent);
		this.convertOceans = convertOceans;
	}

	@Override
	protected BiomeInfo[] getNewBiomes(int x, int z, int width, int length) {
		return forEachWithNeighbors(x, z, width, length, (b, ix, iz, n) ->
			(!this.convertOceans ? neighborsRiverBorder(n, b) : (
				(b.equals(DummyBiome.OCEAN.biomeInfo) || neighborsContain(n, DummyBiome.OCEAN.biomeInfo))
				|| !allNeighborsEqual(n, b)
			)) ? DummyBiome.RIVER.biomeInfo : BiomeInfo.NONE);
	}

	private static boolean neighborsRiverBorder(BiomeInfo[] neighbors, BiomeInfo match) {
		int matchType = match.type();
		if (matchType >= 2) matchType = matchType % 2 + 2;

		for (BiomeInfo neighbor : neighbors) {
			int nType = neighbor.type();
			if (nType >= 2) nType = nType % 2 + 2;
			if (nType != matchType) return true;
		}
		return false;
	}
}

