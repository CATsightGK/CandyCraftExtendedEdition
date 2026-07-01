package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.util.EmblemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChewingGumBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D);

    public ChewingGumBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (isChewingGumNative(entity)) {
            return;
        }
        if (entity instanceof Player player && EmblemHelper.has(player, CCItems.CHEWING_GUM_EMBLEM.get())) {
            return;
        }
        Vec3 movement = entity.getDeltaMovement();
        entity.setDeltaMovement(movement.x * 0.2D, 0.0D, movement.z * 0.2D);
    }

    public static boolean isChewingGumNative(Entity entity) {
        return entity.getType() == CCEntityTypes.BEETLE.get()
            || entity.getType() == CCEntityTypes.BOSS_BEETLE.get()
            || entity.getType() == CCEntityTypes.KING_BEETLE.get();
    }
}
