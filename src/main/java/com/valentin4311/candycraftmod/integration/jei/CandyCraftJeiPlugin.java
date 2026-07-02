package com.valentin4311.candycraftmod.integration.jei;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class CandyCraftJeiPlugin implements IModPlugin {
    public static final RecipeType<SugarFactoryJeiRecipe> SUGAR_FACTORY =
        RecipeType.create(CandyCraft.MODID, "sugar_factory", SugarFactoryJeiRecipe.class);
    public static final RecipeType<AlchemyJeiRecipe> ALCHEMY_TABLE =
        RecipeType.create(CandyCraft.MODID, "alchemy_table", AlchemyJeiRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(CandyCraft.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
            new SugarFactoryRecipeCategory(guiHelper),
            new AlchemyRecipeCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(SUGAR_FACTORY, SugarFactoryJeiRecipe.createRecipes());
        registration.addRecipes(ALCHEMY_TABLE, AlchemyJeiRecipe.createRecipes());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(CCBlocks.SUGAR_FACTORY.get(), SUGAR_FACTORY);
        registration.addRecipeCatalyst(CCBlocks.ADVANCED_SUGAR_FACTORY.get(), SUGAR_FACTORY);
        registration.addRecipeCatalyst(CCBlocks.ALCHEMY_TABLE.get(), ALCHEMY_TABLE);
    }
}
