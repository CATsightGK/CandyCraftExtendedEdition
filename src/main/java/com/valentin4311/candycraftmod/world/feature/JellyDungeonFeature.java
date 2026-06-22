package com.valentin4311.candycraftmod.world.feature;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.BasicCandySlimeEntity;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

public class JellyDungeonFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation LOOT_TABLE = new ResourceLocation(CandyCraft.MODID, "chests/jelly_dungeon");
    private int cursorZ;
    private int posX;
    private int incrementer;

    public JellyDungeonFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public static void generateInDungeonLevel(ServerLevel level, BlockPos origin) {
        RandomSource random = level.getRandom();
        purgeDungeonItemEntities(level, origin, -36, 36, -7, 56, -430, 24);
        clearArea(level, origin, -36, 36, -7, 56, -430, 24);
        new JellyDungeonFeature(NoneFeatureConfiguration.CODEC).legacyDungeon(level, random, origin);
        purgeDungeonItemEntities(level, origin, -36, 36, -7, 56, -430, 24);
    }

    public static void generateDebugShowcase(ServerLevel level, BlockPos origin) {
        RandomSource random = RandomSource.create(189L);
        JellyDungeonFeature feature = new JellyDungeonFeature(NoneFeatureConfiguration.CODEC);
        clearArea(level, origin, -50, 135, -12, 65, -470, 40);
        feature.legacyDungeon(level, random, origin);

        int baseX = origin.getX() + 85;
        int baseY = origin.getY();
        int baseZ = origin.getZ();
        int zCursor = baseZ;
        feature.posX = 0;
        feature.spawnRoom189(level, RandomSource.create(1001L), baseX - 1, baseY - 1, zCursor - 1);
        zCursor -= 16;
        feature.posX = 0;
        feature.genCoridor189(level, RandomSource.create(1002L), baseX + 7, baseY, zCursor);
        zCursor -= 20;
        feature.posX = 0;
        feature.genJumpCraft189(level, RandomSource.create(1003L), baseX + 5, baseY - 3, zCursor);
        zCursor -= 58;
        feature.posX = 0;
        feature.genWaterRoom189(level, RandomSource.create(1004L), baseX + 7, baseY, zCursor);
        zCursor -= 40;
        feature.posX = 0;
        feature.genMob189(level, RandomSource.create(1005L), baseX + 7, baseY, zCursor);
        zCursor -= 70;
        feature.posX = 0;
        feature.genMiniBossRoom189(level, RandomSource.create(1006L), baseX + 7, baseY, zCursor);
        zCursor -= 38;
        feature.posX = 0;
        feature.genBossRoom189(level, RandomSource.create(1007L), baseX + 7, baseY - 3, zCursor);
        zCursor -= 62;
        feature.posX = 0;
        feature.genReward189(level, RandomSource.create(1008L), baseX + 7, baseY - 3, zCursor);
    }

    public static void generateDebugWaterRoom(ServerLevel level, BlockPos origin) {
        JellyDungeonFeature feature = new JellyDungeonFeature(NoneFeatureConfiguration.CODEC);
        clearArea(level, origin, -24, 24, -4, 32, -36, 8);
        feature.genWaterRoom189(level, RandomSource.create(1004L), origin.getX(), origin.getY(), origin.getZ());
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

        legacyDungeon(level, random, origin);
        return true;
    }

    private void legacyDungeon(WorldGenLevel level, RandomSource random, BlockPos origin) {
        int x = origin.getX();
        int y = origin.getY();
        int z = origin.getZ();
        posX = 0;
        spawnRoom189(level, random, x - 1, y - 1, z - 1);
        int route = random.nextInt(4);
        if (route == 0) {
            genCoridor189(level, random, x + 7, y, z - posX);
            genJumpCraft189(level, random, x + 5, y - 3, z - posX);
            genCoridor189(level, random, x + 7, y, z - posX);
            genWaterRoom189(level, random, x + 7, y, z - posX);
            genCoridor189(level, random, x + 7, y, z - posX);
            genMob189(level, random, x + 7, y, z - posX);
        } else if (route == 1) {
            genCoridor189(level, random, x + 7, y, z - posX);
            genMob189(level, random, x + 7, y, z - posX);
            genCoridor189(level, random, x + 7, y, z - posX);
            genWaterRoom189(level, random, x + 7, y, z - posX);
            genCoridor189(level, random, x + 7, y, z - posX);
            genJumpCraft189(level, random, x + 5, y - 3, z - posX);
        } else if (route == 2) {
            genCoridor189(level, random, x + 7, y, z - posX);
            genJumpCraft189(level, random, x + 5, y - 3, z - posX);
            genCoridor189(level, random, x + 7, y, z - posX);
            genMob189(level, random, x + 7, y, z - posX);
            genCoridor189(level, random, x + 7, y, z - posX);
            genWaterRoom189(level, random, x + 7, y, z - posX);
        } else {
            genCoridor189(level, random, x + 7, y, z - posX);
            genWaterRoom189(level, random, x + 7, y, z - posX);
            genCoridor189(level, random, x + 7, y, z - posX);
            genMob189(level, random, x + 7, y, z - posX);
            genCoridor189(level, random, x + 7, y, z - posX);
            genJumpCraft189(level, random, x + 5, y - 3, z - posX);
        }
        genCoridor189(level, random, x + 7, y, z - posX);
        int pezZ = z - posX;
        genMiniBossRoom189(level, random, x + 7, y, pezZ);
        fillLoweredPezGap(level, x + 7, y, pezZ);
        int postPezY = y - 2;
        int kingY = postPezY - 1;
        genCoridor189(level, random, x + 7, postPezY, z - posX);
        if (random.nextBoolean()) {
            genJumpCraft189(level, random, x + 5, postPezY - 3, z - posX);
            genCoridor189(level, random, x + 7, postPezY, z - posX);
            genMob189(level, random, x + 7, postPezY, z - posX);
        } else {
            genMob189(level, random, x + 7, postPezY, z - posX);
            genCoridor189(level, random, x + 7, postPezY, z - posX);
            genJumpCraft189(level, random, x + 5, postPezY - 3, z - posX);
        }
        genCoridor189(level, random, x + 7, postPezY, z - posX);
        int kingZ = z - posX;
        genBossRoom189(level, random, x + 7, kingY, kingZ);
        fillLoweredKingGap(level, x + 7, kingY, kingZ);
        int postKingY = kingY - 1;
        genCoridor189(level, random, x + 7, postKingY, z - posX);
        genReward189(level, random, x + 7, postKingY, z - posX);
    }

    private void spawnRoom189(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        for (int i = 0; i < 10; i++) {
            for (int k = 0; k < 2; k++) {
                for (int j = 0; j < 3; j++) {
                    set(level, x + i, y + 1 + k * 4, z + j, jawBreaker(random));
                }
            }
        }
        for (int i = 0; i < 10; i++) {
            for (int k = 0; k < 3; k++) {
                for (int j = 0; j < 2; j++) {
                    if (!(j == 0 && i >= 8)) {
                        set(level, x + i, y + 2 + k, z - 1 + j * 4, jawBreaker(random));
                    }
                }
            }
        }
        for (int i = 0; i < 2; i++) {
            for (int k = 0; k < 3; k++) {
                for (int j = 0; j < 3; j++) {
                    set(level, x - 1 + i * 11, y + 2 + k, z + j, jawBreaker(random));
                }
            }
        }
        for (int i = 0; i < 2; i++) {
            for (int k = 0; k < 2; k++) {
                for (int j = 0; j < 3; j++) {
                    set(level, x + 8 + i, y + 1 + k * 4, z + j - 3, jawBreaker(random));
                }
            }
        }
        for (int i = 0; i < 2; i++) {
            for (int k = 0; k < 3; k++) {
                for (int j = 0; j < 3; j++) {
                    set(level, x + 7 + i * 3, y + 2 + k, z + j - 3, jawBreaker(random));
                }
            }
        }
        legacyEntranceDoor189(level, x, y, z - 4);
        set(level, x + 1, y + 1, z + 1, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 1, y + 2, z + 1, CCBlocks.BLOCK_TELEPORTER.get().defaultBlockState());
        set(level, x - 1, y + 3, z + 1, CCBlocks.TRAMPOJELLY.get().defaultBlockState());
        set(level, x - 2, y + 3, z + 1, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 10, y + 3, z + 1, CCBlocks.TRAMPOJELLY.get().defaultBlockState());
        set(level, x + 11, y + 3, z + 1, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 10, y + 3, z - 2, CCBlocks.TRAMPOJELLY.get().defaultBlockState());
        set(level, x + 11, y + 3, z - 2, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 7, y + 3, z - 2, CCBlocks.TRAMPOJELLY.get().defaultBlockState());
        set(level, x + 6, y + 3, z - 2, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 6, y + 3, z - 1, CCBlocks.TRAMPOJELLY.get().defaultBlockState());
        set(level, x + 6, y + 3, z + 3, CCBlocks.TRAMPOJELLY.get().defaultBlockState());
        set(level, x + 6, y + 3, z + 4, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 2, y + 3, z - 1, CCBlocks.TRAMPOJELLY.get().defaultBlockState());
        set(level, x + 2, y + 3, z - 2, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 2, y + 3, z + 3, CCBlocks.TRAMPOJELLY.get().defaultBlockState());
        set(level, x + 2, y + 3, z + 4, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        for (int i = 0; i < 3; i++) {
            set(level, x + 6, y + 2 + i, z + 1, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        }
        posX += 5;
    }

    private void genCoridor189(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        for (int i = 0; i < 4; i++) {
            for (int k = 0; k < 2; k++) {
                for (int j = 0; j < 10; j++) {
                    set(level, x - 1 + i, y + 1 + k * 4, z - 1 - j, jawBreaker(random));
                }
            }
        }
        boolean lamp = false;
        boolean side = false;
        for (int i = 0; i < 2; i++) {
            for (int k = 0; k < 3; k++) {
                for (int j = 0; j < 10; j++) {
                    int px = x - 1 + i * 3;
                    int py = y + 2 + k;
                    int pz = z - 1 - j;
                    if (!lamp) {
                        set(level, px, py, pz, jawBreaker(random));
                    } else {
                        set(level, px, py, pz, j < 9 ? CCBlocks.RED_TRAMPOJELLY.get().defaultBlockState() : jawBreaker(random));
                        set(level, px + (side ? 1 : -1), py, pz, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
                    }
                    lamp = !lamp;
                }
            }
            side = !side;
        }
        legacyCorridorDoor189(level, x - 8, y, z - 6);
        posX += 10;
    }

    private void genJumpCraft189(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        incrementer = -2;
        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 2; k++) {
                for (int j = 0; j < 41; j++) {
                    set(level, x - 1 + i, y + 1 + k * 32, z - 1 - j, jawBreaker(random));
                }
            }
        }
        boolean lamp = false;
        boolean side = false;
        for (int i = 0; i < 2; i++) {
            for (int k = 0; k < 31; k++) {
                for (int j = 0; j < 41; j++) {
                    int px = x - 1 + i * 7;
                    int py = y + 2 + k;
                    int pz = z - 1 - j;
                    if (!lamp) {
                        set(level, px, py, pz, jawBreaker(random));
                    } else {
                        set(level, px, py, pz, CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState());
                        set(level, px + (side ? 1 : -1), py, pz, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
                    }
                    lamp = !lamp;
                }
            }
            side = !side;
        }
        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 31; k++) {
                for (int j = 0; j < 10; j++) {
                    set(level, x - 1 + i, y + 2 + k, z - 41, jawBreaker(random));
                }
            }
        }
        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 25; k++) {
                for (int j = 0; j < 10; j++) {
                    set(level, x - 1 + i, y + 8 + k, z + j, jawBreaker(random));
                }
            }
        }
        for (int i = 0; i < 8; i++) {
            for (int k = 0; k < 4; k++) {
                for (int j = 0; j < 10; j++) {
                    set(level, x - 1 + i, y + 1 + k, z + j, jawBreaker(random));
                }
            }
        }
        for (int i = 0; i < 6; i++) {
            for (int k = 0; k < 2; k++) {
                for (int j = 0; j < 40; j++) {
                    set(level, x + i, y + 2 + k, z + j - 40, Blocks.WATER.defaultBlockState());
                }
            }
        }
        set(level, x + 1 + random.nextInt(5), y + 5, z - 2, CCBlocks.PURPLE_TRAMPOJELLY.get().defaultBlockState());
        for (int i = 0; i < 9; i++) {
            genStep189(level, random, x, y, z + (i == 0 ? 0 : incrementer));
        }
        clearDoor(level, x + 2, y + 5, z - 41, 2, 3);
        set(level, x + 3, y + 4, z, jawBreaker(random));
        set(level, x + 2, y + 4, z, jawBreaker(random));
        set(level, x + 3, y + 4, z - 1, CCBlocks.MARSHMALLOW_LADDER.get().defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
        set(level, x + 2, y + 4, z - 1, CCBlocks.MARSHMALLOW_LADDER.get().defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
        set(level, x, y + 8, z - 40, CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState());
        set(level, x + 1, y + 8, z - 40, CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState());
        set(level, x + 4, y + 8, z - 40, CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState());
        set(level, x + 5, y + 8, z - 40, CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState());
        for (int i = 0; i < 4; i++) {
            for (int k = 0; k < 2; k++) {
                for (int j = 0; j < 3; j++) {
                    set(level, x + 1 + i, y + 4 + k * 4, z - 42 - j, jawBreaker(random));
                }
            }
        }
        for (int i = 0; i < 2; i++) {
            for (int k = 0; k < 3; k++) {
                for (int j = 0; j < 3; j++) {
                    set(level, x + 1 + i * 3, y + 5 + k, z - 42 - j, jawBreaker(random));
                }
            }
        }
        clearDoor(level, x + 2, y + 4, z - 41, 2, 5);
        set(level, x + 4, y + 11, z - 41, Blocks.REDSTONE_LAMP.defaultBlockState().setValue(BlockStateProperties.LIT, true));
        set(level, x + 4, y + 11, z - 40, Blocks.LEVER.defaultBlockState()
            .setValue(LeverBlock.FACE, AttachFace.WALL)
            .setValue(LeverBlock.FACING, Direction.SOUTH)
            .setValue(LeverBlock.POWERED, true));
        genRedstone189(level, x + 4, y + 10, z - 42);
        genRedstone189(level, x + 5, y + 8, z - 42);
        genRedstone189(level, x + 6, y + 6, z - 42);
        genRedstone189(level, x + 6, y + 4, z - 43);
        set(level, x + 5, y + 10, z - 42, wallTorchFromLegacyMeta(1, true));
        set(level, x + 6, y + 8, z - 42, wallTorchFromLegacyMeta(1, false));
        set(level, x + 6, y + 6, z - 43, wallTorchFromLegacyMeta(4, true));
        for (int i = 0; i < 6; i++) {
            setStatic(level, x + i, y + 3, z - 40, Blocks.AIR.defaultBlockState());
            setStatic(level, x + i, y + 3, z - 41, CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState());
            setStatic(level, x + i, y + 3, z - 42, stickyPiston(Direction.SOUTH));
            genRedstone189(level, x + i, y + 3, z - 43);
        }
        set(level, x + 2, y + 5, z - 43, slab(CCBlocks.LICORICE_BRICK_SLAB.get(), false));
        set(level, x + 3, y + 5, z - 43, slab(CCBlocks.LICORICE_BRICK_SLAB.get(), false));
        clearDoor(level, x + 2, y + 4, z - 41, 2, 5);
        set(level, x + 2, y + 4, z - 41, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 3, y + 4, z - 41, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        forceJumpEndPistonsRetracted(level, x, y, z);
        legacyCorridorDoor189(level, x - 6, y + 3, z + 4);
        posX += 44;
    }

    private void forceJumpEndPistonsRetracted(WorldGenLevel level, int x, int y, int z) {
        for (int i = 0; i < 6; i++) {
            setStatic(level, x + i, y + 3, z - 40, Blocks.AIR.defaultBlockState());
            setStatic(level, x + i, y + 3, z - 41, CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState());
            setStatic(level, x + i, y + 3, z - 42, stickyPiston(Direction.SOUTH));
            setStatic(level, x + i, y + 3, z - 43, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
            setStatic(level, x + i, y + 4, z - 43, Blocks.AIR.defaultBlockState());
        }
    }

    private void genStep189(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        if (random.nextBoolean()) {
            incrementer -= 4;
            set(level, x + 1 + random.nextInt(5), y + 5 + random.nextInt(4), z - 6, CCBlocks.PURPLE_TRAMPOJELLY.get().defaultBlockState());
        } else {
            incrementer -= 3;
            set(level, x + 2 + random.nextInt(3), y + 20, z - 4, CCBlocks.HONEY_LAMP.get().defaultBlockState());
        }
    }

    private void genWaterRoom189(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        for (int i = 0; i < 24; i++) {
            for (int j = 1; j < 24; j++) {
                for (int k = 0; k < 24; k++) {
                    if (i == 0 || i == 23 || j == 1 || j == 23 || k == 0 || k == 23) {
                        set(level, x + i - 12, y + j, z - k - 1, jawBreaker(random));
                    }
                }
            }
        }
        restoreLayeredWater(level, x, y, z);
        for (int i = 1; i < 23; i++) {
            for (int j = 5; j < 23; j++) {
                for (int k = 1; k < 23; k++) {
                    if (random.nextInt(100) == 0) {
                        spawnEntity(level, CCEntityTypes.TORNADO_JELLY.get(), x + i - 12 + 0.5D, y + j + 0.5D, z - k - 1 + 0.5D, random);
                    }
                }
            }
        }
        for (int i = 1; i < 23; i++) {
            for (int k = 1; k < 23; k++) {
                BlockState floor = random.nextBoolean() ? slab(CCBlocks.LICORICE_BRICK_SLAB.get(), false) : random.nextBoolean() ? CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState() : random.nextBoolean() ? CCBlocks.LICORICE_BLOCK.get().defaultBlockState() : CCBlocks.JAW_BREAKER_LIGHT.get().defaultBlockState();
                set(level, x + i - 12, y + 2, z - k - 1, floor);
            }
        }
        clearDoor(level, x, y + 2, z - 1, 2, 3);
        clearDoor(level, x, y + 20, z - 24, 2, 3);
        sealLayeredWaterDoorways(level, random, x, y, z);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 19; j++) {
                set(level, x + i - 1, y + j + 5, z - 27, jawBreaker(random));
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 23; j++) {
                set(level, x - 1, y + j + 1, z - 25 - i, jawBreaker(random));
                set(level, x + 2, y + j + 1, z - 25 - i, jawBreaker(random));
            }
        }
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = -26; dz <= -25; dz++) {
                set(level, x + dx, y + 23, z + dz, jawBreaker(random));
                set(level, x + dx, y + 1, z + dz, CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState());
                set(level, x + dx, y, z + dz, jawBreaker(random));
            }
        }
        set(level, x + 1, y + 1, z - 27, jawBreaker(random));
        set(level, x, y + 1, z - 27, jawBreaker(random));
        restoreLayeredWater(level, x, y, z);
        legacyCorridorDoor189(level, x - 8, y, z + 4);
        posX += 27;
    }

    private void restoreLayeredWater(WorldGenLevel level, int x, int y, int z) {
        BlockState sourceWater = Blocks.WATER.defaultBlockState().setValue(net.minecraft.world.level.block.LiquidBlock.LEVEL, 0);
        for (int i = 1; i < 23; i++) {
            for (int j = 5; j < 23; j++) {
                for (int k = 1; k < 23; k++) {
                    BlockState state = j < 21 && (j & 1) == 1 ? sourceWater : Blocks.AIR.defaultBlockState();
                    setStatic(level, x + i - 12, y + j, z - k - 1, state);
                }
            }
        }
    }

    private void sealLayeredWaterDoorways(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        for (int dx = -1; dx <= 2; dx++) {
            set(level, x + dx, y + 1, z - 1, jawBreaker(random));
            set(level, x + dx, y + 5, z - 1, jawBreaker(random));
            set(level, x + dx, y + 19, z - 24, jawBreaker(random));
            set(level, x + dx, y + 23, z - 24, jawBreaker(random));
        }
        for (int dy = 2; dy <= 4; dy++) {
            set(level, x - 1, y + dy, z - 1, jawBreaker(random));
            set(level, x + 2, y + dy, z - 1, jawBreaker(random));
        }
        for (int dy = 20; dy <= 22; dy++) {
            set(level, x - 1, y + dy, z - 24, jawBreaker(random));
            set(level, x + 2, y + dy, z - 24, jawBreaker(random));
        }
        clearDoor(level, x, y + 2, z - 1, 2, 3);
        clearDoor(level, x, y + 20, z - 24, 2, 3);
    }

    private void genMob189(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        for (int i = 0; i < 22; i++) {
            for (int j = 0; j < 7; j++) {
                for (int k = 0; k < 55; k++) {
                    if (i == 0 || i == 21 || j == 0 || j == 6 || k == 0 || k == 54) {
                        BlockState wall = random.nextInt(3) != 0 ? CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState() : random.nextInt(10) == 0 ? CCBlocks.LICORICE_BLOCK.get().defaultBlockState() : CCBlocks.LICORICE_BRICK.get().defaultBlockState();
                        set(level, x + i - 10, y + j + 1, z - k - 1, wall);
                    }
                }
            }
        }
        for (int k = 6; k < 56; k++) {
            genRedstone189(level, x + 8, y - 1, z - k);
            genRedstone189(level, x - 7, y - 1, z - k);
        }
        for (int k = 6; k < 20; k++) {
            if (k != 13 && k != 12) {
                genRedstone189(level, x - 12 + k, y - 1, z - 55);
            }
            genRedstone189(level, x - 12 + k, y - 1, z - 53);
            genRedstone189(level, x - 12 + k, y - 1, z - 51);
        }
        set(level, x + 2, y, z - 55, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x - 1, y, z - 55, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 1, y, z - 55, wallTorchFromLegacyMeta(2, false));
        set(level, x, y, z - 55, wallTorchFromLegacyMeta(1, false));
        for (int i = 0; i < 8; i++) {
            genColumn189(level, random, x + 8, y + 1, z - 7 - i * 6, true, i + 1);
            genColumn189(level, random, x - 7, y + 1, z - 7 - i * 6, false, i + 1);
        }
        clearDoor(level, x, y + 2, z - 1, 2, 3);
        legacyIronDoor(level, x, y + 2, z - 55, 3, 8);
        legacyIronDoor(level, x + 1, y + 2, z - 55, 3, 9);
        set(level, x + 1, y, z - 53, repeater(Direction.EAST));
        set(level, x - 2, y, z - 51, repeater(Direction.WEST));
        legacyCorridorDoor189(level, x - 8, y, z + 4);
        posX += 55;
    }

    private void genColumn189(WorldGenLevel level, RandomSource random, int x, int y, int z, boolean side, int id) {
        set(level, x, y, z, Blocks.SPAWNER.defaultBlockState());
        if (level.getBlockEntity(new BlockPos(x, y, z)) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(CCEntityTypes.TORNADO_JELLY.get(), level.getRandom());
        }
        set(level, x, y + 1, z, CCBlocks.LICORICE_BLOCK.get().defaultBlockState());
        set(level, x, y + 5, z, CCBlocks.LICORICE_BLOCK.get().defaultBlockState());
        set(level, x, y + 2, z, Blocks.REDSTONE_LAMP.defaultBlockState());
        set(level, x, y + 3, z, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x, y + 4, z, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + (side ? 2 : -2), y + 5, z, CCBlocks.LICORICE_BRICK.get().defaultBlockState());
        set(level, x + (side ? 1 : -1), y + 5, z, CCBlocks.LICORICE_BRICK.get().defaultBlockState());
        set(level, x + (side ? 2 : -2), y + 1, z, CCBlocks.LICORICE_BRICK.get().defaultBlockState());
        set(level, x + (side ? 1 : -1), y + 1, z, CCBlocks.LICORICE_BRICK.get().defaultBlockState());
        set(level, x + (side ? -1 : 1), y + 1, z + 1, CCBlocks.LICORICE_BRICK_SLAB.get().defaultBlockState());
        set(level, x + (side ? -1 : 1), y + 1, z - 1, CCBlocks.LICORICE_BRICK_SLAB.get().defaultBlockState());
        set(level, x + (side ? -1 : 1), y + 5, z + 1, slab(CCBlocks.LICORICE_BRICK_SLAB.get(), true));
        set(level, x + (side ? -1 : 1), y + 5, z - 1, slab(CCBlocks.LICORICE_BRICK_SLAB.get(), true));
        set(level, x + (side ? 2 : -2), y + 4, z, slab(CCBlocks.LICORICE_BRICK_SLAB.get(), true));
        set(level, x + (side ? 1 : -1), y + 4, z, slab(CCBlocks.LICORICE_BRICK_SLAB.get(), true));
        set(level, x + (side ? 2 : -2), y + 2, z, CCBlocks.LICORICE_BRICK_SLAB.get().defaultBlockState());
        set(level, x + (side ? 1 : -1), y + 2, z, CCBlocks.LICORICE_BRICK_SLAB.get().defaultBlockState());
        set(level, x + (side ? 1 : -1), y + 1, z + 1, legacyLicoriceStair(3));
        set(level, x + (side ? 2 : -2), y + 1, z + 1, legacyLicoriceStair(3));
        set(level, x, y + 1, z + 1, legacyLicoriceStair(3));
        set(level, x + (side ? 1 : -1), y + 5, z + 1, legacyLicoriceStair(7));
        set(level, x + (side ? 2 : -2), y + 5, z + 1, legacyLicoriceStair(7));
        set(level, x, y + 5, z + 1, legacyLicoriceStair(7));
        set(level, x + (side ? 1 : -1), y + 1, z - 1, legacyLicoriceStair(2));
        set(level, x + (side ? 2 : -2), y + 1, z - 1, legacyLicoriceStair(2));
        set(level, x, y + 1, z - 1, legacyLicoriceStair(2));
        set(level, x + (side ? 1 : -1), y + 5, z - 1, legacyLicoriceStair(6));
        set(level, x + (side ? 2 : -2), y + 5, z - 1, legacyLicoriceStair(6));
        set(level, x, y + 5, z - 1, legacyLicoriceStair(6));
        set(level, x, y + 2, z - 1, CCBlocks.LICORICE_BRICK_SLAB.get().defaultBlockState());
        set(level, x, y + 2, z + 1, CCBlocks.LICORICE_BRICK_SLAB.get().defaultBlockState());
        set(level, x + (side ? -1 : 1), y + 1, z, Blocks.LEVER.defaultBlockState()
            .setValue(LeverBlock.FACE, AttachFace.WALL)
            .setValue(LeverBlock.FACING, side ? Direction.WEST : Direction.EAST)
            .setValue(LeverBlock.POWERED, false));
        set(level, x + (side ? 1 : -1), y + 1, z, wallTorchFromLegacyMeta(side ? 1 : 2, true));
        genRedstone189(level, x + (side ? 1 : -1), y - 1, z);
        genRedstone189(level, x + (side ? 1 : -1), y - 2, z - 1);
        clearDoor(level, x + (side ? 1 : -1), y, z - 1, 1, 1);
        if (id != 8) {
            set(level, x, y - 1, z - 2, repeater(Direction.SOUTH));
        }
    }

    private void genMiniBossRoom189(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        y -= 2;
        for (int i = 0; i < 24; i++) {
            for (int j = 0; j < 24; j++) {
                for (int k = 0; k < 24; k++) {
                    if (i == 0 || i == 23 || j == 0 || j == 1 || j == 23 || k == 0 || k == 23) {
                        set(level, x + i - 11, y + j, z - k - 1, jawBreaker(random));
                    }
                    if (k != 0 && k != 23 && i != 23 && i != 0 && j == 1 && (i == 1 || i == 3 || i == 5 || i == 7 || i == 22 || i == 20 || i == 18 || i == 16 || k == 1 || k == 3 || k == 5 || k == 7 || k == 22 || k == 20 || k == 18 || k == 16)) {
                        set(level, x + i - 11, y + j, z - k - 1, CCBlocks.GRENADINE.get().defaultBlockState());
                    }
                    if (j == 6 && (i == 1 || i == 22 || k == 1 || k == 22)) {
                        set(level, x + i - 11, y + j, z - k - 1, jawBreaker(random));
                    }
                    if (j == 6 && ((i == 1 && k == 1) || (i == 22 && k == 1) || (i == 1 && k == 22) || (i == 22 && k == 22))) {
                        set(level, x + i - 11, y + j, z - k - 1, CCBlocks.GRENADINE.get().defaultBlockState());
                    }
                }
            }
        }
        set(level, x - 10, y + 23, z - 2, Blocks.GLASS.defaultBlockState());
        set(level, x + 11, y + 23, z - 2, Blocks.GLASS.defaultBlockState());
        set(level, x - 10, y + 23, z - 23, Blocks.GLASS.defaultBlockState());
        set(level, x + 11, y + 23, z - 23, Blocks.GLASS.defaultBlockState());
        set(level, x, y + 2, z - 24, CCBlocks.JELLY_SENTRY_KEY_HOLE.get().defaultBlockState());
        set(level, x, y + 3, z - 24, CCBlocks.JELLY_SENTRY_KEY_HOLE.get().defaultBlockState());
        clearDoor(level, x, y + 2, z - 1, 2, 4);
        clearDoor(level, x, y + 4, z - 1, 2, 2);
        set(level, x, y + 2, z - 24, CCBlocks.JELLY_SENTRY_KEY_HOLE.get().defaultBlockState());
        set(level, x, y + 3, z - 24, CCBlocks.JELLY_SENTRY_KEY_HOLE.get().defaultBlockState());
        spawnEntity(level, CCEntityTypes.PEZ_JELLY.get(), x + 1.0D, y + 2.0D, z - 12.0D, random);
        legacyCorridorDoor189(level, x - 8, y + 2, z + 4);
        posX += 24;
    }

    private void fillLoweredPezGap(WorldGenLevel level, int x, int y, int z) {
        BlockState wall = CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState();
        for (int dy = 0; dy <= 4; dy++) {
            set(level, x - 1, y + dy, z - 1, wall);
            set(level, x + 2, y + dy, z - 1, wall);
        }
        for (int dx = -1; dx <= 2; dx++) {
            set(level, x + dx, y + 4, z - 1, wall);
        }
        for (int dx = 0; dx <= 1; dx++) {
            set(level, x + dx, y, z - 1, wall);
            set(level, x + dx, y + 1, z - 1, wall);
        }
        clearDoor(level, x, y + 2, z - 1, 2, 2);
    }

    private void fillLoweredKingGap(WorldGenLevel level, int x, int y, int z) {
        flattenKingEntranceWall(level, x, y, z);
    }

    private void flattenKingEntranceWall(WorldGenLevel level, int x, int y, int z) {
        BlockState wall = CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState();
        for (int dx = -1; dx <= 2; dx++) {
            for (int dy = 0; dy <= 5; dy++) {
                set(level, x + dx, y + dy, z - 1, wall);
            }
        }
        set(level, x, y - 1, z, wall);
        set(level, x + 1, y - 1, z, wall);
        clearDoor(level, x, y + 2, z - 1, 2, 3);
        set(level, x, y + 2, z - 1, wall);
        set(level, x + 1, y + 2, z - 1, wall);
    }

    private void genBossRoom189(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 50; j++) {
                for (int k = 0; k < 49; k++) {
                    if (i == 0 || i == 49 || j == 0 || j == 1 || j == 49 || k == 0 || k == 48) {
                        set(level, x + i - 24, y + j - 1, z - k - 1, jawBreaker(random));
                    }
                }
            }
        }
        for (int i = 0; i < 50; i += 2) {
            for (int k = 0; k < 49; k++) {
                if (i != 0 && i != 49 && k != 0 && k != 48) {
                    set(level, x + i - 24, y, z - k - 1, CCBlocks.CANDY_CANE_SLAB.get().defaultBlockState());
                }
            }
        }
        for (int i = 0; i < 50; i++) {
            for (int k = 0; k < 49; k += 2) {
                if (i != 0 && i != 49 && k != 0 && k != 48) {
                    set(level, x + i - 24, y, z - k - 1, CCBlocks.CANDY_CANE_SLAB.get().defaultBlockState());
                }
            }
        }
        set(level, x - 23, y + 48, z - 2, Blocks.GLASS.defaultBlockState());
        set(level, x + 24, y + 48, z - 2, Blocks.GLASS.defaultBlockState());
        set(level, x - 23, y + 48, z - 48, Blocks.GLASS.defaultBlockState());
        set(level, x + 24, y + 48, z - 48, Blocks.GLASS.defaultBlockState());
        clearDoor(level, x, y + 2, z - 1, 2, 1);
        clearDoor(level, x, y + 3, z - 1, 2, 2);
        set(level, x, y + 2, z - 49, CCBlocks.JELLY_BOSS_KEY_HOLE.get().defaultBlockState());
        set(level, x, y + 1, z - 49, CCBlocks.JELLY_BOSS_KEY_HOLE.get().defaultBlockState());
        spawnEntity(level, CCEntityTypes.KING_SLIME.get(), x + 1.0D, y + 2.0D, z - 25.0D, random);
        legacyCorridorDoor189(level, x - 8, y + 1, z + 4);
        posX += 49;
    }

    private void genReward189(WorldGenLevel level, RandomSource random, int x, int y, int z) {
        for (int i = 0; i < 24; i++) {
            for (int j = 0; j < 24; j++) {
                for (int k = 0; k < 24; k++) {
                    if (i == 0 || i == 23 || j == 0 || j == 1 || j == 23 || k == 0 || k == 23) {
                        set(level, x + i - 11, y + j, z - k - 1, jawBreaker(random));
                    }
                    if (k != 0 && k != 23 && i != 23 && i != 0 && j == 1 && (i == 1 || i == 3 || i == 5 || i == 7 || i == 22 || i == 20 || i == 18 || i == 16 || k == 1 || k == 3 || k == 5 || k == 7 || k == 22 || k == 20 || k == 18 || k == 16)) {
                        set(level, x + i - 11, y + j, z - k - 1, CCBlocks.GRENADINE.get().defaultBlockState());
                    }
                    if ((j == 12 || j == 6) && (i == 1 || i == 22 || k == 1 || k == 22)) {
                        set(level, x + i - 11, y + j, z - k - 1, jawBreaker(random));
                    }
                    if ((j == 12 || j == 6) && ((i == 1 && k == 1) || (i == 22 && k == 1) || (i == 1 && k == 22) || (i == 22 && k == 22))) {
                        set(level, x + i - 11, y + j, z - k - 1, CCBlocks.GRENADINE.get().defaultBlockState());
                    }
                }
            }
        }
        set(level, x + 1, y + 2, z - 1, Blocks.AIR.defaultBlockState());
        set(level, x, y + 2, z - 1, Blocks.AIR.defaultBlockState());
        set(level, x + 1, y + 3, z - 1, Blocks.AIR.defaultBlockState());
        set(level, x, y + 3, z - 1, Blocks.AIR.defaultBlockState());
        BlockPos chestPos = new BlockPos(x, y + 2, z - 15);
        set(level, chestPos, Blocks.CHEST.defaultBlockState());
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(LOOT_TABLE, random.nextLong());
            chest.setItem(0, new ItemStack(CCItems.JELLY_CROWN.get()));
        }
        set(level, x + 1, y + 2, z - 15, CCBlocks.BLOCK_TELEPORTER.get().defaultBlockState());
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
        keyChest(level, new BlockPos(x + 3, y + 1, z - 3), CCItems.JELLY_SENTRY_KEY.get());

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
        setSpawner(level, new BlockPos(x - 13, y + 1, z), CCEntityTypes.YELLOW_JELLY.get());
        set(level, new BlockPos(x - 13, y + 1, z + 3), CCBlocks.JELLY_SENTRY_KEY_HOLE.get().defaultBlockState());
        setSpawner(level, new BlockPos(x + 13, y + 1, z), CCEntityTypes.RED_JELLY.get());
        set(level, new BlockPos(x + 13, y + 1, z - 3), CCBlocks.JELLY_BOSS_KEY_HOLE.get().defaultBlockState());
        setSpawner(level, new BlockPos(x - 13, y + 5, z - 13), CCEntityTypes.TORNADO_JELLY.get());
        setSpawner(level, new BlockPos(x + 13, y + 5, z - 13), CCEntityTypes.YELLOW_JELLY.get());
        placeJellyPads(level, x - 13, y + 5, z - 13, 4, CCBlocks.PURPLE_TRAMPOJELLY.get().defaultBlockState());
        placeJellyPads(level, x + 13, y + 5, z - 13, 4, CCBlocks.RED_TRAMPOJELLY.get().defaultBlockState());
        set(level, new BlockPos(x - 13, y - 2, z + 13), CCBlocks.JELLY_SHOCK_ABSORBER.get().defaultBlockState());
        set(level, new BlockPos(x + 13, y - 2, z + 13), CCBlocks.JELLY_BOSS_KEY_HOLE.get().defaultBlockState());

        BlockPos chestPos = new BlockPos(x + 13, y + 1, z + 3);
        keyChest(level, chestPos, CCItems.JELLY_BOSS_KEY.get());
        setSpawner(level, new BlockPos(x + 13, y - 2, z + 13), CCEntityTypes.KING_SLIME.get());
        BlockPos crownChestPos = new BlockPos(x + 13, y - 2, z + 15);
        lootChest(level, random, crownChestPos);
    }

    private void legacyEntranceDoor189(WorldGenLevel level, int x, int y, int z) {
        placeLegacyDoorRedstone189(level, x, y, z);
        set(level, x + 7, y + 2, z, CCBlocks.LICORICE_BRICK.get().defaultBlockState());
        set(level, x + 10, y + 2, z, CCBlocks.LICORICE_BRICK.get().defaultBlockState());
        set(level, x + 8, y + 5, z, CCBlocks.LICORICE_BRICK.get().defaultBlockState());
        set(level, x + 9, y + 5, z, CCBlocks.LICORICE_BRICK.get().defaultBlockState());
        set(level, x + 8, y + 3, z, licoriceDoorStair(Direction.WEST, true));
        set(level, x + 9, y + 3, z, licoriceDoorStair(Direction.EAST, true));
        set(level, x + 8, y + 4, z, licoriceDoorStair(Direction.WEST, true));
        set(level, x + 9, y + 4, z, licoriceDoorStair(Direction.EAST, true));
        set(level, x + 8, y + 2, z, legacyLicoriceStair(3));
        set(level, x + 9, y + 2, z, legacyLicoriceStair(3));
        set(level, x + 8, y + 1, z + 1, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 9, y + 1, z + 1, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 8, y + 2, z + 1, pressurePlate());
        set(level, x + 9, y + 2, z + 1, pressurePlate());
    }

    private void legacyCorridorDoor189(WorldGenLevel level, int x, int y, int z) {
        placeLegacyDoorRedstone189(level, x, y, z);
        set(level, x + 7, y + 2, z, CCBlocks.LICORICE_BRICK.get().defaultBlockState());
        set(level, x + 10, y + 2, z, CCBlocks.LICORICE_BRICK.get().defaultBlockState());
        set(level, x + 8, y + 3, z, licoriceDoorStair(Direction.WEST, true));
        set(level, x + 9, y + 3, z, licoriceDoorStair(Direction.EAST, true));
        set(level, x + 8, y + 4, z, licoriceDoorStair(Direction.WEST, true));
        set(level, x + 9, y + 4, z, licoriceDoorStair(Direction.EAST, true));
        set(level, x + 8, y + 2, z, slab(CCBlocks.LICORICE_BRICK_SLAB.get(), false));
        set(level, x + 9, y + 2, z, slab(CCBlocks.LICORICE_BRICK_SLAB.get(), false));
        set(level, x + 8, y + 1, z + 1, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 9, y + 1, z + 1, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 8, y + 2, z + 1, pressurePlate());
        set(level, x + 9, y + 2, z + 1, pressurePlate());
    }

    private void placeLegacyDoorRedstone189(WorldGenLevel level, int x, int y, int z) {
        set(level, x + 6, y + 3, z, legacyStickyPiston(13));
        set(level, x + 6, y + 4, z, legacyStickyPiston(13));
        set(level, x + 7, y + 3, z, legacyPistonHead(13));
        set(level, x + 7, y + 4, z, legacyPistonHead(13));
        set(level, x + 11, y + 3, z, legacyStickyPiston(12));
        set(level, x + 11, y + 4, z, legacyStickyPiston(12));
        set(level, x + 10, y + 3, z, legacyPistonHead(12));
        set(level, x + 10, y + 4, z, legacyPistonHead(12));
        genRedstone189(level, x + 9, y, z);
        genRedstone189(level, x + 8, y, z);
        genRedstone189(level, x + 11, y, z);
        genRedstone189(level, x + 6, y, z);
        genRedstone189(level, x + 11, y, z + 1);
        genRedstone189(level, x + 6, y, z + 1);
        genRedstone189(level, x + 12, y + 1, z + 1);
        genRedstone189(level, x + 5, y + 1, z + 1);
        genRedstone189(level, x + 13, y + 1, z + 1);
        genRedstone189(level, x + 4, y + 1, z + 1);
        genRedstone189(level, x + 14, y + 1, z + 1);
        genRedstone189(level, x + 3, y + 1, z + 1);
        genRedstone189(level, x + 15, y + 1, z + 1);
        genRedstone189(level, x + 2, y + 1, z + 1);
        set(level, x + 10, y, z, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 7, y, z, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        placeOpposingPistonRepeaters(level, x + 7, x + 10, y + 1, z);
        genRedstone189(level, x + 15, y + 2, z);
        genRedstone189(level, x + 2, y + 2, z);
        genRedstone189(level, x + 14, y + 2, z);
        genRedstone189(level, x + 3, y + 2, z);
        set(level, x + 13, y + 3, z, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 4, y + 3, z, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 12, y + 4, z, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 5, y + 4, z, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 5, y + 3, z, wallTorchFromLegacyMeta(1, true));
        set(level, x + 12, y + 3, z, wallTorchFromLegacyMeta(2, true));
        set(level, x + 14, y + 3, z + 1, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 3, y + 3, z + 1, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        reinforceLegacyDoorCircuit189(level, x, y, z);
    }

    private void genRedstone189(WorldGenLevel level, int x, int y, int z) {
        set(level, x, y, z, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x, y + 1, z, redstoneWire());
    }

    private void reinforceLegacyDoorCircuit189(WorldGenLevel level, int x, int y, int z) {
        int[][] redstoneRuns = {
            {9, 0, 0}, {8, 0, 0}, {11, 0, 0}, {6, 0, 0},
            {11, 0, 1}, {6, 0, 1},
            {12, 1, 1}, {5, 1, 1}, {13, 1, 1}, {4, 1, 1},
            {14, 1, 1}, {3, 1, 1}, {15, 1, 1}, {2, 1, 1},
            {15, 2, 0}, {2, 2, 0}, {14, 2, 0}, {3, 2, 0}
        };
        for (int[] offset : redstoneRuns) {
            genRedstone189(level, x + offset[0], y + offset[1], z + offset[2]);
        }
        set(level, x + 7, y, z, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        set(level, x + 10, y, z, CCBlocks.JAW_BREAKER_BLOCK.get().defaultBlockState());
        placeOpposingPistonRepeaters(level, x + 7, x + 10, y + 1, z);
    }

    private static void placeOpposingPistonRepeaters(WorldGenLevel level, int westDoorX, int eastDoorX, int y, int z) {
        set(level, westDoorX, y, z, repeater(Direction.EAST));
        set(level, eastDoorX, y, z, repeater(Direction.WEST));
    }

    private static void ironDoor(WorldGenLevel level, int x, int y, int z, Direction facing, boolean rightHinge) {
        BlockState lower = Blocks.IRON_DOOR.defaultBlockState()
            .setValue(DoorBlock.FACING, facing)
            .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
            .setValue(DoorBlock.HINGE, rightHinge ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT)
            .setValue(DoorBlock.OPEN, false);
        BlockState upper = lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        set(level, x, y, z, lower);
        set(level, x, y + 1, z, upper);
    }

    private static void legacyIronDoor(WorldGenLevel level, int x, int y, int z, int lowerMeta, int upperMeta) {
        Direction facing = switch (lowerMeta & 3) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.SOUTH;
            case 2 -> Direction.WEST;
            default -> Direction.NORTH;
        };
        boolean open = (lowerMeta & 4) != 0;
        boolean rightHinge = (upperMeta & 1) != 0;
        boolean powered = (upperMeta & 2) != 0;
        BlockState lower = Blocks.IRON_DOOR.defaultBlockState()
            .setValue(DoorBlock.FACING, facing)
            .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
            .setValue(DoorBlock.HINGE, rightHinge ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT)
            .setValue(DoorBlock.OPEN, open)
            .setValue(DoorBlock.POWERED, powered);
        BlockState upper = lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        set(level, x, y, z, lower);
        set(level, x, y + 1, z, upper);
    }

    private static BlockState licoriceDoorStair(Direction facing, boolean top) {
        return CCBlocks.LICORICE_BRICK_STAIRS.get().defaultBlockState()
            .setValue(net.minecraft.world.level.block.StairBlock.FACING, facing)
            .setValue(net.minecraft.world.level.block.StairBlock.HALF, top ? net.minecraft.world.level.block.state.properties.Half.TOP : net.minecraft.world.level.block.state.properties.Half.BOTTOM);
    }

    private static BlockState legacyLicoriceStair(int meta) {
        Direction facing = switch (meta & 3) {
            case 0 -> Direction.EAST;
            case 1 -> Direction.WEST;
            case 2 -> Direction.SOUTH;
            default -> Direction.NORTH;
        };
        return licoriceDoorStair(facing, (meta & 4) != 0);
    }

    private static Direction legacyDirectionalMeta(int meta) {
        return switch (meta & 7) {
            case 0 -> Direction.DOWN;
            case 1 -> Direction.UP;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.WEST;
            case 5 -> Direction.EAST;
            default -> Direction.NORTH;
        };
    }

    private static BlockState legacyStickyPiston(int meta) {
        return Blocks.STICKY_PISTON.defaultBlockState()
            .setValue(BlockStateProperties.FACING, legacyDirectionalMeta(meta))
            .setValue(BlockStateProperties.EXTENDED, (meta & 8) != 0);
    }

    private static BlockState legacyPistonHead(int meta) {
        return Blocks.PISTON_HEAD.defaultBlockState()
            .setValue(PistonHeadBlock.FACING, legacyDirectionalMeta(meta))
            .setValue(PistonHeadBlock.TYPE, PistonType.STICKY)
            .setValue(PistonHeadBlock.SHORT, false);
    }

    private static BlockState wallTorchFromLegacyMeta(int meta, boolean lit) {
        Direction facing = switch (meta) {
            case 1 -> Direction.EAST;
            case 2 -> Direction.WEST;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.NORTH;
            default -> Direction.NORTH;
        };
        BlockState state = Blocks.REDSTONE_WALL_TORCH.defaultBlockState()
            .setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        return state.hasProperty(BlockStateProperties.LIT) ? state.setValue(BlockStateProperties.LIT, lit) : state;
    }

    private static BlockState redstoneWire() {
        return Blocks.REDSTONE_WIRE.defaultBlockState()
            .setValue(BlockStateProperties.POWER, 0)
            .setValue(RedStoneWireBlock.NORTH, RedstoneSide.SIDE)
            .setValue(RedStoneWireBlock.EAST, RedstoneSide.SIDE)
            .setValue(RedStoneWireBlock.SOUTH, RedstoneSide.SIDE)
            .setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE);
    }

    private static BlockState repeater(Direction facing) {
        return Blocks.REPEATER.defaultBlockState()
            .setValue(RepeaterBlock.FACING, facing)
            .setValue(RepeaterBlock.POWERED, false);
    }

    private static BlockState stickyPiston(Direction facing) {
        return Blocks.STICKY_PISTON.defaultBlockState()
            .setValue(BlockStateProperties.FACING, facing)
            .setValue(BlockStateProperties.EXTENDED, false);
    }

    private static BlockState pressurePlate() {
        return Blocks.STONE_PRESSURE_PLATE.defaultBlockState()
            .setValue(BlockStateProperties.POWERED, false);
    }

    private static BlockState slab(net.minecraft.world.level.block.Block block, boolean top) {
        return block.defaultBlockState().setValue(SlabBlock.TYPE, top ? SlabType.TOP : SlabType.BOTTOM);
    }

    private static void clearDoor(WorldGenLevel level, int x, int y, int z, int width, int height) {
        for (int dx = 0; dx < width; dx++) {
            for (int dy = 0; dy < height; dy++) {
                set(level, new BlockPos(x + dx, y + dy, z), Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static void spawnEntity(WorldGenLevel level, EntityType<?> type, double x, double y, double z, RandomSource random) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        Entity entity = type.create(serverLevel);
        if (entity != null) {
            entity.moveTo(x, y, z, random.nextFloat() * 360.0F, 0.0F);
            if (entity instanceof BasicCandySlimeEntity slime) {
                slime.prepareDungeonBossSpawn();
            }
            serverLevel.addFreshEntity(entity);
        }
    }

    private static void set(WorldGenLevel level, int x, int y, int z, BlockState state) {
        set(level, new BlockPos(x, y, z), state);
    }

    private static void setStatic(WorldGenLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, 18);
    }

    private void lootChest(WorldGenLevel level, RandomSource random, BlockPos pos) {
        set(level, pos, Blocks.CHEST.defaultBlockState());
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(LOOT_TABLE, random.nextLong());
        }
    }

    private void keyChest(WorldGenLevel level, BlockPos pos, Item key) {
        set(level, pos, Blocks.CHEST.defaultBlockState());
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            chest.setItem(11, new ItemStack(CCItems.GUMMY.get(), 8));
            chest.setItem(13, new ItemStack(key));
            chest.setItem(15, new ItemStack(CCItems.PEZ.get(), 4));
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
        setSpawner(level, new BlockPos(x - 3, y + 1, z - 7), CCEntityTypes.YELLOW_JELLY.get());
        setSpawner(level, new BlockPos(x + 3, y + 1, z - 10), CCEntityTypes.RED_JELLY.get());
        set(level, new BlockPos(x, y + 1, z - 15), CCBlocks.JELLY_SENTRY_KEY_HOLE.get().defaultBlockState());
        placeJellyPads(level, x, y + 1, z - 8, 6, CCBlocks.RED_TRAMPOJELLY.get().defaultBlockState());
        cursorZ -= 18;
    }

    private void miniBossRoom(WorldGenLevel level, RandomSource random, int x, int y) {
        int z = cursorZ;
        room(level, random, x, y, z, 9, 9, 22);
        setSpawner(level, new BlockPos(x, y + 1, z - 11), CCEntityTypes.PEZ_JELLY.get());
        set(level, new BlockPos(x - 6, y + 1, z - 11), CCBlocks.JELLY_SENTRY_KEY_HOLE.get().defaultBlockState());
        set(level, new BlockPos(x + 6, y + 1, z - 11), CCBlocks.JELLY_SENTRY_KEY_HOLE.get().defaultBlockState());
        cursorZ -= 22;
    }

    private void bossRoom(WorldGenLevel level, RandomSource random, int x, int y) {
        int z = cursorZ;
        room(level, random, x, y, z, 11, 12, 28);
        setSpawner(level, new BlockPos(x, y + 1, z - 14), CCEntityTypes.KING_SLIME.get());
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

    private static void setSpawner(WorldGenLevel level, BlockPos pos, EntityType<?> entityType) {
        set(level, pos, Blocks.SPAWNER.defaultBlockState());
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(entityType, level.getRandom());
        }
    }

    private static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
    }

    private static boolean updateNeighbors(BlockState state) {
        net.minecraft.world.level.block.Block block = state.getBlock();
        return block == Blocks.REDSTONE_WIRE
            || block == Blocks.REDSTONE_TORCH
            || block == Blocks.REDSTONE_WALL_TORCH
            || block == Blocks.REPEATER
            || block == Blocks.REDSTONE_LAMP
            || block == Blocks.STICKY_PISTON
            || block == Blocks.PISTON_HEAD
            || block == Blocks.STONE_PRESSURE_PLATE
            || block == Blocks.IRON_DOOR
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

    private enum Module {
        JUMP,
        WATER,
        MOB
    }
}
