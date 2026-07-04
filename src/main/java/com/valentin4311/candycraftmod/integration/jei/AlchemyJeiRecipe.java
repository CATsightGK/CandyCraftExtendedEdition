package com.valentin4311.candycraftmod.integration.jei;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.alchemy.AlchemyMixing;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public record AlchemyJeiRecipe(List<ItemStack> inputs, ItemStack output, ResourceLocation id, boolean infoPage) {
    public static List<AlchemyJeiRecipe> createRecipes() {
        List<AlchemyJeiRecipe> recipes = new ArrayList<>();
        recipes.add(new AlchemyJeiRecipe(
            List.of(),
            ItemStack.EMPTY,
            new ResourceLocation(CandyCraft.MODID, "alchemy_table/000_instructions"),
            true
        ));
        for (ItemStack ingredient : AlchemyMixing.getDisplayIngredientStacks()) {
            List<ItemStack> inputs = new ArrayList<>(AlchemyMixing.INPUT_SLOTS);
            for (int i = 0; i < AlchemyMixing.INPUT_SLOTS; i++) {
                inputs.add(ingredient.copy());
            }

            ItemStack output = AlchemyMixing.craft(inputs);
            if (!output.isEmpty()) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(ingredient.getItem());
                String path = id == null ? ingredient.getDescriptionId() : id.getPath();
                recipes.add(new AlchemyJeiRecipe(List.copyOf(inputs), output, new ResourceLocation(CandyCraft.MODID, "alchemy_table/" + sanitizeId(path)), false));
            }
        }
        return List.copyOf(recipes);
    }

    private static String sanitizeId(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_./-]", "_");
    }
}
