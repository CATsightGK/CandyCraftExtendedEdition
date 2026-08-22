package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class CCItemTags {
    public static final TagKey<Item> HONEY_SOURCE_TOOLS = create("honey_source_tools");
    public static final TagKey<Item> HONEY_SOURCE_RANGED = create("honey_source_ranged");
    public static final TagKey<Item> PROPOLIS_BONUS_TOOLS = create("propolis_bonus_tools");
    public static final TagKey<Item> SWEET_FOODS = create("sweet_foods");

    private CCItemTags() {
    }

    private static TagKey<Item> create(String path) {
        return ItemTags.create(new ResourceLocation(CandyCraft.MODID, path));
    }
}
