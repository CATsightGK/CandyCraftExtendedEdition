package com.valentin4311.candycraftmod.alchemy;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.common.brewing.IBrewingRecipe;

public final class CloyingBrewingRecipe implements IBrewingRecipe {
    private final Potion inputPotion;
    private final net.minecraft.world.item.Item ingredient;
    private final Potion outputPotion;

    public CloyingBrewingRecipe(Potion inputPotion, net.minecraft.world.item.Item ingredient, Potion outputPotion) {
        this.inputPotion = inputPotion;
        this.ingredient = ingredient;
        this.outputPotion = outputPotion;
    }

    @Override
    public boolean isInput(ItemStack stack) {
        return isPotionContainer(stack) && PotionUtils.getPotion(stack) == inputPotion;
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return stack.is(ingredient);
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredientStack) {
        if (!isInput(input) || !isIngredient(ingredientStack)) {
            return ItemStack.EMPTY;
        }
        ItemStack output = input.copy();
        output.setCount(1);
        return PotionUtils.setPotion(output, outputPotion);
    }

    private static boolean isPotionContainer(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
    }
}
