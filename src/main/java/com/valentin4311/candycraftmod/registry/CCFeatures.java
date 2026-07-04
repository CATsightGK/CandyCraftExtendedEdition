package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.world.feature.CandyGrassFeature;
import com.valentin4311.candycraftmod.world.feature.CandySeaweedFeature;
import com.valentin4311.candycraftmod.world.feature.CottonCandyTreeFeature;
import com.valentin4311.candycraftmod.world.feature.GummyWormFeature;
import com.valentin4311.candycraftmod.world.feature.HoneyDungeonFeature;
import com.valentin4311.candycraftmod.world.feature.JellyDungeonFeature;
import com.valentin4311.candycraftmod.world.feature.LegacyStructureFeature;
import com.valentin4311.candycraftmod.world.feature.LegacyCandyTreeFeature;
import com.valentin4311.candycraftmod.world.feature.MarshmallowWaterlilyPatchFeature;
import com.valentin4311.candycraftmod.world.feature.SweetscapeChocolateTreeFeature;
import com.valentin4311.candycraftmod.world.feature.SuguardDungeonFeature;
import com.valentin4311.candycraftmod.world.tree.CherryTreeDecorator;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CCFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, CandyCraft.MODID);
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATOR_TYPES =
        DeferredRegister.create(ForgeRegistries.TREE_DECORATOR_TYPES, CandyCraft.MODID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> HONEY_DUNGEON = FEATURES.register(
        "honey_dungeon",
        () -> new HoneyDungeonFeature(NoneFeatureConfiguration.CODEC)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> JELLY_DUNGEON = FEATURES.register(
        "jelly_dungeon",
        () -> new JellyDungeonFeature(NoneFeatureConfiguration.CODEC)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SUGUARD_DUNGEON = FEATURES.register(
        "suguard_dungeon",
        () -> new SuguardDungeonFeature(NoneFeatureConfiguration.CODEC)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CANDY_GRASS = FEATURES.register(
        "candy_grass",
        () -> new CandyGrassFeature(NoneFeatureConfiguration.CODEC)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CANDY_SEAWEED = FEATURES.register(
        "candy_seaweed",
        () -> new CandySeaweedFeature(NoneFeatureConfiguration.CODEC)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> MARSHMALLOW_WATERLILY_PATCH = FEATURES.register(
        "marshmallow_waterlily_patch",
        () -> new MarshmallowWaterlilyPatchFeature(NoneFeatureConfiguration.CODEC)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GUMMY_WORM = FEATURES.register(
        "gummy_worm",
        () -> new GummyWormFeature(NoneFeatureConfiguration.CODEC)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> COTTON_CANDY_TREE = FEATURES.register(
        "cotton_candy_tree",
        () -> new CottonCandyTreeFeature(NoneFeatureConfiguration.CODEC)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SWEETSCAPE_CHOCOLATE_TREE = FEATURES.register(
        "sweetscape_chocolate_tree",
        () -> new SweetscapeChocolateTreeFeature(NoneFeatureConfiguration.CODEC)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CARAMEL_TREE = FEATURES.register(
        "caramel_tree",
        () -> new LegacyCandyTreeFeature(NoneFeatureConfiguration.CODEC, LegacyCandyTreeFeature.Kind.CARAMEL)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CARAMEL_FOREST_TREE = FEATURES.register(
        "caramel_forest_tree",
        () -> new LegacyCandyTreeFeature(NoneFeatureConfiguration.CODEC, LegacyCandyTreeFeature.Kind.CARAMEL_FOREST)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CHERRY_TREE = FEATURES.register(
        "cherry_tree",
        () -> new LegacyCandyTreeFeature(NoneFeatureConfiguration.CODEC, LegacyCandyTreeFeature.Kind.CHERRY)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ENCHANTED_TREE = FEATURES.register(
        "enchanted_tree",
        () -> new LegacyCandyTreeFeature(NoneFeatureConfiguration.CODEC, LegacyCandyTreeFeature.Kind.ENCHANTED)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> WHITE_CHOCOLATE_TREE = FEATURES.register(
        "white_chocolate_tree",
        () -> new LegacyCandyTreeFeature(NoneFeatureConfiguration.CODEC, LegacyCandyTreeFeature.Kind.WHITE_CHOCOLATE)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CANDY_HOUSE = FEATURES.register(
        "candy_house",
        () -> new LegacyStructureFeature(NoneFeatureConfiguration.CODEC, LegacyStructureFeature.Kind.CANDY_HOUSE)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ICE_TOWER = FEATURES.register(
        "ice_tower",
        () -> new LegacyStructureFeature(NoneFeatureConfiguration.CODEC, LegacyStructureFeature.Kind.ICE_TOWER)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> ICE_CREAM_DOME = FEATURES.register(
        "ice_cream_dome",
        () -> new LegacyStructureFeature(NoneFeatureConfiguration.CODEC, LegacyStructureFeature.Kind.ICE_CREAM_DOME)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> WATER_TEMPLE = FEATURES.register(
        "water_temple",
        () -> new LegacyStructureFeature(NoneFeatureConfiguration.CODEC, LegacyStructureFeature.Kind.WATER_TEMPLE)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> GEYSER = FEATURES.register(
        "geyser",
        () -> new LegacyStructureFeature(NoneFeatureConfiguration.CODEC, LegacyStructureFeature.Kind.GEYSER)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CHEWING_GUM_TOTEM = FEATURES.register(
        "chewing_gum_totem",
        () -> new LegacyStructureFeature(NoneFeatureConfiguration.CODEC, LegacyStructureFeature.Kind.CHEWING_GUM_TOTEM)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> FLOATING_ISLAND = FEATURES.register(
        "floating_island",
        () -> new LegacyStructureFeature(NoneFeatureConfiguration.CODEC, LegacyStructureFeature.Kind.FLOATING_ISLAND)
    );
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> UNDERGROUND_VILLAGE = FEATURES.register(
        "underground_village",
        () -> new LegacyStructureFeature(NoneFeatureConfiguration.CODEC, LegacyStructureFeature.Kind.UNDERGROUND_VILLAGE)
    );
    public static final RegistryObject<TreeDecoratorType<CherryTreeDecorator>> CHERRY_TREE_DECORATOR =
        TREE_DECORATOR_TYPES.register("cherry_fruit", () -> new TreeDecoratorType<>(CherryTreeDecorator.CODEC));

    private CCFeatures() {
    }

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
        TREE_DECORATOR_TYPES.register(eventBus);
    }
}
