package com.valentin4311.candycraftmod.world;

import com.valentin4311.candycraftmod.CandyCraft;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

public final class CandyFluidTickRepairData extends SavedData {
    private static final String DATA_NAME = CandyCraft.MODID + "_fluid_tick_repairs";
    private static final String CHUNKS_TAG = "Chunks";
    private final Set<Long> repairedChunks = new HashSet<>();

    public static CandyFluidTickRepairData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            CandyFluidTickRepairData::load,
            CandyFluidTickRepairData::new,
            DATA_NAME
        );
    }

    private static CandyFluidTickRepairData load(CompoundTag tag) {
        CandyFluidTickRepairData data = new CandyFluidTickRepairData();
        for (long chunk : tag.getLongArray(CHUNKS_TAG)) {
            data.repairedChunks.add(chunk);
        }
        return data;
    }

    public boolean isRepaired(ChunkPos pos) {
        return repairedChunks.contains(pos.toLong());
    }

    public void markRepaired(ChunkPos pos) {
        if (repairedChunks.add(pos.toLong())) {
            setDirty();
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        long[] chunks = new long[repairedChunks.size()];
        int index = 0;
        for (long chunk : repairedChunks) {
            chunks[index++] = chunk;
        }
        tag.putLongArray(CHUNKS_TAG, chunks);
        return tag;
    }
}
