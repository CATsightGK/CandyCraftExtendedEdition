package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CCPotions {
    private static final DeferredRegister<Potion> POTIONS =
        DeferredRegister.create(ForgeRegistries.POTIONS, CandyCraft.MODID);

    public static final RegistryObject<Potion> CLOYING = POTIONS.register("cloying",
        () -> new Potion("cloying", new MobEffectInstance(CCMobEffects.CLOYING.get(), 3 * 60 * 20)));
    public static final RegistryObject<Potion> LONG_CLOYING = POTIONS.register("long_cloying",
        () -> new Potion("cloying", new MobEffectInstance(CCMobEffects.CLOYING.get(), 8 * 60 * 20)));
    public static final RegistryObject<Potion> STRONG_CLOYING = POTIONS.register("strong_cloying",
        () -> new Potion("cloying", new MobEffectInstance(CCMobEffects.CLOYING.get(), 90 * 20, 1)));

    private CCPotions() {
    }

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}
