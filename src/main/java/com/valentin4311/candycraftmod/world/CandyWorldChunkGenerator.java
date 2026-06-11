package com.valentin4311.candycraftmod.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCSweetscapeBlocks;
import com.valentin4311.candycraftmod.registry.CCWorldgen;
import com.valentin4311.candycraftmod.world.noise.LegacyPerlinOctaveNoise;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

public class CandyWorldChunkGenerator extends ChunkGenerator {
    public static final Codec<CandyWorldChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource)
    ).apply(instance, CandyWorldChunkGenerator::new));

    private static final int MIN_Y = 0;
    private static final int HEIGHT = 256;
    private static final int SEA_LEVEL = 63;
    private static final int LAVA_LEVEL = 10;
    private static final int CARVER_RANGE = 8;
    private static final int NOISE_SIZE_XZ = 5;
    private static final int NOISE_SIZE_Y = 33;
    private static final int CELL_WIDTH = 4;
    private static final int CELL_HEIGHT = 8;
    private static final double COORDINATE_SCALE = 684.412D;
    private static final double HEIGHT_SCALE = 684.412D;
    private static final double MAIN_NOISE_SCALE_XZ = 80.0D;
    private static final double MAIN_NOISE_SCALE_Y = 160.0D;
    private static final double LOWER_LIMIT_SCALE = 512.0D;
    private static final double UPPER_LIMIT_SCALE = 512.0D;
    private static final double DEPTH_NOISE_SCALE_XZ = 200.0D;
    private static final double BIOME_DEPTH_WEIGHT = 1.0D;
    private static final double BIOME_SCALE_WEIGHT = 1.0D;
    private static final double BIOME_DEPTH_OFFSET = 0.0D;
    private static final double BIOME_SCALE_OFFSET = 0.0D;
    private static final double BASE_SIZE = 8.5D;
    private static final double STRETCH_Y = 12.0D;
    private static final ResourceLocation PLAINS = new ResourceLocation("minecraft", "plains");
    private static final ResourceLocation SUGAR_OCEANS = new ResourceLocation(CandyCraft.MODID, "sugar_oceans");
    private static final ResourceLocation SUGAR_RIVER = new ResourceLocation(CandyCraft.MODID, "sugar_river");
    private static final ResourceLocation COTTON_CANDY_PLAINS = new ResourceLocation(CandyCraft.MODID, "cotton_candy_plains");
    private static final ResourceLocation CHOCOLATE_FOREST = new ResourceLocation(CandyCraft.MODID, "chocolate_forest");
    private static final ResourceLocation GUMMY_SWAMP = new ResourceLocation(CandyCraft.MODID, "gummy_swamp");
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState WATER = Blocks.WATER.defaultBlockState();
    private static final BlockState BEDROCK = Blocks.BEDROCK.defaultBlockState();

    private static final float[] PARABOLIC_FIELD = makeParabolicField();
    private static final Map<Long, LegacyTerrainNoise> TERRAIN_NOISE = new ConcurrentHashMap<>();

    public CandyWorldChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CCWorldgen.CANDY_WORLD_CHUNK_GENERATOR.get();
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState,
            StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.supplyAsync(() -> {
            syncBiomeSourceSeed(randomState);
            fillLegacyTerrain(chunk, randomState);
            Heightmap.primeHeightmaps(chunk, Set.of(
                Heightmap.Types.WORLD_SURFACE_WG,
                Heightmap.Types.OCEAN_FLOOR_WG,
                Heightmap.Types.MOTION_BLOCKING,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES
            ));
            return chunk;
        }, executor);
    }

    private void syncBiomeSourceSeed(RandomState randomState) {
        if (getBiomeSource() instanceof CandyBiomeSource candyBiomeSource) {
            candyBiomeSource.setWorldSeed(worldSeed(randomState));
        }
    }

    private static long worldSeed(RandomState randomState) {
        return randomState.getOrCreateRandomFactory(new ResourceLocation(CandyCraft.MODID, "legacy_major_release"))
            .fromHashOf("world")
            .nextLong();
    }

    private void fillLegacyTerrain(ChunkAccess chunk, RandomState randomState) {
        ChunkPos pos = chunk.getPos();
        double[] heightMap = generateHeightMap(pos.x * 4, pos.z * 4, randomState);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int cellX = 0; cellX < 4; ++cellX) {
            int x0 = cellX * NOISE_SIZE_XZ;
            int x1 = (cellX + 1) * NOISE_SIZE_XZ;

            for (int cellZ = 0; cellZ < 4; ++cellZ) {
                int z0 = (x0 + cellZ) * NOISE_SIZE_Y;
                int z1 = (x0 + cellZ + 1) * NOISE_SIZE_Y;
                int z2 = (x1 + cellZ) * NOISE_SIZE_Y;
                int z3 = (x1 + cellZ + 1) * NOISE_SIZE_Y;

                for (int cellY = 0; cellY < 32; ++cellY) {
                    double density000 = heightMap[z0 + cellY];
                    double density001 = heightMap[z1 + cellY];
                    double density100 = heightMap[z2 + cellY];
                    double density101 = heightMap[z3 + cellY];
                    double step000 = (heightMap[z0 + cellY + 1] - density000) / CELL_HEIGHT;
                    double step001 = (heightMap[z1 + cellY + 1] - density001) / CELL_HEIGHT;
                    double step100 = (heightMap[z2 + cellY + 1] - density100) / CELL_HEIGHT;
                    double step101 = (heightMap[z3 + cellY + 1] - density101) / CELL_HEIGHT;

                    for (int subY = 0; subY < CELL_HEIGHT; ++subY) {
                        double yLerp0 = density000;
                        double yLerp1 = density001;
                        double xStep0 = (density100 - density000) / CELL_WIDTH;
                        double xStep1 = (density101 - density001) / CELL_WIDTH;

                        for (int subX = 0; subX < CELL_WIDTH; ++subX) {
                            double density = yLerp0;
                            double zStep = (yLerp1 - yLerp0) / CELL_WIDTH;

                            for (int subZ = 0; subZ < CELL_WIDTH; ++subZ) {
                                int localX = cellX * CELL_WIDTH + subX;
                                int localZ = cellZ * CELL_WIDTH + subZ;
                                int worldX = pos.getBlockX(localX);
                                int worldZ = pos.getBlockZ(localZ);
                                int y = cellY * CELL_HEIGHT + subY;

                                BlockState state = density > 0.0D ? baseStone() : y < SEA_LEVEL ? fluidForBiome(biomeId(worldX, worldZ, randomState)) : AIR;
                                if (!state.isAir() && y >= chunk.getMinBuildHeight() && y < chunk.getMaxBuildHeight()) {
                                    chunk.setBlockState(mutable.set(worldX, y, worldZ), state, false);
                                }
                                density += zStep;
                            }

                            yLerp0 += xStep0;
                            yLerp1 += xStep1;
                        }

                        density000 += step000;
                        density001 += step001;
                        density100 += step100;
                        density101 += step101;
                    }
                }
            }
        }

        applyBedrock(chunk);
        carveLegacyCaves(chunk, randomState);
    }

    private double[] generateHeightMap(int baseNoiseX, int baseNoiseZ, RandomState randomState) {
        double[] heightMap = new double[NOISE_SIZE_XZ * NOISE_SIZE_Y * NOISE_SIZE_XZ];
        LegacyTerrainNoise noise = terrainNoise(randomState);
        int index = 0;

        for (int x = 0; x < NOISE_SIZE_XZ; ++x) {
            for (int z = 0; z < NOISE_SIZE_XZ; ++z) {
                HeightConfig heightConfig = heightConfigAt(baseNoiseX + x, baseNoiseZ + z, randomState);

                for (int y = 0; y < NOISE_SIZE_Y; ++y) {
                    heightMap[index++] = sampleMajorReleaseDensity(noise, baseNoiseX + x, y, baseNoiseZ + z, heightConfig);
                }
            }
        }

        return heightMap;
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState,
            ChunkAccess chunk) {
        syncBiomeSourceSeed(randomState);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        ChunkPos pos = chunk.getPos();

        for (int localX = 0; localX < 16; ++localX) {
            int worldX = pos.getBlockX(localX);
            for (int localZ = 0; localZ < 16; ++localZ) {
                int worldZ = pos.getBlockZ(localZ);
                ResourceLocation biomeId = biomeId(worldX, worldZ, randomState);
                SurfaceMaterials materials = surfaceMaterials(biomeId, worldX, worldZ, randomState);
                int top = findTopSolid(chunk, worldX, worldZ);
                boolean underwater = top < SEA_LEVEL - 1 || biomeId.equals(SUGAR_OCEANS) || biomeId.equals(SUGAR_RIVER);
                int depth = 3 + Math.abs(hash(worldX, 0, worldZ)) % 3;
                int replaced = 0;

                for (int y = top; y > MIN_Y && replaced <= depth; --y) {
                    BlockState state = chunk.getBlockState(mutable.set(worldX, y, worldZ));
                    if (!isBaseStone(state)) {
                        if (replaced > 0) {
                            break;
                        }
                        continue;
                    }

                    chunk.setBlockState(mutable, underwater ? underwaterMaterial(replaced) : replaced > 0 ? materials.under() : materials.top(), false);
                    replaced++;
                }
            }
        }

        generateSurfacePools(chunk, randomState);
    }

    private void generateSurfacePools(ChunkAccess chunk, RandomState randomState) {
        ChunkPos pos = chunk.getPos();
        long seed = worldSeed(randomState);
        long poolRoll = positiveHash(pos.x, pos.z, 0, seed ^ 0x4752454E4144494EL);
        if (poolRoll % 520L == 0L) {
            generateSurfacePool(chunk, randomState, CCBlocks.GRENADINE.get().defaultBlockState(), seed ^ 0x6C616B655F677265L);
        }

        long candyRoll = positiveHash(pos.x, pos.z, 0, seed ^ 0x4C49515549444341L);
        if (candyRoll % 620L == 0L) {
            generateSurfacePool(chunk, randomState, liquidCandy(), seed ^ 0x6C616B655F63616EL);
        }
    }

    private void generateSurfacePool(ChunkAccess chunk, RandomState randomState, BlockState fluid, long salt) {
        ChunkPos pos = chunk.getPos();
        long bits = positiveHash(pos.x, pos.z, 0, salt);
        int centerX = pos.getBlockX(4 + (int)(bits & 7L));
        int centerZ = pos.getBlockZ(4 + (int)((bits >>> 3) & 7L));
        ResourceLocation biomeId = biomeId(centerX, centerZ, randomState);
        if (biomeId.equals(SUGAR_OCEANS) || biomeId.equals(SUGAR_RIVER)) {
            return;
        }

        int centerTop = findTopTerrain(chunk, centerX, centerZ);
        if (centerTop <= SEA_LEVEL - 4 || centerTop >= HEIGHT - 3) {
            return;
        }

        int radiusX = 2 + (int)((bits >>> 6) & 1L);
        int radiusZ = 2 + (int)((bits >>> 7) & 1L);
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        int waterline = centerTop - 1;

        for (int dx = -radiusX - 1; dx <= radiusX + 1; ++dx) {
            int worldX = centerX + dx;
            int localX = worldX - pos.getMinBlockX();
            if (localX < 0 || localX >= 16) {
                continue;
            }

            for (int dz = -radiusZ - 1; dz <= radiusZ + 1; ++dz) {
                int worldZ = centerZ + dz;
                int localZ = worldZ - pos.getMinBlockZ();
                if (localZ < 0 || localZ >= 16) {
                    continue;
                }

                double nx = dx / (double)radiusX;
                double nz = dz / (double)radiusZ;
                double edgeNoise = ((positiveHash(worldX, worldZ, 0, salt) >>> 8) & 255L) / 255.0D * 0.12D;
                double distance = nx * nx + nz * nz;
                if (distance > 1.0D + edgeNoise) {
                    continue;
                }

                int top = findTopTerrain(chunk, worldX, worldZ);
                if (Math.abs(top - centerTop) > 2) {
                    continue;
                }

                boolean inner = distance < 0.62D;
                BlockState rim = surfaceMaterials(biomeId(worldX, worldZ, randomState), worldX, worldZ, randomState).under();
                for (int y = top + 2; y > waterline; --y) {
                    chunk.setBlockState(mutable.set(worldX, y, worldZ), AIR, false);
                }
                if (inner) {
                    chunk.setBlockState(mutable.set(worldX, waterline, worldZ), fluid, false);
                    chunk.setBlockState(mutable.set(worldX, waterline - 1, worldZ), fluid, false);
                } else {
                    chunk.setBlockState(mutable.set(worldX, waterline, worldZ), AIR, false);
                    chunk.setBlockState(mutable.set(worldX, waterline - 1, worldZ), rim, false);
                }
                chunk.setBlockState(mutable.set(worldX, waterline - 2, worldZ), rim, false);
            }
        }
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState randomState, BiomeManager biomeManager,
            StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving carvingStep) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public int getGenDepth() {
        return HEIGHT;
    }

    @Override
    public int getSeaLevel() {
        return SEA_LEVEL;
    }

    @Override
    public int getMinY() {
        return MIN_Y;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor,
            RandomState randomState) {
        syncBiomeSourceSeed(randomState);
        BlockState[] column = buildColumn(x, z, heightAccessor, randomState);
        for (int i = column.length - 1; i >= 0; --i) {
            BlockState state = column[i];
            if (type.isOpaque().test(state) || type == Heightmap.Types.WORLD_SURFACE_WG && !state.isAir()) {
                return heightAccessor.getMinBuildHeight() + i + 1;
            }
        }
        return heightAccessor.getMinBuildHeight();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        syncBiomeSourceSeed(randomState);
        return new NoiseColumn(heightAccessor.getMinBuildHeight(), buildColumn(x, z, heightAccessor, randomState));
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        syncBiomeSourceSeed(randomState);
        info.add("CandyCraft legacy 1.12-style density terrain");
        info.add("Candy biome shape: " + biomeShape(pos.getX(), pos.getZ(), randomState));
    }

    private BlockState[] buildColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        int minY = heightAccessor.getMinBuildHeight();
        int height = heightAccessor.getHeight();
        BlockState[] states = new BlockState[height];
        LegacyTerrainNoise noise = terrainNoise(randomState);
        double noiseX = x / (double)CELL_WIDTH;
        double noiseZ = z / (double)CELL_WIDTH;
        HeightConfig heightConfig = heightConfigAt(Mth.floor(noiseX), Mth.floor(noiseZ), randomState);

        for (int i = 0; i < height; ++i) {
            int y = minY + i;
            double noiseY = y / (double)CELL_HEIGHT;
            double density = sampleMajorReleaseDensity(noise, noiseX, noiseY, noiseZ, heightConfig);
            if (y <= MIN_Y) {
                states[i] = BEDROCK;
            } else if (density > 0.0D) {
                states[i] = baseStone();
            } else if (y < SEA_LEVEL) {
                states[i] = fluidForBiome(biomeId(x, z, randomState));
            } else {
                states[i] = AIR;
            }
        }

        return states;
    }

    private static LegacyTerrainNoise terrainNoise(RandomState randomState) {
        long seed = randomState.getOrCreateRandomFactory(new ResourceLocation(CandyCraft.MODID, "legacy_major_release"))
            .fromHashOf("terrain")
            .nextLong();
        return TERRAIN_NOISE.computeIfAbsent(seed, LegacyTerrainNoise::new);
    }

    private HeightConfig heightConfigAt(double noiseX, double noiseZ, RandomState randomState) {
        BiomeShape center = biomeShape(Mth.floor(noiseX * CELL_WIDTH), Mth.floor(noiseZ * CELL_WIDTH), randomState);
        double scale = 0.0D;
        double depth = 0.0D;
        double totalWeight = 0.0D;

        for (int offX = -2; offX <= 2; ++offX) {
            for (int offZ = -2; offZ <= 2; ++offZ) {
                BiomeShape nearby = biomeShape(Mth.floor((noiseX + offX) * CELL_WIDTH), Mth.floor((noiseZ + offZ) * CELL_WIDTH), randomState);
                double nearbyScale = BIOME_SCALE_OFFSET + nearby.variation() * BIOME_SCALE_WEIGHT;
                double nearbyDepth = BIOME_DEPTH_OFFSET + nearby.baseHeight() * BIOME_DEPTH_WEIGHT;
                double weight = PARABOLIC_FIELD[offX + 2 + (offZ + 2) * 5] / Math.max(nearbyDepth + 2.0D, 0.01D);

                if (nearby.baseHeight() > center.baseHeight()) {
                    weight *= 0.5D;
                }

                scale += nearbyScale * weight;
                depth += nearbyDepth * weight;
                totalWeight += weight;
            }
        }

        scale /= totalWeight;
        depth /= totalWeight;
        scale = scale * 0.9D + 0.1D;
        depth = (depth * 4.0D - 1.0D) / 8.0D;

        return new HeightConfig(depth, scale);
    }

    private static double sampleMajorReleaseDensity(LegacyTerrainNoise noise, double noiseX, double noiseY, double noiseZ, HeightConfig heightConfig) {
        double depthNoise = noise.depth.sampleXZWrapped(noiseX, noiseZ, DEPTH_NOISE_SCALE_XZ, DEPTH_NOISE_SCALE_XZ);
        depthNoise /= 8000.0D;

        if (depthNoise < 0.0D) {
            depthNoise = -depthNoise * 0.3D;
        }

        depthNoise = depthNoise * 3.0D - 2.0D;

        if (depthNoise < 0.0D) {
            depthNoise /= 2.0D;
            depthNoise = Math.max(depthNoise, -1.0D);
            depthNoise /= 1.4D;
            depthNoise /= 2.0D;
        } else {
            depthNoise = Math.min(depthNoise, 1.0D);
            depthNoise /= 8.0D;
        }

        double depth = heightConfig.depth() + depthNoise * 0.2D;
        depth *= BASE_SIZE / 8.0D;
        depth = BASE_SIZE + depth * 4.0D;

        double densityOffset = ((noiseY - depth) * STRETCH_Y) / heightConfig.scale();
        if (densityOffset < 0.0D) {
            densityOffset *= 4.0D;
        }

        double mainNoise = (noise.main.sampleWrapped(
            noiseX,
            noiseY,
            noiseZ,
            COORDINATE_SCALE / MAIN_NOISE_SCALE_XZ,
            HEIGHT_SCALE / MAIN_NOISE_SCALE_Y,
            COORDINATE_SCALE / MAIN_NOISE_SCALE_XZ
        ) / 10.0D + 1.0D) / 2.0D;

        double density;
        if (mainNoise < 0.0D) {
            density = noise.minLimit.sampleWrapped(noiseX, noiseY, noiseZ, COORDINATE_SCALE, HEIGHT_SCALE, COORDINATE_SCALE) / LOWER_LIMIT_SCALE;
        } else if (mainNoise > 1.0D) {
            density = noise.maxLimit.sampleWrapped(noiseX, noiseY, noiseZ, COORDINATE_SCALE, HEIGHT_SCALE, COORDINATE_SCALE) / UPPER_LIMIT_SCALE;
        } else {
            double minLimitNoise = noise.minLimit.sampleWrapped(noiseX, noiseY, noiseZ, COORDINATE_SCALE, HEIGHT_SCALE, COORDINATE_SCALE) / LOWER_LIMIT_SCALE;
            double maxLimitNoise = noise.maxLimit.sampleWrapped(noiseX, noiseY, noiseZ, COORDINATE_SCALE, HEIGHT_SCALE, COORDINATE_SCALE) / UPPER_LIMIT_SCALE;
            density = Mth.lerp(mainNoise, minLimitNoise, maxLimitNoise);
        }

        density -= densityOffset;

        if (noiseY > 29.0D) {
            double topFade = (noiseY - 29.0D) / 3.0D;
            density = Mth.lerp(topFade, density, -10.0D);
        }

        return density;
    }

    private void applyBedrock(ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int localX = 0; localX < 16; ++localX) {
            int worldX = pos.getBlockX(localX);
            for (int localZ = 0; localZ < 16; ++localZ) {
                int worldZ = pos.getBlockZ(localZ);
                for (int y = MIN_Y; y < MIN_Y + 5; ++y) {
                    if (y == MIN_Y || Math.abs(hash(worldX, y, worldZ)) % 5 >= y - MIN_Y) {
                        chunk.setBlockState(mutable.set(worldX, y, worldZ), BEDROCK, false);
                    }
                }
            }
        }
    }

    private void carveLegacyCaves(ChunkAccess chunk, RandomState randomState) {
        long seed = randomState.getOrCreateRandomFactory(new ResourceLocation(CandyCraft.MODID, "legacy_major_release"))
            .fromHashOf("caves")
            .nextLong();
        Random seedRandom = new Random(seed);
        long xSeed = (seedRandom.nextLong() / 2L) * 2L + 1L;
        long zSeed = (seedRandom.nextLong() / 2L) * 2L + 1L;
        ChunkPos pos = chunk.getPos();

        for (int sourceChunkX = pos.x - CARVER_RANGE; sourceChunkX <= pos.x + CARVER_RANGE; ++sourceChunkX) {
            for (int sourceChunkZ = pos.z - CARVER_RANGE; sourceChunkZ <= pos.z + CARVER_RANGE; ++sourceChunkZ) {
                Random random = new Random((long)sourceChunkX * xSeed + (long)sourceChunkZ * zSeed ^ seed);
                carveLegacyCaveStarts(chunk, sourceChunkX, sourceChunkZ, random);
                carveLegacyRavineStart(chunk, sourceChunkX, sourceChunkZ, random);
            }
        }
    }

    private void carveLegacyCaveStarts(ChunkAccess chunk, int sourceChunkX, int sourceChunkZ, Random random) {
        int caveCount = random.nextInt(random.nextInt(random.nextInt(40) + 1) + 1);
        if (random.nextInt(15) != 0) {
            caveCount = 0;
        }

        for (int i = 0; i < caveCount; ++i) {
            double x = sourceChunkX * 16 + random.nextInt(16);
            double y = random.nextInt(random.nextInt(120) + 8);
            double z = sourceChunkZ * 16 + random.nextInt(16);
            int tunnelCount = 1;

            if (random.nextInt(4) == 0) {
                carveLegacyCaveRoom(chunk, random, x, y, z);
                tunnelCount += random.nextInt(4);
            }

            for (int tunnel = 0; tunnel < tunnelCount; ++tunnel) {
                float yaw = random.nextFloat() * Mth.PI * 2.0F;
                float pitch = (random.nextFloat() - 0.5F) * 0.25F;
                float width = random.nextFloat() * 2.0F + random.nextFloat();
                if (random.nextInt(10) == 0) {
                    width *= random.nextFloat() * random.nextFloat() * 3.0F + 1.0F;
                }

                carveLegacyTunnel(chunk, random.nextLong(), x, y, z, width, yaw, pitch, 0, 0, 1.0D);
            }
        }
    }

    private void carveLegacyCaveRoom(ChunkAccess chunk, Random random, double x, double y, double z) {
        carveLegacyTunnel(chunk, random.nextLong(), x, y, z, 1.0F + random.nextFloat() * 6.0F, 0.0F, 0.0F, -1, -1, 0.5D);
    }

    private void carveLegacyTunnel(ChunkAccess chunk, long seed, double x, double y, double z, float width,
            float yaw, float pitch, int branch, int branchCount, double yawPitchRatio) {
        ChunkPos pos = chunk.getPos();
        double centerX = pos.x * 16 + 8;
        double centerZ = pos.z * 16 + 8;
        float yawVelocity = 0.0F;
        float pitchVelocity = 0.0F;
        Random random = new Random(seed);

        if (branchCount <= 0) {
            int maxBranchCount = CARVER_RANGE * 16 - 16;
            branchCount = maxBranchCount - random.nextInt(maxBranchCount / 4);
        }

        boolean room = false;
        if (branch == -1) {
            branch = branchCount / 2;
            room = true;
        }

        int splitBranch = random.nextInt(branchCount / 2) + branchCount / 4;
        boolean slowPitch = random.nextInt(6) == 0;

        for (; branch < branchCount; ++branch) {
            double horizontalScale = 1.5D + Mth.sin((float)branch * Mth.PI / (float)branchCount) * width;
            double verticalScale = horizontalScale * yawPitchRatio;
            float pitchCos = Mth.cos(pitch);

            x += Mth.cos(yaw) * pitchCos;
            y += Mth.sin(pitch);
            z += Mth.sin(yaw) * pitchCos;
            pitch *= slowPitch ? 0.92F : 0.7F;
            pitch += pitchVelocity * 0.1F;
            yaw += yawVelocity * 0.1F;
            pitchVelocity *= 0.9F;
            yawVelocity *= 0.75F;
            pitchVelocity += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            yawVelocity += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

            if (!room && branch == splitBranch && width > 1.0F) {
                carveLegacyTunnel(chunk, random.nextLong(), x, y, z, random.nextFloat() * 0.5F + 0.5F,
                    yaw - Mth.HALF_PI, pitch / 3.0F, branch, branchCount, 1.0D);
                carveLegacyTunnel(chunk, random.nextLong(), x, y, z, random.nextFloat() * 0.5F + 0.5F,
                    yaw + Mth.HALF_PI, pitch / 3.0F, branch, branchCount, 1.0D);
                return;
            }

            if (!room && random.nextInt(4) == 0) {
                continue;
            }

            double dx = x - centerX;
            double dz = z - centerZ;
            double remaining = branchCount - branch;
            double maxDistance = width + 18.0F;
            if (dx * dx + dz * dz - remaining * remaining > maxDistance * maxDistance) {
                return;
            }

            carveLegacyRegion(chunk, x, y, z, horizontalScale, verticalScale);
            if (room) {
                break;
            }
        }
    }

    private void carveLegacyRavineStart(ChunkAccess chunk, int sourceChunkX, int sourceChunkZ, Random random) {
        if (random.nextInt(50) != 0) {
            return;
        }

        double x = sourceChunkX * 16 + random.nextInt(16);
        double y = random.nextInt(random.nextInt(40) + 8) + 20;
        double z = sourceChunkZ * 16 + random.nextInt(16);
        float yaw = random.nextFloat() * Mth.PI * 2.0F;
        float pitch = (random.nextFloat() - 0.5F) * 0.25F;
        float width = (random.nextFloat() * 2.0F + random.nextFloat()) * 2.0F;
        carveLegacyRavine(chunk, random.nextLong(), x, y, z, width, yaw, pitch, 0, 0);
    }

    private void carveLegacyRavine(ChunkAccess chunk, long seed, double x, double y, double z, float width,
            float yaw, float pitch, int branch, int branchCount) {
        ChunkPos pos = chunk.getPos();
        double centerX = pos.x * 16 + 8;
        double centerZ = pos.z * 16 + 8;
        Random random = new Random(seed);
        float yawVelocity = 0.0F;
        float pitchVelocity = 0.0F;
        float[] verticalFactors = new float[HEIGHT];
        float factor = 1.0F;

        for (int i = 0; i < verticalFactors.length; ++i) {
            if (i == 0 || random.nextInt(3) == 0) {
                factor = 1.0F + random.nextFloat() * random.nextFloat();
            }
            verticalFactors[i] = factor * factor;
        }

        if (branchCount <= 0) {
            int maxBranchCount = CARVER_RANGE * 16 - 16;
            branchCount = maxBranchCount - random.nextInt(maxBranchCount / 4);
        }

        for (; branch < branchCount; ++branch) {
            double horizontalScale = 1.5D + Mth.sin((float)branch * Mth.PI / (float)branchCount) * width;
            double verticalScale = horizontalScale * 3.0D;
            float pitchCos = Mth.cos(pitch);

            x += Mth.cos(yaw) * pitchCos;
            y += Mth.sin(pitch);
            z += Mth.sin(yaw) * pitchCos;
            pitch *= 0.7F;
            pitch += pitchVelocity * 0.05F;
            yaw += yawVelocity * 0.05F;
            pitchVelocity *= 0.8F;
            yawVelocity *= 0.5F;
            pitchVelocity += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
            yawVelocity += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

            if (random.nextInt(4) == 0) {
                continue;
            }

            double dx = x - centerX;
            double dz = z - centerZ;
            double remaining = branchCount - branch;
            double maxDistance = width + 18.0F;
            if (dx * dx + dz * dz - remaining * remaining > maxDistance * maxDistance) {
                return;
            }

            carveLegacyRavineRegion(chunk, x, y, z, horizontalScale, verticalScale, verticalFactors);
        }
    }

    private void carveLegacyRegion(ChunkAccess chunk, double x, double y, double z, double horizontalScale, double verticalScale) {
        carveEllipsoid(chunk, x, y, z, horizontalScale, verticalScale, null);
    }

    private void carveLegacyRavineRegion(ChunkAccess chunk, double x, double y, double z, double horizontalScale,
            double verticalScale, float[] verticalFactors) {
        carveEllipsoid(chunk, x, y, z, horizontalScale, verticalScale, verticalFactors);
    }

    private void carveEllipsoid(ChunkAccess chunk, double x, double y, double z, double horizontalScale,
            double verticalScale, float[] verticalFactors) {
        ChunkPos pos = chunk.getPos();
        int chunkStartX = pos.x * 16;
        int chunkStartZ = pos.z * 16;
        int minX = Mth.floor(x - horizontalScale) - chunkStartX - 1;
        int maxX = Mth.floor(x + horizontalScale) - chunkStartX + 1;
        int minY = Mth.floor(y - verticalScale) - 1;
        int maxY = Mth.floor(y + verticalScale) + 1;
        int minZ = Mth.floor(z - horizontalScale) - chunkStartZ - 1;
        int maxZ = Mth.floor(z + horizontalScale) - chunkStartZ + 1;

        minX = Mth.clamp(minX, 0, 16);
        maxX = Mth.clamp(maxX, 0, 16);
        minY = Mth.clamp(minY, MIN_Y + 1, HEIGHT - 8);
        maxY = Mth.clamp(maxY, MIN_Y + 1, HEIGHT - 8);
        minZ = Mth.clamp(minZ, 0, 16);
        maxZ = Mth.clamp(maxZ, 0, 16);

        if (isLegacyRegionBlockedByFluid(chunk, minX, maxX, minY, maxY, minZ, maxZ)) {
            return;
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int localX = minX; localX < maxX; ++localX) {
            double scaledX = ((localX + chunkStartX) + 0.5D - x) / horizontalScale;
            for (int localZ = minZ; localZ < maxZ; ++localZ) {
                double scaledZ = ((localZ + chunkStartZ) + 0.5D - z) / horizontalScale;
                if (scaledX * scaledX + scaledZ * scaledZ >= 1.0D) {
                    continue;
                }

                for (int localY = maxY; localY > minY; --localY) {
                    double scaledY = (localY - 0.5D - y) / verticalScale;
                    double shape = scaledX * scaledX + scaledZ * scaledZ;
                    if (verticalFactors != null && localY >= 0 && localY < verticalFactors.length) {
                        shape *= verticalFactors[localY];
                    }

                    double verticalShape = verticalFactors == null ? scaledY * scaledY : scaledY * scaledY / 6.0D;
                    if (shape + verticalShape >= 1.0D) {
                        continue;
                    }

                    int worldX = chunkStartX + localX;
                    int worldZ = chunkStartZ + localZ;
                    BlockState state = chunk.getBlockState(mutable.set(worldX, localY, worldZ));
                    if (isBaseStone(state)) {
                        chunk.setBlockState(mutable, localY <= LAVA_LEVEL ? liquidCandy() : AIR, false);
                    }
                }
            }
        }
    }

    private boolean isLegacyRegionBlockedByFluid(ChunkAccess chunk, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        ChunkPos pos = chunk.getPos();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int localX = minX; localX < maxX; ++localX) {
            int worldX = pos.getBlockX(localX);
            for (int localZ = minZ; localZ < maxZ; ++localZ) {
                int worldZ = pos.getBlockZ(localZ);
                for (int y = maxY + 1; y >= minY - 1; --y) {
                    if (y < MIN_Y || y >= HEIGHT) {
                        continue;
                    }
                    BlockState state = chunk.getBlockState(mutable.set(worldX, y, worldZ));
                    if (state.is(Blocks.WATER)
                        || state.is(CCSweetscapeBlocks.LIQUID_CHOCOLATE.get())
                        || state.is(CCSweetscapeBlocks.LIQUID_CANDY.get()) && y > LAVA_LEVEL) {
                        return true;
                    }
                    if (y != minY - 1 && localX != minX && localX != maxX - 1 && localZ != minZ && localZ != maxZ - 1) {
                        y = minY;
                    }
                }
            }
        }
        return false;
    }

    private int findTopSolid(ChunkAccess chunk, int worldX, int worldZ) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = Math.min(chunk.getMaxBuildHeight() - 1, HEIGHT - 1); y >= MIN_Y; --y) {
            BlockState state = chunk.getBlockState(mutable.set(worldX, y, worldZ));
            if (isBaseStone(state)) {
                return y;
            }
        }
        return MIN_Y;
    }

    private int findTopTerrain(ChunkAccess chunk, int worldX, int worldZ) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int y = Math.min(chunk.getMaxBuildHeight() - 1, HEIGHT - 1); y >= MIN_Y; --y) {
            BlockState state = chunk.getBlockState(mutable.set(worldX, y, worldZ));
            if (!state.isAir() && state.getFluidState().isEmpty()) {
                return y;
            }
        }
        return MIN_Y;
    }

    private static BlockState baseStone() {
        return CCSweetscapeBlocks.CRYSTALLIZED_SUGAR.get().defaultBlockState();
    }

    private static BlockState liquidCandy() {
        return CCSweetscapeBlocks.LIQUID_CANDY.get().defaultBlockState();
    }

    private static boolean isBaseStone(BlockState state) {
        return state.is(CCSweetscapeBlocks.CRYSTALLIZED_SUGAR.get());
    }

    private static SurfaceMaterials surfaceMaterials(ResourceLocation biomeId, int worldX, int worldZ, RandomState randomState) {
        if (biomeId.equals(COTTON_CANDY_PLAINS)) {
            BlockState under = CCSweetscapeBlocks.MILK_BROWNIE_BLOCK.get().defaultBlockState();
            return new SurfaceMaterials(CCSweetscapeBlocks.CANDY_GRASS_BLOCK.get().defaultBlockState(), under, under);
        }
        if (biomeId.equals(CHOCOLATE_FOREST)) {
            BlockState under = CCSweetscapeBlocks.WHITE_BROWNIE_BLOCK.get().defaultBlockState();
            return new SurfaceMaterials(CCSweetscapeBlocks.CHOCOLATE_COVERED_WHITE_BROWNIE.get().defaultBlockState(), under, under);
        }
        if (biomeId.equals(GUMMY_SWAMP)) {
            return gummySurfaceMaterials(worldX, worldZ, randomState);
        }
        BlockState under = CCBlocks.FLOUR.get().defaultBlockState();
        return new SurfaceMaterials(CCBlocks.PUDDING.get().defaultBlockState(), under, under);
    }

    private static SurfaceMaterials gummySurfaceMaterials(int worldX, int worldZ, RandomState randomState) {
        double noise = octaveNoise2D(worldX * 0.0075D, worldZ * 0.0075D, 4, worldSeed(randomState) ^ 0x6A6D6D7953555246L) * 12.0D;
        int index = (int)(noise * 1.6D) % 10;
        if (index < 0) {
            index += 10;
        }
        return switch (index) {
            case 1, 8 -> new SurfaceMaterials(
                CCSweetscapeBlocks.ORANGE_GUMMY_BLOCK.get().defaultBlockState(),
                CCSweetscapeBlocks.ORANGE_HARDENED_GUMMY_BLOCK.get().defaultBlockState(),
                CCSweetscapeBlocks.ORANGE_GUMMY_BLOCK.get().defaultBlockState()
            );
            case 2, 5, 7 -> new SurfaceMaterials(
                CCSweetscapeBlocks.YELLOW_GUMMY_BLOCK.get().defaultBlockState(),
                CCSweetscapeBlocks.YELLOW_HARDENED_GUMMY_BLOCK.get().defaultBlockState(),
                CCSweetscapeBlocks.YELLOW_GUMMY_BLOCK.get().defaultBlockState()
            );
            case 3, 4 -> new SurfaceMaterials(
                CCSweetscapeBlocks.GREEN_GUMMY_BLOCK.get().defaultBlockState(),
                CCSweetscapeBlocks.GREEN_HARDENED_GUMMY_BLOCK.get().defaultBlockState(),
                CCSweetscapeBlocks.GREEN_GUMMY_BLOCK.get().defaultBlockState()
            );
            case 6 -> new SurfaceMaterials(
                CCSweetscapeBlocks.WHITE_GUMMY_BLOCK.get().defaultBlockState(),
                CCSweetscapeBlocks.WHITE_HARDENED_GUMMY_BLOCK.get().defaultBlockState(),
                CCSweetscapeBlocks.WHITE_GUMMY_BLOCK.get().defaultBlockState()
            );
            default -> new SurfaceMaterials(
                CCSweetscapeBlocks.RED_GUMMY_BLOCK.get().defaultBlockState(),
                CCSweetscapeBlocks.RED_HARDENED_GUMMY_BLOCK.get().defaultBlockState(),
                CCSweetscapeBlocks.RED_GUMMY_BLOCK.get().defaultBlockState()
            );
        };
    }

    private static BlockState fluidForBiome(ResourceLocation biomeId) {
        if (biomeId.equals(CHOCOLATE_FOREST)) {
            return CCSweetscapeBlocks.LIQUID_CHOCOLATE.get().defaultBlockState();
        }
        return WATER;
    }

    private static BlockState underwaterMaterial(int replaced) {
        return replaced == 0
            ? CCSweetscapeBlocks.SUGAR_SAND.get().defaultBlockState()
            : CCBlocks.FLOUR.get().defaultBlockState();
    }

    private BiomeShape biomeShape(int x, int z, RandomState randomState) {
        ResourceLocation location = biomeId(x, z, randomState);
        return switch (location.getPath()) {
            case "sugar_mountains" -> new BiomeShape(0.5F, 0.8F);
            case "sugar_hell_mountains" -> new BiomeShape(1.9F, 2.0F);
            case "sugar_plains" -> new BiomeShape(0.05F, 0.1F);
            case "sugar_forest" -> new BiomeShape(0.1F, 0.15F);
            case "chocolate_forest" -> new BiomeShape(0.1F, 0.15F);
            case "sugar_cold_forest" -> new BiomeShape(0.1F, 0.3F);
            case "sugar_river" -> new BiomeShape(-0.5F, 0.0F);
            case "sugar_oceans" -> new BiomeShape(-1.0F, 0.1F);
            case "sugar_enchanted_forest" -> new BiomeShape(0.23F, 0.25F);
            case "caramel_forest" -> new BiomeShape(0.05F, 0.1F);
            case "cotton_candy_plains" -> new BiomeShape(0.05F, 0.1F);
            case "gummy_swamp" -> new BiomeShape(-0.1F, 0.1F);
            case "ice_cream_plains" -> new BiomeShape(0.05F, 0.1F);
            default -> new BiomeShape(0.05F, 0.1F);
        };
    }

    private ResourceLocation biomeId(int x, int z, RandomState randomState) {
        Holder<Biome> biome = getBiomeSource().getNoiseBiome(Mth.floorDiv(x, 4), 16, Mth.floorDiv(z, 4), randomState.sampler());
        Optional<ResourceLocation> id = biome.unwrapKey().map(key -> key.location());
        return id.orElse(PLAINS);
    }

    private static float[] makeParabolicField() {
        float[] field = new float[25];
        for (int x = -2; x <= 2; ++x) {
            for (int z = -2; z <= 2; ++z) {
                field[x + 2 + (z + 2) * 5] = 10.0F / Mth.sqrt(x * x + z * z + 0.2F);
            }
        }
        return field;
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

    private static double octaveNoise3D(double x, double y, double z, int octaves, long salt) {
        double value = 0.0D;
        double amplitude = 1.0D;
        double frequency = 1.0D;
        double total = 0.0D;

        for (int i = 0; i < octaves; ++i) {
            value += smoothNoise3D(x * frequency, y * frequency, z * frequency, salt + i * 0x632BE59BD9B4E019L) * amplitude;
            total += amplitude;
            amplitude *= 0.5D;
            frequency *= 2.0D;
        }

        return value / total;
    }

    private static double smoothNoise2D(double x, double z, long salt) {
        int x0 = Mth.floor(x);
        int z0 = Mth.floor(z);
        double tx = fade(x - x0);
        double tz = fade(z - z0);
        double a = randomUnit(x0, 0, z0, salt);
        double b = randomUnit(x0 + 1, 0, z0, salt);
        double c = randomUnit(x0, 0, z0 + 1, salt);
        double d = randomUnit(x0 + 1, 0, z0 + 1, salt);
        return Mth.lerp(tz, Mth.lerp(tx, a, b), Mth.lerp(tx, c, d));
    }

    private static double smoothNoise3D(double x, double y, double z, long salt) {
        int x0 = Mth.floor(x);
        int y0 = Mth.floor(y);
        int z0 = Mth.floor(z);
        double tx = fade(x - x0);
        double ty = fade(y - y0);
        double tz = fade(z - z0);
        double a = Mth.lerp(tx, randomUnit(x0, y0, z0, salt), randomUnit(x0 + 1, y0, z0, salt));
        double b = Mth.lerp(tx, randomUnit(x0, y0, z0 + 1, salt), randomUnit(x0 + 1, y0, z0 + 1, salt));
        double c = Mth.lerp(tx, randomUnit(x0, y0 + 1, z0, salt), randomUnit(x0 + 1, y0 + 1, z0, salt));
        double d = Mth.lerp(tx, randomUnit(x0, y0 + 1, z0 + 1, salt), randomUnit(x0 + 1, y0 + 1, z0 + 1, salt));
        return Mth.lerp(tz, Mth.lerp(ty, a, c), Mth.lerp(ty, b, d));
    }

    private static double fade(double value) {
        return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
    }

    private static double randomUnit(int x, int y, int z, long salt) {
        long bits = hash(x, y, z, salt);
        return ((bits >>> 11) * 0x1.0p-53D) * 2.0D - 1.0D;
    }

    private static int hash(int x, int y, int z) {
        return (int)hash(x, y, z, 0xCBF29CE484222325L);
    }

    private static long positiveHash(int x, int y, int z, long salt) {
        return hash(x, y, z, salt) & Long.MAX_VALUE;
    }

    private static long hash(int x, int y, int z, long salt) {
        long h = salt;
        h ^= x * 0x9E3779B97F4A7C15L;
        h = Long.rotateLeft(h, 27) * 0x94D049BB133111EBL;
        h ^= y * 0xC2B2AE3D27D4EB4FL;
        h = Long.rotateLeft(h, 31) * 0x2545F4914F6CDD1DL;
        h ^= z * 0x165667B19E3779F9L;
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return h;
    }

    private record BiomeShape(float baseHeight, float variation) {
    }

    private record HeightConfig(double depth, double scale) {
    }

    private record SurfaceMaterials(BlockState top, BlockState under, BlockState underwater) {
    }

    private static final class LegacyTerrainNoise {
        private final LegacyPerlinOctaveNoise minLimit;
        private final LegacyPerlinOctaveNoise maxLimit;
        private final LegacyPerlinOctaveNoise main;
        private final LegacyPerlinOctaveNoise depth;

        private LegacyTerrainNoise(long seed) {
            Random random = new Random(seed);
            this.minLimit = new LegacyPerlinOctaveNoise(random, 16, true);
            this.maxLimit = new LegacyPerlinOctaveNoise(random, 16, true);
            this.main = new LegacyPerlinOctaveNoise(random, 8, true);
            new LegacyPerlinOctaveNoise(random, 4, false);
            new LegacyPerlinOctaveNoise(random, 10, true);
            this.depth = new LegacyPerlinOctaveNoise(random, 16, true);
            new LegacyPerlinOctaveNoise(random, 8, true);
        }
    }
}
