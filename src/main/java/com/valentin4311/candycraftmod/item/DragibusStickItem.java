package com.valentin4311.candycraftmod.item;

import com.valentin4311.candycraftmod.entity.CandyPigEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DragibusStickItem extends Item {
    private static final int BOOST_DAMAGE = 7;

    public DragibusStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof CandyPigEntity pig && stack.getMaxDamage() - stack.getDamageValue() >= BOOST_DAMAGE && pig.boost()) {
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!level.isClientSide) {
                stack.hurtAndBreak(BOOST_DAMAGE, player, brokenPlayer -> brokenPlayer.broadcastBreakEvent(hand));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }
}
