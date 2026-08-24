package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCFluids;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.util.EmblemHelper;
import com.valentin4311.candycraftmod.world.DungeonResetManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Server-tick driver for the dungeon reset queue plus per-tick player
 * passives (thick fluid drag, water mask, emblem heals). Domain-specific
 * listeners live in the sibling event classes.
 */
@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CCForgeEvents {
    private static final String CRANBERRY_EMBLEM_DAY = CandyCraft.MODID + ".cranberry_emblem_day";

    private CCForgeEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            DungeonResetManager.tickServer();
        }
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

    private static boolean has(Player player, Item item) {
        return EmblemHelper.has(player, item);
    }
}
