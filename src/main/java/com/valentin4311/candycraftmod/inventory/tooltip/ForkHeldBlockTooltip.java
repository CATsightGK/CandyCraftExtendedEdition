package com.valentin4311.candycraftmod.inventory.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public record ForkHeldBlockTooltip(BlockState state) implements TooltipComponent {
    public ItemStack iconStack() {
        return new ItemStack(state.getBlock().asItem());
    }

    public Component name() {
        return state.getBlock().getName();
    }
}
