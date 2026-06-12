package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.CandyCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class PuddingBlock extends Block implements BonemealableBlock {
    public static final int DEFAULT_COLOR = 0xDDA7AA;

    public PuddingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapColor getMapColor(BlockState state, BlockGetter level, BlockPos pos, MapColor defaultColor) {
        if (level instanceof LevelReader reader && pos != null) {
            String biome = reader.getBiome(pos).unwrapKey()
                .map(key -> key.location())
                .filter(id -> CandyCraft.MODID.equals(id.getNamespace()))
                .map(ResourceLocation::getPath)
                .orElse("");
            if ("ice_cream_plains".equals(biome) || "ice_cream_sky_mountains".equals(biome)) {
                return MapColor.SNOW;
            }
        }
        return defaultColor;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) < 4 && level.getBlockState(pos.above()).getLightBlock(level, pos.above()) > 2) {
            level.setBlockAndUpdate(pos, CCBlocks.FLOUR.get().defaultBlockState());
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockPos above = pos.above();
        if (level.isEmptyBlock(above)) {
            level.setBlockAndUpdate(above, CCBlocks.SWEET_GRASS.get().defaultBlockState());
        }
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }
}
