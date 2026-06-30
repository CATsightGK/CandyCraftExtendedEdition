package com.valentin4311.candycraftmod.item;

import net.minecraft.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WikiItem extends Item {
    private static final String WIKI_URL = "https://github.com/CATsightGK/CandyCraftExtendedEdition";

    public WikiItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            Util.getPlatform().openUri(WIKI_URL);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
