package com.valentin4311.candycraftmod.block.entity;

import com.valentin4311.candycraftmod.registry.CCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MarshmallowChestBlockEntity extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private int openCount;

    public MarshmallowChestBlockEntity(BlockPos pos, BlockState state) {
        super(CCBlockEntities.MARSHMALLOW_CHEST.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.candycraftmod.marshmallow_chest");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory, this);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
            && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void startOpen(Player player) {
        if (level == null || player.isSpectator()) {
            return;
        }
        if (openCount++ == 0) {
            level.playSound(null, worldPosition, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F,
                level.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (level == null || player.isSpectator()) {
            return;
        }
        openCount = Math.max(0, openCount - 1);
        if (openCount == 0) {
            level.playSound(null, worldPosition, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F,
                level.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }
}
