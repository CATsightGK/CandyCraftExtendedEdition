package com.valentin4311.candycraftmod;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCBlockEntities;
import com.valentin4311.candycraftmod.registry.CCSweetscapeBlocks;
import com.valentin4311.candycraftmod.registry.CCSweetscapeItems;
import com.valentin4311.candycraftmod.registry.CCCreativeTabs;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCFeatures;
import com.valentin4311.candycraftmod.registry.CCFluids;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCMenus;
import com.valentin4311.candycraftmod.registry.CCRecipeTypes;
import com.valentin4311.candycraftmod.registry.CCSoundEvents;
import com.valentin4311.candycraftmod.registry.CCWorldgen;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CandyCraft.MODID)
public class CandyCraft {
    public static final String MODID = "candycraftmod";

    public CandyCraft() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        CCFluids.register(modBus);
        CCRecipeTypes.register(modBus);
        CCBlocks.register(modBus);
        CCSweetscapeBlocks.register(modBus);
        CCBlockEntities.register(modBus);
        CCWorldgen.register(modBus);
        CCFeatures.register(modBus);
        CCEntityTypes.register(modBus);
        CCSoundEvents.register(modBus);
        CCItems.register(modBus);
        CCSweetscapeItems.register(modBus);
        CCMenus.register(modBus);
        CCCreativeTabs.register(modBus);
    }
}
