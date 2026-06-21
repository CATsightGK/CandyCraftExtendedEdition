package com.valentin4311.candycraftmod.util;

import com.valentin4311.candycraftmod.compat.CuriosCompat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public final class EmblemHelper {
    private EmblemHelper() {
    }

    public static boolean has(Player player, Item item) {
        return player.getInventory().contains(item.getDefaultInstance()) || CuriosCompat.isEquipped(player, item);
    }
}
