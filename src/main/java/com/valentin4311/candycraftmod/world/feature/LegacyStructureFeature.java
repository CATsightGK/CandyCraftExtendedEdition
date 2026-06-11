package com.valentin4311.candycraftmod.world.feature;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.GingerbreadManEntity;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class LegacyStructureFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation CANDY_HOUSE_LOOT = new ResourceLocation(CandyCraft.MODID, "chests/candy_house");
    private static final ResourceLocation ICE_TOWER_LOOT = new ResourceLocation(CandyCraft.MODID, "chests/ice_tower");
    private static final ResourceLocation WATER_TEMPLE_LOOT = new ResourceLocation(CandyCraft.MODID, "chests/water_temple");
    private static final boolean ENABLE_STRUCTURE_GINGERBREAD = false;
    private final Kind kind;

    public LegacyStructureFeature(Codec<NoneFeatureConfiguration> codec, Kind kind) {
        super(codec);
        this.kind = kind;
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return switch (kind) {
            case CANDY_HOUSE -> candyHouse(context.level(), context.random(), surface(context.level(), context.origin()));
            case ICE_TOWER -> iceTower(context.level(), context.random(), surface(context.level(), context.origin()));
            case WATER_TEMPLE -> waterTemple(context.level(), context.random(), context.origin());
            case GEYSER -> geyser(context.level(), context.random(), context.origin());
            case CHEWING_GUM_TOTEM -> chewingGumTotem(context.level(), context.random(), surface(context.level(), context.origin()));
            case FLOATING_ISLAND -> floatingIsland(context.level(), context.random(), context.origin());
            case UNDERGROUND_VILLAGE -> undergroundVillage(context.level(), context.random(), context.origin());
        };
    }

    private static BlockPos surface(WorldGenLevel level, BlockPos origin) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ());
        return new BlockPos(origin.getX(), y, origin.getZ());
    }

    private static boolean candyHouse(WorldGenLevel level, RandomSource random, BlockPos pos) {
        if (!isCandyGround(level.getBlockState(pos.below()))) {
            return false;
        }
        BlockPos base = pos.offset(-2, 0, -2);
        clear(level, base.offset(-1, 0, -1), base.offset(5, 5, 5));
        buildSmallHouse(level, base, random, true);
        set(level, base.offset(2, 0, 2), Blocks.CHEST.defaultBlockState());
        loot(level, random, base.offset(2, 0, 2), CANDY_HOUSE_LOOT);
        set(level, base.offset(2, -1, 2), CCBlocks.HONEY_LAMP.get().defaultBlockState());
        return true;
    }

    private static boolean iceTower(WorldGenLevel level, RandomSource random, BlockPos pos) {
        if (!level.getBlockState(pos.below()).is(CCBlocks.PUDDING.get()) && !level.getBlockState(pos.below()).is(CCBlocks.FLOUR.get())) {
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
        set(level, pos.above(5), Blocks.CHEST.defaultBlockState());
        loot(level, random, pos.above(5), ICE_TOWER_LOOT);
        set(level, pos.above(4), CCBlocks.HONEY_LAMP.get().defaultBlockState());
        return true;
    }

    private static boolean waterTemple(WorldGenLevel level, RandomSource random, BlockPos origin) {
        BlockPos floor = oceanFloor(level, origin);
        if (floor == null || !level.getFluidState(floor.above(4)).isSource()) {
            return false;
        }
        BlockPos center = floor.above();
        BlockState stone = CCBlocks.CHOCOLATE_STONE.get().defaultBlockState();
        BlockState cobble = CCBlocks.CHOCOLATE_COBBLESTONE.get().defaultBlockState();
        BlockState glass = CCBlocks.CARAMEL_GLASS_ROUND.get().defaultBlockState();
        BlockState lamp = CCBlocks.HONEY_LAMP.get().defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                int dist = Math.abs(dx) + Math.abs(dz);
                if (dist <= 5) {
                    set(level, center.offset(dx, -1, dz), dist % 2 == 0 ? stone : cobble);
                    for (int y = 0; y <= 3; y++) {
                        boolean edge = dist == 5 || Math.abs(dx) == 4 || Math.abs(dz) == 4;
                        if (edge) {
                            boolean window = y == 1 && dist >= 4;
                            set(level, center.offset(dx, y, dz), window ? glass : cobble);
                        } else {
                            set(level, center.offset(dx, y, dz), water);
                        }
                    }
                    if (dist <= 4) {
                        set(level, center.offset(dx, 4, dz), dist <= 1 ? glass : stone);
                    }
                }
            }
        }
        for (int d = -4; d <= 4; d += 8) {
            set(level, center.offset(d, 1, 0), lamp);
            set(level, center.offset(0, 1, d), lamp);
        }
        set(level, center.offset(0, 4, 0), lamp);
        set(level, center, Blocks.CHEST.defaultBlockState());
        loot(level, random, center, WATER_TEMPLE_LOOT);
        return true;
    }

    private static BlockPos oceanFloor(WorldGenLevel level, BlockPos origin) {
        int x = origin.getX();
        int z = origin.getZ();
        for (int y = 62; y > level.getMinBuildHeight() + 8; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!level.getBlockState(pos).isAir() && !level.getFluidState(pos).isSource()) {
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
        return true;
    }

    private static void gumPillar(WorldGenLevel level, BlockPos pos, BlockState gum) {
        for (int i = 0; i < 20 && (level.isEmptyBlock(pos.below(i)) || !level.getFluidState(pos.below(i)).isEmpty()); i++) {
            set(level, pos.below(i), gum);
        }
    }

    private static boolean floatingIsland(WorldGenLevel level, RandomSource random, BlockPos origin) {
        BlockPos center = origin.offset(-8, 0, -8);
        int radius = 7 + random.nextInt(4);
        int height = 5 + random.nextInt(3);
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > radius + random.nextDouble()) {
                    continue;
                }
                int column = Math.max(1, height - (int)(dist * 0.6D));
                for (int y = -column; y <= 0; y++) {
                    BlockState state = y == 0 ? CCBlocks.PUDDING.get().defaultBlockState()
                        : y > -2 ? CCBlocks.FLOUR.get().defaultBlockState()
                        : CCBlocks.CHOCOLATE_STONE.get().defaultBlockState();
                    set(level, center.offset(dx, y, dz), state);
                }
                if (random.nextInt(4) == 0) {
                    set(level, center.offset(dx, 1, dz), random.nextBoolean()
                        ? CCBlocks.SWEET_GRASS.get().defaultBlockState()
                        : CCBlocks.DRAGIBUS_CROPS.get().defaultBlockState());
                }
            }
        }
        if (random.nextBoolean()) {
            buildSmallHouse(level, center.offset(-2, 1, -2), random, true);
            spawnGingerbread(level, center.offset(0, 2, 0));
        }
        return true;
    }

    private static boolean undergroundVillage(WorldGenLevel level, RandomSource random, BlockPos origin) {
        if (origin.getY() < 10 || origin.getY() > 48) {
            return false;
        }
        BlockPos base = origin.offset(-24, 0, -24);
        for (int x = 0; x < 48; x++) {
            for (int z = 0; z < 48; z++) {
                for (int y = 0; y < 7; y++) {
                    BlockPos pos = base.offset(x, y, z);
                    boolean border = x == 0 || z == 0 || x == 47 || z == 47;
                    boolean ceiling = y == 6;
                    boolean floor = y == 0;
                    if (floor) {
                        set(level, pos, x < 5 || z < 5 || x > 42 || z > 42
                            ? CCBlocks.MARSHMALLOW_PLANKS.get().defaultBlockState()
                            : CCBlocks.PUDDING.get().defaultBlockState());
                    } else if (border || ceiling) {
                        set(level, pos, ceiling && (x + z) % 9 == 0
                            ? CCBlocks.HONEY_LAMP.get().defaultBlockState()
                            : CCBlocks.CHOCOLATE_COBBLESTONE.get().defaultBlockState());
                    } else {
                        set(level, pos, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        for (int i = 8; i <= 36; i += 14) {
            buildSmallHouse(level, base.offset(i, 1, 8), random, false);
            buildSmallHouse(level, base.offset(8, 1, i), random, false);
            buildSmallHouse(level, base.offset(36, 1, i), random, false);
            buildSmallHouse(level, base.offset(i, 1, 36), random, false);
        }
        for (int i = 0; i < 10; i++) {
            spawnGingerbread(level, base.offset(8 + random.nextInt(32), 2, 8 + random.nextInt(32)));
        }
        callHoneyEmblemPlayers(level, origin);
        return true;
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
        region.addFreshEntity(entity);
    }

    private static void callHoneyEmblemPlayers(WorldGenLevel level, BlockPos pos) {
        if (!(level instanceof WorldGenRegion region)) {
            return;
        }
        region.getLevel().players().forEach(player -> {
            if (player.getInventory().contains(CCItems.HONEY_EMBLEM.get().getDefaultInstance())) {
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
        WATER_TEMPLE,
        GEYSER,
        CHEWING_GUM_TOTEM,
        FLOATING_ISLAND,
        UNDERGROUND_VILLAGE
    }
}
