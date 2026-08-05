package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.compat.CuriosCompat;
import com.valentin4311.candycraftmod.network.CCTasteNetwork;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.client.event.ScreenEvent;

public final class EmblemBasketClientEvents {
    private EmblemBasketClientEvents() {
    }

    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (CuriosCompat.isLoaded()) {
            return;
        }
        if (event.getScreen() instanceof InventoryScreen screen) {
            event.addListener(new EmblemBasketButton(
                () -> screen.getGuiLeft() + 126,
                () -> screen.getGuiTop() + 61,
                button -> CCTasteNetwork.openEmblemBasket()
            ));
        } else if (event.getScreen() instanceof CreativeModeInventoryScreen screen) {
            event.addListener(new EmblemBasketButton(
                () -> screen.getGuiLeft() + 199,
                () -> screen.getGuiTop() + 4,
                button -> CCTasteNetwork.openEmblemBasket()
            ));
        }
    }
}
