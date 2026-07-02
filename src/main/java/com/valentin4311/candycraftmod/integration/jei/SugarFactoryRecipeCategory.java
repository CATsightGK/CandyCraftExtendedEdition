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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class SugarFactoryRecipeCategory implements IRecipeCategory<SugarFactoryJeiRecipe> {
    private static final ResourceLocation SUGAR_FACTORY_GUI = new ResourceLocation(CandyCraft.MODID, "textures/gui/gui_sugar.png");
    private static final ResourceLocation ADVANCED_SUGAR_FACTORY_GUI = new ResourceLocation(CandyCraft.MODID, "textures/gui/gui_advancedsugar.png");
    private final IDrawable icon;
    private final IDrawableStatic sugarFactoryBackground;
    private final IDrawableStatic advancedSugarFactoryBackground;
    private final IDrawableAnimated arrow;

    public SugarFactoryRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(CCBlocks.SUGAR_FACTORY.get());
        this.sugarFactoryBackground = guiHelper.createDrawable(SUGAR_FACTORY_GUI, 0, 0, 174, 58);
        this.advancedSugarFactoryBackground = guiHelper.createDrawable(ADVANCED_SUGAR_FACTORY_GUI, 0, 0, 174, 58);
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
        return 174;
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
        builder.addInputSlot(8, 33)
            .addItemStacks(recipe.inputs());
        builder.addOutputSlot(152, 33)
            .addItemStack(recipe.output());
    }

    @Override
    public void draw(SugarFactoryJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IDrawableStatic background = recipe.advancedFactory() ? advancedSugarFactoryBackground : sugarFactoryBackground;
        background.draw(guiGraphics, 0, 0);
        arrow.draw(guiGraphics, 27, 9);
        Component tier = recipe.advancedFactory()
            ? Component.translatable("container.candycraftmod.advanced_sugar_factory")
            : Component.translatable("container.candycraftmod.sugar_factory");
        guiGraphics.drawString(Minecraft.getInstance().font, tier, 8, 4, recipe.advancedFactory() ? 0xC96622 : 0x6F4A30, false);
    }

    @Override
    public @Nullable net.minecraft.resources.ResourceLocation getRegistryName(SugarFactoryJeiRecipe recipe) {
        return recipe.id();
    }
}
