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
    private static final String TAP_CHARGE_TAG = "CandyCraftJellyWandTapCharge";
    private static final int MAX_USES = 99;
    private static final int RECHARGE_USES = 11;
    private static final int TAP_CHARGES = 5;
    private static final int AIM_MODE_TICKS = 10;
    private static final int RED_JELLY_CHARGE_TICKS = 30;

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

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        int usedTicks = getUseDuration(stack) - timeLeft;
        if (getUses(stack) <= 0) {
            return;
        }

        if (usedTicks < AIM_MODE_TICKS) {
            if (entity instanceof Player player) {
                addTapCharge(stack, level, player);
            }
            return;
        }

        fireRedJellyShot(stack, level, entity, usedTicks);
    }

    private void addTapCharge(ItemStack stack, Level level, Player player) {
        int charge = Math.min(TAP_CHARGES, getTapCharge(stack) + 1);
        setTapCharge(stack, charge);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS, 0.45F, 0.9F + charge * 0.12F);
        if (charge < TAP_CHARGES) {
            return;
        }

        setTapCharge(stack, 0);
        fireScatterShot(stack, level, player);
    }

    private void fireScatterShot(ItemStack stack, Level level, LivingEntity entity) {
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
        consumeUse(stack, entity);
        if (entity instanceof Player player) {
            player.awardStat(Stats.ITEM_USED.get(this));
        }
    }

    private void fireRedJellyShot(ItemStack stack, Level level, LivingEntity entity, int usedTicks) {
        setTapCharge(stack, 0);
        if (!level.isClientSide) {
            float charge = Mth.clamp((usedTicks - AIM_MODE_TICKS) / (float)(RED_JELLY_CHARGE_TICKS - AIM_MODE_TICKS), 0.0F, 1.0F);
            GummyBallEntity ball = new GummyBallEntity(level, entity, 4);
            ball.setVisualVariant(GummyBallEntity.RED_JELLY_VISUAL);
            ball.setBonusDamage(5.0F + charge * 4.0F);
            ball.shootFromRotation(entity, entity.getXRot(), entity.getYRot(), 0.0F, 2.1F + charge * 0.25F, 0.5F);
            level.addFreshEntity(ball);
        }

        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SLIME_SQUISH, SoundSource.PLAYERS, 0.9F, 0.75F + level.getRandom().nextFloat() * 0.2F);
        consumeUse(stack, entity);
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

    public static int getUses(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(USES_TAG)) {
            return MAX_USES;
        }
        return Mth.clamp(tag.getInt(USES_TAG), 0, MAX_USES);
    }

    public static int getTapCharge(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null ? Mth.clamp(tag.getInt(TAP_CHARGE_TAG), 0, TAP_CHARGES) : 0;
    }

    public static float getTapChargeProgress(ItemStack stack) {
        return getTapCharge(stack) / (float)TAP_CHARGES;
    }

    public static float getAimProgress(Player player) {
        ItemStack stack = player.getUseItem();
        if (!player.isUsingItem() || !stack.is(CCItems.JELLY_WAND.get())) {
            return 0.0F;
        }
        int usedTicks = stack.getUseDuration() - player.getUseItemRemainingTicks();
        return usedTicks > 0
            ? Mth.clamp(usedTicks / (float)RED_JELLY_CHARGE_TICKS, 0.0F, 1.0F)
            : 0.0F;
    }

    public static boolean isAiming(Player player) {
        return getAimProgress(player) > 0.0F;
    }

    private static void setUses(ItemStack stack, int uses) {
        stack.getOrCreateTag().putInt(USES_TAG, Mth.clamp(uses, 0, MAX_USES));
    }

    private static void setTapCharge(ItemStack stack, int charge) {
        CompoundTag tag = stack.getOrCreateTag();
        if (charge <= 0) {
            tag.remove(TAP_CHARGE_TAG);
        } else {
            tag.putInt(TAP_CHARGE_TAG, Mth.clamp(charge, 0, TAP_CHARGES));
        }
    }

    private static void consumeUse(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player && (player.getAbilities().instabuild || player.isSpectator())) {
            return;
        }
        setUses(stack, getUses(stack) - 1);
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
