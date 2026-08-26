package com.valentin4311.candycraftmod.world;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.DungeonTeleporterBlock;
import com.valentin4311.candycraftmod.block.DungeonTeleporterBlock.DungeonKind;
import com.valentin4311.candycraftmod.block.DungeonTeleporterBlock.PortalRole;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public final class DungeonProgressData extends SavedData {
    private static final String DATA_NAME = CandyCraft.MODID + "_dungeon_progress";
    private final Map<ProgressKey, Progress> progress = new HashMap<>();
    private final Map<PortalKey, PortalRecord> portals = new HashMap<>();
    private long nextInstanceId = 1L;

    public static DungeonProgressData get(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("CandyCraft dungeon progress requires the overworld");
        }
        return overworld.getDataStorage().computeIfAbsent(
            DungeonProgressData::load,
            DungeonProgressData::new,
            DATA_NAME
        );
    }

    private static DungeonProgressData load(CompoundTag tag) {
        DungeonProgressData data = new DungeonProgressData();
        data.nextInstanceId = Math.max(1L, tag.getLong("NextInstanceId"));
        ListTag progressList = tag.getList("Progress", Tag.TAG_COMPOUND);
        for (Tag raw : progressList) {
            CompoundTag entry = (CompoundTag)raw;
            if (!entry.hasUUID("Owner")) {
                continue;
            }
            DungeonKind kind = DungeonKind.byName(entry.getString("Kind"));
            Progress value = new Progress(entry.getInt("Completions"));
            if (entry.contains("Active", Tag.TAG_COMPOUND)) {
                CompoundTag active = entry.getCompound("Active");
                value.active = new Instance(
                    active.getLong("Id"),
                    BlockPos.of(active.getLong("Origin")),
                    active.getBoolean("GenerationStarted") || active.getBoolean("Generated"),
                    active.getBoolean("Generated"),
                    active.getBoolean("BossDefeated")
                );
            }
            data.progress.put(new ProgressKey(entry.getUUID("Owner"), kind), value);
        }
        ListTag portalList = tag.getList("Portals", Tag.TAG_COMPOUND);
        for (Tag raw : portalList) {
            CompoundTag entry = (CompoundTag)raw;
            if (!entry.hasUUID("Owner")) {
                continue;
            }
            PortalKey key = new PortalKey(entry.getString("Dimension"), entry.getLong("Pos"));
            PortalRecord value = new PortalRecord(
                entry.getUUID("Owner"),
                DungeonKind.byName(entry.getString("Kind")),
                entry.getLong("Instance")
            );
            data.portals.put(key, value);
        }
        return data;
    }

    public Instance getOrCreate(ServerPlayer player, DungeonKind kind) {
        Progress value = progress.computeIfAbsent(new ProgressKey(player.getUUID(), kind), key -> new Progress(0));
        if (value.active != null) {
            return value.active;
        }

        BlockPos origin = randomOrigin(player.server, kind);
        value.active = new Instance(nextInstanceId++, origin, false, false, false);
        setDirty();
        return value.active;
    }

    @Nullable
    public Instance getActive(UUID owner, DungeonKind kind) {
        Progress value = progress.get(new ProgressKey(owner, kind));
        return value == null ? null : value.active;
    }

    public int getCompletionCount(UUID owner, DungeonKind kind) {
        Progress value = progress.get(new ProgressKey(owner, kind));
        return value == null ? 0 : value.completions;
    }

    public boolean isActive(UUID owner, DungeonKind kind, long instanceId) {
        Instance active = getActive(owner, kind);
        return active != null && active.id == instanceId;
    }

    public void markGenerated(UUID owner, DungeonKind kind, long instanceId) {
        Instance active = getActive(owner, kind);
        if (active != null && active.id == instanceId && !active.generated) {
            active.generationStarted = true;
            active.generated = true;
            setDirty();
        }
    }

    public void markGenerationStarted(UUID owner, DungeonKind kind, long instanceId) {
        Instance active = getActive(owner, kind);
        if (active != null && active.id == instanceId && !active.generationStarted) {
            active.generationStarted = true;
            setDirty();
        }
    }

    public boolean markBossDefeated(DungeonKind kind, BlockPos bossPos) {
        for (Map.Entry<ProgressKey, Progress> entry : progress.entrySet()) {
            Instance active = entry.getValue().active;
            if (entry.getKey().kind == kind && active != null && contains(kind, active.origin, bossPos)) {
                if (!active.bossDefeated) {
                    active.bossDefeated = true;
                    setDirty();
                }
                return true;
            }
        }
        return false;
    }

    public boolean markCompleted(UUID owner, DungeonKind kind, long instanceId) {
        Instance active = getActive(owner, kind);
        if (active == null || active.id != instanceId) {
            return false;
        }
        if (!active.bossDefeated) {
            active.bossDefeated = true;
            setDirty();
        }
        return true;
    }

    public boolean finish(UUID owner, DungeonKind kind, long instanceId) {
        Progress value = progress.get(new ProgressKey(owner, kind));
        if (value == null || value.active == null || value.active.id != instanceId || !value.active.bossDefeated) {
            return false;
        }
        value.completions++;
        value.active = null;
        setDirty();
        return true;
    }

    public void registerPortal(ServerLevel level, BlockPos pos, UUID owner, DungeonKind kind, long instanceId) {
        portals.put(portalKey(level, pos), new PortalRecord(owner, kind, instanceId));
        setDirty();
    }

    @Nullable
    public PortalRecord getPortal(ServerLevel level, BlockPos pos) {
        return portals.get(portalKey(level, pos));
    }

    public void removePortal(ServerLevel level, BlockPos pos) {
        if (portals.remove(portalKey(level, pos)) != null) {
            setDirty();
        }
    }

    public List<LocatedPortal> getPortals(UUID owner, DungeonKind kind, long instanceId) {
        List<LocatedPortal> result = new ArrayList<>();
        for (Map.Entry<PortalKey, PortalRecord> entry : portals.entrySet()) {
            PortalRecord record = entry.getValue();
            if (record.owner.equals(owner) && record.kind == kind && record.instanceId == instanceId) {
                result.add(new LocatedPortal(entry.getKey().dimension, BlockPos.of(entry.getKey().pos)));
            }
        }
        return result;
    }

    public boolean hasLiveEntrancePortal(MinecraftServer server, UUID owner, DungeonKind kind, long instanceId) {
        boolean removedStaleRecord = false;
        Iterator<Map.Entry<PortalKey, PortalRecord>> iterator = portals.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<PortalKey, PortalRecord> entry = iterator.next();
            PortalRecord record = entry.getValue();
            if (!record.owner.equals(owner) || record.kind != kind || record.instanceId != instanceId) {
                continue;
            }

            ResourceLocation dimensionId = ResourceLocation.tryParse(entry.getKey().dimension);
            ServerLevel level = dimensionId == null
                ? null
                : server.getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));
            BlockPos pos = BlockPos.of(entry.getKey().pos);
            if (level != null && !level.hasChunkAt(pos)) {
                if (removedStaleRecord) {
                    setDirty();
                }
                return true;
            }
            if (level != null) {
                net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof DungeonTeleporterBlock
                    && state.getValue(DungeonTeleporterBlock.DUNGEON) == kind
                    && state.getValue(DungeonTeleporterBlock.ROLE) == PortalRole.ENTRY) {
                    if (removedStaleRecord) {
                        setDirty();
                    }
                    return true;
                }
            }
            iterator.remove();
            removedStaleRecord = true;
        }
        if (removedStaleRecord) {
            setDirty();
        }
        return false;
    }

    public void removePortal(String dimension, BlockPos pos) {
        if (portals.remove(new PortalKey(dimension, pos.asLong())) != null) {
            setDirty();
        }
    }

    private BlockPos randomOrigin(MinecraftServer server, DungeonKind kind) {
        int spacing = kind == DungeonKind.JELLY ? 1024 : 768;
        for (int attempt = 0; attempt < 256; attempt++) {
            int x = (server.overworld().getRandom().nextInt(2001) - 1000) * spacing;
            int z = (server.overworld().getRandom().nextInt(2001) - 1000) * spacing;
            BlockPos candidate = new BlockPos(x, 64, z);
            if (!isOriginClaimed(kind, candidate)) {
                return candidate;
            }
        }
        return new BlockPos((int)(nextInstanceId * spacing), 64, (int)(nextInstanceId * spacing));
    }

    private boolean isOriginClaimed(DungeonKind kind, BlockPos origin) {
        for (Map.Entry<ProgressKey, Progress> entry : progress.entrySet()) {
            Instance active = entry.getValue().active;
            if (entry.getKey().kind == kind && active != null && active.origin.distSqr(origin) < 600.0D * 600.0D) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(DungeonKind kind, BlockPos origin, BlockPos pos) {
        if (kind == DungeonKind.JELLY) {
            return pos.getX() >= origin.getX() - 36 && pos.getX() <= origin.getX() + 36
                && pos.getY() >= origin.getY() - 7 && pos.getY() <= origin.getY() + 56
                && pos.getZ() >= origin.getZ() - 430 && pos.getZ() <= origin.getZ() + 24;
        }
        return pos.getX() >= origin.getX() - 132 && pos.getX() <= origin.getX() + 64
            && pos.getY() >= origin.getY() - 63 && pos.getY() <= origin.getY() + 190
            && pos.getZ() >= origin.getZ() - 160 && pos.getZ() <= origin.getZ() + 160;
    }

    private static PortalKey portalKey(ServerLevel level, BlockPos pos) {
        return new PortalKey(level.dimension().location().toString(), pos.asLong());
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLong("NextInstanceId", nextInstanceId);
        ListTag progressList = new ListTag();
        for (Map.Entry<ProgressKey, Progress> mapEntry : progress.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Owner", mapEntry.getKey().owner);
            entry.putString("Kind", mapEntry.getKey().kind.getSerializedName());
            Progress value = mapEntry.getValue();
            entry.putInt("Completions", value.completions);
            if (value.active != null) {
                CompoundTag active = new CompoundTag();
                active.putLong("Id", value.active.id);
                active.putLong("Origin", value.active.origin.asLong());
                active.putBoolean("GenerationStarted", value.active.generationStarted);
                active.putBoolean("Generated", value.active.generated);
                active.putBoolean("BossDefeated", value.active.bossDefeated);
                entry.put("Active", active);
            }
            progressList.add(entry);
        }
        tag.put("Progress", progressList);

        ListTag portalList = new ListTag();
        for (Map.Entry<PortalKey, PortalRecord> mapEntry : portals.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Dimension", mapEntry.getKey().dimension);
            entry.putLong("Pos", mapEntry.getKey().pos);
            PortalRecord value = mapEntry.getValue();
            entry.putUUID("Owner", value.owner);
            entry.putString("Kind", value.kind.getSerializedName());
            entry.putLong("Instance", value.instanceId);
            portalList.add(entry);
        }
        tag.put("Portals", portalList);
        return tag;
    }

    private record ProgressKey(UUID owner, DungeonKind kind) {
    }

    private record PortalKey(String dimension, long pos) {
    }

    private static final class Progress {
        private int completions;
        @Nullable
        private Instance active;

        private Progress(int completions) {
            this.completions = completions;
        }
    }

    public static final class Instance {
        private final long id;
        private final BlockPos origin;
        private boolean generationStarted;
        private boolean generated;
        private boolean bossDefeated;

        private Instance(long id, BlockPos origin, boolean generationStarted, boolean generated, boolean bossDefeated) {
            this.id = id;
            this.origin = origin;
            this.generationStarted = generationStarted;
            this.generated = generated;
            this.bossDefeated = bossDefeated;
        }

        public long id() {
            return id;
        }

        public BlockPos origin() {
            return origin;
        }

        public boolean generated() {
            return generated;
        }

        public boolean generationStarted() {
            return generationStarted;
        }

        public boolean bossDefeated() {
            return bossDefeated;
        }
    }

    public record PortalRecord(UUID owner, DungeonKind kind, long instanceId) {
    }

    public record LocatedPortal(String dimension, BlockPos pos) {
    }
}
