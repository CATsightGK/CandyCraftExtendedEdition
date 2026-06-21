package com.valentin4311.candycraftmod.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class CandycraftAmuletItem extends Item {
    public CandycraftAmuletItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.candycraftmod.candycraft_amulet.slots", stack.getCount()).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.candycraftmod.candycraft_amulet").withStyle(ChatFormatting.AQUA));
    }
}
