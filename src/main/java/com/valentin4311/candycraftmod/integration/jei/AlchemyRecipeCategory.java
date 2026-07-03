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
    private static final int WIDTH = 340;
    private static final Component DESCRIPTION = Component.translatable("jei.candycraftmod.alchemy_table.description");
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
        return WIDTH;
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
        builder.addInputSlot(4, 30)
            .setStandardSlotBackground()
            .addItemStack(new ItemStack(CCItems.GRENADINE_BUCKET.get()));
        builder.addInputSlot(32, 18)
            .setStandardSlotBackground()
            .addItemStack(recipe.inputs().get(0));
        builder.addInputSlot(54, 18)
            .setStandardSlotBackground()
            .addItemStack(recipe.inputs().get(1));
        builder.addInputSlot(32, 42)
            .setStandardSlotBackground()
            .addItemStack(recipe.inputs().get(2));
        builder.addInputSlot(54, 42)
            .setStandardSlotBackground()
            .addItemStack(recipe.inputs().get(3));
        builder.addInputSlot(82, 30)
            .setStandardSlotBackground()
            .addItemStack(new ItemStack(Items.SUGAR));
        builder.addOutputSlot(140, 30)
            .setOutputSlotBackground()
            .addItemStack(recipe.output());
    }

    @Override
    public void draw(AlchemyJeiRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        drawFittedText(guiGraphics, DESCRIPTION, 3, 2, WIDTH - 6, 0x7A3C56);
        arrow.draw(guiGraphics, 112, 30);
    }

    private static void drawFittedText(GuiGraphics guiGraphics, Component text, int x, int y, int maxWidth, int color) {
        var font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        float scale = textWidth <= maxWidth ? 1.0F : Math.max(0.55F, maxWidth / (float)textWidth);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.drawString(font, text, Math.round(x / scale), Math.round(y / scale), color, false);
        guiGraphics.pose().popPose();
    }

    @Override
    public @Nullable net.minecraft.resources.ResourceLocation getRegistryName(AlchemyJeiRecipe recipe) {
        return recipe.id();
    }
}
