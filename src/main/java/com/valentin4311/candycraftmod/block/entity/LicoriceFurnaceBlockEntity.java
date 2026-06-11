package com.valentin4311.candycraftmod.block.entity;

import com.valentin4311.candycraftmod.block.LicoriceFurnaceBlock;
import com.valentin4311.candycraftmod.menu.LicoriceFurnaceMenu;
import com.valentin4311.candycraftmod.registry.CCBlockEntities;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LicoriceFurnaceBlockEntity extends AbstractFurnaceBlockEntity {
    public LicoriceFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(CCBlockEntities.LICORICE_FURNACE.get(), pos, state, CCRecipeTypes.LICORICE_SMELTING_TYPE.get());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LicoriceFurnaceBlockEntity blockEntity) {
        boolean wasLit = state.getBlock() instanceof LicoriceFurnaceBlock furnaceBlock && furnaceBlock.isLit();
        AbstractFurnaceBlockEntity.serverTick(level, pos, state, blockEntity);
        boolean isLit = blockEntity.dataAccess.get(0) > 0;
        if (wasLit != isLit && level.getBlockState(pos).getBlock() instanceof LicoriceFurnaceBlock) {
            LicoriceFurnaceBlock.setLit(level, pos, level.getBlockState(pos), isLit);
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.candycraftmod.licorice_furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new LicoriceFurnaceMenu(id, inventory, this, dataAccess);
    }

    @Override
    protected int getBurnDuration(ItemStack fuel) {
        if (fuel.is(Items.SUGAR)) {
            return 300;
        }
        if (fuel.is(CCBlocks.SUGAR_BLOCK.get().asItem())) {
            return 1200;
        }
        return 0;
    }

    public static boolean isLicoriceFuel(ItemStack stack) {
        return stack.is(Items.SUGAR) || stack.is(CCBlocks.SUGAR_BLOCK.get().asItem());
    }
}
