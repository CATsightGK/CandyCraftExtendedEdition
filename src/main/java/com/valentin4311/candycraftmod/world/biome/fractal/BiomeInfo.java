package com.valentin4311.candycraftmod.world.biome.fractal;

import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public record BiomeInfo(ResourceLocation biome, int type) {
    public static final BiomeInfo NONE = new BiomeInfo(null, 0);

    public static BiomeInfo of(BiomeInfo biome) {
        return biome;
    }

    public static BiomeInfo of(BiomeInfo biome, int type) {
        return biome.withType(type);
    }

    public static BiomeInfo of(ResourceLocation biome) {
        return new BiomeInfo(biome, 0);
    }

    public static BiomeInfo of(ResourceLocation biome, int type) {
        return new BiomeInfo(biome, type);
    }

    public static BiomeInfo fromLookup(Object ignored, ResourceLocation key) {
        return of(key);
    }

    public static BiomeInfo fromLookup(Object ignored, ResourceLocation key, int type) {
        return of(key, type);
    }

    public static BiomeInfo fromId(String id, Object ignored) {
        int type = 0;
        int typeIndex = id.indexOf('*');
        if (typeIndex == 0) {
            type = 1;
            id = id.substring(1);
        } else if (typeIndex > 0) {
            try {
                type = Integer.parseInt(id.substring(0, typeIndex));
            } catch (NumberFormatException ignoredException) {
                type = 0;
            }
            id = id.substring(typeIndex + 1);
        }
        return of(new ResourceLocation(id), type);
    }

    public BiomeInfo asSpecial() {
        return withType(1);
    }

    public BiomeInfo asSpecial(boolean special) {
        return withType(special ? 1 : 0);
    }

    public BiomeInfo withType(int type) {
        return new BiomeInfo(biome, type);
    }

    public boolean is(ResourceLocation id) {
        return Objects.equals(biome, id);
    }

    public boolean isOcean() {
        return is(BiomeIds.OCEAN)
            || is(BiomeIds.DEEP_OCEAN)
            || is(BiomeIds.FROZEN_OCEAN)
            || is(BiomeIds.DEEP_FROZEN_OCEAN)
            || is(BiomeIds.WARM_OCEAN)
            || is(BiomeIds.LUKEWARM_OCEAN)
            || is(BiomeIds.COLD_OCEAN)
            || is(BiomeIds.DEEP_LUKEWARM_OCEAN)
            || is(BiomeIds.DEEP_COLD_OCEAN)
            || is(DummyBiome.OCEAN.id)
            || is(DummyBiome.FROZEN_OCEAN.id)
            || is(DummyBiome.DEEP_OCEAN.id)
            || is(DummyBiome.WARM_OCEAN.id)
            || is(DummyBiome.LUKEWARM_OCEAN.id)
            || is(DummyBiome.COLD_OCEAN.id);
    }

    public String getId() {
        String id = biome.toString();
        return switch (type) {
            case 0 -> id;
            case 1 -> "*" + id;
            default -> type + "*" + id;
        };
    }

    @Override
    public String toString() {
        return getId();
    }
}

