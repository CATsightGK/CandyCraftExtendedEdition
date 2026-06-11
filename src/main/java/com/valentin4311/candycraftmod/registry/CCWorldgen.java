package com.valentin4311.candycraftmod.registry;

import com.mojang.serialization.Codec;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.world.CandyWorldChunkGenerator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import com.valentin4311.candycraftmod.world.CandyBiomeSource;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class CCWorldgen {
    public static final DeferredRegister<Codec<? extends ChunkGenerator>> CHUNK_GENERATORS =
        DeferredRegister.create(Registries.CHUNK_GENERATOR, CandyCraft.MODID);
    public static final DeferredRegister<Codec<? extends BiomeSource>> BIOME_SOURCES =
        DeferredRegister.create(Registries.BIOME_SOURCE, CandyCraft.MODID);

    public static final RegistryObject<Codec<CandyWorldChunkGenerator>> CANDY_WORLD_CHUNK_GENERATOR =
        CHUNK_GENERATORS.register("candy_world", () -> CandyWorldChunkGenerator.CODEC);
    public static final RegistryObject<Codec<CandyBiomeSource>> CANDY_BIOME_SOURCE =
        BIOME_SOURCES.register("candy_world", () -> CandyBiomeSource.CODEC);

    private CCWorldgen() {
    }

    public static void register(IEventBus eventBus) {
        BIOME_SOURCES.register(eventBus);
        CHUNK_GENERATORS.register(eventBus);
    }
}
