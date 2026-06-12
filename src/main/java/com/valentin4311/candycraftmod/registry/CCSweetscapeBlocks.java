package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.CCPlantBlock;
import com.valentin4311.candycraftmod.block.CandyLiquidBlock;
import com.valentin4311.candycraftmod.block.JellyBlock;
import com.valentin4311.candycraftmod.block.SameBlockCullBlock;
import com.valentin4311.candycraftmod.block.SameBlockCullRotatedPillarBlock;
import com.valentin4311.candycraftmod.block.SweetscapeChocolateBarBlock;
import com.valentin4311.candycraftmod.block.WaferStickBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CCSweetscapeBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CandyCraft.MODID);
    public static final List<RegistryObject<? extends Block>> BLOCK_ITEMS = new ArrayList<>();

    public static final RegistryObject<Block> WAFER_STICK_BLOCK = register("wafer_stick_block", () -> new WaferStickBlock(wood(MapColor.TERRACOTTA_ORANGE).strength(0.9F).noOcclusion()));
    public static final RegistryObject<Block> MILK_CHOCOLATE_LEAVES = cutout(register("milk_chocolate_leaves", () -> new Block(leaves(MapColor.COLOR_BROWN))));
    public static final RegistryObject<Block> WHITE_CHOCOLATE_LEAVES = cutout(register("white_chocolate_leaves", () -> new Block(leaves(MapColor.SAND))));
    public static final RegistryObject<Block> DARK_CHOCOLATE_LEAVES = cutout(register("dark_chocolate_leaves", () -> new Block(leaves(MapColor.TERRACOTTA_BROWN))));

    public static final RegistryObject<Block> MILK_CHOCOLATE_BAR_BLOCK = register("milk_chocolate_bar_block", () -> new SweetscapeChocolateBarBlock(cake(MapColor.COLOR_BROWN).strength(0.7F).sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> WHITE_CHOCOLATE_BAR_BLOCK = register("white_chocolate_bar_block", () -> new SweetscapeChocolateBarBlock(cake(MapColor.SAND).strength(0.7F).sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> DARK_CHOCOLATE_BAR_BLOCK = register("dark_chocolate_bar_block", () -> new SweetscapeChocolateBarBlock(cake(MapColor.TERRACOTTA_BROWN).strength(0.7F).sound(SoundType.STONE).noOcclusion()));

    public static final RegistryObject<Block> MILK_CHOCOLATE_MUSHROOM = cutout(register("milk_chocolate_mushroom", () -> new CCPlantBlock(candyPlant(MapColor.COLOR_BROWN).sound(SoundType.WOOD))));
    public static final RegistryObject<Block> WHITE_CHOCOLATE_MUSHROOM = cutout(register("white_chocolate_mushroom", () -> new CCPlantBlock(candyPlant(MapColor.SAND).sound(SoundType.WOOD))));
    public static final RegistryObject<Block> DARK_CHOCOLATE_MUSHROOM = cutout(register("dark_chocolate_mushroom", () -> new CCPlantBlock(candyPlant(MapColor.TERRACOTTA_BROWN).sound(SoundType.WOOD))));

    public static final RegistryObject<Block> MILK_CHOCOLATE_BLOCK = register("milk_chocolate_block", () -> new Block(chocolate(MapColor.COLOR_BROWN)));
    public static final RegistryObject<Block> WHITE_CHOCOLATE_BLOCK = register("white_chocolate_block", () -> new Block(chocolate(MapColor.SAND)));
    public static final RegistryObject<Block> DARK_CHOCOLATE_BLOCK = register("dark_chocolate_block", () -> new Block(chocolate(MapColor.TERRACOTTA_BROWN)));
    public static final RegistryObject<Block> MILK_CHOCOLATE_BRICK = register("milk_chocolate_brick", () -> new Block(chocolate(MapColor.COLOR_BROWN)));
    public static final RegistryObject<Block> WHITE_CHOCOLATE_BRICK = register("white_chocolate_brick", () -> new Block(chocolate(MapColor.SAND)));
    public static final RegistryObject<Block> DARK_CHOCOLATE_BRICK = register("dark_chocolate_brick", () -> new Block(chocolate(MapColor.TERRACOTTA_BROWN)));

    public static final RegistryObject<Block> MILK_CHOCOLATE_WORKBENCH = register("milk_chocolate_workbench", () -> new Block(wood(MapColor.COLOR_BROWN).strength(0.9F)));
    public static final RegistryObject<Block> WHITE_CHOCOLATE_WORKBENCH = register("white_chocolate_workbench", () -> new Block(wood(MapColor.SAND).strength(0.9F)));
    public static final RegistryObject<Block> DARK_CHOCOLATE_WORKBENCH = register("dark_chocolate_workbench", () -> new Block(wood(MapColor.TERRACOTTA_BROWN).strength(0.9F)));

    public static final RegistryObject<Block> COTTON_CANDY_SAPLING = cutout(register("cotton_candy_sapling", () -> new CCPlantBlock(plant())));
    public static final RegistryObject<Block> COTTON_CANDY_BLOCK = cutout(register("cotton_candy_block", () -> new Block(leaves(MapColor.COLOR_PINK).sound(SoundType.WOOL))));
    public static final RegistryObject<Block> COTTON_CANDY_STAIRS = cutout(register("cotton_candy_stairs", () -> stairs(Blocks.WHITE_WOOL.defaultBlockState(), leaves(MapColor.COLOR_PINK).sound(SoundType.WOOL))));
    public static final RegistryObject<Block> COTTON_CANDY_SLAB = cutout(register("cotton_candy_slab", () -> new SlabBlock(leaves(MapColor.COLOR_PINK).sound(SoundType.WOOL))));
    public static final RegistryObject<Block> COTTON_CANDY_DOUBLE_SLAB = cutout(register("cotton_candy_double_slab", () -> new SlabBlock(leaves(MapColor.COLOR_PINK).sound(SoundType.WOOL))));
    public static final RegistryObject<Block> COTTON_CANDY_PLANT = cutout(register("cotton_candy_plant", () -> new CCPlantBlock(candyPlant(MapColor.COLOR_PINK).sound(SoundType.WOOL))));
    public static final RegistryObject<Block> COTTON_CANDY_BUSH = cutout(register("cotton_candy_bush", () -> new CCPlantBlock(candyPlant(MapColor.COLOR_PINK).sound(SoundType.WOOL))));

    public static final RegistryObject<Block> WHITE_CANDY_CANE_BLOCK = register("white_candy_cane_block", () -> new Block(candyCane(MapColor.TERRACOTTA_WHITE)));
    public static final RegistryObject<Block> RED_CANDY_CANE_BLOCK = register("red_candy_cane_block", () -> new Block(candyCane(MapColor.COLOR_RED)));
    public static final RegistryObject<Block> GREEN_CANDY_CANE_BLOCK = register("green_candy_cane_block", () -> new Block(candyCane(MapColor.COLOR_GREEN)));
    public static final RegistryObject<Block> WHITE_RED_CANDY_CANE_BLOCK = register("white_red_candy_cane_block", () -> new Block(candyCane(MapColor.COLOR_RED)));
    public static final RegistryObject<Block> WHITE_GREEN_CANDY_CANE_BLOCK = register("white_green_candy_cane_block", () -> new Block(candyCane(MapColor.COLOR_GREEN)));
    public static final RegistryObject<Block> RED_GREEN_CANDY_CANE_BLOCK = register("red_green_candy_cane_block", () -> new Block(candyCane(MapColor.COLOR_RED)));
    public static final RegistryObject<Block> PINK_CANDY_CANE_BLOCK = register("pink_candy_cane_block", () -> new Block(candyCane(MapColor.COLOR_PINK)));

    public static final RegistryObject<Block> WHITE_CANDY_CANE_WORKBENCH = register("white_candy_cane_workbench", () -> new Block(candyCane(MapColor.TERRACOTTA_WHITE)));
    public static final RegistryObject<Block> RED_CANDY_CANE_WORKBENCH = register("red_candy_cane_workbench", () -> new Block(candyCane(MapColor.COLOR_RED)));
    public static final RegistryObject<Block> GREEN_CANDY_CANE_WORKBENCH = register("green_candy_cane_workbench", () -> new Block(candyCane(MapColor.COLOR_GREEN)));
    public static final RegistryObject<Block> WHITE_RED_CANDY_CANE_WORKBENCH = register("white_red_candy_cane_workbench", () -> new Block(candyCane(MapColor.COLOR_RED)));
    public static final RegistryObject<Block> WHITE_GREEN_CANDY_CANE_WORKBENCH = register("white_green_candy_cane_workbench", () -> new Block(candyCane(MapColor.COLOR_GREEN)));
    public static final RegistryObject<Block> RED_GREEN_CANDY_CANE_WORKBENCH = register("red_green_candy_cane_workbench", () -> new Block(candyCane(MapColor.COLOR_RED)));

    public static final RegistryObject<Block> CRYSTALLIZED_SUGAR = register("crystallized_sugar", () -> new Block(stone(MapColor.TERRACOTTA_WHITE).strength(1.5F)));
    public static final RegistryObject<Block> SUGAR_SAND = register("sugar_sand", () -> new FallingBlock(BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(MapColor.SAND).strength(0.5F)));
    public static final RegistryObject<Block> CANDY_GRASS_BLOCK = register("candy_grass_block", () -> new Block(earth(MapColor.COLOR_PINK).randomTicks()));
    public static final RegistryObject<Block> MILK_BROWNIE_BLOCK = register("milk_brownie_block", () -> new Block(earth(MapColor.DIRT)));
    public static final RegistryObject<Block> CHOCOLATE_COVERED_WHITE_BROWNIE = register("chocolate_covered_white_brownie", () -> new Block(earth(MapColor.SAND).randomTicks()));
    public static final RegistryObject<Block> WHITE_BROWNIE_BLOCK = register("white_brownie_block", () -> new Block(earth(MapColor.SAND)));
    public static final RegistryObject<Block> DARK_CANDY_GRASS_BLOCK = register("dark_candy_grass_block", () -> new Block(earth(MapColor.TERRACOTTA_BROWN).randomTicks()));
    public static final RegistryObject<Block> DARK_BROWNIE_BLOCK = register("dark_brownie_block", () -> new Block(earth(MapColor.TERRACOTTA_BROWN)));

    public static final RegistryObject<Block> CRYSTALLIZED_SUGAR_COOKIE_ORE = register("crystallized_sugar_cookie_ore", () -> new Block(stone(MapColor.TERRACOTTA_WHITE).strength(1.5F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> COOKIE_ORE = register("cookie_ore", () -> new Block(stone(MapColor.STONE).strength(1.5F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> TELEPORTER_ORE = register("teleporter_ore", () -> new Block(stone(MapColor.TERRACOTTA_WHITE).strength(1.5F).requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> RED_GUMMY_BLOCK = translucent(register("red_gummy_block", () -> new JellyBlock(0.0D, gummy(MapColor.COLOR_RED))));
    public static final RegistryObject<Block> ORANGE_GUMMY_BLOCK = translucent(register("orange_gummy_block", () -> new JellyBlock(0.0D, gummy(MapColor.COLOR_ORANGE))));
    public static final RegistryObject<Block> YELLOW_GUMMY_BLOCK = translucent(register("yellow_gummy_block", () -> new JellyBlock(0.0D, gummy(MapColor.COLOR_YELLOW))));
    public static final RegistryObject<Block> WHITE_GUMMY_BLOCK = translucent(register("white_gummy_block", () -> new JellyBlock(0.0D, gummy(MapColor.SAND))));
    public static final RegistryObject<Block> GREEN_GUMMY_BLOCK = translucent(register("green_gummy_block", () -> new JellyBlock(0.0D, gummy(MapColor.COLOR_LIGHT_GREEN))));

    public static final RegistryObject<Block> RED_HARDENED_GUMMY_BLOCK = register("red_hardened_gummy_block", () -> new SameBlockCullBlock(gummy(MapColor.COLOR_RED).noOcclusion()));
    public static final RegistryObject<Block> ORANGE_HARDENED_GUMMY_BLOCK = register("orange_hardened_gummy_block", () -> new SameBlockCullBlock(gummy(MapColor.COLOR_ORANGE).noOcclusion()));
    public static final RegistryObject<Block> YELLOW_HARDENED_GUMMY_BLOCK = register("yellow_hardened_gummy_block", () -> new SameBlockCullBlock(gummy(MapColor.COLOR_YELLOW).noOcclusion()));
    public static final RegistryObject<Block> WHITE_HARDENED_GUMMY_BLOCK = register("white_hardened_gummy_block", () -> new SameBlockCullBlock(gummy(MapColor.SAND).noOcclusion()));
    public static final RegistryObject<Block> GREEN_HARDENED_GUMMY_BLOCK = register("green_hardened_gummy_block", () -> new SameBlockCullBlock(gummy(MapColor.COLOR_LIGHT_GREEN).noOcclusion()));

    public static final RegistryObject<Block> RED_GUMMY_WORM_BLOCK = register("red_gummy_worm_block", () -> new SameBlockCullRotatedPillarBlock(gummy(MapColor.COLOR_RED)));
    public static final RegistryObject<Block> ORANGE_GUMMY_WORM_BLOCK = register("orange_gummy_worm_block", () -> new SameBlockCullRotatedPillarBlock(gummy(MapColor.COLOR_ORANGE)));
    public static final RegistryObject<Block> YELLOW_GUMMY_WORM_BLOCK = register("yellow_gummy_worm_block", () -> new SameBlockCullRotatedPillarBlock(gummy(MapColor.COLOR_YELLOW)));
    public static final RegistryObject<Block> WHITE_GUMMY_WORM_BLOCK = register("white_gummy_worm_block", () -> new SameBlockCullRotatedPillarBlock(gummy(MapColor.SAND)));
    public static final RegistryObject<Block> GREEN_GUMMY_WORM_BLOCK = register("green_gummy_worm_block", () -> new SameBlockCullRotatedPillarBlock(gummy(MapColor.COLOR_LIGHT_GREEN)));

    public static final RegistryObject<Block> RED_GUMMY_WORKBENCH = register("red_gummy_workbench", () -> new SameBlockCullBlock(gummy(MapColor.COLOR_RED)));
    public static final RegistryObject<Block> ORANGE_GUMMY_WORKBENCH = register("orange_gummy_workbench", () -> new SameBlockCullBlock(gummy(MapColor.COLOR_ORANGE)));
    public static final RegistryObject<Block> YELLOW_GUMMY_WORKBENCH = register("yellow_gummy_workbench", () -> new SameBlockCullBlock(gummy(MapColor.COLOR_YELLOW)));
    public static final RegistryObject<Block> WHITE_GUMMY_WORKBENCH = register("white_gummy_workbench", () -> new SameBlockCullBlock(gummy(MapColor.SAND)));
    public static final RegistryObject<Block> GREEN_GUMMY_WORKBENCH = register("green_gummy_workbench", () -> new SameBlockCullBlock(gummy(MapColor.COLOR_LIGHT_GREEN)));
    public static final RegistryObject<LiquidBlock> LIQUID_CHOCOLATE = translucent(registerNoItem("liquid_chocolate", () -> new CandyLiquidBlock(CCFluids.SOURCE_LIQUID_CHOCOLATE, BlockBehaviour.Properties.copy(Blocks.WATER).mapColor(MapColor.COLOR_BROWN).noLootTable(), CandyLiquidBlock.Kind.LIQUID_CHOCOLATE)));
    public static final RegistryObject<LiquidBlock> LIQUID_CANDY = translucent(registerNoItem("liquid_candy", () -> new CandyLiquidBlock(CCFluids.SOURCE_LIQUID_CANDY, BlockBehaviour.Properties.copy(Blocks.WATER).mapColor(MapColor.COLOR_PINK).lightLevel(state -> 12).noLootTable(), CandyLiquidBlock.Kind.LIQUID_CANDY)));

    private CCSweetscapeBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> supplier) {
        RegistryObject<T> block = BLOCKS.register(name, supplier);
        CCSweetscapeItems.registerBlockItem(name, block);
        BLOCK_ITEMS.add(block);
        return block;
    }

    private static <T extends Block> RegistryObject<T> registerNoItem(String name, Supplier<T> supplier) {
        return BLOCKS.register(name, supplier);
    }

    private static <T extends Block> RegistryObject<T> cutout(RegistryObject<T> block) {
        CCBlocks.CUTOUT_BLOCKS.add(block);
        return block;
    }

    private static <T extends Block> RegistryObject<T> translucent(RegistryObject<T> block) {
        CCBlocks.TRANSLUCENT_BLOCKS.add(block);
        return block;
    }

    private static BlockBehaviour.Properties plant() {
        return BlockBehaviour.Properties.copy(Blocks.GRASS).noCollission().noOcclusion();
    }

    private static BlockBehaviour.Properties candyPlant(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.DEAD_BUSH).mapColor(color).noCollission().noOcclusion();
    }

    private static BlockBehaviour.Properties leaves(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES).mapColor(color).randomTicks().noOcclusion();
    }

    private static BlockBehaviour.Properties cake(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.CAKE).mapColor(color);
    }

    private static BlockBehaviour.Properties chocolate(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(color).strength(0.7F).sound(SoundType.STONE);
    }

    private static BlockBehaviour.Properties wood(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).mapColor(color).sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties candyCane(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(color).strength(1.2F).sound(SoundType.STONE);
    }

    private static BlockBehaviour.Properties earth(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.DIRT).mapColor(color).strength(0.6F);
    }

    private static BlockBehaviour.Properties stone(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(color);
    }

    private static BlockBehaviour.Properties gummy(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK).mapColor(color).strength(0.4F).friction(0.6F).noOcclusion();
    }

    private static StairBlock stairs(BlockState baseState, BlockBehaviour.Properties properties) {
        return new StairBlock(baseState, properties);
    }
}
