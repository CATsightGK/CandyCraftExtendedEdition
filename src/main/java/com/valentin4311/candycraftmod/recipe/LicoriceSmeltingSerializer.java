package com.valentin4311.candycraftmod.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;

public class LicoriceSmeltingSerializer implements RecipeSerializer<LicoriceSmeltingRecipe> {
    @Override
    public LicoriceSmeltingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        String group = GsonHelper.getAsString(json, "group", "");
        CookingBookCategory category = CookingBookCategory.CODEC.byName(GsonHelper.getAsString(json, "category", null), CookingBookCategory.MISC);
        Ingredient ingredient = Ingredient.fromJson(GsonHelper.isArrayNode(json, "ingredient")
            ? GsonHelper.getAsJsonArray(json, "ingredient")
            : GsonHelper.getAsJsonObject(json, "ingredient"));
        ItemStack result = new ItemStack(itemFromJson(GsonHelper.getAsJsonObject(json, "result")));
        float experience = GsonHelper.getAsFloat(json, "experience", 0.0F);
        int cookingTime = GsonHelper.getAsInt(json, "cookingtime", 200);
        return new LicoriceSmeltingRecipe(recipeId, group, category, ingredient, result, experience, cookingTime);
    }

    @Override
    public LicoriceSmeltingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        String group = buffer.readUtf();
        CookingBookCategory category = buffer.readEnum(CookingBookCategory.class);
        Ingredient ingredient = Ingredient.fromNetwork(buffer);
        ItemStack result = buffer.readItem();
        float experience = buffer.readFloat();
        int cookingTime = buffer.readVarInt();
        return new LicoriceSmeltingRecipe(recipeId, group, category, ingredient, result, experience, cookingTime);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, LicoriceSmeltingRecipe recipe) {
        buffer.writeUtf(recipe.getGroup());
        buffer.writeEnum(recipe.category());
        recipe.getIngredients().get(0).toNetwork(buffer);
        buffer.writeItem(recipe.resultForNetwork());
        buffer.writeFloat(recipe.getExperience());
        buffer.writeVarInt(recipe.getCookingTime());
    }

    private static Item itemFromJson(JsonObject json) {
        String itemName = GsonHelper.getAsString(json, "item");
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName));
        if (item == null) {
            throw new com.google.gson.JsonSyntaxException("Unknown item '" + itemName + "'");
        }
        return item;
    }
}
