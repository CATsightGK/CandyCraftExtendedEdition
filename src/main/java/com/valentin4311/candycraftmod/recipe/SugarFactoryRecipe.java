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

public final class SugarFactoryRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final Ingredient ingredient;
    private final int ingredientCount;
    private final ItemStack result;
    private final int processingTime;
    private final boolean normalFactory;
    private final boolean advancedFactory;

    public SugarFactoryRecipe(ResourceLocation id, Ingredient ingredient, int ingredientCount, ItemStack result,
            int processingTime, boolean normalFactory, boolean advancedFactory) {
        this.id = id;
        this.ingredient = ingredient;
        this.ingredientCount = ingredientCount;
        this.result = result.copy();
        this.processingTime = processingTime;
        this.normalFactory = normalFactory;
        this.advancedFactory = advancedFactory;
    }

    @Override
    public boolean matches(SimpleContainer container, Level level) {
        return container.getContainerSize() > 0 && accepts(container.getItem(0));
    }

    public boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && stack.getCount() >= ingredientCount && ingredient.test(stack);
    }

    public boolean acceptsItem(ItemStack stack) {
        return !stack.isEmpty() && ingredient.test(stack);
    }

    public boolean supportsFactory(boolean advanced) {
        return advanced ? advancedFactory : normalFactory;
    }

    public Ingredient ingredient() {
        return ingredient;
    }

    public int ingredientCount() {
        return ingredientCount;
    }

    public ItemStack resultForNetwork() {
        return result.copy();
    }

    public int processingTime() {
        return processingTime;
    }

    public boolean normalFactory() {
        return normalFactory;
    }

    public boolean advancedFactory() {
        return advancedFactory;
    }

    @Override
    public ItemStack assemble(SimpleContainer container, RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, ingredient);
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CCBlocks.SUGAR_FACTORY.get());
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CCRecipeTypes.SUGAR_FACTORY_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return CCRecipeTypes.SUGAR_FACTORY_TYPE.get();
    }
}
