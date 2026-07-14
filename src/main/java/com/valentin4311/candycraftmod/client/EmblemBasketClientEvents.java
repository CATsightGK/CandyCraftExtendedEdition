package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.compat.CuriosCompat;
import com.valentin4311.candycraftmod.network.CCTasteNetwork;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class EmblemBasketClientEvents {
    private EmblemBasketClientEvents() {
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen screen) {
            if (CuriosCompat.isLoaded()) {
                return;
            }
            int x = screen.getGuiLeft() + 151;
            int y = screen.getGuiTop() + 7;
            event.addListener(new EmblemBasketButton(x, y, button -> CCTasteNetwork.openEmblemBasket()));
        }
    }
}
