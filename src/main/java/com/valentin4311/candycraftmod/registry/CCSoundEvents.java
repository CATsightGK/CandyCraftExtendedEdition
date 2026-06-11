package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CCSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, CandyCraft.MODID);

    public static final RegistryObject<SoundEvent> RECORD_CD_1 = register("records.cd-1");
    public static final RegistryObject<SoundEvent> RECORD_CD_2 = register("records.cd-2");
    public static final RegistryObject<SoundEvent> RECORD_CD_3 = register("records.cd-3");
    public static final RegistryObject<SoundEvent> RECORD_CD_4 = register("records.cd-4");
    public static final RegistryObject<SoundEvent> MOB_NESSIE = register("mob.nessie");
    public static final RegistryObject<SoundEvent> MOB_NESSIE_HURT = register("mob.nessiehurt");
    public static final RegistryObject<SoundEvent> STEP_JELLY = register("step.jelly");
    public static final RegistryObject<SoundEvent> DIG_JELLY = register("dig.jelly");

    private CCSoundEvents() {
    }

    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation id = new ResourceLocation(CandyCraft.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
