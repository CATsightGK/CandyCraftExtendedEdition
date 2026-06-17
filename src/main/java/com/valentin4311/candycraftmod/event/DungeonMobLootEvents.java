package com.valentin4311.candycraftmod.event;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DungeonMobLootEvents {
    private static final String NO_SPAWNER_LOOT_TAG = CandyCraft.MODID + ".no_spawner_loot";

    private DungeonMobLootEvents() {
    }

    @SubscribeEvent
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getSpawnType() == MobSpawnType.SPAWNER && isSmallJelly(event.getEntity().getType())) {
            event.getEntity().getPersistentData().putBoolean(NO_SPAWNER_LOOT_TAG, true);
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().getPersistentData().getBoolean(NO_SPAWNER_LOOT_TAG) && isSmallJelly(event.getEntity().getType())) {
            event.getDrops().clear();
        }
    }

    @SubscribeEvent
    public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        if (event.getEntity().getPersistentData().getBoolean(NO_SPAWNER_LOOT_TAG) && isSmallJelly(event.getEntity().getType())) {
            event.setDroppedExperience(0);
        }
    }

    private static boolean isSmallJelly(EntityType<?> type) {
        return type == CCEntityTypes.YELLOW_JELLY.get()
            || type == CCEntityTypes.RED_JELLY.get()
            || type == CCEntityTypes.TORNADO_JELLY.get();
    }
}
