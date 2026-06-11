package com.valentin4311.candycraftmod.world.biome.fractal;


public class LayerAddMushroomIsland extends Layer {
	private final BiomeInfo ocean, mushroomIsland;

	public LayerAddMushroomIsland(long seed, Layer parent) {
		this(seed, parent, DummyBiome.OCEAN.biomeInfo, DummyBiome.MUSHROOM_ISLAND.biomeInfo);
	}

	public LayerAddMushroomIsland(long seed, Layer parent, BiomeInfo ocean, BiomeInfo mushroomIsland) {
		super(seed, parent);
		this.ocean = BiomeInfo.of(ocean);
		this.mushroomIsland = BiomeInfo.of(mushroomIsland);
	}

	@Override
	protected BiomeInfo[] getNewBiomes(int x, int z, int width, int length) {
		return forEachWithNeighbors(x, z, width, length,
			(b, ix, iz, n) -> b.equals(ocean) && allNeighborsEqual(n, ocean) && nextInt(100) == 0 ? mushroomIsland : b, true);
	}
}

