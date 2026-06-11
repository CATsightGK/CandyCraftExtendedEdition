package com.valentin4311.candycraftmod.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class LegacyTypeBlock extends Block {
    public static final IntegerProperty TYPE = IntegerProperty.create("type", 0, 3);

    public LegacyTypeBlock(BlockBehaviour.Properties properties) {
        this(properties, 0);
    }

    public LegacyTypeBlock(BlockBehaviour.Properties properties, int defaultType) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TYPE, defaultType));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }
}
