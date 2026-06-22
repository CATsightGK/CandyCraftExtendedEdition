package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.block.entity.DragonEggBlockEntity;
import com.valentin4311.candycraftmod.registry.CCBlockEntities;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

public class DragonEggBlock extends BaseEntityBlock implements EntityBlock {
    private static final DustParticleOptions HATCH_PARTICLE = new DustParticleOptions(new Vector3f(0.95F, 0.75F, 0.4F), 1.0F);

    public DragonEggBlock(Properties properties) {
        super(properties.randomTicks());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DragonEggBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTicker(type, CCBlockEntities.DRAGON_EGG.get(), DragonEggBlockEntity::tick);
    }

    @Override
    public boolean canSurvive(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isRedstoneConductor(level, pos.below());
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!DragonEggBlockEntity.hasSugarEssenceRing(level, pos)) {
            return;
        }
        for (int i = 0; i < 7; i++) {
            double x = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
            double y = pos.getY() + 0.6D + random.nextDouble() * 0.4D;
            double z = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;
            level.addParticle(HATCH_PARTICLE, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }

    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTicker(
            BlockEntityType<A> actualType, BlockEntityType<E> expectedType, BlockEntityTicker<? super E> ticker) {
        return expectedType == actualType ? (BlockEntityTicker<A>) ticker : null;
    }
}
