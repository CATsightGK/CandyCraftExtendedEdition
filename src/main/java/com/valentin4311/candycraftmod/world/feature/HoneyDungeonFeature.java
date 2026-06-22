package com.valentin4311.candycraftmod.world.feature;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class HoneyDungeonFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation LOOT_TABLE = new ResourceLocation(CandyCraft.MODID, "chests/honey_dungeon");

    public HoneyDungeonFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int height = 3;
        int xRadius = random.nextInt(2) + 2;
        int zRadius = random.nextInt(2) + 2;
        int openings = 0;

        for (int x = origin.getX() - xRadius - 1; x <= origin.getX() + xRadius + 1; ++x) {
            for (int y = origin.getY() - 1; y <= origin.getY() + height + 1; ++y) {
                for (int z = origin.getZ() - zRadius - 1; z <= origin.getZ() + zRadius + 1; ++z) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (y == origin.getY() - 1 && !isSolid(level, pos)) {
                        return false;
                    }
                    if (y == origin.getY() + height + 1 && !isSolid(level, pos)) {
                        return false;
                    }
                    boolean outerX = x == origin.getX() - xRadius - 1 || x == origin.getX() + xRadius + 1;
                    boolean outerZ = z == origin.getZ() - zRadius - 1 || z == origin.getZ() + zRadius + 1;
                    if ((outerX || outerZ) && y == origin.getY() && level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())) {
                        ++openings;
                    }
                }
            }
        }

        if (openings < 1 || openings > 5) {
            return false;
        }

        BlockState honeyWall = CCBlocks.HONEYCOMB_BLOCK.get().defaultBlockState();
        for (int x = origin.getX() - xRadius - 1; x <= origin.getX() + xRadius + 1; ++x) {
            for (int y = origin.getY() + height; y >= origin.getY() - 1; --y) {
                for (int z = origin.getZ() - zRadius - 1; z <= origin.getZ() + zRadius + 1; ++z) {
                    BlockPos pos = new BlockPos(x, y, z);
                    boolean inside = x != origin.getX() - xRadius - 1
                        && x != origin.getX() + xRadius + 1
                        && y != origin.getY() - 1
                        && y != origin.getY() + height + 1
                        && z != origin.getZ() - zRadius - 1
                        && z != origin.getZ() + zRadius + 1;

                    if (inside) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    } else if (y >= level.getMinBuildHeight() && !isSolid(level, pos.below())) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    } else if (isSolid(level, pos)) {
                        level.setBlock(pos, honeyWall, 2);
                    }
                }
            }
        }

        placeChests(level, random, origin, xRadius, zRadius);
        level.setBlock(origin, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(origin) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(CCEntityTypes.CARAMEL_BEE.get(), random);
        }
        return true;
    }

    private static void placeChests(WorldGenLevel level, RandomSource random, BlockPos origin, int xRadius, int zRadius) {
        int placed = 0;
        for (int attempts = 0; attempts < 6 && placed < 2; ++attempts) {
            BlockPos pos = origin.offset(random.nextInt(xRadius * 2 + 1) - xRadius, 0, random.nextInt(zRadius * 2 + 1) - zRadius);
            if (!level.isEmptyBlock(pos) || countSolidSides(level, pos) != 1) {
                continue;
            }

            level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 2);
            if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
                chest.setLootTable(LOOT_TABLE, random.nextLong());
            }
            ++placed;
        }
    }

    private static int countSolidSides(WorldGenLevel level, BlockPos pos) {
        int solid = 0;
        if (isSolid(level, pos.west())) {
            ++solid;
        }
        if (isSolid(level, pos.east())) {
            ++solid;
        }
        if (isSolid(level, pos.north())) {
            ++solid;
        }
        if (isSolid(level, pos.south())) {
            ++solid;
        }
        return solid;
    }

    private static boolean isSolid(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && !state.getCollisionShape(level, pos).isEmpty();
    }
}
