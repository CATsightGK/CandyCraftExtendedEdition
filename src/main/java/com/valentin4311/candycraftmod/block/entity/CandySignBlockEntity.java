package com.valentin4311.candycraftmod.block.entity;

import com.valentin4311.candycraftmod.registry.CCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CandySignBlockEntity extends SignBlockEntity {
    public CandySignBlockEntity(BlockPos pos, BlockState state) {
        super(CCBlockEntities.CANDY_SIGN.get(), pos, state);
    }

    @Override
    protected SignText createDefaultSignText() {
        return new SignText().setColor(DyeColor.BLACK);
    }
}
