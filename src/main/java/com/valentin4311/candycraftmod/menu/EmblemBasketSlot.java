package com.valentin4311.candycraftmod.menu;

import com.valentin4311.candycraftmod.inventory.EmblemBasketContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EmblemBasketSlot extends Slot {
    private final EmblemBasketContainer basket;

    public EmblemBasketSlot(EmblemBasketContainer basket, int index, int x, int y) {
        super(basket, index, x, y);
        this.basket = basket;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return basket.canPlaceEmblem(getSlotIndex(), stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean isActive() {
        return basket.isSlotVisible(getSlotIndex());
    }
}
