package com.valentin4311.candycraftmod.menu;

import com.valentin4311.candycraftmod.block.CandyWorkbenchBlock;
import com.valentin4311.candycraftmod.registry.CCMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.block.Block;

public class CandyWorkbenchMenu extends CraftingMenu {
    private final ContainerLevelAccess access;
    private final CandyWorkbenchBlock.CandyWorkbenchTheme theme;

    public CandyWorkbenchMenu(int id, Inventory inventory, ContainerLevelAccess access, CandyWorkbenchBlock.CandyWorkbenchTheme theme) {
        super(id, inventory, access);
        this.access = access;
        this.theme = theme;
    }

    public CandyWorkbenchBlock.CandyWorkbenchTheme theme() {
        return theme;
    }

    public int themeId() {
        return theme.id();
    }

    @Override
    public net.minecraft.world.inventory.MenuType<?> getType() {
        return CCMenus.CANDY_WORKBENCH.get();
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, pos) -> isCandyWorkbench(level.getBlockState(pos).getBlock())
            && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D, true);
    }

    private static boolean isCandyWorkbench(Block block) {
        return block instanceof CandyWorkbenchBlock;
    }
}

