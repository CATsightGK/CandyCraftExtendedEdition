package com.valentin4311.candycraftmod.world;

import com.valentin4311.candycraftmod.CandyCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public final class CCDimensions {
    public static final ResourceKey<Level> CANDY_WORLD = ResourceKey.create(
        Registries.DIMENSION,
        new ResourceLocation(CandyCraft.MODID, "candy_world")
    );
    public static final ResourceKey<DimensionType> CANDY_WORLD_TYPE = ResourceKey.create(
        Registries.DIMENSION_TYPE,
        new ResourceLocation(CandyCraft.MODID, "candy_world")
    );
    public static final ResourceKey<Level> CANDY_DUNGEON = ResourceKey.create(
        Registries.DIMENSION,
        new ResourceLocation(CandyCraft.MODID, "candy_dungeon")
    );
    public static final ResourceKey<DimensionType> CANDY_DUNGEON_TYPE = ResourceKey.create(
        Registries.DIMENSION_TYPE,
        new ResourceLocation(CandyCraft.MODID, "candy_dungeon")
    );

    private CCDimensions() {
    }
}
