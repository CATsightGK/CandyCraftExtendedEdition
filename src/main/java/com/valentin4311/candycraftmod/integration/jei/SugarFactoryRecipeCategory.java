package com.valentin4311.candycraftmod.integration.jei;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
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
    private static final ResourceLocation SUGAR_FACTORY_GUI = new ResourceLocation(CandyCraft.MODID, "textures/gui/gui_sugar.png");
    private static final ResourceLocation ADVANCED_SUGAR_FACTORY_GUI = new ResourceLocation(CandyCraft.MODID, "textures/gui/gui_advancedsugar.png");
    private static final int CROP_X = 4;
    private static final int CROP_Y = 4;
    private static final int CROP_WIDTH = 166;
    private static final int CROP_HEIGHT = 50;
    private final IDrawable icon;
    private final IDrawableStatic sugarFactoryBackground;
    private final IDrawableStatic advancedSugarFactoryBackground;
    private final IDrawableAnimated arrow;

    public SugarFactoryRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(CCBlocks.SUGAR_FACTORY.get());
        this.sugarFactoryBackground = guiHelper.createDrawable(SUGAR_FACTORY_GUI, CROP_X, CROP_Y, CROP_WIDTH, CROP_HEIGHT);
        this.advancedSugarFactoryBackground = guiHelper.createDrawable(ADVANCED_SUGAR_FACTORY_GUI, CROP_X, CROP_Y, CROP_WIDTH, CROP_HEIGHT);
        IDrawableStatic progress = guiHelper.createDrawable(SUGAR_FACTORY_GUI, 0, 114, 120, 12);
        this.arrow = guiHelper.createAnimatedDrawable(progress, 240, IDrawableAnimated.StartDirection.LEFT, false);
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
        builder.addInputSlot(8 - CROP_X, 33 - CROP_Y)
            .addItemStacks(recipe.inputs());
        builder.addOutputSlot(152 - CROP_X, 33 - CROP_Y)
            .addItemStack(recipe.output());
    }

    @Override
    public void draw(SugarFactoryJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IDrawableStatic background = recipe.advancedFactory() ? advancedSugarFactoryBackground : sugarFactoryBackground;
        background.draw(guiGraphics, 0, 0);
        arrow.draw(guiGraphics, 27 - CROP_X, 9 - CROP_Y);
        drawFrame(guiGraphics, recipe.advancedFactory());
    }

    private static void drawFrame(GuiGraphics guiGraphics, boolean advanced) {
        int light = advanced ? 0xFFEAA15B : 0xFFE7BE76;
        int mid = advanced ? 0xFFC56A32 : 0xFFAA6E35;
        int dark = advanced ? 0xFF6D2B18 : 0xFF5A3824;
        guiGraphics.fill(0, 0, CROP_WIDTH, 1, light);
        guiGraphics.fill(0, 1, 1, CROP_HEIGHT, light);
        guiGraphics.fill(1, CROP_HEIGHT - 1, CROP_WIDTH, CROP_HEIGHT, dark);
        guiGraphics.fill(CROP_WIDTH - 1, 1, CROP_WIDTH, CROP_HEIGHT, dark);
        guiGraphics.fill(1, 1, CROP_WIDTH - 1, 2, mid);
        guiGraphics.fill(1, 2, 2, CROP_HEIGHT - 1, mid);
    }

    @Override
    public @Nullable net.minecraft.resources.ResourceLocation getRegistryName(SugarFactoryJeiRecipe recipe) {
        return recipe.id();
    }
}
