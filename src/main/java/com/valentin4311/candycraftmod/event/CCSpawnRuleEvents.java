package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import com.valentin4311.candycraftmod.entity.CandyFishEntity;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.world.CCDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Natural-spawn whitelist for the candy dimensions plus the spawn placement
 * predicates registered on the MOD bus.
 */
@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CCSpawnRuleEvents {
    private CCSpawnRuleEvents() {
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getSpawnType() == null) {
            return;
        }
        if (isDungeonLevel(event.getLevel().getLevel()) && isNaturalWorldSpawn(event.getSpawnType())) {
            event.setSpawnCancelled(true);
            return;
        }
        if (isNaturalWorldSpawn(event.getSpawnType())) {
            if (isCandyWorld(event.getLevel().getLevel())) {
                if (!canNaturalCandyMobSpawn(event)) {
                    event.setSpawnCancelled(true);
                }
                return;
            }
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
            if (id != null && CandyCraft.MODID.equals(id.getNamespace())) {
                event.setSpawnCancelled(true);
            }
        }
    }

    private static boolean canNaturalCandyMobSpawn(MobSpawnEvent.FinalizeSpawn event) {
        EntityType<?> type = event.getEntity().getType();
        if (type != CCEntityTypes.CANDY_PIG.get() && type != CCEntityTypes.WAFFLE_SHEEP.get()
            && type != CCEntityTypes.SUGUARD.get() && type != CCEntityTypes.PINGOUIN.get()
            && type != CCEntityTypes.GUMMY_BUNNY.get() && type != CCEntityTypes.CANDY_WOLF.get()
            && type != CCEntityTypes.COTTON_CANDY_SHEEP.get() && type != CCEntityTypes.EASTER_CHICKEN.get()
            && type != CCEntityTypes.GUMMY_MOUSE.get() && type != CCEntityTypes.GUMMY_BEAR.get()
            && type != CCEntityTypes.JELLY_QUEEN.get() && type != CCEntityTypes.CARAMEL_BEE.get()
            && type != CCEntityTypes.BOSS_BEETLE.get()
            && type != CCEntityTypes.CANDY_CREEPER.get() && type != CCEntityTypes.COTTON_CANDY_SPIDER.get()
            && type != CCEntityTypes.MAGE_SUGUARD.get() && type != CCEntityTypes.CANDY_FISH.get()
            && type != CCEntityTypes.NESSIE.get()) {
            return false;
        }
        ResourceLocation biomeId = event.getLevel().getBiome(event.getEntity().blockPosition())
            .unwrapKey()
            .map(key -> key.location())
            .orElse(null);
        if (biomeId == null || !CandyCraft.MODID.equals(biomeId.getNamespace())) {
            return false;
        }
        String path = biomeId.getPath();
        if (type == CCEntityTypes.SUGUARD.get()) {
            if (event.getEntity() instanceof BasicCandyZombieEntity suguard) {
                suguard.setChocolateForestSuguard("chocolate_forest".equals(path));
            }
            if ("sugar_enchanted_forest".equals(path) || "caramel_forest".equals(path)) {
                return true;
            }
            int brightness = event.getLevel().getLevel().getMaxLocalRawBrightness(event.getEntity().blockPosition());
            return switch (path) {
                case "sugar_plains", "hard_candy_plains", "sugar_forest", "sugar_cold_forest", "sugar_mountains", "ice_cream_plains", "ice_cream_sky_mountains", "sugar_hell_mountains",
                    "cotton_candy_plains", "chocolate_forest", "gummy_swamp", "sugar_oceans", "sugar_river", "candycraft_dungeon" -> brightness <= 7;
                default -> false;
            };
        }
        if (type == CCEntityTypes.MAGE_SUGUARD.get()) {
            int brightness = event.getLevel().getLevel().getMaxLocalRawBrightness(event.getEntity().blockPosition());
            return "sugar_enchanted_forest".equals(path) && brightness <= 7;
        }
        if (type == CCEntityTypes.CANDY_CREEPER.get() || type == CCEntityTypes.COTTON_CANDY_SPIDER.get()) {
            int brightness = event.getLevel().getLevel().getMaxLocalRawBrightness(event.getEntity().blockPosition());
            return brightness <= 7;
        }
        if (type == CCEntityTypes.CARAMEL_BEE.get()) {
            if ("gummy_swamp".equals(path)) {
                return false;
            }
            int brightness = event.getLevel().getLevel().getMaxLocalRawBrightness(event.getEntity().blockPosition());
            return brightness <= 7;
        }
        if (type == CCEntityTypes.CANDY_FISH.get()) {
            return "sugar_oceans".equals(path) || "sugar_river".equals(path);
        }
        if (type == CCEntityTypes.NESSIE.get()) {
            return "sugar_oceans".equals(path);
        }
        if (type == CCEntityTypes.BOSS_BEETLE.get()) {
            return event.getSpawnType() == MobSpawnType.STRUCTURE;
        }
        if (type == CCEntityTypes.PINGOUIN.get()) {
            return "ice_cream_plains".equals(path) || "ice_cream_sky_mountains".equals(path);
        }
        if (type == CCEntityTypes.COTTON_CANDY_SHEEP.get()) {
            return isCottonCandySheepBiome(path);
        }
        if (type == CCEntityTypes.EASTER_CHICKEN.get()) {
            return "chocolate_forest".equals(path);
        }
        if (type == CCEntityTypes.GUMMY_MOUSE.get() || type == CCEntityTypes.GUMMY_BEAR.get()) {
            return "gummy_swamp".equals(path);
        }
        if (type == CCEntityTypes.CANDY_WOLF.get()) {
            return "caramel_forest".equals(path) || "sugar_cold_forest".equals(path);
        }
        if (type == CCEntityTypes.JELLY_QUEEN.get()) {
            return event.getSpawnType() == MobSpawnType.CHUNK_GENERATION;
        }
        if (type == CCEntityTypes.CANDY_PIG.get()) {
            return isCandyPigBiome(path);
        }
        if (type == CCEntityTypes.GUMMY_BUNNY.get()) {
            return isGummyBunnyBiome(path);
        }
        if (type == CCEntityTypes.WAFFLE_SHEEP.get()) {
            return isWaffleSheepBiome(path);
        }
        return switch (path) {
            case "sugar_plains", "hard_candy_plains", "sugar_forest", "sugar_cold_forest", "sugar_enchanted_forest", "sugar_mountains", "ice_cream_plains", "ice_cream_sky_mountains", "caramel_forest" -> true;
            default -> false;
        };
    }

    private static boolean isCandyPigBiome(String path) {
        return switch (path) {
            case "sugar_plains", "hard_candy_plains", "sugar_forest", "sugar_cold_forest", "sugar_enchanted_forest",
                "sugar_mountains", "caramel_forest" -> true;
            default -> false;
        };
    }

    private static boolean isCottonCandySheepBiome(String path) {
        return switch (path) {
            case "cotton_candy_plains", "sugar_plains", "hard_candy_plains", "sugar_forest", "sugar_cold_forest",
                "sugar_enchanted_forest", "sugar_mountains", "sugar_hell_mountains",
                "ice_cream_plains", "ice_cream_sky_mountains", "caramel_forest",
                "chocolate_forest", "gummy_swamp" -> true;
            default -> false;
        };
    }

    private static boolean isGummyBunnyBiome(String path) {
        return switch (path) {
            case "sugar_plains", "hard_candy_plains", "sugar_forest", "sugar_cold_forest", "sugar_enchanted_forest",
                "sugar_mountains", "caramel_forest", "chocolate_forest", "gummy_swamp" -> true;
            default -> false;
        };
    }

    private static boolean isWaffleSheepBiome(String path) {
        return switch (path) {
            case "sugar_plains", "hard_candy_plains", "sugar_cold_forest", "sugar_mountains" -> true;
            default -> false;
        };
    }

    private static boolean isNaturalWorldSpawn(MobSpawnType spawnType) {
        return spawnType == MobSpawnType.NATURAL
            || spawnType == MobSpawnType.CHUNK_GENERATION
            || spawnType == MobSpawnType.PATROL
            || spawnType == MobSpawnType.REINFORCEMENT
            || spawnType == MobSpawnType.STRUCTURE;
    }

    private static boolean isDungeonLevel(Level level) {
        return CCDimensions.isDungeon(level);
    }

    private static boolean isCandyWorld(Level level) {
        return CCDimensions.isCandyWorld(level);
    }

    @Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        private ModEvents() {
        }

        @SubscribeEvent
        public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
            event.register(CCEntityTypes.CANDY_PIG.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCSpawnRuleEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.WAFFLE_SHEEP.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCSpawnRuleEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.CANDY_WOLF.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCSpawnRuleEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            // Gummy terrain is 15.92/16 blocks tall, so vanilla ON_GROUND rejects it
            // before our candy-surface predicate can run.
            event.register(CCEntityTypes.GUMMY_BUNNY.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCSpawnRuleEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.COTTON_CANDY_SHEEP.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.valentin4311.candycraftmod.entity.CottonCandySheepEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.EASTER_CHICKEN.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.valentin4311.candycraftmod.entity.EasterChickenEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.GUMMY_MOUSE.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.valentin4311.candycraftmod.entity.GummyMouseEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.GUMMY_BEAR.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.valentin4311.candycraftmod.entity.GummyBearEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.PINGOUIN.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCSpawnRuleEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.SUGUARD.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCSpawnRuleEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.MAGE_SUGUARD.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCSpawnRuleEvents::canHostileCandyMobSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.CANDY_CREEPER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCSpawnRuleEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.COTTON_CANDY_SPIDER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCSpawnRuleEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.JELLY_QUEEN.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCSpawnRuleEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.NOUGAT_GOLEM.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCSpawnRuleEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.BEETLE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCSpawnRuleEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.CARAMEL_BEE.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCSpawnRuleEvents::canCaramelBeeSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.CANDY_FISH.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CandyFishEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.NESSIE.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCSpawnRuleEvents::canNessieSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
        }
    }

    private static boolean canSpawnOnCandySurface(EntityType<? extends Mob> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        if (isCandyLeafSurface(below)) {
            return false;
        }
        if (isCandySpawnSurface(below)) {
            BlockState state = level.getBlockState(pos);
            BlockState above = level.getBlockState(pos.above());
            return state.getCollisionShape(level, pos).isEmpty()
                && above.getCollisionShape(level, pos.above()).isEmpty()
                && level.getFluidState(pos).isEmpty()
                && level.getFluidState(pos.above()).isEmpty();
        }
        return below.isValidSpawn(level, pos.below(), type);
    }

    private static boolean canHostileCandyMobSpawn(EntityType<? extends Mob> type, LevelAccessor level,
            MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getLevelData().getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
            && level.getMaxLocalRawBrightness(pos) <= 7
            && canSpawnOnCandySurface(type, level, reason, pos, random);
    }

    private static boolean canCaramelBeeSpawn(EntityType<? extends Mob> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getLevelData().getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
            && level.getBlockState(pos).isAir()
            && !level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER);
    }

    private static boolean canNessieSpawn(EntityType<? extends Mob> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        if (pos.getY() <= 45 || pos.getY() >= 63 || !level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER)) {
            return false;
        }
        if (level instanceof ServerLevel serverLevel) {
            int nearby = serverLevel.getEntitiesOfClass(com.valentin4311.candycraftmod.entity.NessieEntity.class,
                new AABB(pos).inflate(32.0D)).size();
            return nearby <= 2;
        }
        return true;
    }

    private static boolean isCandySpawnSurface(BlockState state) {
        return state.is(CCBlocks.PUDDING.get())
            || state.is(CCBlocks.FLOUR.get())
            || state.is(CCBlocks.CANDY_GRASS_BLOCK.get())
            || state.is(CCBlocks.DARK_CANDY_GRASS_BLOCK.get())
            || state.is(CCBlocks.SUGAR_SAND.get())
            || state.is(CCBlocks.CRYSTALLIZED_SUGAR.get())
            || state.is(CCBlocks.MILK_BROWNIE_BLOCK.get())
            || state.is(CCBlocks.WHITE_BROWNIE_BLOCK.get())
            || state.is(CCBlocks.DARK_BROWNIE_BLOCK.get())
            || state.is(CCBlocks.MILK_BROWNIE_CAKE_ROLL_BLOCK.get())
            || state.is(CCBlocks.WHITE_BROWNIE_CAKE_ROLL_BLOCK.get())
            || state.is(CCBlocks.DARK_BROWNIE_CAKE_ROLL_BLOCK.get())
            || state.is(CCBlocks.CHOCOLATE_COVERED_WHITE_BROWNIE.get())
            || isGummySpawnSurface(state)
            || state.is(CCBlocks.ICE_CREAM.get())
            || state.is(CCBlocks.STRAWBERRY_ICE_CREAM.get())
            || state.is(CCBlocks.MINT_ICE_CREAM.get())
            || state.is(CCBlocks.BLUEBERRY_ICE_CREAM.get())
            || state.is(CCBlocks.CHOCOLATE_ICE_CREAM.get())
            || state.is(CCBlocks.BANANA_ICE_CREAM.get())
            || state.is(CCBlocks.MARSHMALLOW_PLANKS.get())
            || state.is(CCBlocks.MARSHMALLOW_LOG.get())
            || state.is(CCBlocks.MARSHMALLOW_LOG_DARK.get())
            || state.is(CCBlocks.MARSHMALLOW_LOG_LIGHT.get());
    }

    private static boolean isGummySpawnSurface(BlockState state) {
        return state.is(CCBlocks.RED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.ORANGE_GUMMY_BLOCK.get())
            || state.is(CCBlocks.YELLOW_GUMMY_BLOCK.get())
            || state.is(CCBlocks.WHITE_GUMMY_BLOCK.get())
            || state.is(CCBlocks.GREEN_GUMMY_BLOCK.get())
            || state.is(CCBlocks.RED_HARDENED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.ORANGE_HARDENED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.YELLOW_HARDENED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.WHITE_HARDENED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.GREEN_HARDENED_GUMMY_BLOCK.get())
            || state.is(CCBlocks.RED_GUMMY_WORM_BLOCK.get())
            || state.is(CCBlocks.ORANGE_GUMMY_WORM_BLOCK.get())
            || state.is(CCBlocks.YELLOW_GUMMY_WORM_BLOCK.get())
            || state.is(CCBlocks.WHITE_GUMMY_WORM_BLOCK.get())
            || state.is(CCBlocks.GREEN_GUMMY_WORM_BLOCK.get());
    }

    private static boolean isCandyLeafSurface(BlockState state) {
        return state.is(CCBlocks.CANDY_LEAVES.get())
            || state.is(CCBlocks.CANDY_LEAVES_DARK.get())
            || state.is(CCBlocks.CANDY_LEAVES_LIGHT.get())
            || state.is(CCBlocks.CANDY_LEAVES_CHERRY.get())
            || state.is(CCBlocks.CANDY_LEAVES_ENCHANT.get())
            || state.is(CCBlocks.MILK_CHOCOLATE_LEAVES.get())
            || state.is(CCBlocks.WHITE_CHOCOLATE_LEAVES.get())
            || state.is(CCBlocks.DARK_CHOCOLATE_LEAVES.get())
            || state.is(CCBlocks.COTTON_CANDY_BLOCK.get());
    }
}
