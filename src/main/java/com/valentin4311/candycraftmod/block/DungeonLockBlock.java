package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class DungeonLockBlock extends Block {
    private final Kind kind;

    public DungeonLockBlock(Kind kind, BlockBehaviour.Properties properties) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (!isMatchingKey(stack)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        openDoor(level, pos);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, pos, SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1.0F, 0.8F);
        return InteractionResult.CONSUME;
    }

    private boolean isMatchingKey(ItemStack stack) {
        Item item = switch (kind) {
            case JELLY_SENTRY -> CCItems.JELLY_SENTRY_KEY.get();
            case JELLY_BOSS -> CCItems.JELLY_BOSS_KEY.get();
            case SUGUARD_SENTRY -> CCItems.SUGUARD_SENTRY_KEY.get();
            case SUGUARD_BOSS -> CCItems.SUGUARD_BOSS_KEY.get();
        };
        return stack.is(item);
    }

    private static void openDoor(Level level, BlockPos pos) {
        for (int dy = -2; dy <= 2; dy++) {
            BlockPos lockPos = pos.offset(0, dy, 0);
            if (level.getBlockState(lockPos).getBlock() instanceof DungeonLockBlock) {
                level.setBlock(lockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }

        clearDoorPlane(level, pos, true);
        clearDoorPlane(level, pos, false);
    }

    private static void clearDoorPlane(Level level, BlockPos center, boolean alongX) {
        for (int horizontal = -2; horizontal <= 2; horizontal++) {
            for (int dy = -1; dy <= 3; dy++) {
                BlockPos pos = alongX ? center.offset(horizontal, dy, 0) : center.offset(0, dy, horizontal);
                BlockState state = level.getBlockState(pos);
                if (isDoorBlock(state)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }

    private static boolean isDoorBlock(BlockState state) {
        return state.is(CCBlocks.JAW_BREAKER_BLOCK.get())
            || state.is(CCBlocks.JAW_BREAKER_LIGHT.get())
            || state.getBlock() instanceof DungeonLockBlock;
    }

    public enum Kind {
        JELLY_SENTRY,
        JELLY_BOSS,
        SUGUARD_SENTRY,
        SUGUARD_BOSS
    }
}
