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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.AABB;

public class SuguardDungeonFeature extends Feature<NoneFeatureConfiguration> {
    private interface StatePattern {
        BlockState get(int x, int y, int z);
    }

    public SuguardDungeonFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public static void generateInDungeonLevel(ServerLevel level, BlockPos origin) {
        RandomSource random = level.getRandom();
        purgeDungeonItemEntities(level, origin, -132, 64, -63, 190, -160, 160);
        clearArea(level, origin, -132, 64, -63, 190, -160, 160);
        new SuguardDungeonFeature(NoneFeatureConfiguration.CODEC).legacyDungeon(level, random, origin);
        purgeDungeonItemEntities(level, origin, -132, 64, -63, 190, -160, 160);
    }

    public static void generateDebugShowcase(ServerLevel level, BlockPos origin) {
        RandomSource random = RandomSource.create(1122L);
        clearArea(level, origin, -145, 430, -70, 200, -175, 305);
        SuguardDungeonFeature feature = new SuguardDungeonFeature(NoneFeatureConfiguration.CODEC);
        feature.legacyDungeon(level, random, origin);

        int x = origin.getX() + 165;
        int y = origin.getY();
        int z = origin.getZ();
        feature.spawnRoom(level, RandomSource.create(2001L), new BlockPos(x, y, z));
        feature.zCorridor(level, x + 28, y, z);
        feature.xCorridor(level, x + 56, y, z);
        feature.archerRoom(level, RandomSource.create(2002L), x + 105, y, z + 60);
        feature.waterRoom(level, RandomSource.create(2003L), x + 145, y, z + 40);
        feature.barrierRoom(level, RandomSource.create(2004L), x + 185, y, z);
        feature.jumpRoom(level, RandomSource.create(2005L), x + 225, y, z);
        feature.fallRoom(level, RandomSource.create(2006L), x + 275, y, z);
        feature.fightRoom(level, RandomSource.create(2007L), x + 335, 11, z);
        feature.bossRoom(level, RandomSource.create(2008L), x + 395, y - 3, z, -20, 3, 0, true);
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
        spawnRoomCeiling(level, x, y, z);

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
    }

    private void spawnRoomCeiling(WorldGenLevel level, int x, int y, int z) {
        box(level, x - 3, y + 4, z - 3, x + 3, y + 4, z + 3, caramelBrick());
        box(level, x - 1, y + 4, z - 1, x + 1, y + 4, z + 1, honeyLamp());
    }

    private void zCorridor(WorldGenLevel level, int x, int y, int z) {
        box(level, x - 1, y, z - 8, x + 1, y, z, chocolate());
        box(level, x - 1, y + 4, z - 8, x + 1, y + 4, z, chocolate());
        box(level, x - 1, y + 1, z - 8, x - 1, y + 3, z, stair(Direction.WEST, false));
        box(level, x + 1, y + 1, z - 8, x + 1, y + 3, z, stair(Direction.EAST, false));
        box(level, x - 1, y + 1, z - 4, x, y + 3, z - 4, caramel());
        box(level, x + 1, y + 1, z - 4, x + 1, y + 2, z - 4, Blocks.AIR.defaultBlockState());
        set(level, x + 3, y, z - 4, chocolate());
        set(level, x + 3, y + 1, z - 4, Blocks.REDSTONE_TORCH.defaultBlockState());
        set(level, x + 3, y + 2, z - 4, chocolate());
        redstone(level, x + 1, y, z - 5);
        redstone(level, x + 1, y, z - 4);
        redstone(level, x + 1, y, z - 3);
        redstone(level, x + 2, y, z - 4);
        box(level, x + 2, y + 1, z - 4, x + 2, y + 2, z - 4, stickyPiston(Direction.WEST));
        box(level, x + 1, y + 1, z - 3, x + 1, y + 3, z - 3, caramel());
        box(level, x + 1, y + 1, z - 5, x + 1, y + 3, z - 5, caramel());
        box(level, x - 1, y + 1, z - 3, x - 1, y + 3, z - 3, caramel());
        box(level, x - 1, y + 1, z - 5, x - 1, y + 3, z - 5, caramel());
        set(level, x, y + 1, z - 3, pressurePlate());
        set(level, x, y + 1, z - 5, pressurePlate());
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
        set(level, x - 4, y, z + 3, chocolate());
        set(level, x - 4, y + 1, z + 3, Blocks.REDSTONE_TORCH.defaultBlockState());
        set(level, x - 4, y + 2, z + 3, chocolate());
        redstone(level, x - 5, y, z + 1);
        redstone(level, x - 4, y, z + 1);
        redstone(level, x - 3, y, z + 1);
        redstone(level, x - 4, y, z + 2);
        box(level, x - 4, y + 1, z + 2, x - 4, y + 2, z + 2, stickyPiston(Direction.NORTH));
        box(level, x - 3, y + 1, z + 1, x - 3, y + 3, z + 1, caramel());
        box(level, x - 5, y + 1, z + 1, x - 5, y + 3, z + 1, caramel());
        box(level, x - 3, y + 1, z - 1, x - 3, y + 3, z - 1, caramel());
        box(level, x - 5, y + 1, z - 1, x - 5, y + 3, z - 1, caramel());
        set(level, x - 3, y + 1, z, pressurePlate());
        set(level, x - 5, y + 1, z, pressurePlate());
        xDoor(level, x, y + 1, z);
        xDoor(level, x - 8, y + 1, z);
    }

