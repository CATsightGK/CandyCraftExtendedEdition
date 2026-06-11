package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CCSweetscapeItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CandyCraft.MODID);
    public static final List<RegistryObject<Item>> BLOCK_ITEMS = new ArrayList<>();
    public static final List<RegistryObject<Item>> SIMPLE_ITEMS = new ArrayList<>();
    public static final List<RegistryObject<Item>> TOOL_ITEMS = new ArrayList<>();

    private static final Tier CHOCOLATE = new ForgeTier(2, 750, 7.0F, 2.5F, 25,
        BlockTags.create(new ResourceLocation(CandyCraft.MODID, "needs_chocolate_tool")),
        () -> Ingredient.of(
            BuiltInRegistries.ITEM.get(new ResourceLocation(CandyCraft.MODID, "milk_chocolate_bar")),
            BuiltInRegistries.ITEM.get(new ResourceLocation(CandyCraft.MODID, "white_chocolate_bar")),
            BuiltInRegistries.ITEM.get(new ResourceLocation(CandyCraft.MODID, "dark_chocolate_bar"))
        ));
    private static final Tier COTTON_CANDY = new ForgeTier(1, 5, 15.0F, 5.0F, 65,
        BlockTags.create(new ResourceLocation(CandyCraft.MODID, "needs_cotton_candy_tool")),
        () -> Ingredient.of(CCItems.COTTON_CANDY.get()));

    public static final RegistryObject<Item> BUTTER = registerFood("butter", 1, 1.0F);
    public static final RegistryObject<Item> CANDY_FLOSS = registerFood("candy_floss", 4, 0.5F);
    public static final RegistryObject<Item> WAFER_STICK = registerFood("wafer_stick", 5, 0.6F);
    public static final RegistryObject<Item> ROCK_CANDY = registerFood("rock_candy", 4, 0.2F);
    public static final RegistryObject<Item> WHITE_CANDY_CANE = registerFood("white_candy_cane", 5, 0.6F);
    public static final RegistryObject<Item> RED_CANDY_CANE = registerFood("red_candy_cane", 5, 0.6F);
    public static final RegistryObject<Item> GREEN_CANDY_CANE = registerFood("green_candy_cane", 5, 0.6F);
    public static final RegistryObject<Item> WHITE_RED_CANDY_CANE = registerFood("white_red_candy_cane", 5, 0.6F);
    public static final RegistryObject<Item> WHITE_GREEN_CANDY_CANE = registerFood("white_green_candy_cane", 5, 0.6F);
    public static final RegistryObject<Item> RED_GREEN_CANDY_CANE = registerFood("red_green_candy_cane", 5, 0.6F);
    public static final RegistryObject<Item> MILK_BROWNIE = registerFood("milk_brownie", 4, 0.5F);
    public static final RegistryObject<Item> WHITE_BROWNIE = registerFood("white_brownie", 4, 0.5F);
    public static final RegistryObject<Item> DARK_BROWNIE = registerFood("dark_brownie", 4, 0.5F);
    public static final RegistryObject<Item> MILK_CHOCOLATE_BAR = registerFood("milk_chocolate_bar", 6, 0.6F);
    public static final RegistryObject<Item> WHITE_CHOCOLATE_BAR = registerFood("white_chocolate_bar", 6, 0.6F);
    public static final RegistryObject<Item> DARK_CHOCOLATE_BAR = registerFood("dark_chocolate_bar", 6, 0.6F);
    public static final RegistryObject<Item> MILK_CHOCOLATE_EGG = registerFood("milk_chocolate_egg", 7, 0.8F);
    public static final RegistryObject<Item> WHITE_CHOCOLATE_EGG = registerFood("white_chocolate_egg", 7, 0.8F);
    public static final RegistryObject<Item> DARK_CHOCOLATE_EGG = registerFood("dark_chocolate_egg", 7, 0.8F);
    public static final RegistryObject<Item> RED_GUMMY = registerFood("red_gummy", 4, 0.6F);
    public static final RegistryObject<Item> ORANGE_GUMMY = registerFood("orange_gummy", 4, 0.6F);
    public static final RegistryObject<Item> YELLOW_GUMMY = registerFood("yellow_gummy", 4, 0.6F);
    public static final RegistryObject<Item> WHITE_GUMMY = registerFood("white_gummy", 4, 0.6F);
    public static final RegistryObject<Item> GREEN_GUMMY = registerFood("green_gummy", 4, 0.6F);
    public static final RegistryObject<Item> RED_GUMMY_WORM = registerFood("red_gummy_worm", 6, 1.0F);
    public static final RegistryObject<Item> ORANGE_GUMMY_WORM = registerFood("orange_gummy_worm", 6, 1.0F);
    public static final RegistryObject<Item> YELLOW_GUMMY_WORM = registerFood("yellow_gummy_worm", 6, 1.0F);
    public static final RegistryObject<Item> WHITE_GUMMY_WORM = registerFood("white_gummy_worm", 6, 1.0F);
    public static final RegistryObject<Item> GREEN_GUMMY_WORM = registerFood("green_gummy_worm", 6, 1.0F);
    public static final RegistryObject<Item> TELEPORTER = registerFood("teleporter", 1, 1.0F, true);

    public static final RegistryObject<Item> MILK_CHOCOLATE_AXE = registerTool("milk_chocolate_axe", () -> new AxeItem(CHOCOLATE, 5.5F, -3.0F, food(6, 0.6F)));
    public static final RegistryObject<Item> MILK_CHOCOLATE_PICKAXE = registerTool("milk_chocolate_pickaxe", () -> new PickaxeItem(CHOCOLATE, 1, -2.8F, food(6, 0.6F)));
    public static final RegistryObject<Item> MILK_CHOCOLATE_SHOVEL = registerTool("milk_chocolate_shovel", () -> new ShovelItem(CHOCOLATE, 1.5F, -3.0F, food(6, 0.6F)));
    public static final RegistryObject<Item> MILK_CHOCOLATE_SWORD = registerTool("milk_chocolate_sword", () -> new SwordItem(CHOCOLATE, 3, -2.4F, food(6, 0.6F)));
    public static final RegistryObject<Item> WHITE_CHOCOLATE_AXE = registerTool("white_chocolate_axe", () -> new AxeItem(CHOCOLATE, 5.5F, -3.0F, food(6, 0.6F)));
    public static final RegistryObject<Item> WHITE_CHOCOLATE_PICKAXE = registerTool("white_chocolate_pickaxe", () -> new PickaxeItem(CHOCOLATE, 1, -2.8F, food(6, 0.6F)));
    public static final RegistryObject<Item> WHITE_CHOCOLATE_SHOVEL = registerTool("white_chocolate_shovel", () -> new ShovelItem(CHOCOLATE, 1.5F, -3.0F, food(6, 0.6F)));
    public static final RegistryObject<Item> WHITE_CHOCOLATE_SWORD = registerTool("white_chocolate_sword", () -> new SwordItem(CHOCOLATE, 3, -2.4F, food(6, 0.6F)));
    public static final RegistryObject<Item> DARK_CHOCOLATE_AXE = registerTool("dark_chocolate_axe", () -> new AxeItem(CHOCOLATE, 5.5F, -3.0F, food(6, 0.6F)));
    public static final RegistryObject<Item> DARK_CHOCOLATE_PICKAXE = registerTool("dark_chocolate_pickaxe", () -> new PickaxeItem(CHOCOLATE, 1, -2.8F, food(6, 0.6F)));
    public static final RegistryObject<Item> DARK_CHOCOLATE_SHOVEL = registerTool("dark_chocolate_shovel", () -> new ShovelItem(CHOCOLATE, 1.5F, -3.0F, food(6, 0.6F)));
    public static final RegistryObject<Item> DARK_CHOCOLATE_SWORD = registerTool("dark_chocolate_sword", () -> new SwordItem(CHOCOLATE, 3, -2.4F, food(6, 0.6F)));
    public static final RegistryObject<Item> COTTON_CANDY_AXE = registerTool("cotton_candy_axe", () -> new AxeItem(COTTON_CANDY, 5.0F, -3.0F, food(1, 0.6F)));
    public static final RegistryObject<Item> COTTON_CANDY_PICKAXE = registerTool("cotton_candy_pickaxe", () -> new PickaxeItem(COTTON_CANDY, 1, -2.8F, food(1, 0.6F)));
    public static final RegistryObject<Item> COTTON_CANDY_SHOVEL = registerTool("cotton_candy_shovel", () -> new ShovelItem(COTTON_CANDY, 1.5F, -3.0F, food(1, 0.6F)));
    public static final RegistryObject<Item> COTTON_CANDY_SWORD = registerTool("cotton_candy_sword", () -> new SwordItem(COTTON_CANDY, 3, -2.4F, food(1, 0.6F)));
    public static final RegistryObject<Item> LIQUID_CHOCOLATE_BUCKET = registerSimple("liquid_chocolate_bucket", () -> new BucketItem(CCFluids.SOURCE_LIQUID_CHOCOLATE, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final RegistryObject<Item> LIQUID_CANDY_BUCKET = registerSimple("liquid_candy_bucket", () -> new BucketItem(CCFluids.SOURCE_LIQUID_CANDY, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

    private CCSweetscapeItems() {
    }

    public static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        RegistryObject<Item> item = ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        BLOCK_ITEMS.add(item);
        return item;
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    private static RegistryObject<Item> registerFood(String name, int nutrition, float saturation) {
        return registerFood(name, nutrition, saturation, false);
    }

    private static RegistryObject<Item> registerFood(String name, int nutrition, float saturation, boolean alwaysEat) {
        RegistryObject<Item> item = ITEMS.register(name, () -> new Item(food(nutrition, saturation, alwaysEat)));
        SIMPLE_ITEMS.add(item);
        return item;
    }

    private static RegistryObject<Item> registerSimple(String name, ItemSupplier supplier) {
        RegistryObject<Item> item = ITEMS.register(name, supplier::get);
        SIMPLE_ITEMS.add(item);
        return item;
    }

    private static RegistryObject<Item> registerTool(String name, ItemSupplier supplier) {
        RegistryObject<Item> item = ITEMS.register(name, supplier::get);
        TOOL_ITEMS.add(item);
        return item;
    }

    private static Item.Properties food(int nutrition, float saturation) {
        return food(nutrition, saturation, false);
    }

    private static Item.Properties food(int nutrition, float saturation, boolean alwaysEat) {
        FoodProperties.Builder builder = new FoodProperties.Builder()
            .nutrition(nutrition)
            .saturationMod(saturation);
        if (alwaysEat) {
            builder.alwaysEat();
        }
        return new Item.Properties().food(builder.build());
    }

    @FunctionalInterface
    private interface ItemSupplier {
        Item get();
    }
}
