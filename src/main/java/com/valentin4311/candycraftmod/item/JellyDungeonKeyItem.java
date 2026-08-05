package com.valentin4311.candycraftmod.item;

import com.valentin4311.candycraftmod.block.DungeonTeleporterBlock;
import com.valentin4311.candycraftmod.block.DungeonTeleporterBlock.DungeonKind;
import com.valentin4311.candycraftmod.block.DungeonTeleporterBlock.PortalRole;
import com.valentin4311.candycraftmod.world.CCDimensions;
import com.valentin4311.candycraftmod.world.DungeonProgressData;
import com.valentin4311.candycraftmod.world.DungeonProgressData.Instance;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class JellyDungeonKeyItem extends Item {
    private static final String OWNER_TAG = "CandyCraftDungeonKeyOwner";
    private static final String INSTANCE_TAG = "CandyCraftDungeonKeyInstance";
    private static final String COMPLETIONS_TAG = "CandyCraftDungeonKeyCompletions";
    private static final String ACTIVE_TAG = "CandyCraftDungeonKeyActive";
    private static final String EXHAUSTED_TAG = "CandyCraftDungeonKeyExhausted";
    private final DungeonKind kind;

    public JellyDungeonKeyItem(Properties properties) {
        this(properties, false);
    }

    public JellyDungeonKeyItem(Properties properties, boolean suguardDungeon) {
        super(properties);
        this.kind = suguardDungeon ? DungeonKind.SUGUARD : DungeonKind.JELLY;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (context.getClickedFace() != net.minecraft.core.Direction.UP || context.getPlayer() == null) {
            return InteractionResult.FAIL;
        }
        BlockPos supportPos = context.getClickedPos();
        if (!level.getBlockState(supportPos).isSolid()) {
            return InteractionResult.FAIL;
        }
        BlockPos pos = supportPos.above();
        BlockState replaced = level.getBlockState(pos);
        if (!replaced.canBeReplaced()) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ServerPlayer player = (ServerPlayer)context.getPlayer();
        ItemStack stack = context.getItemInHand();
        refreshState(stack, player);
        if (stack.getOrCreateTag().getBoolean(EXHAUSTED_TAG)) {
            player.displayClientMessage(Component.translatable("message.candycraftmod.dungeon.key_exhausted"), true);
            return InteractionResult.FAIL;
        }

        ServerLevel dungeonLevel = player.server.getLevel(
            kind == DungeonKind.JELLY ? CCDimensions.JELLY_DUNGEON : CCDimensions.SUGUARD_DUNGEON);
        if (dungeonLevel == null) {
            return InteractionResult.FAIL;
        }
        DungeonProgressData data = DungeonProgressData.get(player.server);
        Instance active = data.getActive(player.getUUID(), kind);
        if (active != null && active.generated()) {
            player.displayClientMessage(Component.translatable("message.candycraftmod.dungeon.active_loaded"), true);
            return InteractionResult.FAIL;
        }
        Instance instance = data.getOrCreate(player, kind);
        bind(stack, player, instance, data.getCompletionCount(player.getUUID(), kind));
        level.setBlock(pos, DungeonTeleporterBlock.state(kind, PortalRole.ENTRY), Block.UPDATE_ALL);
        data.registerPortal((ServerLevel)level, pos, player.getUUID(), kind, instance.id());
        level.playSound(null, pos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 1.0F, 1.25F);
        return InteractionResult.CONSUME;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (!level.isClientSide && entity instanceof ServerPlayer player && player.tickCount % 10 == 0) {
            refreshState(stack, player);
        }
    }

    private void refreshState(ItemStack stack, ServerPlayer player) {
        DungeonProgressData data = DungeonProgressData.get(player.server);
        CompoundTag tag = stack.getOrCreateTag();
        Instance active = data.getActive(player.getUUID(), kind);
        boolean bound = tag.contains(INSTANCE_TAG);
        boolean exhausted = bound && (active == null || active.id() != tag.getLong(INSTANCE_TAG));
        tag.putInt(COMPLETIONS_TAG, data.getCompletionCount(player.getUUID(), kind));
        tag.putBoolean(ACTIVE_TAG, active != null);
        tag.putBoolean(EXHAUSTED_TAG, exhausted);
    }

    private static void bind(ItemStack stack, ServerPlayer player, Instance instance, int completions) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID(OWNER_TAG, player.getUUID());
        tag.putLong(INSTANCE_TAG, instance.id());
        tag.putInt(COMPLETIONS_TAG, completions);
        tag.putBoolean(ACTIVE_TAG, true);
        tag.putBoolean(EXHAUSTED_TAG, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        int completions = tag == null ? 0 : tag.getInt(COMPLETIONS_TAG);
        tooltip.add(Component.translatable(
            "tooltip.candycraftmod.dungeon_key.progress." + kind.getSerializedName(), completions
        ).withStyle(ChatFormatting.GRAY));
        if (tag != null && tag.getBoolean(EXHAUSTED_TAG)) {
            tooltip.add(Component.translatable("tooltip.candycraftmod.dungeon_key.exhausted").withStyle(ChatFormatting.RED));
        } else if (tag != null && tag.getBoolean(ACTIVE_TAG)) {
            tooltip.add(Component.translatable("tooltip.candycraftmod.dungeon_key.active").withStyle(ChatFormatting.YELLOW));
        }
    }
}
