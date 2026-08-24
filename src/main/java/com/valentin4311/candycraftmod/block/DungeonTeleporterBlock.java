package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.item.JellyDungeonKeyItem;
import com.valentin4311.candycraftmod.world.CCDimensions;
import com.valentin4311.candycraftmod.world.DungeonProgressData;
import com.valentin4311.candycraftmod.world.DungeonResetManager;
import com.valentin4311.candycraftmod.world.DungeonProgressData.Instance;
import com.valentin4311.candycraftmod.world.DungeonProgressData.LocatedPortal;
import com.valentin4311.candycraftmod.world.DungeonProgressData.PortalRecord;
import com.valentin4311.candycraftmod.world.feature.JellyDungeonFeature;
import com.valentin4311.candycraftmod.world.feature.SuguardDungeonFeature;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DungeonTeleporterBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(3.2D, 0.0D, 3.2D, 12.8D, 0.96D, 12.8D);
    public static final EnumProperty<DungeonKind> DUNGEON = EnumProperty.create("dungeon", DungeonKind.class);
    public static final EnumProperty<PortalRole> ROLE = EnumProperty.create("role", PortalRole.class);
    private static final String RETURN_DIM = "CandyCraftDungeonReturnDim";
    private static final String RETURN_X = "CandyCraftDungeonReturnX";
    private static final String RETURN_Y = "CandyCraftDungeonReturnY";
    private static final String RETURN_Z = "CandyCraftDungeonReturnZ";
    private static final String CURRENT_OWNER = "CandyCraftDungeonOwner";
    private static final String CURRENT_KIND = "CandyCraftDungeonKind";
    private static final String CURRENT_INSTANCE = "CandyCraftDungeonInstance";

    public DungeonTeleporterBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
            .setValue(DUNGEON, DungeonKind.JELLY)
            .setValue(ROLE, PortalRole.ENTRY));
    }

    public static BlockState state(DungeonKind kind, PortalRole role) {
        return com.valentin4311.candycraftmod.registry.CCBlocks.BLOCK_TELEPORTER.get()
            .defaultBlockState().setValue(DUNGEON, kind).setValue(ROLE, role);
    }

    public static boolean isProtectedSupport(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.above()).getBlock() instanceof DungeonTeleporterBlock;
    }

    public static void markSuguard(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            level.setBlock(pos, level.getBlockState(pos).setValue(DUNGEON, DungeonKind.SUGUARD), Block.UPDATE_ALL);
        }
    }

    public static void markJellyCompletedFromBossLock(ServerPlayer player) {
        if (player.level().dimension() != CCDimensions.JELLY_DUNGEON) {
            return;
        }
        CompoundTag playerData = player.getPersistentData();
        if (!playerData.hasUUID(CURRENT_OWNER)
            || !DungeonKind.JELLY.getSerializedName().equals(playerData.getString(CURRENT_KIND))) {
            return;
        }

        UUID owner = playerData.getUUID(CURRENT_OWNER);
        long instanceId = playerData.getLong(CURRENT_INSTANCE);
        DungeonProgressData data = DungeonProgressData.get(player.server);
        Instance instance = data.getActive(owner, DungeonKind.JELLY);
        if (instance == null || instance.id() != instanceId || instance.bossDefeated()) {
            return;
        }
        data.markCompleted(owner, DungeonKind.JELLY, instanceId);
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
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!isDungeonLevel(level) && state.getValue(ROLE) == PortalRole.ENTRY) {
            ItemStack matchingKey = findMatchingKey(player, state.getValue(DUNGEON));
            if (!matchingKey.isEmpty()) {
                JellyDungeonKeyItem key = (JellyDungeonKeyItem) matchingKey.getItem();
                recoverEntrancePortal(state, serverLevel, pos, serverPlayer, matchingKey, key);
                return InteractionResult.CONSUME;
            }
        }

        if (isDungeonLevel(level)) {
            useInsideDungeon(state, serverLevel, pos, serverPlayer);
        } else {
            useEntrancePortal(state, serverLevel, pos, serverPlayer);
        }
        return InteractionResult.CONSUME;
    }

    private static ItemStack findMatchingKey(Player player, DungeonKind kind) {
        for (InteractionHand candidate : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(candidate);
            if (stack.getItem() instanceof JellyDungeonKeyItem key && key.matchesDungeon(kind)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean recoverEntrancePortal(BlockState state, ServerLevel level, BlockPos pos,
            ServerPlayer player, ItemStack keyStack, JellyDungeonKeyItem key) {
        DungeonProgressData data = DungeonProgressData.get(player.server);
        PortalRecord portal = data.getPortal(level, pos);
        if (portal == null || portal.kind() != state.getValue(DUNGEON)
            || !key.recoverPortal(keyStack, player, portal)) {
            return false;
        }

        data.removePortal(level, pos);
        level.levelEvent(2001, pos, Block.getId(state));
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 0.9F, 1.35F);
        player.displayClientMessage(Component.translatable("message.candycraftmod.dungeon.portal_recovered"), true);
        return true;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && !level.isClientSide && level instanceof ServerLevel serverLevel) {
            DungeonProgressData.get(serverLevel.getServer()).removePortal(serverLevel, pos);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    private static void useEntrancePortal(BlockState state, ServerLevel level, BlockPos pos, ServerPlayer player) {
        DungeonProgressData data = DungeonProgressData.get(player.server);
        PortalRecord portal = data.getPortal(level, pos);
        if (portal == null || portal.kind() != state.getValue(DUNGEON)) {
            shatterCompletedPortal(level, pos, player);
            return;
        }
        if (!portal.owner().equals(player.getUUID())) {
            player.displayClientMessage(Component.translatable("message.candycraftmod.dungeon.portal_not_owner"), true);
            return;
        }

        Instance instance = data.getActive(portal.owner(), portal.kind());
        if (instance == null || instance.id() != portal.instanceId()) {
            data.removePortal(level, pos);
            shatterCompletedPortal(level, pos, player);
            return;
        }
        enterDungeon(player, level, pos, portal.owner(), portal.kind(), instance, data);
    }

    private static void useInsideDungeon(BlockState state, ServerLevel level, BlockPos pos, ServerPlayer player) {
        DungeonKind kind = state.getValue(DUNGEON);
        CompoundTag playerData = player.getPersistentData();
        UUID owner = playerData.hasUUID(CURRENT_OWNER) ? playerData.getUUID(CURRENT_OWNER) : player.getUUID();
        long instanceId = playerData.getLong(CURRENT_INSTANCE);
        DungeonProgressData data = DungeonProgressData.get(player.server);
        Instance instance = data.getActive(owner, kind);

        if (instance == null || instance.id() != instanceId) {
            returnFromDungeon(player);
            return;
        }

        PortalRole role = state.getValue(ROLE);
        if (role == PortalRole.ENTRY) {
            returnFromDungeon(player);
            return;
        }
        if (role == PortalRole.RETURN) {
            teleportToDungeonEntry(player, level, kind, instance.origin());
            return;
        }
        if (!instance.bossDefeated()) {
            player.displayClientMessage(Component.translatable("message.candycraftmod.dungeon.not_complete"), true);
            return;
        }

        finishDungeon(level, player, owner, kind, instance, data);
    }

    private static void enterDungeon(ServerPlayer player, ServerLevel source, BlockPos sourcePos, UUID owner,
            DungeonKind kind, Instance instance, DungeonProgressData data) {
        ServerLevel target = player.server.getLevel(dimensionFor(kind));
        if (target == null) {
            return;
        }

        CompoundTag playerData = player.getPersistentData();
        playerData.putString(RETURN_DIM, source.dimension().location().toString());
        playerData.putInt(RETURN_X, sourcePos.getX());
        playerData.putInt(RETURN_Y, sourcePos.getY());
        playerData.putInt(RETURN_Z, sourcePos.getZ());
        playerData.putUUID(CURRENT_OWNER, owner);
        playerData.putString(CURRENT_KIND, kind.getSerializedName());
        playerData.putLong(CURRENT_INSTANCE, instance.id());

        source.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.0F);

        if (!instance.generated()) {
            BlockPos origin = instance.origin();
            boolean started = kind == DungeonKind.JELLY
                ? JellyDungeonFeature.beginPrepare(target, origin)
                : SuguardDungeonFeature.beginPrepare(target, origin);
            if (started) {
                // Marked on completion so a crash mid-rebuild leaves the
                // instance ungenerated and self-heals on the next entry.
                DungeonResetManager.onPrepared(target, origin,
                    () -> data.markGenerated(owner, kind, instance.id()));
            }
            // Instance content is being rebuilt across ticks; hold the teleport
            // until the queued clear+build pipeline reports completion.
            DungeonResetManager.onPrepared(target, origin, () -> {
                if (player.server.getPlayerList().getPlayer(player.getUUID()) == player) {
                    teleportToDungeonEntry(player, target, kind, origin);
                }
            });
            return;
        }

        teleportToDungeonEntry(player, target, kind, instance.origin());
    }

    private static void teleportToDungeonEntry(ServerPlayer player, ServerLevel target, DungeonKind kind, BlockPos origin) {
        BlockPos entry = entryFor(kind, origin);
        player.setPortalCooldown(80);
        player.teleportTo(target, entry.getX() + 0.5D, entry.getY(), entry.getZ() + 0.5D,
            kind == DungeonKind.JELLY ? -90.0F : player.getYRot(), 0.0F);
        target.playSound(null, entry, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    private static void finishDungeon(ServerLevel dungeonLevel, ServerPlayer player, UUID owner, DungeonKind kind,
            Instance instance, DungeonProgressData data) {
        long instanceId = instance.id();
        BlockPos origin = instance.origin();
        if (!data.finish(owner, kind, instanceId)) {
            return;
        }

        List<ServerPlayer> playersInside = new ArrayList<>();
        AABB bounds = bounds(kind, origin);
        for (ServerPlayer candidate : dungeonLevel.players()) {
            if (bounds.contains(candidate.position())) {
                playersInside.add(candidate);
            }
        }
        for (ServerPlayer candidate : playersInside) {
            returnAfterCompletion(candidate);
            candidate.displayClientMessage(Component.translatable(
                "message.candycraftmod.dungeon.finished." + kind.getSerializedName()), false);
        }

        shatterLoadedEntrancePortals(player.server, data, owner, kind, instanceId);
        if (kind == DungeonKind.JELLY) {
            JellyDungeonFeature.clearDungeonInstance(dungeonLevel, origin);
        } else {
            SuguardDungeonFeature.clearDungeonInstance(dungeonLevel, origin);
        }
    }

    private static void shatterLoadedEntrancePortals(MinecraftServer server, DungeonProgressData data, UUID owner,
            DungeonKind kind, long instanceId) {
        for (LocatedPortal portal : data.getPortals(owner, kind, instanceId)) {
            ResourceLocation dimensionId = ResourceLocation.tryParse(portal.dimension());
            if (dimensionId == null) {
                continue;
            }
            ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));
            if (level == null || !level.hasChunkAt(portal.pos())) {
                continue;
            }
            if (level.getBlockState(portal.pos()).getBlock() instanceof DungeonTeleporterBlock) {
                level.levelEvent(2001, portal.pos(), Block.getId(level.getBlockState(portal.pos())));
                level.setBlock(portal.pos(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            data.removePortal(portal.dimension(), portal.pos());
        }
    }

    private static void shatterCompletedPortal(ServerLevel level, BlockPos pos, ServerPlayer player) {
        player.displayClientMessage(Component.translatable("message.candycraftmod.dungeon.already_completed"), true);
        level.levelEvent(2001, pos, Block.getId(level.getBlockState(pos)));
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    private static void returnFromDungeon(ServerPlayer player) {
        teleportOutOfDungeon(player, false);
    }

    private static void returnAfterCompletion(ServerPlayer player) {
        teleportOutOfDungeon(player, true);
    }

    private static void teleportOutOfDungeon(ServerPlayer player, boolean completed) {
        CompoundTag data = player.getPersistentData();
        ResourceLocation dimId = completed ? Level.OVERWORLD.location() : ResourceLocation.tryParse(data.getString(RETURN_DIM));
        ResourceKey<Level> returnKey = dimId == null
            ? CCDimensions.CANDY_WORLD
            : ResourceKey.create(Registries.DIMENSION, dimId);
        ServerLevel target = player.server.getLevel(returnKey);
        if (target == null) {
            target = player.server.getLevel(CCDimensions.CANDY_WORLD);
        }
        if (target == null) {
            target = player.server.getLevel(Level.OVERWORLD);
        }
        if (target == null) {
            return;
        }

        int x = !completed && data.contains(RETURN_X) ? data.getInt(RETURN_X) : target.getSharedSpawnPos().getX();
        int y = !completed && data.contains(RETURN_Y) ? data.getInt(RETURN_Y) + 1 : target.getSharedSpawnPos().getY();
        int z = !completed && data.contains(RETURN_Z) ? data.getInt(RETURN_Z) : target.getSharedSpawnPos().getZ();
        y = Math.max(target.getMinBuildHeight() + 2, Math.min(y, target.getMaxBuildHeight() - 2));

        player.setPortalCooldown(80);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.0F);
        player.teleportTo(target, x + 0.5D, y, z + 0.5D, player.getYRot(), player.getXRot());
        target.playSound(null, new BlockPos(x, y, z), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.0F);
        data.remove(CURRENT_OWNER);
        data.remove(CURRENT_KIND);
        data.remove(CURRENT_INSTANCE);
    }

    private static BlockPos entryFor(DungeonKind kind, BlockPos origin) {
        return kind == DungeonKind.JELLY ? origin.offset(1, 1, 1) : origin.offset(0, 1, 0);
    }

    private static AABB bounds(DungeonKind kind, BlockPos origin) {
        return kind == DungeonKind.JELLY
            ? new AABB(origin.offset(-36, -7, -430), origin.offset(37, 57, 25))
            : new AABB(origin.offset(-132, -63, -160), origin.offset(65, 191, 161));
    }

    private static ResourceKey<Level> dimensionFor(DungeonKind kind) {
        return kind == DungeonKind.JELLY ? CCDimensions.JELLY_DUNGEON : CCDimensions.SUGUARD_DUNGEON;
    }

    private static boolean isDungeonLevel(Level level) {
        return level.dimension() == CCDimensions.JELLY_DUNGEON || level.dimension() == CCDimensions.SUGUARD_DUNGEON;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DUNGEON, ROLE);
    }

    public enum DungeonKind implements StringRepresentable {
        JELLY("jelly"),
        SUGUARD("suguard");

        private final String name;

        DungeonKind(String name) {
            this.name = name;
        }

        public static DungeonKind byName(String name) {
            return SUGUARD.name.equals(name) ? SUGUARD : JELLY;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum PortalRole implements StringRepresentable {
        ENTRY("entry"),
        RETURN("return"),
        END("end");

        private final String name;

        PortalRole(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
