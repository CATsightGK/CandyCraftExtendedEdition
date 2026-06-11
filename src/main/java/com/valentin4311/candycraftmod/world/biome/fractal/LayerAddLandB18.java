package com.valentin4311.candycraftmod.world.biome.fractal;


public class LayerAddLandB18 extends Layer {
	private final BiomeInfo ocean, land, frozenOcean, snowyLand;

	public LayerAddLandB18(long seed, Layer parent) {
		this(seed, parent, DummyBiome.OCEAN.biomeInfo, DummyBiome.PLAINS.biomeInfo, DummyBiome.FROZEN_OCEAN.biomeInfo, DummyBiome.ICE_PLAINS.biomeInfo);
	}

	public LayerAddLandB18(long seed, Layer parent, BiomeInfo ocean, BiomeInfo land, BiomeInfo frozenOcean, BiomeInfo snowyLand) {
		super(seed);
		this.parent = parent;
		this.ocean = BiomeInfo.of(ocean);
		this.land = BiomeInfo.of(land);
		this.frozenOcean = BiomeInfo.of(frozenOcean);
		this.snowyLand = BiomeInfo.of(snowyLand);
	}

	@Override
	protected BiomeInfo[] getNewBiomes(int x, int z, int width, int length) {
		return this.forEachWithNeighbors(x, z, width, length, (input, ix, iz, neighbors) -> {
			if (!input.equals(ocean) || allNeighborsEqual(neighbors, ocean)) {
				if (!input.equals(land) || allNeighborsEqual(neighbors, land)) {
					return input;
				} else {
					boolean isLand = 1 - nextInt(5) / 4 > 0;
					return input.equals(snowyLand)
							? isLand ? snowyLand : frozenOcean
							: isLand ? land : ocean;
				}
			} else {
				boolean isLand = nextInt(3) / 2 > 0;
				return input.equals(snowyLand)
						? isLand ? snowyLand : frozenOcean
						: isLand ? land : ocean;
			}
		}, true);
	}
}

