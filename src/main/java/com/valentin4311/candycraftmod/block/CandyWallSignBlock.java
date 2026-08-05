package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.block.entity.CandySignBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

public class CandyWallSignBlock extends WallSignBlock {
    public CandyWallSignBlock(BlockBehaviour.Properties properties, WoodType woodType) {
        super(properties, woodType);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CandySignBlockEntity(pos, state);
    }
}
