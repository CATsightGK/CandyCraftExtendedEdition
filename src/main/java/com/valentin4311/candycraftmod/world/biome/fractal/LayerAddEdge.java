package com.valentin4311.candycraftmod.world.biome.fractal;

import java.util.Map;

public class LayerAddEdge extends Layer {
	private final BiomeInfo beach, stonyShore, ocean, mushroomIsland;
	private final Map<BiomeInfo, BiomeInfo> edgeVariants;

	public LayerAddEdge(long seed, Layer parent, Map<BiomeInfo, BiomeInfo> edgeVariants, Object ignored) {
		this(seed, parent, null, null, null, null, edgeVariants, ignored);
	}

	public LayerAddEdge(long seed, Layer parent,
						BiomeInfo beach, BiomeInfo stonyShore,
						BiomeInfo ocean, BiomeInfo mushroomIsland,
						Map<BiomeInfo, BiomeInfo> edgeVariants, Object ignored) {
		super(seed, parent);
		this.beach = beach != null ? BiomeInfo.of(beach) : null;
		this.stonyShore = stonyShore != null ? BiomeInfo.of(stonyShore) : null;
		this.ocean = ocean != null ? BiomeInfo.of(ocean) : null;
		this.mushroomIsland = mushroomIsland != null ? BiomeInfo.of(mushroomIsland) : null;
		this.edgeVariants = edgeVariants;
	}

	@Override
	protected BiomeInfo[] getNewBiomes(int x, int z, int width, int length) {
		return forEachWithNeighbors(x, z, width, length, (b, ix, iz, n) -> {
			BiomeInfo edgeVariant;
			if (beach != null && b.equals(mushroomIsland)) {
				return b.asSpecial(neighborsContain(n, ocean));
			} else if (edgeVariants != null && (edgeVariant = edgeVariants.get(b)) != null) {
				if (!allNeighborsEqual(n, b)) {
					return edgeVariant;
				}
			} else if (beach != null && !b.isOcean() && neighborsContain(n, ocean)) {
				if (b.is(BiomeIds.WINDSWEPT_FOREST)) {
					return stonyShore != null ? stonyShore : b;
				}
				return beach;
			}
			return b;
		});
	}
}

