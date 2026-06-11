package com.valentin4311.candycraftmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class AcidMintFlowerBlock extends LegacyMetadataBlock.Plant {
    public AcidMintFlowerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(4) != 2) {
            return;
        }
        double x = pos.getX() + random.nextFloat() / 2.0F + 0.25D;
        double y = pos.getY() + random.nextFloat() + 0.5D;
        double z = pos.getZ() + random.nextFloat() / 2.0F + 0.25D;
        level.addParticle(ParticleTypes.ENTITY_EFFECT, x, y, z, 0.1D, 0.8D, 0.1D);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 20, 1));
        }
    }
}
