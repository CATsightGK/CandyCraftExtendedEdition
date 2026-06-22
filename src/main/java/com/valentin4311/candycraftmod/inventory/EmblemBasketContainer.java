package com.valentin4311.candycraftmod.inventory;

import java.util.function.Predicate;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EmblemBasketContainer implements Container {
    public static final int MAX_EMBLEMS = 8;
    public static final String TAG_NAME = "CandyCraftEmblemBasket";
    private final Player player;
    private final Predicate<ItemStack> emblemPredicate;
    private final NonNullList<ItemStack> items = NonNullList.withSize(MAX_EMBLEMS, ItemStack.EMPTY);

    public EmblemBasketContainer(Player player, Predicate<ItemStack> emblemPredicate) {
        this.player = player;
        this.emblemPredicate = emblemPredicate;
        load();
    }

    public int getVisibleSlots() {
        return Math.min(MAX_EMBLEMS, getOccupiedSlots() + 1);
    }

    public boolean isSlotVisible(int slot) {
        return slot >= 0 && slot < getVisibleSlots();
    }

    public boolean hasEmblem(Item item) {
        for (ItemStack stack : items) {
            if (stack.is(item)) {
                return true;
            }
        }
        return false;
    }

    public boolean canPlaceEmblem(int slot, ItemStack stack) {
        if (!isSlotVisible(slot) || stack.isEmpty() || !emblemPredicate.test(stack)) {
            return false;
        }
        Item item = stack.getItem();
        for (int i = 0; i < items.size(); i++) {
            if (i != slot && items.get(i).is(item)) {
                return false;
            }
        }
        return true;
    }

    private int getOccupiedSlots() {
        int occupied = 0;
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                occupied++;
            }
        }
        return occupied;
    }

    private void load() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        if (!player.getPersistentData().contains(TAG_NAME, Tag.TAG_LIST)) {
            return;
        }
        ListTag list = player.getPersistentData().getList(TAG_NAME, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot >= 0 && slot < items.size()) {
                ItemStack stack = ItemStack.of(entry);
                if (!stack.isEmpty() && emblemPredicate.test(stack)) {
                    stack.setCount(1);
                    items.set(slot, stack);
                }
            }
        }
    }

    private void save() {
        ListTag list = new ListTag();
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                CompoundTag entry = new CompoundTag();
                entry.putByte("Slot", (byte) i);
                stack.save(entry);
                list.add(entry);
            }
        }
        player.getPersistentData().put(TAG_NAME, list);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack result = ContainerHelper.takeItem(items, slot);
        setChanged();
        return result;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!stack.isEmpty()) {
            stack = stack.copy();
            stack.setCount(1);
        }
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public void setChanged() {
        save();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.player == player;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
        setChanged();
    }
}
