package com.valentin4311.candycraftmod.world.structure;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class CandyFeatureLocator {
    private CandyFeatureLocator() {
    }

    public static Pair<BlockPos, Holder<Structure>> find(ChunkGenerator generator, ServerLevel level,
            HolderSet<Structure> structures, BlockPos center, int radius) {
        List<Target> targets = locatorTargets(level, generator, structures);
        if (targets.isEmpty()) {
            return null;
        }

        ChunkPos centerChunk = new ChunkPos(center);
        for (int ring = 0; ring <= radius; ring++) {
            Pair<BlockPos, Holder<Structure>> nearest = null;
            long nearestDistance = Long.MAX_VALUE;
            int minX = centerChunk.x - ring;
            int maxX = centerChunk.x + ring;
            int minZ = centerChunk.z - ring;
            int maxZ = centerChunk.z + ring;
            for (int chunkX = minX; chunkX <= maxX; chunkX++) {
                for (int chunkZ = minZ; chunkZ <= maxZ; chunkZ++) {
                    if (ring > 0 && chunkX != minX && chunkX != maxX && chunkZ != minZ && chunkZ != maxZ) {
                        continue;
                    }
                    for (Target target : targets) {
                        for (BlockPos candidate : positions(level, generator, target, chunkX, chunkZ)) {
                            long dx = candidate.getX() - (long) center.getX();
                            long dz = candidate.getZ() - (long) center.getZ();
                            long distance = dx * dx + dz * dz;
                            if (distance < nearestDistance) {
                                nearestDistance = distance;
                                nearest = Pair.of(candidate, target.structure());
                            }
                        }
                    }
                }
            }
            if (nearest != null) {
                return nearest;
            }
        }
        return null;
    }

    private static List<Target> locatorTargets(ServerLevel level, ChunkGenerator generator,
            HolderSet<Structure> structures) {
        Registry<PlacedFeature> placedFeatures = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        List<FeatureSorter.StepFeatureData> steps = FeatureSorter.buildFeaturesPerStep(
            new ArrayList<>(generator.getBiomeSource().possibleBiomes()),
            biome -> biome.value().getGenerationSettings().features(), true);
        List<Target> targets = new ArrayList<>();
        for (Holder<Structure> structure : structures) {
            if (!(structure.value() instanceof FeatureLocatorStructure locator)) {
                continue;
            }
            PlacedFeature placedFeature = placedFeatures.get(locator.feature());
            if (placedFeature == null) {
                continue;
            }
            for (int step = 0; step < steps.size(); step++) {
                int featureIndex = steps.get(step).features().indexOf(placedFeature);
                if (featureIndex >= 0) {
                    targets.add(new Target(structure, placedFeature, step, featureIndex));
                    break;
                }
            }
        }
        return targets;
    }

    private static List<BlockPos> positions(ServerLevel level, ChunkGenerator generator, Target target,
            int chunkX, int chunkZ) {
        BlockPos origin = new ChunkPos(chunkX, chunkZ).getWorldPosition();
        WorldgenRandom random = new WorldgenRandom(new XoroshiroRandomSource(RandomSupport.generateUniqueSeed()));
        long decorationSeed = random.setDecorationSeed(level.getSeed(), origin.getX(), origin.getZ());
        random.setFeatureSeed(decorationSeed, target.featureIndex(), target.step());
        PlacementContext context = new PlacementContext(level, generator, Optional.of(target.feature()));
        Stream<BlockPos> positions = Stream.of(origin);
        for (PlacementModifier modifier : target.feature().placement()) {
            positions = positions.flatMap(pos -> modifier.getPositions(context, random, pos));
        }
        return positions.toList();
    }

    private record Target(Holder<Structure> structure, PlacedFeature feature, int step, int featureIndex) {
    }
}
