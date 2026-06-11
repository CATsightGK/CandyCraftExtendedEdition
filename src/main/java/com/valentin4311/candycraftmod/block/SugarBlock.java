package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCSweetscapeItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class SugarBlock extends Block {
    public SugarBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        boolean liquidCandy = player.getItemInHand(hand).is(CCSweetscapeItems.LIQUID_CANDY_BUCKET.get());
        if (!player.getItemInHand(hand).is(Items.LAVA_BUCKET) && !player.getItemInHand(hand).is(Items.FLINT_AND_STEEL) && !player.getItemInHand(hand).is(CCItems.CARAMEL_BUCKET.get()) && !liquidCandy) {
            return InteractionResult.PASS;
        }
        CandyPortalBlock portal = liquidCandy ? CCBlocks.LIQUID_CANDY_PORTAL.get() : CCBlocks.CANDY_PORTAL.get();
        for (BlockPos candidate : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (portal.trySpawnPortal(level, candidate.immutable())) {
                if (!player.getAbilities().instabuild && (player.getItemInHand(hand).is(Items.LAVA_BUCKET) || player.getItemInHand(hand).is(CCItems.CARAMEL_BUCKET.get()) || liquidCandy)) {
                    player.setItemInHand(hand, Items.BUCKET.getDefaultInstance());
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        if (player.getItemInHand(hand).is(Items.LAVA_BUCKET) || player.getItemInHand(hand).is(CCItems.CARAMEL_BUCKET.get()) || liquidCandy) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
}
