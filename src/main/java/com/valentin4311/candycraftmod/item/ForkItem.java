package com.valentin4311.candycraftmod.item;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.ThrownForkEntity;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class ForkItem extends TieredItem {
    public static final String HELD_BLOCK_TAG = "CandyForkBlock";
    private static final String EAT_BITES_TAG = "CandyForkEatBites";
    private static final int BITES_TO_EAT = 4;
    private static final int THROW_CHARGE_TICKS = 10;
    private static final float BLOCK_THROW_BONUS_DAMAGE = 5.0F;
    public static final TagKey<Block> FORK_EDIBLE = BlockTags.create(new ResourceLocation(CandyCraft.MODID, "fork_edible"));

    public ForkItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        Level level = player.level();
        BlockState state = level.getBlockState(pos);
        if (!canForkEat(state)) {
            return false;
        }
        if (hasHeldBlock(stack) || player.getCooldowns().isOnCooldown(this)) {
            return true;
        }
        pickUpBlock(stack, level, pos, player, state);
        return true;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return InteractionResult.FAIL;
        }

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!hasHeldBlock(stack) && canForkEat(state) && !player.getCooldowns().isOnCooldown(this)) {
            pickUpBlock(stack, level, pos, player, state);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        player.startUsingItem(context.getHand());
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }
        int usedTicks = getUseDuration(stack) - timeLeft;
        if (usedTicks < THROW_CHARGE_TICKS) {
            if (hasHeldBlock(stack) && !level.isClientSide) {
                eatOneBite(stack, player, entity.getUsedItemHand());
            }
            return;
        }

        if (!level.isClientSide) {
            boolean hadBlock = hasHeldBlock(stack);
            if (hadBlock) {
                spawnHeldBlockParticles(stack, player, 24);
            }
            ItemStack thrownStack = stack.copy();
            thrownStack.setCount(1);
            if (hadBlock) {
                clearHeldBlock(thrownStack);
                clearHeldBlock(stack);
            }
            stack.hurtAndBreak(1, player, living -> living.broadcastBreakEvent(entity.getUsedItemHand()));
            ThrownForkEntity fork = new ThrownForkEntity(level, player, thrownStack);
            if (hadBlock) {
                fork.setBonusDamage(BLOCK_THROW_BONUS_DAMAGE);
            }
            fork.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
            if (player.getAbilities().instabuild) {
                fork.pickup = net.minecraft.world.entity.projectile.AbstractArrow.Pickup.CREATIVE_ONLY;
            }
            level.addFreshEntity(fork);
            level.playSound(null, fork, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    public static boolean hasHeldBlock(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(HELD_BLOCK_TAG);
    }

    public static BlockState getHeldBlockState(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(HELD_BLOCK_TAG)) {
            return Blocks.AIR.defaultBlockState();
        }
        ResourceLocation id = ResourceLocation.tryParse(tag.getString(HELD_BLOCK_TAG));
        if (id == null) {
            return Blocks.AIR.defaultBlockState();
        }
        return BuiltInRegistries.BLOCK.getOptional(id)
            .map(Block::defaultBlockState)
            .orElse(Blocks.AIR.defaultBlockState());
    }

    public static float getEatProgress(ItemStack stack) {
        return Math.min(1.0F, getEatBites(stack) / (float) BITES_TO_EAT);
    }

    private static void eatOneBite(ItemStack stack, Player player, InteractionHand hand) {
        int bites = getEatBites(stack) + 1;
        stack.getOrCreateTag().putInt(EAT_BITES_TAG, bites);
        player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.45F, 0.9F + player.getRandom().nextFloat() * 0.2F);
        spawnHeldBlockParticles(stack, player, 10);
        if (bites >= BITES_TO_EAT) {
            finishEating(stack, player, hand);
        }
    }

    private static void finishEating(ItemStack stack, Player player, InteractionHand hand) {
        if (player.level() instanceof ServerLevel) {
            spawnHeldBlockParticles(stack, player, 18);
            player.getFoodData().eat(1, 0.0F);
            player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, 0.9F + player.getRandom().nextFloat() * 0.1F);
            if (!player.getAbilities().instabuild) {
                stack.hurtAndBreak(1, player, living -> living.broadcastBreakEvent(hand));
            }
        }
        clearHeldBlock(stack);
        player.getCooldowns().addCooldown(stack.getItem(), 8);
    }

    private static void spawnHeldBlockParticles(ItemStack stack, Player player, int count) {
        BlockState state = getHeldBlockState(stack);
        if (state.isAir() || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(
            new BlockParticleOption(ParticleTypes.BLOCK, state),
            player.getX(),
            player.getEyeY() - 0.25D,
            player.getZ(),
            count,
            0.22D,
            0.18D,
            0.22D,
            0.045D
        );
    }

    private static boolean canForkEat(BlockState state) {
        return state.is(FORK_EDIBLE)
            && !state.is(CCBlocks.JAW_BREAKER_BLOCK.get())
            && !state.is(CCBlocks.JAW_BREAKER_LIGHT.get());
    }

    private static void pickUpBlock(ItemStack stack, Level level, BlockPos pos, Player player, BlockState state) {
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel) || !player.mayBuild()) {
            return;
        }
        storeHeldBlock(stack, state);
        resetEatBites(stack);
        serverLevel.levelEvent(2001, pos, Block.getId(state));
        serverLevel.removeBlock(pos, false);
        serverLevel.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
        level.playSound(null, pos, state.getSoundType(level, pos, player).getBreakSound(), SoundSource.BLOCKS, 0.6F, 1.15F);
        player.getCooldowns().addCooldown(stack.getItem(), 4);
    }

    private static void storeHeldBlock(ItemStack stack, BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        stack.getOrCreateTag().putString(HELD_BLOCK_TAG, id.toString());
    }

    public static void clearHeldBlock(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(HELD_BLOCK_TAG);
        tag.remove(EAT_BITES_TAG);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    private static void resetEatBites(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.putInt(EAT_BITES_TAG, 0);
        }
    }

    private static int getEatBites(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(EAT_BITES_TAG);
    }
}
