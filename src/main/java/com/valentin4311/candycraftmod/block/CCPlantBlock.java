package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.CandyCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CCPlantBlock extends BushBlock {
    public static final ResourceLocation CANDY_DIRT_TAG = new ResourceLocation(CandyCraft.MODID, "candy_dirt");

    public CCPlantBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.create(CANDY_DIRT_TAG));
    }
}
