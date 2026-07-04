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
            if ("ice_cream_plains".equals(biome) || "ice_cream_sky_mountains".equals(biome) || "sugar_hell_mountains".equals(biome)) {
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
        BlockPos origin = pos.above();
        for (int i = 0; i < 128; i++) {
            BlockPos target = origin;

            for (int step = 0; step < i / 16; step++) {
                target = target.offset(
                    random.nextInt(3) - 1,
                    (random.nextInt(3) - 1) * random.nextInt(3) / 2,
                    random.nextInt(3) - 1
                );
                if (!level.getBlockState(target.below()).is(CCBlocks.PUDDING.get()) || level.getBlockState(target).isCollisionShapeFullBlock(level, target)) {
                    target = null;
                    break;
                }
            }

            if (target != null && level.isEmptyBlock(target)) {
                BlockState growth = random.nextInt(8) == 0
                    ? CCBlocks.FRAISE_TAGADA_FLOWER.get().defaultBlockState()
                    : randomSweetGrass(random);
                if (growth.canSurvive(level, target)) {
                    level.setBlock(target, growth, 3);
                }
            }
        }
    }

    private static BlockState randomSweetGrass(RandomSource random) {
        return switch (random.nextInt(4)) {
            case 0 -> CCBlocks.SWEET_GRASS_PINK.get().defaultBlockState();
            case 1 -> CCBlocks.SWEET_GRASS_PALE.get().defaultBlockState();
            case 2 -> CCBlocks.SWEET_GRASS_YELLOW.get().defaultBlockState();
            default -> CCBlocks.SWEET_GRASS_RED.get().defaultBlockState();
        };
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }
}
