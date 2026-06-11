package com.valentin4311.candycraftmod.recipe;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCRecipeTypes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class LicoriceSmeltingRecipe extends AbstractCookingRecipe {
    private final ItemStack result;

    public LicoriceSmeltingRecipe(ResourceLocation id, String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience, int cookingTime) {
        super(CCRecipeTypes.LICORICE_SMELTING_TYPE.get(), id, group, category, ingredient, result, experience, cookingTime);
        this.result = result.copy();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result.copy();
    }

    public ItemStack resultForNetwork() {
        return result.copy();
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CCBlocks.LICORICE_FURNACE.get());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CCRecipeTypes.LICORICE_SMELTING_SERIALIZER.get();
    }
}
