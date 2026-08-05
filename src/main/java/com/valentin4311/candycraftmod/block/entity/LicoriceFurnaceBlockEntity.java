package com.valentin4311.candycraftmod.block.entity;

import com.valentin4311.candycraftmod.block.LicoriceFurnaceBlock;
import com.valentin4311.candycraftmod.menu.LicoriceFurnaceMenu;
import com.valentin4311.candycraftmod.recipe.LicoriceFuelRecipe;
import com.valentin4311.candycraftmod.registry.CCBlockEntities;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCRecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class LicoriceFurnaceBlockEntity extends AbstractFurnaceBlockEntity {
    public LicoriceFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(CCBlockEntities.LICORICE_FURNACE.get(), pos, state, CCRecipeTypes.LICORICE_SMELTING_TYPE.get());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LicoriceFurnaceBlockEntity blockEntity) {
        boolean wasLit = state.getBlock() instanceof LicoriceFurnaceBlock furnaceBlock && furnaceBlock.isLit();
        boolean hadGrenadineBucket = blockEntity.getItem(0).is(CCItems.GRENADINE_BUCKET.get());
        int resultCountBefore = blockEntity.getItem(2).getCount();
        AbstractFurnaceBlockEntity.serverTick(level, pos, state, blockEntity);
        if (hadGrenadineBucket && blockEntity.getItem(0).isEmpty()
            && blockEntity.getItem(2).is(CCBlocks.GRENADINE_GLASS.get().asItem())
            && blockEntity.getItem(2).getCount() > resultCountBefore) {
            blockEntity.setItem(0, new ItemStack(Items.BUCKET));
        }
        boolean isLit = blockEntity.dataAccess.get(0) > 0;
        if (wasLit != isLit && level.getBlockState(pos).getBlock() instanceof LicoriceFurnaceBlock) {
            LicoriceFurnaceBlock.setLit(level, pos, level.getBlockState(pos), isLit);
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable(CCBlocks.LICORICE_FURNACE.get().getDescriptionId());
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new LicoriceFurnaceMenu(id, inventory, this, dataAccess);
    }

    @Override
    protected int getBurnDuration(ItemStack fuel) {
        return fuelTime(level, fuel);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 1 ? getBurnDuration(stack) > 0 : super.canPlaceItem(slot, stack);
    }

    public static boolean isLicoriceFuel(Level level, ItemStack stack) {
        return fuelTime(level, stack) > 0;
    }

    private static int fuelTime(Level level, ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return 0;
        }
        return level.getRecipeManager().getAllRecipesFor(CCRecipeTypes.LICORICE_FUEL_TYPE.get()).stream()
            .filter(recipe -> recipe.accepts(stack))
            .mapToInt(LicoriceFuelRecipe::burnTime)
            .findFirst()
            .orElse(0);
    }
}
