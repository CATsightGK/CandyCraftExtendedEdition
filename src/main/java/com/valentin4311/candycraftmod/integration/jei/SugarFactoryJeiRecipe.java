package com.valentin4311.candycraftmod.integration.jei;

import com.valentin4311.candycraftmod.block.entity.SugarFactoryBlockEntity;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record SugarFactoryJeiRecipe(ItemStack input, ItemStack output, boolean advancedFactory, ResourceLocation id) {
    public static List<SugarFactoryJeiRecipe> createRecipes() {
        return SugarFactoryBlockEntity.getDisplayRecipes().stream()
            .map(recipe -> new SugarFactoryJeiRecipe(recipe.input(), recipe.output(), recipe.advancedFactory(), recipe.id()))
            .toList();
    }
}
