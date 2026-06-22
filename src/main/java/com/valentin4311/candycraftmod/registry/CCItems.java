package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.item.CaramelBowItem;
import com.valentin4311.candycraftmod.item.CaramelCrossbowItem;
import com.valentin4311.candycraftmod.item.CandiedCherryItem;
import com.valentin4311.candycraftmod.item.CCArmorItem;
import com.valentin4311.candycraftmod.item.DynamiteItem;
import com.valentin4311.candycraftmod.item.EmblemItem;
import com.valentin4311.candycraftmod.item.GummyBallItem;
import com.valentin4311.candycraftmod.item.HoneyArrowItem;
import com.valentin4311.candycraftmod.item.HoneyBoltItem;
import com.valentin4311.candycraftmod.item.JellyDungeonKeyItem;
import com.valentin4311.candycraftmod.item.JellyWandItem;
import com.valentin4311.candycraftmod.item.RawGummyItem;
import com.valentin4311.candycraftmod.item.SugarPillItem;
import com.valentin4311.candycraftmod.CandyCraft;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CCItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CandyCraft.MODID);
    public static final List<RegistryObject<Item>> BLOCK_ITEMS = new ArrayList<>();
    public static final List<RegistryObject<Item>> PORT_ITEMS = new ArrayList<>();

    public static final RegistryObject<Item> HONEY_SHARD = registerPortItem("honey_shard");
    public static final RegistryObject<Item> NOUGAT_POWDER = registerPortItem("nougat_powder");
    public static final RegistryObject<Item> PEZ = registerPortItem("pez");
    public static final RegistryObject<Item> PEZ_DUST = registerPortItem("pez_dust");
    public static final RegistryObject<Item> LICORICE = registerPortItem("licorice");
    public static final RegistryObject<Item> HONEYCOMB = registerPortItem("honeycomb");
    public static final RegistryObject<Item> CHOCOLATE_COIN = registerPortItem("chocolate_coin");
    public static final RegistryObject<Item> CRANBERRY_SCALE = registerPortItem("cranberry_scale");
    public static final RegistryObject<Item> SUGAR_CRYSTAL = registerPortItem("pure_rock_candy", () -> new Item(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<Item> WAFFLE_NUGGET = registerPortItem("waffle_nugget");
    public static final RegistryObject<Item> MARSHMALLOW_STICK = registerPortItem("marshmallow_stick");
    public static final RegistryObject<Item> LOLLIPOP = registerPortItem("lollipop");
    public static final RegistryObject<Item> LOLLIPOP_SEEDS = registerSeedItem("lollipop_seeds", () -> CCBlocks.LOLLIPOP_PLANT.get());
    public static final RegistryObject<Item> DRAGIBUS = registerSeedItem("dragibus", () -> CCBlocks.DRAGIBUS_CROPS.get());
    public static final RegistryObject<Item> MARSHMALLOW_FLOWER = registerPortItem("marshmallow_flower");
    public static final RegistryObject<Item> CANDIED_CHERRY = registerPortItem("candied_cherry", () -> new CandiedCherryItem(foodProperties(4, 0.3F)));
    public static final RegistryObject<Item> CANDY_CANE = registerFood("candy_cane", 2, 0.2F);
    public static final RegistryObject<Item> CHEWING_GUM = registerFood("chewing_gum", 1, 0.1F);
    public static final RegistryObject<Item> COTTON_CANDY = registerFood("raspberry_cotton_candy", 3, 0.2F);
    public static final RegistryObject<Item> CRANBERRY_FISH = registerFood("cranberry_fish", 2, 0.1F);
    public static final RegistryObject<Item> CRANBERRY_FISH_COOKED = registerFood("cranberry_fish_cooked", 5, 0.6F);
    public static final RegistryObject<Item> DRAGIBUS_STICK = registerFood("dragibus_stick", 5, 0.4F);
    public static final RegistryObject<Item> GUMMY = registerPortItem("gummy", () -> new RawGummyItem(foodProperties(3, 0.25F)));
    public static final RegistryObject<Item> HOT_GUMMY = registerFood("hot_gummy", 6, 0.6F);
    public static final RegistryObject<Item> ALCHEMY_MIXER_BLADE = ITEMS.register("alchemy_mixer_blade", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SUGAR_PILL = registerPortItem("sugar_pill", () -> new SugarPillItem(new Item.Properties().food(new FoodProperties.Builder()
        .nutrition(0)
        .saturationMod(0.0F)
        .alwaysEat()
        .build())));
    public static final RegistryObject<Item> WAFFLE = registerFood("waffle", 5, 0.5F);
    public static final RegistryObject<Item> HONEY_ARROW = registerPortItem("honey_arrow", () -> new HoneyArrowItem(new Item.Properties()));
    public static final RegistryObject<Item> HONEY_BOLT = registerPortItem("honey_bolt", () -> new HoneyBoltItem(new Item.Properties()));
    public static final RegistryObject<Item> CARAMEL_BOW = registerPortItem("caramel_bow", () -> new CaramelBowItem(new Item.Properties().durability(384)));
    public static final RegistryObject<Item> CARAMEL_CROSSBOW = registerPortItem("caramel_crossbow", () -> new CaramelCrossbowItem(new Item.Properties().durability(465)));
    public static final RegistryObject<Item> FORK = registerPortItem("fork", () -> new HoeItem(CCItemTiers.MARSHMALLOW, -1, -1.0F, new Item.Properties().durability(64)));
    public static final RegistryObject<Item> LICORICE_SPEAR = registerPortItem("licorice_spear", () -> new SwordItem(CCItemTiers.LICORICE, 2, -2.2F, new Item.Properties()));
    public static final RegistryObject<Item> GUMMY_BALL = registerPortItem("gummy_ball", () -> new GummyBallItem(new Item.Properties().stacksTo(16)));
    public static final RegistryObject<Item> DYNAMITE = registerPortItem("dynamite", () -> new DynamiteItem(new Item.Properties(), false));
    public static final RegistryObject<Item> GLUE_DYNAMITE = registerPortItem("glue_dynamite", () -> new DynamiteItem(new Item.Properties(), true));
    public static final RegistryObject<Item> JELLY_WAND = registerPortItem("jelly_wand", () -> new JellyWandItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> JUMP_WAND = registerPortItem("jump_wand", () -> new Item(new Item.Properties().stacksTo(1).durability(128)));
    public static final RegistryObject<Item> WIKI = registerPortItem("wiki", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> BEETLE_KEY = registerPortItem("beetle_key");
    public static final RegistryObject<Item> JELLY_KEY = registerPortItem("jelly_key", () -> new JellyDungeonKeyItem(new Item.Properties()));
    public static final RegistryObject<Item> JELLY_SENTRY_KEY = registerPortItem("jelly_sentry_key");
    public static final RegistryObject<Item> JELLY_BOSS_KEY = registerPortItem("jelly_boss_key");
    public static final RegistryObject<Item> SUGUARD_SENTRY_KEY = registerPortItem("suguard_sentry_key");
    public static final RegistryObject<Item> SUGUARD_BOSS_KEY = registerPortItem("suguard_boss_key");
    public static final RegistryObject<Item> SUGUARD_KEY = registerPortItem("suguard_key", () -> new JellyDungeonKeyItem(new Item.Properties(), true));
    public static final RegistryObject<Item> SKY_KEY = registerPortItem("sky_key");
    public static final RegistryObject<Item> CHEWING_GUM_EMBLEM = registerEmblem("chewing_gum_emblem", "tooltip.candycraftmod.chewing_gum_emblem");
    public static final RegistryObject<Item> CRANBERRY_EMBLEM = registerEmblem("cranberry_emblem", "tooltip.candycraftmod.cranberry_emblem");
    public static final RegistryObject<Item> GINGERBREAD_EMBLEM = registerEmblem("gingerbread_emblem", "tooltip.candycraftmod.gingerbread_emblem");
    public static final RegistryObject<Item> HONEY_EMBLEM = registerEmblem("honey_emblem", "tooltip.candycraftmod.honey_emblem");
    public static final RegistryObject<Item> JELLY_EMBLEM = registerEmblem("jelly_emblem", "tooltip.candycraftmod.jelly_emblem");
    public static final RegistryObject<Item> SKY_EMBLEM = registerEmblem("sky_emblem", "tooltip.candycraftmod.sky_emblem");
    public static final RegistryObject<Item> SUGUARD_EMBLEM = registerEmblem("suguard_emblem", "tooltip.candycraftmod.suguard_emblem");
    public static final RegistryObject<Item> WATER_EMBLEM = registerEmblem("water_emblem", "tooltip.candycraftmod.water_emblem");
    public static final RegistryObject<Item> JELLY_CROWN = registerPortItem("jelly_crown", () -> new CCArmorItem(CCArmorMaterials.JELLY, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> WATER_MASK = registerPortItem("water_mask", () -> new CCArmorItem(CCArmorMaterials.MASK, ArmorItem.Type.HELMET, new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> RECORD_1 = registerRecord("record_1", CCSoundEvents.RECORD_CD_1, 2400);
    public static final RegistryObject<Item> RECORD_2 = registerRecord("record_2", CCSoundEvents.RECORD_CD_2, 2400);
    public static final RegistryObject<Item> RECORD_3 = registerRecord("record_3", CCSoundEvents.RECORD_CD_3, 2400);
    public static final RegistryObject<Item> RECORD_4 = registerRecord("record_4", CCSoundEvents.RECORD_CD_4, 2400);
    public static final RegistryObject<Item> CARAMEL_BUCKET = registerPortItem("caramel_bucket", () -> new BucketItem(CCFluids.SOURCE_CARAMEL, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final RegistryObject<Item> GRENADINE_BUCKET = registerPortItem("grenadine_bucket", () -> new BucketItem(CCFluids.SOURCE_GRENADINE, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    private static final ToolSet MARSHMALLOW_TOOLS = registerToolSet("marshmallow", CCItemTiers.MARSHMALLOW);
    private static final ToolSet LICORICE_TOOLS = registerToolSet("licorice", CCItemTiers.LICORICE);
    private static final ToolSet HONEY_TOOLS = registerToolSet("honey", CCItemTiers.HONEY);
    private static final ToolSet PEZ_TOOLS = registerToolSet("pez", CCItemTiers.PEZ);
    public static final RegistryObject<Item> MARSHMALLOW_SWORD = MARSHMALLOW_TOOLS.sword;
    public static final RegistryObject<Item> MARSHMALLOW_SHOVEL = MARSHMALLOW_TOOLS.shovel;
    public static final RegistryObject<Item> MARSHMALLOW_PICKAXE = MARSHMALLOW_TOOLS.pickaxe;
    public static final RegistryObject<Item> MARSHMALLOW_AXE = MARSHMALLOW_TOOLS.axe;
    public static final RegistryObject<Item> MARSHMALLOW_HOE = MARSHMALLOW_TOOLS.hoe;
    public static final RegistryObject<Item> LICORICE_SWORD = LICORICE_TOOLS.sword;
    public static final RegistryObject<Item> LICORICE_SHOVEL = LICORICE_TOOLS.shovel;
    public static final RegistryObject<Item> LICORICE_PICKAXE = LICORICE_TOOLS.pickaxe;
    public static final RegistryObject<Item> LICORICE_AXE = LICORICE_TOOLS.axe;
    public static final RegistryObject<Item> LICORICE_HOE = LICORICE_TOOLS.hoe;
    public static final RegistryObject<Item> HONEY_SWORD = HONEY_TOOLS.sword;
    public static final RegistryObject<Item> HONEY_SHOVEL = HONEY_TOOLS.shovel;
    public static final RegistryObject<Item> HONEY_PICKAXE = HONEY_TOOLS.pickaxe;
    public static final RegistryObject<Item> HONEY_AXE = HONEY_TOOLS.axe;
    public static final RegistryObject<Item> HONEY_HOE = HONEY_TOOLS.hoe;
    public static final RegistryObject<Item> PEZ_SWORD = PEZ_TOOLS.sword;
    public static final RegistryObject<Item> PEZ_SHOVEL = PEZ_TOOLS.shovel;
    public static final RegistryObject<Item> PEZ_PICKAXE = PEZ_TOOLS.pickaxe;
    public static final RegistryObject<Item> PEZ_AXE = PEZ_TOOLS.axe;
    public static final RegistryObject<Item> PEZ_HOE = PEZ_TOOLS.hoe;
    public static final RegistryObject<Item> HONEY_HELMET = registerArmor("honey_helmet", CCArmorMaterials.HONEY, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> HONEY_PLATE = registerArmor("honey_plate", CCArmorMaterials.HONEY, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> HONEY_LEGGINGS = registerArmor("honey_leggings", CCArmorMaterials.HONEY, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> HONEY_BOOTS = registerArmor("honey_boots", CCArmorMaterials.HONEY, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> LICORICE_HELMET = registerArmor("licorice_helmet", CCArmorMaterials.LICORICE, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> LICORICE_PLATE = registerArmor("licorice_plate", CCArmorMaterials.LICORICE, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> LICORICE_LEGGINGS = registerArmor("licorice_leggings", CCArmorMaterials.LICORICE, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> LICORICE_BOOTS = registerArmor("licorice_boots", CCArmorMaterials.LICORICE, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> PEZ_HELMET = registerArmor("pez_helmet", CCArmorMaterials.PEZ, ArmorItem.Type.HELMET);
    public static final RegistryObject<Item> PEZ_PLATE = registerArmor("pez_plate", CCArmorMaterials.PEZ, ArmorItem.Type.CHESTPLATE);
    public static final RegistryObject<Item> PEZ_LEGGINGS = registerArmor("pez_leggings", CCArmorMaterials.PEZ, ArmorItem.Type.LEGGINGS);
    public static final RegistryObject<Item> PEZ_BOOTS = registerArmor("pez_boots", CCArmorMaterials.PEZ, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> JELLY_BOOTS = registerArmor("jelly_boots", CCArmorMaterials.JELLY, ArmorItem.Type.BOOTS);
    public static final RegistryObject<Item> CANDY_PIG_SPAWN_EGG = registerSpawnEgg("candy_pig_spawn_egg", CCEntityTypes.CANDY_PIG, 0xD88C9A, 0xF7F0E1);
    public static final RegistryObject<Item> WAFFLE_SHEEP_SPAWN_EGG = registerSpawnEgg("waffle_sheep_spawn_egg", CCEntityTypes.WAFFLE_SHEEP, 0xE4B76E, 0xFFF0C2);
    public static final RegistryObject<Item> CANDY_CREEPER_SPAWN_EGG = registerSpawnEgg("candy_creeper_spawn_egg", CCEntityTypes.CANDY_CREEPER, 0x8A5A2B, 0xF0D58A);
    public static final RegistryObject<Item> COTTON_CANDY_SPIDER_SPAWN_EGG = registerSpawnEgg("cotton_candy_spider_spawn_egg", CCEntityTypes.COTTON_CANDY_SPIDER, 0xF2A0C8, 0xFFFFFF);
    public static final RegistryObject<Item> SUGUARD_SPAWN_EGG = registerSpawnEgg("suguard_spawn_egg", CCEntityTypes.SUGUARD, 0xF3F3F3, 0xD14646);
    public static final RegistryObject<Item> MAGE_SUGUARD_SPAWN_EGG = registerSpawnEgg("mage_suguard_spawn_egg", CCEntityTypes.MAGE_SUGUARD, 0xF3F3F3, 0x7A42C8);
    public static final RegistryObject<Item> CANDY_WOLF_SPAWN_EGG = registerSpawnEgg("candy_wolf_spawn_egg", CCEntityTypes.CANDY_WOLF, 0x4B2E22, 0xDDB98F);
    public static final RegistryObject<Item> GUMMY_BUNNY_SPAWN_EGG = registerSpawnEgg("gummy_bunny_spawn_egg", CCEntityTypes.GUMMY_BUNNY, 0xEBA3C8, 0xFFFFFF);
    public static final RegistryObject<Item> CARAMEL_BEE_SPAWN_EGG = registerSpawnEgg("caramel_bee_spawn_egg", CCEntityTypes.CARAMEL_BEE, 0xB26B20, 0xFFD65A);
    public static final RegistryObject<Item> GINGERBREAD_MAN_SPAWN_EGG = registerSpawnEgg("gingerbread_man_spawn_egg", CCEntityTypes.GINGERBREAD_MAN, 0xB87535, 0xFFFFFF);
    public static final RegistryObject<Item> CANDY_FISH_SPAWN_EGG = registerSpawnEgg("candy_fish_spawn_egg", CCEntityTypes.CANDY_FISH, 0xB61E34, 0xF7A3B1);
    public static final RegistryObject<Item> PINGOUIN_SPAWN_EGG = registerSpawnEgg("pingouin_spawn_egg", CCEntityTypes.PINGOUIN, 0x2F3552, 0xB9E7FF);
    public static final RegistryObject<Item> BEETLE_SPAWN_EGG = registerSpawnEgg("beetle_spawn_egg", CCEntityTypes.BEETLE, 0xF08AC1, 0x7A2E5F);
    public static final RegistryObject<Item> NESSIE_SPAWN_EGG = registerSpawnEgg("nessie_spawn_egg", CCEntityTypes.NESSIE, 0x3C8AA0, 0x92D7E8);
    public static final RegistryObject<Item> DRAGON_SPAWN_EGG = registerSpawnEgg("dragon_spawn_egg", CCEntityTypes.DRAGON, 0x5572C6, 0xBBD6FF);
    public static final RegistryObject<Item> KING_BEETLE_SPAWN_EGG = registerSpawnEgg("king_beetle_spawn_egg", CCEntityTypes.KING_BEETLE, 0x33201A, 0xE6C17A);
    public static final RegistryObject<Item> MERMAID_SPAWN_EGG = registerSpawnEgg("mermaid_spawn_egg", CCEntityTypes.MERMAID, 0x4AA7B5, 0xF5C0D5);
    public static final RegistryObject<Item> NOUGAT_GOLEM_SPAWN_EGG = registerSpawnEgg("nougat_golem_spawn_egg", CCEntityTypes.NOUGAT_GOLEM, 0xD8C18C, 0x805B38);
    public static final RegistryObject<Item> YELLOW_JELLY_SPAWN_EGG = registerSpawnEgg("yellow_jelly_spawn_egg", CCEntityTypes.YELLOW_JELLY, 0xFFF15A, 0xFFFFFF);
    public static final RegistryObject<Item> RED_JELLY_SPAWN_EGG = registerSpawnEgg("red_jelly_spawn_egg", CCEntityTypes.RED_JELLY, 0xE94242, 0xFFFFFF);
    public static final RegistryObject<Item> TORNADO_JELLY_SPAWN_EGG = registerSpawnEgg("tornado_jelly_spawn_egg", CCEntityTypes.TORNADO_JELLY, 0x78E0B5, 0xFFFFFF);
    public static final RegistryObject<Item> PEZ_JELLY_SPAWN_EGG = registerSpawnEgg("pez_jelly_spawn_egg", CCEntityTypes.PEZ_JELLY, 0x7EC8F0, 0xFFFFFF);
    public static final RegistryObject<Item> KING_SLIME_SPAWN_EGG = registerSpawnEgg("king_slime_spawn_egg", CCEntityTypes.KING_SLIME, 0xA76BEA, 0xFFD94A);
    public static final RegistryObject<Item> JELLY_QUEEN_SPAWN_EGG = registerSpawnEgg("jelly_queen_spawn_egg", CCEntityTypes.JELLY_QUEEN, 0xD85AFF, 0xFFF15A);
    public static final RegistryObject<Item> BOSS_SUGUARD_SPAWN_EGG = registerSpawnEgg("boss_suguard_spawn_egg", CCEntityTypes.BOSS_SUGUARD, 0xE8E8E8, 0xA12424);
    public static final RegistryObject<Item> BOSS_BEETLE_SPAWN_EGG = registerSpawnEgg("boss_beetle_spawn_egg", CCEntityTypes.BOSS_BEETLE, 0x171313, 0x8B1F2D);

    private CCItems() {
    }

    public static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        RegistryObject<Item> item = ITEMS.register(name, () -> createBlockItem(name, block.get()));
        BLOCK_ITEMS.add(item);
        return item;
    }

    private static Item createBlockItem(String name, Block block) {
        Item.Properties properties = new Item.Properties();
        return switch (name) {
            case "marshmallow_door" -> new DoubleHighBlockItem(block, properties);
            case "cotton_candy_bed_block" -> new BedItem(block, properties.stacksTo(1));
            case "honey_torch" -> new StandingAndWallBlockItem(block, CCBlocks.HONEY_WALL_TORCH.get(), properties, Direction.DOWN);
            default -> new BlockItem(block, properties);
        };
    }

    private static RegistryObject<Item> registerPortItem(String name) {
        RegistryObject<Item> item = ITEMS.register(name, () -> new Item(new Item.Properties()));
        PORT_ITEMS.add(item);
        return item;
    }

    private static RegistryObject<Item> registerPortItem(String name, SupplierItem itemSupplier) {
        RegistryObject<Item> item = ITEMS.register(name, itemSupplier::get);
        PORT_ITEMS.add(item);
        return item;
    }

    private static RegistryObject<Item> registerFood(String name, int nutrition, float saturation) {
        return registerPortItem(name, () -> new Item(foodProperties(nutrition, saturation)));
    }

    private static RegistryObject<Item> registerEmblem(String name, String descriptionKey) {
        return registerPortItem(name, () -> new EmblemItem(descriptionKey, new Item.Properties()));
    }

    private static Item.Properties foodProperties(int nutrition, float saturation) {
        return new Item.Properties().food(new FoodProperties.Builder()
            .nutrition(nutrition)
            .saturationMod(saturation)
            .build());
    }

    private static RegistryObject<Item> registerRecord(String name, RegistryObject<net.minecraft.sounds.SoundEvent> sound, int lengthInTicks) {
        return registerPortItem(name, () -> new RecordItem(1, sound, new Item.Properties().stacksTo(1).rarity(Rarity.RARE), lengthInTicks));
    }

    private static <T extends net.minecraft.world.entity.Mob> RegistryObject<Item> registerSpawnEgg(String name, RegistryObject<net.minecraft.world.entity.EntityType<T>> entityType, int backgroundColor, int highlightColor) {
        return registerPortItem(name, () -> new ForgeSpawnEggItem(entityType, backgroundColor, highlightColor, new Item.Properties()));
    }

    private static RegistryObject<Item> registerSeedItem(String name, SupplierBlock block) {
        RegistryObject<Item> item = ITEMS.register(name, () -> new ItemNameBlockItem(block.get(), new Item.Properties()));
        PORT_ITEMS.add(item);
        return item;
    }

    private static ToolSet registerToolSet(String prefix, Tier tier) {
        return new ToolSet(
            registerPortItem(prefix + "_sword", () -> new SwordItem(tier, 3, -2.4F, new Item.Properties())),
            registerPortItem(prefix + "_shovel", () -> new ShovelItem(tier, 1.5F, -3.0F, new Item.Properties())),
            registerPortItem(prefix + "_pickaxe", () -> new PickaxeItem(tier, 1, -2.8F, new Item.Properties())),
            registerPortItem(prefix + "_axe", () -> new AxeItem(tier, 5.0F, -3.1F, new Item.Properties())),
            registerPortItem(prefix + "_hoe", () -> new HoeItem(tier, -2, -1.0F, new Item.Properties()))
        );
    }

    private static RegistryObject<Item> registerArmor(String name, CCArmorMaterials material, ArmorItem.Type type) {
        return registerPortItem(name, () -> new CCArmorItem(material, type, new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    @FunctionalInterface
    private interface SupplierBlock {
        Block get();
    }

    @FunctionalInterface
    private interface SupplierItem {
        Item get();
    }

    private record ToolSet(
        RegistryObject<Item> sword,
        RegistryObject<Item> shovel,
        RegistryObject<Item> pickaxe,
        RegistryObject<Item> axe,
        RegistryObject<Item> hoe
    ) {
    }
}
