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
    public static final RegistryObject<SimpleParticleType> ALCHEMY_SPLASH =
        PARTICLE_TYPES.register("alchemy_splash", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> STRAWBERRY_JELLY_FRAGMENT =
        PARTICLE_TYPES.register("strawberry_jelly_fragment", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> CARAMEL_JELLY_FRAGMENT =
        PARTICLE_TYPES.register("caramel_jelly_fragment", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> ROYAL_RATIONS_FRAGMENT =
        PARTICLE_TYPES.register("royal_rations_fragment", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> LEMON_JELLY_FRAGMENT =
        PARTICLE_TYPES.register("lemon_jelly_fragment", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> RASPBERRY_JELLY_FRAGMENT =
        PARTICLE_TYPES.register("raspberry_jelly_fragment", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> MINT_JELLY_FRAGMENT =
        PARTICLE_TYPES.register("mint_jelly_fragment", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> LIQUID_CANDY_FLAME =
        PARTICLE_TYPES.register("liquid_candy_flame", () -> new SimpleParticleType(false));

    private CCParticleTypes() {
    }

    public static void register(IEventBus bus) {
        PARTICLE_TYPES.register(bus);
    }
}
