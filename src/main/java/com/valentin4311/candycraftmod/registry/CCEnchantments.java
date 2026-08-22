package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.enchantment.ForkEnchantment;
import com.valentin4311.candycraftmod.enchantment.HoneySourceEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CCEnchantments {
    private static final DeferredRegister<Enchantment> ENCHANTMENTS =
        DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, CandyCraft.MODID);

    public static final RegistryObject<Enchantment> HONEY_SOURCE =
        ENCHANTMENTS.register("honey_source", HoneySourceEnchantment::new);
    public static final RegistryObject<Enchantment> GLUTTONY =
        ENCHANTMENTS.register("gluttony", () -> new ForkEnchantment(Enchantment.Rarity.VERY_RARE, 1, 25, true));
    public static final RegistryObject<Enchantment> DEVOURING =
        ENCHANTMENTS.register("devouring", () -> new ForkEnchantment(Enchantment.Rarity.RARE, 3, 10, false));

    private CCEnchantments() {
    }

    public static void register(IEventBus eventBus) {
        ENCHANTMENTS.register(eventBus);
    }
}
