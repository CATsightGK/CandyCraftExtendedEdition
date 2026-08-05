package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.LegacySaplingBlock;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import com.valentin4311.candycraftmod.entity.CandyFishEntity;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCFluids;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCToolProperties;
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
import net.minecraft.tags.TagKey;
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
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CCForgeEvents {
    // Item keeps these UUIDs protected, but the tooltip recognizes them to display final main-hand values.
    private static final java.util.UUID BASE_ATTACK_DAMAGE_UUID = java.util.UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final java.util.UUID BASE_ATTACK_SPEED_UUID = java.util.UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    private static final String CRANBERRY_EMBLEM_DAY = CandyCraft.MODID + ".cranberry_emblem_day";
    private static final ResourceKey<Level> CANDY_WORLD = dimensionKey("candy_world");
    private static final ResourceKey<Level> JELLY_DUNGEON = dimensionKey("jelly_dungeon");
    private static final ResourceKey<Level> SUGUARD_DUNGEON = dimensionKey("suguard_dungeon");
    private static final TagKey<Block> CANDY_WORLD_BEDS = TagKey.create(
        Registries.BLOCK, new ResourceLocation(CandyCraft.MODID, "candy_world_beds"));

    private CCForgeEvents() {
    }

    @SubscribeEvent
    public static void onItemAttributes(ItemAttributeModifierEvent event) {
        CCToolProperties.Profile profile = CCToolProperties.get(event.getItemStack());
        if (profile == null) {
            return;
        }

        if (event.getSlotType() == EquipmentSlot.MAINHAND) {
            if (profile.attackDamage() != null) {
                event.removeAttribute(Attributes.ATTACK_DAMAGE);
                event.addModifier(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                    BASE_ATTACK_DAMAGE_UUID, "CandyCraft configured attack damage",
                    profile.attackDamage() - 1.0D, AttributeModifier.Operation.ADDITION));
            }
            if (profile.attackSpeed() != null) {
                event.removeAttribute(Attributes.ATTACK_SPEED);
                event.addModifier(Attributes.ATTACK_SPEED, new AttributeModifier(
                    BASE_ATTACK_SPEED_UUID, "CandyCraft configured attack speed",
                    profile.attackSpeed() - 4.0D, AttributeModifier.Operation.ADDITION));
            }
        }

        if (isConfiguredArmorSlot(profile.toolType(), event.getSlotType())) {
            applyEquipmentAttribute(event, Attributes.ARMOR, profile.armor(), "armor");
            applyEquipmentAttribute(event, Attributes.ARMOR_TOUGHNESS, profile.armorToughness(), "armor_toughness");
            applyEquipmentAttribute(event, Attributes.KNOCKBACK_RESISTANCE, profile.knockbackResistance(), "knockback_resistance");
        }
    }

    private static boolean isConfiguredArmorSlot(String toolType, EquipmentSlot slot) {
        return switch (toolType) {
            case "helmet" -> slot == EquipmentSlot.HEAD;
            case "chestplate" -> slot == EquipmentSlot.CHEST;
            case "leggings" -> slot == EquipmentSlot.LEGS;
            case "boots" -> slot == EquipmentSlot.FEET;
            default -> false;
        };
    }

    private static void applyEquipmentAttribute(ItemAttributeModifierEvent event,
            net.minecraft.world.entity.ai.attributes.Attribute attribute, Double value, String key) {
        if (value == null) {
            return;
        }
        event.removeAttribute(attribute);
        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(event.getItemStack().getItem());
        java.util.UUID uuid = java.util.UUID.nameUUIDFromBytes(
            (CandyCraft.MODID + ":" + itemId + ":" + event.getSlotType() + ":" + key)
                .getBytes(java.nio.charset.StandardCharsets.UTF_8));
        event.addModifier(attribute, new AttributeModifier(
            uuid, "CandyCraft configured " + key, value, AttributeModifier.Operation.ADDITION));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        applyThickFluidDrag(player);
        if (player.level().isClientSide) {
            return;
        }
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(CCItems.WATER_MASK.get())) {
            player.setAirSupply(player.getMaxAirSupply());
        }
        if (player.isInWater() && player.tickCount % 600 == 0 && has(player, CCItems.WATER_EMBLEM.get())) {
            player.heal(1.0F);
        }
        if (isDawn(player) && has(player, CCItems.CRANBERRY_EMBLEM.get())) {
            healAtDawn(player);
        }
    }

    private static void applyThickFluidDrag(Player player) {
        double drag = player.getFluidTypeHeight(CCFluids.LIQUID_CANDY_TYPE.get()) > 0.0D ? 0.72D
            : player.getFluidTypeHeight(CCFluids.LIQUID_CHOCOLATE_TYPE.get()) > 0.0D ? 0.82D
            : 1.0D;
        if (drag >= 1.0D) {
            return;
        }
        net.minecraft.world.phys.Vec3 movement = player.getDeltaMovement();
        player.setDeltaMovement(movement.x * drag, movement.y * 0.92D, movement.z * drag);
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide
            && isBlockedDungeonMob(event.getEntity())) {
            event.setCanceled(true);
        }
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
        if (tryRejectVanillaBed(event)) {
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            event.setCanceled(true);
            return;
        }
        if (tryTillCandySoil(event) || tryGrowCandySapling(event)) {
            event.setCancellationResult(InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            event.setCanceled(true);
        }
    }

    private static boolean tryRejectVanillaBed(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockState state = level.getBlockState(event.getPos());
        if (!isCandyWorld(level) || !(state.getBlock() instanceof BedBlock)) {
            return false;
        }
        if (state.is(CANDY_WORLD_BEDS)) {
            return false;
        }
        if (!level.isClientSide) {
            event.getEntity().displayClientMessage(
                Component.translatable("message.candycraftmod.vanilla_bed_unresponsive"), true);
        }
        return true;
    }

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        ServerLevel level = (ServerLevel) event.getLevel();
        if (!isCandyWorld(level)) {
            return;
        }
        ServerLevel overworld = level.getServer().overworld();
        long dayTime = overworld.getDayTime();
        long nextMorning = dayTime - Math.floorMod(dayTime, 24000L) + 24000L;
        // Custom dimensions use derived level data, whose setDayTime is a no-op.
        overworld.setDayTime(nextMorning);
        event.setTimeAddition(nextMorning);
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
            case "sugar_plains", "sugar_forest", "sugar_cold_forest", "sugar_enchanted_forest", "sugar_mountains", "ice_cream_plains", "ice_cream_sky_mountains", "caramel_forest" -> true;
            default -> false;
        };
    }

    private static boolean isCandyPigBiome(String path) {
        return switch (path) {
            case "sugar_plains", "sugar_forest", "sugar_cold_forest", "sugar_enchanted_forest",
                "sugar_mountains", "caramel_forest" -> true;
            default -> false;
        };
    }

    private static boolean isCottonCandySheepBiome(String path) {
        return switch (path) {
            case "cotton_candy_plains", "sugar_plains", "sugar_forest", "sugar_cold_forest",
                "sugar_enchanted_forest", "sugar_mountains", "sugar_hell_mountains",
                "ice_cream_plains", "ice_cream_sky_mountains", "caramel_forest",
                "chocolate_forest", "gummy_swamp" -> true;
            default -> false;
        };
    }

    private static boolean isGummyBunnyBiome(String path) {
        return switch (path) {
            case "sugar_plains", "sugar_forest", "sugar_cold_forest", "sugar_enchanted_forest",
                "sugar_mountains", "caramel_forest", "chocolate_forest", "gummy_swamp" -> true;
            default -> false;
        };
    }

    private static boolean isWaffleSheepBiome(String path) {
        return switch (path) {
            case "sugar_plains", "sugar_cold_forest", "sugar_mountains" -> true;
            default -> false;
        };
    }

    private static void healAtDawn(Player player) {
        long dayTime = player.level().getDayTime();
        long day = dayTime / 24000L;
        CompoundTag data = player.getPersistentData();
        if (data.getLong(CRANBERRY_EMBLEM_DAY) == day) {
            return;
        }
        data.putLong(CRANBERRY_EMBLEM_DAY, day);
        player.heal(200.0F);
        player.displayClientMessage(Component.translatable("message.candycraftmod.cranberry_emblem"), true);
    }

    private static boolean isDawn(Player player) {
        return player.level().getDayTime() % 24000L <= 20L;
    }

    private static boolean isCandyWorld(Level level) {
        return level.dimension().equals(CANDY_WORLD);
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
        if (!state.is(CCBlocks.COTTON_CANDY_SAPLING.get())) {
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
            // Gummy terrain is 15.92/16 blocks tall, so vanilla ON_GROUND rejects it
            // before our candy-surface predicate can run.
            event.register(CCEntityTypes.GUMMY_BUNNY.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CCForgeEvents::canSpawnOnCandySurface, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.COTTON_CANDY_SHEEP.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.valentin4311.candycraftmod.entity.CottonCandySheepEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.EASTER_CHICKEN.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.valentin4311.candycraftmod.entity.EasterChickenEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.GUMMY_MOUSE.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.valentin4311.candycraftmod.entity.GummyMouseEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
            event.register(CCEntityTypes.GUMMY_BEAR.get(), SpawnPlacements.Type.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, com.valentin4311.candycraftmod.entity.GummyBearEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
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

    private static boolean canCaramelBeeSpawn(EntityType<? extends Mob> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, net.minecraft.util.RandomSource random) {
        return level.getLevelData().getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
            && level.getBlockState(pos).isAir()
            && !level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER);
    }

    private static boolean canNessieSpawn(EntityType<? extends Mob> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (pos.getY() <= 45 || pos.getY() >= 63 || !level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER)) {
            return false;
        }
        if (level instanceof ServerLevel serverLevel) {
            int nearby = serverLevel.getEntitiesOfClass(com.valentin4311.candycraftmod.entity.NessieEntity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(32.0D)).size();
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

