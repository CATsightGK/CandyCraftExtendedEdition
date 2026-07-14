package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.eventbus.api.IEventBus;

public final class CCParticleTypes {
    private static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
        DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, CandyCraft.MODID);

    public static final RegistryObject<SimpleParticleType> CHOCOLATE_SPLASH =
        PARTICLE_TYPES.register("chocolate_splash", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> MILK_RAIN_DROP =
        PARTICLE_TYPES.register("milk_rain_drop", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> MILK_RAIN_SPLASH =
        PARTICLE_TYPES.register("milk_rain_splash", () -> new SimpleParticleType(false));

    private CCParticleTypes() {
    }

    public static void register(IEventBus bus) {
        PARTICLE_TYPES.register(bus);
    }
}
