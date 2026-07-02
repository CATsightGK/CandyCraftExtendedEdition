package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.BlockHitResult;

public class CandyWaterlilyBlock extends WaterlilyBlock {
    private static final VoxelShape LILY_SHAPE = box(1.0D, 0.0D, 1.0D, 15.0D, 1.5D, 15.0D);
    private final boolean flower;

    public CandyWaterlilyBlock(boolean flower, BlockBehaviour.Properties properties) {
        super(properties);
        this.flower = flower;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return !flower;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return LILY_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return LILY_SHAPE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!flower && level.getRawBrightness(pos.above(), 0) >= 9 && random.nextInt(80) == 0 && !hasFlowerInChunk(level, pos)) {
            level.setBlock(pos, CCBlocks.MARSHMALLOW_FLOWER_BLOCK.get().defaultBlockState(), 2);
        }
    }

    private static boolean hasFlowerInChunk(ServerLevel level, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minY = Math.max(level.getMinBuildHeight(), pos.getY() - 2);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, pos.getY() + 2);
        for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
            for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                for (int y = minY; y <= maxY; y++) {
                    if (level.getBlockState(cursor.set(x, y, z)).is(CCBlocks.MARSHMALLOW_FLOWER_BLOCK.get())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!flower) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            level.setBlock(pos, CCBlocks.MARSHMALLOW_SLICE.get().defaultBlockState(), 2);
            popResource(level, pos, new ItemStack(CCItems.MARSHMALLOW_FLOWER.get()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
