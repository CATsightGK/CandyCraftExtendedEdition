package com.valentin4311.candycraftmod.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCWorldgen;
import com.valentin4311.candycraftmod.world.biome.fractal.BiomeIds;
import com.valentin4311.candycraftmod.world.biome.fractal.BiomeInfo;
import com.valentin4311.candycraftmod.world.biome.fractal.ClimaticBiomeList;
import com.valentin4311.candycraftmod.world.biome.fractal.FractalSettings;
import com.valentin4311.candycraftmod.world.biome.fractal.Layer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

public class CandyBiomeSource extends BiomeSource {
    public static final Codec<CandyBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Biome.CODEC.listOf().fieldOf("biomes").forGetter(source -> source.biomes)
    ).apply(instance, CandyBiomeSource::new));

    private static final long LEGACY_BIOME_SEED_SALT = 1122L;
    private final List<Holder<Biome>> biomes;
    private final Map<String, Holder<Biome>> byPath;
    private final Map<Long, Layer> legacyLayers = new ConcurrentHashMap<>();
    private volatile long worldSeedOverride = Long.MIN_VALUE;

    public CandyBiomeSource(List<Holder<Biome>> biomes) {
        this.biomes = List.copyOf(biomes);
        this.byPath = new HashMap<>();
        for (Holder<Biome> biome : biomes) {
            Optional<ResourceLocation> id = biome.unwrapKey().map(key -> key.location());
            id.ifPresent(location -> byPath.put(location.getPath(), biome));
        }
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CCWorldgen.CANDY_BIOME_SOURCE.get();
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return biomes.stream();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        long worldSeed = worldSeedOverride == Long.MIN_VALUE ? samplerSeed(sampler) : worldSeedOverride;
        Layer legacyLayer = legacyLayers.computeIfAbsent(worldSeed, seed -> Layer.getLayer(seed, create1122Settings()));
        BiomeInfo legacyBiome = legacyLayer.getBiomes(quartX, quartZ, 1, 1)[0];
        String path = mapLegacyBiome(legacyBiome, quartX, quartZ, worldSeed).getPath();
        return byPath.getOrDefault(path, biomes.get(0));
    }

    public void setWorldSeed(long worldSeed) {
        this.worldSeedOverride = worldSeed;
    }

    private static ResourceLocation candy(String path) {
        return new ResourceLocation(CandyCraft.MODID, path);
    }

    private static ResourceLocation mapLegacyBiome(BiomeInfo biome, int quartX, int quartZ, long worldSeed) {
        ResourceLocation id = biome.biome();
        if (id == null) {
            return candy("sugar_plains");
        }

        if (biome.is(BiomeIds.RIVER) || biome.is(BiomeIds.FROZEN_RIVER)) {
            return candy("sugar_river");
        }
        if (biome.isOcean()) {
            return candy("sugar_oceans");
        }
        if (biome.is(BiomeIds.SNOWY_PLAINS)) {
            return biome.type() == 1 ? candy("sugar_mountains") : candy("ice_cream_plains");
        }
        if (biome.is(BiomeIds.SNOWY_TAIGA)) {
            return biome.type() == 1 ? candy("sugar_mountains") : candy("sugar_cold_forest");
        }
        if (biome.is(BiomeIds.SWAMP)) {
            return gummyRegion(quartX, quartZ, worldSeed) ? candy("gummy_swamp") : candy("sugar_plains");
        }
        if (biome.is(BiomeIds.DESERT)) {
            if (biome.type() == 2) {
                return candy("sugar_hell_mountains");
            }
            return biome.type() == 1 ? candy("sugar_mountains") : candy("caramel_forest");
        }
        if (biome.is(BiomeIds.FOREST)) {
            if (biome.type() == 1) {
                return candy("sugar_mountains");
            }
            return chocolateRegion(quartX, quartZ, worldSeed)
                ? candy("chocolate_forest")
                : candy("sugar_forest");
        }
        if (biome.is(BiomeIds.JUNGLE)) {
            return biome.type() == 1 ? candy("sugar_mountains") : candy("sugar_enchanted_forest");
        }
        if (biome.is(BiomeIds.SPARSE_JUNGLE)) {
            return candy("sugar_forest");
        }
        if (biome.is(BiomeIds.WINDSWEPT_FOREST) || biome.is(BiomeIds.STONY_SHORE)) {
            return candy("sugar_mountains");
        }
        if (biome.is(BiomeIds.MUSHROOM_FIELDS)) {
            return candy("sugar_plains");
        }
        if (biome.is(BiomeIds.BEACH)) {
            return candy("sugar_plains");
        }
        if (biome.is(BiomeIds.PLAINS)) {
            if (biome.type() == 1) {
                return candy("sugar_mountains");
            }
            return cottonCandyRegion(quartX, quartZ, worldSeed)
                ? candy("cotton_candy_plains")
                : candy("sugar_plains");
        }
        if (biome.type() == 1) {
            return candy("sugar_mountains");
        }

        return candy("sugar_plains");
    }

    private static boolean gummyRegion(int quartX, int quartZ, long worldSeed) {
        int blockX = quartX << 2;
        int blockZ = quartZ << 2;
        long distanceSq = (long)blockX * blockX + (long)blockZ * blockZ;
        if (distanceSq < 500L * 500L) {
            return false;
        }
        return regionNoise(quartX, quartZ, worldSeed ^ 0x6A6D6D7953555246L, 0.0105D) > 0.0D;
    }

    private static boolean chocolateRegion(int quartX, int quartZ, long worldSeed) {
        return regionNoise(quartX, quartZ, worldSeed ^ 0x5F356495L, 0.008D) > 0.0D;
    }

    private static boolean cottonCandyRegion(int quartX, int quartZ, long worldSeed) {
        return regionNoise(quartX, quartZ, worldSeed ^ 0x34F1A52DL, 0.008D) > 0.0D;
    }

    private static double regionNoise(int quartX, int quartZ, long salt, double scale) {
        return octaveNoise2D(quartX * scale, quartZ * scale, 3, salt);
    }

    private static double octaveNoise2D(double x, double z, int octaves, long salt) {
        double value = 0.0D;
        double amplitude = 1.0D;
        double frequency = 1.0D;
        double total = 0.0D;

        for (int i = 0; i < octaves; ++i) {
            value += smoothNoise2D(x * frequency, z * frequency, salt + i * 0x632BE59BD9B4E019L) * amplitude;
            total += amplitude;
            amplitude *= 0.5D;
            frequency *= 2.0D;
        }

        return value / total;
    }

    private static double smoothNoise2D(double x, double z, long salt) {
        int x0 = (int)Math.floor(x);
        int z0 = (int)Math.floor(z);
        double tx = fade(x - x0);
        double tz = fade(z - z0);
        double a = randomUnit(x0, z0, salt);
        double b = randomUnit(x0 + 1, z0, salt);
        double c = randomUnit(x0, z0 + 1, salt);
        double d = randomUnit(x0 + 1, z0 + 1, salt);
        double ab = a + (b - a) * tx;
        double cd = c + (d - c) * tx;
        return ab + (cd - ab) * tz;
    }

    private static double fade(double value) {
        return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
    }

    private static double randomUnit(int x, int z, long salt) {
        long bits = hash2D(x, z, salt);
        return ((bits >>> 11) * 0x1.0p-53D) * 2.0D - 1.0D;
    }

    private static long hash2D(int x, int z, long salt) {
        long h = salt;
        h ^= x * 0x9E3779B97F4A7C15L;
        h = Long.rotateLeft(h, 27) * 0x94D049BB133111EBL;
        h ^= z * 0x165667B19E3779F9L;
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return h;
    }

    private static long samplerSeed(Climate.Sampler sampler) {
        Climate.TargetPoint point = sampler.sample(0, 0, 0);
        long h = LEGACY_BIOME_SEED_SALT;
        h = mix(h, point.temperature());
        h = mix(h, point.humidity());
        h = mix(h, point.continentalness());
        h = mix(h, point.erosion());
        h = mix(h, point.depth());
        h = mix(h, point.weirdness());
        return h;
    }

    private static long mix(long h, long value) {
        h ^= value + 0x9E3779B97F4A7C15L + (h << 6) + (h >>> 2);
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return h;
    }

    private static FractalSettings create1122Settings() {
        FractalSettings.Builder builder = new FractalSettings.Builder();
        builder.terrainType = FractalSettings.TerrainType.MAJOR_RELEASE;
        builder.biomeScale = 4;
        builder.hillScale = 2;
        builder.oceanShrink = 0;
        builder.beachShrink = 1;
        builder.plains = BiomeInfo.of(BiomeIds.PLAINS);
        builder.icePlains = BiomeInfo.of(BiomeIds.SNOWY_PLAINS);
        builder.addSnow = true;
        builder.addMushroomIslands = true;
        builder.addBeaches = true;
        builder.addStonyShores = true;
        builder.addHills = true;
        builder.addDeepOceans = true;
        builder.addMutations = true;
        builder.useClimaticBiomes = true;
        builder.biomes = List.of(
            BiomeInfo.of(BiomeIds.PLAINS),
            BiomeInfo.of(BiomeIds.PLAINS),
            BiomeInfo.of(BiomeIds.DESERT),
            BiomeInfo.of(BiomeIds.FOREST),
            BiomeInfo.of(BiomeIds.JUNGLE),
            BiomeInfo.of(BiomeIds.SWAMP),
            BiomeInfo.of(BiomeIds.WINDSWEPT_FOREST),
            BiomeInfo.of(BiomeIds.SNOWY_TAIGA),
            BiomeInfo.of(BiomeIds.SNOWY_PLAINS)
        );
        builder.climaticBiomes = List.of(
            new ClimaticBiomeList<>(
                List.of(BiomeInfo.of(BiomeIds.JUNGLE), BiomeInfo.of(BiomeIds.DESERT)),
                List.of(BiomeInfo.of(BiomeIds.JUNGLE), BiomeInfo.of(BiomeIds.JUNGLE), BiomeInfo.of(BiomeIds.PLAINS))
            ),
            new ClimaticBiomeList<>(
                List.of(BiomeInfo.of(BiomeIds.FOREST), BiomeInfo.of(BiomeIds.PLAINS), BiomeInfo.of(BiomeIds.OCEAN)),
                List.of(
                    BiomeInfo.of(BiomeIds.FOREST),
                    BiomeInfo.of(BiomeIds.FOREST),
                    BiomeInfo.of(BiomeIds.PLAINS),
                    BiomeInfo.of(BiomeIds.PLAINS),
                    BiomeInfo.of(BiomeIds.SWAMP)
                )
            ),
            new ClimaticBiomeList<>(
                List.of(
                    BiomeInfo.of(BiomeIds.SNOWY_TAIGA),
                    BiomeInfo.of(BiomeIds.WINDSWEPT_FOREST),
                    BiomeInfo.of(BiomeIds.SNOWY_PLAINS),
                    BiomeInfo.of(BiomeIds.OCEAN)
                ),
                List.of(
                    BiomeInfo.of(BiomeIds.WINDSWEPT_FOREST),
                    BiomeInfo.of(BiomeIds.WINDSWEPT_FOREST),
                    BiomeInfo.of(BiomeIds.WINDSWEPT_FOREST),
                    BiomeInfo.of(BiomeIds.WINDSWEPT_FOREST),
                    BiomeInfo.of(BiomeIds.WINDSWEPT_FOREST),
                    BiomeInfo.of(BiomeIds.WINDSWEPT_FOREST),
                    BiomeInfo.of(BiomeIds.WINDSWEPT_FOREST),
                    BiomeInfo.of(BiomeIds.WINDSWEPT_FOREST),
                    BiomeInfo.of(BiomeIds.WINDSWEPT_FOREST),
                    BiomeInfo.of(BiomeIds.WINDSWEPT_FOREST),
                    BiomeInfo.of(BiomeIds.WINDSWEPT_FOREST),
                    BiomeInfo.of(BiomeIds.DESERT, 2)
                )
            ),
            new ClimaticBiomeList<>(
                List.of(BiomeInfo.of(BiomeIds.DESERT, 2)),
                List.of(BiomeInfo.of(BiomeIds.DESERT, 2))
            )
        );
        builder.hillVariants = Map.ofEntries(
            Map.entry(BiomeInfo.of(BiomeIds.PLAINS), BiomeInfo.of(BiomeIds.PLAINS, 1)),
            Map.entry(BiomeInfo.of(BiomeIds.DESERT), BiomeInfo.of(BiomeIds.DESERT, 1)),
            Map.entry(BiomeInfo.of(BiomeIds.FOREST), BiomeInfo.of(BiomeIds.FOREST, 1)),
            Map.entry(BiomeInfo.of(BiomeIds.JUNGLE), BiomeInfo.of(BiomeIds.JUNGLE, 1)),
            Map.entry(BiomeInfo.of(BiomeIds.WINDSWEPT_FOREST), BiomeInfo.of(BiomeIds.WINDSWEPT_FOREST, 1)),
            Map.entry(BiomeInfo.of(BiomeIds.SNOWY_TAIGA), BiomeInfo.of(BiomeIds.SNOWY_TAIGA, 1)),
            Map.entry(BiomeInfo.of(BiomeIds.SNOWY_PLAINS), BiomeInfo.of(BiomeIds.SNOWY_PLAINS, 1)),
            Map.entry(BiomeInfo.of(BiomeIds.OCEAN, 1), BiomeInfo.of(BiomeIds.DEEP_OCEAN))
        );
        builder.edgeVariants = Map.ofEntries(
            Map.entry(BiomeInfo.of(BiomeIds.JUNGLE), BiomeInfo.of(BiomeIds.SPARSE_JUNGLE, -1)),
            Map.entry(BiomeInfo.of(BiomeIds.DESERT), BiomeInfo.of(BiomeIds.PLAINS))
        );
        builder.mutatedVariants = Map.ofEntries(
            Map.entry(BiomeInfo.of(BiomeIds.PLAINS), BiomeInfo.of(BiomeIds.PLAINS, 1)),
            Map.entry(BiomeInfo.of(BiomeIds.FOREST), BiomeInfo.of(BiomeIds.FOREST, 1)),
            Map.entry(BiomeInfo.of(BiomeIds.JUNGLE), BiomeInfo.of(BiomeIds.JUNGLE, 1)),
            Map.entry(BiomeInfo.of(BiomeIds.SNOWY_PLAINS), BiomeInfo.of(BiomeIds.SNOWY_PLAINS, 1))
        );
        builder.veryRareVariants = Map.of(BiomeInfo.of(BiomeIds.PLAINS), BiomeInfo.of(BiomeIds.PLAINS, 1));
        return builder.build();
    }
}
