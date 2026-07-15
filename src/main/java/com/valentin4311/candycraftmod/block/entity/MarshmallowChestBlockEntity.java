package com.valentin4311.candycraftmod.block.entity;

import com.valentin4311.candycraftmod.registry.CCBlockEntities;
import com.valentin4311.candycraftmod.block.MarshmallowChestBlock;
import com.valentin4311.candycraftmod.menu.MarshmallowChestMenu;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.ChestType;

public class MarshmallowChestBlockEntity extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private int openCount;
    private float openness;
    private float oldOpenness;

    public MarshmallowChestBlockEntity(BlockPos pos, BlockState state) {
        super(CCBlockEntities.MARSHMALLOW_CHEST.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.candycraftmod." + theme().serializedName());
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new MarshmallowChestMenu(id, inventory, this, theme());
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
        if (openCount++ == 0 && shouldPlaySound()) {
            level.playSound(null, worldPosition, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F,
                level.random.nextFloat() * 0.1F + 0.9F);
        }
        level.blockEvent(worldPosition, getBlockState().getBlock(), 1, openCount);
    }

    @Override
    public void stopOpen(Player player) {
        if (level == null || player.isSpectator()) {
            return;
        }
        openCount = Math.max(0, openCount - 1);
        if (openCount == 0 && shouldPlaySound()) {
            level.playSound(null, worldPosition, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F,
                level.random.nextFloat() * 0.1F + 0.9F);
        }
        level.blockEvent(worldPosition, getBlockState().getBlock(), 1, openCount);
    }

    @Override
    public boolean triggerEvent(int id, int data) {
        if (id == 1) {
            openCount = data;
            return true;
        }
        return super.triggerEvent(id, data);
    }

    public float getOpenNess(float partialTick) {
        return Mth.lerp(partialTick, oldOpenness, openness);
    }

    public MarshmallowChestBlock.Theme theme() {
        return getBlockState().getBlock() instanceof MarshmallowChestBlock chest
            ? chest.theme()
            : MarshmallowChestBlock.Theme.NORMAL;
    }

    private boolean shouldPlaySound() {
        return !getBlockState().hasProperty(MarshmallowChestBlock.TYPE)
            || getBlockState().getValue(MarshmallowChestBlock.TYPE) != ChestType.RIGHT;
    }

    public static void lidAnimateTick(Level level, BlockPos pos, BlockState state, MarshmallowChestBlockEntity chest) {
        chest.oldOpenness = chest.openness;
        float target = chest.openCount > 0 ? 1.0F : 0.0F;
        chest.openness = Mth.clamp(chest.openness + Math.signum(target - chest.openness) * 0.1F, 0.0F, 1.0F);
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
