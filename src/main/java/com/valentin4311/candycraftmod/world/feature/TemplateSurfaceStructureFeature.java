package com.valentin4311.candycraftmod.world.feature;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.OptionalInt;

public final class TemplateSurfaceStructureFeature extends Feature<NoneFeatureConfiguration> {
    private final ResourceLocation templateId;
    private final boolean requireFlatSite;
    private final boolean addFlourSupports;
    private final int maxFlatSiteHeightVariation;

    public TemplateSurfaceStructureFeature(Codec<NoneFeatureConfiguration> codec, ResourceLocation templateId) {
        this(codec, templateId, false, false, 4);
    }

    public TemplateSurfaceStructureFeature(Codec<NoneFeatureConfiguration> codec, ResourceLocation templateId,
            boolean requireFlatSite, boolean addFlourSupports) {
        this(codec, templateId, requireFlatSite, addFlourSupports, 4);
    }

    public TemplateSurfaceStructureFeature(Codec<NoneFeatureConfiguration> codec, ResourceLocation templateId,
            boolean requireFlatSite, boolean addFlourSupports, int maxFlatSiteHeightVariation) {
        super(codec);
        this.templateId = templateId;
        this.requireFlatSite = requireFlatSite;
        this.addFlourSupports = addFlourSupports;
        this.maxFlatSiteHeightVariation = Math.max(0, maxFlatSiteHeightVariation);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        StructureTemplate template = level.getLevel().getStructureManager().get(templateId).orElse(null);
        if (template == null || template.getSize().getX() == 0 || template.getSize().getY() == 0
                || template.getSize().getZ() == 0) {
            return false;
        }

        int placementX = origin.getX() - template.getSize().getX() / 2;
        int placementZ = origin.getZ() - template.getSize().getZ() / 2;
        int placementY;
        if (requireFlatSite) {
            OptionalInt flatSiteY = findFlatSiteY(level, placementX, placementZ,
                template.getSize().getX(), template.getSize().getZ(), maxFlatSiteHeightVariation);
            if (flatSiteY.isEmpty()) {
                return false;
            }
            placementY = flatSiteY.getAsInt();
        } else {
            placementY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                origin.getX(), origin.getZ());
            if (!hasDryCandyFootprint(level, placementX, placementZ,
                    template.getSize().getX(), template.getSize().getZ())) {
                return false;
            }
        }
        BlockPos placement = new BlockPos(placementX, placementY, placementZ);

        StructurePlaceSettings settings = new StructurePlaceSettings()
            .setMirror(Mirror.NONE)
            .setRotation(Rotation.NONE)
            .setIgnoreEntities(false)
            .setKeepLiquids(false)
            .setFinalizeEntities(true)
            .setRandom(random);
        boolean placed = template.placeInWorld(level, placement, placement, settings, random, 2 | 16);
        if (placed && addFlourSupports) {
            addFlourSupports(level, placement, template.getSize().getX(), template.getSize().getZ());
        }
        return placed;
    }

    private static OptionalInt findFlatSiteY(WorldGenLevel level, int placementX, int placementZ,
            int width, int depth, int maxHeightVariation) {
        int minHeight = Integer.MAX_VALUE;
        int maxHeight = Integer.MIN_VALUE;
        for (int x = placementX; x < placementX + width; x++) {
            for (int z = placementZ; z < placementZ + depth; z++) {
                int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                BlockPos surface = new BlockPos(x, height - 1, z);
                if (height <= level.getMinBuildHeight()
                        || !isCandyGround(level.getBlockState(surface))
                        || !level.getFluidState(surface).isEmpty()
                        || !level.getFluidState(surface.above()).isEmpty()) {
                    return OptionalInt.empty();
                }
                minHeight = Math.min(minHeight, height);
                maxHeight = Math.max(maxHeight, height);
                if (maxHeight - minHeight > maxHeightVariation) {
                    return OptionalInt.empty();
                }
            }
        }
        return OptionalInt.of(maxHeight);
    }

    private static boolean hasDryCandyFootprint(WorldGenLevel level, int placementX, int placementZ,
            int width, int depth) {
        for (int x = placementX; x < placementX + width; x++) {
            for (int z = placementZ; z < placementZ + depth; z++) {
                int height = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                if (height <= level.getMinBuildHeight()) {
                    return false;
                }

                BlockPos surface = new BlockPos(x, height - 1, z);
                if (!isCandyGround(level.getBlockState(surface))
                        || !level.getFluidState(surface).isEmpty()
                        || !level.getFluidState(surface.above()).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void addFlourSupports(WorldGenLevel level, BlockPos placement, int width, int depth) {
        BlockState flour = CCBlocks.FLOUR.get().defaultBlockState();
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < depth; dz++) {
                BlockPos foundation = placement.offset(dx, 0, dz);
                if (level.getBlockState(foundation).isAir()) {
                    continue;
                }
                BlockPos.MutableBlockPos support = foundation.below().mutable();
                while (support.getY() >= level.getMinBuildHeight() && level.getBlockState(support).isAir()) {
                    level.setBlock(support, flour, 2 | 16);
                    support.move(0, -1, 0);
                }
            }
        }
    }

    private static boolean isCandyGround(BlockState state) {
        return state.is(CCBlocks.PUDDING.get())
            || state.is(CCBlocks.FLOUR.get())
            || state.is(CCBlocks.CANDY_FARMLAND.get());
    }
}
