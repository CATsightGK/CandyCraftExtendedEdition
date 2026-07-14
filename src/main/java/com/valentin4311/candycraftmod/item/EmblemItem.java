package com.valentin4311.candycraftmod.item;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class EmblemItem extends Item {
    private final String descriptionKey;

    public EmblemItem(String descriptionKey, Properties properties) {
        super(properties);
        this.descriptionKey = descriptionKey;
    }

    public static int getRegisteredCount() {
        return Math.max(1, (int) ForgeRegistries.ITEMS.getValues().stream()
            .filter(EmblemItem.class::isInstance)
            .count());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(descriptionKey).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.candycraftmod.emblem").withStyle(ChatFormatting.GREEN));
    }
}
