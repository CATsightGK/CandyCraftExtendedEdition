package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.recipe.LicoriceSmeltingRecipe;
import com.valentin4311.candycraftmod.recipe.LicoriceSmeltingSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class CCRecipeTypes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, CandyCraft.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE, CandyCraft.MODID);

    public static final RegistryObject<RecipeType<LicoriceSmeltingRecipe>> LICORICE_SMELTING_TYPE =
        RECIPE_TYPES.register("licorice_smelting", () -> new RecipeType<>() {
            @Override
            public String toString() {
                return CandyCraft.MODID + ":licorice_smelting";
            }
        });

    public static final RegistryObject<RecipeSerializer<LicoriceSmeltingRecipe>> LICORICE_SMELTING_SERIALIZER =
        RECIPE_SERIALIZERS.register("licorice_smelting", LicoriceSmeltingSerializer::new);

    private CCRecipeTypes() {
    }

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
