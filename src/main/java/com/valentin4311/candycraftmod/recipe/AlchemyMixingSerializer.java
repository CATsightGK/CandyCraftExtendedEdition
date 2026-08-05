package com.valentin4311.candycraftmod.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.valentin4311.candycraftmod.block.entity.AlchemyLiquidKind;
import com.valentin4311.candycraftmod.item.SugarPillItem;
import com.valentin4311.candycraftmod.recipe.AlchemyMixingRecipe.IngredientAmount;
import com.valentin4311.candycraftmod.registry.CCItems;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.ForgeRegistries;

public final class AlchemyMixingSerializer implements RecipeSerializer<AlchemyMixingRecipe> {
    private static final int DEFAULT_MIXING_TIME = 20 * 20;
    private static final int DEFAULT_SUGAR_MIXING_TIME = 10 * 20;

    @Override
    public AlchemyMixingRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
        JsonArray ingredientsJson = GsonHelper.getAsJsonArray(json, "ingredients");
        List<IngredientAmount> ingredients = new ArrayList<>();
        int total = 0;
        for (int i = 0; i < ingredientsJson.size(); i++) {
            JsonObject entry = GsonHelper.convertToJsonObject(ingredientsJson.get(i), "ingredients[" + i + "]");
            Ingredient ingredient = Ingredient.fromJson(entry.get("ingredient"));
            int count = GsonHelper.getAsInt(entry, "count", 1);
            if (ingredient.isEmpty() || count < 1 || count > AlchemyMixingRecipe.REQUIRED_INPUTS) {
                throw new JsonSyntaxException("Alchemy ingredient count must be between 1 and 4");
            }
            total += count;
            ingredients.add(new IngredientAmount(ingredient, count));
        }
        if (total != AlchemyMixingRecipe.REQUIRED_INPUTS) {
            throw new JsonSyntaxException("Alchemy recipes must consume exactly 4 material slots, got " + total);
        }

        String liquidId = GsonHelper.getAsString(json, "liquid", AlchemyLiquidKind.GRENADINE.id());
        AlchemyLiquidKind liquid = AlchemyLiquidKind.byId(liquidId);
        if (liquid == AlchemyLiquidKind.NONE) {
            throw new JsonSyntaxException("Unknown or unusable alchemy liquid '" + liquidId + "'");
        }
        ItemStack result = parseResult(GsonHelper.getAsJsonObject(json, "result"));
        int mixingTime = positiveTime(json, "mixing_time", DEFAULT_MIXING_TIME);
        int sugarMixingTime = positiveTime(json, "sugar_mixing_time", DEFAULT_SUGAR_MIXING_TIME);
        if (sugarMixingTime > mixingTime) {
            throw new JsonSyntaxException("sugar_mixing_time cannot be longer than mixing_time");
        }
        return new AlchemyMixingRecipe(recipeId, ingredients, liquid, result, mixingTime, sugarMixingTime);
    }

    private static ItemStack parseResult(JsonObject resultJson) {
        String type = GsonHelper.getAsString(resultJson, "type", "item");
        int count = GsonHelper.getAsInt(resultJson, "count", 1);
        if (count < 1 || count > 64) {
            throw new JsonSyntaxException("Alchemy result count must be between 1 and 64");
        }
        if ("item".equals(type)) {
            Item item = registryItem(GsonHelper.getAsString(resultJson, "item"));
            return new ItemStack(item, count);
        }
        if (!"sugar_pill".equals(type)) {
            throw new JsonSyntaxException("Unknown alchemy result type '" + type + "'");
        }

        List<MobEffectInstance> effects = new ArrayList<>();
        JsonArray effectsJson = GsonHelper.getAsJsonArray(resultJson, "effects", new JsonArray());
        for (int i = 0; i < effectsJson.size(); i++) {
            JsonObject effectJson = GsonHelper.convertToJsonObject(effectsJson.get(i), "effects[" + i + "]");
            String effectId = GsonHelper.getAsString(effectJson, "id");
            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(effectId));
            if (effect == null) {
                throw new JsonSyntaxException("Unknown mob effect '" + effectId + "'");
            }
            int duration = positiveTime(effectJson, "duration", 20 * 60);
            int amplifier = GsonHelper.getAsInt(effectJson, "amplifier", 0);
            if (amplifier < 0 || amplifier > 255) {
                throw new JsonSyntaxException("Effect amplifier must be between 0 and 255");
            }
            effects.add(new MobEffectInstance(effect, duration, amplifier, false, true, true));
        }

        int[] colors = {0xFFFFFF, 0xFFFFFF, 0xFFFFFF, 0xFFFFFF};
        JsonArray colorsJson = GsonHelper.getAsJsonArray(resultJson, "colors", new JsonArray());
        for (int i = 0; i < Math.min(colors.length, colorsJson.size()); i++) {
            colors[i] = parseColor(colorsJson.get(i).getAsString());
        }
        ItemStack pill = new ItemStack(CCItems.SUGAR_PILL.get(), count);
        SugarPillItem.setData(pill, effects, colors);
        return pill;
    }

    private static int parseColor(String value) {
        String clean = value.startsWith("#") ? value.substring(1) : value;
        if (!clean.matches("[0-9a-fA-F]{6}")) {
            throw new JsonSyntaxException("Colors must use six hexadecimal digits");
        }
        return Integer.parseInt(clean, 16);
    }

    private static int positiveTime(JsonObject json, String key, int fallback) {
        int value = GsonHelper.getAsInt(json, key, fallback);
        if (value < 1) {
            throw new JsonSyntaxException(key + " must be at least 1 tick");
        }
        return value;
    }

    private static Item registryItem(String id) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        if (item == null) {
            throw new JsonSyntaxException("Unknown item '" + id + "'");
        }
        return item;
    }

    @Override
    public AlchemyMixingRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<IngredientAmount> ingredients = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            ingredients.add(new IngredientAmount(Ingredient.fromNetwork(buffer), buffer.readVarInt()));
        }
        ItemStack result = buffer.readItem();
        AlchemyLiquidKind liquid = buffer.readEnum(AlchemyLiquidKind.class);
        return new AlchemyMixingRecipe(recipeId, ingredients, liquid, result, buffer.readVarInt(), buffer.readVarInt());
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, AlchemyMixingRecipe recipe) {
        buffer.writeVarInt(recipe.ingredientAmounts().size());
        for (IngredientAmount entry : recipe.ingredientAmounts()) {
            entry.ingredient().toNetwork(buffer);
            buffer.writeVarInt(entry.count());
        }
        buffer.writeItem(recipe.resultForNetwork());
        buffer.writeEnum(recipe.liquid());
        buffer.writeVarInt(recipe.normalMixingTime());
        buffer.writeVarInt(recipe.sugarMixingTime());
    }
}
