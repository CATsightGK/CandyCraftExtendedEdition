package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class CCCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CandyCraft.MODID);

    public static final RegistryObject<CreativeModeTab> CANDYCRAFT = TABS.register("candycraft", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.candycraftmod"))
        .icon(() -> new ItemStack(CCBlocks.PUDDING.get()))
        .displayItems((parameters, output) -> {
            List<RegistryObject<Item>> orderedItems = List.of(
                CCItems.HONEY_SHARD, CCItems.HONEYCOMB, CCItems.NOUGAT_POWDER, CCItems.PEZ, CCItems.PEZ_DUST,
                CCItems.LICORICE, CCItems.CHOCOLATE_COIN, CCItems.CRANBERRY_SCALE, CCItems.SUGAR_CRYSTAL,
                CCItems.WAFFLE_NUGGET, CCItems.MARSHMALLOW_STICK,
                CCItems.LOLLIPOP_SEEDS, CCItems.DRAGIBUS, CCItems.MARSHMALLOW_FLOWER,
                CCItems.CANDIED_CHERRY, CCItems.CANDY_CANE, CCItems.CHEWING_GUM, CCItems.COTTON_CANDY,
                CCItems.CRANBERRY_FISH, CCItems.CRANBERRY_FISH_COOKED, CCItems.DRAGIBUS_STICK, CCItems.GUMMY,
                CCItems.HOT_GUMMY, CCItems.LOLLIPOP, CCItems.SUGAR_PILL, CCItems.WAFFLE,
                CCItems.MARSHMALLOW_SWORD, CCItems.MARSHMALLOW_SHOVEL, CCItems.MARSHMALLOW_PICKAXE, CCItems.MARSHMALLOW_AXE, CCItems.MARSHMALLOW_HOE,
                CCItems.LICORICE_SWORD, CCItems.LICORICE_SHOVEL, CCItems.LICORICE_PICKAXE, CCItems.LICORICE_AXE, CCItems.LICORICE_HOE,
                CCItems.HONEY_SWORD, CCItems.HONEY_SHOVEL, CCItems.HONEY_PICKAXE, CCItems.HONEY_AXE, CCItems.HONEY_HOE,
                CCItems.PEZ_SWORD, CCItems.PEZ_SHOVEL, CCItems.PEZ_PICKAXE, CCItems.PEZ_AXE, CCItems.PEZ_HOE,
                CCItems.FORK, CCItems.LICORICE_SPEAR, CCItems.CARAMEL_BOW, CCItems.CARAMEL_CROSSBOW, CCItems.HONEY_ARROW, CCItems.HONEY_BOLT,
                CCItems.GUMMY_BALL, CCItems.DYNAMITE, CCItems.GLUE_DYNAMITE,
                CCItems.HONEY_HELMET, CCItems.HONEY_PLATE, CCItems.HONEY_LEGGINGS, CCItems.HONEY_BOOTS,
                CCItems.LICORICE_HELMET, CCItems.LICORICE_PLATE, CCItems.LICORICE_LEGGINGS, CCItems.LICORICE_BOOTS,
                CCItems.PEZ_HELMET, CCItems.PEZ_PLATE, CCItems.PEZ_LEGGINGS, CCItems.PEZ_BOOTS, CCItems.JELLY_BOOTS,
                CCItems.BEETLE_KEY, CCItems.JELLY_KEY, CCItems.JELLY_SENTRY_KEY, CCItems.JELLY_BOSS_KEY,
                CCItems.SUGUARD_KEY, CCItems.SUGUARD_SENTRY_KEY, CCItems.SUGUARD_BOSS_KEY, CCItems.SKY_KEY,
                CCItems.CHEWING_GUM_EMBLEM, CCItems.CRANBERRY_EMBLEM, CCItems.GINGERBREAD_EMBLEM, CCItems.HONEY_EMBLEM,
                CCItems.JELLY_EMBLEM, CCItems.SKY_EMBLEM, CCItems.SUGUARD_EMBLEM, CCItems.WATER_EMBLEM,
                CCItems.JELLY_CROWN, CCItems.WATER_MASK, CCItems.JELLY_WAND, CCItems.JUMP_WAND,
                CCItems.RECORD_1, CCItems.RECORD_2, CCItems.RECORD_3, CCItems.RECORD_4,
                CCItems.CARAMEL_BUCKET, CCItems.GRENADINE_BUCKET
            );
            orderedItems.forEach(item -> output.accept(item.get()));
            CCItems.BLOCK_ITEMS.stream()
                .filter(item -> {
                    String path = item.getId().getPath();
                    return !"candy_portal".equals(path)
                        && !"block_teleporter".equals(path)
                        && !"licorice_furnace_on".equals(path)
                        && !"cherry_block".equals(path)
                        && !"sweet_grass".equals(path)
                        && !path.matches("caramel_(glass|pane)_[0-9]+")
                        && !path.contains("double_slab")
                        && !path.contains(".");
                })
                .forEach(item -> output.accept(item.get()));
        })
        .build());

    public static final RegistryObject<CreativeModeTab> SWEETSCAPE = TABS.register("sweetscape", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.candycraftmod.sweetscape"))
        .icon(() -> new ItemStack(CCSweetscapeBlocks.CANDY_GRASS_BLOCK.get()))
        .displayItems((parameters, output) -> {
            CCSweetscapeItems.SIMPLE_ITEMS.forEach(item -> output.accept(item.get()));
            CCSweetscapeItems.TOOL_ITEMS.forEach(item -> output.accept(item.get()));
            CCSweetscapeItems.BLOCK_ITEMS.forEach(item -> output.accept(item.get()));
        })
        .build());

    private CCCreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
        eventBus.addListener(CCCreativeTabs::buildVanillaTabs);
    }

    private static void buildVanillaTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.SPAWN_EGGS) {
            return;
        }
        List<RegistryObject<Item>> spawnEggs = List.of(
            CCItems.CANDY_PIG_SPAWN_EGG, CCItems.WAFFLE_SHEEP_SPAWN_EGG,
            CCItems.CANDY_CREEPER_SPAWN_EGG, CCItems.COTTON_CANDY_SPIDER_SPAWN_EGG,
            CCItems.SUGUARD_SPAWN_EGG, CCItems.MAGE_SUGUARD_SPAWN_EGG, CCItems.CANDY_WOLF_SPAWN_EGG,
            CCItems.GUMMY_BUNNY_SPAWN_EGG, CCItems.CARAMEL_BEE_SPAWN_EGG, CCItems.GINGERBREAD_MAN_SPAWN_EGG,
            CCItems.CANDY_FISH_SPAWN_EGG, CCItems.PINGOUIN_SPAWN_EGG, CCItems.BEETLE_SPAWN_EGG,
            CCItems.NESSIE_SPAWN_EGG, CCItems.DRAGON_SPAWN_EGG, CCItems.KING_BEETLE_SPAWN_EGG,
            CCItems.MERMAID_SPAWN_EGG, CCItems.NOUGAT_GOLEM_SPAWN_EGG,
            CCItems.YELLOW_JELLY_SPAWN_EGG, CCItems.RED_JELLY_SPAWN_EGG, CCItems.TORNADO_JELLY_SPAWN_EGG,
            CCItems.PEZ_JELLY_SPAWN_EGG, CCItems.KING_SLIME_SPAWN_EGG, CCItems.JELLY_QUEEN_SPAWN_EGG,
            CCItems.BOSS_SUGUARD_SPAWN_EGG, CCItems.BOSS_BEETLE_SPAWN_EGG
        );
        spawnEggs.forEach(item -> event.accept(item.get()));
    }
}
