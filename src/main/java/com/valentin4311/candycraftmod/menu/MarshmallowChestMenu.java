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
    private final int rows;
    private final Container container;
    private final MarshmallowChestBlock.Theme theme;

    public MarshmallowChestMenu(int id, Inventory inventory, MarshmallowChestBlock.Theme theme) {
        this(id, inventory, new SimpleContainer(27), theme);
    }

    public MarshmallowChestMenu(int id, Inventory inventory, Container container, MarshmallowChestBlock.Theme theme) {
        super(CCMenus.MARSHMALLOW_CHEST.get(), id);
        this.rows = container.getContainerSize() / 9;
        checkContainerSize(container, rows * 9);
        this.container = container;
        this.theme = theme;
        container.startOpen(inventory.player);

        for (int row = 0; row < rows; ++row) {
            for (int column = 0; column < 9; ++column) {
                addSlot(new Slot(container, column + row * 9, 8 + column * 18, 18 + row * 18));
            }
        }
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 31 + rows * 18 + row * 18));
            }
        }
        for (int column = 0; column < 9; ++column) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 89 + rows * 18));
        }
    }

    public MarshmallowChestBlock.Theme theme() {
        return theme;
    }

    public int rows() {
        return rows;
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
            int chestSize = rows * 9;
            if (index < chestSize) {
                if (!moveItemStackTo(stack, chestSize, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, chestSize, false)) {
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
