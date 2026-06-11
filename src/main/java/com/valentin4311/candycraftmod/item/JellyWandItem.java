package com.valentin4311.candycraftmod.item;

import com.valentin4311.candycraftmod.entity.GummyBallEntity;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class JellyWandItem extends Item {
    private static final String USES_TAG = "CandyCraftJellyWandUses";
    private static final String CHARGE_BONUS_TAG = "CandyCraftJellyWandChargeBonus";
    private static final int MAX_USES = 99;
    private static final int RECHARGE_USES = 11;
    private static final int FULL_CHARGE_TICKS = 70;
    private static final int LEFT_CLICK_BONUS_TICKS = 8;

    public JellyWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if ((player.isShiftKeyDown() || getUses(stack) <= 0) && tryRecharge(player, stack)) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.4F);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        if (getUses(stack) <= 0) {
            return InteractionResultHolder.fail(stack);
        }

        stack.getOrCreateTag().putInt(CHARGE_BONUS_TAG, 0);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        int usedTicks = getUseDuration(stack) - timeLeft + getChargeBonus(stack);
        stack.getOrCreateTag().remove(CHARGE_BONUS_TAG);
        if (usedTicks < FULL_CHARGE_TICKS || getUses(stack) <= 0) {
            return;
        }

        if (!level.isClientSide) {
            int count = 3 + level.getRandom().nextInt(2);
            for (int i = 0; i < count; i++) {
                GummyBallEntity ball = new GummyBallEntity(level, entity, 3);
                ball.setVisualVariant(level.getRandom().nextInt(3));
                float yawOffset = (i - (count - 1) * 0.5F) * 6.5F + (level.getRandom().nextFloat() - 0.5F) * 2.5F;
                float pitchOffset = (level.getRandom().nextFloat() - 0.5F) * 2.0F;
                ball.shootFromRotation(entity, entity.getXRot() + pitchOffset, entity.getYRot() + yawOffset, 0.0F, 1.75F, 2.5F);
                level.addFreshEntity(ball);
            }
        }

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.8F, 0.85F + level.getRandom().nextFloat() * 0.25F);
        setUses(stack, getUses(stack) - 1);
        if (entity instanceof Player player) {
            player.awardStat(Stats.ITEM_USED.get(this));
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getUses(stack) < MAX_USES;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getUses(stack) / (float)MAX_USES);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(0.88F, 0.65F, 1.0F);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(CCItems.GUMMY_BALL.get());
    }

    public static void accelerateCharge(Player player) {
        if (!player.isUsingItem()) {
            return;
        }
        ItemStack stack = player.getUseItem();
        if (!stack.is(CCItems.JELLY_WAND.get())) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        int bonus = Math.min(FULL_CHARGE_TICKS, tag.getInt(CHARGE_BONUS_TAG) + LEFT_CLICK_BONUS_TICKS);
        tag.putInt(CHARGE_BONUS_TAG, bonus);
    }

    public static int getUses(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(USES_TAG)) {
            return MAX_USES;
        }
        return Mth.clamp(tag.getInt(USES_TAG), 0, MAX_USES);
    }

    private static void setUses(ItemStack stack, int uses) {
        stack.getOrCreateTag().putInt(USES_TAG, Mth.clamp(uses, 0, MAX_USES));
    }

    private static int getChargeBonus(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? tag.getInt(CHARGE_BONUS_TAG) : 0;
    }

    private static boolean tryRecharge(Player player, ItemStack wand) {
        if (getUses(wand) >= MAX_USES) {
            return false;
        }
        if (!player.getAbilities().instabuild) {
            ItemStack gummy = findGummyBall(player);
            if (gummy.isEmpty()) {
                return false;
            }
            gummy.shrink(1);
        }
        setUses(wand, getUses(wand) + RECHARGE_USES);
        return true;
    }

    private static ItemStack findGummyBall(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(CCItems.GUMMY_BALL.get())) {
                return stack;
            }
        }
        ItemStack offhand = player.getOffhandItem();
        return offhand.is(CCItems.GUMMY_BALL.get()) ? offhand : ItemStack.EMPTY;
    }
}
