package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.compat.CuriosCompat;
import com.valentin4311.candycraftmod.inventory.EmblemBasketContainer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class EmblemBasketEvents {
    private EmblemBasketEvents() {
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal().getPersistentData().contains(EmblemBasketContainer.TAG_NAME)) {
            event.getEntity().getPersistentData().put(
                EmblemBasketContainer.TAG_NAME,
                event.getOriginal().getPersistentData().get(EmblemBasketContainer.TAG_NAME).copy()
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            CuriosCompat.syncEmblemSlots(event.player);
        }
    }
}
