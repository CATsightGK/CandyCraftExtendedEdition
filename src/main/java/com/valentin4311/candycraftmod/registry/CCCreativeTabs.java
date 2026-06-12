package com.valentin4311.candycraftmod.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.valentin4311.candycraftmod.CandyCraft;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class CCCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CandyCraft.MODID);
    private static final String CREATIVE_TAB_ORDER_PATH = "data/" + CandyCraft.MODID + "/creative_tabs/order.json";
    private static JsonObject creativeTabOrder;
    private static boolean creativeTabOrderLoaded;

    public static final RegistryObject<CreativeModeTab> BLOCKS = TABS.register("blocks", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.candycraftmod.blocks"))
        .icon(() -> new ItemStack(CCBlocks.PUDDING.get()))
        .displayItems((parameters, output) -> {
            if (acceptOrderedTab("blocks", output)) {
                return;
            }
            CCItems.BLOCK_ITEMS.forEach(item -> output.accept(item.get()));
            CCSweetscapeItems.BLOCK_ITEMS.forEach(item -> output.accept(item.get()));
        })
        .build());

    public static final RegistryObject<CreativeModeTab> FOOD = TABS.register("food", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.candycraftmod.food"))
        .icon(() -> new ItemStack(CCItems.CANDIED_CHERRY.get()))
        .displayItems((parameters, output) -> {
            if (acceptOrderedTab("food", output)) {
                return;
            }
            CCItems.PORT_ITEMS.forEach(item -> output.accept(item.get()));
            CCSweetscapeItems.SIMPLE_ITEMS.forEach(item -> output.accept(item.get()));
        })
        .build());

    public static final RegistryObject<CreativeModeTab> TOOLS_ARMOR = TABS.register("tools_armor", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.candycraftmod.tools_armor"))
        .icon(() -> new ItemStack(CCItems.HONEY_SWORD.get()))
        .displayItems((parameters, output) -> {
            if (acceptOrderedTab("tools_armor", output)) {
                return;
            }
            CCSweetscapeItems.TOOL_ITEMS.forEach(item -> output.accept(item.get()));
        })
        .build());

    public static final RegistryObject<CreativeModeTab> MISC = TABS.register("misc", () -> CreativeModeTab.builder()
        .title(Component.translatable("itemGroup.candycraftmod.misc"))
        .icon(() -> new ItemStack(CCItems.CHOCOLATE_COIN.get()))
        .displayItems((parameters, output) -> {
            if (acceptOrderedTab("misc", output)) {
                return;
            }
            CCItems.PORT_ITEMS.forEach(item -> output.accept(item.get()));
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

    private static boolean acceptOrderedTab(String key, CreativeModeTab.Output output) {
        JsonObject order = creativeTabOrder();
        if (order == null || !order.has(key) || !order.get(key).isJsonArray()) {
            return false;
        }
        JsonArray entries = order.getAsJsonArray(key);
        for (JsonElement entry : entries) {
            if (!entry.isJsonPrimitive()) {
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(entry.getAsString());
            if (id == null) {
                continue;
            }
            BuiltInRegistries.ITEM.getOptional(id).ifPresent(output::accept);
        }
        return true;
    }

    private static JsonObject creativeTabOrder() {
        if (creativeTabOrderLoaded) {
            return creativeTabOrder;
        }
        creativeTabOrderLoaded = true;
        try (InputStream stream = CCCreativeTabs.class.getClassLoader().getResourceAsStream(CREATIVE_TAB_ORDER_PATH)) {
            if (stream == null) {
                return null;
            }
            JsonElement parsed = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            if (parsed != null && parsed.isJsonObject()) {
                creativeTabOrder = parsed.getAsJsonObject();
            }
        } catch (RuntimeException | java.io.IOException ignored) {
            creativeTabOrder = null;
        }
        return creativeTabOrder;
    }

}
