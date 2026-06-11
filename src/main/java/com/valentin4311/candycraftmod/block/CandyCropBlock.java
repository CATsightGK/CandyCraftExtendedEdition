package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
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
}
