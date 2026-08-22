package com.valentin4311.candycraftmod.block;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Mth;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

public class CandyPortalBlock extends Block {
    private static final String PORTAL_TIME_TAG = "CandyCraftPortalTime";
    private static final String PORTAL_LAST_TICK_TAG = "CandyCraftPortalLastTick";
    private static final String RETURN_PORTAL_VALID_TAG = "CandyCraftCandyWorldReturnPortalValid";
    private static final String RETURN_PORTAL_X_TAG = "CandyCraftCandyWorldReturnPortalX";
    private static final String RETURN_PORTAL_Y_TAG = "CandyCraftCandyWorldReturnPortalY";
    private static final String RETURN_PORTAL_Z_TAG = "CandyCraftCandyWorldReturnPortalZ";
    private static final ResourceKey<Level> CANDY_WORLD = ResourceKey.create(
        Registries.DIMENSION,
        new ResourceLocation(CandyCraft.MODID, "candy_world")
    );
    private static final int SURVIVAL_PORTAL_DELAY = 80;
    private static final int CREATIVE_PORTAL_DELAY = 1;
    private static final int ARRIVAL_PRELOAD_RADIUS = 1;
    private static final int CANDY_WORLD_ARRIVAL_Y = 300;
    private static final int ARRIVAL_SEARCH_RADIUS = 4;
    private static final int ARRIVAL_SEARCH_HEIGHT = 16;
    private static final int ARRIVAL_CLEAR_HEIGHT = 3;
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private static final int MIN_WIDTH = 2;
    private static final int MAX_WIDTH = 21;
    private static final int MIN_HEIGHT = 3;
    private static final int MAX_HEIGHT = 21;
    private static final VoxelShape X_AXIS_AABB = Block.box(0.0D, 0.0D, 6.0D, 16.0D, 16.0D, 10.0D);
    private static final VoxelShape Z_AXIS_AABB = Block.box(6.0D, 0.0D, 0.0D, 10.0D, 16.0D, 16.0D);
    private final float particleRed;
    private final float particleGreen;
    private final float particleBlue;

    public CandyPortalBlock(BlockBehaviour.Properties properties) {
        this(properties, 0.95F, 0.55F, 0.12F);
    }

