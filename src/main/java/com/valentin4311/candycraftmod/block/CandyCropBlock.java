package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import java.util.function.Supplier;

public class CandyCropBlock extends CropBlock {
    private final Supplier<Item> seed;

    public CandyCropBlock(BlockBehaviour.Properties properties) {
        this(() -> Items.WHEAT_SEEDS, properties);
    }

    public CandyCropBlock(Supplier<Item> seed, BlockBehaviour.Properties properties) {
        super(properties);
        this.seed = seed;
    }

    @Override
    protected Item getBaseSeedId() {
        return seed.get();
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(CCBlocks.CANDY_FARMLAND.get());
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        if (!isMaxAge(state)) {
            return super.use(state, level, pos, player, hand, hit);
        }

        if (level instanceof ServerLevel serverLevel) {
            boolean keptSeed = false;
            for (ItemStack generated : Block.getDrops(
                    state, serverLevel, pos, null, player, player.getItemInHand(hand))) {
                ItemStack drop = generated.copy();
                if (!keptSeed && drop.is(seed.get())) {
                    drop.shrink(1);
                    keptSeed = true;
                }
                popResource(serverLevel, pos, drop);
            }
            serverLevel.setBlock(pos, getStateForAge(0), Block.UPDATE_ALL);
            serverLevel.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            serverLevel.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 0.8F, 1.0F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
