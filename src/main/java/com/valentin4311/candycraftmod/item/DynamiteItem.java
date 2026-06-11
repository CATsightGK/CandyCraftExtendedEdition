package com.valentin4311.candycraftmod.item;

import com.valentin4311.candycraftmod.entity.DynamiteEntity;
import com.valentin4311.candycraftmod.entity.GlueDynamiteEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public class DynamiteItem extends Item {
    private static final int MIN_THROW_TICKS = 15;
    private static final int HAND_EXPLOSION_TICKS = 80;
    public static final int MID_STAGE_TICKS = 60;
    private final boolean glue;

    public DynamiteItem(Properties properties, boolean glue) {
        super(properties);
        this.glue = glue;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        int usedTicks = getUseDuration(stack) - remainingUseDuration;
        if (level.isClientSide && entity.getRandom().nextInt(10) == 0) {
            level.addParticle(ParticleTypes.SMOKE,
                entity.getX() + (entity.getRandom().nextDouble() - 0.5D) * 0.4D,
                entity.getEyeY() + 0.4D,
                entity.getZ() + (entity.getRandom().nextDouble() - 0.5D) * 0.4D,
                0.0D, 0.1D, 0.0D);
        }

        if (usedTicks == HAND_EXPLOSION_TICKS && entity instanceof Player player) {
            if (!level.isClientSide) {
                boolean mobGriefing = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
                level.explode(null, player.getX(), player.getY(), player.getZ(), 3.0F,
                    mobGriefing ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            player.stopUsingItem();
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }

        int usedTicks = getUseDuration(stack) - timeLeft;
        if (usedTicks <= MIN_THROW_TICKS || usedTicks >= HAND_EXPLOSION_TICKS) {
            return;
        }

        level.playSound(null, player.getX() + 0.5D, player.getY() + 0.5D, player.getZ() + 0.5D,
            SoundEvents.CREEPER_PRIMED, SoundSource.PLAYERS, 0.5F,
            0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!level.isClientSide) {
            DynamiteEntity dynamite = glue ? new GlueDynamiteEntity(level, player) : new DynamiteEntity(level, player);
            dynamite.setItem(stack.copyWithCount(1));
            dynamite.setFuse(HAND_EXPLOSION_TICKS - usedTicks);
            dynamite.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(dynamite);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    public static float modelStage(ItemStack stack, LivingEntity entity) {
        if (entity == null || entity.getUseItem() != stack) {
            return 0.0F;
        }
        int usedTicks = stack.getUseDuration() - entity.getUseItemRemainingTicks();
        if (usedTicks >= MID_STAGE_TICKS) {
            return 2.0F;
        }
        if (usedTicks > MIN_THROW_TICKS) {
            return 1.0F;
        }
        return 0.0F;
    }
}
