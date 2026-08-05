package com.valentin4311.candycraftmod.world.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.valentin4311.candycraftmod.registry.CCStructures;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public final class FeatureLocatorStructure extends Structure {
    public static final Codec<FeatureLocatorStructure> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        settingsCodec(instance),
        ResourceLocation.CODEC.fieldOf("feature").forGetter(FeatureLocatorStructure::feature)
    ).apply(instance, FeatureLocatorStructure::new));

    private final ResourceLocation feature;

    public FeatureLocatorStructure(StructureSettings settings, ResourceLocation feature) {
        super(settings);
        this.feature = feature;
    }

    public ResourceLocation feature() {
        return feature;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        return Optional.empty();
    }

    @Override
    public StructureType<?> type() {
        return CCStructures.FEATURE_LOCATOR.get();
    }
}
