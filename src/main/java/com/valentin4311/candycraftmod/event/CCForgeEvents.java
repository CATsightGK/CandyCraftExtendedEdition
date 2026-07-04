package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.LegacySaplingBlock;
import com.valentin4311.candycraftmod.entity.CandyFishEntity;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCFluids;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCSweetscapeBlocks;
import com.valentin4311.candycraftmod.util.EmblemHelper;
import com.valentin4311.candycraftmod.world.feature.CottonCandyTreeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CCForgeEvents {
    private static final String CRANBERRY_EMBLEM_DAY = CandyCraft.MODID + ".cranberry_emblem_day";
    private static final ResourceKey<Level> CANDY_WORLD = dimensionKey("candy_world");
    private static final ResourceKey<Level> JELLY_DUNGEON = dimensionKey("jelly_dungeon");
    private static final ResourceKey<Level> SUGUARD_DUNGEON = dimensionKey("suguard_dungeon");

    private CCForgeEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }
        Player player = event.player;
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(CCItems.WATER_MASK.get())) {
            player.setAirSupply(player.getMaxAirSupply());
        }
        if (has(player, CCItems.WATER_EMBLEM.get()) && player.isInWater() && player.tickCount % 600 == 0) {
            player.heal(1.0F);
        }
        if (has(player, CCItems.CRANBERRY_EMBLEM.get())) {
            healAtDawn(player);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide
            && (isBlockedCandyWorldMob(event.getEntity()) || isBlockedDungeonMob(event.getEntity()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !isCandyWorld(level)) {
            return;
        }
        scheduleExposedCandyFluidTicks(level, event.getChunk());
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (event.getSource().is(DamageTypeTags.IS_FALL) && player.getItemBySlot(EquipmentSlot.FEET).is(CCItems.JELLY_BOOTS.get())) {
            event.setCanceled(true);
            event.setAmount(0.0F);
            return;
        }
        if (event.getSource().is(DamageTypeTags.IS_FALL) && has(player, CCItems.JELLY_EMBLEM.get())) {
            event.setAmount(event.getAmount() * 0.7F);
        }
        if (event.getSource().getDirectEntity() instanceof AbstractArrow && has(player, CCItems.SUGUARD_EMBLEM.get())) {
            event.setAmount(event.getAmount() * 0.8F);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isProtectedDungeonInteraction(event.getLevel(), event.getEntity())) {
            return;
        }
        if (tryTillCandySoil(event) || tryGrowCandySapling(event)) {
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (isProtectedDungeonInteraction(event.getLevel(), event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player && isProtectedDungeonInteraction(event.getLevel(), player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onSpawnEggInteractEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getItemStack().getItem() instanceof ForgeSpawnEggItem) {
            event.setCancellationResult(net.minecraft.world.InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            event.setCanceled(true);
        }
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
            && type != CCEntityTypes.JELLY_QUEEN.get() && type != CCEntityTypes.CARAMEL_BEE.get()
            && type != CCEntityTypes.NOUGAT_GOLEM.get() && type != CCEntityTypes.BEETLE.get()
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
            if ("sugar_enchanted_forest".equals(path) || "caramel_forest".equals(path)) {
                return true;
            }
            int brightness = event.getLevel().getLevel().getMaxLocalRawBrightness(event.getEntity().blockPosition());
            return switch (path) {
                case "sugar_plains", "sugar_forest", "sugar_cold_forest", "sugar_mountains", "ice_cream_plains", "ice_cream_sky_mountains", "sugar_hell_mountains",
                    "cotton_candy_plains", "chocolate_forest", "gummy_swamp", "sugar_oceans", "sugar_river", "candycraft_dungeon" -> brightness <= 7;
                default -> false;
            };
        }
        if (type == CCEntityTypes.MAGE_SUGUARD.get()) {
            return "sugar_enchanted_forest".equals(path);
        }
        if (type == CCEntityTypes.CANDY_CREEPER.get() || type == CCEntityTypes.COTTON_CANDY_SPIDER.get()) {
            int brightness = event.getLevel().getLevel().getMaxLocalRawBrightness(event.getEntity().blockPosition());
            return brightness <= 7;
        }
        if (type == CCEntityTypes.CARAMEL_BEE.get()) {
            int brightness = event.getLevel().getLevel().getMaxLocalRawBrightness(event.getEntity().blockPosition());
            return brightness <= 7;
        }
        if (type == CCEntityTypes.CANDY_FISH.get()) {
            return "sugar_oceans".equals(path);
        }
        if (type == CCEntityTypes.NESSIE.get()) {
            return "sugar_oceans".equals(path);
        }
        if (type == CCEntityTypes.NOUGAT_GOLEM.get()) {
            return "sugar_plains".equals(path);
        }
        if (type == CCEntityTypes.BEETLE.get()) {
            return switch (path) {
                case "sugar_plains", "gummy_swamp" -> true;
                default -> false;
            };
        }
        if (type == CCEntityTypes.BOSS_BEETLE.get()) {
            return event.getSpawnType() == MobSpawnType.STRUCTURE;
        }
        if (type == CCEntityTypes.PINGOUIN.get()) {
            return "ice_cream_plains".equals(path);
        }
        if (type == CCEntityTypes.CANDY_WOLF.get()) {
            return switch (path) {
                case "sugar_cold_forest", "caramel_forest" -> true;
                default -> false;
            };
        }
        if (type == CCEntityTypes.JELLY_QUEEN.get()) {
            return event.getSpawnType() == MobSpawnType.CHUNK_GENERATION;
        }
        if (type == CCEntityTypes.CANDY_PIG.get()
            || type == CCEntityTypes.GUMMY_BUNNY.get()
            || type == CCEntityTypes.WAFFLE_SHEEP.get()) {
            return isRegularCandyCreatureBiome(path);
        }
        return switch (path) {
            case "sugar_plains", "sugar_forest", "sugar_cold_forest", "sugar_enchanted_forest", "sugar_mountains", "ice_cream_plains", "ice_cream_sky_mountains", "caramel_forest" -> true;
            default -> false;
        };
    }

    private static boolean isRegularCandyCreatureBiome(String path) {
        return switch (path) {
            case "sugar_plains", "sugar_forest", "sugar_cold_forest", "sugar_enchanted_forest", "sugar_mountains",
                "ice_cream_plains", "ice_cream_sky_mountains", "caramel_forest", "chocolate_forest",
                "cotton_candy_plains", "gummy_swamp" -> true;
            default -> false;
        };
    }

    private static void healAtDawn(Player player) {
        long dayTime = player.level().getDayTime();
        if (dayTime % 24000L > 20L) {
            return;
        }
        long day = dayTime / 24000L;
        CompoundTag data = player.getPersistentData();
        if (data.getLong(CRANBERRY_EMBLEM_DAY) == day) {
            return;
        }
        data.putLong(CRANBERRY_EMBLEM_DAY, day);
        player.heal(200.0F);
        player.displayClientMessage(Component.translatable("message.candycraftmod.cranberry_emblem"), true);
    }

    private static boolean isCandyWorld(Level level) {
        return level.dimension().equals(CANDY_WORLD);
    }

    private static void scheduleExposedCandyFluidTicks(ServerLevel level, ChunkAccess chunk) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        ChunkAccess actualChunk = chunk;
        int minY = Math.max(63, actualChunk.getMinBuildHeight());
        int maxY = Math.min(level.getMaxBuildHeight(), actualChunk.getMaxBuildHeight());
        int minX = actualChunk.getPos().getMinBlockX();
        int minZ = actualChunk.getPos().getMinBlockZ();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    pos.set(minX + x, y, minZ + z);
                    FluidState fluid = actualChunk.getFluidState(pos);
                    if (isCandyFluid(fluid) && isExposedFluid(level, pos)) {
                        level.scheduleTick(pos.immutable(), fluid.getType(), fluid.getType().getTickDelay(level));
                    }
                }
            }
        }
    }

    private static boolean isExposedFluid(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos.below()).isAir()) {
            return true;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(direction)).isAir()) {
                return true;
            }
        }
        return false;
    }

    private static boolean isCandyFluid(FluidState state) {
        return state.is(CCFluids.SOURCE_GRENADINE.get()) || state.is(CCFluids.FLOWING_GRENADINE.get())
            || state.is(CCFluids.SOURCE_LIQUID_CANDY.get()) || state.is(CCFluids.FLOWING_LIQUID_CANDY.get())
            || state.is(CCFluids.SOURCE_CARAMEL.get()) || state.is(CCFluids.FLOWING_CARAMEL.get())
            || state.is(CCFluids.SOURCE_LIQUID_CHOCOLATE.get()) || state.is(CCFluids.FLOWING_LIQUID_CHOCOLATE.get());
    }

    private static boolean isDungeonLevel(Level level) {
        return level.dimension().equals(JELLY_DUNGEON) || level.dimension().equals(SUGUARD_DUNGEON);
    }

    private static boolean isNaturalWorldSpawn(MobSpawnType spawnType) {
        return spawnType == MobSpawnType.NATURAL
            || spawnType == MobSpawnType.CHUNK_GENERATION
            || spawnType == MobSpawnType.PATROL
            || spawnType == MobSpawnType.REINFORCEMENT
            || spawnType == MobSpawnType.STRUCTURE;
    }

    private static boolean isProtectedDungeonInteraction(LevelAccessor level, Player player) {
        if (player == null || player.getAbilities().instabuild) {
            return false;
        }
        if (!(level instanceof Level actualLevel)) {
            return false;
        }
        return actualLevel.dimension().equals(JELLY_DUNGEON) || actualLevel.dimension().equals(SUGUARD_DUNGEON);
    }

    private static ResourceKey<Level> dimensionKey(String path) {
        return ResourceKey.create(Registries.DIMENSION, new ResourceLocation(CandyCraft.MODID, path));
    }

    private static boolean isBlockedCandyWorldMob(Entity entity) {
        if (!(entity instanceof Mob) || !isCandyWorld(entity.level())) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return id != null && "minecraft".equals(id.getNamespace());
    }

    private static boolean isBlockedDungeonMob(Entity entity) {
        if (!(entity instanceof Mob) || !isDungeonLevel(entity.level())) {
            return false;
        }
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (id == null) {
            return false;
        }
        return "minecraft".equals(id.getNamespace()) || entity.getType() == CCEntityTypes.CARAMEL_BEE.get();
    }

    private static boolean tryTillCandySoil(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof HoeItem)) {
            return false;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(CCBlocks.PUDDING.get()) && !state.is(CCBlocks.FLOUR.get())) {
            return false;
        }
        if (!level.getBlockState(pos.above()).isAir()) {
            return false;
        }

        if (!level.isClientSide) {
            level.setBlock(pos, CCBlocks.CANDY_FARMLAND.get().defaultBlockState(), 11);
            level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
            stack.hurtAndBreak(1, event.getEntity(), player -> player.broadcastBreakEvent(event.getHand()));
        }
        return true;
    }

    private static boolean tryGrowCandySapling(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(CCItems.NOUGAT_POWDER.get())) {
            return false;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof LegacySaplingBlock) {
            return false;
        }
        if (!state.is(CCSweetscapeBlocks.COTTON_CANDY_SAPLING.get())) {
            return false;
        }

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            level.removeBlock(pos, false);
            if (!CottonCandyTreeFeature.generate(serverLevel, level.random, pos)) {
                level.setBlock(pos, state, 4);
                return false;
            }
            if (!event.getEntity().getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return true;
    }

    private static boolean has(Player player, Item item) {
        return EmblemHelper.has(player, item);
    }

    @Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModEvents {
        private ModEvents() {
        }

        @SubscribeEvent
        public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
            event.register(CCEntityTypes.CANDY_PIG.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.WAFFLE_SHEEP.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.CANDY_WOLF.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.GUMMY_BUNNY.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.PINGOUIN.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.SUGUARD.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.MAGE_SUGUARD.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.CANDY_CREEPER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.COTTON_CANDY_SPIDER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.JELLY_QUEEN.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.NOUGAT_GOLEM.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.BEETLE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.CARAMEL_BEE.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canCaramelBeeSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.CANDY_FISH.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CandyFishEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.NESSIE.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canNessieSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
        }
    }

    private static boolean canSpawnOnCandySurface(EntityType<? extends Mob> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, net.minecraft.util.RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
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

    private static boolean canCaramelBeeSpawn(EntityType<? extends Mob> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, net.minecraft.util.RandomSource random) {
        return level.getLevelData().getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
            && level.getBlockState(pos).isAir()
            && !level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER);
    }

    private static boolean canNessieSpawn(EntityType<? extends Mob> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, net.minecraft.util.RandomSource random) {
        return level.getLevelData().getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
            && pos.getY() > 45 && pos.getY() < 63
            && level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER);
    }

    private static boolean isCandySpawnSurface(BlockState state) {
        return state.is(CCBlocks.CANDY_LEAVE.get())
            || state.is(CCBlocks.PUDDING.get())
            || state.is(CCBlocks.FLOUR.get())
            || state.is(CCBlocks.CANDY_LEAVE2.get())
            || state.is(CCBlocks.CANDY_LEAVES.get())
            || state.is(CCBlocks.CANDY_LEAVES_DARK.get())
            || state.is(CCBlocks.CANDY_LEAVES_LIGHT.get())
            || state.is(CCBlocks.CANDY_LEAVES_CHERRY.get())
            || state.is(CCBlocks.CANDY_LEAVES_ENCHANT.get())
            || state.is(CCBlocks.MARSHMALLOW_PLANKS.get())
            || state.is(CCBlocks.MARSHMALLOW_LOG.get())
            || state.is(CCBlocks.MARSHMALLOW_LOG_DARK.get())
            || state.is(CCBlocks.MARSHMALLOW_LOG_LIGHT.get())
            || state.is(CCSweetscapeBlocks.COTTON_CANDY_BLOCK.get());
    }
}
