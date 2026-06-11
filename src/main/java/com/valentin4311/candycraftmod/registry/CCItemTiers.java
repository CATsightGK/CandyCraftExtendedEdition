package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;

public final class CCItemTiers {
    public static final Tier MARSHMALLOW = new ForgeTier(1, 131, 4.0F, 1.0F, 8, needsTool("marshmallow"), () -> Ingredient.of(CCBlocks.MARSHMALLOW_PLANKS.get()));
    public static final Tier LICORICE = new ForgeTier(2, 250, 6.0F, 2.0F, 12, needsTool("licorice"), () -> Ingredient.of(CCBlocks.LICORICE_BLOCK.get()));
    public static final Tier HONEY = new ForgeTier(2, 220, 6.0F, 2.0F, 18, needsTool("honey"), () -> Ingredient.of(CCItems.HONEY_SHARD.get()));
    public static final Tier PEZ = new ForgeTier(3, 561, 8.0F, 3.0F, 14, needsTool("pez"), () -> Ingredient.of(CCBlocks.PEZ_BLOCK.get()));

    private CCItemTiers() {
    }

    private static TagKey<Block> needsTool(String name) {
        return BlockTags.create(new ResourceLocation(CandyCraft.MODID, "needs_" + name + "_tool"));
    }
}
