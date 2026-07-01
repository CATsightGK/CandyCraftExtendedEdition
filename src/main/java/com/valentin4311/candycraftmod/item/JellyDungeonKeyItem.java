package com.valentin4311.candycraftmod.item;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.block.DungeonTeleporterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class JellyDungeonKeyItem extends Item {
    private final boolean suguardDungeon;

    public JellyDungeonKeyItem(Properties properties) {
        this(properties, false);
    }

    public JellyDungeonKeyItem(Properties properties, boolean suguardDungeon) {
        super(properties);
        this.suguardDungeon = suguardDungeon;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.FAIL;
        }

        BlockPos supportPos = context.getClickedPos();
        if (!level.getBlockState(supportPos).isSolid()) {
            return InteractionResult.FAIL;
        }

        BlockPos pos = supportPos.above();
        BlockState state = level.getBlockState(pos);
        if (!state.canBeReplaced()) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide) {
            level.setBlock(pos, CCBlocks.BLOCK_TELEPORTER.get().defaultBlockState(), Block.UPDATE_ALL);
            if (suguardDungeon) {
                DungeonTeleporterBlock.markSuguard(level, pos);
            }
            level.playSound(null, pos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 1.0F, 1.25F);
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(Component.translatable("chat.generating"), true);
            }
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
