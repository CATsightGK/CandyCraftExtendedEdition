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
import java.util.Set;
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
    private static final int SPAWN_LAND_RADIUS_BLOCKS = 100;
    private static final int SPAWN_LAND_MAX_RADIUS_BLOCKS = 148;
    private static final long SPAWN_ISLAND_NOISE_SALT = 0x534957454554534CL;
    private static final int BIOME_TILE_SIZE = 16;
    private static final double GUMMY_REGION_THRESHOLD = 0.69D;
    private static final double CHOCOLATE_REGION_THRESHOLD = 0.47D;
    private static final double COTTON_CANDY_REGION_THRESHOLD = 0.51D;
    /** Biomes whose surface blocks are not pudding (see CandyWorldChunkGenerator#surfaceMaterials). */
    private static final Set<String> SPECIAL_SURFACE_BIOMES = Set.of(
        "chocolate_forest", "gummy_swamp");
    private static final String SUGAR_RIVER_PATH = "sugar_river";
    private static final String SUGAR_OCEANS_PATH = "sugar_oceans";
    private final List<Holder<Biome>> biomes;
    private final Map<String, Holder<Biome>> byPath;
    private final Map<Long, Layer> legacyLayers = new ConcurrentHashMap<>();
    /** Final biome samples are requested repeatedly while terrain and surfaces are generated. */
    private final Map<Long, Holder<Biome>> biomeSamples = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> spawnLandNeeded = new ConcurrentHashMap<>();
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
        long sampleKey = packCoordinates(quartX, quartZ);
        Holder<Biome> cached = biomeSamples.get(sampleKey);
        if (cached != null) {
            return cached;
        }

        Layer legacyLayer = legacyLayers.computeIfAbsent(worldSeed, seed -> Layer.getLayer(seed, create1122Settings()));
        if (worldSeedOverride == Long.MIN_VALUE) {
            // The real world seed is only known once chunk generation starts. Never cache these
            // fallback-seed samples: a stale tile survives the later seed sync and shows up as a
            // square patch of biomes that does not match the surrounding world.
            boolean needsSpawnLand = spawnLandNeeded.computeIfAbsent(
                worldSeed, seed -> isOceanLike(legacyLayer.getBiomes(0, 0, 1, 1)[0]));
            ResourceLocation mapped = borderRiverOr(
                rawMappedBiome(legacyLayer, quartX, quartZ, worldSeed, needsSpawnLand),
                rawMappedBiome(legacyLayer, quartX - 1, quartZ, worldSeed, needsSpawnLand),
                rawMappedBiome(legacyLayer, quartX + 1, quartZ, worldSeed, needsSpawnLand),
                rawMappedBiome(legacyLayer, quartX, quartZ - 1, worldSeed, needsSpawnLand),
                rawMappedBiome(legacyLayer, quartX, quartZ + 1, worldSeed, needsSpawnLand));
            return byPath.getOrDefault(mapped.getPath(), biomes.get(0));
        }
        synchronized (biomeSamples) {
            cached = biomeSamples.get(sampleKey);
            if (cached != null) {
                return cached;
            }

            // Sample one ring of margin cells around the tile so the special/normal
            // surface border pass can inspect every cell's four neighbors.
            int tileX = Math.floorDiv(quartX, BIOME_TILE_SIZE) * BIOME_TILE_SIZE;
            int tileZ = Math.floorDiv(quartZ, BIOME_TILE_SIZE) * BIOME_TILE_SIZE;
            int gridSize = BIOME_TILE_SIZE + 2;
            int originX = tileX - 1;
            int originZ = tileZ - 1;
            BiomeInfo[] legacyTile = legacyLayer.getBiomes(originX, originZ, gridSize, gridSize);
            if (biomeSamples.size() + BIOME_TILE_SIZE * BIOME_TILE_SIZE > 65536) {
                biomeSamples.clear();
            }
            boolean needsSpawnLand = spawnLandNeeded.computeIfAbsent(
                worldSeed, seed -> isOceanLike(legacyLayer.getBiomes(0, 0, 1, 1)[0]));
            ResourceLocation[] rawGrid = new ResourceLocation[gridSize * gridSize];
            for (int i = 0; i < rawGrid.length; ++i) {
                int sampleX = originX + i % gridSize;
                int sampleZ = originZ + i / gridSize;
                rawGrid[i] = rawMappedBiome(legacyLayer, legacyTile[i], sampleX, sampleZ, worldSeed, needsSpawnLand);
            }
            for (int localZ = 0; localZ < BIOME_TILE_SIZE; ++localZ) {
                for (int localX = 0; localX < BIOME_TILE_SIZE; ++localX) {
                    int gridIndex = (localX + 1) + (localZ + 1) * gridSize;
                    ResourceLocation mapped = borderRiverOr(
                        rawGrid[gridIndex],
                        rawGrid[gridIndex - 1],
                        rawGrid[gridIndex + 1],
                        rawGrid[gridIndex - gridSize],
                        rawGrid[gridIndex + gridSize]);
                    Holder<Biome> result = byPath.getOrDefault(mapped.getPath(), biomes.get(0));
                    biomeSamples.put(packCoordinates(tileX + localX, tileZ + localZ), result);
                }
            }
            return biomeSamples.get(sampleKey);
        }
    }

    public void setWorldSeed(long worldSeed) {
        if (this.worldSeedOverride != worldSeed) {
            synchronized (biomeSamples) {
                biomeSamples.clear();
            }
            legacyLayers.clear();
            spawnLandNeeded.clear();
        }
        this.worldSeedOverride = worldSeed;
    }

    private ResourceLocation rawMappedBiome(Layer legacyLayer, int quartX, int quartZ, long worldSeed, boolean needsSpawnLand) {
        return rawMappedBiome(legacyLayer, legacyLayer.getBiomes(quartX, quartZ, 1, 1)[0], quartX, quartZ, worldSeed, needsSpawnLand);
    }

    private static ResourceLocation rawMappedBiome(Layer legacyLayer, BiomeInfo legacyBiome, int quartX, int quartZ,
            long worldSeed, boolean needsSpawnLand) {
        return needsSpawnLand && isSpawnLandRadius(quartX, quartZ, worldSeed) && isOceanLike(legacyBiome)
            ? candy("sugar_plains")
            : mapLegacyBiome(legacyBiome, quartX, quartZ, worldSeed);
    }

    /**
     * Guarantees a river between special-surface biomes and the normal pudding
     * biomes: a special cell bordering ordinary land becomes sugar_river, which
     * the chunk generator carves into a real fluid channel. Rivers and oceans
     * are left untouched.
     */
    private static ResourceLocation borderRiverOr(ResourceLocation self, ResourceLocation west, ResourceLocation east,
            ResourceLocation north, ResourceLocation south) {
        if (!isSpecialSurface(self)) {
            return self;
        }
        if (isNormalLand(west) || isNormalLand(east) || isNormalLand(north) || isNormalLand(south)) {
            return candy(SUGAR_RIVER_PATH);
        }
        return self;
    }

    private static boolean isSpecialSurface(ResourceLocation id) {
        return id != null && SPECIAL_SURFACE_BIOMES.contains(id.getPath());
    }

    private static boolean isNormalLand(ResourceLocation id) {
        return id != null && !SPECIAL_SURFACE_BIOMES.contains(id.getPath())
            && !SUGAR_RIVER_PATH.equals(id.getPath()) && !SUGAR_OCEANS_PATH.equals(id.getPath());
    }

    private static long packCoordinates(int x, int z) {
        return ((long) x << 32) ^ (z & 0xFFFFFFFFL);
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
        ResourceLocation base = mapBaseBiome(biome, quartX, quartZ, worldSeed);
        // Chocolate forest, cotton candy plains and gummy swamp are standalone region biomes:
        // they never take part in the forest/plains/swamp generation lists and only appear where
        // their own region noise fields peak. The overlay covers every land biome (only rivers
        // and oceans are exempt, handled by the early returns above): masking it to flat
        // temperate land clipped each region at mountain or cold-biome borders and left thin
        // biome slivers that carved canyon-like seams into the terrain.
        ResourceLocation region = regionBiome(biome, quartX, quartZ, worldSeed);
        if (region != null) {
            return region;
        }
        return base;
    }

    private static ResourceLocation mapBaseBiome(BiomeInfo biome, int quartX, int quartZ, long worldSeed) {
        if (biome.is(BiomeIds.SNOWY_PLAINS)) {
            if (biome.type() == 1) {
                return skyMountainRegion(quartX, quartZ, worldSeed)
                    ? candy("ice_cream_sky_mountains")
                    : candy("sugar_mountains");
            }
            return candy("ice_cream_plains");
        }
        if (biome.is(BiomeIds.SNOWY_TAIGA)) {
            if (biome.type() == 1) {
                return skyMountainRegion(quartX, quartZ, worldSeed)
                    ? candy("ice_cream_sky_mountains")
                    : candy("sugar_mountains");
            }
            return candy("sugar_cold_forest");
        }
        if (biome.is(BiomeIds.SWAMP)) {
            return candy("sugar_plains");
        }
        if (biome.is(BiomeIds.DESERT)) {
            if (biome.type() == 2) {
                return candy("sugar_hell_mountains");
            }
            return biome.type() == 1 ? candy("sugar_mountains") : candy("caramel_forest");
        }
        if (biome.is(BiomeIds.FOREST)) {
            return biome.type() == 1 ? candy("sugar_mountains") : candy("sugar_forest");
        }
        if (biome.is(BiomeIds.JUNGLE)) {
            return biome.type() == 1 ? candy("sugar_mountains") : candy("sugar_enchanted_forest");
        }
        if (biome.is(BiomeIds.SPARSE_JUNGLE)) {
            return candy("sugar_forest");
        }
        if (biome.is(BiomeIds.WINDSWEPT_FOREST) || biome.is(BiomeIds.STONY_SHORE)) {
            return skyMountainRegion(quartX, quartZ, worldSeed)
                ? candy("ice_cream_sky_mountains")
                : candy("sugar_mountains");
        }
        if (biome.is(BiomeIds.MUSHROOM_FIELDS)) {
            return candy("sugar_plains");
        }
        if (biome.is(BiomeIds.BEACH)) {
            return candy("sugar_plains");
        }
        if (biome.is(BiomeIds.PLAINS)) {
            return biome.type() == 1 ? candy("sugar_mountains") : candy("sugar_plains");
        }
        if (biome.type() == 1) {
            return candy("sugar_mountains");
        }

        return candy("sugar_plains");
    }

    private static ResourceLocation regionBiome(BiomeInfo biome, int quartX, int quartZ, long worldSeed) {
        if (gummyRegionNoise(quartX, quartZ, worldSeed) > GUMMY_REGION_THRESHOLD) {
            return candy("gummy_swamp");
        }
        // Chocolate forest is a warm biome: it accompanies sugar forests and never intrudes
        // into the cold climates (ice cream plains, sugar cold forest, sky mountains).
        if (!isColdClimate(biome)
            && regionNoise(quartX, quartZ, worldSeed ^ 0x5F356495L, 0.008D) > CHOCOLATE_REGION_THRESHOLD) {
            return candy("chocolate_forest");
        }
        if (regionNoise(quartX, quartZ, worldSeed ^ 0x34F1A52DL, 0.008D) > COTTON_CANDY_REGION_THRESHOLD) {
            return candy("cotton_candy_plains");
        }
        return null;
    }

    private static boolean isColdClimate(BiomeInfo biome) {
        return biome.is(BiomeIds.SNOWY_PLAINS) || biome.is(BiomeIds.SNOWY_TAIGA)
            || biome.is(BiomeIds.WINDSWEPT_FOREST);
    }

    private static boolean isSpawnLandRadius(int quartX, int quartZ, long worldSeed) {
        long blockX = (long)quartX << 2;
        long blockZ = (long)quartZ << 2;
        return isWithinSpawnIsland(blockX, blockZ, worldSeed);
    }

    static boolean isWithinSpawnIsland(double blockX, double blockZ, long worldSeed) {
        double distance = Math.sqrt(blockX * blockX + blockZ * blockZ);
        if (distance <= SPAWN_LAND_RADIUS_BLOCKS) {
            return true;
        }
        if (distance > SPAWN_LAND_MAX_RADIUS_BLOCKS) {
            return false;
        }
        return distance <= spawnIslandRadius(blockX, blockZ, worldSeed);
    }

    static double spawnIslandInfluence(double blockX, double blockZ, long worldSeed) {
        double distance = Math.sqrt(blockX * blockX + blockZ * blockZ);
        if (distance <= SPAWN_LAND_RADIUS_BLOCKS) {
            return 1.0D;
        }

        double radius = spawnIslandRadius(blockX, blockZ, worldSeed);
        if (distance >= radius) {
            return 0.0D;
        }

        double blend = (radius - distance) / Math.max(radius - SPAWN_LAND_RADIUS_BLOCKS, 1.0D);
        blend = Math.max(0.0D, Math.min(1.0D, blend));
        return blend * blend * (3.0D - 2.0D * blend);
    }

    private static double spawnIslandRadius(double blockX, double blockZ, long worldSeed) {
        double broad = octaveNoise2D(blockX * 0.016D, blockZ * 0.016D, 4, worldSeed ^ SPAWN_ISLAND_NOISE_SALT);
        double detail = octaveNoise2D(blockX * 0.045D, blockZ * 0.045D, 2, worldSeed ^ 0x1C1A2D5E7B9A531FL);
        double radius = 124.0D + broad * 22.0D + detail * 7.0D;
        return Math.max(SPAWN_LAND_RADIUS_BLOCKS + 2.0D, Math.min(SPAWN_LAND_MAX_RADIUS_BLOCKS, radius));
    }

    private static boolean isWaterLike(BiomeInfo biome) {
        return biome != null && (biome.isOcean() || biome.is(BiomeIds.RIVER) || biome.is(BiomeIds.FROZEN_RIVER));
    }

    private static boolean isOceanLike(BiomeInfo biome) {
        return biome != null && biome.isOcean();
    }

    private static double gummyRegionNoise(int quartX, int quartZ, long worldSeed) {
        int blockX = quartX << 2;
        int blockZ = quartZ << 2;
        long distanceSq = (long)blockX * blockX + (long)blockZ * blockZ;
        if (distanceSq < 500L * 500L) {
            return -1.0D;
        }
        return regionNoise(quartX, quartZ, worldSeed ^ 0x6A6D6D7953555246L, 0.0105D);
    }

    private static boolean skyMountainRegion(int quartX, int quartZ, long worldSeed) {
        return regionNoise(quartX, quartZ, worldSeed ^ 0x15CE5CA1E5L, 0.006D) > 0.18D;
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
                List.of(
                    BiomeInfo.of(BiomeIds.DESERT, 2)
                ),
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
