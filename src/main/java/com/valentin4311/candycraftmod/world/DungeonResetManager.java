package com.valentin4311.candycraftmod.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * Server-thread work queue that spreads huge region rewrites (dungeon instance
 * resets) over multiple ticks so entering or finishing a dungeon never freezes
 * the server. Jobs may be enqueued from world-gen worker threads; draining
 * happens once per server tick within a fixed time budget.
 */
public final class DungeonResetManager {
    private static final long TIME_BUDGET_NANOS = 8_000_000L;

    private static final ConcurrentLinkedDeque<Job> JOBS = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedQueue<Runnable> IDLE_CALLBACKS = new ConcurrentLinkedQueue<>();
    private static final Map<PrepareKey, List<Runnable>> ACTIVE_PREPARES = new ConcurrentHashMap<>();

    private DungeonResetManager() {
    }

    /**
     * Schedules a full dungeon prepare (region clear followed by a build step)
     * for the instance identified by {@code dimension}+{@code origin}. Returns
     * false if a prepare for that instance is already queued or running, in
     * which case the caller should only register a callback.
     */
    public static boolean beginPrepare(ServerLevel level, BlockPos origin, int minX, int maxX, int minY, int maxY,
            int minZ, int maxZ, Runnable buildStep) {
        PrepareKey key = new PrepareKey(level.dimension(), origin);
        if (ACTIVE_PREPARES.containsKey(key)) {
            return false;
        }
        ACTIVE_PREPARES.put(key, new ArrayList<>());
        JOBS.addLast(new ClearRegionJob(level, origin, minX, maxX, minY, maxY, minZ, maxZ));
        if (buildStep != null) {
            JOBS.addLast(new UnitJob(buildStep));
        }
        JOBS.addLast(new PrepareCompletionJob(key));
        return true;
    }

    /** Registers {@code callback} to run once the prepare finishes (or immediately when none is active). */
    public static void onPrepared(ServerLevel level, BlockPos origin, Runnable callback) {
        PrepareKey key = new PrepareKey(level.dimension(), origin);
        List<Runnable> callbacks = ACTIVE_PREPARES.get(key);
        if (callbacks == null) {
            callback.run();
            return;
        }
        callbacks.add(callback);
    }

    /** Queues a standalone region clear; used when players have already left the area. */
    public static void enqueueClear(ServerLevel level, BlockPos origin, int minX, int maxX, int minY, int maxY,
            int minZ, int maxZ) {
        JOBS.addLast(new ClearRegionJob(level, origin, minX, maxX, minY, maxY, minZ, maxZ));
    }

    /** Queues an arbitrary main-thread build step behind any pending resets. */
    public static void enqueueUnit(Runnable step) {
        JOBS.addLast(new UnitJob(step));
    }

    public static void tickServer() {
        if (JOBS.isEmpty() && IDLE_CALLBACKS.isEmpty()) {
            return;
        }
        long deadline = System.nanoTime() + TIME_BUDGET_NANOS;
        while (!JOBS.isEmpty()) {
            Job job = JOBS.peekFirst();
            if (!job.advance()) {
                JOBS.pollFirst();
                if (job instanceof PrepareFinalizer finalizer) {
                    finishPrepare(finalizer);
                }
                continue;
            }
            if (System.nanoTime() >= deadline) {
                return;
            }
        }
        if (IDLE_CALLBACKS.isEmpty()) {
            return;
        }
        Runnable callback;
        while ((callback = IDLE_CALLBACKS.poll()) != null) {
            callback.run();
        }
    }

    private static void finishPrepare(PrepareFinalizer finalizer) {
        List<Runnable> callbacks = ACTIVE_PREPARES.remove(finalizer.key());
        if (callbacks != null) {
            callbacks.forEach(Runnable::run);
        }
    }

    private interface Job {
        /** Performs one bounded slice of work; returns true while more work remains. */
        boolean advance();
    }

    /** Marker for jobs that complete an ACTIVE_PREPARES entry when done. */
    private interface PrepareFinalizer {
        PrepareKey key();
    }

    private record PrepareKey(ResourceKey<Level> dimension, BlockPos origin) {
    }

    private static final class UnitJob implements Job {
        private final Runnable step;

        UnitJob(Runnable step) {
            this.step = step;
        }

        @Override
        public boolean advance() {
            step.run();
            return false;
        }
    }

    private static final class PrepareCompletionJob implements Job, PrepareFinalizer {
        private final PrepareKey key;

        PrepareCompletionJob(PrepareKey key) {
            this.key = key;
        }

        @Override
        public PrepareKey key() {
            return key;
        }

        @Override
        public boolean advance() {
            return false;
        }
    }

    /**
     * Column-major region clearer. Skips already-air positions and relies on
     * lazy synchronous chunk loads so no upfront bulk chunk load is needed.
     */
    private static final class ClearRegionJob implements Job {
        private final ServerLevel level;
        private final BlockPos origin;
        private final int minX;
        private final int maxX;
        private final int minY;
        private final int maxY;
        private final int minZ;
        private final int maxZ;
        private int cursorX;
        private int cursorY;
        private int cursorZ;

        ClearRegionJob(ServerLevel level, BlockPos origin, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
            this.level = level;
            this.origin = origin;
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.minZ = minZ;
            this.maxZ = maxZ;
            this.cursorX = minX;
            this.cursorY = minY;
            this.cursorZ = minZ;
        }

        @Override
        public boolean advance() {
            long deadline = System.nanoTime() + TIME_BUDGET_NANOS;
            while (cursorX <= maxX) {
                while (cursorZ <= maxZ) {
                    while (cursorY <= maxY) {
                        BlockPos pos = origin.offset(cursorX, cursorY, cursorZ);
                        if (!level.getBlockState(pos).isAir()) {
                            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 50);
                        }
                        cursorY++;
                        if (System.nanoTime() >= deadline) {
                            return true;
                        }
                    }
                    cursorY = minY;
                    cursorZ++;
                }
                cursorZ = minZ;
                cursorX++;
            }
            return false;
        }
    }
}
