package com.valentin4311.candycraftmod.integration.jei;

import com.valentin4311.candycraftmod.registry.CCBlocks;
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
import org.jetbrains.annotations.Nullable;

public class SugarFactoryRecipeCategory implements IRecipeCategory<SugarFactoryJeiRecipe> {
    private final IDrawable icon;
    private final IDrawableAnimated arrow;

    public SugarFactoryRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(CCBlocks.SUGAR_FACTORY.get());
        this.arrow = guiHelper.createAnimatedRecipeArrow(240);
    }

    @Override
    public RecipeType<SugarFactoryJeiRecipe> getRecipeType() {
        return CandyCraftJeiPlugin.SUGAR_FACTORY;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("container.candycraftmod.sugar_factory");
    }

    @Override
    public int getWidth() {
        return 130;
    }

    @Override
    public int getHeight() {
        return 58;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SugarFactoryJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(18, 22)
            .setStandardSlotBackground()
            .addItemStack(recipe.input());
        builder.addOutputSlot(94, 22)
            .setOutputSlotBackground()
            .addItemStack(recipe.output());
    }

    @Override
    public void draw(SugarFactoryJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        arrow.draw(guiGraphics, 52, 22);
        Component tier = recipe.advancedFactory()
            ? Component.translatable("container.candycraftmod.advanced_sugar_factory")
            : Component.translatable("container.candycraftmod.sugar_factory");
        guiGraphics.drawString(Minecraft.getInstance().font, tier, 6, 4, recipe.advancedFactory() ? 0xC96622 : 0x6F4A30, false);
    }

    @Override
    public @Nullable net.minecraft.resources.ResourceLocation getRegistryName(SugarFactoryJeiRecipe recipe) {
        return recipe.id();
    }
}
