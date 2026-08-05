package com.valentin4311.candycraftmod.item;

import com.valentin4311.candycraftmod.block.StrawberryJellyBlock;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

public final class StrawberryJellyItem extends BlockItem {
    public StrawberryJellyItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public String getDescriptionId() {
        return Util.makeDescriptionId("item", BuiltInRegistries.ITEM.getKey(this));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos supportPos = context.getClickedPos();
        if (context.getClickedFace() != Direction.UP
                || !StrawberryJellyBlock.hasFullSupport(context.getLevel(), supportPos)) {
            return InteractionResult.PASS;
        }
        return super.useOn(context);
    }
}
