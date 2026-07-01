package com.valentin4311.candycraftmod.block.entity;

import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import com.valentin4311.candycraftmod.registry.CCBlockEntities;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;

public class DragonEggBlockEntity extends BlockEntity {
    private static final int MIN_HATCH_TICKS = 48000;
    private static final int HATCH_TICK_SPREAD = 24000;
    private static final int SPAWN_DELAY_TICKS = 50;
    private int timeLeft = -1;

    public DragonEggBlockEntity(BlockPos pos, BlockState state) {
        super(CCBlockEntities.DRAGON_EGG.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DragonEggBlockEntity egg) {
        if (egg.timeLeft < 0) {
            RandomSource random = level.getRandom();
            egg.timeLeft = MIN_HATCH_TICKS + random.nextInt(HATCH_TICK_SPREAD + 1);
            egg.setChanged();
        }
        if (!hasSugarEssenceRing(level, pos) && egg.timeLeft > SPAWN_DELAY_TICKS) {
            return;
        }
        egg.timeLeft--;
        egg.setChanged();
        if (!level.isClientSide && egg.timeLeft == SPAWN_DELAY_TICKS && level instanceof ServerLevel serverLevel) {
            spawnHatchedEntity(serverLevel, pos, state);
        }
        if (!level.isClientSide && egg.timeLeft <= 0) {
            level.removeBlock(pos, false);
            convertSugarEssenceRing(level, pos);
        }
    }

    private static void spawnHatchedEntity(ServerLevel level, BlockPos pos, BlockState state) {
        BasicCandyZombieEntity entity = state.is(CCBlocks.BEETLE_EGG_BLOCK.get())
            ? CCEntityTypes.KING_BEETLE.get().create(level)
            : CCEntityTypes.DRAGON.get().create(level);

        if (entity == null) {
            return;
        }

        if (entity.getType() == CCEntityTypes.DRAGON.get()) {
            entity.setBabyDragon(true);
        }

        entity.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D, level.getRandom().nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(entity);

        if (entity.getType() == CCEntityTypes.KING_BEETLE.get()) {
            entity.spawnAtLocation(new ItemStack(CCItems.CHEWING_GUM_EMBLEM.get()), 0.5F);
        }
    }

    public static boolean hasSugarEssenceRing(BlockGetter level, BlockPos pos) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) != 2 && Math.abs(z) != 2) {
                    continue;
                }
                if (!level.getBlockState(pos.offset(x, 0, z)).is(CCBlocks.SUGAR_ESSENCE_FLOWER.get())) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void convertSugarEssenceRing(Level level, BlockPos pos) {
        BlockState replacement = CCBlocks.FRAISE_TAGADA_FLOWER.get().defaultBlockState();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) != 2 && Math.abs(z) != 2) {
                    continue;
                }
                BlockPos flowerPos = pos.offset(x, 0, z);
                if (level.getBlockState(flowerPos).is(CCBlocks.SUGAR_ESSENCE_FLOWER.get())) {
                    level.setBlock(flowerPos, replacement, Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("hatch", timeLeft);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        timeLeft = tag.contains("hatch") ? tag.getInt("hatch") : -1;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
