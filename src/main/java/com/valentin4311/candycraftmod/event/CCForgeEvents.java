package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.CandyLiquidBlock;
import com.valentin4311.candycraftmod.block.LegacySaplingBlock;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.level.BlockEvent;
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
        if (has(player, CCItems.WATER_EMBLEM.get()) && player.isInWater() && player.tickCount % 600 == 0) {
            player.heal(1.0F);
        }
        if (has(player, CCItems.CRANBERRY_EMBLEM.get())) {
            healAtDawn(player);
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity living = event.getEntity();
        if (living.level().isClientSide) {
            return;
        }
        if (isBlockedCandyWorldMob(living)) {
            living.discard();
            return;
        }
        tickPinkFire(living);
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide && isBlockedCandyWorldMob(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide || !isCandyWorld(event.level)) {
            return;
        }
        if (event.level.getGameTime() % 20L != 0L || !(event.level instanceof ServerLevel serverLevel)) {
            return;
        }
        for (Entity entity : serverLevel.getAllEntities()) {
            if (isBlockedCandyWorldMob(entity)) {
                entity.discard();
            }
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
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
        if (event.getSpawnType() == net.minecraft.world.entity.MobSpawnType.NATURAL
            || event.getSpawnType() == net.minecraft.world.entity.MobSpawnType.CHUNK_GENERATION) {
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
            && type != CCEntityTypes.NOUGAT_GOLEM.get() && type != CCEntityTypes.BEETLE.get()) {
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
        if (type == CCEntityTypes.CARAMEL_BEE.get()) {
            int brightness = event.getLevel().getLevel().getMaxLocalRawBrightness(event.getEntity().blockPosition());
            return brightness <= 7;
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
        if (type == CCEntityTypes.CANDY_PIG.get()) {
            return switch (path) {
                case "sugar_mountains", "ice_cream_plains", "sugar_enchanted_forest", "ice_cream_sky_mountains" -> true;
                default -> false;
            };
        }
        if (type == CCEntityTypes.PINGOUIN.get()) {
            return "ice_cream_plains".equals(path);
        }
        if (type == CCEntityTypes.GUMMY_BUNNY.get()) {
            return switch (path) {
                case "sugar_mountains", "ice_cream_plains", "sugar_enchanted_forest", "gummy_swamp" -> true;
                default -> false;
            };
        }
        if (type == CCEntityTypes.CANDY_WOLF.get()) {
            return switch (path) {
                case "sugar_cold_forest", "caramel_forest" -> true;
                default -> false;
            };
        }
        if (type == CCEntityTypes.JELLY_QUEEN.get()) {
            return "gummy_swamp".equals(path);
        }
        return switch (path) {
            case "sugar_plains", "sugar_forest", "sugar_cold_forest", "sugar_enchanted_forest", "sugar_mountains", "ice_cream_plains", "ice_cream_sky_mountains", "caramel_forest" -> true;
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

    private static void tickPinkFire(LivingEntity living) {
        CompoundTag data = living.getPersistentData();
        if (data.contains(CandyLiquidBlock.PINK_FIRE_TICKS_TAG)) {
            data.remove(CandyLiquidBlock.PINK_FIRE_TICKS_TAG);
            living.clearFire();
        }
    }

    private static boolean isCandyWorld(Level level) {
        return level.dimension().equals(CANDY_WORLD);
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
            event.register(CCEntityTypes.JELLY_QUEEN.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.NOUGAT_GOLEM.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.BEETLE.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.CARAMEL_BEE.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canCaramelBeeSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
        }
    }

    private static boolean canSpawnOnCandySurface(EntityType<? extends Mob> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, net.minecraft.util.RandomSource random) {
        BlockState below = level.getBlockState(pos.below());
        if (isCandySpawnSurface(below)) {
            return level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir();
        }
        return below.isValidSpawn(level, pos.below(), type);
    }

    private static boolean canCaramelBeeSpawn(EntityType<? extends Mob> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, net.minecraft.util.RandomSource random) {
        return level.getLevelData().getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
            && level.getBlockState(pos).isAir()
            && !level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER);
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
