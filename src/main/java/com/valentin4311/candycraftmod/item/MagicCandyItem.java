package com.valentin4311.candycraftmod.item;

import com.valentin4311.candycraftmod.block.CandyPortalBlock;
import com.valentin4311.candycraftmod.registry.CCToolProperties;
import com.valentin4311.candycraftmod.world.CCDimensions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

public class MagicCandyItem extends Item {
    public static final int USES = 4;

    public MagicCandyItem(Properties properties) {
        super(properties.durability(USES));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.dimension() != Level.OVERWORLD && level.dimension() != CCDimensions.CANDY_WORLD) {
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            Level target = level.dimension() == CCDimensions.CANDY_WORLD
                ? serverPlayer.server.getLevel(Level.OVERWORLD)
                : serverPlayer.server.getLevel(CCDimensions.CANDY_WORLD);
            if (target == null) {
                return InteractionResultHolder.fail(stack);
            }
        }
        return super.use(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
        if (!(living instanceof Player player)) {
            return super.finishUsingItem(stack, level, living);
        }

        InteractionHand hand = living.getUsedItemHand();
        super.finishUsingItem(stack, level, living);
        if (!player.getAbilities().instabuild) {
            stack.setCount(1);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
            && CandyPortalBlock.teleportPlayer(serverPlayer)) {
            stack.hurtAndBreak(1, player, brokenPlayer -> brokenPlayer.broadcastBreakEvent(hand));
        }
        return stack;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        Boolean configured = CCToolProperties.configuredEnchantmentRule(stack, enchantment);
        return configured != null ? configured
            : enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.MENDING;
    }
}
