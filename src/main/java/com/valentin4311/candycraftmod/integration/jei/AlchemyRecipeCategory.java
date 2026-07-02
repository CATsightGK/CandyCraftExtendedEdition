package com.valentin4311.candycraftmod.integration.jei;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class AlchemyRecipeCategory implements IRecipeCategory<AlchemyJeiRecipe> {
    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public AlchemyRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(CCBlocks.ALCHEMY_TABLE.get());
        this.arrow = guiHelper.createAnimatedRecipeArrow(200);
    }

    @Override
    public RecipeType<AlchemyJeiRecipe> getRecipeType() {
        return CandyCraftJeiPlugin.ALCHEMY_TABLE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("container.candycraftmod.alchemy_table");
    }

    @Override
    public int getWidth() {
        return 150;
    }

    @Override
    public int getHeight() {
        return 72;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AlchemyJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(4, 22)
            .setStandardSlotBackground()
            .addItemStack(new ItemStack(CCItems.GRENADINE_BUCKET.get()));
        builder.addInputSlot(27, 10)
            .setStandardSlotBackground()
            .addItemStack(recipe.inputs().get(0));
        builder.addInputSlot(47, 10)
            .setStandardSlotBackground()
            .addItemStack(recipe.inputs().get(1));
        builder.addInputSlot(27, 32)
            .setStandardSlotBackground()
            .addItemStack(recipe.inputs().get(2));
        builder.addInputSlot(47, 32)
            .setStandardSlotBackground()
            .addItemStack(recipe.inputs().get(3));
        builder.addInputSlot(72, 22)
            .setStandardSlotBackground()
            .addItemStack(new ItemStack(Items.SUGAR));
        builder.addOutputSlot(118, 22)
            .setOutputSlotBackground()
            .addItemStack(recipe.output());
    }

    @Override
    public void draw(AlchemyJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 93, 22);
        guiGraphics.drawString(Minecraft.getInstance().font, Component.literal("Grenadine + 4 ingredients + sugar"), 3, 58, 0x7A3C56, false);
    }

    @Override
    public @Nullable net.minecraft.resources.ResourceLocation getRegistryName(AlchemyJeiRecipe recipe) {
        return recipe.id();
    }
}
