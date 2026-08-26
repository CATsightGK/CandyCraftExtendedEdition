package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.CCPlantBlock;
import com.valentin4311.candycraftmod.block.SameBlockCullBlock;
import com.valentin4311.candycraftmod.block.SameBlockCullRotatedPillarBlock;
import com.valentin4311.candycraftmod.block.SweetscapeChocolateBarBlock;
import com.valentin4311.candycraftmod.block.StaticWaterBlock;
import com.valentin4311.candycraftmod.block.StrawberryJellyBlock;
import com.valentin4311.candycraftmod.block.WaferStickBlock;
import com.valentin4311.candycraftmod.block.WaferChocolateSaplingBlock;
import com.valentin4311.candycraftmod.block.CandyCropBlock;
import com.valentin4311.candycraftmod.block.CandyBedBlock;
import com.valentin4311.candycraftmod.block.CandyStandingSignBlock;
import com.valentin4311.candycraftmod.block.CandyWallSignBlock;
import com.valentin4311.candycraftmod.block.CandyFarmlandBlock;
import com.valentin4311.candycraftmod.block.CandyLiquidBlock;
import com.valentin4311.candycraftmod.block.CandyPortalBlock;
import com.valentin4311.candycraftmod.block.CandyWaterlilyBlock;
import com.valentin4311.candycraftmod.block.CandyWebBlock;
import com.valentin4311.candycraftmod.block.CandyWorkbenchBlock;
import com.valentin4311.candycraftmod.block.CottonCandyJukeboxBlock;
import com.valentin4311.candycraftmod.block.CherryBlock;
import com.valentin4311.candycraftmod.block.CherryLeavesBlock;
import com.valentin4311.candycraftmod.block.ChewingGumBlock;
import com.valentin4311.candycraftmod.block.ChewingGumPuddleBlock;
import com.valentin4311.candycraftmod.block.DragonEggBlock;
import com.valentin4311.candycraftmod.block.AcidMintFlowerBlock;
import com.valentin4311.candycraftmod.block.AlchemyTableBlock;
import com.valentin4311.candycraftmod.block.DungeonTeleporterBlock;
import com.valentin4311.candycraftmod.block.DungeonLockBlock;
import com.valentin4311.candycraftmod.block.FacingModelBlock;
import com.valentin4311.candycraftmod.block.FragileGrenadineBlock;
import com.valentin4311.candycraftmod.block.JellyBlock;
import com.valentin4311.candycraftmod.block.LegacyLeavesBlock;
import com.valentin4311.candycraftmod.block.LegacyLogBlock;
import com.valentin4311.candycraftmod.block.LegacyMetadataBlock;
import com.valentin4311.candycraftmod.block.LegacySaplingBlock;
import com.valentin4311.candycraftmod.block.LegacyTypeBlock;
import com.valentin4311.candycraftmod.block.LicoriceFurnaceBlock;
import com.valentin4311.candycraftmod.block.LazyParticleTorchBlock;
import com.valentin4311.candycraftmod.block.LollipopBlock;
import com.valentin4311.candycraftmod.block.LollipopPlantBlock;
import com.valentin4311.candycraftmod.block.MarshmallowChestBlock;
import com.valentin4311.candycraftmod.block.MarshmallowRopeBlock;
import com.valentin4311.candycraftmod.block.MilkCauldronBlock;
import com.valentin4311.candycraftmod.block.NougatHeadBlock;
import com.valentin4311.candycraftmod.block.PuddingBlock;
import com.valentin4311.candycraftmod.block.PorousBiscuitSliceBlock;
import com.valentin4311.candycraftmod.block.PlaceableJellyFoodBlock;
import com.valentin4311.candycraftmod.block.SeaweedBlock;
import com.valentin4311.candycraftmod.block.SpikesBlock;
import com.valentin4311.candycraftmod.block.SugarFactoryBlock;
import com.valentin4311.candycraftmod.block.SugarBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CCBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CandyCraft.MODID);
    public static final List<RegistryObject<? extends Block>> CUTOUT_BLOCKS = new ArrayList<>();
    public static final List<RegistryObject<? extends Block>> TRANSLUCENT_BLOCKS = new ArrayList<>();

    public static final RegistryObject<Block> PUDDING = cutout(register("pudding", () -> new PuddingBlock(wool(MapColor.COLOR_PINK).strength(0.6F).randomTicks())));
    public static final RegistryObject<Block> STRAWBERRY_FILLED_PUDDING_BLOCK = register("strawberry_filled_pudding_block", () -> new Block(wool(MapColor.COLOR_PINK).strength(0.6F)));
    public static final RegistryObject<Block> FLOUR = register("flour", () -> new Block(wool(MapColor.SAND).strength(0.6F)));
    public static final RegistryObject<Block> MARSHMALLOW_PLANKS = register("marshmallow_planks", () -> new LegacyMetadataBlock(wood(MapColor.COLOR_PINK).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_LOG = register("marshmallow_log", () -> new LegacyLogBlock(wood(MapColor.COLOR_PINK).strength(2.0F)));
    public static final RegistryObject<Block> CANDY_SAPLING = cutout(register("candy_sapling", () -> new LegacySaplingBlock(plant())));
    public static final RegistryObject<Block> CANDY_FARMLAND = register("candy_farmland", () -> new CandyFarmlandBlock(earth(MapColor.SAND).strength(0.6F)));
    public static final RegistryObject<Block> LICORICE_ORE = register("licorice_ore", () -> new Block(stone().requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> MARSHMALLOW_FENCE = register("marshmallow_fence", () -> new FenceBlock(wood(MapColor.COLOR_PINK).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_FENCE_DARK = register("marshmallow_fence_dark", () -> new FenceBlock(wood(MapColor.COLOR_BROWN).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_FENCE_LIGHT = register("marshmallow_fence_light", () -> new FenceBlock(wood(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_FENCE_GATE = register("marshmallow_fence_gate", () ->
        new FenceGateBlock(wood(MapColor.COLOR_PINK).strength(3.0F, 5.0F), CCWoodTypes.MARSHMALLOW));
    public static final RegistryObject<Block> MARSHMALLOW_FENCE_GATE_DARK = register("marshmallow_fence_gate_dark", () ->
        new FenceGateBlock(wood(MapColor.COLOR_BROWN).strength(3.0F, 5.0F), CCWoodTypes.MARSHMALLOW_DARK));
    public static final RegistryObject<Block> MARSHMALLOW_FENCE_GATE_LIGHT = register("marshmallow_fence_gate_light", () ->
        new FenceGateBlock(wood(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F), CCWoodTypes.MARSHMALLOW_LIGHT));
    public static final RegistryObject<Block> LICORICE_BRICK = register("licorice_brick", () -> new Block(stone().strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> LICORICE_BRICK_STAIRS = register("licorice_brick_stairs", () -> stairs(Blocks.STONE.defaultBlockState(), stone().strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> LICORICE_BRICK_SLAB = register("licorice_brick_slab", () -> new SlabBlock(stone().strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> LICORICE_BLOCK = register("licorice_block", () -> new Block(metal(MapColor.COLOR_BLACK).strength(5.0F, 10.0F)));
    public static final RegistryObject<Block> COOKIE_BLOCK = register("cookie_block", () -> new Block(cookieBlockProperties()));
    public static final RegistryObject<Block> POROUS_BISCUIT_SLICE = cutout(register("porous_biscuit_slice", () ->
        new PorousBiscuitSliceBlock(cookieBlockProperties().strength(0.5F).noOcclusion())));
    public static final RegistryObject<Block> WAFFLE_BLOCK = register("waffle_block", () -> new Block(cookieBlockProperties()));
    public static final RegistryObject<Block> WAFER_CONE_BLOCK = register("wafer_cone_block", () -> new Block(cookieBlockProperties()));
    public static final RegistryObject<Block> SOLID_WAFER_BLOCK = register("solid_wafer_block", () ->
        new Block(BlockBehaviour.Properties.copy(Blocks.OAK_LOG).mapColor(MapColor.TERRACOTTA_ORANGE)
            .sound(CCSoundTypes.COOKIE)));
    public static final RegistryObject<Block> COOKIE_BLOCK_STAIRS = register("cookie_block_stairs", () ->
        stairs(COOKIE_BLOCK.get().defaultBlockState(), cookieBlockProperties()));
    public static final RegistryObject<Block> COOKIE_BLOCK_SLAB = register("cookie_block_slab", () -> new SlabBlock(cookieBlockProperties()));
    public static final RegistryObject<Block> CANDY_CANE_BLOCK = register("candy_cane_block", () -> new net.minecraft.world.level.block.RotatedPillarBlock(wood(MapColor.COLOR_RED).strength(1.0F, 2.0F)));
    public static final RegistryObject<Block> CANDY_CANE_FENCE = register("candy_cane_fence", () -> new FenceBlock(wood(MapColor.COLOR_RED).strength(1.0F, 2.0F)));
    public static final RegistryObject<Block> CANDY_CANE_WALL = register("candy_cane_wall", () -> new WallBlock(wood(MapColor.COLOR_RED).strength(1.0F, 2.0F)));
    public static final RegistryObject<Block> CANDY_CANE_STAIRS = register("candy_cane_stairs", () -> stairs(Blocks.OAK_PLANKS.defaultBlockState(), wood(MapColor.COLOR_RED).strength(1.0F, 2.0F)));
    public static final RegistryObject<Block> CANDY_CANE_SLAB = register("candy_cane_slab", () -> new SlabBlock(wood(MapColor.COLOR_RED).strength(1.0F, 2.0F)));
    public static final RegistryObject<Block> JELLY_ORE = register("jelly_ore", () -> new Block(stone().requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> STRAWBERRY_JELLY_BLOCK = registerNoItem("strawberry_jelly_block", () ->
        new StrawberryJellyBlock(BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK)
            .mapColor(MapColor.COLOR_PINK).strength(0.3F).sound(SoundType.SLIME_BLOCK).noOcclusion(),
            () -> CCItems.STRAWBERRY_JELLY.get(), () -> CCItems.STRAWBERRY_JELLY_SLICE.get(),
            () -> CCParticleTypes.STRAWBERRY_JELLY_FRAGMENT.get()));
    public static final RegistryObject<Block> CARAMEL_JELLY_BLOCK = registerNoItem("caramel_jelly_block", () ->
        new StrawberryJellyBlock(BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK)
            .mapColor(MapColor.COLOR_ORANGE).strength(0.3F).sound(SoundType.SLIME_BLOCK).noOcclusion(),
            () -> CCItems.CARAMEL_JELLY.get(), () -> CCItems.CARAMEL_JELLY_SLICE.get(),
            () -> CCParticleTypes.CARAMEL_JELLY_FRAGMENT.get()));
    public static final RegistryObject<Block> ROYAL_RATIONS_BLOCK = registerNoItem("royal_rations_block", () ->
        new StrawberryJellyBlock(BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK)
            .mapColor(MapColor.METAL).strength(0.3F).sound(SoundType.SLIME_BLOCK).noOcclusion(),
            () -> CCItems.ROYAL_RATIONS.get(), () -> CCItems.ROYAL_RATIONS_SLICE.get(),
            () -> CCParticleTypes.ROYAL_RATIONS_FRAGMENT.get()));
    // Not wrapped in translucent(): the composite models declare per-child render
    // types (cutout core + translucent shell), and a block-wide translucent layer
    // override would flatten them into a single pass like the big jelly blocks.
    public static final RegistryObject<Block> LEMON_JELLY_FOOD = registerNoItem("lemon_jelly_food", () ->
        new PlaceableJellyFoodBlock(BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK)
            .mapColor(MapColor.COLOR_YELLOW).strength(0.3F).sound(SoundType.SLIME_BLOCK).noOcclusion(),
            () -> CCItems.LEMON_JELLY_SLICE.get(), () -> CCParticleTypes.LEMON_JELLY_FRAGMENT.get()));
    public static final RegistryObject<Block> RASPBERRY_JELLY_FOOD = registerNoItem("raspberry_jelly_food", () ->
        new PlaceableJellyFoodBlock(BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK)
            .mapColor(MapColor.COLOR_RED).strength(0.3F).sound(SoundType.SLIME_BLOCK).noOcclusion(),
            () -> CCItems.RASPBERRY_JELLY_SLICE.get(), () -> CCParticleTypes.RASPBERRY_JELLY_FRAGMENT.get()));
    public static final RegistryObject<Block> MINT_JELLY_FOOD = registerNoItem("mint_jelly_food", () ->
        new PlaceableJellyFoodBlock(BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK)
            .mapColor(MapColor.COLOR_LIGHT_GREEN).strength(0.3F).sound(SoundType.SLIME_BLOCK).noOcclusion(),
            () -> CCItems.MINT_JELLY_SLICE.get(), () -> CCParticleTypes.MINT_JELLY_FRAGMENT.get()));
    public static final RegistryObject<Block> TRAMPOJELLY = translucent(register("trampojelly", () -> new JellyBlock(2.0D, jelly())));
    public static final RegistryObject<Block> RED_TRAMPOJELLY = translucent(register("red_trampojelly", () -> new JellyBlock(4.0D, jelly())));
    public static final RegistryObject<Block> JELLY_SHOCK_ABSORBER = translucent(register("jelly_shock_absorber", () -> new JellyBlock(-1.0D, jelly())));
    public static final RegistryObject<Block> LOLLIPOP_BLOCK = cutout(register("lollipop_block", () -> new LollipopBlock(cropPlant().strength(0.0F, 0.0F))));
    public static final RegistryObject<Block> LOLLIPOP_PLANT = cutout(register("lollipop_plant", () -> new LollipopPlantBlock(cropPlant().randomTicks())));
    public static final RegistryObject<Block> CARAMEL_BLOCK = register("caramel_block", () -> new Block(metal(MapColor.COLOR_ORANGE).strength(2.0F, 2000.0F)));
    public static final RegistryObject<Block> SUGAR_FACTORY = register("sugar_factory", () -> new SugarFactoryBlock(false, metal(MapColor.METAL).strength(2.0F, 5.0F)));
    public static final RegistryObject<Block> LICORICE_FURNACE = register("licorice_furnace", () -> new LicoriceFurnaceBlock(false, stone().strength(5.0F, 10.0F)));
    public static final RegistryObject<Block> LICORICE_FURNACE_ON = registerNoItem("licorice_furnace_on", () -> new LicoriceFurnaceBlock(true, stone().strength(5.0F, 10.0F).lightLevel(state -> 14)));
    public static final RegistryObject<CandyPortalBlock> CANDY_PORTAL = translucent(registerNoItem("candy_portal", () -> new CandyPortalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(-1.0F, 3600000.0F).lightLevel(state -> 12).sound(SoundType.GLASS).noCollission().noOcclusion())));
    public static final RegistryObject<CandyPortalBlock> LIQUID_CANDY_PORTAL = translucent(registerNoItem("liquid_candy_portal", () -> new CandyPortalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(-1.0F, 3600000.0F).lightLevel(state -> 13).sound(SoundType.GLASS).noCollission().noOcclusion(), 1.0F, 0.45F, 0.78F)));
    public static final RegistryObject<Block> SUGAR_BLOCK = register("sugar_block", () -> new SugarBlock(BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(MapColor.SAND).strength(0.3F)));
    public static final RegistryObject<Block> SUGAR_BLOCK_STAIRS = register("sugar_block_stairs", () -> stairs(SUGAR_BLOCK.get().defaultBlockState(), sugarBlockProperties()));
    public static final RegistryObject<Block> SUGAR_BLOCK_SLAB = register("sugar_block_slab", () -> new SlabBlock(sugarBlockProperties()));
    public static final RegistryObject<Block> SUGAR_BLOCK_WALL = register("sugar_block_wall", () -> new WallBlock(sugarBlockProperties()));
    public static final RegistryObject<Block> SUGAR_BRICK = register("sugar_brick", () -> new Block(sugarBlockProperties()));
    public static final RegistryObject<Block> SUGAR_BRICK_STAIRS = register("sugar_brick_stairs", () -> stairs(SUGAR_BRICK.get().defaultBlockState(), sugarBlockProperties()));
    public static final RegistryObject<Block> SUGAR_BRICK_SLAB = register("sugar_brick_slab", () -> new SlabBlock(sugarBlockProperties()));
    public static final RegistryObject<Block> SUGAR_BRICK_WALL = register("sugar_brick_wall", () -> new WallBlock(sugarBlockProperties()));
    public static final RegistryObject<Block> CHOCOLATE_STONE = register("chocolate_stone", () -> new Block(stone().mapColor(MapColor.DIRT).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> CHOCOLATE_COBBLESTONE = register("chocolate_cobblestone", () -> new Block(stone().mapColor(MapColor.COLOR_BROWN).strength(2.0F, 10.0F)));
    public static final RegistryObject<Block> CHOCOLATE_COBBLESTONE_WALL = register("chocolate_cobblestone_wall", () -> new WallBlock(stone().mapColor(MapColor.COLOR_BROWN).strength(2.0F, 10.0F)));
    public static final RegistryObject<Block> DRAGIBUS_CROPS = cutout(registerNoItem("dragibus_crops", () -> new CandyCropBlock(() -> CCItems.DRAGIBUS.get(), cropPlant())));
    public static final RegistryObject<Block> ROPE_LICORICE = cutout(register("rope_licorice", () -> new SeaweedBlock(true, plant())));
    public static final RegistryObject<Block> MINT = cutout(register("mint", () -> new SeaweedBlock(false, plant())));
    public static final RegistryObject<Block> MARSHMALLOW_WORKBENCH = register("marshmallow_workbench", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.MARSHMALLOW, wood(MapColor.COLOR_PINK).strength(2.5F)));
    public static final RegistryObject<Block> MARSHMALLOW_WORKBENCH_LIGHT = register("marshmallow_workbench_light", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.MARSHMALLOW_LIGHT, wood(MapColor.TERRACOTTA_WHITE).strength(2.5F)));
    public static final RegistryObject<Block> MARSHMALLOW_WORKBENCH_DARK = register("marshmallow_workbench_dark", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.MARSHMALLOW_DARK, wood(MapColor.COLOR_BROWN).strength(2.5F)));
    public static final RegistryObject<Block> MARSHMALLOW_LADDER = cutout(register("marshmallow_ladder", () -> new LadderBlock(wood(MapColor.COLOR_PINK).strength(2.5F).noOcclusion())));
    public static final RegistryObject<Block> MARSHMALLOW_ROPE = cutout(register("marshmallow_rope", () ->
        new MarshmallowRopeBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).mapColor(MapColor.COLOR_PINK)
            .sound(SoundType.WOOL).noOcclusion())));
    public static final RegistryObject<Block> MARSHMALLOW_DOOR = cutout(register("marshmallow_door", () -> new DoorBlock(oakDoor(MapColor.COLOR_PINK), BlockSetType.OAK)));
    public static final RegistryObject<Block> MARSHMALLOW_DOOR_DARK = cutout(register("marshmallow_door_dark", () -> new DoorBlock(oakDoor(MapColor.COLOR_BROWN), BlockSetType.OAK)));
    public static final RegistryObject<Block> MARSHMALLOW_DOOR_LIGHT = cutout(register("marshmallow_door_light", () -> new DoorBlock(oakDoor(MapColor.TERRACOTTA_WHITE), BlockSetType.OAK)));
    public static final RegistryObject<Block> MINT_DOOR = cutout(register("mint_door", () -> new DoorBlock(oakDoor(MapColor.COLOR_LIGHT_GREEN), BlockSetType.OAK)));
    public static final RegistryObject<Block> MILK_CHOCOLATE_DOOR = cutout(register("milk_chocolate_door", () -> new DoorBlock(oakDoor(MapColor.COLOR_BROWN), BlockSetType.OAK)));
    public static final RegistryObject<Block> WHITE_CHOCOLATE_DOOR = cutout(register("white_chocolate_door", () -> new DoorBlock(oakDoor(MapColor.SAND), BlockSetType.OAK)));
    public static final RegistryObject<Block> DARK_CHOCOLATE_DOOR = cutout(register("dark_chocolate_door", () -> new DoorBlock(oakDoor(MapColor.TERRACOTTA_BROWN), BlockSetType.OAK)));
    public static final RegistryObject<Block> MILK_CHOCOLATE_TRAPDOOR = cutout(register("milk_chocolate_trapdoor", () -> new TrapDoorBlock(oakTrapdoor(MapColor.COLOR_BROWN), BlockSetType.OAK)));
    public static final RegistryObject<Block> WHITE_CHOCOLATE_TRAPDOOR = cutout(register("white_chocolate_trapdoor", () -> new TrapDoorBlock(oakTrapdoor(MapColor.SAND), BlockSetType.OAK)));
    public static final RegistryObject<Block> DARK_CHOCOLATE_TRAPDOOR = cutout(register("dark_chocolate_trapdoor", () -> new TrapDoorBlock(oakTrapdoor(MapColor.TERRACOTTA_BROWN), BlockSetType.OAK)));
    public static final RegistryObject<Block> FRAISE_TAGADA_FLOWER = cutout(register("fraise_tagada_flower", () -> new LegacyMetadataBlock.Plant(plant())));
    public static final RegistryObject<Block> MARSHMALLOW_CHEST = register("marshmallow_chest", () -> new MarshmallowChestBlock(MarshmallowChestBlock.Theme.NORMAL, wood(MapColor.COLOR_PINK).strength(2.5F).noOcclusion()));
    public static final RegistryObject<Block> MARSHMALLOW_CHEST_DARK = register("marshmallow_chest_dark", () -> new MarshmallowChestBlock(MarshmallowChestBlock.Theme.DARK, wood(MapColor.COLOR_BROWN).strength(2.5F).noOcclusion()));
    public static final RegistryObject<Block> MARSHMALLOW_CHEST_LIGHT = register("marshmallow_chest_light", () -> new MarshmallowChestBlock(MarshmallowChestBlock.Theme.LIGHT, wood(MapColor.TERRACOTTA_WHITE).strength(2.5F).noOcclusion()));
    public static final RegistryObject<Block> HONEY_ORE = register("honey_ore", () -> new Block(stone().strength(3.0F, 5.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> HONEY_TORCH = cutout(register("honey_torch", () -> new LazyParticleTorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).lightLevel(state -> 15), CCParticleTypes.LIQUID_CANDY_FLAME)));
    public static final RegistryObject<Block> HONEY_WALL_TORCH = cutout(registerNoItem("honey_wall_torch", () -> new LazyParticleTorchBlock.Wall(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).lightLevel(state -> 15), CCParticleTypes.LIQUID_CANDY_FLAME)));
    public static final RegistryObject<Block> HONEYCOMB_BLOCK = register("honeycomb_block", () -> new Block(stone().mapColor(MapColor.COLOR_YELLOW).strength(2.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_LANTERN = cutout(register("marshmallow_lantern", () ->
        new LanternBlock(BlockBehaviour.Properties.copy(Blocks.LANTERN).mapColor(MapColor.COLOR_PINK).lightLevel(state -> 15))));
    public static final RegistryObject<Block> HONEY_LAMP = register("honey_lamp", () ->
        new Block(metal(MapColor.COLOR_YELLOW).strength(1.0F).sound(SoundType.GLASS).lightLevel(state -> 15)));
    public static final RegistryObject<Block> HONEY_LANTERN = cutout(register("honey_lantern", () ->
        new LanternBlock(BlockBehaviour.Properties.copy(Blocks.LANTERN).mapColor(MapColor.COLOR_YELLOW).lightLevel(state -> 15))));
    public static final RegistryObject<Block> CARAMEL_LAMP = cutout(register("caramel_lamp", () ->
        new LanternBlock(BlockBehaviour.Properties.copy(Blocks.LANTERN).mapColor(MapColor.COLOR_ORANGE).lightLevel(state -> 15))));
    public static final RegistryObject<Block> MILK_CAULDRON = registerNoItem("milk_cauldron", () ->
        new MilkCauldronBlock(BlockBehaviour.Properties.copy(Blocks.CAULDRON).mapColor(MapColor.SNOW).noOcclusion()));
    public static final RegistryObject<Block> PEZ_ORE = register("pez_ore", () -> new Block(stone().strength(3.0F, 5.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> PEZ_BLOCK = register("pez_block", () -> new Block(metal(MapColor.COLOR_RED).strength(5.0F, 10.0F)));
    public static final RegistryObject<LiquidBlock> CARAMEL = translucent(registerNoItem("caramel", () -> new CandyLiquidBlock(CCFluids.SOURCE_CARAMEL, liquid(MapColor.COLOR_ORANGE), CandyLiquidBlock.Kind.CARAMEL)));
    public static final RegistryObject<LiquidBlock> GRENADINE = translucent(registerNoItem("grenadine", () -> new CandyLiquidBlock(CCFluids.SOURCE_GRENADINE, liquid(MapColor.COLOR_RED), CandyLiquidBlock.Kind.GRENADINE)));
    public static final RegistryObject<Block> STATIC_WATER = translucent(registerNoItem("static_water", () -> new StaticWaterBlock(BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable())));
    public static final RegistryObject<Block> JAW_BREAKER_BLOCK = register("jaw_breaker_block", () -> new Block(stone().strength(-1.0F, 6000000.0F)));
    public static final RegistryObject<Block> PURPLE_TRAMPOJELLY = translucent(register("purple_trampojelly", () -> new JellyBlock(2.1D, jelly().lightLevel(state -> 13))));
    public static final RegistryObject<Block> RASPBERRY_COTTON_CANDY_BLOCK = register("raspberry_cotton_candy_block", () -> new Block(wool(MapColor.COLOR_PINK).strength(0.6F)));
    public static final RegistryObject<Block> JAW_BREAKER_LIGHT = register("jaw_breaker_light", () -> new Block(stone().strength(-1.0F, 6000000.0F).lightLevel(state -> 11)));
    public static final RegistryObject<Block> CRANBERRY_SPIKES = cutout(register("cranberry_spikes", () -> new SpikesBlock(2, true, spikes())));
    public static final RegistryObject<Block> RASPBERRY_COTTON_CANDY_STAIRS = register("raspberry_cotton_candy_stairs", () -> stairs(Blocks.WHITE_WOOL.defaultBlockState(), wool(MapColor.COLOR_PINK).strength(0.6F)));
    public static final RegistryObject<Block> RASPBERRY_COTTON_CANDY_SLAB = register("raspberry_cotton_candy_slab", () -> new SlabBlock(wool(MapColor.COLOR_PINK).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> COTTON_CANDY_BED_BLOCK = cutout(register("cotton_candy_bed_block", () -> new CandyBedBlock(DyeColor.PINK, Blocks.PINK_BED)));
    public static final RegistryObject<Block> MINT_BED_BLOCK = cutout(register("mint_bed_block", () -> new CandyBedBlock(DyeColor.CYAN, Blocks.CYAN_BED)));
    public static final RegistryObject<Block> BANANA_SEAWEED_BED_BLOCK = cutout(register("banana_seaweed_bed_block", () -> new CandyBedBlock(DyeColor.YELLOW, Blocks.YELLOW_BED)));
    public static final RegistryObject<Block> CHEWING_GUM_BED_BLOCK = cutout(register("chewing_gum_bed_block", () -> new CandyBedBlock(DyeColor.PURPLE, Blocks.PURPLE_BED)));
    public static final RegistryObject<Block> MINT_BLOCK = register("mint_block", () -> new Block(wool(MapColor.COLOR_LIGHT_GREEN).strength(1.0F).sound(SoundType.GRASS)));
    public static final RegistryObject<Block> RASPBERRY_BLOCK = register("raspberry_block", () -> new Block(wool(MapColor.COLOR_RED).strength(1.0F).sound(SoundType.GRASS)));
    public static final RegistryObject<Block> JELLY_SENTRY_KEY_HOLE = register("jelly_sentry_key_hole", () -> new DungeonLockBlock(DungeonLockBlock.Kind.JELLY_SENTRY, stone().strength(-1.0F, 6000000.0F)));
    public static final RegistryObject<Block> JELLY_BOSS_KEY_HOLE = register("jelly_boss_key_hole", () -> new DungeonLockBlock(DungeonLockBlock.Kind.JELLY_BOSS, stone().strength(-1.0F, 6000000.0F)));
    public static final RegistryObject<Block> SUGAR_SPIKES = cutout(register("sugar_spikes", () -> new SpikesBlock(4, spikes())));
    public static final RegistryObject<Block> BLOCK_TELEPORTER = registerNoItem("block_teleporter", () -> new DungeonTeleporterBlock(stone().strength(3.0F, 2000.0F).lightLevel(state -> 15).noOcclusion()));
    public static final RegistryObject<Block> COTTON_CANDY_WEB = cutout(register("cotton_candy_web", () -> new CandyWebBlock(BlockBehaviour.Properties.copy(Blocks.COBWEB).mapColor(MapColor.COLOR_PINK))));
    public static final RegistryObject<Block> CHERRY_BLOCK = cutout(registerNoItem("cherry_block", () -> new CherryBlock(wood(MapColor.COLOR_RED).strength(0.2F).noOcclusion())));
    public static final RegistryObject<Block> BANANA_SEAWEED = cutout(register("banana_seaweed", () -> new SeaweedBlock(false, plant())));
    public static final RegistryObject<Block> NOUGAT_ORE = register("nougat_ore", () -> new Block(stone().strength(3.0F, 5.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> ADVANCED_SUGAR_FACTORY = register("advanced_sugar_factory", () -> new SugarFactoryBlock(true, metal(MapColor.METAL).strength(2.0F, 5.0F)));
    public static final RegistryObject<Block> ACID_MINT_FLOWER = cutout(register("acid_mint_flower", () -> new AcidMintFlowerBlock(plant())));
    public static final RegistryObject<Block> NOUGAT_BLOCK = register("nougat_block", () -> new Block(metal(MapColor.COLOR_BROWN).strength(1.0F)));
    public static final RegistryObject<Block> NOUGAT_BLOCK_STAIRS = register("nougat_block_stairs", () -> stairs(NOUGAT_BLOCK.get().defaultBlockState(), metal(MapColor.COLOR_BROWN).strength(1.0F)));
    public static final RegistryObject<Block> NOUGAT_BLOCK_SLAB = register("nougat_block_slab", () -> new SlabBlock(metal(MapColor.COLOR_BROWN).strength(1.0F)));
    public static final RegistryObject<Block> CHISELED_NOUGAT_BLOCK = register("chiseled_nougat_block", () -> new Block(metal(MapColor.COLOR_BROWN).strength(1.0F)));
    public static final RegistryObject<Block> SQUARE_PATTERN_NOUGAT_BLOCK = register("square_pattern_nougat_block", () -> new Block(metal(MapColor.COLOR_BROWN).strength(1.0F)));
    public static final RegistryObject<Block> NOUGAT_HEAD = register("nougat_head", () -> new NougatHeadBlock(metal(MapColor.COLOR_BROWN).strength(1.0F)));
    public static final RegistryObject<Block> BANANA_BLOCK = register("banana_block", () -> new Block(wool(MapColor.COLOR_YELLOW).strength(1.0F).sound(SoundType.GRASS)));
    public static final RegistryObject<Block> CHEWING_GUM_BLOCK = register("chewing_gum_block", () -> new ChewingGumBlock(jelly().mapColor(MapColor.COLOR_PINK).strength(1.0F).noOcclusion()));
    public static final RegistryObject<Block> CHEWING_GUM_PUDDLE = cutout(register("chewing_gum_puddle", () -> new ChewingGumPuddleBlock(jelly().mapColor(MapColor.COLOR_PINK).strength(1.0F).noCollission().noOcclusion())));
    public static final RegistryObject<Block> ALCHEMY_TABLE = cutout(register("alchemy_table", () -> new AlchemyTableBlock(stone().mapColor(MapColor.COLOR_BROWN).strength(1.0F).sound(SoundType.METAL).noOcclusion())));
    public static final RegistryObject<Block> MARSHMALLOW_FLOWER_BLOCK = cutout(register("marshmallow_flower_block", () -> new CandyWaterlilyBlock(true, lilyPad(MapColor.COLOR_PINK))));
    public static final RegistryObject<Block> GRENADINE_BLOCK = translucent(register("grenadine_block", () -> new GlassBlock(glass(MapColor.COLOR_RED).strength(1.0F))));
    public static final RegistryObject<Block> FRAGILE_GRENADINE_BLOCK = translucent(registerNoItem("fragile_grenadine_block", () -> new FragileGrenadineBlock(glass(MapColor.COLOR_RED).strength(0.25F).randomTicks().noLootTable())));
    public static final RegistryObject<Block> ICE_CREAM = register("ice_cream", () -> new LegacyTypeBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.SNOW).strength(1.0F), 3));
    public static final RegistryObject<Block> DRAGON_EGG_BLOCK = register("dragon_egg_block", () -> new DragonEggBlock(stone().mapColor(MapColor.COLOR_BLUE).strength(3.0F, 15.0F).noOcclusion()));
    public static final RegistryObject<Block> BEETLE_EGG_BLOCK = register("beetle_egg_block", () -> new DragonEggBlock(stone().mapColor(MapColor.COLOR_PURPLE).strength(3.0F, 15.0F).noOcclusion()));
    public static final RegistryObject<Block> SUGAR_ESSENCE_FLOWER = cutout(register("sugar_essence_flower", () -> new LegacyMetadataBlock.Plant(plant())));
    public static final RegistryObject<Block> MARSHMALLOW_PLANKS_DARK = register("marshmallow_planks_dark", () -> new Block(wood(MapColor.COLOR_BROWN).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_PLANKS_LIGHT = register("marshmallow_planks_light", () -> new Block(wood(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_LOG_DARK = register("marshmallow_log_dark", () -> new LegacyLogBlock(wood(MapColor.COLOR_BROWN).strength(2.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_LOG_LIGHT = register("marshmallow_log_light", () -> new LegacyLogBlock(wood(MapColor.TERRACOTTA_WHITE).strength(2.0F)));
    public static final RegistryObject<CandyStandingSignBlock> MARSHMALLOW_SIGN = registerNoItem("marshmallow_sign", () ->
        new CandyStandingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SIGN).mapColor(MapColor.COLOR_PINK), CCWoodTypes.MARSHMALLOW));
    public static final RegistryObject<CandyWallSignBlock> MARSHMALLOW_WALL_SIGN = registerNoItem("marshmallow_wall_sign", () ->
        new CandyWallSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WALL_SIGN).mapColor(MapColor.COLOR_PINK).dropsLike(MARSHMALLOW_SIGN.get()), CCWoodTypes.MARSHMALLOW));
    public static final RegistryObject<CandyStandingSignBlock> MARSHMALLOW_SIGN_LIGHT = registerNoItem("marshmallow_sign_light", () ->
        new CandyStandingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SIGN).mapColor(MapColor.TERRACOTTA_WHITE), CCWoodTypes.MARSHMALLOW_LIGHT));
    public static final RegistryObject<CandyWallSignBlock> MARSHMALLOW_WALL_SIGN_LIGHT = registerNoItem("marshmallow_wall_sign_light", () ->
        new CandyWallSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WALL_SIGN).mapColor(MapColor.TERRACOTTA_WHITE).dropsLike(MARSHMALLOW_SIGN_LIGHT.get()), CCWoodTypes.MARSHMALLOW_LIGHT));
    public static final RegistryObject<CandyStandingSignBlock> MARSHMALLOW_SIGN_DARK = registerNoItem("marshmallow_sign_dark", () ->
        new CandyStandingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SIGN).mapColor(MapColor.COLOR_BROWN), CCWoodTypes.MARSHMALLOW_DARK));
    public static final RegistryObject<CandyWallSignBlock> MARSHMALLOW_WALL_SIGN_DARK = registerNoItem("marshmallow_wall_sign_dark", () ->
        new CandyWallSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WALL_SIGN).mapColor(MapColor.COLOR_BROWN).dropsLike(MARSHMALLOW_SIGN_DARK.get()), CCWoodTypes.MARSHMALLOW_DARK));
    public static final RegistryObject<CandyStandingSignBlock> MILK_CHOCOLATE_SIGN = registerNoItem("milk_chocolate_sign", () ->
        new CandyStandingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SIGN).mapColor(MapColor.COLOR_BROWN), CCWoodTypes.MILK_CHOCOLATE));
    public static final RegistryObject<CandyWallSignBlock> MILK_CHOCOLATE_WALL_SIGN = registerNoItem("milk_chocolate_wall_sign", () ->
        new CandyWallSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WALL_SIGN).mapColor(MapColor.COLOR_BROWN).dropsLike(MILK_CHOCOLATE_SIGN.get()), CCWoodTypes.MILK_CHOCOLATE));
    public static final RegistryObject<CandyStandingSignBlock> WHITE_CHOCOLATE_SIGN = registerNoItem("white_chocolate_sign", () ->
        new CandyStandingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SIGN).mapColor(MapColor.SAND), CCWoodTypes.WHITE_CHOCOLATE));
    public static final RegistryObject<CandyWallSignBlock> WHITE_CHOCOLATE_WALL_SIGN = registerNoItem("white_chocolate_wall_sign", () ->
        new CandyWallSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WALL_SIGN).mapColor(MapColor.SAND).dropsLike(WHITE_CHOCOLATE_SIGN.get()), CCWoodTypes.WHITE_CHOCOLATE));
    public static final RegistryObject<CandyStandingSignBlock> DARK_CHOCOLATE_SIGN = registerNoItem("dark_chocolate_sign", () ->
        new CandyStandingSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SIGN).mapColor(MapColor.TERRACOTTA_BROWN), CCWoodTypes.DARK_CHOCOLATE));
    public static final RegistryObject<CandyWallSignBlock> DARK_CHOCOLATE_WALL_SIGN = registerNoItem("dark_chocolate_wall_sign", () ->
        new CandyWallSignBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WALL_SIGN).mapColor(MapColor.TERRACOTTA_BROWN).dropsLike(DARK_CHOCOLATE_SIGN.get()), CCWoodTypes.DARK_CHOCOLATE));
    public static final RegistryObject<Block> CANDY_LEAVES = cutout(register("candy_leaves", () -> new LegacyLeavesBlock(leaves(MapColor.COLOR_BROWN))));
    public static final RegistryObject<Block> CANDY_LEAVES_DARK = cutout(register("candy_leaves_dark", () -> new LegacyLeavesBlock(leaves(MapColor.COLOR_BROWN))));
    public static final RegistryObject<Block> CANDY_LEAVES_LIGHT = cutout(register("candy_leaves_light", () -> new LegacyLeavesBlock(leaves(MapColor.TERRACOTTA_WHITE))));
    public static final RegistryObject<Block> CANDY_LEAVES_CHERRY = cutout(register("candy_leaves_cherry", () -> new CherryLeavesBlock(leaves(MapColor.COLOR_RED).randomTicks())));
    public static final RegistryObject<Block> CANDY_LEAVES_ENCHANT = cutout(register("candy_leaves_enchant", () -> new LegacyLeavesBlock(leaves(MapColor.COLOR_PURPLE))));
    public static final RegistryObject<Block> CREAM_LEAVES = cutout(register("cream_leaves", () -> new LegacyLeavesBlock(leaves(MapColor.TERRACOTTA_WHITE))));
    public static final RegistryObject<Block> PINK_CREAM_LEAVES = cutout(register("pink_cream_leaves", () -> new LegacyLeavesBlock(leaves(MapColor.COLOR_PINK))));
    public static final RegistryObject<Block> MATCHA_CREAM_LEAVES = cutout(register("matcha_cream_leaves", () -> new LegacyLeavesBlock(leaves(MapColor.COLOR_LIGHT_GREEN))));
    public static final RegistryObject<Block> CANDY_SAPLING_DARK = cutout(register("candy_sapling_dark", () -> new LegacySaplingBlock(plant())));
    public static final RegistryObject<Block> CANDY_SAPLING_LIGHT = cutout(register("candy_sapling_light", () -> new LegacySaplingBlock(plant())));
    public static final RegistryObject<Block> CANDY_SAPLING_CHERRY = cutout(register("candy_sapling_cherry", () -> new LegacySaplingBlock(plant())));
    public static final RegistryObject<Block> SWEET_GRASS_PINK = cutout(register("sweet_grass_pink", () -> new LegacyMetadataBlock.Plant(plant())));
    public static final RegistryObject<Block> SWEET_GRASS_PALE = cutout(register("sweet_grass_pale", () -> new LegacyMetadataBlock.Plant(plant())));
    public static final RegistryObject<Block> SWEET_GRASS_YELLOW = cutout(register("sweet_grass_yellow", () -> new LegacyMetadataBlock.Plant(plant())));
    public static final RegistryObject<Block> SWEET_GRASS_RED = cutout(register("sweet_grass_red", () -> new LegacyMetadataBlock.Plant(plant())));
    public static final RegistryObject<Block> COFFEE_CREAM_FERN = cutout(register("coffee_cream_fern", () -> new LegacyMetadataBlock.Plant(plant())));
    public static final RegistryObject<Block> MATCHA_CREAM_GRASS = cutout(register("matcha_cream_grass", () -> new LegacyMetadataBlock.Plant(plant())));
    public static final RegistryObject<Block> SAKURA_CREAM_GRASS = cutout(register("sakura_cream_grass", () -> new LegacyMetadataBlock.Plant(plant())));
    public static final RegistryObject<Block> MARSHMALLOW_STAIRS = register("marshmallow_stairs", () -> stairs(Blocks.OAK_PLANKS.defaultBlockState(), wood(MapColor.COLOR_PINK).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> DARK_MARSHMALLOW_STAIRS = register("dark_marshmallow_stairs", () -> stairs(Blocks.OAK_PLANKS.defaultBlockState(), wood(MapColor.COLOR_BROWN).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> LIGHT_MARSHMALLOW_STAIRS = register("light_marshmallow_stairs", () -> stairs(Blocks.OAK_PLANKS.defaultBlockState(), wood(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_SLAB = register("marshmallow_slab", () -> new SlabBlock(wood(MapColor.COLOR_PINK).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> DARK_MARSHMALLOW_SLAB = register("dark_marshmallow_slab", () -> new SlabBlock(wood(MapColor.COLOR_BROWN).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> LIGHT_MARSHMALLOW_SLAB = register("light_marshmallow_slab", () -> new SlabBlock(wood(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_TRAPDOOR = cutout(register("marshmallow_trapdoor", () -> new TrapDoorBlock(oakTrapdoor(MapColor.COLOR_PINK), BlockSetType.OAK)));
    public static final RegistryObject<Block> MARSHMALLOW_TRAPDOOR_DARK = cutout(register("marshmallow_trapdoor_dark", () -> new TrapDoorBlock(oakTrapdoor(MapColor.COLOR_BROWN), BlockSetType.OAK)));
    public static final RegistryObject<Block> MARSHMALLOW_TRAPDOOR_LIGHT = cutout(register("marshmallow_trapdoor_light", () -> new TrapDoorBlock(oakTrapdoor(MapColor.TERRACOTTA_WHITE), BlockSetType.OAK)));
    public static final RegistryObject<Block> YELLOW_TRAMPOJELLY = translucent(register("yellow_trampojelly", () -> new JellyBlock(1.0D, jelly().mapColor(MapColor.COLOR_YELLOW))));
    public static final RegistryObject<Block> CARAMEL_BRICK = register("caramel_brick", () -> new Block(metal(MapColor.COLOR_ORANGE).strength(2.0F, 2000.0F)));
    public static final RegistryObject<Block> CARAMEL_BRICK_STAIRS = register("caramel_brick_stairs", () -> stairs(Blocks.IRON_BLOCK.defaultBlockState(), metal(MapColor.COLOR_ORANGE).strength(2.0F, 2000.0F)));
    public static final RegistryObject<Block> CARAMEL_BRICK_SLAB = register("caramel_brick_slab", () -> new SlabBlock(metal(MapColor.COLOR_ORANGE).strength(2.0F, 2000.0F)));
    public static final RegistryObject<Block> CHOCOLATE_STONE_STAIRS = register("chocolate_stone_stairs", () -> stairs(Blocks.STONE.defaultBlockState(), stone().mapColor(MapColor.DIRT).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> CHOCOLATE_STONE_SLAB = register("chocolate_stone_slab", () -> new SlabBlock(stone().mapColor(MapColor.DIRT).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> CHOCOLATE_COBBLESTONE_STAIRS = register("chocolate_cobblestone_stairs", () -> stairs(Blocks.COBBLESTONE.defaultBlockState(), stone().mapColor(MapColor.COLOR_BROWN).strength(2.0F, 10.0F)));
    public static final RegistryObject<Block> CHOCOLATE_COBBLESTONE_SLAB = register("chocolate_cobblestone_slab", () -> new SlabBlock(stone().mapColor(MapColor.COLOR_BROWN).strength(2.0F, 10.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_SLICE = cutout(register("marshmallow_slice", () -> new CandyWaterlilyBlock(false, lilyPad(MapColor.COLOR_PINK))));
    public static final RegistryObject<Block> COTTON_CANDY_JUKEBOX = register("cotton_candy_jukebox", () -> new CottonCandyJukeboxBlock(wood(MapColor.COLOR_PINK).strength(2.0F, 6.0F)));
    public static final RegistryObject<Block> SUGUARD_SENTRY_KEY_HOLE = register("suguard_sentry_key_hole", () -> new DungeonLockBlock(DungeonLockBlock.Kind.SUGUARD_SENTRY, stone().strength(-1.0F, 6000000.0F)));
    public static final RegistryObject<Block> SUGUARD_BOSS_KEY_HOLE = register("suguard_boss_key_hole", () -> new DungeonLockBlock(DungeonLockBlock.Kind.SUGUARD_BOSS, stone().strength(-1.0F, 6000000.0F)));
    public static final RegistryObject<Block> CARAMEL_GLASS = translucent(register("caramel_glass", () -> new GlassBlock(glass(MapColor.COLOR_ORANGE).strength(0.3F))));
    public static final RegistryObject<Block> CARAMEL_GLASS_ROUND = translucent(register("caramel_glass_round", () -> new GlassBlock(glass(MapColor.COLOR_ORANGE).strength(0.5F))));
    public static final RegistryObject<Block> CARAMEL_GLASS_DIAMOND = translucent(register("caramel_glass_diamond", () -> new GlassBlock(glass(MapColor.COLOR_ORANGE).strength(0.7F))));
    public static final RegistryObject<Block> CARAMEL_PANE = translucent(register("caramel_pane", () -> new IronBarsBlock(glass(MapColor.COLOR_ORANGE).strength(0.3F))));
    public static final RegistryObject<Block> CARAMEL_PANE_ROUND = translucent(register("caramel_pane_round", () -> new IronBarsBlock(glass(MapColor.COLOR_ORANGE).strength(0.5F))));
    public static final RegistryObject<Block> CARAMEL_PANE_DIAMOND = translucent(register("caramel_pane_diamond", () -> new IronBarsBlock(glass(MapColor.COLOR_ORANGE).strength(0.7F))));
    public static final RegistryObject<Block> DARK_CARAMEL_GLASS = translucent(register("dark_caramel_glass", () -> new GlassBlock(glass(MapColor.COLOR_BROWN).strength(0.3F))));
    public static final RegistryObject<Block> DARK_CARAMEL_GLASS_ROUND = translucent(register("dark_caramel_glass_round", () -> new GlassBlock(glass(MapColor.COLOR_BROWN).strength(0.5F))));
    public static final RegistryObject<Block> DARK_CARAMEL_GLASS_DIAMOND = translucent(register("dark_caramel_glass_diamond", () -> new GlassBlock(glass(MapColor.COLOR_BROWN).strength(0.7F))));
    public static final RegistryObject<Block> DARK_CARAMEL_PANE = translucent(register("dark_caramel_pane", () -> new IronBarsBlock(glass(MapColor.COLOR_BROWN).strength(0.3F))));
    public static final RegistryObject<Block> DARK_CARAMEL_PANE_ROUND = translucent(register("dark_caramel_pane_round", () -> new IronBarsBlock(glass(MapColor.COLOR_BROWN).strength(0.5F))));
    public static final RegistryObject<Block> DARK_CARAMEL_PANE_DIAMOND = translucent(register("dark_caramel_pane_diamond", () -> new IronBarsBlock(glass(MapColor.COLOR_BROWN).strength(0.7F))));
    public static final RegistryObject<Block> HONEY_GLASS = translucent(register("honey_glass", () -> new GlassBlock(glass(MapColor.COLOR_YELLOW).strength(0.3F))));
    public static final RegistryObject<Block> HONEY_GLASS_ROUND = translucent(register("honey_glass_round", () -> new GlassBlock(glass(MapColor.COLOR_YELLOW).strength(0.5F))));
    public static final RegistryObject<Block> HONEY_GLASS_DIAMOND = translucent(register("honey_glass_diamond", () -> new GlassBlock(glass(MapColor.COLOR_YELLOW).strength(0.7F))));
    public static final RegistryObject<Block> HONEY_PANE = translucent(register("honey_pane", () -> new IronBarsBlock(glass(MapColor.COLOR_YELLOW).strength(0.3F))));
    public static final RegistryObject<Block> HONEY_PANE_ROUND = translucent(register("honey_pane_round", () -> new IronBarsBlock(glass(MapColor.COLOR_YELLOW).strength(0.5F))));
    public static final RegistryObject<Block> HONEY_PANE_DIAMOND = translucent(register("honey_pane_diamond", () -> new IronBarsBlock(glass(MapColor.COLOR_YELLOW).strength(0.7F))));
    public static final RegistryObject<Block> SUGAR_GLASS = translucent(register("sugar_glass", () -> new GlassBlock(glass(MapColor.SNOW).strength(0.3F))));
    public static final RegistryObject<Block> SUGAR_GLASS_ROUND = translucent(register("sugar_glass_round", () -> new GlassBlock(glass(MapColor.SNOW).strength(0.5F))));
    public static final RegistryObject<Block> SUGAR_GLASS_DIAMOND = translucent(register("sugar_glass_diamond", () -> new GlassBlock(glass(MapColor.SNOW).strength(0.7F))));
    public static final RegistryObject<Block> SUGAR_PANE = translucent(register("sugar_pane", () -> new IronBarsBlock(glass(MapColor.SNOW).strength(0.3F))));
    public static final RegistryObject<Block> SUGAR_PANE_ROUND = translucent(register("sugar_pane_round", () -> new IronBarsBlock(glass(MapColor.SNOW).strength(0.5F))));
    public static final RegistryObject<Block> SUGAR_PANE_DIAMOND = translucent(register("sugar_pane_diamond", () -> new IronBarsBlock(glass(MapColor.SNOW).strength(0.7F))));
    public static final RegistryObject<Block> GRENADINE_GLASS = translucent(register("grenadine_glass", () -> new GlassBlock(glass(MapColor.COLOR_RED).strength(0.3F))));
    public static final RegistryObject<Block> GRENADINE_GLASS_GRID = translucent(register("grenadine_glass_grid", () -> new GlassBlock(glass(MapColor.COLOR_RED).strength(0.5F))));
    public static final RegistryObject<Block> GRENADINE_PANE = translucent(register("grenadine_pane", () -> new IronBarsBlock(glass(MapColor.COLOR_RED).strength(0.3F))));
    public static final RegistryObject<Block> GRENADINE_PANE_GRID = translucent(register("grenadine_pane_grid", () -> new IronBarsBlock(glass(MapColor.COLOR_RED).strength(0.5F))));
    public static final RegistryObject<Block> STRAWBERRY_ICE_CREAM = register("strawberry_ice_cream", () -> new Block(BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(MapColor.COLOR_RED).strength(1.0F)));
    public static final RegistryObject<Block> MINT_ICE_CREAM = register("mint_ice_cream", () -> new Block(BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(MapColor.COLOR_LIGHT_GREEN).strength(1.0F)));
    public static final RegistryObject<Block> BLUEBERRY_ICE_CREAM = register("blueberry_ice_cream", () -> new Block(BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(MapColor.COLOR_BLUE).strength(1.0F)));
    public static final RegistryObject<Block> CHOCOLATE_ICE_CREAM = register("chocolate_ice_cream", () -> new Block(BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(MapColor.COLOR_BROWN).strength(1.0F)));
    public static final RegistryObject<Block> BANANA_ICE_CREAM = register("banana_ice_cream", () -> new Block(BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(MapColor.COLOR_YELLOW).strength(1.0F)));
    public static final RegistryObject<Block> STRAWBERRY_ICE_CREAM_STAIRS = register("strawberry_ice_cream_stairs", () -> stairs(Blocks.SNOW_BLOCK.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_RED).strength(1.0F)));
    public static final RegistryObject<Block> MINT_ICE_CREAM_STAIRS = register("mint_ice_cream_stairs", () -> stairs(Blocks.SNOW_BLOCK.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_LIGHT_GREEN).strength(1.0F)));
    public static final RegistryObject<Block> BLUEBERRY_ICE_CREAM_STAIRS = register("blueberry_ice_cream_stairs", () -> stairs(Blocks.SNOW_BLOCK.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_BLUE).strength(1.0F)));
    public static final RegistryObject<Block> CHOCOLATE_ICE_CREAM_STAIRS = register("chocolate_ice_cream_stairs", () -> stairs(Blocks.SNOW_BLOCK.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_BROWN).strength(1.0F)));
    public static final RegistryObject<Block> BANANA_ICE_CREAM_STAIRS = register("banana_ice_cream_stairs", () -> stairs(Blocks.SNOW_BLOCK.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_YELLOW).strength(1.0F)));
    public static final RegistryObject<Block> ICE_CREAM_STAIRS = register("ice_cream_stairs", () -> stairs(Blocks.SNOW_BLOCK.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).strength(1.0F)));
    public static final RegistryObject<Block> STRAWBERRY_ICE_CREAM_SLAB = register("strawberry_ice_cream_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_RED).strength(1.0F)));
    public static final RegistryObject<Block> MINT_ICE_CREAM_SLAB = register("mint_ice_cream_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_LIGHT_GREEN).strength(1.0F)));
    public static final RegistryObject<Block> BLUEBERRY_ICE_CREAM_SLAB = register("blueberry_ice_cream_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_BLUE).strength(1.0F)));
    public static final RegistryObject<Block> CHOCOLATE_ICE_CREAM_SLAB = register("chocolate_ice_cream_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_BROWN).strength(1.0F)));
    public static final RegistryObject<Block> BANANA_ICE_CREAM_SLAB = register("banana_ice_cream_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_YELLOW).strength(1.0F)));
    public static final RegistryObject<Block> ICE_CREAM_SLAB = register("ice_cream_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).strength(1.0F)));


    public static final RegistryObject<Block> WAFER_STICK_BLOCK = register("wafer_stick_block", () -> new WaferStickBlock(wood(MapColor.TERRACOTTA_ORANGE).strength(0.9F).noOcclusion()));
    public static final RegistryObject<Block> MATCHA_WAFER_CONE_BLOCK = register("matcha_wafer_cone_block", () -> new WaferStickBlock(wood(MapColor.COLOR_LIGHT_GREEN).strength(0.9F).noOcclusion()));
    public static final RegistryObject<Block> WAFER_CHOCOLATE_SAPLING = cutout(register("wafer_chocolate_sapling",
        () -> new WaferChocolateSaplingBlock(BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING).randomTicks())));
    public static final RegistryObject<Block> MILK_CHOCOLATE_LEAVES = cutout(register("milk_chocolate_leaves", () -> new Block(sweetscapeLeaves(MapColor.COLOR_BROWN))));
    public static final RegistryObject<Block> WHITE_CHOCOLATE_LEAVES = cutout(register("white_chocolate_leaves", () -> new Block(sweetscapeLeaves(MapColor.SAND))));
    public static final RegistryObject<Block> DARK_CHOCOLATE_LEAVES = cutout(register("dark_chocolate_leaves", () -> new Block(sweetscapeLeaves(MapColor.TERRACOTTA_BROWN))));

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
    public static final RegistryObject<Block> MILK_CHOCOLATE_BRICK_STAIRS = register("milk_chocolate_brick_stairs", () -> stairs(MILK_CHOCOLATE_BRICK.get().defaultBlockState(), chocolate(MapColor.COLOR_BROWN)));
    public static final RegistryObject<Block> MILK_CHOCOLATE_BRICK_SLAB = register("milk_chocolate_brick_slab", () -> new SlabBlock(chocolate(MapColor.COLOR_BROWN)));
    public static final RegistryObject<Block> MILK_CHOCOLATE_BRICK_WALL = register("milk_chocolate_brick_wall", () -> new WallBlock(chocolate(MapColor.COLOR_BROWN)));
    public static final RegistryObject<Block> WHITE_CHOCOLATE_BRICK_STAIRS = register("white_chocolate_brick_stairs", () -> stairs(WHITE_CHOCOLATE_BRICK.get().defaultBlockState(), chocolate(MapColor.SAND)));
    public static final RegistryObject<Block> WHITE_CHOCOLATE_BRICK_SLAB = register("white_chocolate_brick_slab", () -> new SlabBlock(chocolate(MapColor.SAND)));
    public static final RegistryObject<Block> WHITE_CHOCOLATE_BRICK_WALL = register("white_chocolate_brick_wall", () -> new WallBlock(chocolate(MapColor.SAND)));
    public static final RegistryObject<Block> DARK_CHOCOLATE_BRICK_STAIRS = register("dark_chocolate_brick_stairs", () -> stairs(DARK_CHOCOLATE_BRICK.get().defaultBlockState(), chocolate(MapColor.TERRACOTTA_BROWN)));
    public static final RegistryObject<Block> DARK_CHOCOLATE_BRICK_SLAB = register("dark_chocolate_brick_slab", () -> new SlabBlock(chocolate(MapColor.TERRACOTTA_BROWN)));
    public static final RegistryObject<Block> DARK_CHOCOLATE_BRICK_WALL = register("dark_chocolate_brick_wall", () -> new WallBlock(chocolate(MapColor.TERRACOTTA_BROWN)));

    public static final RegistryObject<Block> MILK_CHOCOLATE_WORKBENCH = register("milk_chocolate_workbench", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.MILK_CHOCOLATE, wood(MapColor.COLOR_BROWN).strength(0.9F)));
    public static final RegistryObject<Block> WHITE_CHOCOLATE_WORKBENCH = register("white_chocolate_workbench", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.WHITE_CHOCOLATE, wood(MapColor.SAND).strength(0.9F)));
    public static final RegistryObject<Block> DARK_CHOCOLATE_WORKBENCH = register("dark_chocolate_workbench", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.DARK_CHOCOLATE, wood(MapColor.TERRACOTTA_BROWN).strength(0.9F)));

    public static final RegistryObject<Block> COTTON_CANDY_SAPLING = cutout(register("cotton_candy_sapling", () -> new CCPlantBlock(plant())));
    public static final RegistryObject<Block> COTTON_CANDY_BLOCK = cutout(register("cotton_candy_block", () -> new Block(sweetscapeLeaves(MapColor.COLOR_PINK).sound(SoundType.WOOL))));
    public static final RegistryObject<Block> COTTON_CANDY_STAIRS = cutout(register("cotton_candy_stairs", () -> stairs(Blocks.WHITE_WOOL.defaultBlockState(), sweetscapeLeaves(MapColor.COLOR_PINK).sound(SoundType.WOOL))));
    public static final RegistryObject<Block> COTTON_CANDY_SLAB = cutout(register("cotton_candy_slab", () -> new SlabBlock(sweetscapeLeaves(MapColor.COLOR_PINK).sound(SoundType.WOOL))));
    public static final RegistryObject<Block> COTTON_CANDY_PLANT = cutout(register("cotton_candy_plant", () -> new CCPlantBlock(candyPlant(MapColor.COLOR_PINK).sound(SoundType.WOOL))));
    public static final RegistryObject<Block> COTTON_CANDY_BUSH = cutout(register("cotton_candy_bush", () -> new CCPlantBlock(candyPlant(MapColor.COLOR_PINK).sound(SoundType.WOOL))));

    public static final RegistryObject<Block> WHITE_HARD_CANDY_BLOCK = register("white_hard_candy_block", () -> new RotatedPillarBlock(hardCandy(MapColor.TERRACOTTA_WHITE)));
    public static final RegistryObject<Block> RED_HARD_CANDY_BLOCK = register("red_hard_candy_block", () -> new RotatedPillarBlock(hardCandy(MapColor.COLOR_RED)));
    public static final RegistryObject<Block> GREEN_HARD_CANDY_BLOCK = register("green_hard_candy_block", () -> new RotatedPillarBlock(hardCandy(MapColor.COLOR_GREEN)));
    public static final RegistryObject<Block> YELLOW_HARD_CANDY_BLOCK = register("yellow_hard_candy_block", () -> new RotatedPillarBlock(hardCandy(MapColor.COLOR_YELLOW)));
    public static final RegistryObject<Block> ORANGE_HARD_CANDY_BLOCK = register("orange_hard_candy_block", () -> new RotatedPillarBlock(hardCandy(MapColor.COLOR_ORANGE)));
    public static final RegistryObject<Block> LIGHT_BLUE_HARD_CANDY_BLOCK = register("light_blue_hard_candy_block", () -> new RotatedPillarBlock(hardCandy(MapColor.COLOR_LIGHT_BLUE)));
    public static final RegistryObject<Block> PINK_HARD_CANDY_BLOCK = register("pink_hard_candy_block", () -> new RotatedPillarBlock(hardCandy(MapColor.COLOR_PINK)));
    public static final RegistryObject<Block> PURPLE_HARD_CANDY_BLOCK = register("purple_hard_candy_block", () -> new RotatedPillarBlock(hardCandy(MapColor.COLOR_PURPLE)));
    public static final RegistryObject<Block> WHITE_RED_HARD_CANDY_BLOCK = register("white_red_hard_candy_block", () -> new RotatedPillarBlock(hardCandy(MapColor.COLOR_RED)));
    public static final RegistryObject<Block> WHITE_GREEN_HARD_CANDY_BLOCK = register("white_green_hard_candy_block", () -> new RotatedPillarBlock(hardCandy(MapColor.COLOR_GREEN)));
    public static final RegistryObject<Block> WHITE_YELLOW_HARD_CANDY_BLOCK = register("white_yellow_hard_candy_block", () -> new RotatedPillarBlock(hardCandy(MapColor.COLOR_YELLOW)));
    public static final RegistryObject<Block> WHITE_ORANGE_HARD_CANDY_BLOCK = register("white_orange_hard_candy_block", () -> new RotatedPillarBlock(hardCandy(MapColor.COLOR_ORANGE)));
    public static final RegistryObject<Block> WHITE_LIGHT_BLUE_HARD_CANDY_BLOCK = register("white_light_blue_hard_candy_block", () -> new RotatedPillarBlock(hardCandy(MapColor.COLOR_LIGHT_BLUE)));
    public static final RegistryObject<Block> WHITE_PINK_HARD_CANDY_BLOCK = register("white_pink_hard_candy_block", () -> new RotatedPillarBlock(hardCandy(MapColor.COLOR_PINK)));
    public static final RegistryObject<Block> WHITE_PURPLE_HARD_CANDY_BLOCK = register("white_purple_hard_candy_block", () -> new RotatedPillarBlock(hardCandy(MapColor.COLOR_PURPLE)));
    public static final RegistryObject<Block> RED_GREEN_HARD_CANDY_BLOCK = register("red_green_hard_candy_block", () -> new RotatedPillarBlock(hardCandy(MapColor.COLOR_RED)));

    public static final RegistryObject<Block> WHITE_HARD_CANDY_WORKBENCH = register("white_hard_candy_workbench", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.WHITE_HARD_CANDY, hardCandy(MapColor.TERRACOTTA_WHITE)));
    public static final RegistryObject<Block> RED_HARD_CANDY_WORKBENCH = register("red_hard_candy_workbench", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.RED_HARD_CANDY, hardCandy(MapColor.COLOR_RED)));
    public static final RegistryObject<Block> GREEN_HARD_CANDY_WORKBENCH = register("green_hard_candy_workbench", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.GREEN_HARD_CANDY, hardCandy(MapColor.COLOR_GREEN)));
    public static final RegistryObject<Block> WHITE_RED_HARD_CANDY_WORKBENCH = register("white_red_hard_candy_workbench", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.WHITE_RED_HARD_CANDY, hardCandy(MapColor.COLOR_RED)));
    public static final RegistryObject<Block> WHITE_GREEN_HARD_CANDY_WORKBENCH = register("white_green_hard_candy_workbench", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.WHITE_GREEN_HARD_CANDY, hardCandy(MapColor.COLOR_GREEN)));
    public static final RegistryObject<Block> RED_GREEN_HARD_CANDY_WORKBENCH = register("red_green_hard_candy_workbench", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.RED_GREEN_HARD_CANDY, hardCandy(MapColor.COLOR_RED)));

    public static final RegistryObject<Block> CRYSTALLIZED_SUGAR = register("crystallized_sugar", () -> new Block(stone(MapColor.TERRACOTTA_WHITE).strength(1.5F)));
    public static final RegistryObject<Block> PINK_CRYSTALLIZED_SUGAR = register("pink_crystallized_sugar", () -> new Block(stone(MapColor.COLOR_PINK).strength(1.5F)));
    public static final RegistryObject<Block> SMOOTH_PINK_SUGAR = register("smooth_pink_sugar", () -> new Block(deepslatePinkProperties()));
    public static final RegistryObject<Block> SMOOTH_PINK_SUGAR_STAIRS = register("smooth_pink_sugar_stairs", () -> stairs(SMOOTH_PINK_SUGAR.get().defaultBlockState(), deepslatePinkProperties()));
    public static final RegistryObject<Block> SMOOTH_PINK_SUGAR_SLAB = register("smooth_pink_sugar_slab", () -> new SlabBlock(deepslatePinkProperties()));
    public static final RegistryObject<Block> PINK_SUGAR_BRICK = register("pink_sugar_brick", () -> new Block(deepslatePinkProperties()));
    public static final RegistryObject<Block> PINK_SUGAR_BRICK_STAIRS = register("pink_sugar_brick_stairs", () -> stairs(PINK_SUGAR_BRICK.get().defaultBlockState(), deepslatePinkProperties()));
    public static final RegistryObject<Block> PINK_SUGAR_BRICK_SLAB = register("pink_sugar_brick_slab", () -> new SlabBlock(deepslatePinkProperties()));
    public static final RegistryObject<Block> SUGAR_SAND = register("sugar_sand", () -> new FallingBlock(BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(MapColor.SAND).strength(0.5F)));
    public static final RegistryObject<Block> CANDY_GRASS_BLOCK = register("candy_grass_block", () -> new Block(earth(MapColor.COLOR_PINK).randomTicks()));
    public static final RegistryObject<Block> MILK_BROWNIE_BLOCK = register("milk_brownie_block", () -> new Block(earth(MapColor.DIRT)));
    public static final RegistryObject<Block> CHOCOLATE_COVERED_WHITE_BROWNIE = register("chocolate_covered_white_brownie", () -> new Block(earth(MapColor.SAND).randomTicks()));
    public static final RegistryObject<Block> WHITE_BROWNIE_BLOCK = register("white_brownie_block", () -> new Block(earth(MapColor.SAND)));
    public static final RegistryObject<Block> DARK_CANDY_GRASS_BLOCK = register("dark_candy_grass_block", () -> new Block(earth(MapColor.TERRACOTTA_BROWN).randomTicks()));
    public static final RegistryObject<Block> DARK_BROWNIE_BLOCK = register("dark_brownie_block", () -> new Block(earth(MapColor.TERRACOTTA_BROWN)));
    public static final RegistryObject<Block> MILK_BROWNIE_CAKE_ROLL_BLOCK = register("milk_brownie_cake_roll_block", () -> new RotatedPillarBlock(earth(MapColor.DIRT).strength(0.6F)));
    public static final RegistryObject<Block> WHITE_BROWNIE_CAKE_ROLL_BLOCK = register("white_brownie_cake_roll_block", () -> new RotatedPillarBlock(earth(MapColor.SAND).strength(0.6F)));
    public static final RegistryObject<Block> DARK_BROWNIE_CAKE_ROLL_BLOCK = register("dark_brownie_cake_roll_block", () -> new RotatedPillarBlock(earth(MapColor.TERRACOTTA_BROWN).strength(0.6F)));
    public static final RegistryObject<Block> MILK_CHIFFON_CAKE_BLOCK = register("milk_chiffon_cake_block", () -> new RotatedPillarBlock(cake(MapColor.DIRT).strength(0.6F).sound(SoundType.WOOL)));
    public static final RegistryObject<Block> WHITE_CHIFFON_CAKE_BLOCK = register("white_chiffon_cake_block", () -> new RotatedPillarBlock(cake(MapColor.SAND).strength(0.6F).sound(SoundType.WOOL)));
    public static final RegistryObject<Block> DARK_CHIFFON_CAKE_BLOCK = register("dark_chiffon_cake_block", () -> new RotatedPillarBlock(cake(MapColor.TERRACOTTA_BROWN).strength(0.6F).sound(SoundType.WOOL)));
    public static final RegistryObject<Block> MATCHA_CAKE_ROLL = register("matcha_cake_roll", () -> new RotatedPillarBlock(cake(MapColor.COLOR_LIGHT_GREEN).strength(0.6F).sound(SoundType.WOOL)));
    public static final RegistryObject<Block> BAUMKUCHEN_LOG = register("baumkuchen_log", () -> new RotatedPillarBlock(cake(MapColor.COLOR_BROWN).strength(0.6F).sound(SoundType.WOOL)));
    public static final RegistryObject<Block> CAKE_BLOCK = register("cake_block", () -> new Block(cake(MapColor.TERRACOTTA_BROWN).strength(0.5F).sound(SoundType.WOOL).noOcclusion()));
    public static final RegistryObject<Block> MATCHA_CREAM_CAKE_BASE = register("matcha_cream_cake_base", () -> new Block(wool(MapColor.COLOR_LIGHT_GREEN).strength(0.6F)));
    public static final RegistryObject<Block> MATCHA_WHITE_CREAM_CAKE_BASE = register("matcha_white_cream_cake_base", () -> new Block(wool(MapColor.COLOR_LIGHT_GREEN).strength(0.6F)));
    public static final RegistryObject<Block> MATCHA_CAKE_BASE = register("matcha_cake_base", () -> new Block(wool(MapColor.COLOR_LIGHT_GREEN).strength(0.6F)));

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

    public static final RegistryObject<Block> RED_GUMMY_WORKBENCH = register("red_gummy_workbench", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.RED_GUMMY, gummy(MapColor.COLOR_RED)));
    public static final RegistryObject<Block> ORANGE_GUMMY_WORKBENCH = register("orange_gummy_workbench", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.ORANGE_GUMMY, gummy(MapColor.COLOR_ORANGE)));
    public static final RegistryObject<Block> YELLOW_GUMMY_WORKBENCH = register("yellow_gummy_workbench", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.YELLOW_GUMMY, gummy(MapColor.COLOR_YELLOW)));
    public static final RegistryObject<Block> WHITE_GUMMY_WORKBENCH = register("white_gummy_workbench", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.WHITE_GUMMY, gummy(MapColor.SAND)));
    public static final RegistryObject<Block> GREEN_GUMMY_WORKBENCH = register("green_gummy_workbench", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.GREEN_GUMMY, gummy(MapColor.COLOR_LIGHT_GREEN)));
    public static final RegistryObject<LiquidBlock> LIQUID_CHOCOLATE = translucent(registerNoItem("liquid_chocolate", () -> new CandyLiquidBlock(CCFluids.SOURCE_LIQUID_CHOCOLATE, BlockBehaviour.Properties.copy(Blocks.WATER).mapColor(MapColor.COLOR_BROWN).noLootTable(), CandyLiquidBlock.Kind.LIQUID_CHOCOLATE)));
    public static final RegistryObject<LiquidBlock> LIQUID_CANDY = translucent(registerNoItem("liquid_candy", () -> new CandyLiquidBlock(CCFluids.SOURCE_LIQUID_CANDY, BlockBehaviour.Properties.copy(Blocks.WATER).mapColor(MapColor.COLOR_PINK).lightLevel(state -> 12).noLootTable(), CandyLiquidBlock.Kind.LIQUID_CANDY)));

    private CCBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    private static <T extends Block> RegistryObject<T> register(String name, Supplier<T> supplier) {
        RegistryObject<T> block = BLOCKS.register(name, supplier);
        CCItems.registerBlockItem(name, block);
        return block;
    }

    private static <T extends Block> RegistryObject<T> registerNoItem(String name, Supplier<T> supplier) {
        return BLOCKS.register(name, supplier);
    }

    private static <T extends Block> RegistryObject<T> cutout(RegistryObject<T> block) {
        CUTOUT_BLOCKS.add(block);
        return block;
    }

    private static <T extends Block> RegistryObject<T> translucent(RegistryObject<T> block) {
        TRANSLUCENT_BLOCKS.add(block);
        return block;
    }

    private static BlockBehaviour.Properties earth(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.DIRT).mapColor(color).strength(0.6F);
    }

    private static BlockBehaviour.Properties wood(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).mapColor(color);
    }

    private static BlockBehaviour.Properties oakDoor(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.OAK_DOOR).mapColor(color);
    }

    private static BlockBehaviour.Properties oakTrapdoor(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.OAK_TRAPDOOR).mapColor(color);
    }

    private static BlockBehaviour.Properties stone() {
        return BlockBehaviour.Properties.copy(Blocks.STONE);
    }

    private static BlockBehaviour.Properties metal(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).mapColor(color);
    }

    private static BlockBehaviour.Properties wool(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.WHITE_WOOL).mapColor(color);
    }

    private static BlockBehaviour.Properties leaves(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES).mapColor(color).noOcclusion();
    }

    private static BlockBehaviour.Properties plant() {
        return BlockBehaviour.Properties.copy(Blocks.GRASS).noCollission().noOcclusion();
    }

    private static BlockBehaviour.Properties spikes() {
        return plant().offsetType(BlockBehaviour.OffsetType.NONE);
    }

    private static BlockBehaviour.Properties cropPlant() {
        return BlockBehaviour.Properties.copy(Blocks.WHEAT).noCollission().noOcclusion();
    }

    private static BlockBehaviour.Properties lilyPad(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.LILY_PAD).mapColor(color).noOcclusion();
    }

    private static BlockBehaviour.Properties jelly() {
        return BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK).mapColor(MapColor.COLOR_PURPLE)
            .strength(3.0F, 2000.0F).sound(CCSoundTypes.JELLY).noOcclusion();
    }

    private static BlockBehaviour.Properties glass(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.GLASS).mapColor(color).noOcclusion();
    }

    private static BlockBehaviour.Properties liquid(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.WATER).mapColor(color).noLootTable();
    }

    private static BlockBehaviour.Properties candyPlant(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.DEAD_BUSH).mapColor(color).noCollission().noOcclusion();
    }

    private static BlockBehaviour.Properties sweetscapeLeaves(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES).mapColor(color).randomTicks().noOcclusion();
    }

    private static BlockBehaviour.Properties cake(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.CAKE).mapColor(color);
    }

    private static BlockBehaviour.Properties chocolate(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(color).strength(0.7F).sound(SoundType.STONE);
    }

    private static BlockBehaviour.Properties hardCandy(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(color).strength(1.2F).sound(SoundType.STONE);
    }

    private static BlockBehaviour.Properties sugarBlockProperties() {
        return BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(MapColor.SAND).strength(0.3F);
    }

    private static BlockBehaviour.Properties cookieBlockProperties() {
        return BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).mapColor(MapColor.TERRACOTTA_ORANGE)
            .sound(CCSoundTypes.COOKIE);
    }

    private static BlockBehaviour.Properties stone(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.STONE).mapColor(color);
    }

    private static BlockBehaviour.Properties deepslatePinkProperties() {
        return BlockBehaviour.Properties.copy(Blocks.DEEPSLATE).mapColor(MapColor.COLOR_PINK);
    }

    private static BlockBehaviour.Properties gummy(MapColor color) {
        return BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK).mapColor(color).strength(0.4F).friction(0.6F).noOcclusion();
    }
    private static StairBlock stairs(BlockState baseState, BlockBehaviour.Properties properties) {
        return new StairBlock(baseState, properties);
    }
}
