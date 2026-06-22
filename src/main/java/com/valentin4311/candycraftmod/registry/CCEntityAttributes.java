package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.entity.BasicCandySlimeEntity;
import com.valentin4311.candycraftmod.entity.BasicCandySpiderEntity;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import com.valentin4311.candycraftmod.entity.CaramelBeeEntity;
import com.valentin4311.candycraftmod.entity.CandyCreeperEntity;
import com.valentin4311.candycraftmod.entity.CandyFishEntity;
import com.valentin4311.candycraftmod.entity.CandyPigEntity;
import com.valentin4311.candycraftmod.entity.CandyWolfEntity;
import com.valentin4311.candycraftmod.entity.CottonCandySpiderEntity;
import com.valentin4311.candycraftmod.entity.GummyBunnyEntity;
import com.valentin4311.candycraftmod.entity.NougatGolemEntity;
import com.valentin4311.candycraftmod.entity.PingouinEntity;
import com.valentin4311.candycraftmod.entity.WaffleSheepEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CCEntityAttributes {
    private CCEntityAttributes() {
    }

    @SubscribeEvent
    public static void register(EntityAttributeCreationEvent event) {
        event.put(CCEntityTypes.CANDY_PIG.get(), Pig.createAttributes().build());
        event.put(CCEntityTypes.WAFFLE_SHEEP.get(), Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 10.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.23000000417232513D)
            .build());
        event.put(CCEntityTypes.CANDY_WOLF.get(), Wolf.createAttributes()
            .add(Attributes.MAX_HEALTH, 10.0D)
            .add(Attributes.ATTACK_DAMAGE, 2.0D)
            .build());
        event.put(CCEntityTypes.GUMMY_BUNNY.get(), Rabbit.createAttributes()
            .add(Attributes.MAX_HEALTH, 10.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.20000000298023224D)
            .build());
        event.put(CCEntityTypes.CANDY_FISH.get(), CandyFishEntity.createAttributes().build());
        event.put(CCEntityTypes.PINGOUIN.get(), PingouinEntity.createAttributes().build());
        event.put(CCEntityTypes.CANDY_CREEPER.get(), Creeper.createAttributes()
            .add(Attributes.MAX_HEALTH, 5.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            .build());
        event.put(CCEntityTypes.COTTON_CANDY_SPIDER.get(), Spider.createAttributes()
            .add(Attributes.MAX_HEALTH, 24.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            .build());
        event.put(CCEntityTypes.GINGERBREAD_MAN.get(), Villager.createAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.33D)
            .build());

        AttributeSupplier suguard = BasicCandyZombieEntity.createAttributes().build();
        event.put(CCEntityTypes.SUGUARD.get(), suguard);

        AttributeSupplier mageSuguard = BasicCandyZombieEntity.createAttributes()
            .add(Attributes.ATTACK_DAMAGE, 4.0D)
            .build();
        event.put(CCEntityTypes.MAGE_SUGUARD.get(), mageSuguard);

        AttributeSupplier zombie = Zombie.createAttributes()
            .add(Attributes.MAX_HEALTH, 20.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D)
            .add(Attributes.ATTACK_DAMAGE, 3.0D)
            .build();
        putZombie(event, zombie);
        event.put(CCEntityTypes.DRAGON.get(), Zombie.createAttributes()
            .add(Attributes.MAX_HEALTH, 80.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.4D)
            .add(Attributes.ATTACK_DAMAGE, 6.0D)
            .build());
        event.put(CCEntityTypes.KING_BEETLE.get(), Zombie.createAttributes()
            .add(Attributes.MAX_HEALTH, 80.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            .add(Attributes.ATTACK_DAMAGE, 8.0D)
            .build());

        AttributeSupplier spider = Spider.createAttributes()
            .add(Attributes.MAX_HEALTH, 16.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            .add(Attributes.ATTACK_DAMAGE, 3.0D)
            .build();
        putSpider(event, spider);

        AttributeSupplier slime = Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 16.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.35D)
            .add(Attributes.FOLLOW_RANGE, 16.0D)
            .add(Attributes.ATTACK_DAMAGE, 4.0D)
            .build();
        putSlime(event, slime);

        event.put(CCEntityTypes.CARAMEL_BEE.get(), CaramelBeeEntity.createAttributes().build());
        event.put(CCEntityTypes.BEETLE.get(), Spider.createAttributes()
            .add(Attributes.MAX_HEALTH, 25.0D)
            .add(Attributes.MOVEMENT_SPEED, 1.2D)
            .add(Attributes.ATTACK_DAMAGE, 3.0D)
            .build());
        event.put(CCEntityTypes.BOSS_BEETLE.get(), Spider.createAttributes()
            .add(Attributes.MAX_HEALTH, 300.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.35D)
            .add(Attributes.ATTACK_DAMAGE, 10.0D)
            .build());
        event.put(CCEntityTypes.BOSS_SUGUARD.get(), Zombie.createAttributes()
            .add(Attributes.MAX_HEALTH, 400.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.35D)
            .add(Attributes.ATTACK_DAMAGE, 12.0D)
            .build());
        event.put(CCEntityTypes.JELLY_QUEEN.get(), Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 300.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.7D)
            .add(Attributes.FOLLOW_RANGE, 32.0D)
            .add(Attributes.ATTACK_DAMAGE, 8.0D)
            .build());
        event.put(CCEntityTypes.KING_SLIME.get(), Mob.createMobAttributes()
            .add(Attributes.MAX_HEALTH, 800.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.35D)
            .add(Attributes.FOLLOW_RANGE, 32.0D)
            .add(Attributes.ATTACK_DAMAGE, 12.0D)
            .build());
        event.put(CCEntityTypes.NOUGAT_GOLEM.get(), NougatGolemEntity.createAttributes().build());
    }

    private static void putZombie(EntityAttributeCreationEvent event, AttributeSupplier attributes) {
        event.put(CCEntityTypes.NESSIE.get(), attributes);
        event.put(CCEntityTypes.MERMAID.get(), attributes);
    }

    private static void putSpider(EntityAttributeCreationEvent event, AttributeSupplier attributes) {
    }

    private static void putSlime(EntityAttributeCreationEvent event, AttributeSupplier attributes) {
        event.put(CCEntityTypes.YELLOW_JELLY.get(), attributes);
        event.put(CCEntityTypes.RED_JELLY.get(), attributes);
        event.put(CCEntityTypes.TORNADO_JELLY.get(), attributes);
        event.put(CCEntityTypes.PEZ_JELLY.get(), attributes);
    }
}
