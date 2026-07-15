package com.valentin4311.candycraftmod.menu;

import com.valentin4311.candycraftmod.block.MarshmallowChestBlock;
import com.valentin4311.candycraftmod.registry.CCMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MarshmallowChestMenu extends AbstractContainerMenu {
    private static final int ROWS = 3;
    private static final int CHEST_SIZE = 9 * ROWS;
    private final Container container;
    private final MarshmallowChestBlock.Theme theme;

    public MarshmallowChestMenu(int id, Inventory inventory, MarshmallowChestBlock.Theme theme) {
        this(id, inventory, new SimpleContainer(CHEST_SIZE), theme);
    }

    public MarshmallowChestMenu(int id, Inventory inventory, Container container, MarshmallowChestBlock.Theme theme) {
        super(CCMenus.MARSHMALLOW_CHEST.get(), id);
        checkContainerSize(container, CHEST_SIZE);
        this.container = container;
        this.theme = theme;
        container.startOpen(inventory.player);

        for (int row = 0; row < ROWS; ++row) {
            for (int column = 0; column < 9; ++column) {
                addSlot(new Slot(container, column + row * 9, 8 + column * 18, 18 + row * 18));
            }
        }
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 85 + row * 18));
            }
        }
        for (int column = 0; column < 9; ++column) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 143));
        }
    }

    public MarshmallowChestBlock.Theme theme() {
        return theme;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < CHEST_SIZE) {
                if (!moveItemStackTo(stack, CHEST_SIZE, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, CHEST_SIZE, false)) {
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
