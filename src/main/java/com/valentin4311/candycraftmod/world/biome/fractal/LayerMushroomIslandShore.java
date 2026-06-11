package com.valentin4311.candycraftmod.world.biome.fractal;


public class LayerMushroomIslandShore extends Layer {
	private final BiomeInfo mushroomIsland, ocean;

	public LayerMushroomIslandShore(Layer parent, BiomeInfo mushroomIsland, BiomeInfo ocean) {
		super(0, parent);
		this.mushroomIsland = BiomeInfo.of(mushroomIsland);
		this.ocean = BiomeInfo.of(ocean);
	}

	@Override
	protected BiomeInfo[] getNewBiomes(int x, int z, int width, int length) {
		return forEachWithNeighbors(x, z, width, length, (b, ix, iz, n) -> b.equals(mushroomIsland) && neighborsContain(n, ocean) ? b.asSpecial() : b);
	}
}

