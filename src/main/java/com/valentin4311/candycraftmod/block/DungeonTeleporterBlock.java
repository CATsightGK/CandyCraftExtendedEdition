package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.world.CCDimensions;
import com.valentin4311.candycraftmod.world.feature.JellyDungeonFeature;
import com.valentin4311.candycraftmod.world.feature.SuguardDungeonFeature;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DungeonTeleporterBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(3.2D, 0.0D, 3.2D, 12.8D, 0.96D, 12.8D);
    public static final EnumProperty<DungeonKind> DUNGEON = EnumProperty.create("dungeon", DungeonKind.class);
    private static final String RETURN_DIM = "CandyCraftDungeonReturnDim";
    private static final String RETURN_X = "CandyCraftDungeonReturnX";
    private static final String RETURN_Y = "CandyCraftDungeonReturnY";
    private static final String RETURN_Z = "CandyCraftDungeonReturnZ";
    private static final BlockPos JELLY_DUNGEON_ORIGIN = new BlockPos(0, 64, 0);
    private static final BlockPos JELLY_DUNGEON_ENTRY = JELLY_DUNGEON_ORIGIN.offset(1, 1, 1);
    private static final float JELLY_DUNGEON_ENTRY_YAW = -90.0F;
    private static final BlockPos SUGUARD_DUNGEON_ORIGIN = new BlockPos(0, 64, 10000);
    private static final BlockPos SUGUARD_DUNGEON_ENTRY = SUGUARD_DUNGEON_ORIGIN.offset(0, 1, 0);

    public DungeonTeleporterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(DUNGEON, DungeonKind.JELLY));
    }

    public static void markSuguard(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            level.setBlock(pos, level.getBlockState(pos).setValue(DUNGEON, DungeonKind.SUGUARD), Block.UPDATE_ALL);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).isSolid();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (level.dimension() == CCDimensions.JELLY_DUNGEON || level.dimension() == CCDimensions.SUGUARD_DUNGEON) {
            if (state.getValue(DUNGEON) == DungeonKind.SUGUARD && pos.closerThan(SUGUARD_DUNGEON_ORIGIN, 220.0D) && !pos.closerThan(SUGUARD_DUNGEON_ENTRY, 4.0D)) {
                serverPlayer.setPortalCooldown(80);
                serverPlayer.teleportTo((ServerLevel) level, SUGUARD_DUNGEON_ENTRY.getX() + 0.5D, SUGUARD_DUNGEON_ENTRY.getY(), SUGUARD_DUNGEON_ENTRY.getZ() + 0.5D, serverPlayer.getYRot(), serverPlayer.getXRot());
                level.playSound(null, SUGUARD_DUNGEON_ENTRY, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.0F);
            } else {
                returnFromDungeon(serverPlayer);
            }
        } else {
            if (state.getValue(DUNGEON) == DungeonKind.SUGUARD) {
                enterSuguardDungeon(serverPlayer, pos);
            } else {
                enterJellyDungeon(serverPlayer, pos);
            }
        }
        return InteractionResult.CONSUME;
    }

    private static void enterJellyDungeon(ServerPlayer player, BlockPos sourcePos) {
        ServerLevel source = player.serverLevel();
        ServerLevel target = player.server.getLevel(CCDimensions.JELLY_DUNGEON);
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
        player.teleportTo(target, JELLY_DUNGEON_ENTRY.getX() + 0.5D, JELLY_DUNGEON_ENTRY.getY(), JELLY_DUNGEON_ENTRY.getZ() + 0.5D, JELLY_DUNGEON_ENTRY_YAW, 0.0F);
        target.playSound(null, JELLY_DUNGEON_ENTRY, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    private static void enterSuguardDungeon(ServerPlayer player, BlockPos sourcePos) {
        ServerLevel source = player.serverLevel();
        ServerLevel target = player.server.getLevel(CCDimensions.SUGUARD_DUNGEON);
        if (target == null) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        data.putString(RETURN_DIM, source.dimension().location().toString());
        data.putInt(RETURN_X, sourcePos.getX());
        data.putInt(RETURN_Y, sourcePos.getY());
        data.putInt(RETURN_Z, sourcePos.getZ());

        prepareSuguardDungeon(target);
        player.setPortalCooldown(80);
        source.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.0F);
        player.teleportTo(target, SUGUARD_DUNGEON_ENTRY.getX() + 0.5D, SUGUARD_DUNGEON_ENTRY.getY(), SUGUARD_DUNGEON_ENTRY.getZ() + 0.5D, player.getYRot(), player.getXRot());
        target.playSound(null, SUGUARD_DUNGEON_ENTRY, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.0F);
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
        JellyDungeonFeature.generateInDungeonLevel(level, JELLY_DUNGEON_ORIGIN);
    }

    private static void prepareSuguardDungeon(ServerLevel level) {
        int centerChunkX = SUGUARD_DUNGEON_ORIGIN.getX() >> 4;
        int centerChunkZ = SUGUARD_DUNGEON_ORIGIN.getZ() >> 4;
        for (int cx = centerChunkX - 8; cx <= centerChunkX + 4; cx++) {
            for (int cz = centerChunkZ - 10; cz <= centerChunkZ + 10; cz++) {
                level.getChunk(cx, cz);
            }
        }
        SuguardDungeonFeature.generateInDungeonLevel(level, SUGUARD_DUNGEON_ORIGIN);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DUNGEON);
    }

    public enum DungeonKind implements StringRepresentable {
        JELLY("jelly"),
        SUGUARD("suguard");

        private final String name;

        DungeonKind(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
