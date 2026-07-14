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
    public static final int IMAGE_WIDTH = 176;
    public static final int MAX_EMBLEM_COLUMNS = 8;
    public static final int EMBLEM_START_Y = 20;
    private final EmblemBasketContainer basket;
    private final int emblemSlotCount;
    private final int inventoryStartY;

    public EmblemBasketMenu(int id, Inventory inventory) {
        super(CCMenus.EMBLEM_BASKET.get(), id);
        this.basket = new EmblemBasketContainer(inventory.player, EmblemBasketMenu::isEmblem);
        this.emblemSlotCount = basket.getContainerSize();
        int columns = Math.min(MAX_EMBLEM_COLUMNS, emblemSlotCount);
        int emblemRows = (emblemSlotCount + MAX_EMBLEM_COLUMNS - 1) / MAX_EMBLEM_COLUMNS;
        int startX = (IMAGE_WIDTH - columns * 18) / 2;
        this.inventoryStartY = EMBLEM_START_Y + emblemRows * 18 + 14;

        for (int slot = 0; slot < emblemSlotCount; slot++) {
            int row = slot / MAX_EMBLEM_COLUMNS;
            int column = slot % MAX_EMBLEM_COLUMNS;
            addSlot(new EmblemBasketSlot(basket, slot, startX + column * 18, EMBLEM_START_Y + row * 18));
        }

        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, inventoryStartY + row * 18));
            }
        }

        for (int column = 0; column < 9; ++column) {
            addSlot(new Slot(inventory, column, 8 + column * 18, inventoryStartY + 58));
        }
    }

    public static boolean isEmblem(ItemStack stack) {
        return stack.getItem() instanceof EmblemItem;
    }

    public int getVisibleEmblemSlots() {
        return basket.getVisibleSlots();
    }

    public int getEmblemSlotCount() {
        return emblemSlotCount;
    }

    public int getInventoryStartY() {
        return inventoryStartY;
    }

    public int getImageHeight() {
        return inventoryStartY + 83;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copied = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copied = stack.copy();
            if (index < emblemSlotCount) {
                if (!moveItemStackTo(stack, emblemSlotCount, slots.size(), true)) {
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
