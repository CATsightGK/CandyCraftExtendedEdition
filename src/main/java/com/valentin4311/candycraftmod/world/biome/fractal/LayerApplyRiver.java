package com.valentin4311.candycraftmod.world.biome.fractal;


public class LayerApplyRiver extends Layer {
	private final Layer riverLayout;
	private final BiomeInfo ocean, deepOcean, river, snowyLand, frozenRiver;
	private final BiomeInfo mushroomIsland;

	public LayerApplyRiver(Layer parent, Layer riverLayout, BiomeInfo ocean, BiomeInfo deepOcean,
						   BiomeInfo river, BiomeInfo mushroomIsland,
						   BiomeInfo snowyLand, BiomeInfo frozenRiver) {
		super(0, parent);
		this.riverLayout = riverLayout;
		this.ocean = BiomeInfo.of(ocean);
		this.deepOcean = BiomeInfo.of(deepOcean);
		this.river = BiomeInfo.of(river);
		this.mushroomIsland = mushroomIsland;
		this.snowyLand = BiomeInfo.of(snowyLand);
		this.frozenRiver = BiomeInfo.of(frozenRiver);
	}

	@Override
	public void setWorldSeed(long seed) {
		this.riverLayout.setWorldSeed(seed);
		super.setWorldSeed(seed);
	}

	@Override
	protected BiomeInfo[] getNewBiomes(int x, int z, int width, int length) {
		BiomeInfo[] input = this.parent.getBiomes(x, z, width, length);
		BiomeInfo[] inputRiver = this.riverLayout.getBiomes(x, z, width, length);
		BiomeInfo[] output = new BiomeInfo[width * length];

		for (int i = 0; i < width * length; i++) {
			if (input[i].equals(ocean) || input[i].equals(deepOcean)) {
				output[i] = ocean;
			} else if (!inputRiver[i].equals(BiomeInfo.NONE)) {
				if (input[i].equals(snowyLand)) {
					output[i] = frozenRiver;
				} else if (input[i].equals(mushroomIsland)) {
					output[i] = input[i].asSpecial();
				} else if (inputRiver[i].equals(DummyBiome.RIVER.biomeInfo)) {
					output[i] = river;
				} else {
					output[i] = ocean;
				}
			} else {
				output[i] = input[i];
			}
		}

		return output;
	}
}

