package com.valentin4311.candycraftmod.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.ThrownForkBlockEntity;
import com.valentin4311.candycraftmod.entity.ThrownForkEntity;
import com.valentin4311.candycraftmod.inventory.tooltip.ForkHeldBlockTooltip;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCCriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;
import java.util.Optional;
import java.util.List;

public class ForkItem extends TieredItem {
    public static final String HELD_BLOCK_TAG = "CandyForkBlock";
    private static final String HELD_BLOCK_STATE_TAG = "CandyForkBlockState";
    private static final String EAT_BITES_TAG = "CandyForkEatBites";
    private static final String EAT_ANIMATION_TAG = "CandyForkEatAnimation";
    private static final String PENDING_BLOCK_TAG = "CandyForkPendingBlock";
    private static final int BITES_TO_EAT = 4;
    private static final int THROW_CHARGE_TICKS = 10;
    private static final int HELD_BLOCK_DURABILITY_COST = 3;
    public static final int EAT_ANIMATION_TICKS = 12;
    public static final TagKey<Block> FORK_EDIBLE = BlockTags.create(new ResourceLocation(CandyCraft.MODID, "fork_edible"));
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public ForkItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, properties);
        this.defaultModifiers = ImmutableMultimap.<Attribute, AttributeModifier>builder()
            .put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,
                "Weapon modifier", attackDamageModifier, AttributeModifier.Operation.ADDITION))
            .put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID,
                "Weapon modifier", attackSpeedModifier, AttributeModifier.Operation.ADDITION))
            .build();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new com.valentin4311.candycraftmod.client.ForkItemRenderer(
                        net.minecraft.client.Minecraft.getInstance().getBlockEntityRenderDispatcher()
                    );
                }
                return renderer;
            }

            @Override
            public net.minecraft.client.model.HumanoidModel.ArmPose getArmPose(
                    LivingEntity entity, InteractionHand hand, ItemStack stack) {
                return com.valentin4311.candycraftmod.client.ForkClientAnimations.getArmPose(stack);
            }

            @Override
            public boolean applyForgeHandTransform(com.mojang.blaze3d.vertex.PoseStack poseStack,
                    net.minecraft.client.player.LocalPlayer player, net.minecraft.world.entity.HumanoidArm arm,
                    ItemStack stack, float partialTick, float equipProcess, float swingProcess) {
                return com.valentin4311.candycraftmod.client.ForkClientAnimations.applyFirstPersonTransform(
                    poseStack, arm, stack, partialTick
                );
            }
        });
    }

    @Override
    public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, Player player) {
        if (!player.isShiftKeyDown()) {
            return false;
        }
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
        if (isEatAnimationPlaying(stack)) {
            return InteractionResult.FAIL;
        }
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return InteractionResult.FAIL;
        }

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (beginForkingBlock(stack, level, pos, player, context.getHand(), state)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        clearPendingBlock(stack);
        player.startUsingItem(context.getHand());
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isEatAnimationPlaying(stack)) {
            return InteractionResultHolder.fail(stack);
        }
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(stack);
        }
        clearPendingBlock(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player)) {
            return;
        }
        if (isEatAnimationPlaying(stack)) {
            return;
        }
        int usedTicks = getUseDuration(stack) - timeLeft;
        InteractionHand hand = entity.getUsedItemHand();
        if (hasHeldBlock(stack)) {
            clearPendingBlock(stack);
            if (usedTicks < THROW_CHARGE_TICKS) {
                startEatAnimation(stack);
                if (!level.isClientSide) {
                    eatOneBite(stack, player, hand);
                }
            } else if (!level.isClientSide) {
                throwHeldBlock(stack, player, hand);
            }
            return;
        }

        if (usedTicks < THROW_CHARGE_TICKS) {
            BlockPos pendingPos = getPendingBlock(stack);
            if (!level.isClientSide && pendingPos != null) {
                BlockState pendingState = level.getBlockState(pendingPos);
                tryPickUpBlock(stack, level, pendingPos, player, pendingState);
            }
            clearPendingBlock(stack);
            return;
        }

        clearPendingBlock(stack);
        if (!level.isClientSide) {
            ItemStack thrownStack = stack.copy();
            thrownStack.setCount(1);
            stack.hurtAndBreak(1, player, living -> living.broadcastBreakEvent(hand));
            ThrownForkEntity fork = new ThrownForkEntity(level, player, thrownStack);
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
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.candycraftmod.fork.pick_up").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.candycraftmod.fork.use").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return getTier().getEnchantmentValue();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.MENDING;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        BlockState state = getHeldBlockState(stack);
        return state.isAir() ? Optional.empty() : Optional.of(new ForkHeldBlockTooltip(state));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(EAT_ANIMATION_TAG)) {
            return;
        }
        int remaining = tag.getInt(EAT_ANIMATION_TAG) - 1;
        if (remaining > 0) {
            tag.putInt(EAT_ANIMATION_TAG, remaining);
        } else {
            tag.remove(EAT_ANIMATION_TAG);
            if (tag.isEmpty()) {
                stack.setTag(null);
            }
        }
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
        if (tag.contains(HELD_BLOCK_STATE_TAG, Tag.TAG_COMPOUND)) {
            return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound(HELD_BLOCK_STATE_TAG));
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

    public static int getEatAnimationTicks(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : Math.max(0, tag.getInt(EAT_ANIMATION_TAG));
    }

    public static boolean isEatAnimationPlaying(ItemStack stack) {
        return getEatAnimationTicks(stack) > 0;
    }

    public static boolean tryPickUpBlock(ItemStack stack, Level level, BlockPos pos, Player player, BlockState state) {
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1
                || !player.isShiftKeyDown()
                || hasHeldBlock(stack)
                || !player.mayBuild()
                || player.getCooldowns().isOnCooldown(stack.getItem())
                || !canForkEat(state)) {
            return false;
        }
        if (!level.isClientSide) {
            pickUpBlock(stack, level, pos, player, state);
        }
        return true;
    }

    public static boolean beginForkingBlock(ItemStack stack, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockState state) {
        if (isEatAnimationPlaying(stack)
                || stack.getDamageValue() >= stack.getMaxDamage() - 1
                || !player.isShiftKeyDown()
                || hasHeldBlock(stack)
                || !player.mayBuild()
                || player.getCooldowns().isOnCooldown(stack.getItem())
                || !canForkEat(state)) {
            return false;
        }
        stack.getOrCreateTag().putLong(PENDING_BLOCK_TAG, pos.asLong());
        player.startUsingItem(hand);
        return true;
    }

    private static void eatOneBite(ItemStack stack, Player player, InteractionHand hand) {
        int bites = getEatBites(stack) + 1;
        stack.getOrCreateTag().putInt(EAT_BITES_TAG, bites);
        player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.45F, 0.9F + player.getRandom().nextFloat() * 0.2F);
        spawnHeldBlockParticles(stack, player, hand, 10);
        if (bites >= BITES_TO_EAT) {
            finishEating(stack, player, hand);
        }
    }

    private static void startEatAnimation(ItemStack stack) {
        stack.getOrCreateTag().putInt(EAT_ANIMATION_TAG, EAT_ANIMATION_TICKS);
    }

    private static void throwHeldBlock(ItemStack stack, Player player, InteractionHand hand) {
        BlockState state = getHeldBlockState(stack);
        boolean shattersOnImpact = getEatBites(stack) > 0;
        clearHeldBlock(stack);
        if (state.isAir()) {
            return;
        }

        ThrownForkBlockEntity projectile = new ThrownForkBlockEntity(
            player.level(), player, state, shattersOnImpact
        );
        projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 0.25F);
        player.level().addFreshEntity(projectile);
        player.level().playSound(null, projectile, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.8F, 1.15F);
        if (!player.getAbilities().instabuild) {
            stack.hurtAndBreak(HELD_BLOCK_DURABILITY_COST, player, living -> living.broadcastBreakEvent(hand));
        }
    }

    private static void finishEating(ItemStack stack, Player player, InteractionHand hand) {
        if (player.level() instanceof ServerLevel) {
            spawnHeldBlockParticles(stack, player, hand, 18);
            player.getFoodData().eat(1, 0.0F);
            if (player instanceof ServerPlayer serverPlayer) {
                CCCriteriaTriggers.EAT_BLOCK.trigger(serverPlayer);
            }
            player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, 0.9F + player.getRandom().nextFloat() * 0.1F);
            if (!player.getAbilities().instabuild) {
                stack.hurtAndBreak(HELD_BLOCK_DURABILITY_COST, player, living -> living.broadcastBreakEvent(hand));
            }
        }
        clearHeldBlock(stack);
        player.getCooldowns().addCooldown(stack.getItem(), 8);
    }

    private static void spawnHeldBlockParticles(ItemStack stack, Player player, InteractionHand hand, int count) {
        BlockState state = getHeldBlockState(stack);
        if (state.isAir() || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Vec3 view = player.getViewVector(1.0F);
        Vec3 horizontalRight = new Vec3(view.z, 0.0D, -view.x);
        if (horizontalRight.lengthSqr() > 1.0E-6D) {
            horizontalRight = horizontalRight.normalize();
        }
        HumanoidArm usedArm = hand == InteractionHand.MAIN_HAND
            ? player.getMainArm()
            : player.getMainArm().getOpposite();
        double handSide = usedArm == HumanoidArm.RIGHT ? 1.0D : -1.0D;
        Vec3 particlePos = player.getEyePosition()
            .add(view.scale(0.42D))
            .add(horizontalRight.scale(0.08D * handSide))
            .add(0.0D, -0.12D, 0.0D);
        serverLevel.sendParticles(
            new BlockParticleOption(ParticleTypes.BLOCK, state),
            particlePos.x,
            particlePos.y,
            particlePos.z,
            count,
            0.12D,
            0.06D,
            0.12D,
            0.025D
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
        removeForkedBlock(serverLevel, pos, player, state);
        level.playSound(null, pos, state.getSoundType(level, pos, player).getBreakSound(), SoundSource.BLOCKS, 0.6F, 1.15F);
        player.getCooldowns().addCooldown(stack.getItem(), 4);
    }

    private static void removeForkedBlock(ServerLevel level, BlockPos pos, Player player, BlockState state) {
        if (state.getBlock() instanceof DoorBlock) {
            BlockPos lowerPos = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
            BlockPos upperPos = lowerPos.above();
            BlockState lowerState = level.getBlockState(lowerPos);
            BlockState upperState = level.getBlockState(upperPos);
            emitForkBreak(level, lowerPos, player, lowerState);
            emitForkBreak(level, upperPos, player, upperState);
            level.removeBlock(upperPos, false);
            level.removeBlock(lowerPos, false);
            return;
        }

        emitForkBreak(level, pos, player, state);
        level.removeBlock(pos, false);
    }

    private static void emitForkBreak(ServerLevel level, BlockPos pos, Player player, BlockState state) {
        if (state.isAir()) {
            return;
        }
        level.levelEvent(2001, pos, Block.getId(state));
        level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
    }

    private static void storeHeldBlock(ItemStack stack, BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        stack.getOrCreateTag().putString(HELD_BLOCK_TAG, id.toString());
        stack.getOrCreateTag().put(HELD_BLOCK_STATE_TAG, NbtUtils.writeBlockState(state));
    }

    public static void clearHeldBlock(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(HELD_BLOCK_TAG);
        tag.remove(HELD_BLOCK_STATE_TAG);
        tag.remove(EAT_BITES_TAG);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    private static BlockPos getPendingBlock(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(PENDING_BLOCK_TAG) ? BlockPos.of(tag.getLong(PENDING_BLOCK_TAG)) : null;
    }

    private static void clearPendingBlock(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(PENDING_BLOCK_TAG);
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
