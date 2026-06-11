package com.valentin4311.candycraftmod.world.biome.fractal;

import java.util.List;
import java.util.Map;

public class LayerAddBiomes extends Layer {
	private final List<BiomeInfo> biomes;
	private final Map<BiomeInfo, BiomeInfo> replacedBiomes;
	private final List<ClimaticBiomeList<BiomeInfo>> climaticBiomes;

	public LayerAddBiomes(long seed, Layer parent, List<BiomeInfo> biomes,
						  Map<BiomeInfo, BiomeInfo> replacedBiomes,
						  List<ClimaticBiomeList<BiomeInfo>> climaticBiomes) {
		super(seed);
		this.parent = parent;
		this.biomes = biomes;
		this.replacedBiomes = replacedBiomes;
		this.climaticBiomes = climaticBiomes;
	}

	@Override
	protected BiomeInfo[] getNewBiomes(int x, int z, int width, int length) {
		return forEach(x, z, width, length, i -> {
			if (i.equals(DummyBiome.CLIMATE.biomeInfo) && this.climaticBiomes != null) {
				int index = (i.type() & ~0xF00) - 1;
				if (index >= 0 && index < this.climaticBiomes.size()) {
					ClimaticBiomeList<BiomeInfo> climate = this.climaticBiomes.get(index);
					List<BiomeInfo> biomeList = (i.type() & 0xF00) >> 8 > 0 ? climate.rareBiomes() : climate.normalBiomes();
					if (biomeList.isEmpty()) biomeList = climate.normalBiomes();
					if (biomeList.isEmpty()) {
						biomeList = this.biomes;
					}
					return biomeList.get(nextInt(biomeList.size()));
				}
			}
			if (replacedBiomes.containsKey(i)) return replacedBiomes.get(i);
			return this.biomes.get(nextInt(biomes.size()));
		});
	}
}