    private void archerRoom(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        hollowBox(level, x - 10, y - 20, z - 50, x + 10, y + 10, z, fastCheckerPattern(nougat(), caramel(), nougat(), honeyLamp()));
        box(level, x - 9, y - 19, z - 49, x + 9, y - 19, z - 1, (px, py, pz) ->
            random.nextFloat() < 0.85F ? CCBlocks.GRENADINE.get().defaultBlockState() : nougat());
        for (int dz = 1; dz > -50; dz--) {
            int floorY = y - 12 - (int) (Math.sin(dz / 49.0D * Math.PI) * 9.0D);
            box(level, x, floorY, z + dz, x, y - 1, z + dz, fastCheckerPattern(nougat(), caramel(), nougat(), honeyLamp()));
            set(level, x, floorY - 1, z + dz, caramel());
        }
        box(level, x, y, z - 49, x, y, z - 1, caramel());
        for (int dz = 2; dz > -50; dz -= 2) {
            int offset = (int) (Math.sin(dz / 4.0F) * 3.0F);
            int offset2 = offset / 2;
            archerTower(level, x + 5 + offset, y + offset2, z + dz);
            archerTower(level, x - 8 + (3 - offset), y + offset2, z + dz);
        }
        zDoor(level, x, y + 1, z);
        zDoor(level, x, y + 1, z - 50);
        box(level, x - 1, y - 18, z - 1, x - 1, y + 1, z - 1, CCBlocks.MARSHMALLOW_LADDER.get().defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
        setSpawner(level, x, y - 17, z - 25, CCEntityTypes.SUGUARD.get());
        keyChest(level, x + 2, y - 18, z - 44, CCItems.SUGUARD_SENTRY_KEY.get());
    }

    private void waterRoom(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        hollowBox(level, x - 5, y - 2, z - 30, x + 5, y + 5, z, fastCheckerPattern(honeyLamp(), honeyBlock()));
        box(level, x - 4, y - 1, z - 29, x + 4, y + 4, z - 1, CCBlocks.GRENADINE.get().defaultBlockState());
        zDoor(level, x, y + 1, z);
        box(level, x, y + 3, z, x, y + 4, z, chocolate());
        box(level, x, y, z, x, y - 1, z, chocolate());
        placeMarshmallowDoor(level, x, y + 1, z, Direction.SOUTH);
        zDoor(level, x, y + 1, z - 30);
        box(level, x, y + 3, z - 30, x, y + 4, z - 30, chocolate());
        box(level, x, y, z - 30, x, y - 1, z - 30, chocolate());
        placeMarshmallowDoor(level, x, y + 1, z - 30, Direction.NORTH);
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
        hollowBox(level, x - 11, y - 18, z, x + 11, y + 10, z + 52, yFastCheckerPattern(chocolate(), cobble()));
        checkerBox(level, x - 11, y + 2, z, x + 11, y + 2, z + 52, chocolate(), honeyLamp());
        box(level, x - 10, y - 17, z + 1, x + 10, y - 7, z + 51, CCBlocks.GRENADINE.get().defaultBlockState());
        for (int dx = -9; dx < 10; dx++) {
            for (int dz = 1; dz < 52; dz++) {
                if (random.nextFloat() < 0.2F) {
                    spikesPillar(level, random, x + dx, y - 12, z + dz);
                }
                box(level, x + dx, y - 17, z + dz, x + dx, y - 17 + random.nextInt(3), z + dz, chocolate());
            }
        }
        box(level, x - 10, y, z + 1, x + 10, y, z + 1, caramel());
        box(level, x - 10, y, z + 51, x + 10, y, z + 51, caramel());
        box(level, x + 10, y - 6, z + 51, x + 10, y - 1, z + 51, ladder(Direction.NORTH));
        box(level, x + 10, y - 6, z + 1, x + 10, y, z + 1, ladder(Direction.SOUTH));
        set(level, x + 10, y, z + 52, Blocks.AIR.defaultBlockState());
        set(level, x + 10, y, z + 53, stickyPiston(Direction.NORTH));
        set(level, x + 10, y + 2, z + 51, Blocks.LEVER.defaultBlockState()
            .setValue(LeverBlock.FACE, AttachFace.WALL)
            .setValue(LeverBlock.FACING, Direction.NORTH)
            .setValue(LeverBlock.POWERED, false));
        set(level, x + 10, y + 2, z + 53, wallRedstoneTorch(Direction.SOUTH));
        barrierPath(level, random, x, y, z);
        zDoor(level, x, y + 1, z);
        zDoor(level, x, y + 1, z + 52);
        keyChest(level, x - 8, y + 1, z + 49, CCItems.SUGUARD_SENTRY_KEY.get());
    }

    private void jumpRoom(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        hollowBox(level, x - 1, 10, z, x + 1, y + 4, z + 2, fastCheckerPattern(caramelBrick(), honeyLamp()));
        box(level, x, y + 1, z, x, y + 3, z, Blocks.AIR.defaultBlockState());
        hollowBox(level, x - 1, 11, z + 2, x + 1, 14, z + 4, caramelBrick());
        hollowBox(level, x - 4, 10, z + 3, x + 4, 251, z + 11, caramelBrick());
        hollowBox(level, x - 3, 11, z + 4, x + 3, 250, z + 10, jumpWallPattern());
        jellyColumn(level, x - 2, 11, z + 5, 250, 0);
        jellyColumn(level, x + 2, 11, z + 5, 250, 1);
        jellyColumn(level, x - 2, 11, z + 9, 250, 2);
        jellyColumn(level, x + 2, 11, z + 9, 250, 7);
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
        hollowBox(level, x - 4, 10, z + 11, x + 4, 251, z + 19, caramelBrick());
        hollowBox(level, x - 3, 11, z + 12, x + 3, 250, z + 18, jumpWallPattern());
        jellyColumn(level, x - 2, 11, z + 13, 250, 0);
        jellyColumn(level, x + 2, 11, z + 13, 250, 1);
        jellyColumn(level, x - 2, 11, z + 17, 250, 2);
        jellyColumn(level, x + 2, 11, z + 17, 250, 7);
        box(level, x - 2, 11, z + 13, x + 2, 11, z + 18, CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState());
        box(level, x, 12, z + 2, x, 13, z + 4, Blocks.AIR.defaultBlockState());
        box(level, x - 1, 240, z + 9, x + 1, 249, z + 12, Blocks.AIR.defaultBlockState());
        hollowBox(level, x - 1, 239, z + 11, x + 1, 249, z + 11, jumpWallPattern());
        box(level, x - 1, 239, z + 10, x + 1, 239, z + 12, CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState());
        box(level, x, 12, z + 17, x, 14, z + 19, Blocks.AIR.defaultBlockState());
        keyChest(level, x + 3, 12, z + 17, CCItems.SUGUARD_SENTRY_KEY.get());
    }

    private void fallRoom(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        hollowBox(level, x - 3, y, z - 1, x, 250, z + 1, fastCheckerPattern(chocolate(), cobble(), chocolate(), honeyLamp()));
        set(level, x - 2, y, z, CCBlocks.RED_TRAMPOJELLY.get().defaultBlockState());
        set(level, x - 1, y + 59, z, CCBlocks.RED_TRAMPOJELLY.get().defaultBlockState());
        set(level, x - 2, y + 116, z, CCBlocks.RED_TRAMPOJELLY.get().defaultBlockState());
        set(level, x - 1, y + 166, z, CCBlocks.TRAMPOJELLY.get().defaultBlockState());
        set(level, x - 2, y - 1, z, chocolate());
        set(level, x - 1, 246, z, Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH));
        set(level, x - 2, 246, z, Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH));
        set(level, x - 1, 247, z, CCBlocks.GRENADINE.get().defaultBlockState());
        set(level, x - 2, 247, z, CCBlocks.GRENADINE.get().defaultBlockState());
        box(level, x, y + 1, z, x, y + 2, z, Blocks.AIR.defaultBlockState());
        int sx = x - 4;
        hollowBox(level, sx - 8, 10, z - 4, sx, 250, z + 4, yCheckerPattern(chocolate(), cobble(), honeyLamp()));
        box(level, sx - 7, 11, z - 3, sx - 1, 11, z + 3, CCBlocks.GRENADINE.get().defaultBlockState());
        box(level, sx - 1, 10, z - 3, sx - 1, 249, z - 3, yCheckerPattern(chocolate(), cobble(), honeyLamp()));
        box(level, sx - 1, 10, z + 3, sx - 1, 249, z + 3, yCheckerPattern(chocolate(), cobble(), honeyLamp()));
        box(level, sx - 7, 10, z - 3, sx - 7, 249, z - 3, yCheckerPattern(chocolate(), cobble(), honeyLamp()));
        box(level, sx - 7, 10, z + 3, sx - 7, 249, z + 3, yCheckerPattern(chocolate(), cobble(), honeyLamp()));
        box(level, sx - 2, 248, z, sx + 1, 249, z, Blocks.AIR.defaultBlockState());
        for (int yy = 230; yy > 13; yy -= 8) {
            if (random.nextBoolean()) {
                int ox = random.nextInt(6);
                box(level, sx - 1 - ox, yy, z - 3, sx - 1 - ox, yy, z + 3, (px, py, pz) ->
                    random.nextBoolean() ? CCBlocks.JAW_BREAKER_LIGHT.get().defaultBlockState() : CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
            } else {
                int oz = random.nextInt(6);
                box(level, sx - 7, yy, z - 2 + oz, sx - 1, yy, z - 2 + oz, (px, py, pz) ->
                    random.nextBoolean() ? CCBlocks.JAW_BREAKER_LIGHT.get().defaultBlockState() : CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
            }
        }
        box(level, sx - 8, 12, z, sx - 8, 14, z, Blocks.AIR.defaultBlockState());
        keyChest(level, sx - 7, 12, z + 3, CCItems.SUGUARD_SENTRY_KEY.get());
    }

    private void fightRoom(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        BlockState licorice = CCBlocks.LICORICE_BLOCK.get().defaultBlockState();
        BlockState licoriceBrick = CCBlocks.LICORICE_BRICK.get().defaultBlockState();
        cylinder(level, x - 20, 1, z, 20, 1, licoriceBrick);
        cylinder(level, x - 20, 2, z, 20, 10, CCBlocks.GRENADINE.get().defaultBlockState());
        hollowCylinder(level, x - 20, 2, z, 20, 70, yFastCheckerPattern(licorice, licoriceBrick, caramelBrick(), honeyLamp()));
        cylinder(level, x - 20, 70, z, 20, 1, licoriceBrick);
        hollowCylinder(level, x - 20, 2, z, 5, 70, yFastCheckerPattern(licorice, licoriceBrick));
        cylinder(level, x - 20, 9, z, 15, 3, checkerPattern(caramel(), nougat()));
        cylinder(level, x - 20, 9, z, 15, 3, true, yFastCheckerPattern(licorice, licoriceBrick).offset(1));
        cylinder(level, x - 20, 30, z, 15, 3, checkerPattern(caramel(), nougat()));
        cylinder(level, x - 20, 30, z, 15, 3, true, yFastCheckerPattern(licorice, licoriceBrick).offset(2));
        cylinder(level, x - 20, 51, z, 15, 3, checkerPattern(caramel(), nougat()));
        cylinder(level, x - 20, 51, z, 15, 3, true, yFastCheckerPattern(licorice, licoriceBrick).offset(1));
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
        set(level, x - 24, 30 + yOffset, z, stickyPiston(Direction.WEST));
        set(level, x - 26, 11 + yOffset, z, CCBlocks.GRENADINE.get().defaultBlockState());
        box(level, x - 26, 31 + yOffset, z, x - 26, 32 + yOffset, z, CCBlocks.GRENADINE.get().defaultBlockState());
        set(level, x - 24, 12 + yOffset, z, wallRedstoneTorch(Direction.EAST));
        set(level, x - 16, 12 + yOffset, z, wallRedstoneTorch(Direction.WEST));
        set(level, x - 20, 12 + yOffset, z - 4, wallRedstoneTorch(Direction.SOUTH));
        set(level, x - 20, 12 + yOffset, z + 4, wallRedstoneTorch(Direction.NORTH));
        redstoneLampTorchColumn(level, x - 24, 13 + yOffset, z, 17, yOffset == 0 ? 1 : 0, Direction.EAST);
        redstoneLampTorchColumn(level, x - 16, 13 + yOffset, z, 17, yOffset == 0 ? 1 : 0, Direction.WEST);
        redstoneLampTorchColumn(level, x - 20, 13 + yOffset, z - 4, 17, yOffset == 0 ? 1 : 0, Direction.SOUTH);
        redstoneLampTorchColumn(level, x - 20, 13 + yOffset, z + 4, 17, yOffset == 0 ? 1 : 0, Direction.NORTH);
        set(level, x - 24, 30 + yOffset, z, stickyPiston(Direction.WEST));
        box(level, x - 23, 28 + yOffset, z, x - 17, 28 + yOffset, z, caramel());
        box(level, x - 20, 28 + yOffset, z - 3, x - 20, 28 + yOffset, z + 3, caramel());
        box(level, x - 23, 29 + yOffset, z, x - 17, 29 + yOffset, z, redstoneWire());
        box(level, x - 20, 29 + yOffset, z - 3, x - 20, 29 + yOffset, z + 3, redstoneWire());
    }

    private void bossRoom(WorldGenLevel level, RandomSource random, int x, int y, int z, int doorX, int doorY, int doorZ, boolean boss) {
        BlockState licorice = CCBlocks.LICORICE_BLOCK.get().defaultBlockState();
        BlockState licoriceBrick = CCBlocks.LICORICE_BRICK.get().defaultBlockState();
        BlockState lightJawBreaker = CCBlocks.JAW_BREAKER_LIGHT.get().defaultBlockState();
        BlockState jawBreaker = CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState();
        cylinder(level, x, y, z, 20, 1, fastCheckerPattern(licorice, licoriceBrick));
        hollowCylinder(level, x, y + 1, z, 20, 15, checkerPattern(licorice, lightJawBreaker, licorice));
        cylinder(level, x, y, z, 6, 1, checkerPattern(lightJawBreaker, jawBreaker));
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
        topSphere(level, x, y + 16, z, 20, lightJawBreaker, licorice);
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
        set(level, x, y, z, redstoneWire());
    }

    private void bossPillar(WorldGenLevel level, int x, int y, int z) {
        stairsLayer(level, x, y + 1, z, false);
        for (int dy = 4; dy < 30; dy += 5) {
            stairsLayer(level, x, y + dy, z, true);
            stairsLayer(level, x, y + dy + 2, z, false);
            set(level, x, y + dy, z, CCBlocks.GRENADINE.get().defaultBlockState());
            set(level, x, y + dy + 1, z, honeyLamp());
            set(level, x - 1, y + dy + 1, z, honeyLamp());
            set(level, x + 1, y + dy + 1, z, honeyLamp());
            set(level, x, y + dy + 1, z - 1, honeyLamp());
            set(level, x, y + dy + 1, z + 1, honeyLamp());
            set(level, x - 1, y + dy + 1, z - 1, CCBlocks.LICORICE_BLOCK.get().defaultBlockState());
            set(level, x - 1, y + dy + 1, z + 1, CCBlocks.LICORICE_BLOCK.get().defaultBlockState());
            set(level, x + 1, y + dy + 1, z - 1, CCBlocks.LICORICE_BLOCK.get().defaultBlockState());
            set(level, x + 1, y + dy + 1, z + 1, CCBlocks.LICORICE_BLOCK.get().defaultBlockState());
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
    private BlockState redstoneWire() { return Blocks.REDSTONE_WIRE.defaultBlockState().setValue(BlockStateProperties.POWER, 0); }
    private BlockState stickyPiston(Direction facing) { return Blocks.STICKY_PISTON.defaultBlockState().setValue(BlockStateProperties.FACING, facing).setValue(BlockStateProperties.EXTENDED, false); }
    private BlockState pressurePlate() { return Blocks.STONE_PRESSURE_PLATE.defaultBlockState().setValue(BlockStateProperties.POWERED, false); }

    private void archerTower(WorldGenLevel level, int x, int y, int z) {
        set(level, x, y, z, caramel());
    }

    private void placeMarshmallowDoor(WorldGenLevel level, int x, int y, int z, Direction facing) {
        BlockState base = CCBlocks.MARSHMALLOW_DOOR.get().defaultBlockState()
            .setValue(DoorBlock.FACING, facing)
            .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT)
            .setValue(DoorBlock.OPEN, false)
            .setValue(DoorBlock.POWERED, false);
        set(level, x, y, z, base.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        set(level, x, y + 1, z, base.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }

    private void spikesPillar(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        int height = random.nextInt(3) + 5;
        int baseY = y + random.nextInt(3);
        box(level, x, baseY, z, x, y + height, z, caramel());
        set(level, x, y + height, z, honeyLamp());
        set(level, x, y + height + 1, z, CCBlocks.SUGAR_SPIKES.get().defaultBlockState());
    }

    private void barrierPath(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        int currentX = random.nextInt(18) - 9;
        int currentZ = 2;
        int direction = 0;
        int lastDirection = 0;
        int holesLeft = 0;
        int length = random.nextInt(2) + 2;
        while (currentZ <= 50) {
            for (int i = 0; i < length && currentZ <= 50; i++) {
                if (holesLeft > 0 && random.nextFloat() < 0.2F) {
                    holesLeft--;
                } else {
                    set(level, x + currentX, y, z + currentZ, random.nextFloat() < 0.1F ? cobble() : Blocks.BARRIER.defaultBlockState());
                    set(level, x + currentX, y + 10, z + currentZ, honeyLamp());
                }
                if (direction == 0) {
                    currentZ++;
                } else if (direction == 1) {
                    currentX--;
                } else {
                    currentX++;
                }
            }
            if (direction == 0) {
                direction = lastDirection == 1 ? 2 : 1;
                lastDirection = direction;
                int wallX = direction == 1 ? -10 : 10;
                length = Math.max(1, Math.abs(wallX - currentX) - (random.nextInt(6) + 1));
            } else {
                direction = 0;
                length = Math.min(51 - currentZ, random.nextInt(4) + 4);
            }
            holesLeft = random.nextInt(2) + 2;
        }
    }

    private void jellyColumn(WorldGenLevel level, int x, int y1, int z, int y2, int offset) {
        BlockState[] states = new BlockState[] {
            CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState(),
            CCBlocks.YELLOW_TRAMPOJELLY.get().defaultBlockState(),
            CCBlocks.RED_TRAMPOJELLY.get().defaultBlockState(),
            CCBlocks.TRAMPOJELLY.get().defaultBlockState(),
            CCBlocks.PURPLE_TRAMPOJELLY.get().defaultBlockState()
        };
        for (int y = y1; y <= y2; y++) {
            set(level, x, y, z, states[Math.floorMod(y + offset, states.length)]);
        }
    }

    private BlockState wallRedstoneTorch(Direction facing) {
        return Blocks.REDSTONE_WALL_TORCH.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
            .setValue(BlockStateProperties.LIT, true);
    }

    private void redstoneLampTorchColumn(WorldGenLevel level, int x, int y, int z, int height, int offset, Direction torchFacing) {
        for (int dy = 0; dy < height; dy++) {
            if (((dy + offset) & 1) == 0) {
                set(level, x, y + dy, z, Blocks.REDSTONE_LAMP.defaultBlockState());
            } else {
                set(level, x, y + dy, z, wallRedstoneTorch(torchFacing));
            }
        }
    }

    private void hollowBox(WorldGenLevel level, int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
        hollowBox(level, x1, y1, z1, x2, y2, z2, constantPattern(state));
    }

    private void hollowBox(WorldGenLevel level, int x1, int y1, int z1, int x2, int y2, int z2, StatePattern pattern) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ) {
                        set(level, x, y, z, pattern.get(x, y, z));
                    }
                }
            }
        }
    }

    private void box(WorldGenLevel level, int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
        box(level, x1, y1, z1, x2, y2, z2, constantPattern(state));
    }

    private void box(WorldGenLevel level, int x1, int y1, int z1, int x2, int y2, int z2, StatePattern pattern) {
        int minX = Math.min(x1, x2), maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2), maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2), maxZ = Math.max(z1, z2);
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    set(level, x, y, z, pattern.get(x, y, z));
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
        cylinder(level, cx, y, cz, radius, height, false, constantPattern(state));
    }

    private void cylinder(WorldGenLevel level, int cx, int y, int cz, int radius, int height, StatePattern pattern) {
        cylinder(level, cx, y, cz, radius, height, false, pattern);
    }

    private void cylinder(WorldGenLevel level, int cx, int y, int cz, int radius, int height, boolean hollow, StatePattern pattern) {
        for (int dy = 0; dy < height; dy++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    double distance = Math.sqrt(x * x + z * z);
                    if ((!hollow && distance <= radius + 0.5D) || (hollow && Math.round(distance) == radius)) {
                        int px = cx + x;
                        int py = y + dy;
                        int pz = cz + z;
                        set(level, px, py, pz, pattern.get(px, py, pz));
                    }
                }
            }
        }
    }

    private void hollowCylinder(WorldGenLevel level, int cx, int y, int cz, int radius, int height, StatePattern pattern) {
        cylinder(level, cx, y, cz, radius, height, true, pattern);
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

    private StatePattern constantPattern(BlockState state) {
        return (x, y, z) -> state;
    }

    private PatternBuilder checkerPattern(BlockState... states) {
        return new PatternBuilder(states, Axis.ALL, false);
    }

    private PatternBuilder fastCheckerPattern(BlockState... states) {
        return new PatternBuilder(states, Axis.ALL, true);
    }

    private PatternBuilder yCheckerPattern(BlockState... states) {
        return new PatternBuilder(states, Axis.Y, false);
    }

    private PatternBuilder yFastCheckerPattern(BlockState... states) {
        return new PatternBuilder(states, Axis.Y, true);
    }

    private PatternBuilder jumpWallPattern() {
        return checkerPattern(
            CCBlocks.YELLOW_TRAMPOJELLY.get().defaultBlockState(),
            CCBlocks.RED_TRAMPOJELLY.get().defaultBlockState(),
            CCBlocks.TRAMPOJELLY.get().defaultBlockState(),
            CCBlocks.PURPLE_TRAMPOJELLY.get().defaultBlockState(),
            CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState()
        );
    }

    private enum Axis {
        ALL,
        Y
    }

    private static final class PatternBuilder implements StatePattern {
        private final BlockState[] states;
        private final Axis axis;
        private final boolean fast;
        private int offset;

        private PatternBuilder(BlockState[] states, Axis axis, boolean fast) {
            this.states = states;
            this.axis = axis;
            this.fast = fast;
        }

        private PatternBuilder offset(int offset) {
            this.offset = offset;
            return this;
        }

        @Override
        public BlockState get(int x, int y, int z) {
            int value = (axis == Axis.Y ? y : x + y + z) + offset;
            int index = fast ? (value & (states.length - 1)) : Math.floorMod(value, states.length);
            return states[index];
        }
    }

    private static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, updateNeighbors(state) ? 3 : 2);
    }

    private static boolean updateNeighbors(BlockState state) {
        Block block = state.getBlock();
        return block == Blocks.REDSTONE_WIRE
            || block == Blocks.REDSTONE_TORCH
            || block == Blocks.REDSTONE_WALL_TORCH
            || block == Blocks.REPEATER
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
                    level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), 50);
                }
            }
        }
    }

    private static void purgeDungeonItemEntities(ServerLevel level, BlockPos origin, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        AABB bounds = new AABB(
            origin.getX() + minX, origin.getY() + minY, origin.getZ() + minZ,
            origin.getX() + maxX + 1, origin.getY() + maxY + 1, origin.getZ() + maxZ + 1
        );
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, bounds)) {
            item.discard();
        }
    }
}
