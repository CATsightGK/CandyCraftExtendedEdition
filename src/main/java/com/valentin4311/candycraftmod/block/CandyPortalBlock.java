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
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

public class CandyPortalBlock extends Block {
    private static final String PORTAL_TIME_TAG = "CandyCraftPortalTime";
    private static final String PORTAL_LAST_TICK_TAG = "CandyCraftPortalLastTick";
    private static final ResourceKey<Level> CANDY_WORLD = ResourceKey.create(
        Registries.DIMENSION,
        new ResourceLocation(CandyCraft.MODID, "candy_world")
    );
    private static final int SURVIVAL_PORTAL_DELAY = 80;
    private static final int CREATIVE_PORTAL_DELAY = 1;
    private static final int CANDY_WORLD_PRELOAD_RADIUS = 0;
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
        return findContainingFrame(level, pos, state.getValue(AXIS), true, this) != null ? state : Blocks.AIR.defaultBlockState();
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

        ServerLevel source = player.serverLevel();
        ServerLevel target = source.dimension() == CANDY_WORLD
            ? player.server.getLevel(Level.OVERWORLD)
            : player.server.getLevel(CANDY_WORLD);
        if (target == null) {
            return;
        }

        BlockPos targetPos = findArrivalPos(player, target);
        prepareArrivalPlatform(target, targetPos);
        player.setPortalCooldown(80);
        source.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.0F);
        player.teleportTo(target, targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D, player.getYRot(), player.getXRot());
        target.playSound(null, targetPos, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.8F, 1.0F);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 19, false, false, true));
        if (target.dimension() == CANDY_WORLD) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 20 * 12, 0, false, false, false));
        }
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
        if (level.isClientSide) {
            return true;
        }
        for (Direction.Axis axis : new Direction.Axis[] { Direction.Axis.X, Direction.Axis.Z }) {
            CandyPortalFrame frame = findContainingFrame(level, pos, axis, false, this);
            if (frame != null) {
                spawnPortal(level, frame);
                return true;
            }
        }
        return false;
    }

    private static CandyPortalFrame findContainingFrame(LevelAccessor level, BlockPos pos, Direction.Axis axis, boolean allowPortalInterior, Block portalBlock) {
        Direction right = right(axis);
        for (int widthOffset = -MAX_WIDTH + 1; widthOffset <= 0; widthOffset++) {
            for (int heightOffset = -MAX_HEIGHT + 1; heightOffset <= 0; heightOffset++) {
                BlockPos origin = pos.relative(right, widthOffset).offset(0, heightOffset, 0);
                CandyPortalFrame frame = findCompleteFrameAt(level, origin, axis, allowPortalInterior, portalBlock);
                if (frame != null) {
                    return frame;
                }
            }
        }
        return null;
    }

    private static CandyPortalFrame findCompleteFrameAt(LevelAccessor level, BlockPos origin, Direction.Axis axis, boolean allowPortalInterior, Block portalBlock) {
        for (int width = MIN_WIDTH; width <= MAX_WIDTH; width++) {
            for (int height = MIN_HEIGHT; height <= MAX_HEIGHT; height++) {
                if (isCompleteFrame(level, origin, axis, width, height, allowPortalInterior, portalBlock)) {
                    return new CandyPortalFrame(origin, axis, width, height);
                }
            }
        }
        return null;
    }

    private static boolean isCompleteFrame(LevelAccessor level, BlockPos origin, Direction.Axis axis, int width, int height, boolean allowPortalInterior, Block portalBlock) {
        Direction right = right(axis);
        for (int x = -1; x <= width; x++) {
            for (int y = -1; y <= height; y++) {
                BlockState state = level.getBlockState(origin.relative(right, x).offset(0, y, 0));
                boolean frame = x == -1 || x == width || y == -1 || y == height;
                if (frame) {
                    if (!state.is(CCBlocks.SUGAR_BLOCK.get())) {
                        return false;
                    }
                } else if (!state.isAir() && (!allowPortalInterior || !state.is(portalBlock))) {
                    return false;
                }
            }
        }
        return true;
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
        int x = player.getBlockX();
        int z = player.getBlockZ();
        if (target.dimension() == CANDY_WORLD) {
            preloadChunks(target, x, z, CANDY_WORLD_PRELOAD_RADIUS);
            int y = Math.max(target.getMinBuildHeight() + 2, target.getMaxBuildHeight() - 8);
            return new BlockPos(x, y, z);
        }
        int y = Math.max(target.getMinBuildHeight() + 2, Math.min(player.getBlockY(), target.getMaxBuildHeight() - 2));
        return new BlockPos(x, y, z);
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

    private static BlockPos findCandySurface(ServerLevel target, int x, int z) {
        target.getChunk(Mth.floorDiv(x, 16), Mth.floorDiv(z, 16));
        BlockPos best = findLandSurface(target, x, z, 0);
        if (best == null) {
            for (int radius = 4; radius <= 32; radius += 4) {
                for (int dx = -radius; dx <= radius; dx += 4) {
                    for (int dz = -radius; dz <= radius; dz += 4) {
                        if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
                            continue;
                        }
                        best = findLandSurface(target, x + dx, z + dz, 0);
                        if (best != null) {
                            return best;
                        }
                    }
                }
            }
        }
        if (best != null) {
            return best;
        }
        int surface = Math.max(
            target.getHeight(Heightmap.Types.WORLD_SURFACE, x, z),
            target.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z)
        );
        int y = Math.max(target.getMinBuildHeight() + 2, Math.min(surface + 1, target.getMaxBuildHeight() - 4));
        return new BlockPos(x, y, z);
    }

    private static BlockPos findLandSurface(ServerLevel target, int x, int z, int extraClearance) {
        int surface = Math.max(
            target.getHeight(Heightmap.Types.WORLD_SURFACE, x, z),
            target.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z)
        );
        int y = Math.max(target.getMinBuildHeight() + 2, Math.min(surface, target.getMaxBuildHeight() - 4));

        for (int scanY = y; scanY >= target.getMinBuildHeight() + 2; --scanY) {
            BlockPos floorPos = new BlockPos(x, scanY - 1, z);
            BlockPos feetPos = new BlockPos(x, scanY, z);
            BlockPos headPos = new BlockPos(x, scanY + 1, z);
            BlockState floor = target.getBlockState(floorPos);
            BlockState feet = target.getBlockState(feetPos);
            BlockState head = target.getBlockState(headPos);
            if (floor.isFaceSturdy(target, floorPos, Direction.UP)
                && floor.getFluidState().isEmpty()
                && feet.getFluidState().isEmpty()
                && head.getFluidState().isEmpty()
                && feet.getCollisionShape(target, feetPos).isEmpty()
                && head.getCollisionShape(target, headPos).isEmpty()) {
                return new BlockPos(x, scanY + extraClearance, z);
            }
        }

        return null;
    }

    private static void prepareArrivalPlatform(ServerLevel level, BlockPos pos) {
        if (level.dimension() != CANDY_WORLD) {
            return;
        }
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                level.setBlockAndUpdate(pos.offset(x, 0, z), Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(pos.offset(x, 1, z), Blocks.AIR.defaultBlockState());
            }
        }
    }
}
