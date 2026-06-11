package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCSweetscapeBlocks;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class CandyLiquidBlock extends LiquidBlock {
    public static final String PINK_FIRE_TICKS_TAG = "CandyCraftPinkFireTicks";

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
            if (isColdCandyLiquid(neighbor.getFluidState())) {
                level.setBlock(source ? pos : neighborPos, source
                    ? CCBlocks.CARAMEL_BLOCK.get().defaultBlockState()
                    : CCSweetscapeBlocks.CRYSTALLIZED_SUGAR.get().defaultBlockState(), Block.UPDATE_ALL);
                return;
            }
        }
    }

    private static boolean isColdCandyLiquid(FluidState state) {
        return state.is(FluidTags.WATER)
            || state.is(com.valentin4311.candycraftmod.registry.CCFluids.SOURCE_CARAMEL.get())
            || state.is(com.valentin4311.candycraftmod.registry.CCFluids.FLOWING_CARAMEL.get())
            || state.is(com.valentin4311.candycraftmod.registry.CCFluids.SOURCE_GRENADINE.get())
            || state.is(com.valentin4311.candycraftmod.registry.CCFluids.FLOWING_GRENADINE.get())
            || state.is(com.valentin4311.candycraftmod.registry.CCFluids.SOURCE_LIQUID_CHOCOLATE.get())
            || state.is(com.valentin4311.candycraftmod.registry.CCFluids.FLOWING_LIQUID_CHOCOLATE.get());
    }
}
