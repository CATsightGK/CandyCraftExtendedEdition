package com.valentin4311.candycraftmod.item;

import com.valentin4311.candycraftmod.registry.CCItems;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class JumpWandItem extends Item {
    private static final String USES_TAG = "CandyCraftJumpWandUses";
    private static final int MAX_USES = 10;
    private static final int RECHARGE_USES = 1;
    private static final int CHARGE_TICKS = 30;

    public JumpWandItem(Properties properties) {
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
        if (getUses(stack) <= 0) {
            return;
        }
        int usedTicks = getUseDuration(stack) - timeLeft;
        float charge = Mth.clamp(usedTicks / (float)CHARGE_TICKS, 0.15F, 1.0F);
        if (!level.isClientSide) {
            launch(entity, charge);
        }
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SLIME_JUMP, SoundSource.PLAYERS, 0.9F, 0.75F + charge * 0.35F);
        consumeUse(stack, entity);
        if (entity instanceof Player player) {
            player.awardStat(Stats.ITEM_USED.get(this));
        }
    }

    private static void launch(LivingEntity entity, float charge) {
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.45D, 0.0D, 0.45D).add(0.0D, 0.95D + 1.65D * charge, 0.0D));
        entity.hurtMarked = true;
        entity.hasImpulse = true;
        entity.fallDistance = 0.0F;
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
        return Mth.hsvToRgb(0.58F, 0.7F, 1.0F);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(CCItems.GUMMY_BALL.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.candycraftmod.wand_uses", getUses(stack), MAX_USES).withStyle(ChatFormatting.GRAY));
    }

    public static int getUses(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(USES_TAG)) {
            return MAX_USES;
        }
        return Mth.clamp(tag.getInt(USES_TAG), 0, MAX_USES);
    }

    public static float getChargeProgress(Player player) {
        ItemStack stack = player.getUseItem();
        if (!player.isUsingItem() || !stack.is(CCItems.JUMP_WAND.get())) {
            return 0.0F;
        }
        int usedTicks = stack.getUseDuration() - player.getUseItemRemainingTicks();
        return usedTicks > 0 ? Mth.clamp(usedTicks / (float)CHARGE_TICKS, 0.0F, 1.0F) : 0.0F;
    }

    private static void setUses(ItemStack stack, int uses) {
        stack.getOrCreateTag().putInt(USES_TAG, Mth.clamp(uses, 0, MAX_USES));
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
