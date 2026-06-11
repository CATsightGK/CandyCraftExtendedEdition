package com.valentin4311.candycraftmod.world.biome.fractal;

import com.valentin4311.candycraftmod.CandyCraft;
import net.minecraft.resources.ResourceLocation;

public final class DummyBiome {
    public static final DummyBiome OCEAN = makeDummy("ocean");
    public static final DummyBiome PLAINS = makeDummy("plains");
    public static final DummyBiome RIVER = makeDummy("river");
    public static final DummyBiome FROZEN_OCEAN = makeDummy("frozen_ocean");
    public static final DummyBiome ICE_PLAINS = makeDummy("ice_plains");
    public static final DummyBiome MUSHROOM_ISLAND = makeDummy("mushroom_island");
    public static final DummyBiome DEEP_OCEAN = makeDummy("deep_ocean");
    public static final DummyBiome CLIMATE = makeDummy("climate");
    public static final DummyBiome WARM_OCEAN = makeDummy("warm_ocean");
    public static final DummyBiome LUKEWARM_OCEAN = makeDummy("lukewarm_ocean");
    public static final DummyBiome COLD_OCEAN = makeDummy("cold_ocean");

    public final ResourceLocation id;
    public final BiomeInfo biomeInfo;

    private DummyBiome(ResourceLocation id) {
        this.id = id;
        this.biomeInfo = BiomeInfo.of(id);
    }

    private static DummyBiome makeDummy(String id) {
        return new DummyBiome(new ResourceLocation(CandyCraft.MODID, "fractal_" + id));
    }
}

