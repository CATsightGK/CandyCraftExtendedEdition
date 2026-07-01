package com.valentin4311.candycraftmod.item;

import com.valentin4311.candycraftmod.block.CherryBlock;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class CandiedCherryItem extends Item {
    public CandiedCherryItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction face = context.getClickedFace();
        if (face == Direction.UP) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos supportPos = context.getClickedPos();
        BlockState supportState = level.getBlockState(supportPos);
        boolean hanging = face == Direction.DOWN && CherryBlock.isValidLeafSupport(supportState);
        boolean sideMounted = face.getAxis().isHorizontal() && CherryBlock.isValidSideSupport(supportState);
        if (!hanging && !sideMounted) {
            return InteractionResult.PASS;
        }

        BlockPos placePos = supportPos.relative(face);
        BlockState state = CCBlocks.CHERRY_BLOCK.get().defaultBlockState().setValue(CherryBlock.FACING, hanging ? Direction.UP : face.getOpposite());
        if (!level.getBlockState(placePos).canBeReplaced() || !state.canSurvive(level, placePos)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            level.setBlock(placePos, state, 11);
            SoundType sound = state.getSoundType(level, placePos, context.getPlayer());
            level.playSound(null, placePos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
