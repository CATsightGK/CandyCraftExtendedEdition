package com.valentin4311.candycraftmod.integration.jei;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.entity.SugarFactoryBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record SugarFactoryJeiRecipe(List<ItemStack> inputs, ItemStack output, boolean normalFactory, boolean advancedFactory, ResourceLocation id) {
    public static List<SugarFactoryJeiRecipe> createRecipes() {
        List<SugarFactoryJeiRecipe> recipes = new ArrayList<>();
        List<ItemStack> sugarInputs = new ArrayList<>();
        List<ItemStack> advancedSugarInputs = new ArrayList<>();

        for (SugarFactoryBlockEntity.DisplayRecipe recipe : SugarFactoryBlockEntity.getDisplayRecipes()) {
            if (recipe.output().is(Items.SUGAR)) {
                if (recipe.normalFactory() && recipe.advancedFactory()) {
                    sugarInputs.add(recipe.input().copy());
                } else if (recipe.advancedFactory()) {
                    advancedSugarInputs.add(recipe.input().copy());
                } else if (recipe.normalFactory()) {
                    sugarInputs.add(recipe.input().copy());
                }
            } else {
                recipes.add(new SugarFactoryJeiRecipe(List.of(recipe.input().copy()), recipe.output().copy(), recipe.normalFactory(), recipe.advancedFactory(), recipe.id()));
            }
        }

        if (!advancedSugarInputs.isEmpty()) {
            recipes.add(0, new SugarFactoryJeiRecipe(
                List.copyOf(advancedSugarInputs),
                new ItemStack(Items.SUGAR),
                false,
                true,
                new ResourceLocation(CandyCraft.MODID, "sugar_factory/candy_items_to_sugar_advanced")
            ));
        }
        if (!sugarInputs.isEmpty()) {
            recipes.add(0, new SugarFactoryJeiRecipe(
                List.copyOf(sugarInputs),
                new ItemStack(Items.SUGAR),
                true,
                true,
                new ResourceLocation(CandyCraft.MODID, "sugar_factory/candy_items_to_sugar")
            ));
        }
        return List.copyOf(recipes);
    }
}
