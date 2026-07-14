package com.valentin4311.candycraftmod.util;

import com.valentin4311.candycraftmod.compat.CuriosCompat;
import com.valentin4311.candycraftmod.inventory.EmblemBasketContainer;
import com.valentin4311.candycraftmod.menu.EmblemBasketMenu;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public final class EmblemHelper {
    private static final Map<Player, TickCache> CACHE = new WeakHashMap<>();

    private EmblemHelper() {
    }

    public static synchronized boolean has(Player player, Item item) {
        TickCache cache = CACHE.computeIfAbsent(player, ignored -> new TickCache());
        if (cache.tick != player.tickCount) {
            cache.tick = player.tickCount;
            cache.results.clear();
            cache.basket = null;
        }
        return cache.results.computeIfAbsent(item, checkedItem -> hasUncached(player, checkedItem, cache));
    }

    public static synchronized void invalidate(Player player) {
        CACHE.remove(player);
    }

    private static boolean hasUncached(Player player, Item item, TickCache cache) {
        if (CuriosCompat.isLoaded()) {
            return CuriosCompat.isEquipped(player, item);
        }
        if (cache.basket == null) {
            cache.basket = new EmblemBasketContainer(player, EmblemBasketMenu::isEmblem);
        }
        return cache.basket.hasEmblem(item);
    }

    private static final class TickCache {
        private int tick = Integer.MIN_VALUE;
        private final Map<Item, Boolean> results = new HashMap<>();
        private EmblemBasketContainer basket;
    }
}
