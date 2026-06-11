package com.valentin4311.candycraftmod.world.tree;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.block.CherryBlock;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCFeatures;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public final class CherryTreeDecorator extends TreeDecorator {
    public static final Codec<CherryTreeDecorator> CODEC = Codec.unit(CherryTreeDecorator::new);
    private static final Direction[] HORIZONTAL = new Direction[] {
        Direction.NORTH,
        Direction.SOUTH,
        Direction.WEST,
        Direction.EAST
    };

    @Override
    protected TreeDecoratorType<?> type() {
        return CCFeatures.CHERRY_TREE_DECORATOR.get();
    }

    @Override
    public void place(Context context) {
        for (BlockPos logPos : context.logs()) {
            for (Direction direction : HORIZONTAL) {
                BlockPos fruitPos = logPos.relative(direction);
                if (context.isAir(fruitPos) && context.random().nextInt(7) == 0) {
                    placeCherry(context, fruitPos, direction.getOpposite());
                }
            }
        }
    }

    private static void placeCherry(Context context, BlockPos pos, Direction supportDirection) {
        BlockState state = CCBlocks.CHERRY_BLOCK.get().defaultBlockState().setValue(CherryBlock.FACING, supportDirection);
        context.setBlock(pos, state);
    }
}
