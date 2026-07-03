package com.valentin4311.candycraftmod.integration.jei;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class SugarFactoryRecipeCategory implements IRecipeCategory<SugarFactoryJeiRecipe> {
    private static final ResourceLocation SUGAR_FACTORY_GUI = new ResourceLocation(CandyCraft.MODID, "textures/gui/jei_sugar_factory.png");
    private static final ResourceLocation ADVANCED_SUGAR_FACTORY_GUI = new ResourceLocation(CandyCraft.MODID, "textures/gui/jei_advanced_sugar_factory.png");
    private static final int CROP_X = 0;
    private static final int CROP_Y = 0;
    private static final int CROP_WIDTH = 174;
    private static final int CROP_HEIGHT = 26;
    private final IDrawable icon;
    private final IDrawableStatic sugarFactoryBackground;
    private final IDrawableStatic advancedSugarFactoryBackground;

    public SugarFactoryRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(CCBlocks.SUGAR_FACTORY.get());
        this.sugarFactoryBackground = guiHelper.createDrawable(SUGAR_FACTORY_GUI, CROP_X, CROP_Y, CROP_WIDTH, CROP_HEIGHT);
        this.advancedSugarFactoryBackground = guiHelper.createDrawable(ADVANCED_SUGAR_FACTORY_GUI, CROP_X, CROP_Y, CROP_WIDTH, CROP_HEIGHT);
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
        return CROP_WIDTH;
    }

    @Override
    public int getHeight() {
        return CROP_HEIGHT;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SugarFactoryJeiRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(8, 7)
            .addItemStacks(recipe.inputs());
        builder.addOutputSlot(152, 7)
            .addItemStack(recipe.output());
    }

    @Override
    public void draw(SugarFactoryJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IDrawableStatic background = recipe.advancedFactory() ? advancedSugarFactoryBackground : sugarFactoryBackground;
        background.draw(guiGraphics, 0, 0);
    }

    @Override
    public @Nullable net.minecraft.resources.ResourceLocation getRegistryName(SugarFactoryJeiRecipe recipe) {
        return recipe.id();
    }
}
