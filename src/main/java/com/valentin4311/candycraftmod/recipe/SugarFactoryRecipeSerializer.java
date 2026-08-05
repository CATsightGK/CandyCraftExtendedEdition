package com.valentin4311.candycraftmod.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;

public final class SugarFactoryRecipeSerializer implements RecipeSerializer<SugarFactoryRecipe> {
    @Override
    public SugarFactoryRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
        if (ingredient.isEmpty()) {
            throw new JsonSyntaxException("Sugar factory ingredient cannot be empty");
        }
        int ingredientCount = bounded(json, "input_count", 1, 1, 64);
        JsonObject resultJson = GsonHelper.getAsJsonObject(json, "result");
        ItemStack result = new ItemStack(item(GsonHelper.getAsString(resultJson, "item")),
            bounded(resultJson, "count", 1, 1, 64));
        int processingTime = bounded(json, "processing_time", 240, 1, 72000);
        String factory = GsonHelper.getAsString(json, "factory", "both");
        boolean normal = "both".equals(factory) || "normal".equals(factory);
        boolean advanced = "both".equals(factory) || "advanced".equals(factory);
        if (!normal && !advanced) {
            throw new JsonSyntaxException("factory must be 'normal', 'advanced', or 'both'");
        }
        return new SugarFactoryRecipe(recipeId, ingredient, ingredientCount, result, processingTime, normal, advanced);
    }

    @Override
    public SugarFactoryRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        Ingredient ingredient = Ingredient.fromNetwork(buffer);
        int ingredientCount = buffer.readVarInt();
        ItemStack result = buffer.readItem();
        int processingTime = buffer.readVarInt();
        return new SugarFactoryRecipe(recipeId, ingredient, ingredientCount, result, processingTime,
            buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, SugarFactoryRecipe recipe) {
        recipe.ingredient().toNetwork(buffer);
        buffer.writeVarInt(recipe.ingredientCount());
        buffer.writeItem(recipe.resultForNetwork());
        buffer.writeVarInt(recipe.processingTime());
        buffer.writeBoolean(recipe.normalFactory());
        buffer.writeBoolean(recipe.advancedFactory());
    }

    private static int bounded(JsonObject json, String key, int fallback, int min, int max) {
        int value = GsonHelper.getAsInt(json, key, fallback);
        if (value < min || value > max) {
            throw new JsonSyntaxException(key + " must be between " + min + " and " + max);
        }
        return value;
    }

    private static Item item(String id) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (item == null) {
            throw new JsonSyntaxException("Unknown item '" + id + "'");
        }
        return item;
    }
}
