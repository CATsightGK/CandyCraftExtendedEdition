package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.CandyLiquidBlock;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCFluids;
import com.valentin4311.candycraftmod.item.JellyWandItem;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
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
        tickPinkFire(living);
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
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        JellyWandItem.accelerateCharge(event.getEntity());
    }

    @SubscribeEvent
    public static void onSpawnEggInteractEntity(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.getItemStack().getItem() instanceof ForgeSpawnEggItem) {
            event.setCancellationResult(net.minecraft.world.InteractionResult.sidedSuccess(event.getLevel().isClientSide));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        JellyWandItem.accelerateCharge(event.getEntity());
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getSpawnType() == null) {
            return;
        }
        if (event.getSpawnType() == net.minecraft.world.entity.MobSpawnType.NATURAL
            || event.getSpawnType() == net.minecraft.world.entity.MobSpawnType.CHUNK_GENERATION) {
            ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
            if (id != null && CandyCraft.MODID.equals(id.getNamespace())) {
                event.setSpawnCancelled(true);
            }
        }
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

    private static boolean has(Player player, Item item) {
        return player.getInventory().contains(item.getDefaultInstance());
    }
}
