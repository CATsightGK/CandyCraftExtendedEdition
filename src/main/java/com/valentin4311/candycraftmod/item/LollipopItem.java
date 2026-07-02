package com.valentin4311.candycraftmod.item;

import com.valentin4311.candycraftmod.entity.CandyCreeperEntity;
import com.valentin4311.candycraftmod.entity.CandyWolfEntity;
import com.valentin4311.candycraftmod.registry.CCCriteriaTriggers;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class LollipopItem extends Item {
    private static final float WOLF_HEAL_AMOUNT = 8.0F;
    private static final int CREEPER_STALL_TICKS = 20 * 8;

    public LollipopItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof CandyWolfEntity wolf && wolf.getHealth() < wolf.getMaxHealth()) {
            if (!player.level().isClientSide) {
                wolf.heal(WOLF_HEAL_AMOUNT);
                consumeOne(stack, player);
                playUseEffects(player, wolf);
                if (player instanceof ServerPlayer serverPlayer) {
                    CCCriteriaTriggers.HEAL_CANDY_WOLF.trigger(serverPlayer);
                }
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }

        if (target instanceof CandyCreeperEntity creeper) {
            if (!player.level().isClientSide) {
                creeper.stallWithLollipop(CREEPER_STALL_TICKS);
                consumeOne(stack, player);
                playUseEffects(player, creeper);
                if (player instanceof ServerPlayer serverPlayer) {
                    CCCriteriaTriggers.STALL_CANDY_CREEPER.trigger(serverPlayer);
                }
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }

        return super.interactLivingEntity(stack, player, target, hand);
    }

    private static void consumeOne(ItemStack stack, Player player) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }

    private static void playUseEffects(Player player, LivingEntity target) {
        target.level().playSound(null, target.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.7F, 1.1F);
        if (target.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.HEART,
                target.getX(),
                target.getY() + target.getBbHeight() + 0.2D,
                target.getZ(),
                6,
                target.getBbWidth() * 0.35D,
                0.25D,
                target.getBbWidth() * 0.35D,
                0.02D
            );
        }
    }
}
