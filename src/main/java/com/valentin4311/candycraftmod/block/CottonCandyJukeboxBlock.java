package com.valentin4311.candycraftmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;

public class CottonCandyJukeboxBlock extends JukeboxBlock {
    public CottonCandyJukeboxBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(HAS_RECORD)) {
            return super.use(state, level, pos, player, hand, hit);
        }

        ItemStack held = player.getItemInHand(hand);
        if (!held.is(ItemTags.MUSIC_DISCS)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            if (!(level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox)) {
                return InteractionResult.FAIL;
            }
            ItemStack record = held.copy();
            record.setCount(1);
            jukebox.setFirstItem(record);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, state));
            held.shrink(1);
            player.awardStat(Stats.PLAY_RECORD);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
