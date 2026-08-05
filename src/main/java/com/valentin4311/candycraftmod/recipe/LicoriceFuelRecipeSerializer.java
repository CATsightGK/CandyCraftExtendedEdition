package com.valentin4311.candycraftmod.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class LicoriceFuelRecipeSerializer implements RecipeSerializer<LicoriceFuelRecipe> {
    @Override
    public LicoriceFuelRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        Ingredient ingredient = Ingredient.fromJson(json.get("ingredient"));
        int burnTime = GsonHelper.getAsInt(json, "burn_time", 300);
        if (ingredient.isEmpty() || burnTime < 1 || burnTime > 72000) {
            throw new JsonSyntaxException("Licorice fuel requires an ingredient and burn_time between 1 and 72000");
        }
        return new LicoriceFuelRecipe(recipeId, ingredient, burnTime);
    }

    @Override
    public LicoriceFuelRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        return new LicoriceFuelRecipe(recipeId, Ingredient.fromNetwork(buffer), buffer.readVarInt());
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, LicoriceFuelRecipe recipe) {
        recipe.ingredient().toNetwork(buffer);
        buffer.writeVarInt(recipe.burnTime());
    }
}
