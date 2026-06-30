package com.valentin4311.candycraftmod.item;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class DragibusSeedFoodItem extends ItemNameBlockItem {
    private static final FoodProperties FOOD = new FoodProperties.Builder()
        .nutrition(1)
        .saturationMod(0.3F)
        .alwaysEat()
        .build();

    public DragibusSeedFoodItem(Block crop) {
        super(crop, new Properties().food(FOOD));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult placement = super.useOn(context);
        if (placement.consumesAction()) {
            return placement;
        }

        Player player = context.getPlayer();
        if (player == null) {
            return placement;
        }

        player.startUsingItem(context.getHand());
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }
}
