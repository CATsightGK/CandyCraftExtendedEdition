package com.valentin4311.candycraftmod.effect;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCItemTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public final class CloyingSweetFoodHelper {
    private static final ItemStack SUGAR = new ItemStack(Items.SUGAR);

    private CloyingSweetFoodHelper() {
    }

    public static boolean isSweetFood(ItemStack stack, Level level) {
        if (stack.isEmpty() || !stack.isEdible()) {
            return false;
        }
        if (stack.is(CCItemTags.SWEET_FOODS)) {
            return true;
        }

        ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId != null && CandyCraft.MODID.equals(itemId.getNamespace())) {
            return true;
        }

        for (Recipe<?> recipe : level.getRecipeManager().getRecipes()) {
            ItemStack result = recipe.getResultItem(level.registryAccess());
            if (!result.is(stack.getItem())) {
                continue;
            }
            for (Ingredient ingredient : recipe.getIngredients()) {
                if (ingredient.test(SUGAR)) {
                    return true;
                }
            }
        }
        return false;
    }
}
