package com.valentin4311.candycraftmod;

import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCBlockEntities;
import com.valentin4311.candycraftmod.registry.CCCriteriaTriggers;
import com.valentin4311.candycraftmod.registry.CCSweetscapeBlocks;
import com.valentin4311.candycraftmod.registry.CCSweetscapeItems;
import com.valentin4311.candycraftmod.registry.CCCreativeTabs;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCFeatures;
import com.valentin4311.candycraftmod.registry.CCFluids;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCMenus;
import com.valentin4311.candycraftmod.registry.CCParticleTypes;
import com.valentin4311.candycraftmod.registry.CCRecipeTypes;
import com.valentin4311.candycraftmod.registry.CCSoundEvents;
import com.valentin4311.candycraftmod.registry.CCWorldgen;
import com.valentin4311.candycraftmod.network.CCTasteNetwork;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.resource.PathPackResources;

import java.nio.file.Path;

@Mod(CandyCraft.MODID)
public class CandyCraft {
    public static final String MODID = "candycraftmod";

    public CandyCraft() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(CandyCraft::addPackFinders);
        CCFluids.register(modBus);
        CCParticleTypes.register(modBus);
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
        CCCriteriaTriggers.register();
        CCTasteNetwork.register();
    }

    private static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }

        event.addRepositorySource(packConsumer -> {
            Path packPath = ModList.get()
                    .getModFileById(MODID)
                    .getFile()
                    .findResource("resourcepacks", "candycraft_classic");
            Pack pack = Pack.readMetaAndCreate(
                    MODID + ":classic_textures",
                    Component.literal("CandyCraft 经典材质"),
                    false,
                    id -> new PathPackResources(id, false, packPath),
                    PackType.CLIENT_RESOURCES,
                    Pack.Position.TOP,
                    PackSource.BUILT_IN
            );
            if (pack != null) {
                packConsumer.accept(pack);
            }
        });
    }
}
