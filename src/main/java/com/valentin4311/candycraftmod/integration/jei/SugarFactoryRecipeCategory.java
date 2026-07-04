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
    private static final ResourceLocation SUGAR_FACTORY_GUI = new ResourceLocation(CandyCraft.MODID, "textures/gui/jei_sugar_factory.png");
    private static final ResourceLocation ADVANCED_SUGAR_FACTORY_GUI = new ResourceLocation(CandyCraft.MODID, "textures/gui/jei_advanced_sugar_factory.png");
    private static final int CROP_X = 0;
    private static final int CROP_Y = 0;
    private static final int CROP_WIDTH = 174;
    private static final int CROP_HEIGHT = 30;
    private static final int ROW_GAP = 4;
    private static final int ADVANCED_ROW_Y = CROP_HEIGHT + ROW_GAP;
    private static final int INPUT_SLOT_X = 8;
    private static final int OUTPUT_SLOT_X = 151;
    private static final int SLOT_Y = 8;
    private static final int PROGRESS_X = 27;
    private static final int PROGRESS_Y = 10;
    private final IDrawable icon;
    private final IDrawableStatic sugarFactoryBackground;
    private final IDrawableStatic advancedSugarFactoryBackground;
    private final IDrawableAnimated sugarFactoryProgress;
    private final IDrawableAnimated advancedSugarFactoryProgress;

    public SugarFactoryRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(CCBlocks.SUGAR_FACTORY.get());
        this.sugarFactoryBackground = guiHelper.createDrawable(SUGAR_FACTORY_GUI, CROP_X, CROP_Y, CROP_WIDTH, CROP_HEIGHT);
        this.advancedSugarFactoryBackground = guiHelper.createDrawable(ADVANCED_SUGAR_FACTORY_GUI, CROP_X, CROP_Y, CROP_WIDTH, CROP_HEIGHT);
        IDrawableStatic sugarProgress = guiHelper.createDrawable(SUGAR_FACTORY_GUI, 1, 32, 120, 12);
        IDrawableStatic advancedProgress = guiHelper.createDrawable(ADVANCED_SUGAR_FACTORY_GUI, 1, 32, 120, 12);
        this.sugarFactoryProgress = guiHelper.createAnimatedDrawable(sugarProgress, 240, IDrawableAnimated.StartDirection.LEFT, false);
        this.advancedSugarFactoryProgress = guiHelper.createAnimatedDrawable(advancedProgress, 240, IDrawableAnimated.StartDirection.LEFT, false);
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
        return CROP_HEIGHT * 2 + ROW_GAP;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SugarFactoryJeiRecipe recipe, IFocusGroup focuses) {
        if (recipe.normalFactory()) {
            builder.addInputSlot(INPUT_SLOT_X, SLOT_Y)
                .addItemStacks(recipe.inputs());
            builder.addOutputSlot(OUTPUT_SLOT_X, SLOT_Y)
                .addItemStack(recipe.output());
        }
        if (recipe.advancedFactory()) {
            builder.addInputSlot(INPUT_SLOT_X, ADVANCED_ROW_Y + SLOT_Y)
                .addItemStacks(recipe.inputs());
            builder.addOutputSlot(OUTPUT_SLOT_X, ADVANCED_ROW_Y + SLOT_Y)
                .addItemStack(recipe.output());
        }
    }

    @Override
    public void draw(SugarFactoryJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        if (recipe.normalFactory()) {
            sugarFactoryBackground.draw(guiGraphics, 0, 0);
            sugarFactoryProgress.draw(guiGraphics, PROGRESS_X, PROGRESS_Y);
        }
        if (recipe.advancedFactory()) {
            advancedSugarFactoryBackground.draw(guiGraphics, 0, ADVANCED_ROW_Y);
            advancedSugarFactoryProgress.draw(guiGraphics, PROGRESS_X, ADVANCED_ROW_Y + PROGRESS_Y);
        }
    }

    @Override
    public @Nullable net.minecraft.resources.ResourceLocation getRegistryName(SugarFactoryJeiRecipe recipe) {
        return recipe.id();
    }
}
