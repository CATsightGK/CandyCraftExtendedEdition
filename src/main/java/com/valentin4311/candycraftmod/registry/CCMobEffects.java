package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.effect.CloyingMobEffect;
import com.valentin4311.candycraftmod.effect.PropolisMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CCMobEffects {
    private static final DeferredRegister<MobEffect> MOB_EFFECTS =
        DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, CandyCraft.MODID);

    public static final RegistryObject<MobEffect> HONEY_GLUE =
        MOB_EFFECTS.register("honey_glue", PropolisMobEffect::new);
    public static final RegistryObject<MobEffect> CLOYING =
        MOB_EFFECTS.register("cloying", CloyingMobEffect::new);

    private CCMobEffects() {
    }

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
