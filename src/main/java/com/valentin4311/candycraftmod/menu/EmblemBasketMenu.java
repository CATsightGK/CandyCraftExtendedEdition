package com.valentin4311.candycraftmod.menu;

import com.valentin4311.candycraftmod.inventory.EmblemBasketContainer;
import com.valentin4311.candycraftmod.item.EmblemItem;
import com.valentin4311.candycraftmod.registry.CCMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class EmblemBasketMenu extends AbstractContainerMenu {
    private static final int EMBLEM_SLOT_COUNT = EmblemBasketContainer.MAX_EMBLEMS;
    private final EmblemBasketContainer basket;

    public EmblemBasketMenu(int id, Inventory inventory) {
        super(CCMenus.EMBLEM_BASKET.get(), id);
        this.basket = new EmblemBasketContainer(inventory.player, EmblemBasketMenu::isEmblem);

        for (int slot = 0; slot < EMBLEM_SLOT_COUNT; slot++) {
            addSlot(new EmblemBasketSlot(basket, slot, 17 + slot * 18, 18));
        }

        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 49 + row * 18));
            }
        }

        for (int column = 0; column < 9; ++column) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 107));
        }
    }

    public static boolean isEmblem(ItemStack stack) {
        return stack.getItem() instanceof EmblemItem;
    }

    public int getVisibleEmblemSlots() {
        return basket.getVisibleSlots();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copied = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copied = stack.copy();
            if (index < EMBLEM_SLOT_COUNT) {
                if (!moveItemStackTo(stack, EMBLEM_SLOT_COUNT, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (isEmblem(stack)) {
                if (!moveItemStackTo(stack, 0, basket.getVisibleSlots(), false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == copied.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, stack);
        }
        return copied;
    }

    @Override
    public boolean stillValid(Player player) {
        return basket.stillValid(player);
    }
}
