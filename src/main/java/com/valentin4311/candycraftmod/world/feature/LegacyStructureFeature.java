package com.valentin4311.candycraftmod.world.feature;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.LegacyLeavesBlock;
import com.valentin4311.candycraftmod.block.LegacyLogBlock;
import com.valentin4311.candycraftmod.block.LegacyMetadataBlock;
import com.valentin4311.candycraftmod.block.LegacyTypeBlock;
import com.valentin4311.candycraftmod.entity.BasicCandySpiderEntity;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import com.valentin4311.candycraftmod.entity.GingerbreadManEntity;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCSweetscapeBlocks;
import com.valentin4311.candycraftmod.util.EmblemHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class LegacyStructureFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation CANDY_HOUSE_LOOT = new ResourceLocation(CandyCraft.MODID, "chests/candy_house");
    private static final ResourceLocation ICE_TOWER_LOOT = new ResourceLocation(CandyCraft.MODID, "chests/ice_tower");
    private static final ResourceLocation WATER_TEMPLE_LOOT = new ResourceLocation(CandyCraft.MODID, "chests/water_temple");
    private static final boolean ENABLE_STRUCTURE_GINGERBREAD = true;
    private final Kind kind;

    public LegacyStructureFeature(Codec<NoneFeatureConfiguration> codec, Kind kind) {
        super(codec);
        this.kind = kind;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        if (kind == Kind.CANDY_HOUSE) {
            return candyHouse(level, random, surface(level, origin));
        }
        if (kind == Kind.ICE_TOWER) {
            return iceTower(level, random, surface(level, origin));
        }
        if (kind == Kind.ICE_CREAM_DOME) {
            return iceCreamDome(level, random, surface(level, origin));
        }
        if (kind == Kind.WATER_TEMPLE) {
            return waterTemple(level, random, origin);
        }
        if (kind == Kind.GEYSER) {
            return geyser(level, random, origin);
        }
        if (kind == Kind.CHEWING_GUM_TOTEM) {
            return chewingGumTotem(level, random, surface(level, origin));
        }
        if (kind == Kind.FLOATING_ISLAND) {
            return floatingIsland(level, random, origin);
        }
        return undergroundVillage(level, random, origin);
    }

    private static BlockPos surface(WorldGenLevel level, BlockPos origin) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ());
        return new BlockPos(origin.getX(), y, origin.getZ());
    }

    private static boolean candyHouse(WorldGenLevel level, RandomSource random, BlockPos pos) {
        if (random.nextInt(2) != 0) {
            return false;
        }
        BlockPos center = pos.offset(-8, 0, -8);
        while (center.getY() > 5 && shouldCandyHouseSinkThrough(level, center)) {
            center = center.below();
        }
        center = center.above();
        if (!level.getBlockState(center).is(CCBlocks.PUDDING.get())) {
            return false;
        }

        clear(level, center.offset(-2, 0, -2), center.offset(2, 4, 2));

        BlockState log = marshmallowLog(0, Direction.Axis.Y);
        BlockState lightLogZ = CCBlocks.MARSHMALLOW_LOG_LIGHT.get().defaultBlockState()
            .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
        BlockState planks = marshmallowPlanks(0);
        BlockState darkLeaves = leafState(1);
        BlockState caneWall = CCBlocks.CANDY_CANE_WALL.get().defaultBlockState();
        BlockState caramel = CCBlocks.CARAMEL_BLOCK.get().defaultBlockState();
        BlockState slabBottom = CCBlocks.MARSHMALLOW_SLAB.get().defaultBlockState();
        BlockState slabTop = slabBottom.setValue(SlabBlock.TYPE, SlabType.TOP);
        BlockState flour = CCBlocks.FLOUR.get().defaultBlockState();

        for (int layer = -1; layer <= 4; layer++) {
            if (layer == -1 || layer == 3) {
                place(level, center, lightLogZ,
                    -2, layer, 0, -2, layer, -1, -2, layer, 1,
                    2, layer, 0, 2, layer, -1, 2, layer, 1);
                place(level, center, log,
                    -2, layer, -2, -2, layer, 2, 2, layer, -2, 2, layer, 2);
                place(level, center, darkLeaves,
                    -1, layer, -2, 0, layer, -2, 1, layer, -2,
                    -1, layer, 2, 0, layer, 2, 1, layer, 2);
                if (layer == -1) {
                    place(level, center, planks,
                        1, layer, 0, -1, layer, 0, 0, layer, -1, 0, layer, 1,
                        1, layer, 1, -1, layer, 1, 1, layer, -1, -1, layer, -1, 0, layer, 0);
                }
            }
            if (layer == 0 || layer == 2) {
                place(level, center, caneWall, -2, layer, -2, -2, layer, 2, 2, layer, 2, 2, layer, -2);
            } else {
                place(level, center, log, -2, layer, -2, -2, layer, 2, 2, layer, 2, 2, layer, -2);
            }
        }

        place(level, center, candyCaneBlock(1), -2, 0, 0, 2, 0, 0, 0, 0, 2, 0, 0, -2);
        place(level, center, candyCaneBlock(2), -2, 1, 0, 2, 1, 0);
        place(level, center, candyCaneBlock(0), 0, 1, 2, 0, 1, -2);
        place(level, center, candyCaneBlock(1),
            -2, 1, 1, 2, 1, -1, 1, 1, 2, -1, 1, -2,
            -2, 1, -1, 2, 1, 1, -1, 1, 2, 1, 1, -2);
        place(level, center, candyCaneBlock(2),
            -2, 0, 1, 2, 0, -1, -2, 0, -1, 2, 0, 1);
        place(level, center, candyCaneBlock(0),
            1, 0, 2, -1, 0, -2, -1, 0, 2, 1, 0, -2);

        place(level, center, caramel,
            2, 2, 0, 2, 2, 1, 2, 2, -1,
            -2, 2, 0, -2, 2, 1, -2, 2, -1,
            0, 2, 2, 1, 2, 2, -1, 2, 2,
            0, 2, -2, 1, 2, -2, -1, 2, -2);

        place(level, center, slabTop, 1, 3, 0, -1, 3, 0, 0, 3, -1, 0, 3, 1);
        place(level, center, slabBottom, 0, 4, 2, 0, 4, -2, 2, 4, 0, -2, 4, 0);
        place(level, center, planks, 1, 3, 1, -1, 3, 1, 1, 3, -1, -1, 3, -1);

        place(level, center, flour,
            -2, -2, 0, -2, -2, -1, -2, -2, -2, -2, -2, 1, -2, -2, 2,
            2, -2, 0, 2, -2, -1, 2, -2, -2, 2, -2, 1, 2, -2, 2,
            -1, -2, -2, 0, -2, -2, 1, -2, -2,
            -1, -2, 2, 0, -2, 2, 1, -2, 2);

        BlockPos chest = center.offset(1, -2, 2);
        set(level, chest, Blocks.CHEST.defaultBlockState());
        loot(level, random, chest, CANDY_HOUSE_LOOT);
        set(level, center.below(), CCBlocks.HONEY_LAMP.get().defaultBlockState());
        return true;
    }

    private static boolean iceTower(WorldGenLevel level, RandomSource random, BlockPos pos) {
        BlockPos ground = null;
        for (int y = Math.min(100, level.getMaxBuildHeight() - 1); y > 50; y--) {
            BlockPos candidate = new BlockPos(pos.getX(), y, pos.getZ());
            if (level.getBlockState(candidate).is(CCBlocks.PUDDING.get())) {
                ground = candidate;
                break;
            }
        }
        if (ground == null) {
            return false;
        }
        for (int dx = 0; dx <= 6; dx++) {
            for (int dz = 0; dz <= 6; dz++) {
                if (!level.getBlockState(ground.offset(dx, 0, dz)).is(CCBlocks.PUDDING.get())) {
                    return false;
                }
            }
        }
        pos = ground.above();

        BlockState vanilla = iceCream(3);
        for (int y = 0; y < 2; y++) {
            set(level, pos.offset(2, y, 0), vanilla);
            set(level, pos.offset(4, y, 0), vanilla);
            set(level, pos.offset(1, y, 1), vanilla);
            set(level, pos.offset(5, y, 1), vanilla);
            set(level, pos.offset(0, y, 2), vanilla);
            set(level, pos.offset(6, y, 2), vanilla);
            set(level, pos.offset(0, y, 4), vanilla);
            set(level, pos.offset(6, y, 4), vanilla);
            set(level, pos.offset(1, y, 5), vanilla);
            set(level, pos.offset(5, y, 5), vanilla);
            set(level, pos.offset(2, y, 6), vanilla);
            set(level, pos.offset(4, y, 6), vanilla);
        }

        set(level, pos.offset(1, 0, 0), vanilla);
        set(level, pos.offset(0, 0, 1), vanilla);
        set(level, pos.offset(5, 0, 0), vanilla);
        set(level, pos.offset(6, 0, 1), vanilla);
        set(level, pos.offset(0, 0, 5), vanilla);
        set(level, pos.offset(1, 0, 6), vanilla);
        set(level, pos.offset(5, 0, 6), vanilla);
        set(level, pos.offset(6, 0, 5), vanilla);
        set(level, pos.offset(3, 2, 0), vanilla);
        set(level, pos.offset(3, 2, 6), vanilla);
        set(level, pos.offset(0, 2, 3), vanilla);
        set(level, pos.offset(6, 2, 3), vanilla);

        for (int y = 0; y < 4; y++) {
            int metadata = y == 0 || y == 2 ? 1 : y == 3 ? 0 : 2;
            BlockState layer = iceCream(metadata);
            set(level, pos.offset(2, 2 + y, 1), layer);
            set(level, pos.offset(1, 2 + y, 2), layer);
            set(level, pos.offset(4, 2 + y, 1), layer);
            set(level, pos.offset(5, 2 + y, 2), layer);
            set(level, pos.offset(2, 2 + y, 5), layer);
            set(level, pos.offset(1, 2 + y, 4), layer);
            set(level, pos.offset(5, 2 + y, 4), layer);
            set(level, pos.offset(4, 2 + y, 5), layer);

            if (y != 0 && y != 3) {
                BlockState innerLayer = iceCream(y - 1);
                set(level, pos.offset(3, 2 + y, 1), innerLayer);
                set(level, pos.offset(3, 2 + y, 5), innerLayer);
                set(level, pos.offset(1, 2 + y, 3), innerLayer);
                set(level, pos.offset(5, 2 + y, 3), innerLayer);
            }
        }

        for (int dx = 0; dx < 3; dx++) {
            for (int dz = 0; dz < 3; dz++) {
                set(level, pos.offset(2 + dx, 3, 2 + dz), vanilla);
            }
        }

        BlockPos chest = pos.offset(3, 3, 3);
        set(level, chest, Blocks.CHEST.defaultBlockState());
        loot(level, random, chest, ICE_TOWER_LOOT);
        return true;
    }

    private static boolean iceCreamDome(WorldGenLevel level, RandomSource random, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (!below.is(CCBlocks.PUDDING.get()) && !below.is(CCBlocks.FLOUR.get()) && !below.is(CCBlocks.ICE_CREAM.get())) {
            return false;
        }
        BlockState ice = CCBlocks.ICE_CREAM.get().defaultBlockState();
        for (int y = 0; y <= 8; y++) {
            int radius = y < 2 ? 3 : y < 6 ? 2 : 1;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.abs(dx) == radius || Math.abs(dz) == radius || y == 0 || y == 7) {
                        set(level, pos.offset(dx, y, dz), ice);
                    } else {
                        set(level, pos.offset(dx, y, dz), Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        if (random.nextInt(12) == 0) {
            BlockPos chest = pos.above(5);
            set(level, chest, Blocks.CHEST.defaultBlockState());
            loot(level, random, chest, ICE_TOWER_LOOT);
            set(level, pos.above(4), CCBlocks.HONEY_LAMP.get().defaultBlockState());
        }
        return true;
    }

    private static BlockState iceCream(int metadata) {
        return CCBlocks.ICE_CREAM.get().defaultBlockState().setValue(LegacyTypeBlock.TYPE, metadata & 3);
    }

    private static boolean shouldCandyHouseSinkThrough(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir()
            || state.is(CCBlocks.SWEET_GRASS.get())
            || state.is(CCBlocks.CANDY_LEAVES.get())
            || state.is(CCBlocks.CANDY_LEAVES_DARK.get())
            || state.is(CCBlocks.CANDY_LEAVES_LIGHT.get())
            || state.is(CCBlocks.CANDY_LEAVES_CHERRY.get())
            || state.is(CCBlocks.CANDY_LEAVES_ENCHANT.get());
    }

    private static BlockState candyCaneBlock(int metadata) {
        Direction.Axis axis = switch (metadata & 3) {
            case 1 -> Direction.Axis.X;
            case 2 -> Direction.Axis.Z;
            default -> Direction.Axis.Y;
        };
        return CCBlocks.CANDY_CANE_BLOCK.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static boolean waterTemple(WorldGenLevel level, RandomSource random, BlockPos origin) {
        BlockPos floor = oceanFlourFloor(level, origin);
        if (floor == null || !level.getFluidState(floor.above(13)).isSource()) {
            return false;
        }
        BlockPos center = floor.above();
        BlockState stone = CCBlocks.CHOCOLATE_STONE.get().defaultBlockState();
        BlockState cobble = CCBlocks.CHOCOLATE_COBBLESTONE.get().defaultBlockState();
        BlockState glass = CCBlocks.CARAMEL_GLASS_ROUND.get().defaultBlockState();
        BlockState topGlass = CCBlocks.CARAMEL_GLASS_DIAMOND.get().defaultBlockState();
        BlockState lamp = CCBlocks.HONEY_LAMP.get().defaultBlockState();

        int[][] footprint = {
            {0, 0}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1},
            {-2, 0}, {2, 0}, {0, 2}, {0, -2}, {-2, 1}, {-2, -1}, {2, -1}, {2, 1},
            {-1, 2}, {-1, -2}, {1, 2}, {1, -2}, {0, -3}, {0, 3}, {3, 0}, {-3, 0},
            {-3, 1}, {-3, -1}, {3, 1}, {3, -1}, {-1, -3}, {1, -3}, {-1, 3}, {1, 3},
            {2, -2}, {2, 2}, {-2, -2}, {-2, 2}
        };
        for (int y = 0; y <= 3; y++) {
            for (int[] p : footprint) {
                set(level, center.offset(p[0], y, p[1]), Blocks.AIR.defaultBlockState());
            }
        }

        place(level, center, stone, 0, -1, 0, -1, -1, -1, -1, -1, 1, 1, -1, -1, 1, -1, 1);
        place(level, center, cobble, -1, -1, 0, 1, -1, 0, 0, -1, -1, 0, -1, 1,
            -2, -1, 1, -2, -1, -1, 2, -1, -1, 2, -1, 1, -1, -1, 2, -1, -1, -2,
            1, -1, 2, 1, -1, -2, 0, -1, -3, 0, -1, 3, 3, -1, 0, -3, -1, 0);
        place(level, center, stone, -3, -1, 1, -3, -1, -1, 3, -1, 1, 3, -1, -1,
            -1, -1, -3, 1, -1, -3, -1, -1, 3, 1, -1, 3,
            2, -1, -2, 2, -1, 2, -2, -1, -2, -2, -1, 2);
        place(level, center, lamp, -2, -1, 0, 2, -1, 0, 0, -1, 2, 0, -1, -2);
        place(level, center, CCBlocks.FLOUR.get().defaultBlockState(),
            0, -1, 4, 1, -1, 4, -1, -1, 4, 0, -1, -4, 1, -1, -4, -1, -1, -4,
            4, -1, 0, 4, -1, 1, 4, -1, -1, -4, -1, 0, -4, -1, 1, -4, -1, -1,
            2, -1, 3, -2, -1, 3, 2, -1, -3, -2, -1, -3,
            3, -1, -2, 3, -1, 2, -3, -1, -2, -3, -1, 2);

        for (int y = 0; y <= 1; y++) {
            int yy = y * 2;
            place(level, center, cobble,
                0, yy, 4, 1, yy, 4, -1, yy, 4, 0, yy, -4, 1, yy, -4, -1, yy, -4,
                4, yy, 0, 4, yy, 1, 4, yy, -1, -4, yy, 0, -4, yy, 1, -4, yy, -1);
        }
        place(level, center, lamp, 0, 1, 4, 0, 1, -4, 4, 1, 0, -4, 1, 0);
        place(level, center, glass,
            1, 1, 4, -1, 1, 4, 1, 1, -4, -1, 1, -4,
            4, 1, 1, 4, 1, -1, -4, 1, 1, -4, 1, -1);

        place(level, center, cobble,
            2, 0, 3, -2, 0, 3, 2, 0, -3, -2, 0, -3, 3, 0, -2, 3, 0, 2, -3, 0, -2, -3, 0, 2,
            2, 3, 3, -2, 3, 3, 2, 3, -3, -2, 3, -3, 3, 3, -2, 3, 3, 2, -3, 3, -2, -3, 3, 2,
            2, 3, -2, 2, 3, 2, -2, 3, -2, -2, 3, 2);
        for (int y = 1; y <= 2; y++) {
            place(level, center, glass,
                2, y, 3, -2, y, 3, 2, y, -3, -2, y, -3,
                3, y, -2, 3, y, 2, -3, y, -2, -3, y, 2);
        }

        place(level, center, lamp, 3, 3, 0, -3, 3, 0, 0, 3, 3, 0, 3, -3);
        place(level, center, stone,
            3, 3, 1, 3, 3, -1, -3, 3, 1, -3, 3, -1,
            -1, 3, 3, 1, 3, 3, -1, 3, -3, 1, 3, -3);

        place(level, center, topGlass, 0, 4, 0, 0, 4, 1, 0, 4, -1, -1, 4, 0, 1, 4, 0);
        place(level, center, stone,
            0, 4, 2, 0, 4, -2, -2, 4, 0, 2, 4, 0,
            1, 4, 2, -1, 4, 2, 1, 4, -2, -1, 4, -2,
            2, 4, -1, 2, 4, 1, -2, 4, -1, -2, 4, 1,
            1, 4, 1, -1, 4, 1, 1, 4, -1, -1, 4, -1);

        set(level, center, Blocks.CHEST.defaultBlockState());
        loot(level, random, center, CANDY_HOUSE_LOOT);
        return true;
    }

    private static void place(WorldGenLevel level, BlockPos center, BlockState state, int... coordinates) {
        for (int i = 0; i + 2 < coordinates.length; i += 3) {
            set(level, center.offset(coordinates[i], coordinates[i + 1], coordinates[i + 2]), state);
        }
    }

    private static BlockPos oceanFlourFloor(WorldGenLevel level, BlockPos origin) {
        int x = origin.getX();
        int z = origin.getZ();
        for (int y = origin.getY() + 9; y >= 20; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos).is(CCBlocks.FLOUR.get())) {
                return pos;
            }
        }
        return null;
    }

    private static boolean geyser(WorldGenLevel level, RandomSource random, BlockPos origin) {
        BlockPos base = new BlockPos(origin.getX(), 62, origin.getZ());
        if (level.getFluidState(base).isEmpty()) {
            return false;
        }
        int height = 4 + random.nextInt(7);
        for (int y = 0; y <= height; y++) {
            set(level, base.above(y), Blocks.WATER.defaultBlockState());
            if (y == height / 2 && random.nextBoolean()) {
                set(level, base.offset(1, y, 0), Blocks.WATER.defaultBlockState());
                set(level, base.offset(-1, y, 0), Blocks.WATER.defaultBlockState());
                set(level, base.offset(0, y, 1), Blocks.WATER.defaultBlockState());
                set(level, base.offset(0, y, -1), Blocks.WATER.defaultBlockState());
            }
        }
        return true;
    }

    private static boolean chewingGumTotem(WorldGenLevel level, RandomSource random, BlockPos pos) {
        if (!isCandyGround(level.getBlockState(pos.below()))) {
            return false;
        }
        BlockState gum = CCBlocks.CHEWING_GUM_BLOCK.get().defaultBlockState();
        BlockPos center = pos.above(4);
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (Math.abs(dx) == 4 || Math.abs(dz) == 4 || Math.abs(dx) + Math.abs(dz) == 4) {
                    set(level, center.offset(dx, 0, dz), gum);
                    gumPillar(level, center.offset(dx, -1, dz), gum);
                }
            }
        }
        BlockPos spawnerPos = center.below(3);
        set(level, spawnerPos, Blocks.SPAWNER.defaultBlockState());
        if (level.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(CCEntityTypes.BEETLE.get(), random);
        }
        return true;
    }

    private static void gumPillar(WorldGenLevel level, BlockPos pos, BlockState gum) {
        for (int i = 0; i < 20 && (level.isEmptyBlock(pos.below(i)) || !level.getFluidState(pos.below(i)).isEmpty()); i++) {
            set(level, pos.below(i), gum);
        }
    }

    private static boolean floatingIsland(WorldGenLevel level, RandomSource random, BlockPos origin) {
        BlockPos base = origin.offset(-16, 0, -16);
        int nX = random.nextInt(8) - 4;
        int nZ = random.nextInt(8) - 4;
        int[][] lastLayer = new int[32][32];
        lastLayer[16][16] = 2;
        lastLayer[16 + nX][16 + nZ] = 2;
        int maxHeight = random.nextInt(3) + 7;

        for (int y = 0; y < maxHeight; y++) {
            int[][] newLayer = new int[32][32];
            for (int x = 1; x < 31; x++) {
                for (int z = 1; z < 31; z++) {
                    if (lastLayer[x][z] != 2) {
                        continue;
                    }
                    placeFloatingIslandColumn(level, random, base, newLayer, x, y, z, maxHeight);
                    if (random.nextInt(4) < 3 || y == maxHeight - 1) {
                        placeFloatingIslandColumn(level, random, base, newLayer, x + 1, y, z, maxHeight);
                    }
                    if (random.nextInt(4) < 3 || y == maxHeight - 1) {
                        placeFloatingIslandColumn(level, random, base, newLayer, x - 1, y, z, maxHeight);
                    }
                    if (random.nextInt(4) < 3 || y == maxHeight - 1) {
                        placeFloatingIslandColumn(level, random, base, newLayer, x, y, z - 1, maxHeight);
                    }
                    if (random.nextInt(4) < 3 || y == maxHeight - 1) {
                        placeFloatingIslandColumn(level, random, base, newLayer, x, y, z + 1, maxHeight);
                    }
                    if (random.nextInt(4) < 1) {
                        placeFloatingIslandColumn(level, random, base, newLayer, x - 1, y, z - 1, maxHeight);
                    }
                    if (random.nextInt(4) < 1) {
                        placeFloatingIslandColumn(level, random, base, newLayer, x + 1, y, z + 1, maxHeight);
                    }
                    if (random.nextInt(4) < 1) {
                        placeFloatingIslandColumn(level, random, base, newLayer, x + 1, y, z - 1, maxHeight);
                    }
                    if (random.nextInt(4) < 1) {
                        placeFloatingIslandColumn(level, random, base, newLayer, x - 1, y, z + 1, maxHeight);
                    }
                }
            }
            lastLayer = newLayer;
        }

        List<BlockPos> top = new ArrayList<>();
        for (int x = 0; x < 32; x++) {
            for (int z = 0; z < 32; z++) {
                if (lastLayer[x][z] == 2) {
                    top.add(base.offset(x, maxHeight - 1, z));
                }
            }
        }

        int type = random.nextInt(3);
        if (type == 0 || type == 1) {
            decoratePigFeedIsland(level, random, top);
        }
        if (type == 1) {
            BlockPos house = base.offset(14 + random.nextInt(4) - 2, maxHeight - 1, 14 + random.nextInt(4) - 2);
            buildVillageHouse(level, random, house, random.nextInt(4), true);
        }
        if (type == 2) {
            decorateChewingGumIsland(level, random, top);
            spawnBossBeetle(level, base.offset(16, maxHeight + 2, 16));
        }
        return true;
    }

    private static void placeFloatingIslandColumn(WorldGenLevel level, RandomSource random, BlockPos base, int[][] layer,
            int x, int y, int z, int maxHeight) {
        if (x < 0 || x >= 32 || z < 0 || z >= 32) {
            return;
        }
        layer[x][z] = 2;
        set(level, base.offset(x, y, z), floatingIslandBlockForHeight(y, maxHeight, random));
    }

    private static BlockState floatingIslandBlockForHeight(int height, int maxHeight, RandomSource random) {
        int distance = maxHeight - height;
        if (distance == 1) {
            return CCBlocks.PUDDING.get().defaultBlockState();
        }
        if (distance == 2) {
            return CCBlocks.FLOUR.get().defaultBlockState();
        }
        if (distance > 2 && distance <= 6) {
            return random.nextInt(5) < distance
                ? CCBlocks.CHOCOLATE_STONE.get().defaultBlockState()
                : CCBlocks.FLOUR.get().defaultBlockState();
        }
        return CCBlocks.CHOCOLATE_STONE.get().defaultBlockState();
    }

    private static void decoratePigFeedIsland(WorldGenLevel level, RandomSource random, List<BlockPos> top) {
        for (BlockPos pos : top) {
            BlockPos above = pos.above();
            if (random.nextInt(3) == 0) {
                set(level, pos, CCBlocks.CANDY_FARMLAND.get().defaultBlockState());
                set(level, above, CCBlocks.DRAGIBUS_CROPS.get().defaultBlockState().setValue(CropBlock.AGE, 7));
            } else if (level.isEmptyBlock(above) && random.nextBoolean()) {
                set(level, above, randomSweetGrass(random));
            }
        }
    }

    private static void decorateChewingGumIsland(WorldGenLevel level, RandomSource random, List<BlockPos> top) {
        for (BlockPos pos : top) {
            BlockPos above = pos.above();
            if (!level.isEmptyBlock(above)) {
                continue;
            }
            if (random.nextBoolean()) {
                set(level, above, CCBlocks.CHEWING_GUM_PUDDLE.get().defaultBlockState());
            } else if (random.nextInt(3) == 0) {
                set(level, above, randomSweetGrass(random));
            }
        }
    }

    private static void decorateOrdinaryIsland(WorldGenLevel level, RandomSource random, List<BlockPos> top) {
        for (BlockPos pos : top) {
            BlockPos above = pos.above();
            if (level.isEmptyBlock(above) && random.nextInt(3) != 0) {
                set(level, above, randomSweetGrass(random));
            }
        }
    }

    private static BlockState randomSweetGrass(RandomSource random) {
        BlockState state = switch (random.nextInt(3)) {
            case 0 -> CCBlocks.SWEET_GRASS_PINK.get().defaultBlockState();
            case 1 -> CCBlocks.SWEET_GRASS_PALE.get().defaultBlockState();
            default -> CCBlocks.SWEET_GRASS_YELLOW.get().defaultBlockState();
        };
        return state.setValue(LegacyMetadataBlock.Plant.METADATA, random.nextInt(4));
    }

    private static void spawnBossBeetle(WorldGenLevel level, BlockPos pos) {
        if (!(level instanceof WorldGenRegion region)) {
            return;
        }
        BasicCandySpiderEntity entity = CCEntityTypes.BOSS_BEETLE.get().create(region.getLevel());
        if (entity == null) {
            return;
        }
        entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
        entity.finalizeSpawn(region, region.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
        region.addFreshEntity(entity);
    }

    private static boolean undergroundVillage(WorldGenLevel level, RandomSource random, BlockPos origin) {
        if (origin.getY() < 10 || origin.getY() > 48) {
            return false;
        }
        BlockPos base = origin.offset(-32, 0, -32);
        for (int x = 0; x < 64; x++) {
            boolean lamp = (x & 1) == 0;
            for (int z = 0; z < 64; z++) {
                for (int y = 0; y < 7; y++) {
                    BlockPos pos = base.offset(x, y, z);
                    if (y < 2) {
                        set(level, pos, CCSweetscapeBlocks.CRYSTALLIZED_SUGAR.get().defaultBlockState());
                    } else if (x == 0 || z == 0 || x == 63 || z == 63) {
                        set(level, pos, CCBlocks.CHOCOLATE_COBBLESTONE.get().defaultBlockState());
                    } else if ((y == 2 || y == 5) && (x == 1 || z == 1 || x == 62 || z == 62)) {
                        set(level, pos, CCBlocks.CANDY_CANE_BLOCK.get().defaultBlockState());
                    } else if (y == 6) {
                        set(level, pos, lamp ? CCBlocks.HONEY_LAMP.get().defaultBlockState() : CCBlocks.CHOCOLATE_COBBLESTONE.get().defaultBlockState());
                        lamp = !lamp;
                    } else {
                        set(level, pos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        for (int x = 0; x < 64; x++) {
            for (int z = 0; z < 64; z++) {
                BlockState floor = x <= 5 || x >= 58 || z <= 5 || z >= 58
                    ? CCBlocks.MARSHMALLOW_PLANKS.get().defaultBlockState()
                    : x == 6 || x == 57 || z == 6 || z == 57
                    ? CCBlocks.MARSHMALLOW_LOG.get().defaultBlockState()
                    : CCBlocks.PUDDING.get().defaultBlockState();
                set(level, base.offset(x, 1, z), floor);
            }
        }

        for (int x = 6; x < 58; x++) {
            for (int z = 6; z < 58; z++) {
                if (isVillageGate(x, z)) {
                    set(level, base.offset(x, 1, z), CCBlocks.MARSHMALLOW_WORKBENCH.get().defaultBlockState());
                } else if (isVillageRoad(x, z)) {
                    set(level, base.offset(x, 1, z), random.nextBoolean()
                        ? CCBlocks.CHOCOLATE_STONE.get().defaultBlockState()
                        : CCBlocks.CHOCOLATE_COBBLESTONE.get().defaultBlockState());
                } else if (isVillageRoadEdge(x, z)) {
                    set(level, base.offset(x, 1, z), CCBlocks.MARSHMALLOW_LOG.get().defaultBlockState());
                }
            }
        }

        buildVillageHouse(level, random, base.offset(8, 1, 8), random.nextInt(2), false);
        buildVillageHouse(level, random, base.offset(14, 1, 8), 1, true);
        buildVillageHouse(level, random, base.offset(8, 1, 14), 0, true);
        buildVillageHouse(level, random, base.offset(26, 1, 8), 3, random.nextBoolean());
        buildVillageHouse(level, random, base.offset(33, 1, 8), 3, random.nextBoolean());
        buildVillageHouse(level, random, base.offset(51, 1, 8), random.nextInt(2) + 1, false);
        buildVillageHouse(level, random, base.offset(45, 1, 8), 1, true);
        buildVillageHouse(level, random, base.offset(51, 1, 14), 2, true);
        buildVillageHouse(level, random, base.offset(26, 1, 51), 1, random.nextBoolean());
        buildVillageHouse(level, random, base.offset(33, 1, 51), 1, random.nextBoolean());
        buildVillageHouse(level, random, base.offset(51, 1, 51), random.nextInt(2) + 2, false);
        buildVillageHouse(level, random, base.offset(51, 1, 45), 2, true);
        buildVillageHouse(level, random, base.offset(45, 1, 51), 3, true);
        buildVillageHouse(level, random, base.offset(8, 1, 26), 2, random.nextBoolean());
        buildVillageHouse(level, random, base.offset(8, 1, 33), 2, random.nextBoolean());
        buildVillageHouse(level, random, base.offset(8, 1, 51), random.nextInt(2) == 0 ? 3 : 0, false);
        buildVillageHouse(level, random, base.offset(8, 1, 45), 0, true);
        buildVillageHouse(level, random, base.offset(14, 1, 51), 3, true);
        buildVillageHouse(level, random, base.offset(51, 1, 26), 0, random.nextBoolean());
        buildVillageHouse(level, random, base.offset(51, 1, 33), 0, random.nextBoolean());

        for (int i = 0; i < 6; i++) {
            set(level, base.offset(26, 2, 50 - i), CCBlocks.CANDY_CANE_FENCE.get().defaultBlockState());
            set(level, base.offset(37, 2, 50 - i), CCBlocks.CANDY_CANE_FENCE.get().defaultBlockState());
            set(level, base.offset(26, 2, 13 + i), CCBlocks.CANDY_CANE_FENCE.get().defaultBlockState());
            set(level, base.offset(37, 2, 13 + i), CCBlocks.CANDY_CANE_FENCE.get().defaultBlockState());
            set(level, base.offset(50 - i, 2, 26), CCBlocks.CANDY_CANE_FENCE.get().defaultBlockState());
            set(level, base.offset(50 - i, 2, 37), CCBlocks.CANDY_CANE_FENCE.get().defaultBlockState());
            set(level, base.offset(13 + i, 2, 26), CCBlocks.CANDY_CANE_FENCE.get().defaultBlockState());
            set(level, base.offset(13 + i, 2, 37), CCBlocks.CANDY_CANE_FENCE.get().defaultBlockState());
        }
        int[][] fencePosts = {{18, 27}, {18, 36}, {45, 27}, {45, 36}, {27, 18}, {36, 18}, {27, 45}, {36, 45}};
        for (int[] post : fencePosts) {
            set(level, base.offset(post[0], 2, post[1]), CCBlocks.CANDY_CANE_FENCE.get().defaultBlockState());
        }

        buildVillageCenter(level, random, base);
        decorateVillageLeaves(level, random, base);
        scatterVillageSweetGrass(level, random, base);
        spawnBossSuguard(level, base.offset(32, 3, 32));
        callHoneyEmblemPlayers(level, origin);
        return true;
    }

    private static void spawnUndergroundVillageGingerbread(WorldGenLevel level, RandomSource random, BlockPos base) {
        int count = 3 + random.nextInt(4);
        for (int i = 0; i < count; i++) {
            BlockPos pos = base.offset(8 + random.nextInt(48), 2, 8 + random.nextInt(48));
            if (!level.getBlockState(pos).isAir()) {
                pos = pos.above();
            }
            spawnGingerbread(level, pos, random.nextInt(3));
        }
    }

    private static void spawnBossSuguard(WorldGenLevel level, BlockPos pos) {
        if (!(level instanceof WorldGenRegion region)) {
            return;
        }
        BasicCandyZombieEntity entity = CCEntityTypes.BOSS_SUGUARD.get().create(region.getLevel());
        if (entity == null) {
            return;
        }
        entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
        entity.finalizeSpawn(region, region.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
        region.addFreshEntity(entity);
    }

    private static boolean isVillageGate(int x, int z) {
        return (x == 6 || x == 57) && ((z > 20 && z < 24) || (z > 39 && z < 43))
            || (z == 6 || z == 57) && ((x > 20 && x < 24) || (x > 39 && x < 43));
    }

    private static boolean isVillageRoad(int x, int z) {
        return (x >= 21 && x <= 23) || (x >= 40 && x <= 42) || (z >= 21 && z <= 23) || (z >= 40 && z <= 42);
    }

    private static boolean isVillageRoadEdge(int x, int z) {
        return x == 20 || x == 24 || x == 39 || x == 43 || z == 20 || z == 24 || z == 39 || z == 43;
    }

    private static void buildVillageCenter(WorldGenLevel level, RandomSource random, BlockPos base) {
        int[][] pudding = {
            {31, 31}, {32, 31}, {31, 32}, {32, 32}, {30, 31}, {30, 32}, {33, 31}, {33, 32},
            {31, 30}, {32, 30}, {31, 33}, {32, 33}
        };
        for (int[] p : pudding) {
            set(level, base.offset(p[0], 2, p[1]), CCBlocks.PUDDING.get().defaultBlockState());
        }
        int[][] slabs = {
            {30, 30}, {33, 30}, {30, 33}, {33, 33}, {34, 33}, {33, 34}, {30, 29}, {29, 30},
            {33, 29}, {34, 30}, {29, 33}, {30, 34}, {31, 29}, {32, 29}, {31, 34}, {32, 34},
            {29, 31}, {29, 32}, {34, 31}, {34, 32}
        };
        for (int[] p : slabs) {
            set(level, base.offset(p[0], 2, p[1]), CCBlocks.MARSHMALLOW_SLAB.get().defaultBlockState());
        }
    }

    private static void decorateVillageLeaves(WorldGenLevel level, RandomSource random, BlockPos base) {
        int meta = random.nextInt(3);
        int meta2 = random.nextInt(3);
        int meta3 = random.nextInt(3);
        int meta4 = random.nextInt(3);
        placeLeafL(level, base, 14, 14, 1, 1, meta);
        placeLeafL(level, base, 49, 14, -1, 1, meta);
        placeLeafL(level, base, 49, 49, -1, -1, meta);
        placeLeafL(level, base, 14, 49, 1, -1, meta);
        placeLeafL(level, base, 16, 16, 1, 1, meta2);
        placeLeafL(level, base, 16, 47, 1, -1, meta2);
        placeLeafL(level, base, 47, 47, -1, -1, meta2);
        placeLeafL(level, base, 47, 16, -1, 1, meta2);

        set(level, base.offset(18, 2, 18), leafState(meta3));
        set(level, base.offset(18, 2, 45), leafState(meta3));
        set(level, base.offset(45, 2, 18), leafState(meta3));
        set(level, base.offset(45, 2, 45), leafState(meta3));
        placeLeafCorner(level, base, 26, 26, 1, 1, meta4);
        placeLeafCorner(level, base, 37, 26, -1, 1, meta4);
        placeLeafCorner(level, base, 37, 37, -1, -1, meta4);
        placeLeafCorner(level, base, 26, 37, 1, -1, meta4);
    }

    private static void placeLeafL(WorldGenLevel level, BlockPos base, int x, int z, int dx, int dz, int metadata) {
        for (int i = 0; i < 5; i++) {
            set(level, base.offset(x + dx * i, 2, z), leafState(metadata));
            set(level, base.offset(x, 2, z + dz * i), leafState(metadata));
        }
    }

    private static void placeLeafCorner(WorldGenLevel level, BlockPos base, int x, int z, int dx, int dz, int metadata) {
        set(level, base.offset(x, 2, z), leafState(metadata));
        set(level, base.offset(x + dx, 2, z), leafState(metadata));
        set(level, base.offset(x + 2 * dx, 2, z), leafState(metadata));
        set(level, base.offset(x, 2, z + dz), leafState(metadata));
        set(level, base.offset(x, 2, z + 2 * dz), leafState(metadata));
    }

    private static BlockState leafState(int metadata) {
        BlockState state = switch (metadata % 3) {
            case 1 -> CCBlocks.CANDY_LEAVES_DARK.get().defaultBlockState();
            case 2 -> CCBlocks.CANDY_LEAVES_LIGHT.get().defaultBlockState();
            default -> CCBlocks.CANDY_LEAVES.get().defaultBlockState();
        };
        return state
            .setValue(LegacyLeavesBlock.CHECK_DECAY, false)
            .setValue(LegacyLeavesBlock.DECAYABLE, false);
    }

    private static void scatterVillageSweetGrass(WorldGenLevel level, RandomSource random, BlockPos base) {
        for (int x = 0; x < 64; x++) {
            for (int z = 0; z < 64; z++) {
                BlockPos pos = base.offset(x, 2, z);
                if (level.isEmptyBlock(pos) && random.nextInt(3) == 0) {
                    set(level, pos, CCBlocks.SWEET_GRASS.get().defaultBlockState()
                        .setValue(LegacyMetadataBlock.Plant.METADATA, random.nextInt(4)));
                }
            }
        }
    }

    private static void buildVillageHouse(WorldGenLevel level, RandomSource random, BlockPos base, int side, boolean window) {
        int metadata = random.nextInt(3);
        BlockState planks = marshmallowPlanks(metadata);
        BlockState logs = marshmallowLog(metadata, Direction.Axis.Y);
        BlockState logX = marshmallowLog(metadata, Direction.Axis.X);
        BlockState logZ = marshmallowLog(metadata, Direction.Axis.Z);
        BlockState slab = marshmallowSlab(metadata);
        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                set(level, base.offset(dx, 0, dz), CCBlocks.CHOCOLATE_STONE.get().defaultBlockState());
                set(level, base.offset(dx, 3, dz), planks);
            }
        }
        for (int y = 1; y <= 2; y++) {
            for (int dx = 0; dx < 5; dx++) {
                for (int dz = 0; dz < 5; dz++) {
                    boolean corner = (dx == 0 || dx == 4) && (dz == 0 || dz == 4);
                    boolean edge = dx == 0 || dx == 4 || dz == 0 || dz == 4;
                    set(level, base.offset(dx, y, dz), corner ? logs : edge ? planks : Blocks.AIR.defaultBlockState());
                }
            }
        }
        for (int dx = 1; dx <= 3; dx++) {
            set(level, base.offset(dx, 3, 0), logX);
            set(level, base.offset(dx, 3, 4), logX);
        }
        for (int dz = 1; dz <= 3; dz++) {
            set(level, base.offset(0, 3, dz), logZ);
            set(level, base.offset(4, 3, dz), logZ);
        }

        if (window) {
            BlockPos glass = houseWindowPos(base, side, random.nextInt(3));
            set(level, glass, random.nextInt(3) == 0
                ? CCBlocks.CARAMEL_PANE.get().defaultBlockState()
                : random.nextBoolean() ? CCBlocks.CARAMEL_PANE_ROUND.get().defaultBlockState() : CCBlocks.CARAMEL_PANE_DIAMOND.get().defaultBlockState());
        }
        BlockPos door = houseWallPos(base, side, random.nextInt(3));
        set(level, door, Blocks.AIR.defaultBlockState());
        set(level, door.above(), slab);
        set(level, base.offset(0, 3, 0), Blocks.AIR.defaultBlockState());
        set(level, base.offset(4, 3, 0), Blocks.AIR.defaultBlockState());
        set(level, base.offset(4, 3, 4), Blocks.AIR.defaultBlockState());
        set(level, base.offset(0, 3, 4), Blocks.AIR.defaultBlockState());
        spawnGingerbread(level, base.offset(2, 2, 2), base.getY() > 100 ? GingerbreadManEntity.ELDER : -1);
    }

    private static BlockPos houseWallPos(BlockPos base, int side, int offset) {
        int direction = side & 3;
        if (direction == 0) {
            return base.offset(0, 1, 1 + offset);
        }
        if (direction == 1) {
            return base.offset(1 + offset, 1, 0);
        }
        if (direction == 2) {
            return base.offset(4, 1, 1 + offset);
        }
        return base.offset(1 + offset, 1, 4);
    }

    private static BlockPos houseWindowPos(BlockPos base, int side, int offset) {
        int direction = side & 3;
        if (direction == 0) {
            return base.offset(4, 2, 1 + offset);
        }
        if (direction == 1) {
            return base.offset(1 + offset, 2, 4);
        }
        if (direction == 2) {
            return base.offset(0, 2, 1 + offset);
        }
        return base.offset(1 + offset, 2, 0);
    }

    private static BlockState marshmallowPlanks(int metadata) {
        return CCBlocks.MARSHMALLOW_PLANKS.get().defaultBlockState()
            .setValue(LegacyMetadataBlock.METADATA, metadata & 3);
    }

    private static BlockState marshmallowLog(int metadata, Direction.Axis axis) {
        return CCBlocks.MARSHMALLOW_LOG.get().defaultBlockState()
            .setValue(LegacyLogBlock.METADATA, metadata % 3)
            .setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static BlockState marshmallowSlab(int metadata) {
        return switch (metadata % 3) {
            case 1 -> CCBlocks.DARK_MARSHMALLOW_SLAB.get().defaultBlockState();
            case 2 -> CCBlocks.LIGHT_MARSHMALLOW_SLAB.get().defaultBlockState();
            default -> CCBlocks.MARSHMALLOW_SLAB.get().defaultBlockState();
        };
    }

    private static void buildSmallHouse(WorldGenLevel level, BlockPos base, RandomSource random, boolean chest) {
        BlockState planks = CCBlocks.MARSHMALLOW_PLANKS.get().defaultBlockState();
        BlockState logs = CCBlocks.MARSHMALLOW_LOG.get().defaultBlockState();
        BlockState wall = CCBlocks.CANDY_CANE_BLOCK.get().defaultBlockState();
        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                set(level, base.offset(dx, 0, dz), CCBlocks.CHOCOLATE_STONE.get().defaultBlockState());
                set(level, base.offset(dx, 3, dz), planks);
            }
        }
        for (int y = 1; y <= 2; y++) {
            for (int dx = 0; dx < 5; dx++) {
                for (int dz = 0; dz < 5; dz++) {
                    boolean corner = (dx == 0 || dx == 4) && (dz == 0 || dz == 4);
                    boolean edge = dx == 0 || dx == 4 || dz == 0 || dz == 4;
                    if (corner) {
                        set(level, base.offset(dx, y, dz), logs);
                    } else if (edge) {
                        set(level, base.offset(dx, y, dz), wall);
                    } else {
                        set(level, base.offset(dx, y, dz), Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        set(level, base.offset(2, 1, 0), Blocks.AIR.defaultBlockState());
        set(level, base.offset(2, 2, 0), Blocks.AIR.defaultBlockState());
        if (chest) {
            set(level, base.offset(2, 1, 2), Blocks.CHEST.defaultBlockState());
            loot(level, random, base.offset(2, 1, 2), CANDY_HOUSE_LOOT);
        }
    }

    private static void spawnGingerbread(WorldGenLevel level, BlockPos pos) {
        spawnGingerbread(level, pos, -1);
    }

    private static void spawnGingerbread(WorldGenLevel level, BlockPos pos, int profession) {
        if (!ENABLE_STRUCTURE_GINGERBREAD) {
            return;
        }
        if (!(level instanceof WorldGenRegion region)) {
            return;
        }
        GingerbreadManEntity entity = CCEntityTypes.GINGERBREAD_MAN.get().create(region.getLevel());
        if (entity == null) {
            return;
        }
        entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
        entity.finalizeSpawn(region, region.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
        if (profession >= 0) {
            entity.setGingerProfession(profession);
        }
        region.addFreshEntity(entity);
    }

    private static void callHoneyEmblemPlayers(WorldGenLevel level, BlockPos pos) {
        if (!(level instanceof WorldGenRegion region)) {
            return;
        }
        region.getLevel().players().forEach(player -> {
            if (EmblemHelper.has(player, CCItems.HONEY_EMBLEM.get())) {
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.candycraftmod.honey_emblem_found",
                    pos.getX(), pos.getY(), pos.getZ()
                ), false);
            }
        });
    }

    private static void loot(WorldGenLevel level, RandomSource random, BlockPos pos, ResourceLocation table) {
        if (level.getBlockEntity(pos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(table, random.nextLong());
        }
    }

    private static void clear(WorldGenLevel level, BlockPos min, BlockPos max) {
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            set(level, pos, Blocks.AIR.defaultBlockState());
        }
    }

    private static boolean isCandyGround(BlockState state) {
        return state.is(CCBlocks.PUDDING.get()) || state.is(CCBlocks.FLOUR.get()) || state.is(CCBlocks.CANDY_FARMLAND.get());
    }

    private static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        if (!level.isOutsideBuildHeight(pos)) {
            level.setBlock(pos, state, 2 | 16);
        }
    }

    public enum Kind {
        CANDY_HOUSE,
        ICE_TOWER,
        ICE_CREAM_DOME,
        WATER_TEMPLE,
        GEYSER,
        CHEWING_GUM_TOTEM,
        FLOATING_ISLAND,
        UNDERGROUND_VILLAGE
    }
}
