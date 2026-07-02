package com.valentin4311.candycraftmod.integration.jei;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.entity.SugarFactoryBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record SugarFactoryJeiRecipe(List<ItemStack> inputs, ItemStack output, boolean advancedFactory, ResourceLocation id) {
    public static List<SugarFactoryJeiRecipe> createRecipes() {
        List<SugarFactoryJeiRecipe> recipes = new ArrayList<>();
        List<ItemStack> sugarInputs = new ArrayList<>();

        for (SugarFactoryBlockEntity.DisplayRecipe recipe : SugarFactoryBlockEntity.getDisplayRecipes()) {
            if (!recipe.advancedFactory() && recipe.output().is(Items.SUGAR)) {
                sugarInputs.add(recipe.input().copy());
            } else {
                recipes.add(new SugarFactoryJeiRecipe(List.of(recipe.input().copy()), recipe.output().copy(), recipe.advancedFactory(), recipe.id()));
            }
        }

        if (!sugarInputs.isEmpty()) {
            recipes.add(0, new SugarFactoryJeiRecipe(
                List.copyOf(sugarInputs),
                new ItemStack(Items.SUGAR),
                false,
                new ResourceLocation(CandyCraft.MODID, "sugar_factory/candy_items_to_sugar")
            ));
        }
        return List.copyOf(recipes);
    }
}
