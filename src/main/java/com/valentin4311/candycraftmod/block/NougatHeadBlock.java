package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.entity.NougatGolemEntity;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class NougatHeadBlock extends FacingModelBlock {
    public NougatHeadBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (level.isClientSide || moving || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!level.getBlockState(pos.below()).is(CCBlocks.NOUGAT_BLOCK.get())
            || !level.getBlockState(pos.below(2)).is(CCBlocks.NOUGAT_BLOCK.get())) {
            return;
        }

        int bodySegments = 0;
        BlockPos cursor = pos.below();
        while (cursor.getY() > level.getMinBuildHeight() && level.getBlockState(cursor).is(CCBlocks.NOUGAT_BLOCK.get())) {
            level.removeBlock(cursor, false);
            bodySegments++;
            cursor = cursor.below();
        }
        level.removeBlock(pos, false);

        NougatGolemEntity base = CCEntityTypes.NOUGAT_GOLEM.get().create(serverLevel);
        if (base == null) {
            return;
        }
        base.moveTo(pos.getX() + 0.5D, pos.getY() - bodySegments, pos.getZ() + 0.5D, state.getValue(FACING).toYRot(), 0.0F);
        serverLevel.addFreshEntity(base);
        NougatGolemEntity.createStack(serverLevel, base, bodySegments);
    }
}
