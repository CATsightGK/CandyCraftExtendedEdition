package com.valentin4311.candycraftmod.recipe;

import com.valentin4311.candycraftmod.block.entity.AlchemyLiquidKind;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCRecipeTypes;
import java.util.ArrayList;
import java.util.List;
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

public final class AlchemyMixingRecipe implements Recipe<SimpleContainer> {
    public static final int REQUIRED_INPUTS = 4;
    private final ResourceLocation id;
    private final List<IngredientAmount> ingredients;
    private final AlchemyLiquidKind liquid;
    private final ItemStack result;
    private final int mixingTime;
    private final int sugarMixingTime;

    public AlchemyMixingRecipe(ResourceLocation id, List<IngredientAmount> ingredients, AlchemyLiquidKind liquid, ItemStack result,
            int mixingTime, int sugarMixingTime) {
        this.id = id;
        this.ingredients = List.copyOf(ingredients);
        this.liquid = liquid;
        this.result = result.copy();
        this.mixingTime = mixingTime;
        this.sugarMixingTime = sugarMixingTime;
    }

    @Override
    public boolean matches(SimpleContainer container, Level level) {
        List<ItemStack> inputs = new ArrayList<>(REQUIRED_INPUTS);
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }
        if (inputs.size() != REQUIRED_INPUTS || totalIngredientCount() != REQUIRED_INPUTS) {
            return false;
        }

        List<Ingredient> expanded = new ArrayList<>(REQUIRED_INPUTS);
        for (IngredientAmount entry : ingredients) {
            for (int i = 0; i < entry.count(); i++) {
                expanded.add(entry.ingredient());
            }
        }
        return matchesUnordered(inputs, expanded, 0, new boolean[expanded.size()]);
    }

    private static boolean matchesUnordered(List<ItemStack> inputs, List<Ingredient> ingredients,
            int inputIndex, boolean[] used) {
        if (inputIndex >= inputs.size()) {
            return true;
        }
        ItemStack input = inputs.get(inputIndex);
        for (int i = 0; i < ingredients.size(); i++) {
            if (!used[i] && ingredients.get(i).test(input)) {
                used[i] = true;
                if (matchesUnordered(inputs, ingredients, inputIndex + 1, used)) {
                    return true;
                }
                used[i] = false;
            }
        }
        return false;
    }

    public boolean accepts(ItemStack stack) {
        return !stack.isEmpty() && ingredients.stream().anyMatch(entry -> entry.ingredient().test(stack));
    }

    public boolean matchesLiquid(AlchemyLiquidKind liquid) {
        return this.liquid == liquid;
    }

    public AlchemyLiquidKind liquid() {
        return liquid;
    }

    public int mixingTime(boolean withSugar) {
        return withSugar ? sugarMixingTime : mixingTime;
    }

    public List<IngredientAmount> ingredientAmounts() {
        return ingredients;
    }

    public ItemStack resultForNetwork() {
        return result.copy();
    }

    public int normalMixingTime() {
        return mixingTime;
    }

    public int sugarMixingTime() {
        return sugarMixingTime;
    }

    private int totalIngredientCount() {
        return ingredients.stream().mapToInt(IngredientAmount::count).sum();
    }

    @Override
    public ItemStack assemble(SimpleContainer container, RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= REQUIRED_INPUTS;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> result = NonNullList.create();
        for (IngredientAmount entry : ingredients) {
            for (int i = 0; i < entry.count(); i++) {
                result.add(entry.ingredient());
            }
        }
        return result;
    }

    @Override
    public ItemStack getToastSymbol() {
        return new ItemStack(CCBlocks.ALCHEMY_TABLE.get());
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CCRecipeTypes.ALCHEMY_MIXING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return CCRecipeTypes.ALCHEMY_MIXING_TYPE.get();
    }

    public record IngredientAmount(Ingredient ingredient, int count) {
    }
}
