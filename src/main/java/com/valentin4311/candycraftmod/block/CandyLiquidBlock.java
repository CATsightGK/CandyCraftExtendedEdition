package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCSweetscapeBlocks;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class CandyLiquidBlock extends LiquidBlock {
    public enum Kind {
        CARAMEL,
        GRENADINE,
        LIQUID_CHOCOLATE,
        LIQUID_CANDY
    }

    private final Kind kind;

    public CandyLiquidBlock(Supplier<? extends FlowingFluid> fluid, BlockBehaviour.Properties properties, Kind kind) {
        super(fluid, properties);
        this.kind = kind;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        reactWithNeighboringFluid(state, level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        reactWithNeighboringFluid(state, level, pos);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (kind == Kind.LIQUID_CHOCOLATE) {
            handleLiquidChocolateMovement(level, pos, entity);
            return;
        }

        if (kind != Kind.LIQUID_CANDY || !(entity instanceof LivingEntity living) || level.isClientSide) {
            return;
        }

        if (living.tickCount % 10 == 0) {
            living.hurt(level.damageSources().hotFloor(), 2.0F);
        }
    }

    private void reactWithNeighboringFluid(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }

        FluidState fluidState = state.getFluidState();
        switch (kind) {
            case CARAMEL -> reactCaramel(level, pos, fluidState.isSource());
            case GRENADINE -> reactGrenadine(level, pos, fluidState.isSource());
            case LIQUID_CHOCOLATE -> reactLiquidChocolate(level, pos, fluidState.isSource());
            case LIQUID_CANDY -> reactLiquidCandy(level, pos, fluidState.isSource());
        }
    }

    private void reactCaramel(Level level, BlockPos pos, boolean source) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (!neighbor.getFluidState().is(FluidTags.LAVA)) {
                continue;
            }

            if (source) {
                level.setBlock(pos, CCBlocks.CARAMEL_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
                return;
            }

            level.setBlock(neighborPos, Blocks.OBSIDIAN.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private void reactGrenadine(Level level, BlockPos pos, boolean source) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (neighbor.getFluidState().is(FluidTags.WATER)) {
                level.setBlock(source ? pos : neighborPos, source
                    ? CCBlocks.GRENADINE_BLOCK.get().defaultBlockState()
                    : CCBlocks.FRAGILE_GRENADINE_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
                return;
            }
        }
    }

    private void reactLiquidChocolate(Level level, BlockPos pos, boolean source) {
        if (!source) {
            return;
        }

        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN) {
                continue;
            }

            BlockPos neighborPos = pos.relative(direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (neighbor.getFluidState().is(FluidTags.WATER)) {
                level.setBlock(pos, com.valentin4311.candycraftmod.registry.CCSweetscapeBlocks.MILK_CHOCOLATE_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
                return;
            }
        }
    }

    private void reactLiquidCandy(Level level, BlockPos pos, boolean source) {
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighbor = level.getBlockState(neighborPos);
            if (isNonLavaLiquid(neighbor.getFluidState())) {
                level.setBlock(source ? pos : neighborPos, source
                    ? CCBlocks.CANDY_CANE_BLOCK.get().defaultBlockState()
                    : CCSweetscapeBlocks.CRYSTALLIZED_SUGAR.get().defaultBlockState(), Block.UPDATE_ALL);
                return;
            }
        }
    }

    private static boolean isNonLavaLiquid(FluidState state) {
        return !state.isEmpty() && !state.is(FluidTags.LAVA)
            && !state.is(com.valentin4311.candycraftmod.registry.CCFluids.SOURCE_LIQUID_CANDY.get())
            && !state.is(com.valentin4311.candycraftmod.registry.CCFluids.FLOWING_LIQUID_CANDY.get());
    }

    private static void handleLiquidChocolateMovement(Level level, BlockPos pos, Entity entity) {
        Vec3 movement = entity.getDeltaMovement();
        double horizontalSpeed = movement.horizontalDistance();
        double fluidSurface = pos.getY() + 0.92D;
        boolean headInChocolate = isHeadInLiquidChocolate(level, entity);

        if (headInChocolate && movement.y > 0.005D) {
            entity.setDeltaMovement(movement.x * 0.84D, Math.max(movement.y, 0.28D), movement.z * 0.84D);
            entity.resetFallDistance();
            spawnChocolateStepParticles(level, pos, entity, horizontalSpeed);
            return;
        }

        if (horizontalSpeed > 0.025D) {
            entity.setOnGround(true);
            double y = movement.y < 0.0D ? (headInChocolate ? 0.05D : 0.0D) : Math.min(movement.y, headInChocolate ? 0.08D : 0.018D);
            entity.setDeltaMovement(movement.x * 0.88D, y, movement.z * 0.88D);
            entity.resetFallDistance();
            spawnChocolateStepParticles(level, pos, entity, horizontalSpeed);
            return;
        }

        entity.makeStuckInBlock(Blocks.HONEY_BLOCK.defaultBlockState(), new Vec3(0.70D, 0.62D, 0.70D));
        Vec3 sunkMovement = entity.getDeltaMovement();
        double y = sunkMovement.y;
        if (headInChocolate && y > 0.005D) {
            y = 0.18D;
        } else if (y > -0.12D) {
            y -= 0.038D;
        }
        entity.setDeltaMovement(sunkMovement.x, y, sunkMovement.z);
    }

    private static boolean isHeadInLiquidChocolate(Level level, Entity entity) {
        FluidState state = level.getFluidState(BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ()));
        return state.is(com.valentin4311.candycraftmod.registry.CCFluids.SOURCE_LIQUID_CHOCOLATE.get())
            || state.is(com.valentin4311.candycraftmod.registry.CCFluids.FLOWING_LIQUID_CHOCOLATE.get());
    }

    private static void spawnChocolateStepParticles(Level level, BlockPos pos, Entity entity, double horizontalSpeed) {
        if (!level.isClientSide || entity.tickCount % 3 != 0) {
            return;
        }

        double fluidSurface = pos.getY() + 0.92D;
        if (Math.abs(entity.getY() - fluidSurface) > 0.8D) {
            return;
        }

        int count = horizontalSpeed > 0.12D ? 5 : 3;
        for (int i = 0; i < count; i++) {
            double x = entity.getX() + (level.random.nextDouble() - 0.5D) * entity.getBbWidth();
            double z = entity.getZ() + (level.random.nextDouble() - 0.5D) * entity.getBbWidth();
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            double spread = 0.018D + level.random.nextDouble() * 0.045D;
            double vx = Math.cos(angle) * spread;
            double vz = Math.sin(angle) * spread;
            double vy = 0.035D + level.random.nextDouble() * 0.035D;
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, CCSweetscapeBlocks.LIQUID_CHOCOLATE.get().defaultBlockState()),
                x, fluidSurface + 0.01D, z, vx, vy, vz);
        }
    }
}
