package com.valentin4311.candycraftmod.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class LazyParticleTorchBlock extends TorchBlock {
    private final Supplier<? extends ParticleOptions> particleSupplier;

    public LazyParticleTorchBlock(BlockBehaviour.Properties properties, Supplier<? extends ParticleOptions> particleSupplier) {
        super(properties, ParticleTypes.FLAME);
        this.particleSupplier = particleSupplier;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.7D;
        double z = pos.getZ() + 0.5D;
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        level.addParticle(particleSupplier.get(), x, y, z, 0.0D, 0.0D, 0.0D);
    }

    public static class Wall extends WallTorchBlock {
        private final Supplier<? extends ParticleOptions> particleSupplier;

        public Wall(BlockBehaviour.Properties properties, Supplier<? extends ParticleOptions> particleSupplier) {
            super(properties, ParticleTypes.FLAME);
            this.particleSupplier = particleSupplier;
        }

        @Override
        public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
            Direction direction = state.getValue(FACING).getOpposite();
            double x = pos.getX() + 0.5D + 0.27D * direction.getStepX();
            double y = pos.getY() + 0.92D;
            double z = pos.getZ() + 0.5D + 0.27D * direction.getStepZ();
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
            level.addParticle(particleSupplier.get(), x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }
}
