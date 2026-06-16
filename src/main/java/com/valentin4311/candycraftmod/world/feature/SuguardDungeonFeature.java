package com.valentin4311.candycraftmod.world.feature;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.block.DungeonTeleporterBlock;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SuguardDungeonFeature extends Feature<NoneFeatureConfiguration> {
    public SuguardDungeonFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public static void generateInDungeonLevel(ServerLevel level, BlockPos origin) {
        RandomSource random = level.getRandom();
        clearArea(level, origin, -132, 64, -63, 190, -160, 160);
        new SuguardDungeonFeature(NoneFeatureConfiguration.CODEC).legacyDungeon(level, random, origin);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        legacyDungeon(context.level(), context.random(), context.origin());
        return true;
    }

    private void legacyDungeon(WorldGenLevel level, RandomSource random, BlockPos origin) {
        int x = origin.getX();
        int y = origin.getY();
        int z = origin.getZ();

        spawnRoom(level, random, origin);
        zCorridor(level, x, y, z - 5);
        zCorridor(level, x, y, z + 13);
        xCorridor(level, x - 5, y, z);
        xCorridor(level, x + 19, y, z);
        bossRoom(level, random, x + 40, y - 3, z, -20, 3, 0, true);

        archerRoom(level, random, x, y, z - 14);
        zCorridor(level, x, y, z - 65);
        waterRoom(level, random, x, y, z - 73);
        zCorridor(level, x, y, z - 104);
        bossRoom(level, random, x, y - 3, z - 132, 0, 3, 20, false);

        barrierRoom(level, random, x, y, z + 14);
        zCorridor(level, x, y, z + 75);
        jumpRoom(level, random, x, y, z + 76);
        zCorridor(level, x, 11, z + 104);
        bossRoom(level, random, x, 8, z + 125, 0, 3, -20, false);

        fallRoom(level, random, x - 14, y, z);
        xCorridor(level, x - 27, 11, z);
        fightRoom(level, random, x - 36, 11, z);
        xCorridor(level, x - 77, 55, z);
        bossRoom(level, random, x - 105, 52, z, 20, 3, 0, false);
    }

    private void spawnRoom(WorldGenLevel level, RandomSource random, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        checkerBox(level, x - 4, y, z - 4, x + 4, y, z + 4, caramel(), honeyLamp());
        set(level, x, y, z, caramel());
        set(level, x, y + 1, z, suguardTeleporter());
        pillar(level, x - 3, y + 1, z - 3);
        pillar(level, x + 3, y + 1, z - 3);
        pillar(level, x + 3, y + 1, z + 3);
        pillar(level, x - 3, y + 1, z + 3);

        hollowBox(level, x - 4, y + 1, z - 4, x + 4, y + 3, z + 4, caramelBrick());
        hollowBox(level, x - 3, y + 4, z - 3, x + 3, y + 4, z + 3, caramelBrick());
        hollowBox(level, x - 2, y + 5, z - 2, x + 2, y + 5, z + 2, caramel());
        set(level, x - 2, y + 4, z - 2, chocolate());
        set(level, x + 2, y + 4, z - 2, chocolate());
        set(level, x + 2, y + 4, z + 2, chocolate());
        set(level, x - 2, y + 4, z + 2, chocolate());
        set(level, x - 1, y + 5, z - 1, caramel());
        set(level, x + 1, y + 5, z - 1, caramel());
        set(level, x + 1, y + 5, z + 1, caramel());
        set(level, x - 1, y + 5, z + 1, caramel());
        box(level, x - 1, y + 6, z - 1, x + 1, y + 6, z + 1, honeyLamp());

        box(level, x - 4, y + 1, z, x - 4, y + 2, z, CCBlocks.SUGUARD_SENTRY_KEY_HOLE.get().defaultBlockState());
        box(level, x, y + 1, z + 4, x, y + 2, z + 4, CCBlocks.SUGUARD_SENTRY_KEY_HOLE.get().defaultBlockState());
        box(level, x, y + 1, z - 4, x, y + 2, z - 4, CCBlocks.SUGUARD_SENTRY_KEY_HOLE.get().defaultBlockState());
        box(level, x + 4, y + 1, z, x + 4, y + 2, z, CCBlocks.SUGUARD_BOSS_KEY_HOLE.get().defaultBlockState());
        box(level, x + 6, y + 1, z, x + 6, y + 2, z, CCBlocks.SUGUARD_BOSS_KEY_HOLE.get().defaultBlockState());
        box(level, x + 8, y + 1, z, x + 8, y + 2, z, CCBlocks.SUGUARD_BOSS_KEY_HOLE.get().defaultBlockState());
        box(level, x + 10, y + 1, z, x + 10, y + 2, z, CCBlocks.SUGUARD_SENTRY_KEY_HOLE.get().defaultBlockState());
        checkerBox(level, x + 5, y, z, x + 10, y, z, caramel(), honeyLamp());
        checkerBox(level, x + 5, y + 3, z - 1, x + 10, y + 3, z + 1, caramel(), chocolate());
        box(level, x + 5, y + 1, z + 1, x + 10, y + 2, z + 1, caramelBrick());
        box(level, x + 5, y + 1, z - 1, x + 10, y + 2, z - 1, caramelBrick());
        int r = random.nextInt(3);
        if (r == 0) {
            box(level, x - 4, y + 1, z, x - 4, y + 2, z, Blocks.AIR.defaultBlockState());
        } else if (r == 1) {
            box(level, x, y + 1, z + 4, x, y + 2, z + 4, Blocks.AIR.defaultBlockState());
        } else {
            box(level, x, y + 1, z - 4, x, y + 2, z - 4, Blocks.AIR.defaultBlockState());
        }
        clearEntry(level, x, y, z);
    }

    private void clearEntry(WorldGenLevel level, int x, int y, int z) {
        set(level, x, y, z, caramel());
        set(level, x, y + 1, z, suguardTeleporter());
        box(level, x - 1, y + 2, z - 1, x + 1, y + 3, z + 1, Blocks.AIR.defaultBlockState());
        set(level, x, y, z, caramel());
        set(level, x, y + 1, z, suguardTeleporter());
    }

    private void zCorridor(WorldGenLevel level, int x, int y, int z) {
        box(level, x - 1, y, z - 8, x + 1, y, z, chocolate());
        box(level, x - 1, y + 4, z - 8, x + 1, y + 4, z, chocolate());
        box(level, x - 1, y + 1, z - 8, x - 1, y + 3, z, stair(Direction.WEST, false));
        box(level, x + 1, y + 1, z - 8, x + 1, y + 3, z, stair(Direction.EAST, false));
        box(level, x - 1, y + 1, z - 4, x, y + 3, z - 4, caramel());
        box(level, x + 1, y + 1, z - 4, x + 1, y + 2, z - 4, Blocks.AIR.defaultBlockState());
        redstone(level, x + 1, y, z - 5);
        redstone(level, x + 1, y, z - 4);
        redstone(level, x + 1, y, z - 3);
        redstone(level, x + 2, y, z - 4);
        set(level, x + 3, y, z - 4, chocolate());
        set(level, x + 3, y + 1, z - 4, Blocks.REDSTONE_TORCH.defaultBlockState());
        set(level, x + 3, y + 2, z - 4, chocolate());
        box(level, x + 2, y + 1, z - 4, x + 2, y + 2, z - 4, Blocks.STICKY_PISTON.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.WEST));
        box(level, x + 1, y + 1, z - 3, x + 1, y + 3, z - 3, caramel());
        box(level, x + 1, y + 1, z - 5, x + 1, y + 3, z - 5, caramel());
        box(level, x - 1, y + 1, z - 3, x - 1, y + 3, z - 3, caramel());
        box(level, x - 1, y + 1, z - 5, x - 1, y + 3, z - 5, caramel());
        set(level, x, y + 1, z - 3, Blocks.STONE_PRESSURE_PLATE.defaultBlockState());
        set(level, x, y + 1, z - 5, Blocks.STONE_PRESSURE_PLATE.defaultBlockState());
        zDoor(level, x, y + 1, z);
        zDoor(level, x, y + 1, z - 8);
    }

    private void xCorridor(WorldGenLevel level, int x, int y, int z) {
        box(level, x - 8, y, z - 1, x, y, z + 1, chocolate());
        box(level, x - 8, y + 4, z - 1, x, y + 4, z + 1, chocolate());
        box(level, x - 8, y + 1, z - 1, x, y + 3, z - 1, stair(Direction.NORTH, false));
        box(level, x - 8, y + 1, z + 1, x, y + 3, z + 1, stair(Direction.SOUTH, false));
        box(level, x - 4, y + 1, z - 1, x - 4, y + 3, z, caramel());
        box(level, x - 4, y + 1, z + 1, x - 4, y + 2, z + 1, Blocks.AIR.defaultBlockState());
        redstone(level, x - 5, y, z + 1);
        redstone(level, x - 4, y, z + 1);
        redstone(level, x - 3, y, z + 1);
        redstone(level, x - 4, y, z + 2);
        set(level, x - 4, y, z + 3, chocolate());
        set(level, x - 4, y + 1, z + 3, Blocks.REDSTONE_TORCH.defaultBlockState());
        set(level, x - 4, y + 2, z + 3, chocolate());
        box(level, x - 4, y + 1, z + 2, x - 4, y + 2, z + 2, Blocks.STICKY_PISTON.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH));
        box(level, x - 3, y + 1, z + 1, x - 3, y + 3, z + 1, caramel());
        box(level, x - 5, y + 1, z + 1, x - 5, y + 3, z + 1, caramel());
        box(level, x - 3, y + 1, z - 1, x - 3, y + 3, z - 1, caramel());
        box(level, x - 5, y + 1, z - 1, x - 5, y + 3, z - 1, caramel());
        set(level, x - 3, y + 1, z, Blocks.STONE_PRESSURE_PLATE.defaultBlockState());
        set(level, x - 5, y + 1, z, Blocks.STONE_PRESSURE_PLATE.defaultBlockState());
        xDoor(level, x, y + 1, z);
        xDoor(level, x - 8, y + 1, z);
    }

    private void archerRoom(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        hollowBox(level, x - 10, y - 20, z - 50, x + 10, y + 10, z, checker(random, nougat(), caramel()));
        box(level, x - 9, y - 19, z - 49, x + 9, y - 19, z - 1, CCBlocks.GRENADINE.get().defaultBlockState());
        for (int dz = 1; dz > -50; dz--) {
            int floorY = y - 12 - (int) (Math.sin(dz / 49.0D * Math.PI) * 9.0D);
            box(level, x, floorY, z + dz, x, y - 1, z + dz, checker(random, nougat(), caramel()));
            set(level, x, floorY - 1, z + dz, caramel());
        }
        box(level, x, y, z - 49, x, y, z - 1, caramel());
        zDoor(level, x, y + 1, z);
        zDoor(level, x, y + 1, z - 50);
        box(level, x - 1, y - 18, z - 1, x - 1, y + 1, z - 1, CCBlocks.MARSHMALLOW_LADDER.get().defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
        setSpawner(level, x, y - 17, z - 25, CCEntityTypes.SUGUARD.get());
        keyChest(level, x + 2, y - 18, z - 44, CCItems.SUGUARD_SENTRY_KEY.get());
    }

    private void waterRoom(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        hollowBox(level, x - 5, y - 2, z - 30, x + 5, y + 5, z, checker(random, honeyLamp(), honeyBlock()));
        box(level, x - 4, y - 1, z - 29, x + 4, y + 4, z - 1, CCBlocks.GRENADINE.get().defaultBlockState());
        zDoor(level, x, y + 1, z);
        box(level, x, y + 3, z, x, y + 4, z, chocolate());
        box(level, x, y, z, x, y - 1, z, chocolate());
        zDoor(level, x, y + 1, z - 30);
        box(level, x, y + 3, z - 30, x, y + 4, z - 30, chocolate());
        box(level, x, y, z - 30, x, y - 1, z - 30, chocolate());
        for (int dz = 0; dz >= -25; dz -= 5) {
            box(level, x - 4, y - 1, z + dz - 2, x + 4, y + 4, z + dz - 2, honeyLamp());
            int rx = x + random.nextInt(8) - 4;
            int ry = y + random.nextInt(5) - 1;
            box(level, rx, ry, z + dz - 2, rx + 1, ry + 1, z + dz - 2, Blocks.AIR.defaultBlockState());
            if (dz != 0 && dz != -25) {
                box(level, x - 4, y + 4, z + dz - 7, x + 4, y + 4, z + dz - 3, Blocks.AIR.defaultBlockState());
            }
        }
        keyChest(level, x + 3, y + 1, z - 27, CCItems.SUGUARD_SENTRY_KEY.get());
    }

    private void barrierRoom(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        hollowBox(level, x - 11, y - 18, z, x + 11, y + 10, z + 52, checker(random, chocolate(), cobble()));
        box(level, x - 10, y - 17, z + 1, x + 10, y - 7, z + 51, CCBlocks.GRENADINE.get().defaultBlockState());
        box(level, x - 10, y, z + 1, x + 10, y, z + 1, caramel());
        box(level, x - 10, y, z + 51, x + 10, y, z + 51, caramel());
        box(level, x + 10, y - 6, z + 51, x + 10, y - 1, z + 51, ladder(Direction.NORTH));
        box(level, x + 10, y - 6, z + 1, x + 10, y, z + 1, ladder(Direction.SOUTH));
        for (int dz = 2; dz < 51; dz += 4) {
            int rx = x + random.nextInt(19) - 9;
            box(level, rx, y, z + dz, rx, y + 10, z + dz, random.nextBoolean() ? Blocks.BARRIER.defaultBlockState() : cobble());
            set(level, rx, y + 11, z + dz, honeyLamp());
        }
        zDoor(level, x, y + 1, z);
        zDoor(level, x, y + 1, z + 52);
        keyChest(level, x - 8, y + 1, z + 49, CCItems.SUGUARD_SENTRY_KEY.get());
    }

    private void jumpRoom(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        hollowBox(level, x - 4, 10, z + 3, x + 4, 251, z + 19, caramelBrick());
        box(level, x - 3, 11, z + 4, x + 3, 250, z + 18, checker(random, CCBlocks.YELLOW_TRAMPOJELLY.get().defaultBlockState(), CCBlocks.RED_TRAMPOJELLY.get().defaultBlockState()));
        box(level, x - 2, 11, z + 5, x + 2, 11, z + 9, CCBlocks.YELLOW_TRAMPOJELLY.get().defaultBlockState());
        box(level, x, 11, z + 1, x, 11, z + 4, CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState());
        int yy = 15;
        while (yy < 240) {
            set(level, x - 2 + random.nextInt(5), yy, z + 5 + random.nextInt(5), jumpPad(random, yy));
            yy += random.nextInt(8) + 5;
        }
        yy = 235;
        while (yy > 20) {
            for (int i = 0; i < random.nextInt(8) + 2; i++) {
                set(level, x - 2 + random.nextInt(5), yy, z + 13 + random.nextInt(5), CCBlocks.RED_TRAMPOJELLY.get().defaultBlockState());
            }
            yy -= random.nextInt(3) + 3;
        }
        box(level, x, 12, z + 2, x, 13, z + 4, Blocks.AIR.defaultBlockState());
        box(level, x - 1, 240, z + 9, x + 1, 249, z + 12, Blocks.AIR.defaultBlockState());
        box(level, x, 12, z + 17, x, 14, z + 19, Blocks.AIR.defaultBlockState());
        keyChest(level, x + 3, 12, z + 17, CCItems.SUGUARD_SENTRY_KEY.get());
    }

    private void fallRoom(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        hollowBox(level, x - 3, y, z - 1, x, 250, z + 1, checker(random, chocolate(), cobble()));
        set(level, x - 2, y, z, CCBlocks.RED_TRAMPOJELLY.get().defaultBlockState());
        set(level, x - 1, y + 59, z, CCBlocks.RED_TRAMPOJELLY.get().defaultBlockState());
        set(level, x - 2, y + 116, z, CCBlocks.RED_TRAMPOJELLY.get().defaultBlockState());
        set(level, x - 1, y + 166, z, CCBlocks.TRAMPOJELLY.get().defaultBlockState());
        box(level, x, y + 1, z, x, y + 2, z, Blocks.AIR.defaultBlockState());
        int sx = x - 4;
        hollowBox(level, sx - 8, 10, z - 4, sx, 250, z + 4, checker(random, chocolate(), cobble()));
        box(level, sx - 7, 11, z - 3, sx - 1, 11, z + 3, CCBlocks.GRENADINE.get().defaultBlockState());
        for (int yy = 230; yy > 13; yy -= 8) {
            if (random.nextBoolean()) {
                int ox = random.nextInt(6);
                box(level, sx - 1 - ox, yy, z - 3, sx - 1 - ox, yy, z + 3, checker(random, CCBlocks.JAW_BREAKER_LIGHT.get().defaultBlockState(), CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState()));
            } else {
                int oz = random.nextInt(6);
                box(level, sx - 7, yy, z - 2 + oz, sx - 1, yy, z - 2 + oz, checker(random, CCBlocks.JAW_BREAKER_LIGHT.get().defaultBlockState(), CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState()));
            }
        }
        box(level, sx - 8, 12, z, sx - 8, 14, z, Blocks.AIR.defaultBlockState());
        keyChest(level, sx - 7, 12, z + 3, CCItems.SUGUARD_SENTRY_KEY.get());
    }

    private void fightRoom(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        hollowCylinder(level, x - 20, 1, z, 20, 70, CCBlocks.LICORICE_BLOCK.get().defaultBlockState(), CCBlocks.LICORICE_BRICK.get().defaultBlockState(), random);
        cylinder(level, x - 20, 1, z, 20, 1, CCBlocks.LICORICE_BRICK.get().defaultBlockState());
        cylinder(level, x - 20, 2, z, 20, 10, CCBlocks.GRENADINE.get().defaultBlockState());
        cylinder(level, x - 20, 2, z, 5, 70, CCBlocks.LICORICE_BRICK.get().defaultBlockState());
        cylinder(level, x - 20, 9, z, 15, 3, checker(random, caramel(), nougat()));
        cylinder(level, x - 20, 30, z, 15, 3, checker(random, caramel(), nougat()));
        cylinder(level, x - 20, 51, z, 15, 3, checker(random, caramel(), nougat()));
        box(level, x - 39, 52, z, x - 39, 55, z, CCBlocks.MARSHMALLOW_LADDER.get().defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST));
        box(level, x, 12, z, x, 14, z, Blocks.AIR.defaultBlockState());
        box(level, x - 40, 56, z, x - 40, 58, z, Blocks.AIR.defaultBlockState());
        fightPillarLayer(level, x, z, 0);
        fightPillarLayer(level, x, z, 21);
        fightPillarLayer(level, x, z, 42);
        keyChest(level, x - 38, 56, z, CCItems.SUGUARD_BOSS_KEY.get());
    }

    private void fightPillarLayer(WorldGenLevel level, int x, int z, int yOffset) {
        setSpawner(level, x - 32, 12 + yOffset, z, CCEntityTypes.SUGUARD.get());
        setSpawner(level, x - 8, 12 + yOffset, z, CCEntityTypes.SUGUARD.get());
        setSpawner(level, x - 20, 12 + yOffset, z + 12, CCEntityTypes.SUGUARD.get());
        setSpawner(level, x - 20, 12 + yOffset, z - 12, CCEntityTypes.SUGUARD.get());
        setSpawner(level, x - 28, 12 + yOffset, z + 8, CCEntityTypes.SUGUARD.get());
        setSpawner(level, x - 28, 12 + yOffset, z - 8, CCEntityTypes.SUGUARD.get());
        setSpawner(level, x - 12, 12 + yOffset, z + 8, CCEntityTypes.SUGUARD.get());
        setSpawner(level, x - 12, 12 + yOffset, z - 8, CCEntityTypes.SUGUARD.get());
        setSpawner(level, x - 25, 12 + yOffset, z, CCEntityTypes.MAGE_SUGUARD.get());
        setSpawner(level, x - 15, 12 + yOffset, z, CCEntityTypes.MAGE_SUGUARD.get());
        setSpawner(level, x - 20, 12 + yOffset, z - 5, CCEntityTypes.SUGUARD.get());
        setSpawner(level, x - 20, 12 + yOffset, z + 5, CCEntityTypes.SUGUARD.get());

        if (yOffset == 42) {
            return;
        }

        set(level, x - 25, 30 + yOffset, z, Blocks.AIR.defaultBlockState());
        set(level, x - 24, 30 + yOffset, z, Blocks.STICKY_PISTON.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.WEST));
        set(level, x - 26, 11 + yOffset, z, CCBlocks.GRENADINE.get().defaultBlockState());
        box(level, x - 26, 31 + yOffset, z, x - 26, 32 + yOffset, z, CCBlocks.GRENADINE.get().defaultBlockState());
        set(level, x - 24, 12 + yOffset, z, Blocks.REDSTONE_TORCH.defaultBlockState());
        set(level, x - 16, 12 + yOffset, z, Blocks.REDSTONE_TORCH.defaultBlockState());
        set(level, x - 20, 12 + yOffset, z - 4, Blocks.REDSTONE_TORCH.defaultBlockState());
        set(level, x - 20, 12 + yOffset, z + 4, Blocks.REDSTONE_TORCH.defaultBlockState());
        box(level, x - 24, 13 + yOffset, z, x - 24, 29 + yOffset, z, Blocks.REDSTONE_LAMP.defaultBlockState());
        box(level, x - 16, 13 + yOffset, z, x - 16, 29 + yOffset, z, Blocks.REDSTONE_LAMP.defaultBlockState());
        box(level, x - 20, 13 + yOffset, z - 4, x - 20, 29 + yOffset, z - 4, Blocks.REDSTONE_LAMP.defaultBlockState());
        box(level, x - 20, 13 + yOffset, z + 4, x - 20, 29 + yOffset, z + 4, Blocks.REDSTONE_LAMP.defaultBlockState());
        set(level, x - 24, 30 + yOffset, z, Blocks.STICKY_PISTON.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.WEST));
        box(level, x - 23, 28 + yOffset, z, x - 17, 28 + yOffset, z, caramel());
        box(level, x - 20, 28 + yOffset, z - 3, x - 20, 28 + yOffset, z + 3, caramel());
        box(level, x - 23, 29 + yOffset, z, x - 17, 29 + yOffset, z, Blocks.REDSTONE_WIRE.defaultBlockState());
        box(level, x - 20, 29 + yOffset, z - 3, x - 20, 29 + yOffset, z + 3, Blocks.REDSTONE_WIRE.defaultBlockState());
    }

    private void bossRoom(WorldGenLevel level, RandomSource random, int x, int y, int z, int doorX, int doorY, int doorZ, boolean boss) {
        BlockState licorice = CCBlocks.LICORICE_BLOCK.get().defaultBlockState();
        BlockState lightJawBreaker = CCBlocks.JAW_BREAKER_LIGHT.get().defaultBlockState();
        hollowCylinder(level, x, y + 1, z, 20, 15, licorice, lightJawBreaker, random);
        bossFloor(level, x, y, z);
        for (int dx : new int[]{-14, 14}) {
            for (int dz : new int[]{-7, 7}) {
                bossPillar(level, x + dx, y, z + dz);
            }
        }
        for (int dx : new int[]{-7, 7}) {
            for (int dz : new int[]{-14, 14}) {
                bossPillar(level, x + dx, y, z + dz);
            }
        }
        for (int dy = 10; dy < 31; dy += 5) {
            checkerBox(level, x - 14, y + dy, z - 14, x - 14, y + dy, z + 14, licorice, lightJawBreaker);
            checkerBox(level, x + 14, y + dy, z - 14, x + 14, y + dy, z + 14, licorice, lightJawBreaker);
            checkerBox(level, x - 14, y + dy, z - 14, x + 14, y + dy, z - 14, licorice, lightJawBreaker);
            checkerBox(level, x - 14, y + dy, z + 14, x + 14, y + dy, z + 14, licorice, lightJawBreaker);
            checkerBox(level, x - 14, y + dy, z - 7, x + 14, y + dy, z - 7, licorice, lightJawBreaker);
            checkerBox(level, x - 14, y + dy, z + 7, x + 14, y + dy, z + 7, licorice, lightJawBreaker);
            checkerBox(level, x - 7, y + dy, z - 14, x - 7, y + dy, z + 14, licorice, lightJawBreaker);
            checkerBox(level, x + 7, y + dy, z - 14, x + 7, y + dy, z + 14, licorice, lightJawBreaker);
        }
        topSphere(level, x, y + 16, z, 20, licorice, lightJawBreaker);
        box(level, x + doorX, y + doorY + 1, z + doorZ, x + doorX, y + doorY + 3, z + doorZ, Blocks.AIR.defaultBlockState());
        hollowBox(level, x - 1, y - 2, z - 1, x + 1, y, z + 1, licorice);
        if (boss) {
            setSpawner(level, x, y + 1, z, CCEntityTypes.BOSS_SUGUARD.get());
            keyChest(level, x + 2, y + 1, z, CCItems.SUGUARD_EMBLEM.get());
        } else {
            set(level, x, y, z, CCBlocks.MARSHMALLOW_TRAPDOOR.get().defaultBlockState().setValue(TrapDoorBlock.HALF, Half.BOTTOM));
            set(level, x, y - 1, z, suguardTeleporter());
            keyChest(level, x + 2, y + 1, z, CCItems.SUGUARD_BOSS_KEY.get());
        }
    }

    private void bossFloor(WorldGenLevel level, int x, int y, int z) {
        BlockState licorice = CCBlocks.LICORICE_BLOCK.get().defaultBlockState();
        BlockState lightJawBreaker = CCBlocks.JAW_BREAKER_LIGHT.get().defaultBlockState();
        BlockState jawBreaker = CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState();
        for (int dx = -20; dx <= 20; dx++) {
            for (int dz = -20; dz <= 20; dz++) {
                int distance = dx * dx + dz * dz;
                if (distance <= 20 * 20) {
                    set(level, x + dx, y, z + dz, fastChecker(x + dx, y, z + dz, licorice, lightJawBreaker));
                }
                if (distance <= 6 * 6) {
                    set(level, x + dx, y, z + dz, checker(x + dx, y, z + dz, lightJawBreaker, jawBreaker));
                }
            }
        }
    }

    private void pillar(WorldGenLevel level, int x, int y, int z) {
        set(level, x, y, z, chocolate());
        set(level, x, y + 1, z, honeyBlock());
        set(level, x, y + 2, z, chocolate());
    }

    private void zDoor(WorldGenLevel level, int x, int y, int z) {
        pillar(level, x + 1, y, z);
        pillar(level, x - 1, y, z);
        box(level, x, y, z, x, y + 2, z, Blocks.AIR.defaultBlockState());
    }

    private void xDoor(WorldGenLevel level, int x, int y, int z) {
        pillar(level, x, y, z + 1);
        pillar(level, x, y, z - 1);
        box(level, x, y, z, x, y + 2, z, Blocks.AIR.defaultBlockState());
    }

    private void redstone(WorldGenLevel level, int x, int y, int z) {
        set(level, x, y - 1, z, chocolate());
        set(level, x, y, z, Blocks.REDSTONE_WIRE.defaultBlockState());
    }

    private void bossPillar(WorldGenLevel level, int x, int y, int z) {
        stairsLayer(level, x, y + 1, z, false);
        for (int dy = 0; dy < 30; dy += 5) {
            stairsLayer(level, x, y + dy + 4, z, true);
            stairsLayer(level, x, y + dy + 6, z, false);
            box(level, x - 1, y + dy, z - 1, x + 1, y + dy, z + 1, CCBlocks.LICORICE_BRICK.get().defaultBlockState());
            set(level, x, y + dy + 1, z, CCBlocks.GRENADINE.get().defaultBlockState());
            set(level, x, y + dy + 2, z, honeyLamp());
            set(level, x - 1, y + dy + 2, z, honeyLamp());
            set(level, x + 1, y + dy + 2, z, honeyLamp());
            set(level, x, y + dy + 2, z - 1, honeyLamp());
            set(level, x, y + dy + 2, z + 1, honeyLamp());
            set(level, x - 1, y + dy + 2, z - 1, CCBlocks.LICORICE_BLOCK.get().defaultBlockState());
            set(level, x - 1, y + dy + 2, z + 1, CCBlocks.LICORICE_BLOCK.get().defaultBlockState());
            set(level, x + 1, y + dy + 2, z - 1, CCBlocks.LICORICE_BLOCK.get().defaultBlockState());
            set(level, x + 1, y + dy + 2, z + 1, CCBlocks.LICORICE_BLOCK.get().defaultBlockState());
        }
    }

    private void stairsLayer(WorldGenLevel level, int x, int y, int z, boolean top) {
        BlockState base = CCBlocks.LICORICE_BRICK_STAIRS.get().defaultBlockState();
        set(level, x + 1, y, z, base.setValue(StairBlock.FACING, Direction.WEST).setValue(StairBlock.HALF, top ? Half.TOP : Half.BOTTOM));
        set(level, x - 1, y, z, base.setValue(StairBlock.FACING, Direction.EAST).setValue(StairBlock.HALF, top ? Half.TOP : Half.BOTTOM));
        set(level, x, y, z + 1, base.setValue(StairBlock.FACING, Direction.NORTH).setValue(StairBlock.HALF, top ? Half.TOP : Half.BOTTOM));
        set(level, x, y, z - 1, base.setValue(StairBlock.FACING, Direction.SOUTH).setValue(StairBlock.HALF, top ? Half.TOP : Half.BOTTOM));
        set(level, x + 1, y, z + 1, base.setValue(StairBlock.FACING, Direction.NORTH).setValue(StairBlock.HALF, top ? Half.TOP : Half.BOTTOM).setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT));
        set(level, x - 1, y, z + 1, base.setValue(StairBlock.FACING, Direction.EAST).setValue(StairBlock.HALF, top ? Half.TOP : Half.BOTTOM).setValue(StairBlock.SHAPE, StairsShape.OUTER_LEFT));
        set(level, x + 1, y, z - 1, base.setValue(StairBlock.FACING, Direction.SOUTH).setValue(StairBlock.HALF, top ? Half.TOP : Half.BOTTOM).setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT));
        set(level, x - 1, y, z - 1, base.setValue(StairBlock.FACING, Direction.EAST).setValue(StairBlock.HALF, top ? Half.TOP : Half.BOTTOM).setValue(StairBlock.SHAPE, StairsShape.OUTER_RIGHT));
    }

    private BlockState jumpPad(RandomSource random, int y) {
        if (y > 220) return CCBlocks.YELLOW_TRAMPOJELLY.get().defaultBlockState();
        if (y > 170) return random.nextBoolean() ? CCBlocks.YELLOW_TRAMPOJELLY.get().defaultBlockState() : CCBlocks.TRAMPOJELLY.get().defaultBlockState();
        int r = random.nextInt(3);
        return r == 0 ? CCBlocks.RED_TRAMPOJELLY.get().defaultBlockState() : r == 1 ? CCBlocks.TRAMPOJELLY.get().defaultBlockState() : CCBlocks.YELLOW_TRAMPOJELLY.get().defaultBlockState();
    }

    private void setSpawner(WorldGenLevel level, int x, int y, int z, EntityType<?> type) {
        set(level, x, y, z, Blocks.SPAWNER.defaultBlockState());
        BlockPos pos = new BlockPos(x, y, z);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.getSpawner().setEntityId(type, level.getLevel(), level.getRandom(), pos);
        }
    }

    private void keyChest(WorldGenLevel level, int x, int y, int z, net.minecraft.world.item.Item item) {
        set(level, x, y, z, Blocks.CHEST.defaultBlockState());
        if (level.getBlockEntity(new BlockPos(x, y, z)) instanceof ChestBlockEntity chest) {
            chest.setItem(13, new ItemStack(item));
        }
    }

    private BlockState ladder(Direction facing) {
        return CCBlocks.MARSHMALLOW_LADDER.get().defaultBlockState().setValue(LadderBlock.FACING, facing);
    }

    private BlockState stair(Direction facing, boolean top) {
        return CCBlocks.CARAMEL_BRICK_STAIRS.get().defaultBlockState()
            .setValue(StairBlock.FACING, facing)
            .setValue(StairBlock.HALF, top ? Half.TOP : Half.BOTTOM)
            .setValue(StairBlock.SHAPE, StairsShape.STRAIGHT);
    }

    private BlockState checker(RandomSource random, BlockState a, BlockState b) {
        return random.nextBoolean() ? a : b;
    }

    private BlockState checker(int x, int y, int z, BlockState a, BlockState b) {
        return Math.floorMod(x + y + z, 2) == 0 ? a : b;
    }

    private BlockState fastChecker(int x, int y, int z, BlockState a, BlockState b) {
        return ((x + y + z) & 1) == 0 ? a : b;
    }

    private BlockState caramel() { return CCBlocks.CARAMEL_BLOCK.get().defaultBlockState(); }
    private BlockState caramelBrick() { return CCBlocks.CARAMEL_BRICK.get().defaultBlockState(); }
    private BlockState chocolate() { return CCBlocks.CHOCOLATE_STONE.get().defaultBlockState(); }
    private BlockState cobble() { return CCBlocks.CHOCOLATE_COBBLESTONE.get().defaultBlockState(); }
    private BlockState honeyBlock() { return CCBlocks.HONEYCOMB_BLOCK.get().defaultBlockState(); }
    private BlockState honeyLamp() { return CCBlocks.HONEY_LAMP.get().defaultBlockState(); }
    private BlockState nougat() { return CCBlocks.NOUGAT_BLOCK.get().defaultBlockState(); }
    private BlockState suguardTeleporter() { return CCBlocks.BLOCK_TELEPORTER.get().defaultBlockState().setValue(DungeonTeleporterBlock.DUNGEON, DungeonTeleporterBlock.DungeonKind.SUGUARD); }

    private void hollowBox(WorldGenLevel level, int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
                        set(level, x, y, z, state);
                    }
                }
            }
        }
    }

    private void box(WorldGenLevel level, int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    set(level, x, y, z, state);
                }
            }
        }
    }

    private void checkerBox(WorldGenLevel level, int x1, int y1, int z1, int x2, int y2, int z2, BlockState a, BlockState b) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    set(level, x, y, z, fastChecker(x, y, z, a, b));
                }
            }
        }
    }

    private void cylinder(WorldGenLevel level, int cx, int y, int cz, int radius, int height, BlockState state) {
        for (int dy = 0; dy < height; dy++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z <= radius * radius) {
                        set(level, cx + x, y + dy, cz + z, state);
                    }
                }
            }
        }
    }

    private void hollowCylinder(WorldGenLevel level, int cx, int y, int cz, int radius, int height, BlockState a, BlockState b, RandomSource random) {
        for (int dy = 0; dy < height; dy++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    int d = x * x + z * z;
                    if (d <= radius * radius && d >= (radius - 1) * (radius - 1)) {
                        set(level, cx + x, y + dy, cz + z, checker(random, a, b));
                    }
                }
            }
        }
    }

    private void topSphere(WorldGenLevel level, int cx, int cy, int cz, int radius, BlockState a, BlockState b) {
        for (int x = -radius; x <= radius; x++) {
            for (int y = 0; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + y * y + z * z);
                    if (Math.round(distance) == radius) {
                        int px = cx + x;
                        int py = cy + y;
                        int pz = cz + z;
                        set(level, px, py, pz, fastChecker(px, py, pz, a, b));
                    }
                }
            }
        }
    }

    private static void set(WorldGenLevel level, int x, int y, int z, BlockState state) {
        set(level, new BlockPos(x, y, z), state);
    }

    private static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, updateNeighbors(state) ? 3 : 2);
    }

    private static boolean updateNeighbors(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.REDSTONE_WIRE
            || block == Blocks.REDSTONE_TORCH
            || block == Blocks.REDSTONE_WALL_TORCH
            || block == Blocks.REDSTONE_LAMP
            || block == Blocks.STICKY_PISTON
            || block == Blocks.PISTON_HEAD
            || block == Blocks.STONE_PRESSURE_PLATE
            || block == Blocks.LEVER;
    }

    private static void clearArea(ServerLevel level, BlockPos origin, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }
}
