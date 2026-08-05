package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.recipe.LicoriceSmeltingRecipe;
import com.valentin4311.candycraftmod.recipe.LicoriceSmeltingSerializer;
import com.valentin4311.candycraftmod.recipe.LicoriceFuelRecipe;
import com.valentin4311.candycraftmod.recipe.LicoriceFuelRecipeSerializer;
import com.valentin4311.candycraftmod.recipe.SugarFactoryRecipe;
import com.valentin4311.candycraftmod.recipe.SugarFactoryRecipeSerializer;
import com.valentin4311.candycraftmod.recipe.AlchemyMixingRecipe;
import com.valentin4311.candycraftmod.recipe.AlchemyMixingSerializer;
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

    public static final RegistryObject<RecipeType<AlchemyMixingRecipe>> ALCHEMY_MIXING_TYPE =
        RECIPE_TYPES.register("alchemy_mixing", () -> new RecipeType<>() {
            @Override
            public String toString() {
                return CandyCraft.MODID + ":alchemy_mixing";
            }
        });

    public static final RegistryObject<RecipeType<LicoriceFuelRecipe>> LICORICE_FUEL_TYPE =
        RECIPE_TYPES.register("licorice_fuel", () -> new RecipeType<>() {
            @Override
            public String toString() {
                return CandyCraft.MODID + ":licorice_fuel";
            }
        });

    public static final RegistryObject<RecipeType<SugarFactoryRecipe>> SUGAR_FACTORY_TYPE =
        RECIPE_TYPES.register("sugar_factory", () -> new RecipeType<>() {
            @Override
            public String toString() {
                return CandyCraft.MODID + ":sugar_factory";
            }
        });

    public static final RegistryObject<RecipeSerializer<LicoriceSmeltingRecipe>> LICORICE_SMELTING_SERIALIZER =
        RECIPE_SERIALIZERS.register("licorice_smelting", LicoriceSmeltingSerializer::new);

    public static final RegistryObject<RecipeSerializer<AlchemyMixingRecipe>> ALCHEMY_MIXING_SERIALIZER =
        RECIPE_SERIALIZERS.register("alchemy_mixing", AlchemyMixingSerializer::new);

    public static final RegistryObject<RecipeSerializer<LicoriceFuelRecipe>> LICORICE_FUEL_SERIALIZER =
        RECIPE_SERIALIZERS.register("licorice_fuel", LicoriceFuelRecipeSerializer::new);

    public static final RegistryObject<RecipeSerializer<SugarFactoryRecipe>> SUGAR_FACTORY_SERIALIZER =
        RECIPE_SERIALIZERS.register("sugar_factory", SugarFactoryRecipeSerializer::new);

    private CCRecipeTypes() {
    }

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
