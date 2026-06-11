package com.valentin4311.candycraftmod.menu;

import com.valentin4311.candycraftmod.block.entity.LicoriceFurnaceBlockEntity;
import com.valentin4311.candycraftmod.registry.CCMenus;
import com.valentin4311.candycraftmod.registry.CCRecipeTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class LicoriceFurnaceMenu extends AbstractFurnaceMenu {
    public LicoriceFurnaceMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(3), new SimpleContainerData(4));
    }

    public LicoriceFurnaceMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(CCMenus.LICORICE_FURNACE.get(), CCRecipeTypes.LICORICE_SMELTING_TYPE.get(), RecipeBookType.FURNACE, id, inventory, container, data);
    }

    @Override
    protected boolean isFuel(ItemStack stack) {
        return LicoriceFurnaceBlockEntity.isLicoriceFuel(stack);
    }

}
