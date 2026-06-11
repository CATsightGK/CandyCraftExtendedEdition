package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.world.feature.JellyDungeonFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class DungeonTeleporterBlock extends Block {
    private static final String RETURN_DIM = "CandyCraftDungeonReturnDim";
    private static final String RETURN_X = "CandyCraftDungeonReturnX";
    private static final String RETURN_Y = "CandyCraftDungeonReturnY";
    private static final String RETURN_Z = "CandyCraftDungeonReturnZ";
    private static final ResourceKey<Level> CANDY_DUNGEON = ResourceKey.create(
        Registries.DIMENSION,
        new ResourceLocation(CandyCraft.MODID, "candy_dungeon")
    );
    private static final BlockPos JELLY_DUNGEON_ORIGIN = new BlockPos(0, 64, 0);
    private static final BlockPos JELLY_DUNGEON_EXIT = JELLY_DUNGEON_ORIGIN.offset(-3, 2, -3);

    public DungeonTeleporterBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.dimension() == CANDY_DUNGEON) {
            returnFromDungeon(serverPlayer);
        } else {
            enterJellyDungeon(serverPlayer, pos);
        }
        return InteractionResult.CONSUME;
    }

    private static void enterJellyDungeon(ServerPlayer player, BlockPos sourcePos) {
        ServerLevel source = player.serverLevel();
        ServerLevel target = player.server.getLevel(CANDY_DUNGEON);
        if (target == null) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        data.putString(RETURN_DIM, source.dimension().location().toString());
        data.putInt(RETURN_X, sourcePos.getX());
        data.putInt(RETURN_Y, sourcePos.getY());
        data.putInt(RETURN_Z, sourcePos.getZ());

        prepareDungeon(target);
        player.setPortalCooldown(80);
        source.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.0F);
        player.teleportTo(target, JELLY_DUNGEON_EXIT.getX() + 0.5D, JELLY_DUNGEON_EXIT.getY() + 1.0D, JELLY_DUNGEON_EXIT.getZ() + 0.5D, player.getYRot(), player.getXRot());
        target.playSound(null, JELLY_DUNGEON_EXIT, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    private static void returnFromDungeon(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        ResourceLocation dimId = ResourceLocation.tryParse(data.getString(RETURN_DIM));
        ResourceKey<Level> returnKey = dimId == null
            ? Level.OVERWORLD
            : ResourceKey.create(Registries.DIMENSION, dimId);
        ServerLevel target = player.server.getLevel(returnKey);
        if (target == null) {
            target = player.server.getLevel(Level.OVERWORLD);
        }
        if (target == null) {
            return;
        }

        int x = data.contains(RETURN_X) ? data.getInt(RETURN_X) : target.getSharedSpawnPos().getX();
        int y = data.contains(RETURN_Y) ? data.getInt(RETURN_Y) + 1 : target.getSharedSpawnPos().getY();
        int z = data.contains(RETURN_Z) ? data.getInt(RETURN_Z) : target.getSharedSpawnPos().getZ();
        y = Math.max(target.getMinBuildHeight() + 2, Math.min(y, target.getMaxBuildHeight() - 2));

        player.setPortalCooldown(80);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.0F);
        player.teleportTo(target, x + 0.5D, y, z + 0.5D, player.getYRot(), player.getXRot());
        target.playSound(null, new BlockPos(x, y, z), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    private static void prepareDungeon(ServerLevel level) {
        for (int cx = -2; cx <= 2; cx++) {
            for (int cz = -2; cz <= 2; cz++) {
                level.getChunk(cx, cz);
            }
        }
        if (level.getBlockState(JELLY_DUNGEON_EXIT).is(Blocks.AIR)) {
            JellyDungeonFeature.generateInDungeonLevel(level, JELLY_DUNGEON_ORIGIN);
        }
    }
}
