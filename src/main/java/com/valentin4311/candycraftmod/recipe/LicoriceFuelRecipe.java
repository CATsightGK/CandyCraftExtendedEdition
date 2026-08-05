package com.valentin4311.candycraftmod.recipe;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCRecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public final class LicoriceFuelRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final int burnTime;

    public LicoriceFuelRecipe(ResourceLocation id, Ingredient ingredient, int burnTime) {
        this.id = id;
        this.ingredient = ingredient;
        this.burnTime = burnTime;
    }

    @Override
    public boolean matches(SimpleContainer container, Level level) {
        return container.getContainerSize() > 0 && ingredient.test(container.getItem(0));
    }

    public boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && ingredient.test(stack);
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public int burnTime() {
        return burnTime;
    }

    @Override
    public ItemStack assemble(SimpleContainer container, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, ingredient);
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CCBlocks.LICORICE_FURNACE.get());
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CCRecipeTypes.LICORICE_FUEL_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return CCRecipeTypes.LICORICE_FUEL_TYPE.get();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
