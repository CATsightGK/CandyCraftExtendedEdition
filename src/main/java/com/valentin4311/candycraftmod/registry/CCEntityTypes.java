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
import com.valentin4311.candycraftmod.entity.CottonCandySheepEntity;
import com.valentin4311.candycraftmod.entity.CottonCandySpiderEntity;
import com.valentin4311.candycraftmod.entity.DynamiteEntity;
import com.valentin4311.candycraftmod.entity.EasterChickenEntity;
import com.valentin4311.candycraftmod.entity.GlueDynamiteEntity;
import com.valentin4311.candycraftmod.entity.GummyBallEntity;
import com.valentin4311.candycraftmod.entity.GummyBearEntity;
import com.valentin4311.candycraftmod.entity.GummyBunnyEntity;
import com.valentin4311.candycraftmod.entity.GummyMouseEntity;
import com.valentin4311.candycraftmod.entity.GingerbreadManEntity;
import com.valentin4311.candycraftmod.entity.HoneyArrowEntity;
import com.valentin4311.candycraftmod.entity.HoneyBoltEntity;
import com.valentin4311.candycraftmod.entity.NougatGolemEntity;
import com.valentin4311.candycraftmod.entity.PingouinEntity;
import com.valentin4311.candycraftmod.entity.ThrownForkEntity;
import com.valentin4311.candycraftmod.entity.ThrownForkBlockEntity;
import com.valentin4311.candycraftmod.entity.WaffleSheepEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CCEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, CandyCraft.MODID);

    public static final RegistryObject<EntityType<HoneyArrowEntity>> HONEY_ARROW = ENTITY_TYPES.register("honey_arrow", () ->
        EntityType.Builder.<HoneyArrowEntity>of(HoneyArrowEntity::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build(CandyCraft.MODID + ":honey_arrow")
    );

    public static final RegistryObject<EntityType<HoneyBoltEntity>> HONEY_BOLT = ENTITY_TYPES.register("honey_bolt", () ->
        EntityType.Builder.<HoneyBoltEntity>of(HoneyBoltEntity::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build(CandyCraft.MODID + ":honey_bolt")
    );

    public static final RegistryObject<EntityType<DynamiteEntity>> DYNAMITE = ENTITY_TYPES.register("dynamite", () ->
        EntityType.Builder.<DynamiteEntity>of(DynamiteEntity::new, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build(CandyCraft.MODID + ":dynamite")
    );

    public static final RegistryObject<EntityType<GlueDynamiteEntity>> GLUE_DYNAMITE = ENTITY_TYPES.register("glue_dynamite", () ->
        EntityType.Builder.<GlueDynamiteEntity>of(GlueDynamiteEntity::new, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(10)
            .build(CandyCraft.MODID + ":glue_dynamite")
    );

    public static final RegistryObject<EntityType<GummyBallEntity>> GUMMY_BALL = ENTITY_TYPES.register("gummy_ball", () ->
        EntityType.Builder.<GummyBallEntity>of(GummyBallEntity::new, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .clientTrackingRange(8)
            .updateInterval(10)
            .build(CandyCraft.MODID + ":gummy_ball")
    );

    public static final RegistryObject<EntityType<ThrownForkEntity>> THROWN_FORK = ENTITY_TYPES.register("thrown_fork", () ->
        EntityType.Builder.<ThrownForkEntity>of(ThrownForkEntity::new, MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(4)
            .updateInterval(20)
            .build(CandyCraft.MODID + ":thrown_fork")
    );

    public static final RegistryObject<EntityType<ThrownForkBlockEntity>> THROWN_FORK_BLOCK = ENTITY_TYPES.register("thrown_fork_block", () ->
        EntityType.Builder.<ThrownForkBlockEntity>of(ThrownForkBlockEntity::new, MobCategory.MISC)
            .sized(0.65F, 0.65F)
            .clientTrackingRange(8)
            .updateInterval(2)
            .build(CandyCraft.MODID + ":thrown_fork_block")
    );

    public static final RegistryObject<EntityType<CandyPigEntity>> CANDY_PIG = ENTITY_TYPES.register("candy_pig", () ->
        EntityType.Builder.of(CandyPigEntity::new, MobCategory.CREATURE)
            .sized(0.9F, 0.9F)
            .clientTrackingRange(10)
            .build(CandyCraft.MODID + ":candy_pig")
    );

    public static final RegistryObject<EntityType<WaffleSheepEntity>> WAFFLE_SHEEP = ENTITY_TYPES.register("waffle_sheep", () ->
        EntityType.Builder.of(WaffleSheepEntity::new, MobCategory.CREATURE)
            .sized(0.9F, 1.3F)
            .clientTrackingRange(10)
            .build(CandyCraft.MODID + ":waffle_sheep")
    );

    public static final RegistryObject<EntityType<CandyCreeperEntity>> CANDY_CREEPER = ENTITY_TYPES.register("candy_creeper", () ->
        EntityType.Builder.of(CandyCreeperEntity::new, MobCategory.MONSTER)
            .sized(0.6F, 1.7F)
            .clientTrackingRange(8)
            .build(CandyCraft.MODID + ":candy_creeper")
    );

    public static final RegistryObject<EntityType<CottonCandySpiderEntity>> COTTON_CANDY_SPIDER = ENTITY_TYPES.register("cotton_candy_spider", () ->
        EntityType.Builder.of(CottonCandySpiderEntity::new, MobCategory.MONSTER)
            .sized(1.4F, 0.9F)
            .clientTrackingRange(8)
            .build(CandyCraft.MODID + ":cotton_candy_spider")
    );

    public static final RegistryObject<EntityType<CandyWolfEntity>> CANDY_WOLF = ENTITY_TYPES.register("candy_wolf", () ->
        EntityType.Builder.of(CandyWolfEntity::new, MobCategory.CREATURE)
            .sized(0.6F, 0.85F)
            .clientTrackingRange(10)
            .build(CandyCraft.MODID + ":candy_wolf")
    );

    public static final RegistryObject<EntityType<GummyBunnyEntity>> GUMMY_BUNNY = ENTITY_TYPES.register("gummy_bunny", () ->
        EntityType.Builder.of(GummyBunnyEntity::new, MobCategory.CREATURE)
            .sized(0.5F, 0.5F)
            .clientTrackingRange(10)
            .build(CandyCraft.MODID + ":gummy_bunny")
    );
    public static final RegistryObject<EntityType<CottonCandySheepEntity>> COTTON_CANDY_SHEEP = ENTITY_TYPES.register("cotton_candy_sheep", () ->
        EntityType.Builder.of(CottonCandySheepEntity::new, MobCategory.CREATURE)
            .sized(0.9F, 1.3F)
            .clientTrackingRange(10)
            .build(CandyCraft.MODID + ":cotton_candy_sheep")
    );
    public static final RegistryObject<EntityType<EasterChickenEntity>> EASTER_CHICKEN = ENTITY_TYPES.register("easter_chicken", () ->
        EntityType.Builder.of(EasterChickenEntity::new, MobCategory.CREATURE)
            .sized(0.4F, 0.7F)
            .clientTrackingRange(10)
            .build(CandyCraft.MODID + ":easter_chicken")
    );
    public static final RegistryObject<EntityType<GummyMouseEntity>> GUMMY_MOUSE = ENTITY_TYPES.register("gummy_mouse", () ->
        EntityType.Builder.of(GummyMouseEntity::new, MobCategory.CREATURE)
            .sized(0.35F, 0.2F)
            .clientTrackingRange(10)
            .build(CandyCraft.MODID + ":gummy_mouse")
    );
    public static final RegistryObject<EntityType<GummyBearEntity>> GUMMY_BEAR = ENTITY_TYPES.register("gummy_bear", () ->
        EntityType.Builder.of(GummyBearEntity::new, MobCategory.CREATURE)
            .sized(1.4F, 1.4F)
            .clientTrackingRange(10)
            .build(CandyCraft.MODID + ":gummy_bear")
    );

    public static final RegistryObject<EntityType<BasicCandyZombieEntity>> SUGUARD = basicZombie("suguard", 0.5F, 0.9F);
    public static final RegistryObject<EntityType<BasicCandyZombieEntity>> MAGE_SUGUARD = basicZombie("mage_suguard", 0.5F, 0.9F);
    public static final RegistryObject<EntityType<CaramelBeeEntity>> CARAMEL_BEE = ENTITY_TYPES.register("caramel_bee", () ->
        EntityType.Builder.of(CaramelBeeEntity::new, MobCategory.MONSTER)
            .sized(0.8F, 1.0F)
            .clientTrackingRange(8)
            .build(CandyCraft.MODID + ":caramel_bee")
    );
    public static final RegistryObject<EntityType<GingerbreadManEntity>> GINGERBREAD_MAN = ENTITY_TYPES.register("gingerbread_man", () ->
        EntityType.Builder.of(GingerbreadManEntity::new, MobCategory.CREATURE)
            .sized(0.3F, 0.9F)
            .clientTrackingRange(10)
            .build(CandyCraft.MODID + ":gingerbread_man")
    );
    public static final RegistryObject<EntityType<CandyFishEntity>> CANDY_FISH = ENTITY_TYPES.register("candy_fish", () ->
        EntityType.Builder.of(CandyFishEntity::new, MobCategory.WATER_CREATURE)
            .sized(0.95F, 0.95F)
            .clientTrackingRange(8)
            .build(CandyCraft.MODID + ":candy_fish")
    );
    public static final RegistryObject<EntityType<PingouinEntity>> PINGOUIN = ENTITY_TYPES.register("pingouin", () ->
        EntityType.Builder.of(PingouinEntity::new, MobCategory.CREATURE)
            .sized(0.6F, 1.0F)
            .clientTrackingRange(10)
            .build(CandyCraft.MODID + ":pingouin")
    );
    public static final RegistryObject<EntityType<BasicCandySpiderEntity>> BEETLE = basicSpider("beetle", 1.0F, 0.8F);
    public static final RegistryObject<EntityType<BasicCandyZombieEntity>> NESSIE = basicZombie("nessie", MobCategory.WATER_CREATURE, 1.2F, 1.6F);
    public static final RegistryObject<EntityType<BasicCandyZombieEntity>> DRAGON = basicZombie("dragon", MobCategory.CREATURE, 3.0F, 2.2F);
    public static final RegistryObject<EntityType<BasicCandyZombieEntity>> KING_BEETLE = basicZombie("king_beetle", MobCategory.CREATURE, 3.0F, 2.0F);
    public static final RegistryObject<EntityType<BasicCandyZombieEntity>> MERMAID = basicZombie("mermaid", 0.95F, 1.0F);
    public static final RegistryObject<EntityType<NougatGolemEntity>> NOUGAT_GOLEM = ENTITY_TYPES.register("nougat_golem", () ->
        EntityType.Builder.of(NougatGolemEntity::new, MobCategory.CREATURE)
            .sized(1.0F, 1.0F)
            .clientTrackingRange(10)
            .build(CandyCraft.MODID + ":nougat_golem")
    );
    public static final RegistryObject<EntityType<BasicCandySlimeEntity>> YELLOW_JELLY = basicSlime("yellow_jelly", 0.8F, 0.8F);
    public static final RegistryObject<EntityType<BasicCandySlimeEntity>> RED_JELLY = basicSlime("red_jelly", 0.8F, 0.8F);
    public static final RegistryObject<EntityType<BasicCandySlimeEntity>> TORNADO_JELLY = basicSlime("tornado_jelly", 0.8F, 0.8F);
    public static final RegistryObject<EntityType<BasicCandySlimeEntity>> PEZ_JELLY = basicSlime("pez_jelly", 1.2F, 1.2F);
    public static final RegistryObject<EntityType<BasicCandySlimeEntity>> KING_SLIME = basicSlime("king_slime", 2.4F, 2.4F);
    public static final RegistryObject<EntityType<BasicCandySlimeEntity>> JELLY_QUEEN = basicSlime("jelly_queen", 2.2F, 2.2F);
    public static final RegistryObject<EntityType<BasicCandyZombieEntity>> BOSS_SUGUARD = basicZombie("boss_suguard", 0.8F, 1.5F);
    public static final RegistryObject<EntityType<BasicCandySpiderEntity>> BOSS_BEETLE = basicSpider("boss_beetle", 2.0F, 1.6F);

    private CCEntityTypes() {
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }

    private static RegistryObject<EntityType<BasicCandyZombieEntity>> basicZombie(String name, float width, float height) {
        return basicZombie(name, MobCategory.MONSTER, width, height);
    }

    private static RegistryObject<EntityType<BasicCandyZombieEntity>> basicZombie(String name, MobCategory category, float width, float height) {
        return registerBasic(name, BasicCandyZombieEntity::new, category, width, height);
    }

    private static RegistryObject<EntityType<BasicCandySpiderEntity>> basicSpider(String name, float width, float height) {
        return registerBasic(name, BasicCandySpiderEntity::new, MobCategory.MONSTER, width, height);
    }

    private static RegistryObject<EntityType<BasicCandySlimeEntity>> basicSlime(String name, float width, float height) {
        return registerBasic(name, BasicCandySlimeEntity::new, MobCategory.MONSTER, width, height);
    }

    private static <T extends Entity> RegistryObject<EntityType<T>> registerBasic(String name, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height) {
        return ENTITY_TYPES.register(name, () -> EntityType.Builder.of(factory, category)
            .sized(width, height)
            .clientTrackingRange(8)
            .build(CandyCraft.MODID + ":" + name));
    }
}
