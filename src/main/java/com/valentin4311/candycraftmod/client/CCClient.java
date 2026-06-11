package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.block.CandyLiquidBlock;
import com.valentin4311.candycraftmod.block.PuddingBlock;
import com.valentin4311.candycraftmod.client.particle.ChocolateSplashParticle;
import com.valentin4311.candycraftmod.client.model.CandyFishModel;
import com.valentin4311.candycraftmod.client.model.BeeModel;
import com.valentin4311.candycraftmod.client.model.BeetleModel;
import com.valentin4311.candycraftmod.client.model.DragonModel;
import com.valentin4311.candycraftmod.client.model.GummyBunnyModel;
import com.valentin4311.candycraftmod.client.model.MermaidModel;
import com.valentin4311.candycraftmod.client.model.NessieModel;
import com.valentin4311.candycraftmod.client.model.NougatGolemModel;
import com.valentin4311.candycraftmod.client.model.PingouinModel;
import com.valentin4311.candycraftmod.client.model.SuguardModel;
import com.valentin4311.candycraftmod.entity.CandyFishEntity;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCFluids;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCMenus;
import com.valentin4311.candycraftmod.registry.CCParticleTypes;
import com.valentin4311.candycraftmod.registry.CCSweetscapeBlocks;
import com.valentin4311.candycraftmod.item.CaramelCrossbowItem;
import com.valentin4311.candycraftmod.item.DynamiteItem;
import com.valentin4311.candycraftmod.item.RawGummyItem;
import com.valentin4311.candycraftmod.item.SugarPillItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CCClient {
    private static final ResourceLocation PINK_FIRE_TEXTURE = new ResourceLocation("minecraft", "textures/block/fire_1.png");

    private CCClient() {
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            CCBlocks.CUTOUT_BLOCKS.forEach(block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutoutMipped()));
            CCBlocks.TRANSLUCENT_BLOCKS.forEach(block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.translucent()));
            ItemBlockRenderTypes.setRenderLayer(CCFluids.SOURCE_CARAMEL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(CCFluids.FLOWING_CARAMEL.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(CCFluids.SOURCE_GRENADINE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(CCFluids.FLOWING_GRENADINE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(CCFluids.SOURCE_LIQUID_CHOCOLATE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(CCFluids.FLOWING_LIQUID_CHOCOLATE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(CCFluids.SOURCE_LIQUID_CANDY.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(CCFluids.FLOWING_LIQUID_CANDY.get(), RenderType.translucent());
            MenuScreens.register(CCMenus.SUGAR_FACTORY.get(), SugarFactoryScreen::new);
            MenuScreens.register(CCMenus.LICORICE_FURNACE.get(), LicoriceFurnaceScreen::new);
            registerProjectileItemProperties();
        });
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(CCParticleTypes.CHOCOLATE_SPLASH.get(), ChocolateSplashParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(AlchemyTableRenderer.MIX_MODEL);
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerBelowAll("pink_fire", CCClient::renderPinkFireOverlay);
    }

    private static void renderPinkFireOverlay(net.minecraftforge.client.gui.overlay.ForgeGui gui, GuiGraphics graphics,
            float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = gui.getMinecraft();
        if (minecraft.player == null || pinkFireTicks(minecraft.player) <= 0 || minecraft.player.isSpectator()) {
            return;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 0.72F, 0.94F, 0.38F);
        int size = Math.max(56, Math.min(90, screenWidth / 9));
        int y = screenHeight - size;
        graphics.blit(PINK_FIRE_TEXTURE, -size / 5, y, 0, 0.0F, 0.0F, size, size, 16, 16);
        graphics.blit(PINK_FIRE_TEXTURE, screenWidth - size + size / 5, y, 0, 0.0F, 0.0F, size, size, 16, 16);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static int pinkFireTicks(net.minecraft.world.entity.player.Player player) {
        return player.getPersistentData().getInt(CandyLiquidBlock.PINK_FIRE_TICKS_TAG);
    }

    private static void registerProjectileItemProperties() {
        ItemProperties.register(CCItems.DYNAMITE.get(), new ResourceLocation("stage"), (stack, level, entity, seed) ->
            entity != null ? DynamiteItem.modelStage(stack, entity) : 0.0F
        );
        ItemProperties.register(CCItems.GLUE_DYNAMITE.get(), new ResourceLocation("stage"), (stack, level, entity, seed) ->
            entity != null ? DynamiteItem.modelStage(stack, entity) : 0.0F
        );
        ItemProperties.register(CCItems.CARAMEL_BOW.get(), new ResourceLocation("pull"), (stack, level, entity, seed) -> {
            if (entity == null || entity.getUseItem() != stack) {
                return 0.0F;
            }
            return (float)(stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 10.0F;
        });
        ItemProperties.register(CCItems.CARAMEL_BOW.get(), new ResourceLocation("pulling"), (stack, level, entity, seed) ->
            entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F
        );
        ItemProperties.register(CCItems.CARAMEL_CROSSBOW.get(), new ResourceLocation("pull"), (stack, level, entity, seed) -> {
            if (entity == null || CrossbowItem.isCharged(stack)) {
                return 0.0F;
            }
            return entity.getUseItem() != stack ? 0.0F : (float)(stack.getUseDuration() - entity.getUseItemRemainingTicks()) / CaramelCrossbowItem.HONEY_CHARGE_DURATION;
        });
        ItemProperties.register(CCItems.CARAMEL_CROSSBOW.get(), new ResourceLocation("pulling"), (stack, level, entity, seed) ->
            entity != null && entity.isUsingItem() && entity.getUseItem() == stack && !CrossbowItem.isCharged(stack) ? 1.0F : 0.0F
        );
        ItemProperties.register(CCItems.CARAMEL_CROSSBOW.get(), new ResourceLocation("charged"), (stack, level, entity, seed) ->
            CrossbowItem.isCharged(stack) ? 1.0F : 0.0F
        );
    }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        BlockColors colors = event.getBlockColors();
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex < 0) {
                return -1;
            }
            return level != null && pos != null ? puddingColor(level, pos) : PuddingBlock.DEFAULT_COLOR;
        }, CCBlocks.PUDDING.get());
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex < 0) {
                return -1;
            }
            return level != null && pos != null ? enchantedLeavesColor(level, pos) : 0x8f8ac8;
        }, CCBlocks.CANDY_LEAVES_ENCHANT.get(), CCBlocks.CANDY_LEAVE2.get());
        registerGummyBlockColors(colors);
    }

    private static int puddingColor(net.minecraft.world.level.BlockAndTintGetter level, net.minecraft.core.BlockPos pos) {
        String biomePath = biomePath(biomeAt(level, pos));
        if ("sugar_enchanted_forest".equals(biomePath)) {
            return averageLegacyCandyGrassColor(level, pos, 3);
        }
        return averageLegacyCandyGrassColor(level, pos, 1);
    }

    private static int enchantedLeavesColor(net.minecraft.world.level.BlockAndTintGetter level, net.minecraft.core.BlockPos pos) {
        return averageLegacyCandyGrassColor(level, pos, 1);
    }

    private static int averageLegacyCandyGrassColor(net.minecraft.world.level.BlockAndTintGetter level, net.minecraft.core.BlockPos center, int radius) {
        int red = 0;
        int green = 0;
        int blue = 0;
        int samples = 0;
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                net.minecraft.core.BlockPos samplePos = center.offset(dx, 0, dz);
                String biomePath = biomePath(biomeAt(level, samplePos));
                if (biomePath == null) {
                    continue;
                }
                int color = legacyCandyGrassColor(biomePath, samplePos.getX(), samplePos.getZ());
                red += (color >> 16) & 255;
                green += (color >> 8) & 255;
                blue += color & 255;
                samples++;
            }
        }
        if (samples == 0) {
            return 0xEEAABB;
        }
        return ((red / samples) & 255) << 16 | ((green / samples) & 255) << 8 | (blue / samples) & 255;
    }

    private static net.minecraft.core.Holder<Biome> biomeAt(net.minecraft.world.level.BlockAndTintGetter level, net.minecraft.core.BlockPos pos) {
        if (level instanceof LevelReader reader) {
            return reader.getBiome(pos);
        }
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level != null ? minecraft.level.getBiome(pos) : null;
    }

    private static int legacyCandyGrassColor(net.minecraft.core.Holder<Biome> biome, double x, double z) {
        String path = biomePath(biome);
        return path != null
            ? legacyCandyGrassColor(path, x, z)
            : biome != null ? biome.value().getGrassColor(x, z) : legacyCandyGrassColor("sugar_plains", x, z);
    }

    private static int legacyCandyFogColor(Level level, net.minecraft.core.BlockPos pos) {
        if (!level.hasChunkAt(pos)) {
            return legacyCandyGrassColor("sugar_plains", pos.getX(), pos.getZ());
        }

        String path = biomePath(level.getBiome(pos));
        if (path != null) {
            return legacyCandyGrassColor(path, pos.getX(), pos.getZ());
        }

        int red = 0;
        int green = 0;
        int blue = 0;
        int samples = 0;
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                net.minecraft.core.BlockPos samplePos = pos.offset(dx * 8, 0, dz * 8);
                if (!level.hasChunkAt(samplePos)) {
                    continue;
                }
                String samplePath = biomePath(level.getBiome(samplePos));
                if (samplePath == null) {
                    continue;
                }
                int color = legacyCandyGrassColor(samplePath, samplePos.getX(), samplePos.getZ());
                red += (color >> 16) & 255;
                green += (color >> 8) & 255;
                blue += color & 255;
                samples++;
            }
        }

        if (samples == 0) {
            return legacyCandyGrassColor("sugar_plains", pos.getX(), pos.getZ());
        }
        return ((red / samples) & 255) << 16 | ((green / samples) & 255) << 8 | (blue / samples) & 255;
    }

    private static String biomePath(net.minecraft.core.Holder<Biome> biome) {
        if (biome == null) {
            return null;
        }
        return biome.unwrapKey()
            .map(key -> key.location())
            .filter(id -> CandyCraft.MODID.equals(id.getNamespace()))
            .map(ResourceLocation::getPath)
            .orElse(null);
    }

    private static int legacyCandyGrassColor(String biomePath, double x, double z) {
        return switch (biomePath) {
            case "sugar_enchanted_forest" -> enchantedColor(x, z);
            case "sugar_plains", "sugar_forest" -> 0xEEAABB;
            case "sugar_mountains" -> 0xEEBBCC;
            case "sugar_cold_forest" -> 0xFFDDEE;
            case "ice_cream_plains", "sugar_hell_mountains" -> 0xFFFFFF;
            case "sugar_oceans" -> 0xB35EFF;
            case "caramel_forest" -> 0xB05C28;
            case "cotton_candy_plains" -> 0xFFC6E4;
            case "gummy_swamp" -> 0xFFFEB0;
            case "chocolate_forest" -> 0xF3DFA8;
            case "sugar_river", "candycraft_dungeon" -> 0xFFBBCC;
            default -> 0xFFBBCC;
        };
    }

    private static int enchantedColor(double x, double z) {
        double noise = Biome.BIOME_INFO_NOISE.getValue(x * 0.0225D, z * 0.0225D, false);
        if (noise < -0.5D) {
            double blend = smoothstep(-0.85D, -0.5D, noise);
            return lerpColor(0xB0ECFF, 0xB0D8FF, blend);
        }
        if (noise < -0.1D) {
            double blend = smoothstep(-0.5D, -0.1D, noise);
            return lerpColor(0xB0D8FF, 0xB0B0FF, blend);
        }
        double blend = smoothstep(-0.1D, 0.45D, noise);
        return lerpColor(0xB0B0FF, 0xA376DA, blend);
    }

    private static double smoothstep(double edge0, double edge1, double value) {
        double t = (value - edge0) / (edge1 - edge0);
        t = Math.max(0.0D, Math.min(1.0D, t));
        return t * t * (3.0D - 2.0D * t);
    }

    private static int lerpColor(int from, int to, double amount) {
        int fromRed = (from >> 16) & 255;
        int fromGreen = (from >> 8) & 255;
        int fromBlue = from & 255;
        int toRed = (to >> 16) & 255;
        int toGreen = (to >> 8) & 255;
        int toBlue = to & 255;
        int red = (int)Math.round(fromRed + (toRed - fromRed) * amount);
        int green = (int)Math.round(fromGreen + (toGreen - fromGreen) * amount);
        int blue = (int)Math.round(fromBlue + (toBlue - fromBlue) * amount);
        return (red & 255) << 16 | (green & 255) << 8 | (blue & 255);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        ItemColors colors = event.getItemColors();
        colors.register((stack, tintIndex) -> tintIndex >= 0 ? PuddingBlock.DEFAULT_COLOR : -1, CCBlocks.PUDDING.get());
        colors.register(SugarPillItem::getLayerColor, CCItems.SUGAR_PILL.get());
        colors.register((stack, tintIndex) -> 0xff4530,
            CCSweetscapeBlocks.RED_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.RED_HARDENED_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.RED_GUMMY_WORKBENCH.get(),
            CCSweetscapeBlocks.RED_GUMMY_WORM_BLOCK.get()
        );
        colors.register((stack, tintIndex) -> 0xff9b4f,
            CCSweetscapeBlocks.ORANGE_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.ORANGE_HARDENED_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.ORANGE_GUMMY_WORKBENCH.get(),
            CCSweetscapeBlocks.ORANGE_GUMMY_WORM_BLOCK.get()
        );
        colors.register((stack, tintIndex) -> 0xffe563,
            CCSweetscapeBlocks.YELLOW_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.YELLOW_HARDENED_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.YELLOW_GUMMY_WORKBENCH.get(),
            CCSweetscapeBlocks.YELLOW_GUMMY_WORM_BLOCK.get()
        );
        colors.register((stack, tintIndex) -> 0xfffeb0,
            CCSweetscapeBlocks.WHITE_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.WHITE_HARDENED_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.WHITE_GUMMY_WORKBENCH.get(),
            CCSweetscapeBlocks.WHITE_GUMMY_WORM_BLOCK.get()
        );
        colors.register((stack, tintIndex) -> 0x80e22b,
            CCSweetscapeBlocks.GREEN_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.GREEN_HARDENED_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.GREEN_GUMMY_WORKBENCH.get(),
            CCSweetscapeBlocks.GREEN_GUMMY_WORM_BLOCK.get()
        );
    }

    private static void registerGummyBlockColors(BlockColors colors) {
        colors.register((state, level, pos, tintIndex) -> 0xff4530,
            CCSweetscapeBlocks.RED_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.RED_HARDENED_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.RED_GUMMY_WORKBENCH.get(),
            CCSweetscapeBlocks.RED_GUMMY_WORM_BLOCK.get()
        );
        colors.register((state, level, pos, tintIndex) -> 0xff9b4f,
            CCSweetscapeBlocks.ORANGE_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.ORANGE_HARDENED_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.ORANGE_GUMMY_WORKBENCH.get(),
            CCSweetscapeBlocks.ORANGE_GUMMY_WORM_BLOCK.get()
        );
        colors.register((state, level, pos, tintIndex) -> 0xffe563,
            CCSweetscapeBlocks.YELLOW_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.YELLOW_HARDENED_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.YELLOW_GUMMY_WORKBENCH.get(),
            CCSweetscapeBlocks.YELLOW_GUMMY_WORM_BLOCK.get()
        );
        colors.register((state, level, pos, tintIndex) -> 0xfffeb0,
            CCSweetscapeBlocks.WHITE_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.WHITE_HARDENED_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.WHITE_GUMMY_WORKBENCH.get(),
            CCSweetscapeBlocks.WHITE_GUMMY_WORM_BLOCK.get()
        );
        colors.register((state, level, pos, tintIndex) -> 0x80e22b,
            CCSweetscapeBlocks.GREEN_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.GREEN_HARDENED_GUMMY_BLOCK.get(),
            CCSweetscapeBlocks.GREEN_GUMMY_WORKBENCH.get(),
            CCSweetscapeBlocks.GREEN_GUMMY_WORM_BLOCK.get()
        );
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(com.valentin4311.candycraftmod.registry.CCBlockEntities.ALCHEMY_TABLE.get(), AlchemyTableRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.HONEY_ARROW.get(), HoneyArrowRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.HONEY_BOLT.get(), HoneyBoltRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.DYNAMITE.get(), context -> new FixedThrownItemRenderer<>(context, new net.minecraft.world.item.ItemStack(CCItems.DYNAMITE.get()), 0.5F));
        event.registerEntityRenderer(CCEntityTypes.GLUE_DYNAMITE.get(), context -> new FixedThrownItemRenderer<>(context, new net.minecraft.world.item.ItemStack(CCItems.GLUE_DYNAMITE.get()), 0.5F));
        event.registerEntityRenderer(CCEntityTypes.GUMMY_BALL.get(), GummyBallRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.CANDY_PIG.get(), CandyPigRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.WAFFLE_SHEEP.get(), WaffleSheepRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.CANDY_CREEPER.get(), CandyCreeperRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.COTTON_CANDY_SPIDER.get(), CottonCandySpiderRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.SUGUARD.get(), SuguardRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.MAGE_SUGUARD.get(), SuguardRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.CANDY_WOLF.get(), CandyWolfRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.GUMMY_BUNNY.get(), GummyBunnyRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.GINGERBREAD_MAN.get(), GingerbreadManRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.CANDY_FISH.get(), CandyFishRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.PINGOUIN.get(), PingouinRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.NESSIE.get(), NessieRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.DRAGON.get(), DragonRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.KING_BEETLE.get(), KingBeetleRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.MERMAID.get(), MermaidRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.NOUGAT_GOLEM.get(), NougatGolemRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.BOSS_SUGUARD.get(), SuguardRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.CARAMEL_BEE.get(), BeeRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.BEETLE.get(), BeetleRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.BOSS_BEETLE.get(), BeetleRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.YELLOW_JELLY.get(), BasicCandySlimeRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.RED_JELLY.get(), BasicCandySlimeRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.TORNADO_JELLY.get(), BasicCandySlimeRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.PEZ_JELLY.get(), BasicCandySlimeRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.KING_SLIME.get(), BasicCandySlimeRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.JELLY_QUEEN.get(), BasicCandySlimeRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CandyFishModel.LAYER, CandyFishModel::createBodyLayer);
        event.registerLayerDefinition(PingouinModel.LAYER, PingouinModel::createBodyLayer);
        event.registerLayerDefinition(GummyBunnyModel.LAYER, GummyBunnyModel::createBodyLayer);
        event.registerLayerDefinition(GingerbreadManModel.LAYER, GingerbreadManModel::createBodyLayer);
        event.registerLayerDefinition(SuguardModel.LAYER, SuguardModel::createBodyLayer);
        event.registerLayerDefinition(BeeModel.LAYER, BeeModel::createBodyLayer);
        event.registerLayerDefinition(BeetleModel.LAYER, BeetleModel::createBodyLayer);
        event.registerLayerDefinition(NessieModel.LAYER, NessieModel::createBodyLayer);
        event.registerLayerDefinition(DragonModel.LAYER, DragonModel::createBodyLayer);
        event.registerLayerDefinition(MermaidModel.LAYER, MermaidModel::createBodyLayer);
        event.registerLayerDefinition(NougatGolemModel.LAYER, NougatGolemModel::createBodyLayer);
    }

    @Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class CommonModEvents {
        @SubscribeEvent
        public static void registerSpawnPlacements(SpawnPlacementRegisterEvent event) {
            event.register(CCEntityTypes.CANDY_FISH.get(), SpawnPlacements.Type.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, CandyFishEntity::canSpawn, SpawnPlacementRegisterEvent.Operation.REPLACE);
        }
    }

    @Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeEvents {
        private static final int FLUID_NONE = 0;
        private static final int FLUID_CHOCOLATE = 1;
        private static final int FLUID_CANDY = 2;
        private static net.minecraft.resources.ResourceKey<Level> fogLevelKey;
        private static float smoothedFogRed;
        private static float smoothedFogGreen;
        private static float smoothedFogBlue;
        private static long lastFogSampleTime;
        private static boolean fogColorInitialized;

        private ForgeEvents() {
        }

        @SubscribeEvent
        public static void computeFogColor(ViewportEvent.ComputeFogColor event) {
            int view = fluidView(event.getCamera());
            if (view == FLUID_CHOCOLATE) {
                event.setRed(0.48F);
                event.setGreen(0.28F);
                event.setBlue(0.18F);
            } else if (view == FLUID_CANDY) {
                event.setRed(1.0F);
                event.setGreen(0.26F);
                event.setBlue(0.58F);
            } else if (isCandyWorld(event.getCamera())) {
                Level level = event.getCamera().getEntity().level();
                net.minecraft.core.BlockPos pos = event.getCamera().getBlockPosition();
                int color = legacyCandyFogColor(level, pos);
                float dayFactor = candyDayFactor(level, (float)event.getPartialTick());
                float targetRed = ((color >> 16) & 255) / 255.0F * dayFactor;
                float targetGreen = ((color >> 8) & 255) / 255.0F * dayFactor;
                float targetBlue = (color & 255) / 255.0F * dayFactor;
                long now = System.nanoTime();
                if (!fogColorInitialized || fogLevelKey != level.dimension()) {
                    fogLevelKey = level.dimension();
                    smoothedFogRed = targetRed;
                    smoothedFogGreen = targetGreen;
                    smoothedFogBlue = targetBlue;
                    lastFogSampleTime = now;
                    fogColorInitialized = true;
                } else {
                    float deltaSeconds = Math.max(0.0F, Math.min(0.25F, (now - lastFogSampleTime) / 1_000_000_000.0F));
                    lastFogSampleTime = now;
                    float blend = 1.0F - (float)Math.exp(-deltaSeconds * 3.0F);
                    smoothedFogRed += (targetRed - smoothedFogRed) * blend;
                    smoothedFogGreen += (targetGreen - smoothedFogGreen) * blend;
                    smoothedFogBlue += (targetBlue - smoothedFogBlue) * blend;
                }
                event.setRed(smoothedFogRed);
                event.setGreen(smoothedFogGreen);
                event.setBlue(smoothedFogBlue);
            }
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) {
                return;
            }
            int ticks = pinkFireTicks(minecraft.player);
            if (ticks > 0) {
                if (minecraft.player.getAbilities().instabuild && !isInLiquidCandy(minecraft.player)) {
                    minecraft.player.getPersistentData().remove(CandyLiquidBlock.PINK_FIRE_TICKS_TAG);
                    minecraft.player.clearFire();
                    return;
                }
                minecraft.player.getPersistentData().putInt(CandyLiquidBlock.PINK_FIRE_TICKS_TAG, ticks - 1);
            }
        }

        @SubscribeEvent
        public static void renderBlockOverlay(RenderBlockScreenEffectEvent event) {
            if (event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE && pinkFireTicks(event.getPlayer()) > 0) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void renderFog(ViewportEvent.RenderFog event) {
            int view = fluidView(event.getCamera());
            if (view == FLUID_NONE) {
                return;
            }
            event.setNearPlaneDistance(-8.0F);
            event.setFarPlaneDistance(event.getFarPlaneDistance() * (view == FLUID_CHOCOLATE ? 0.55F : 0.35F));
        }

        private static int fluidView(net.minecraft.client.Camera camera) {
            Level level = camera.getEntity().level();
            FluidState state = level.getFluidState(camera.getBlockPosition());
            if (state.is(CCFluids.SOURCE_LIQUID_CHOCOLATE.get()) || state.is(CCFluids.FLOWING_LIQUID_CHOCOLATE.get())) {
                return FLUID_CHOCOLATE;
            }
            if (state.is(CCFluids.SOURCE_LIQUID_CANDY.get()) || state.is(CCFluids.FLOWING_LIQUID_CANDY.get())) {
                return FLUID_CANDY;
            }
            return FLUID_NONE;
        }

        private static boolean isCandyWorld(net.minecraft.client.Camera camera) {
            ResourceLocation dimension = camera.getEntity().level().dimension().location();
            return CandyCraft.MODID.equals(dimension.getNamespace()) && "candy_world".equals(dimension.getPath());
        }

        private static boolean isInLiquidCandy(net.minecraft.world.entity.LivingEntity living) {
            FluidState body = living.level().getFluidState(living.blockPosition());
            FluidState eye = living.level().getFluidState(net.minecraft.core.BlockPos.containing(living.getX(), living.getEyeY(), living.getZ()));
            return body.is(CCFluids.SOURCE_LIQUID_CANDY.get())
                || body.is(CCFluids.FLOWING_LIQUID_CANDY.get())
                || eye.is(CCFluids.SOURCE_LIQUID_CANDY.get())
                || eye.is(CCFluids.FLOWING_LIQUID_CANDY.get());
        }

        private static float candyDayFactor(Level level, float partialTick) {
            float value = (float)Math.cos(level.getTimeOfDay(partialTick) * ((float)Math.PI * 2.0F)) * 2.0F + 0.5F;
            return Math.max(0.0F, Math.min(1.0F, value));
        }
    }
}
