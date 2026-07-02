package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.CandyCropBlock;
import com.valentin4311.candycraftmod.block.CandyFarmlandBlock;
import com.valentin4311.candycraftmod.block.CandyLiquidBlock;
import com.valentin4311.candycraftmod.block.CandyPortalBlock;
import com.valentin4311.candycraftmod.block.CandyWaterlilyBlock;
import com.valentin4311.candycraftmod.block.CandyWebBlock;
import com.valentin4311.candycraftmod.block.CandyWorkbenchBlock;
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
import com.valentin4311.candycraftmod.block.LollipopBlock;
import com.valentin4311.candycraftmod.block.LollipopPlantBlock;
import com.valentin4311.candycraftmod.block.MarshmallowChestBlock;
import com.valentin4311.candycraftmod.block.NougatHeadBlock;
import com.valentin4311.candycraftmod.block.PuddingBlock;
import com.valentin4311.candycraftmod.block.SeaweedBlock;
import com.valentin4311.candycraftmod.block.SpikesBlock;
import com.valentin4311.candycraftmod.block.SugarFactoryBlock;
import com.valentin4311.candycraftmod.block.SugarBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallTorchBlock;
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
    public static final RegistryObject<Block> FLOUR = register("flour", () -> new Block(wool(MapColor.SAND).strength(0.6F)));
    public static final RegistryObject<Block> MARSHMALLOW_PLANKS = register("marshmallow_planks", () -> new LegacyMetadataBlock(wood(MapColor.COLOR_PINK).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_LOG = register("marshmallow_log", () -> new LegacyLogBlock(wood(MapColor.COLOR_PINK).strength(2.0F)));
    public static final RegistryObject<Block> CANDY_LEAVE = cutout(registerNoItem("candy_leave", () -> new LegacyLeavesBlock(leaves(MapColor.COLOR_PINK))));
    public static final RegistryObject<Block> CANDY_LEAVE2 = cutout(registerNoItem("candy_leave2", () -> new LegacyLeavesBlock(leaves(MapColor.COLOR_PURPLE))));
    public static final RegistryObject<Block> CANDY_SAPLING = cutout(register("candy_sapling", () -> new LegacySaplingBlock(plant())));
    public static final RegistryObject<Block> CANDY_FARMLAND = register("candy_farmland", () -> new CandyFarmlandBlock(earth(MapColor.SAND).strength(0.6F)));
    public static final RegistryObject<Block> SWEET_GRASS = cutout(register("sweet_grass", () -> new LegacyMetadataBlock.Plant(plant())));
    public static final RegistryObject<Block> LICORICE_ORE = register("licorice_ore", () -> new Block(stone().requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> MARSHMALLOW_FENCE = register("marshmallow_fence", () -> new FenceBlock(wood(MapColor.COLOR_PINK).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_STAIRS_0 = register("marshmallow_stairs.0", () -> stairs(Blocks.OAK_PLANKS.defaultBlockState(), wood(MapColor.COLOR_PINK).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_STAIRS_1 = register("marshmallow_stairs.1", () -> stairs(Blocks.OAK_PLANKS.defaultBlockState(), wood(MapColor.COLOR_BROWN).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_STAIRS_2 = register("marshmallow_stairs.2", () -> stairs(Blocks.OAK_PLANKS.defaultBlockState(), wood(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_SLAB_0 = register("marshmallow_slab.0", () -> new SlabBlock(wood(MapColor.COLOR_PINK).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_DOUBLE_SLAB_0 = register("marshmallow_double_slab.0", () -> new SlabBlock(wood(MapColor.COLOR_PINK).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_SLAB_1 = register("marshmallow_slab.1", () -> new SlabBlock(wood(MapColor.COLOR_BROWN).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_DOUBLE_SLAB_1 = register("marshmallow_double_slab.1", () -> new SlabBlock(wood(MapColor.COLOR_BROWN).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_SLAB_2 = register("marshmallow_slab.2", () -> new SlabBlock(wood(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_DOUBLE_SLAB_2 = register("marshmallow_double_slab.2", () -> new SlabBlock(wood(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> LICORICE_BRICK = register("licorice_brick", () -> new Block(stone().strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> LICORICE_BRICK_STAIRS = register("licorice_brick_stairs", () -> stairs(Blocks.STONE.defaultBlockState(), stone().strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> LICORICE_BRICK_SLAB = register("licorice_brick_slab", () -> new SlabBlock(stone().strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> LICORICE_BRICK_DOUBLE_SLAB = register("licorice_brick_double_slab", () -> new SlabBlock(stone().strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> LICORICE_BLOCK = register("licorice_block", () -> new Block(metal(MapColor.COLOR_BLACK).strength(5.0F, 10.0F)));
    public static final RegistryObject<Block> CANDY_CANE_BLOCK = register("candy_cane_block", () -> new net.minecraft.world.level.block.RotatedPillarBlock(wood(MapColor.COLOR_RED).strength(1.0F, 2.0F)));
    public static final RegistryObject<Block> CANDY_CANE_FENCE = register("candy_cane_fence", () -> new FenceBlock(wood(MapColor.COLOR_RED).strength(1.0F, 2.0F)));
    public static final RegistryObject<Block> CANDY_CANE_WALL = register("candy_cane_wall", () -> new WallBlock(wood(MapColor.COLOR_RED).strength(1.0F, 2.0F)));
    public static final RegistryObject<Block> CANDY_CANE_STAIRS = register("candy_cane_stairs", () -> stairs(Blocks.OAK_PLANKS.defaultBlockState(), wood(MapColor.COLOR_RED).strength(1.0F, 2.0F)));
    public static final RegistryObject<Block> CANDY_CANE_SLAB = register("candy_cane_slab", () -> new SlabBlock(wood(MapColor.COLOR_RED).strength(1.0F, 2.0F)));
    public static final RegistryObject<Block> CANDY_CANE_DOUBLE_SLAB = register("candy_cane_double_slab", () -> new SlabBlock(wood(MapColor.COLOR_RED).strength(1.0F, 2.0F)));
    public static final RegistryObject<Block> JELLY_ORE = register("jelly_ore", () -> new Block(stone().requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> TRAMPOJELLY = translucent(register("trampojelly", () -> new JellyBlock(2.0D, jelly())));
    public static final RegistryObject<Block> RED_TRAMPOJELLY = translucent(register("red_trampojelly", () -> new JellyBlock(4.0D, jelly())));
    public static final RegistryObject<Block> JELLY_SHOCK_ABSORBER = translucent(register("jelly_shock_absorber", () -> new JellyBlock(-1.0D, jelly())));
    public static final RegistryObject<Block> LOLLIPOP_BLOCK = cutout(register("lollipop_block", () -> new LollipopBlock(cropPlant().strength(0.0F, 0.0F))));
    public static final RegistryObject<Block> LOLLIPOP_PLANT = cutout(register("lollipop_plant", () -> new LollipopPlantBlock(cropPlant().randomTicks())));
    public static final RegistryObject<Block> CARAMEL_BLOCK = register("caramel_block", () -> new Block(metal(MapColor.COLOR_ORANGE).strength(2.0F, 2000.0F)));
    public static final RegistryObject<Block> SUGAR_FACTORY = register("sugar_factory", () -> new SugarFactoryBlock(false, metal(MapColor.METAL).strength(2.0F, 5.0F)));
    public static final RegistryObject<Block> LICORICE_FURNACE = register("licorice_furnace", () -> new LicoriceFurnaceBlock(false, stone().strength(5.0F, 10.0F)));
    public static final RegistryObject<Block> LICORICE_FURNACE_ON = register("licorice_furnace_on", () -> new LicoriceFurnaceBlock(true, stone().strength(5.0F, 10.0F).lightLevel(state -> 14)));
    public static final RegistryObject<CandyPortalBlock> CANDY_PORTAL = translucent(register("candy_portal", () -> new CandyPortalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(-1.0F, 3600000.0F).lightLevel(state -> 12).sound(SoundType.GLASS).noCollission().noOcclusion())));
    public static final RegistryObject<CandyPortalBlock> LIQUID_CANDY_PORTAL = translucent(register("liquid_candy_portal", () -> new CandyPortalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PINK).strength(-1.0F, 3600000.0F).lightLevel(state -> 13).sound(SoundType.GLASS).noCollission().noOcclusion(), 1.0F, 0.45F, 0.78F)));
    public static final RegistryObject<Block> SUGAR_BLOCK = register("sugar_block", () -> new SugarBlock(BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(MapColor.SAND).strength(0.3F)));
    public static final RegistryObject<Block> CHOCOLATE_STONE = register("chocolate_stone", () -> new Block(stone().mapColor(MapColor.DIRT).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> CHOCOLATE_COBBLESTONE = register("chocolate_cobblestone", () -> new Block(stone().mapColor(MapColor.COLOR_BROWN).strength(2.0F, 10.0F)));
    public static final RegistryObject<Block> CHOCOLATE_COBBLESTONE_WALL = register("chocolate_cobblestone_wall", () -> new WallBlock(stone().mapColor(MapColor.COLOR_BROWN).strength(2.0F, 10.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_SLICE_BLOCK = cutout(register("marshmallow_slice_block", () -> new CandyWaterlilyBlock(false, lilyPad(MapColor.COLOR_PINK))));
    public static final RegistryObject<Block> DRAGIBUS_CROPS = cutout(register("dragibus_crops", () -> new CandyCropBlock(() -> CCItems.DRAGIBUS.get(), cropPlant())));
    public static final RegistryObject<Block> ROPE_LICORICE = cutout(register("rope_licorice", () -> new SeaweedBlock(true, plant())));
    public static final RegistryObject<Block> MINT = cutout(register("mint", () -> new SeaweedBlock(false, plant())));
    public static final RegistryObject<Block> MARSHMALLOW_WORKBENCH = register("marshmallow_workbench", () -> new CandyWorkbenchBlock(CandyWorkbenchBlock.CandyWorkbenchTheme.MARSHMALLOW, wood(MapColor.COLOR_PINK).strength(2.5F)));
    public static final RegistryObject<Block> MARSHMALLOW_LADDER = cutout(register("marshmallow_ladder", () -> new LadderBlock(wood(MapColor.COLOR_PINK).strength(2.5F).noOcclusion())));
    public static final RegistryObject<Block> MARSHMALLOW_DOOR = cutout(register("marshmallow_door", () -> new DoorBlock(wood(MapColor.COLOR_PINK).strength(2.5F).noOcclusion(), BlockSetType.OAK)));
    public static final RegistryObject<Block> FRAISE_TAGADA_FLOWER = cutout(register("fraise_tagada_flower", () -> new LegacyMetadataBlock.Plant(plant())));
    public static final RegistryObject<Block> MARSHMALLOW_CHEST = register("marshmallow_chest", () -> new MarshmallowChestBlock(wood(MapColor.COLOR_PINK).strength(2.5F).noOcclusion()));
    public static final RegistryObject<Block> HONEY_ORE = register("honey_ore", () -> new Block(stone().strength(3.0F, 5.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> HONEY_TORCH = cutout(register("honey_torch", () -> new TorchBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).lightLevel(state -> 15), ParticleTypes.FLAME)));
    public static final RegistryObject<Block> HONEY_WALL_TORCH = cutout(registerNoItem("honey_wall_torch", () -> new WallTorchBlock(BlockBehaviour.Properties.copy(Blocks.WALL_TORCH).lightLevel(state -> 15), ParticleTypes.FLAME)));
    public static final RegistryObject<Block> HONEYCOMB_BLOCK = register("honeycomb_block", () -> new Block(stone().mapColor(MapColor.COLOR_YELLOW).strength(2.0F)));
    public static final RegistryObject<Block> HONEY_LAMP = register("honey_lamp", () -> new Block(metal(MapColor.COLOR_YELLOW).strength(1.0F).sound(SoundType.GLASS).lightLevel(state -> 15)));
    public static final RegistryObject<Block> PEZ_ORE = register("pez_ore", () -> new Block(stone().strength(3.0F, 5.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> PEZ_BLOCK = register("pez_block", () -> new Block(metal(MapColor.COLOR_RED).strength(5.0F, 10.0F)));
    public static final RegistryObject<LiquidBlock> CARAMEL = translucent(registerNoItem("caramel", () -> new CandyLiquidBlock(CCFluids.SOURCE_CARAMEL, liquid(MapColor.COLOR_ORANGE), CandyLiquidBlock.Kind.CARAMEL)));
    public static final RegistryObject<LiquidBlock> GRENADINE = translucent(registerNoItem("grenadine", () -> new CandyLiquidBlock(CCFluids.SOURCE_GRENADINE, liquid(MapColor.COLOR_RED), CandyLiquidBlock.Kind.GRENADINE)));
    public static final RegistryObject<Block> JAW_BREAKER_BLOCK = register("jaw_breaker_block", () -> new Block(stone().strength(-1.0F, 6000000.0F)));
    public static final RegistryObject<Block> PURPLE_TRAMPOJELLY = translucent(register("purple_trampojelly", () -> new JellyBlock(2.1D, jelly().lightLevel(state -> 13))));
    public static final RegistryObject<Block> COTTON_CANDY_BLOCK = register("raspberry_cotton_candy_block", () -> new Block(wool(MapColor.COLOR_PINK).strength(0.6F)));
    public static final RegistryObject<Block> JAW_BREAKER_LIGHT = register("jaw_breaker_light", () -> new Block(stone().strength(-1.0F, 6000000.0F).lightLevel(state -> 11)));
    public static final RegistryObject<Block> CRANBERRY_SPIKES = cutout(register("cranberry_spikes", () -> new SpikesBlock(2, plant())));
    public static final RegistryObject<Block> COTTON_CANDY_STAIRS = register("raspberry_cotton_candy_stairs", () -> stairs(Blocks.WHITE_WOOL.defaultBlockState(), wool(MapColor.COLOR_PINK).strength(0.6F)));
    public static final RegistryObject<Block> COTTON_CANDY_SLAB = register("raspberry_cotton_candy_slab", () -> new SlabBlock(wool(MapColor.COLOR_PINK).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> COTTON_CANDY_DOUBLE_SLAB = register("raspberry_cotton_candy_double_slab", () -> new SlabBlock(wool(MapColor.COLOR_PINK).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> COTTON_CANDY_BED_BLOCK = cutout(register("cotton_candy_bed_block", () -> new BedBlock(DyeColor.PINK, wool(MapColor.COLOR_PINK).strength(0.2F).noOcclusion())));
    public static final RegistryObject<Block> MINT_BLOCK = register("mint_block", () -> new Block(wool(MapColor.COLOR_LIGHT_GREEN).strength(1.0F).sound(SoundType.GRASS)));
    public static final RegistryObject<Block> RASPBERRY_BLOCK = register("raspberry_block", () -> new Block(wool(MapColor.COLOR_RED).strength(1.0F).sound(SoundType.GRASS)));
    public static final RegistryObject<Block> JELLY_SENTRY_KEY_HOLE = register("jelly_sentry_key_hole", () -> new DungeonLockBlock(DungeonLockBlock.Kind.JELLY_SENTRY, stone().strength(-1.0F, 6000000.0F)));
    public static final RegistryObject<Block> JELLY_BOSS_KEY_HOLE = register("jelly_boss_key_hole", () -> new DungeonLockBlock(DungeonLockBlock.Kind.JELLY_BOSS, stone().strength(-1.0F, 6000000.0F)));
    public static final RegistryObject<Block> SUGAR_SPIKES = cutout(register("sugar_spikes", () -> new SpikesBlock(4, plant())));
    public static final RegistryObject<Block> BLOCK_TELEPORTER = register("block_teleporter", () -> new DungeonTeleporterBlock(stone().strength(3.0F, 2000.0F).lightLevel(state -> 15).noOcclusion()));
    public static final RegistryObject<Block> COTTON_CANDY_WEB = cutout(register("cotton_candy_web", () -> new CandyWebBlock(BlockBehaviour.Properties.copy(Blocks.COBWEB).mapColor(MapColor.COLOR_PINK))));
    public static final RegistryObject<Block> CHERRY_BLOCK = cutout(registerNoItem("cherry_block", () -> new CherryBlock(wood(MapColor.COLOR_RED).strength(0.2F).noOcclusion())));
    public static final RegistryObject<Block> BANANA_SEAWEED = cutout(register("banana_seaweed", () -> new SeaweedBlock(false, plant())));
    public static final RegistryObject<Block> NOUGAT_ORE = register("nougat_ore", () -> new Block(stone().strength(3.0F, 5.0F).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> ADVANCED_SUGAR_FACTORY = register("advanced_sugar_factory", () -> new SugarFactoryBlock(true, metal(MapColor.METAL).strength(2.0F, 5.0F)));
    public static final RegistryObject<Block> ACID_MINT_FLOWER = cutout(register("acid_mint_flower", () -> new AcidMintFlowerBlock(plant())));
    public static final RegistryObject<Block> NOUGAT_BLOCK = register("nougat_block", () -> new Block(metal(MapColor.COLOR_BROWN).strength(1.0F)));
    public static final RegistryObject<Block> NOUGAT_HEAD = register("nougat_head", () -> new NougatHeadBlock(metal(MapColor.COLOR_BROWN).strength(1.0F)));
    public static final RegistryObject<Block> BANANA_BLOCK = register("banana_block", () -> new Block(wool(MapColor.COLOR_YELLOW).strength(1.0F).sound(SoundType.GRASS)));
    public static final RegistryObject<Block> CHEWING_GUM_BLOCK = register("chewing_gum_block", () -> new ChewingGumBlock(jelly().mapColor(MapColor.COLOR_PINK).strength(1.0F).noOcclusion()));
    public static final RegistryObject<Block> CHEWING_GUM_PUDDLE = cutout(register("chewing_gum_puddle", () -> new ChewingGumPuddleBlock(jelly().mapColor(MapColor.COLOR_PINK).strength(1.0F).noCollission().noOcclusion())));
    public static final RegistryObject<Block> ALCHEMY_TABLE = cutout(register("alchemy_table", () -> new AlchemyTableBlock(stone().mapColor(MapColor.COLOR_BROWN).strength(1.0F).sound(SoundType.METAL).noOcclusion())));
    public static final RegistryObject<Block> MARSHMALLOW_FLOWER_BLOCK = cutout(register("marshmallow_flower_block", () -> new CandyWaterlilyBlock(true, lilyPad(MapColor.COLOR_PINK))));
    public static final RegistryObject<Block> GRENADINE_BLOCK = translucent(register("grenadine_block", () -> new GlassBlock(glass(MapColor.COLOR_RED).strength(1.0F))));
    public static final RegistryObject<Block> FRAGILE_GRENADINE_BLOCK = translucent(registerNoItem("fragile_grenadine_block", () -> new FragileGrenadineBlock(glass(MapColor.COLOR_RED).strength(0.25F).randomTicks().noLootTable())));
    public static final RegistryObject<Block> ICE_CREAM = register("ice_cream", () -> new LegacyTypeBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.SNOW).strength(1.0F), 3));
    public static final RegistryObject<Block> ICE_CREAM_STAIRS_0 = register("ice_cream_stairs.0", () -> stairs(Blocks.SNOW_BLOCK.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).strength(1.0F)));
    public static final RegistryObject<Block> ICE_CREAM_STAIRS_1 = register("ice_cream_stairs.1", () -> stairs(Blocks.SNOW_BLOCK.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).strength(1.0F)));
    public static final RegistryObject<Block> ICE_CREAM_STAIRS_2 = register("ice_cream_stairs.2", () -> stairs(Blocks.SNOW_BLOCK.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).strength(1.0F)));
    public static final RegistryObject<Block> ICE_CREAM_STAIRS_3 = register("ice_cream_stairs.3", () -> stairs(Blocks.SNOW_BLOCK.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).strength(1.0F)));
    public static final RegistryObject<Block> ICE_CREAM_SLAB_0 = register("ice_cream_slab.0", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).strength(1.0F)));
    public static final RegistryObject<Block> ICE_CREAM_DOUBLE_SLAB_0 = register("ice_cream_double_slab.0", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).strength(1.0F)));
    public static final RegistryObject<Block> ICE_CREAM_SLAB_1 = register("ice_cream_slab.1", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).strength(1.0F)));
    public static final RegistryObject<Block> ICE_CREAM_DOUBLE_SLAB_1 = register("ice_cream_double_slab.1", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).strength(1.0F)));
    public static final RegistryObject<Block> ICE_CREAM_SLAB_2 = register("ice_cream_slab.2", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).strength(1.0F)));
    public static final RegistryObject<Block> ICE_CREAM_DOUBLE_SLAB_2 = register("ice_cream_double_slab.2", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).strength(1.0F)));
    public static final RegistryObject<Block> ICE_CREAM_SLAB_3 = register("ice_cream_slab.3", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).strength(1.0F)));
    public static final RegistryObject<Block> ICE_CREAM_DOUBLE_SLAB_3 = register("ice_cream_double_slab.3", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).strength(1.0F)));
    public static final RegistryObject<Block> DRAGON_EGG_BLOCK = register("dragon_egg_block", () -> new DragonEggBlock(stone().mapColor(MapColor.COLOR_BLUE).strength(3.0F, 15.0F).noOcclusion()));
    public static final RegistryObject<Block> BEETLE_EGG_BLOCK = register("beetle_egg_block", () -> new DragonEggBlock(stone().mapColor(MapColor.COLOR_PURPLE).strength(3.0F, 15.0F).noOcclusion()));
    public static final RegistryObject<Block> SUGAR_ESSENCE_FLOWER = cutout(register("sugar_essence_flower", () -> new LegacyMetadataBlock.Plant(plant())));
    public static final RegistryObject<Block> MARSHMALLOW_PLANKS_DARK = register("marshmallow_planks_dark", () -> new Block(wood(MapColor.COLOR_BROWN).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_PLANKS_LIGHT = register("marshmallow_planks_light", () -> new Block(wood(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_LOG_DARK = register("marshmallow_log_dark", () -> new LegacyLogBlock(wood(MapColor.COLOR_BROWN).strength(2.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_LOG_LIGHT = register("marshmallow_log_light", () -> new LegacyLogBlock(wood(MapColor.TERRACOTTA_WHITE).strength(2.0F)));
    public static final RegistryObject<Block> CANDY_LEAVES = cutout(register("candy_leaves", () -> new LegacyLeavesBlock(leaves(MapColor.COLOR_BROWN))));
    public static final RegistryObject<Block> CANDY_LEAVES_DARK = cutout(register("candy_leaves_dark", () -> new LegacyLeavesBlock(leaves(MapColor.COLOR_BROWN))));
    public static final RegistryObject<Block> CANDY_LEAVES_LIGHT = cutout(register("candy_leaves_light", () -> new LegacyLeavesBlock(leaves(MapColor.TERRACOTTA_WHITE))));
    public static final RegistryObject<Block> CANDY_LEAVES_CHERRY = cutout(register("candy_leaves_cherry", () -> new CherryLeavesBlock(leaves(MapColor.COLOR_RED))));
    public static final RegistryObject<Block> CANDY_LEAVES_ENCHANT = cutout(register("candy_leaves_enchant", () -> new LegacyLeavesBlock(leaves(MapColor.COLOR_PURPLE))));
    public static final RegistryObject<Block> CANDY_SAPLING_DARK = cutout(register("candy_sapling_dark", () -> new LegacySaplingBlock(plant())));
    public static final RegistryObject<Block> CANDY_SAPLING_LIGHT = cutout(register("candy_sapling_light", () -> new LegacySaplingBlock(plant())));
    public static final RegistryObject<Block> CANDY_SAPLING_CHERRY = cutout(register("candy_sapling_cherry", () -> new LegacySaplingBlock(plant())));
    public static final RegistryObject<Block> SWEET_GRASS_PINK = cutout(register("sweet_grass_pink", () -> new LegacyMetadataBlock.Plant(plant())));
    public static final RegistryObject<Block> SWEET_GRASS_PALE = cutout(register("sweet_grass_pale", () -> new LegacyMetadataBlock.Plant(plant())));
    public static final RegistryObject<Block> SWEET_GRASS_YELLOW = cutout(register("sweet_grass_yellow", () -> new LegacyMetadataBlock.Plant(plant())));
    public static final RegistryObject<Block> SWEET_GRASS_RED = cutout(register("sweet_grass_red", () -> new LegacyMetadataBlock.Plant(plant())));
    public static final RegistryObject<Block> MARSHMALLOW_STAIRS = register("marshmallow_stairs", () -> stairs(Blocks.OAK_PLANKS.defaultBlockState(), wood(MapColor.COLOR_PINK).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> DARK_MARSHMALLOW_STAIRS = register("dark_marshmallow_stairs", () -> stairs(Blocks.OAK_PLANKS.defaultBlockState(), wood(MapColor.COLOR_BROWN).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> LIGHT_MARSHMALLOW_STAIRS = register("light_marshmallow_stairs", () -> stairs(Blocks.OAK_PLANKS.defaultBlockState(), wood(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_SLAB = register("marshmallow_slab", () -> new SlabBlock(wood(MapColor.COLOR_PINK).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_DOUBLE_SLAB = register("marshmallow_double_slab", () -> new SlabBlock(wood(MapColor.COLOR_PINK).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> DARK_MARSHMALLOW_SLAB = register("dark_marshmallow_slab", () -> new SlabBlock(wood(MapColor.COLOR_BROWN).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> DARK_MARSHMALLOW_DOUBLE_SLAB = register("dark_marshmallow_double_slab", () -> new SlabBlock(wood(MapColor.COLOR_BROWN).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> LIGHT_MARSHMALLOW_SLAB = register("light_marshmallow_slab", () -> new SlabBlock(wood(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> LIGHT_MARSHMALLOW_DOUBLE_SLAB = register("light_marshmallow_double_slab", () -> new SlabBlock(wood(MapColor.TERRACOTTA_WHITE).strength(3.0F, 5.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_TRAPDOOR = cutout(register("marshmallow_trapdoor", () -> new TrapDoorBlock(wood(MapColor.COLOR_PINK).strength(2.5F).noOcclusion(), BlockSetType.OAK)));
    public static final RegistryObject<Block> YELLOW_TRAMPOJELLY = translucent(register("yellow_trampojelly", () -> new JellyBlock(1.0D, jelly().mapColor(MapColor.COLOR_YELLOW))));
    public static final RegistryObject<Block> CARAMEL_BRICK = register("caramel_brick", () -> new Block(metal(MapColor.COLOR_ORANGE).strength(2.0F, 2000.0F)));
    public static final RegistryObject<Block> CARAMEL_BRICK_STAIRS = register("caramel_brick_stairs", () -> stairs(Blocks.IRON_BLOCK.defaultBlockState(), metal(MapColor.COLOR_ORANGE).strength(2.0F, 2000.0F)));
    public static final RegistryObject<Block> CARAMEL_BRICK_SLAB = register("caramel_brick_slab", () -> new SlabBlock(metal(MapColor.COLOR_ORANGE).strength(2.0F, 2000.0F)));
    public static final RegistryObject<Block> CARAMEL_BRICK_DOUBLE_SLAB = register("caramel_brick_double_slab", () -> new SlabBlock(metal(MapColor.COLOR_ORANGE).strength(2.0F, 2000.0F)));
    public static final RegistryObject<Block> CHOCOLATE_STONE_STAIRS = register("chocolate_stone_stairs", () -> stairs(Blocks.STONE.defaultBlockState(), stone().mapColor(MapColor.DIRT).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> CHOCOLATE_STONE_SLAB = register("chocolate_stone_slab", () -> new SlabBlock(stone().mapColor(MapColor.DIRT).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> CHOCOLATE_STONE_DOUBLE_SLAB = register("chocolate_stone_double_slab", () -> new SlabBlock(stone().mapColor(MapColor.DIRT).strength(1.5F, 10.0F)));
    public static final RegistryObject<Block> CHOCOLATE_COBBLESTONE_STAIRS = register("chocolate_cobblestone_stairs", () -> stairs(Blocks.COBBLESTONE.defaultBlockState(), stone().mapColor(MapColor.COLOR_BROWN).strength(2.0F, 10.0F)));
    public static final RegistryObject<Block> CHOCOLATE_COBBLESTONE_SLAB = register("chocolate_cobblestone_slab", () -> new SlabBlock(stone().mapColor(MapColor.COLOR_BROWN).strength(2.0F, 10.0F)));
    public static final RegistryObject<Block> CHOCOLATE_COBBLESTONE_DOUBLE_SLAB = register("chocolate_cobblestone_double_slab", () -> new SlabBlock(stone().mapColor(MapColor.COLOR_BROWN).strength(2.0F, 10.0F)));
    public static final RegistryObject<Block> MARSHMALLOW_SLICE = cutout(register("marshmallow_slice", () -> new CandyWaterlilyBlock(false, lilyPad(MapColor.COLOR_PINK))));
    public static final RegistryObject<Block> COTTON_CANDY_JUKEBOX = register("cotton_candy_jukebox", () -> new net.minecraft.world.level.block.JukeboxBlock(wood(MapColor.COLOR_PINK).strength(2.0F, 6.0F)));
    public static final RegistryObject<Block> SUGUARD_SENTRY_KEY_HOLE = register("suguard_sentry_key_hole", () -> new DungeonLockBlock(DungeonLockBlock.Kind.SUGUARD_SENTRY, stone().strength(-1.0F, 6000000.0F)));
    public static final RegistryObject<Block> SUGUARD_BOSS_KEY_HOLE = register("suguard_boss_key_hole", () -> new DungeonLockBlock(DungeonLockBlock.Kind.SUGUARD_BOSS, stone().strength(-1.0F, 6000000.0F)));
    public static final RegistryObject<Block> CARAMEL_GLASS = translucent(register("caramel_glass", () -> new GlassBlock(glass(MapColor.COLOR_ORANGE).strength(0.3F))));
    public static final RegistryObject<Block> CARAMEL_GLASS_ROUND = translucent(register("caramel_glass_round", () -> new GlassBlock(glass(MapColor.COLOR_ORANGE).strength(0.5F))));
    public static final RegistryObject<Block> CARAMEL_GLASS_DIAMOND = translucent(register("caramel_glass_diamond", () -> new GlassBlock(glass(MapColor.COLOR_ORANGE).strength(0.7F))));
    public static final RegistryObject<Block> CARAMEL_PANE = translucent(register("caramel_pane", () -> new IronBarsBlock(glass(MapColor.COLOR_ORANGE).strength(0.3F))));
    public static final RegistryObject<Block> CARAMEL_PANE_ROUND = translucent(register("caramel_pane_round", () -> new IronBarsBlock(glass(MapColor.COLOR_ORANGE).strength(0.5F))));
    public static final RegistryObject<Block> CARAMEL_PANE_DIAMOND = translucent(register("caramel_pane_diamond", () -> new IronBarsBlock(glass(MapColor.COLOR_ORANGE).strength(0.7F))));
    public static final RegistryObject<Block> STRAWBERRY_ICE_CREAM = register("strawberry_ice_cream", () -> new Block(BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(MapColor.COLOR_RED).strength(1.0F)));
    public static final RegistryObject<Block> MINT_ICE_CREAM = register("mint_ice_cream", () -> new Block(BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(MapColor.COLOR_LIGHT_GREEN).strength(1.0F)));
    public static final RegistryObject<Block> BLUEBERRY_ICE_CREAM = register("blueberry_ice_cream", () -> new Block(BlockBehaviour.Properties.copy(Blocks.SAND).mapColor(MapColor.COLOR_BLUE).strength(1.0F)));
    public static final RegistryObject<Block> STRAWBERRY_ICE_CREAM_STAIRS = register("strawberry_ice_cream_stairs", () -> stairs(Blocks.SNOW_BLOCK.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_RED).strength(1.0F)));
    public static final RegistryObject<Block> MINT_ICE_CREAM_STAIRS = register("mint_ice_cream_stairs", () -> stairs(Blocks.SNOW_BLOCK.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_LIGHT_GREEN).strength(1.0F)));
    public static final RegistryObject<Block> BLUEBERRY_ICE_CREAM_STAIRS = register("blueberry_ice_cream_stairs", () -> stairs(Blocks.SNOW_BLOCK.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_BLUE).strength(1.0F)));
    public static final RegistryObject<Block> ICE_CREAM_STAIRS = register("ice_cream_stairs", () -> stairs(Blocks.SNOW_BLOCK.defaultBlockState(), BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).strength(1.0F)));
    public static final RegistryObject<Block> STRAWBERRY_ICE_CREAM_SLAB = register("strawberry_ice_cream_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_RED).strength(1.0F)));
    public static final RegistryObject<Block> STRAWBERRY_ICE_CREAM_DOUBLE_SLAB = register("strawberry_ice_cream_double_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_RED).strength(1.0F)));
    public static final RegistryObject<Block> MINT_ICE_CREAM_SLAB = register("mint_ice_cream_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_LIGHT_GREEN).strength(1.0F)));
    public static final RegistryObject<Block> MINT_ICE_CREAM_DOUBLE_SLAB = register("mint_ice_cream_double_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_LIGHT_GREEN).strength(1.0F)));
    public static final RegistryObject<Block> BLUEBERRY_ICE_CREAM_SLAB = register("blueberry_ice_cream_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_BLUE).strength(1.0F)));
    public static final RegistryObject<Block> BLUEBERRY_ICE_CREAM_DOUBLE_SLAB = register("blueberry_ice_cream_double_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).mapColor(MapColor.COLOR_BLUE).strength(1.0F)));
    public static final RegistryObject<Block> ICE_CREAM_SLAB = register("ice_cream_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).strength(1.0F)));
    public static final RegistryObject<Block> ICE_CREAM_DOUBLE_SLAB = register("ice_cream_double_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(Blocks.SNOW_BLOCK).strength(1.0F)));

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

    private static StairBlock stairs(BlockState baseState, BlockBehaviour.Properties properties) {
        return new StairBlock(baseState, properties);
    }
}
