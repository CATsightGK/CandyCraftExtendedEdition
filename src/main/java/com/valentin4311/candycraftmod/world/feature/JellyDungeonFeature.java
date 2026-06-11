package com.valentin4311.candycraftmod.world.feature;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class JellyDungeonFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation LOOT_TABLE = new ResourceLocation(CandyCraft.MODID, "chests/jelly_dungeon");
    private int cursorZ;

    public JellyDungeonFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public static void generateInDungeonLevel(ServerLevel level, BlockPos origin) {
        RandomSource random = level.getRandom();
        BlockState wall = CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState();
        for (int x = -32; x <= 32; x++) {
            for (int z = -32; z <= 32; z++) {
                level.setBlock(origin.offset(x, -1, z), wall, 2);
                for (int y = 0; y <= 18; y++) {
                    level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        new JellyDungeonFeature(NoneFeatureConfiguration.CODEC).compactDungeon(level, random, origin);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        if (origin.getY() < level.getMinBuildHeight() + 12 || origin.getY() > level.getMaxBuildHeight() - 48) {
            return false;
        }
        if (!canFit(level, origin)) {
            return false;
        }

        compactDungeon(level, random, origin);
        return true;
    }

    private static boolean canFit(WorldGenLevel level, BlockPos origin) {
        int minY = origin.getY() - 8;
        int maxY = origin.getY() + 36;
        if (minY <= level.getMinBuildHeight() || maxY >= level.getMaxBuildHeight()) {
            return false;
        }
        for (int x = -18; x <= 18; x += 18) {
            for (int z = -18; z <= 18; z += 18) {
                BlockPos pos = origin.offset(x, 0, z);
                if (level.getBlockState(pos).isAir() || level.getBlockState(pos.below()).isAir()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void compactDungeon(WorldGenLevel level, RandomSource random, BlockPos origin) {
        int x = origin.getX();
        int y = origin.getY();
        int z = origin.getZ();
        boxRoom(level, random, x, y, z, 6, 6, 6);
        boxRoom(level, random, x, y, z - 13, 5, 8, 5);
        boxRoom(level, random, x, y - 2, z + 13, 6, 6, 5);
        boxRoom(level, random, x - 13, y, z, 5, 7, 5);
        boxRoom(level, random, x + 13, y, z, 5, 7, 5);
        boxRoom(level, random, x - 13, y + 4, z - 13, 5, 6, 5);
        boxRoom(level, random, x + 13, y + 4, z - 13, 5, 6, 5);
        boxRoom(level, random, x - 13, y - 3, z + 13, 5, 6, 5);
        boxRoom(level, random, x + 13, y - 3, z + 13, 5, 6, 5);
        connector(level, random, x, y, z, 0, -1, 7);
        connector(level, random, x, y, z, 0, 1, 7);
        connector(level, random, x, y, z, -1, 0, 7);
        connector(level, random, x, y, z, 1, 0, 7);
        diagonalConnector(level, random, x, y + 2, z, -1, -1);
        diagonalConnector(level, random, x, y + 2, z, 1, -1);
        diagonalConnector(level, random, x, y - 2, z, -1, 1);
        diagonalConnector(level, random, x, y - 2, z, 1, 1);
        lockedDoor(level, random, new BlockPos(x - 7, y + 2, z), false, CCBlocks.JELLY_SENTRY_KEY_HOLE.get().defaultBlockState());
        lockedDoor(level, random, new BlockPos(x + 7, y + 2, z), false, CCBlocks.JELLY_BOSS_KEY_HOLE.get().defaultBlockState());
        lockedDoor(level, random, new BlockPos(x, y + 2, z - 7), true, CCBlocks.JELLY_SENTRY_KEY_HOLE.get().defaultBlockState());

        placeJellyPads(level, x, y + 1, z - 2, 4, CCBlocks.TRAMPOJELLY.get().defaultBlockState());
        set(level, new BlockPos(x - 3, y + 1, z - 3), CCBlocks.CARAMEL_BLOCK.get().defaultBlockState());
        set(level, new BlockPos(x - 3, y + 2, z - 3), CCBlocks.BLOCK_TELEPORTER.get().defaultBlockState());
        lootChest(level, random, new BlockPos(x + 3, y + 1, z - 3));

        for (int i = 0; i < 5; i++) {
            set(level, new BlockPos(x - 2 + i, y + 2 + i, z - 10 - i), randomJelly(random));
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) < 3 && Math.abs(dz) < 3) {
                    set(level, new BlockPos(x + dx, y + 1, z + 13 + dz), CCBlocks.GRENADINE.get().defaultBlockState());
                }
            }
        }
        setSpawner(level, new BlockPos(x - 13, y + 1, z));
        set(level, new BlockPos(x - 13, y + 1, z + 3), CCBlocks.JELLY_SENTRY_KEY_HOLE.get().defaultBlockState());
        setSpawner(level, new BlockPos(x + 13, y + 1, z));
        set(level, new BlockPos(x + 13, y + 1, z - 3), CCBlocks.JELLY_BOSS_KEY_HOLE.get().defaultBlockState());
        setSpawner(level, new BlockPos(x - 13, y + 5, z - 13));
        setSpawner(level, new BlockPos(x + 13, y + 5, z - 13));
        placeJellyPads(level, x - 13, y + 5, z - 13, 4, CCBlocks.PURPLE_TRAMPOJELLY.get().defaultBlockState());
        placeJellyPads(level, x + 13, y + 5, z - 13, 4, CCBlocks.RED_TRAMPOJELLY.get().defaultBlockState());
        set(level, new BlockPos(x - 13, y - 2, z + 13), CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState());
        set(level, new BlockPos(x + 13, y - 2, z + 13), CCBlocks.JELLY_BOSS_KEY_HOLE.get().defaultBlockState());

        BlockPos chestPos = new BlockPos(x + 13, y + 1, z + 3);
        lootChest(level, random, chestPos);
        BlockPos crownChestPos = new BlockPos(x + 13, y - 2, z + 15);
        lootChest(level, random, crownChestPos);
    }

    private void lootChest(WorldGenLevel level, RandomSource random, BlockPos pos) {
        set(level, pos, Blocks.CHEST.defaultBlockState());
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(LOOT_TABLE, random.nextLong());
        }
    }

    private void lockedDoor(WorldGenLevel level, RandomSource random, BlockPos center, boolean alongX, BlockState lock) {
        for (int horizontal = -2; horizontal <= 2; horizontal++) {
            for (int dy = -1; dy <= 2; dy++) {
                BlockPos pos = alongX ? center.offset(horizontal, dy, 0) : center.offset(0, dy, horizontal);
                set(level, pos, jawBreaker(random));
            }
        }
        set(level, center, lock);
        set(level, center.above(), lock);
    }

    private void boxRoom(WorldGenLevel level, RandomSource random, int centerX, int y, int centerZ, int radiusX, int height, int radiusZ) {
        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dy = 0; dy <= height; dy++) {
                for (int dz = -radiusZ; dz <= radiusZ; dz++) {
                    boolean wall = dx == -radiusX || dx == radiusX || dy == 0 || dy == height || dz == -radiusZ || dz == radiusZ;
                    set(level, new BlockPos(centerX + dx, y + dy, centerZ + dz), wall ? jawBreaker(random) : Blocks.AIR.defaultBlockState());
                }
            }
        }
        set(level, new BlockPos(centerX - radiusX, y + 3, centerZ), CCBlocks.JAW_BREAKER_LIGHT.get().defaultBlockState());
        set(level, new BlockPos(centerX + radiusX, y + 3, centerZ), CCBlocks.JAW_BREAKER_LIGHT.get().defaultBlockState());
        set(level, new BlockPos(centerX, y + 3, centerZ - radiusZ), CCBlocks.JAW_BREAKER_LIGHT.get().defaultBlockState());
        set(level, new BlockPos(centerX, y + 3, centerZ + radiusZ), CCBlocks.JAW_BREAKER_LIGHT.get().defaultBlockState());
    }

    private void connector(WorldGenLevel level, RandomSource random, int centerX, int y, int centerZ, int stepX, int stepZ, int length) {
        for (int i = 0; i <= length; i++) {
            int cx = centerX + stepX * i;
            int cz = centerZ + stepZ * i;
            for (int ox = -1; ox <= 1; ox++) {
                for (int oz = -1; oz <= 1; oz++) {
                    for (int dy = 1; dy <= 3; dy++) {
                        set(level, new BlockPos(cx + ox, y + dy, cz + oz), Blocks.AIR.defaultBlockState());
                    }
                }
            }
            if (i > 0 && i < length && i % 3 == 0) {
                set(level, new BlockPos(cx, y + 1, cz), randomJelly(random));
            }
        }
    }

    private void diagonalConnector(WorldGenLevel level, RandomSource random, int centerX, int y, int centerZ, int stepX, int stepZ) {
        for (int i = 2; i <= 12; i++) {
            int cx = centerX + stepX * i;
            int cz = centerZ + stepZ * i;
            for (int ox = -1; ox <= 1; ox++) {
                for (int oz = -1; oz <= 1; oz++) {
                    for (int dy = 1; dy <= 3; dy++) {
                        set(level, new BlockPos(cx + ox, y + dy, cz + oz), Blocks.AIR.defaultBlockState());
                    }
                }
            }
            if (i % 4 == 0) {
                set(level, new BlockPos(cx, y + 1, cz), randomJelly(random));
            }
        }
    }

    private void placeModule(WorldGenLevel level, RandomSource random, Module module, int x, int y) {
        switch (module) {
            case JUMP -> jumpRoom(level, random, x, y - 3);
            case WATER -> waterRoom(level, random, x, y);
            case MOB -> mobRoom(level, random, x, y);
        }
    }

    private void spawnRoom(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        room(level, random, x, y, z, 6, 7, 11);
        placeJellyPads(level, x, y + 1, z - 4, 5, CCBlocks.TRAMPOJELLY.get().defaultBlockState());
        set(level, new BlockPos(x - 4, y + 1, z - 2), CCBlocks.CARAMEL_BLOCK.get().defaultBlockState());
        set(level, new BlockPos(x - 4, y + 2, z - 2), CCBlocks.BLOCK_TELEPORTER.get().defaultBlockState());
        cursorZ -= 11;
    }

    private void corridor(WorldGenLevel level, RandomSource random, int x, int y) {
        room(level, random, x, y, cursorZ, 3, 5, 10);
        for (int z = cursorZ - 1; z >= cursorZ - 9; z -= 2) {
            set(level, new BlockPos(x - 3, y + 2, z), randomJelly(random));
            set(level, new BlockPos(x + 3, y + 2, z), randomJelly(random));
        }
        cursorZ -= 10;
    }

    private void jumpRoom(WorldGenLevel level, RandomSource random, int x, int y) {
        int z = cursorZ;
        room(level, random, x, y, z, 5, 34, 42);
        for (int i = 0; i < 9; i++) {
            int stepZ = z - 4 - i * 4;
            int stepY = y + 2 + i * 3;
            set(level, new BlockPos(x + random.nextInt(5) - 2, stepY, stepZ), randomJelly(random));
        }
        set(level, new BlockPos(x, y + 31, z - 38), CCBlocks.PURPLE_TRAMPOJELLY.get().defaultBlockState());
        cursorZ -= 42;
    }

    private void waterRoom(WorldGenLevel level, RandomSource random, int x, int y) {
        int z = cursorZ;
        room(level, random, x, y, z, 7, 8, 18);
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -14; dz <= -4; dz++) {
                if (Math.abs(dx) == 5 || dz == -14 || dz == -4) {
                    continue;
                }
                set(level, new BlockPos(x + dx, y + 1, z + dz), CCBlocks.GRENADINE.get().defaultBlockState());
                set(level, new BlockPos(x + dx, y + 2, z + dz), CCBlocks.GRENADINE.get().defaultBlockState());
            }
        }
        set(level, new BlockPos(x, y + 3, z - 9), CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState());
        cursorZ -= 18;
    }

    private void mobRoom(WorldGenLevel level, RandomSource random, int x, int y) {
        int z = cursorZ;
        room(level, random, x, y, z, 8, 8, 18);
        setSpawner(level, new BlockPos(x - 3, y + 1, z - 7));
        setSpawner(level, new BlockPos(x + 3, y + 1, z - 10));
        set(level, new BlockPos(x, y + 1, z - 15), CCBlocks.JELLY_SENTRY_KEY_HOLE.get().defaultBlockState());
        placeJellyPads(level, x, y + 1, z - 8, 6, CCBlocks.RED_TRAMPOJELLY.get().defaultBlockState());
        cursorZ -= 18;
    }

    private void miniBossRoom(WorldGenLevel level, RandomSource random, int x, int y) {
        int z = cursorZ;
        room(level, random, x, y, z, 9, 9, 22);
        setSpawner(level, new BlockPos(x, y + 1, z - 11));
        set(level, new BlockPos(x - 6, y + 1, z - 11), CCBlocks.JELLY_SENTRY_KEY_HOLE.get().defaultBlockState());
        set(level, new BlockPos(x + 6, y + 1, z - 11), CCBlocks.JELLY_SENTRY_KEY_HOLE.get().defaultBlockState());
        cursorZ -= 22;
    }

    private void bossRoom(WorldGenLevel level, RandomSource random, int x, int y) {
        int z = cursorZ;
        room(level, random, x, y, z, 11, 12, 28);
        setSpawner(level, new BlockPos(x, y + 1, z - 14));
        set(level, new BlockPos(x, y + 1, z - 25), CCBlocks.JELLY_BOSS_KEY_HOLE.get().defaultBlockState());
        for (int dx = -7; dx <= 7; dx += 7) {
            for (int dz = -20; dz <= -8; dz += 6) {
                set(level, new BlockPos(x + dx, y + 1, z + dz), CCBlocks.PURPLE_TRAMPOJELLY.get().defaultBlockState());
            }
        }
        cursorZ -= 28;
    }

    private void rewardRoom(WorldGenLevel level, RandomSource random, int x, int y) {
        int z = cursorZ;
        room(level, random, x, y, z, 6, 6, 18);
        BlockPos chestPos = new BlockPos(x, y + 1, z - 13);
        set(level, chestPos, Blocks.CHEST.defaultBlockState());
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(LOOT_TABLE, random.nextLong());
        }
        set(level, new BlockPos(x + 2, y + 1, z - 13), CCBlocks.BLOCK_TELEPORTER.get().defaultBlockState());
        cursorZ -= 18;
    }

    private void room(WorldGenLevel level, RandomSource random, int centerX, int y, int startZ, int radiusX, int height, int length) {
        for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dy = 0; dy <= height; dy++) {
                for (int dz = 0; dz >= -length; dz--) {
                    boolean wall = dx == -radiusX || dx == radiusX || dy == 0 || dy == height || dz == 0 || dz == -length;
                    BlockPos pos = new BlockPos(centerX + dx, y + dy, startZ + dz);
                    if (wall) {
                        set(level, pos, jawBreaker(random));
                    } else {
                        set(level, pos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        for (int dz = -2; dz >= -length + 2; dz -= 5) {
            set(level, new BlockPos(centerX - radiusX, y + 3, startZ + dz), CCBlocks.JAW_BREAKER_LIGHT.get().defaultBlockState());
            set(level, new BlockPos(centerX + radiusX, y + 3, startZ + dz), CCBlocks.JAW_BREAKER_LIGHT.get().defaultBlockState());
        }
        for (int dx = -1; dx <= 1; dx++) {
            set(level, new BlockPos(centerX + dx, y + 1, startZ), Blocks.AIR.defaultBlockState());
            set(level, new BlockPos(centerX + dx, y + 2, startZ), Blocks.AIR.defaultBlockState());
            set(level, new BlockPos(centerX + dx, y + 1, startZ - length), Blocks.AIR.defaultBlockState());
            set(level, new BlockPos(centerX + dx, y + 2, startZ - length), Blocks.AIR.defaultBlockState());
        }
    }

    private void placeJellyPads(WorldGenLevel level, int x, int y, int z, int count, BlockState state) {
        for (int i = 0; i < count; i++) {
            int dx = (i % 3 - 1) * 2;
            int dz = -i * 2;
            set(level, new BlockPos(x + dx, y, z + dz), state);
        }
    }

    private static BlockState jawBreaker(RandomSource random) {
        return random.nextInt(10) == 0
            ? CCBlocks.JAW_BREAKER_LIGHT.get().defaultBlockState()
            : CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState();
    }

    private static BlockState randomJelly(RandomSource random) {
        return switch (random.nextInt(5)) {
            case 0 -> CCBlocks.RED_TRAMPOJELLY.get().defaultBlockState();
            case 1 -> CCBlocks.PURPLE_TRAMPOJELLY.get().defaultBlockState();
            case 2 -> CCBlocks.YELLOW_TRAMPOJELLY.get().defaultBlockState();
            case 3 -> CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState();
            default -> CCBlocks.TRAMPOJELLY.get().defaultBlockState();
        };
    }

    private static void setSpawner(WorldGenLevel level, BlockPos pos) {
        set(level, pos, Blocks.SPAWNER.defaultBlockState());
    }

    private static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
    }

    private enum Module {
        JUMP,
        WATER,
        MOB
    }
}
