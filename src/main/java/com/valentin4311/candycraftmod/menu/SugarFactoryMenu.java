package com.valentin4311.candycraftmod.menu;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SugarFactoryMenu extends AbstractContainerMenu {
    private static final int CONTAINER_SIZE = 2;
    private final Container container;
    private final ContainerData data;
    private final boolean advanced;

    public SugarFactoryMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(CONTAINER_SIZE), new SimpleContainerData(2), false);
    }

    public SugarFactoryMenu(int id, Inventory inventory, boolean advanced) {
        this(id, inventory, new SimpleContainer(CONTAINER_SIZE), new SimpleContainerData(2), advanced);
    }

    public SugarFactoryMenu(int id, Inventory inventory, Container container, ContainerData data) {
        this(id, inventory, container, data, false);
    }

    public SugarFactoryMenu(int id, Inventory inventory, Container container, ContainerData data, boolean advanced) {
        super(CCMenus.SUGAR_FACTORY.get(), id);
        checkContainerSize(container, CONTAINER_SIZE);
        checkContainerDataCount(data, 2);
        this.container = container;
        this.data = data;
        this.advanced = advanced;
        container.startOpen(inventory.player);

        addSlot(new Slot(container, 0, 8, 33));
        addSlot(new Slot(container, 1, 152, 33) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 59 + row * 18));
            }
        }

        for (int column = 0; column < 9; ++column) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 117));
        }

        addDataSlots(data);
    }

    public int getProgressScaled(int width) {
        int progress = data.get(0);
        int total = data.get(1);
        return total != 0 && progress != 0 ? progress * width / total : 0;
    }

    public boolean isAdvanced() {
        return advanced;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copied = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copied = stack.copy();
            if (index == 1) {
                if (!moveItemStackTo(stack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, copied);
            } else if (index != 0) {
                if (container.canPlaceItem(0, stack)) {
                    if (!moveItemStackTo(stack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < 29) {
                    if (!moveItemStackTo(stack, 29, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(stack, 2, 29, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 2, 38, false)) {
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
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