    public CandyPortalBlock(BlockBehaviour.Properties properties, float particleRed, float particleGreen, float particleBlue) {
        super(properties);
        this.particleRed = particleRed;
        this.particleGreen = particleGreen;
        this.particleBlue = particleBlue;
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, side);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction.Axis portalAxis = state.getValue(AXIS);
        boolean perpendicularNeighbor = direction.getAxis().isHorizontal() && direction.getAxis() != portalAxis;
        if (perpendicularNeighbor || neighborState.is(this)) {
            return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        }
        return hasValidPortalFrame(level, pos, portalAxis, this)
            ? super.updateShape(state, direction, neighborState, level, pos, neighborPos)
            : Blocks.AIR.defaultBlockState();
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || entity.isOnPortalCooldown() || !(entity instanceof ServerPlayer player)) {
            return;
        }

        long gameTime = level.getGameTime();
        long lastPortalTick = player.getPersistentData().getLong(PORTAL_LAST_TICK_TAG);
        int portalTime = gameTime - lastPortalTick <= 1L ? player.getPersistentData().getInt(PORTAL_TIME_TAG) + 1 : 1;
        player.getPersistentData().putInt(PORTAL_TIME_TAG, portalTime);
        player.getPersistentData().putLong(PORTAL_LAST_TICK_TAG, gameTime);
        if (portalTime == 1) {
            level.playSound(null, pos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.7F, 1.0F);
        }
        int delay = player.getAbilities().instabuild ? CREATIVE_PORTAL_DELAY : SURVIVAL_PORTAL_DELAY;
        if (portalTime < delay) {
            return;
        }
        player.getPersistentData().putInt(PORTAL_TIME_TAG, 0);

        teleportPlayer(player, pos);
    }

    public static boolean teleportPlayer(ServerPlayer player) {
        return teleportPlayer(player, null);
    }

    private static boolean teleportPlayer(ServerPlayer player, BlockPos sourcePortalPos) {
        ServerLevel source = player.serverLevel();
        ServerLevel target = source.dimension() == CANDY_WORLD
            ? player.server.getLevel(Level.OVERWORLD)
            : player.server.getLevel(CANDY_WORLD);
        if (target == null) {
            return false;
        }

        if (source.dimension() == Level.OVERWORLD && target.dimension() == CANDY_WORLD && sourcePortalPos != null) {
            rememberReturnPortal(player, sourcePortalPos);
        }
        boolean fallingReturn = source.dimension() == CANDY_WORLD
            && target.dimension() == Level.OVERWORLD
            && hasReturnPortal(player);
        BlockPos targetPos = findArrivalPos(player, target);
        player.setPortalCooldown(80);
        source.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.0F);
        player.teleportTo(target, targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, player.getYRot(), player.getXRot());
        player.setDeltaMovement(0.0D, fallingReturn ? -0.08D : 0.0D, 0.0D);
        player.fallDistance = 0.0F;
        target.playSound(null, targetPos, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.0F);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 19, false, false, true));
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) != 0) {
            return;
        }
        Direction.Axis axis = state.getValue(AXIS);
        double x = pos.getX() + random.nextDouble();
        double y = pos.getY() + random.nextDouble();
        double z = pos.getZ() + random.nextDouble();
        double spread = 0.25D;
        double dx = axis == Direction.Axis.X ? (random.nextDouble() - 0.5D) * 0.08D : (random.nextBoolean() ? spread : -spread);
        double dy = (random.nextDouble() - 0.5D) * 0.08D;
        double dz = axis == Direction.Axis.Z ? (random.nextDouble() - 0.5D) * 0.08D : (random.nextBoolean() ? spread : -spread);
        level.addParticle(new DustParticleOptions(new Vector3f(particleRed, particleGreen, particleBlue), 1.0F), x, y, z, dx, dy, dz);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90, COUNTERCLOCKWISE_90 -> state.setValue(AXIS, state.getValue(AXIS) == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X);
            default -> state;
        };
    }

    public boolean trySpawnPortal(Level level, BlockPos pos) {
        for (Direction.Axis axis : new Direction.Axis[] { Direction.Axis.X, Direction.Axis.Z }) {
            CandyPortalFrame frame = findEmptyFrameContaining(level, pos, axis);
            if (frame != null) {
                if (!level.isClientSide) {
                    spawnPortal(level, frame);
                }
                return true;
            }
        }
        return false;
    }

    private static CandyPortalFrame findEmptyFrameContaining(LevelAccessor level, BlockPos interiorPos, Direction.Axis axis) {
        if (!level.getBlockState(interiorPos).isAir()) {
            return null;
        }

        Direction widthDirection = right(axis);
        BlockPos bottom = interiorPos;
        int distanceDown = 0;
        while (distanceDown < MAX_HEIGHT && level.getBlockState(bottom.below()).isAir()) {
            bottom = bottom.below();
            distanceDown++;
        }

        BlockPos left = bottom;
        int distanceLeft = 0;
        while (distanceLeft < MAX_WIDTH && level.getBlockState(left.relative(widthDirection.getOpposite())).isAir()) {
            left = left.relative(widthDirection.getOpposite());
            distanceLeft++;
        }
        if (!level.getBlockState(left.relative(widthDirection.getOpposite())).is(CCBlocks.SUGAR_BLOCK.get())) {
            return null;
        }

        int width = 0;
        BlockPos cursor = left;
        while (width <= MAX_WIDTH && level.getBlockState(cursor).isAir()) {
            width++;
            cursor = cursor.relative(widthDirection);
        }
        if (width < MIN_WIDTH || width > MAX_WIDTH || !level.getBlockState(cursor).is(CCBlocks.SUGAR_BLOCK.get())) {
            return null;
        }

        for (int x = 0; x < width; x++) {
            if (!level.getBlockState(left.relative(widthDirection, x).below()).is(CCBlocks.SUGAR_BLOCK.get())) {
                return null;
            }
        }

        int height = 0;
        while (height <= MAX_HEIGHT && isCompleteEmptyRow(level, left.above(height), widthDirection, width)) {
            if (!level.getBlockState(left.relative(widthDirection.getOpposite()).above(height)).is(CCBlocks.SUGAR_BLOCK.get())
                    || !level.getBlockState(left.relative(widthDirection, width).above(height)).is(CCBlocks.SUGAR_BLOCK.get())) {
                return null;
            }
            height++;
        }
        if (height < MIN_HEIGHT || height > MAX_HEIGHT) {
            return null;
        }

        for (int x = 0; x < width; x++) {
            if (!level.getBlockState(left.relative(widthDirection, x).above(height)).is(CCBlocks.SUGAR_BLOCK.get())) {
                return null;
            }
        }
        return new CandyPortalFrame(left, axis, width, height);
    }

    private static boolean isCompleteEmptyRow(LevelAccessor level, BlockPos left, Direction widthDirection, int width) {
        for (int x = 0; x < width; x++) {
            if (!level.getBlockState(left.relative(widthDirection, x)).isAir()) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasValidPortalFrame(LevelAccessor level, BlockPos portalPos, Direction.Axis axis, Block portalBlock) {
        Direction widthDirection = right(axis);
        BlockPos bottom = portalPos;
        int distanceDown = 0;
        while (distanceDown < MAX_HEIGHT && isMatchingPortal(level, bottom.below(), axis, portalBlock)) {
            bottom = bottom.below();
            distanceDown++;
        }

        BlockPos left = bottom;
        int distanceLeft = 0;
        while (distanceLeft < MAX_WIDTH && isMatchingPortal(level, left.relative(widthDirection.getOpposite()), axis, portalBlock)) {
            left = left.relative(widthDirection.getOpposite());
            distanceLeft++;
        }
        if (!level.getBlockState(left.relative(widthDirection.getOpposite())).is(CCBlocks.SUGAR_BLOCK.get())) {
            return false;
        }

        int width = 0;
        BlockPos cursor = left;
        while (width <= MAX_WIDTH && isMatchingPortal(level, cursor, axis, portalBlock)) {
            width++;
            cursor = cursor.relative(widthDirection);
        }
        if (width < MIN_WIDTH || width > MAX_WIDTH || !level.getBlockState(cursor).is(CCBlocks.SUGAR_BLOCK.get())) {
            return false;
        }

        for (int x = 0; x < width; x++) {
            if (!level.getBlockState(left.relative(widthDirection, x).below()).is(CCBlocks.SUGAR_BLOCK.get())) {
                return false;
            }
        }

        int height = 0;
        while (height <= MAX_HEIGHT && isCompletePortalRow(level, left.above(height), widthDirection, width, axis, portalBlock)) {
            if (!level.getBlockState(left.relative(widthDirection.getOpposite()).above(height)).is(CCBlocks.SUGAR_BLOCK.get())
                    || !level.getBlockState(left.relative(widthDirection, width).above(height)).is(CCBlocks.SUGAR_BLOCK.get())) {
                return false;
            }
            height++;
        }
        if (height < MIN_HEIGHT || height > MAX_HEIGHT) {
            return false;
        }

        for (int x = 0; x < width; x++) {
            if (!level.getBlockState(left.relative(widthDirection, x).above(height)).is(CCBlocks.SUGAR_BLOCK.get())) {
                return false;
            }
        }
        return true;
    }

    private static boolean isCompletePortalRow(LevelAccessor level, BlockPos left, Direction widthDirection, int width,
            Direction.Axis axis, Block portalBlock) {
        for (int x = 0; x < width; x++) {
            if (!isMatchingPortal(level, left.relative(widthDirection, x), axis, portalBlock)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isMatchingPortal(LevelAccessor level, BlockPos pos, Direction.Axis axis, Block portalBlock) {
        BlockState state = level.getBlockState(pos);
        return state.is(portalBlock) && state.getValue(AXIS) == axis;
    }

    private void spawnPortal(Level level, CandyPortalFrame frame) {
        BlockState portal = defaultBlockState().setValue(AXIS, frame.axis());
        Direction right = right(frame.axis());
        for (int x = 0; x < frame.width(); x++) {
            for (int y = 0; y < frame.height(); y++) {
                level.setBlock(frame.origin().relative(right, x).offset(0, y, 0), portal, Block.UPDATE_ALL);
            }
        }
    }

    private static Direction right(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
    }

    private static BlockPos findArrivalPos(ServerPlayer player, ServerLevel target) {
        BlockPos returnPortal = target.dimension() == Level.OVERWORLD ? getReturnPortal(player) : null;
        int x = returnPortal != null ? returnPortal.getX() : player.getBlockX();
        int z = returnPortal != null ? returnPortal.getZ() : player.getBlockZ();
        preloadChunks(target, x, z, ARRIVAL_PRELOAD_RADIUS);
        if (target.dimension() == CANDY_WORLD) {
            int y = Mth.clamp(CANDY_WORLD_ARRIVAL_Y, target.getMinBuildHeight() + 2, target.getMaxBuildHeight() - ARRIVAL_CLEAR_HEIGHT);
            BlockPos arrival = new BlockPos(x, y, z);
            clearArrivalSpace(target, arrival);
            return arrival;
        }

        if (returnPortal != null) {
            int y = Mth.clamp(returnPortal.getY() + 200,
                target.getMinBuildHeight() + 2, target.getMaxBuildHeight() - ARRIVAL_CLEAR_HEIGHT);
            BlockPos arrival = new BlockPos(x, y, z);
            clearArrivalSpace(target, arrival);
            return arrival;
        }

        BlockPos safeArrival = findSafeSurfaceArrival(player, target, x, z);
        if (safeArrival != null) {
            return safeArrival;
        }

        int surfaceY = target.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int y = Mth.clamp(surfaceY, target.getMinBuildHeight() + 2, target.getMaxBuildHeight() - ARRIVAL_CLEAR_HEIGHT);
        BlockPos fallback = new BlockPos(x, y, z);
        prepareEmergencyArrival(target, fallback);
        return fallback;
    }

    private static void rememberReturnPortal(ServerPlayer player, BlockPos portalPos) {
        player.getPersistentData().putBoolean(RETURN_PORTAL_VALID_TAG, true);
        player.getPersistentData().putInt(RETURN_PORTAL_X_TAG, portalPos.getX());
        player.getPersistentData().putInt(RETURN_PORTAL_Y_TAG, portalPos.getY());
        player.getPersistentData().putInt(RETURN_PORTAL_Z_TAG, portalPos.getZ());
    }

    private static boolean hasReturnPortal(ServerPlayer player) {
        return player.getPersistentData().getBoolean(RETURN_PORTAL_VALID_TAG);
    }

    private static BlockPos getReturnPortal(ServerPlayer player) {
        if (!hasReturnPortal(player)) {
            return null;
        }
        CompoundTag data = player.getPersistentData();
        return new BlockPos(
            data.getInt(RETURN_PORTAL_X_TAG),
            data.getInt(RETURN_PORTAL_Y_TAG),
            data.getInt(RETURN_PORTAL_Z_TAG)
        );
    }

    private static BlockPos findSafeSurfaceArrival(ServerPlayer player, ServerLevel level, int centerX, int centerZ) {
        int minFeetY = level.getMinBuildHeight() + 1;
        int maxFeetY = level.getMaxBuildHeight() - ARRIVAL_CLEAR_HEIGHT;
        for (int radius = 0; radius <= ARRIVAL_SEARCH_RADIUS; radius++) {
            for (int offsetX = -radius; offsetX <= radius; offsetX++) {
                for (int offsetZ = -radius; offsetZ <= radius; offsetZ++) {
                    if (radius > 0 && Math.max(Math.abs(offsetX), Math.abs(offsetZ)) != radius) {
                        continue;
                    }
                    int x = centerX + offsetX;
                    int z = centerZ + offsetZ;
                    int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    int startY = Mth.clamp(surfaceY - 1, minFeetY, maxFeetY);
                    int endY = Math.min(startY + ARRIVAL_SEARCH_HEIGHT, maxFeetY);
                    for (int y = startY; y <= endY; y++) {
                        BlockPos candidate = new BlockPos(x, y, z);
                        if (isSafeArrival(player, level, candidate)) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean isSafeArrival(ServerPlayer player, ServerLevel level, BlockPos pos) {
        BlockPos floorPos = pos.below();
        if (!level.getBlockState(floorPos).isFaceSturdy(level, floorPos, Direction.UP)) {
            return false;
        }
        if (!level.getFluidState(pos).isEmpty() || !level.getFluidState(pos.above()).isEmpty()) {
            return false;
        }
        AABB standingBounds = player.getDimensions(Pose.STANDING)
            .makeBoundingBox(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D)
            .deflate(1.0E-7D);
        return level.noCollision(player, standingBounds);
    }

    private static void preloadChunks(ServerLevel level, int blockX, int blockZ, int radius) {
        int centerChunkX = Mth.floorDiv(blockX, 16);
        int centerChunkZ = Mth.floorDiv(blockZ, 16);
        for (int chunkX = centerChunkX - radius; chunkX <= centerChunkX + radius; chunkX++) {
            for (int chunkZ = centerChunkZ - radius; chunkZ <= centerChunkZ + radius; chunkZ++) {
                level.getChunk(chunkX, chunkZ);
            }
        }
    }

    private static void clearArrivalSpace(ServerLevel level, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y < ARRIVAL_CLEAR_HEIGHT; y++) {
                    level.setBlockAndUpdate(pos.offset(x, y, z), Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void prepareEmergencyArrival(ServerLevel level, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                level.setBlockAndUpdate(pos.offset(x, -1, z), Blocks.COBBLESTONE.defaultBlockState());
            }
        }
        clearArrivalSpace(level, pos);
    }
}
