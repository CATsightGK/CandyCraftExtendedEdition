package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.mojang.logging.LogUtils;
import com.valentin4311.candycraftmod.block.PuddingBlock;
import com.valentin4311.candycraftmod.client.particle.ChocolateSplashParticle;
import com.valentin4311.candycraftmod.client.particle.MilkRainDropParticle;
import com.valentin4311.candycraftmod.client.particle.MilkRainSplashParticle;
import com.valentin4311.candycraftmod.client.particle.AlchemySplashParticle;
import com.valentin4311.candycraftmod.client.particle.JellyFragmentParticle;
import com.valentin4311.candycraftmod.client.model.CandyFishModel;
import com.valentin4311.candycraftmod.client.model.BeeModel;
import com.valentin4311.candycraftmod.client.model.BeetleModel;
import com.valentin4311.candycraftmod.client.model.DragonModel;
import com.valentin4311.candycraftmod.client.model.GummyBunnyModel;
import com.valentin4311.candycraftmod.client.model.GummyBunnyOuterModel;
import com.valentin4311.candycraftmod.client.model.GummyBearModel;
import com.valentin4311.candycraftmod.client.model.GummyMouseModel;
import com.valentin4311.candycraftmod.client.model.GummyMouseOuterModel;
import com.valentin4311.candycraftmod.client.model.MermaidModel;
import com.valentin4311.candycraftmod.client.model.NessieModel;
import com.valentin4311.candycraftmod.client.model.NougatGolemModel;
import com.valentin4311.candycraftmod.client.model.PingouinModel;
import com.valentin4311.candycraftmod.client.model.SuguardModel;
import com.valentin4311.candycraftmod.client.model.WaffleSheepModel;
import com.valentin4311.candycraftmod.entity.BasicCandyZombieEntity;
import com.valentin4311.candycraftmod.registry.CCBlocks;
import com.valentin4311.candycraftmod.registry.CCEntityTypes;
import com.valentin4311.candycraftmod.registry.CCFluids;
import com.valentin4311.candycraftmod.registry.CCItems;
import com.valentin4311.candycraftmod.registry.CCMenus;
import com.valentin4311.candycraftmod.registry.CCParticleTypes;
import com.valentin4311.candycraftmod.item.CaramelCrossbowItem;
import com.valentin4311.candycraftmod.item.DynamiteItem;
import com.valentin4311.candycraftmod.item.JellyWandItem;
import com.valentin4311.candycraftmod.item.JumpWandItem;
import com.valentin4311.candycraftmod.item.RawGummyItem;
import com.valentin4311.candycraftmod.item.SugarPillItem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.FlameParticle;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CCClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation SUGAR_FACTORY_GUI = new ResourceLocation(CandyCraft.MODID, "textures/gui/gui_sugar.png");
    private static final ResourceLocation PUDDING_LOADING_TOP = new ResourceLocation(CandyCraft.MODID, "textures/block/pudding_side.png");
    private static final ResourceLocation FLOUR_LOADING_BACKGROUND = new ResourceLocation(CandyCraft.MODID, "textures/block/flour.png");
    private static final ResourceLocation JAWBREAKER_LOADING_BACKGROUND = new ResourceLocation(CandyCraft.MODID, "textures/block/jaw_breaker_block.png");
    private static final ResourceLocation JAWBREAKER_RUNE_BACKGROUND = new ResourceLocation(CandyCraft.MODID, "textures/block/jaw_breaker_light.png");
    private static final ResourceLocation VANILLA_PORTAL_OVERLAY = new ResourceLocation("textures/misc/nausea.png");
    private static final ResourceLocation CANDY_WORLD_EFFECTS = new ResourceLocation(CandyCraft.MODID, "candy_world_effects");
    private static final ResourceLocation DUNGEON_EFFECTS = new ResourceLocation(CandyCraft.MODID, "candy_dungeon_effects");
    private static final float CANDY_WORLD_MIN_DAY_FACTOR = 0.3957580F;
    private static int portalOverlayTicks;
    private static boolean candyWorldLoadingActive;
    private static int candyWorldLoadingTimeoutTicks;
    private static int candyWorldLoadingGraceTicks;
    private static boolean candyWorldLoadingExit;
    private static boolean loadingBackgroundDrawnThisFrame;

    private CCClient() {
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener(EmblemBasketClientEvents::onScreenInit);
        MinecraftForge.EVENT_BUS.addListener(CandyCraftUpdateChecker::onClientTick);
        event.enqueueWork(() -> {
            com.valentin4311.candycraftmod.registry.CCWoodTypes.values().forEach(net.minecraft.client.renderer.Sheets::addWoodType);
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
            MenuScreens.register(CCMenus.EMBLEM_BASKET.get(), EmblemBasketScreen::new);
            MenuScreens.register(CCMenus.CANDY_WORKBENCH.get(), CandyWorkbenchScreen::new);
            MenuScreens.register(CCMenus.MARSHMALLOW_CHEST.get(), MarshmallowChestScreen::new);
            registerProjectileItemProperties();
        });
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(CCParticleTypes.CHOCOLATE_SPLASH.get(), ChocolateSplashParticle.Provider::new);
        event.registerSpriteSet(CCParticleTypes.MILK_RAIN_DROP.get(), MilkRainDropParticle.Provider::new);
        event.registerSpriteSet(CCParticleTypes.MILK_RAIN_SPLASH.get(), MilkRainSplashParticle.Provider::new);
        event.registerSpriteSet(CCParticleTypes.ALCHEMY_SPLASH.get(), AlchemySplashParticle.Provider::new);
        event.registerSpriteSet(CCParticleTypes.STRAWBERRY_JELLY_FRAGMENT.get(),
            sprites -> new JellyFragmentParticle.Provider(sprites, 1.0F, 0.47F, 0.63F));
        event.registerSpriteSet(CCParticleTypes.CARAMEL_JELLY_FRAGMENT.get(),
            sprites -> new JellyFragmentParticle.Provider(sprites, 0.93F, 0.43F, 0.08F));
        event.registerSpriteSet(CCParticleTypes.ROYAL_RATIONS_FRAGMENT.get(),
            sprites -> new JellyFragmentParticle.Provider(sprites, 0.72F, 0.77F, 0.84F));
        event.registerSpriteSet(CCParticleTypes.LEMON_JELLY_FRAGMENT.get(),
            sprites -> new JellyFragmentParticle.Provider(sprites, 0.85F, 0.86F, 0.40F));
        event.registerSpriteSet(CCParticleTypes.RASPBERRY_JELLY_FRAGMENT.get(),
            sprites -> new JellyFragmentParticle.Provider(sprites, 0.92F, 0.37F, 0.30F));
        event.registerSpriteSet(CCParticleTypes.MINT_JELLY_FRAGMENT.get(),
            sprites -> new JellyFragmentParticle.Provider(sprites, 0.54F, 0.90F, 0.80F));
        event.registerSpriteSet(CCParticleTypes.LIQUID_CANDY_FLAME.get(), FlameParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(
            com.valentin4311.candycraftmod.inventory.tooltip.ForkHeldBlockTooltip.class,
            com.valentin4311.candycraftmod.client.tooltip.ForkHeldBlockClientTooltip::new
        );
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(AlchemyTableRenderer.MIX_MODEL);
        event.register(AlchemyTableRenderer.STRIPS_MODEL);
        event.register(ForkItemRenderer.INVENTORY_MODEL);
    }

    @SubscribeEvent
    public static void registerDimensionSpecialEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(CANDY_WORLD_EFFECTS, new CandyWorldEffects());
        event.register(DUNGEON_EFFECTS, new DungeonEffects());
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("jelly_wand_charge", CCClient::renderJellyWandChargeOverlay);
        event.registerAboveAll("candy_portal_view", CCClient::renderCandyPortalOverlay);
    }

    private static void renderCandyPortalOverlay(net.minecraftforge.client.gui.overlay.ForgeGui gui, GuiGraphics graphics,
            float partialTick, int screenWidth, int screenHeight) {
        if (portalOverlayTicks <= 0) {
            return;
        }
        float progress = Mth.clamp((portalOverlayTicks + partialTick) / 80.0F, 0.0F, 1.0F);
        if (progress < 1.0F) {
            progress *= progress;
            progress = progress * 0.8F + 0.2F;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, Mth.clamp(progress, 0.0F, 1.0F));
        graphics.blit(VANILLA_PORTAL_OVERLAY, 0, 0, -90, 0.0F, 0.0F, screenWidth, screenHeight, screenWidth, screenHeight);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }

    private static void renderJellyWandChargeOverlay(net.minecraftforge.client.gui.overlay.ForgeGui gui, GuiGraphics graphics,
            float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = gui.getMinecraft();
        if (minecraft.player == null || minecraft.options.hideGui || minecraft.player.isSpectator()) {
            return;
        }
        net.minecraft.world.item.ItemStack stack = activeChargeStack(minecraft.player);
        if (stack.isEmpty()) {
            return;
        }

        boolean jellyWand = stack.is(CCItems.JELLY_WAND.get());
        float tapProgress = jellyWand ? JellyWandItem.getTapChargeProgress(stack) : 0.0F;
        float aimProgress = jellyWand ? JellyWandItem.getAimProgress(minecraft.player) : JumpWandItem.getChargeProgress(minecraft.player);
        if (tapProgress <= 0.0F && aimProgress <= 0.0F) {
            return;
        }

        int width = 64;
        int height = 8;
        int x = screenWidth / 2 - width / 2;
        int y = screenHeight - 36;
        float progress = Math.max(tapProgress, aimProgress);
        int fillWidth = Math.round((width - 4) * progress);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (progress > 0.0F) {
            graphics.fill(x, y, x + width, y + height, 0xAA211323);
            graphics.blit(SUGAR_FACTORY_GUI, x + 2, y + 1, 0, 114, width - 4, height - 2, 256, 256);
            int fillColor;
            if (jellyWand && aimProgress >= 0.5F) {
                fillColor = snipeChargeColor(progress, minecraft.player.tickCount) | 0xFF000000;
            } else if (jellyWand) {
                fillColor = jellyScatterChargeColor(progress) | 0xFF000000;
            } else {
                fillColor = Mth.hsvToRgb(0.58F - progress * 0.08F, 0.7F, 1.0F) | 0xFF000000;
            }
            graphics.fill(x + 2, y + 2, x + 2 + fillWidth, y + height - 2, fillColor);
            graphics.fill(x + 2, y + 2, x + 2 + fillWidth, y + 3, 0x88FFFFFF);
            graphics.fill(x, y, x + width, y + 1, 0xCCF4C2DA);
            graphics.fill(x, y + height - 1, x + width, y + height, 0xCC6D3456);
            graphics.fill(x, y, x + 1, y + height, 0xCCF4C2DA);
            graphics.fill(x + width - 1, y, x + width, y + height, 0xCC6D3456);
        }
        RenderSystem.disableBlend();

        if (jellyWand && aimProgress >= 0.5F) {
            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2;
            int color = Mth.hsvToRgb((minecraft.player.tickCount % 40) / 40.0F, 0.8F, 1.0F) | 0xFF000000;
            graphics.fill(centerX - 7, centerY, centerX - 2, centerY + 1, color);
            graphics.fill(centerX + 3, centerY, centerX + 8, centerY + 1, color);
            graphics.fill(centerX, centerY - 7, centerX + 1, centerY - 2, color);
            graphics.fill(centerX, centerY + 3, centerX + 1, centerY + 8, color);
        }
    }

    private static int jellyScatterChargeColor(float progress) {
        int[] palette = {0xFFE75A, 0xE94242, 0x78E0B5, 0x7EC8F0, 0xB87532, 0xFF77A8};
        return paletteColor(palette, progress);
    }

    private static int snipeChargeColor(float progress, int tick) {
        int[] palette = {0xF8E071, 0xF47A45, 0xE94242, 0xFF77A8, 0x7EC8F0, 0xF8E071};
        float shimmer = (Mth.sin(tick * 0.22F) + 1.0F) * 0.04F;
        return paletteColor(palette, Mth.clamp(progress + shimmer, 0.0F, 1.0F));
    }

    private static int paletteColor(int[] palette, float progress) {
        if (palette.length == 0) {
            return 0xFFFFFF;
        }
        float scaled = Mth.clamp(progress, 0.0F, 1.0F) * (palette.length - 1);
        int index = Mth.clamp((int)scaled, 0, palette.length - 1);
        int next = Math.min(index + 1, palette.length - 1);
        return CandyColors.lerpColor(palette[index], palette[next], scaled - index);
    }

    private static net.minecraft.world.item.ItemStack activeChargeStack(net.minecraft.world.entity.player.Player player) {
        net.minecraft.world.item.ItemStack using = player.getUseItem();
        if (player.isUsingItem() && (using.is(CCItems.JELLY_WAND.get()) || using.is(CCItems.JUMP_WAND.get()))) {
            return using;
        }
        net.minecraft.world.item.ItemStack main = player.getMainHandItem();
        if (main.is(CCItems.JELLY_WAND.get()) && JellyWandItem.isChargingTap(main)) {
            return main;
        }
        if (main.is(CCItems.JUMP_WAND.get())) {
            return main;
        }
        net.minecraft.world.item.ItemStack offhand = player.getOffhandItem();
        if (offhand.is(CCItems.JELLY_WAND.get()) && JellyWandItem.isChargingTap(offhand)) {
            return offhand;
        }
        if (offhand.is(CCItems.JUMP_WAND.get())) {
            return offhand;
        }
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    private static void registerProjectileItemProperties() {
        ItemProperties.register(CCItems.FORK.get(), new ResourceLocation(CandyCraft.MODID, "eating"),
            (stack, level, entity, seed) -> com.valentin4311.candycraftmod.item.ForkItem.getEatAnimationTicks(stack) > 0
                ? 1.0F : 0.0F
        );
        ItemProperties.register(CCItems.FORK.get(), new ResourceLocation(CandyCraft.MODID, "throwing"),
            (stack, level, entity, seed) -> entity != null
                && entity.isUsingItem()
                && entity.getUseItem() == stack ? 1.0F : 0.0F
        );
        ItemProperties.register(CCItems.DYNAMITE.get(), new ResourceLocation("stage"), (stack, level, entity, seed) ->
            entity != null ? DynamiteItem.modelStage(stack, entity) : 0.0F
        );
        ItemProperties.register(CCItems.GLUE_DYNAMITE.get(), new ResourceLocation("stage"), (stack, level, entity, seed) ->
            entity != null ? DynamiteItem.modelStage(stack, entity) : 0.0F
        );
        ItemProperties.register(CCItems.CARAMEL_BOW.get(), new ResourceLocation("pull"), (stack, level, entity, seed) -> {
            if (entity instanceof BasicCandyZombieEntity suguard
                    && entity.getType() == CCEntityTypes.BOSS_SUGUARD.get()
                    && entity.getMainHandItem() == stack) {
                return suguard.getBossBowDrawProgress(0.0F);
            }
            if (entity == null || entity.getUseItem() != stack) {
                return 0.0F;
            }
            return (float)(stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 10.0F;
        });
        ItemProperties.register(CCItems.CARAMEL_BOW.get(), new ResourceLocation("pulling"), (stack, level, entity, seed) -> {
            if (entity instanceof BasicCandyZombieEntity suguard
                    && entity.getType() == CCEntityTypes.BOSS_SUGUARD.get()
                    && entity.getMainHandItem() == stack) {
                return suguard.getBossBowDrawTicks() > 0 ? 1.0F : 0.0F;
            }
            return entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F;
        });
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
        event.register((state, level, pos, tintIndex) -> tintIndex != 0 || level == null || pos == null
                ? PuddingBlock.DEFAULT_COLOR
                : puddingColor(level, pos), CCBlocks.PUDDING.get());
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex < 0) {
                return -1;
            }
            return level != null && pos != null ? enchantedLeavesColor(level, pos) : 0x8f8ac8;
        }, CCBlocks.CANDY_LEAVES_ENCHANT.get());
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
            return CandyColors.CANDY_WORLD_FOG_FALLBACK;
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
            return CandyColors.CANDY_WORLD_FOG_FALLBACK;
        }
        return ((red / samples) & 255) << 16 | ((green / samples) & 255) << 8 | (blue / samples) & 255;
    }

    private static int legacyCandySkyColor(Level level, net.minecraft.core.BlockPos pos, float partialTick) {
        if (!level.hasChunkAt(pos)) {
            return CandyColors.CANDY_WORLD_SKY_FALLBACK;
        }
        return level.getBiome(pos).value().getSkyColor();
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
            case "sugar_plains", "hard_candy_plains", "sugar_forest" -> 0xEEAABB;
            case "sugar_mountains" -> 0xEEBBCC;
            case "sugar_cold_forest" -> 0xFFDDEE;
            case "ice_cream_plains", "ice_cream_sky_mountains", "sugar_hell_mountains" -> 0xFFFFFF;
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
        // Using custom noise as a replacement for deprecated Biome.BIOME_INFO_NOISE
        double noise = smoothNoise2D(x * 0.0225D, z * 0.0225D, 0x53494E4B5F435259L);
        if (noise < -0.5D) {
            double blend = smoothstep(-0.85D, -0.5D, noise);
            return CandyColors.lerpColor(0xB0ECFF, 0xB0D8FF, blend);
        }
        if (noise < -0.1D) {
            double blend = smoothstep(-0.5D, -0.1D, noise);
            return CandyColors.lerpColor(0xB0D8FF, 0xB0B0FF, blend);
        }
        double blend = smoothstep(-0.1D, 0.45D, noise);
        return CandyColors.lerpColor(0xB0B0FF, 0xA376DA, blend);
    }

    private static double smoothNoise2D(double x, double z, long salt) {
        int x0 = (int)Math.floor(x);
        int z0 = (int)Math.floor(z);
        double tx = fade(x - x0);
        double tz = fade(z - z0);
        double a = randomUnit2D(x0, 0, z0, salt);
        double b = randomUnit2D(x0 + 1, 0, z0, salt);
        double c = randomUnit2D(x0, 0, z0 + 1, salt);
        double d = randomUnit2D(x0 + 1, 0, z0 + 1, salt);
        return Mth.lerp(tz, Mth.lerp(tx, a, b), Mth.lerp(tx, c, d));
    }

    private static double fade(double value) {
        return value * value * value * (value * (value * 6.0D - 15.0D) + 10.0D);
    }

    private static double randomUnit2D(int x, int y, int z, long salt) {
        long bits = hash2D(x, y, z, salt);
        return ((bits >>> 11) * 0x1.0p-53D) * 2.0D - 1.0D;
    }

    private static long hash2D(int x, int y, int z, long salt) {
        long h = salt;
        h ^= x * 0x9E3779B97F4A7C15L;
        h = Long.rotateLeft(h, 27) * 0x94D049BB133111EBL;
        h ^= y * 0xC2B2AE3D27D4EB4FL;
        h ^= h >>> 33;
        h *= 0xff51afd7ed558ccdL;
        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= h >>> 33;
        return h;
    }

    private static double smoothstep(double edge0, double edge1, double value) {
        double t = (value - edge0) / (edge1 - edge0);
        t = Math.max(0.0D, Math.min(1.0D, t));
        return t * t * (3.0D - 2.0D * t);
    }


    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        ItemColors colors = event.getItemColors();
        colors.register((stack, tintIndex) -> tintIndex >= 0 ? PuddingBlock.DEFAULT_COLOR : -1, CCBlocks.PUDDING.get());
        colors.register(SugarPillItem::getLayerColor, CCItems.SUGAR_PILL.get());
        colors.register((stack, tintIndex) -> 0xff4530,
            CCBlocks.RED_GUMMY_BLOCK.get(),
            CCBlocks.RED_HARDENED_GUMMY_BLOCK.get(),
            CCBlocks.RED_GUMMY_WORKBENCH.get(),
            CCBlocks.RED_GUMMY_WORM_BLOCK.get()
        );
        colors.register((stack, tintIndex) -> 0xff9b4f,
            CCBlocks.ORANGE_GUMMY_BLOCK.get(),
            CCBlocks.ORANGE_HARDENED_GUMMY_BLOCK.get(),
            CCBlocks.ORANGE_GUMMY_WORKBENCH.get(),
            CCBlocks.ORANGE_GUMMY_WORM_BLOCK.get()
        );
        colors.register((stack, tintIndex) -> 0xffe563,
            CCBlocks.YELLOW_GUMMY_BLOCK.get(),
            CCBlocks.YELLOW_HARDENED_GUMMY_BLOCK.get(),
            CCBlocks.YELLOW_GUMMY_WORKBENCH.get(),
            CCBlocks.YELLOW_GUMMY_WORM_BLOCK.get()
        );
        colors.register((stack, tintIndex) -> 0xfffeb0,
            CCBlocks.WHITE_GUMMY_BLOCK.get(),
            CCBlocks.WHITE_HARDENED_GUMMY_BLOCK.get(),
            CCBlocks.WHITE_GUMMY_WORKBENCH.get(),
            CCBlocks.WHITE_GUMMY_WORM_BLOCK.get()
        );
        colors.register((stack, tintIndex) -> 0x80e22b,
            CCBlocks.GREEN_GUMMY_BLOCK.get(),
            CCBlocks.GREEN_HARDENED_GUMMY_BLOCK.get(),
            CCBlocks.GREEN_GUMMY_WORKBENCH.get(),
            CCBlocks.GREEN_GUMMY_WORM_BLOCK.get()
        );
    }

    private static void registerGummyBlockColors(BlockColors colors) {
        colors.register((state, level, pos, tintIndex) -> 0xff4530,
            CCBlocks.RED_GUMMY_BLOCK.get(),
            CCBlocks.RED_HARDENED_GUMMY_BLOCK.get(),
            CCBlocks.RED_GUMMY_WORKBENCH.get(),
            CCBlocks.RED_GUMMY_WORM_BLOCK.get()
        );
        colors.register((state, level, pos, tintIndex) -> 0xff9b4f,
            CCBlocks.ORANGE_GUMMY_BLOCK.get(),
            CCBlocks.ORANGE_HARDENED_GUMMY_BLOCK.get(),
            CCBlocks.ORANGE_GUMMY_WORKBENCH.get(),
            CCBlocks.ORANGE_GUMMY_WORM_BLOCK.get()
        );
        colors.register((state, level, pos, tintIndex) -> 0xffe563,
            CCBlocks.YELLOW_GUMMY_BLOCK.get(),
            CCBlocks.YELLOW_HARDENED_GUMMY_BLOCK.get(),
            CCBlocks.YELLOW_GUMMY_WORKBENCH.get(),
            CCBlocks.YELLOW_GUMMY_WORM_BLOCK.get()
        );
        colors.register((state, level, pos, tintIndex) -> 0xfffeb0,
            CCBlocks.WHITE_GUMMY_BLOCK.get(),
            CCBlocks.WHITE_HARDENED_GUMMY_BLOCK.get(),
            CCBlocks.WHITE_GUMMY_WORKBENCH.get(),
            CCBlocks.WHITE_GUMMY_WORM_BLOCK.get()
        );
        colors.register((state, level, pos, tintIndex) -> 0x80e22b,
            CCBlocks.GREEN_GUMMY_BLOCK.get(),
            CCBlocks.GREEN_HARDENED_GUMMY_BLOCK.get(),
            CCBlocks.GREEN_GUMMY_WORKBENCH.get(),
            CCBlocks.GREEN_GUMMY_WORM_BLOCK.get()
        );
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(com.valentin4311.candycraftmod.registry.CCBlockEntities.ALCHEMY_TABLE.get(), AlchemyTableRenderer::new);
        event.registerBlockEntityRenderer(com.valentin4311.candycraftmod.registry.CCBlockEntities.MARSHMALLOW_CHEST.get(), MarshmallowChestRenderer::new);
        event.registerBlockEntityRenderer(com.valentin4311.candycraftmod.registry.CCBlockEntities.CANDY_SIGN.get(), net.minecraft.client.renderer.blockentity.SignRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.HONEY_ARROW.get(), HoneyArrowRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.HONEY_BOLT.get(), HoneyBoltRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.DYNAMITE.get(), context -> new FixedThrownItemRenderer<>(context, new net.minecraft.world.item.ItemStack(CCItems.DYNAMITE.get()), 0.5F));
        event.registerEntityRenderer(CCEntityTypes.GLUE_DYNAMITE.get(), context -> new FixedThrownItemRenderer<>(context, new net.minecraft.world.item.ItemStack(CCItems.GLUE_DYNAMITE.get()), 0.5F));
        event.registerEntityRenderer(CCEntityTypes.THROWN_FORK.get(), ThrownForkRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.THROWN_FORK_BLOCK.get(), ThrownForkBlockRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.GUMMY_BALL.get(), GummyBallRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.CANDY_PIG.get(), CandyPigRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.WAFFLE_SHEEP.get(), WaffleSheepRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.CANDY_CREEPER.get(), CandyCreeperRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.COTTON_CANDY_SPIDER.get(), CottonCandySpiderRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.SUGUARD.get(), SuguardRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.MAGE_SUGUARD.get(), SuguardRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.CANDY_WOLF.get(), CandyWolfRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.GUMMY_BUNNY.get(), GummyBunnyRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.COTTON_CANDY_SHEEP.get(), CottonCandySheepRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.EASTER_CHICKEN.get(), EasterChickenRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.GUMMY_MOUSE.get(), GummyMouseRenderer::new);
        event.registerEntityRenderer(CCEntityTypes.GUMMY_BEAR.get(), GummyBearRenderer::new);
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
    public static void addLivingRenderLayers(EntityRenderersEvent.AddLayers event) {
        int attached = 0;
        for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES.getValues()) {
            if (addPropolisLayer(event, entityType)) {
                attached++;
            }
        }
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            renderer.addLayer(new CandyProjectileStuckLayer(event.getContext().getEntityRenderDispatcher(), renderer));
            renderer.addLayer(new PropolisEntityOverlayLayer<>(renderer));
            attached++;
        }
        LOGGER.info("Attached CandyCraft propolis overlays to {} living entity renderers", attached);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static boolean addPropolisLayer(EntityRenderersEvent.AddLayers event, EntityType<?> entityType) {
        try {
            LivingEntityRenderer renderer = event.getRenderer((EntityType)entityType);
            if (renderer != null) {
                if (renderer instanceof SlimeRenderer slimeRenderer) {
                    slimeRenderer.addLayer(new PropolisSlimeGlintLayer(
                        slimeRenderer, event.getContext().getModelSet()));
                } else {
                    renderer.addLayer(new PropolisEntityOverlayLayer<>(renderer));
                }
                return true;
            }
        } catch (ClassCastException ignored) {
            // The registry includes projectiles and other entity types without living renderers.
        }
        return false;
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CandyFishModel.LAYER, CandyFishModel::createBodyLayer);
        event.registerLayerDefinition(PingouinModel.LAYER, PingouinModel::createBodyLayer);
        event.registerLayerDefinition(GummyBunnyModel.LAYER, GummyBunnyModel::createBodyLayer);
        event.registerLayerDefinition(GummyBunnyOuterModel.LAYER, GummyBunnyOuterModel::createBodyLayer);
        event.registerLayerDefinition(GummyMouseModel.LAYER, GummyMouseModel::createBodyLayer);
        event.registerLayerDefinition(GummyMouseOuterModel.LAYER, GummyMouseOuterModel::createBodyLayer);
        event.registerLayerDefinition(GummyBearModel.LAYER, GummyBearModel::createBodyLayer);
        event.registerLayerDefinition(GingerbreadManModel.LAYER, GingerbreadManModel::createBodyLayer);
        event.registerLayerDefinition(SuguardModel.LAYER, SuguardModel::createBodyLayer);
        event.registerLayerDefinition(WaffleSheepModel.LAYER, WaffleSheepModel::createBodyLayer);
        event.registerLayerDefinition(WaffleSheepModel.FUR_LAYER, WaffleSheepModel::createFurLayer);
        event.registerLayerDefinition(BeeModel.LAYER, BeeModel::createBodyLayer);
        event.registerLayerDefinition(BeetleModel.LAYER, BeetleModel::createBodyLayer);
        event.registerLayerDefinition(NessieModel.LAYER, NessieModel::createBodyLayer);
        event.registerLayerDefinition(DragonModel.LAYER, DragonModel::createBodyLayer);
        event.registerLayerDefinition(MermaidModel.LAYER, MermaidModel::createBodyLayer);
        event.registerLayerDefinition(NougatGolemModel.LAYER, NougatGolemModel::createBodyLayer);
    }

    @Mod.EventBusSubscriber(modid = CandyCraft.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class ForgeEvents {
        private static final int FLUID_NONE = 0;
        private static final int FLUID_CHOCOLATE = 1;
        private static final int FLUID_CANDY = 2;
        private static final int FLUID_GRENADINE = 3;
        private static final int FLUID_CARAMEL = 4;
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
                setFogColor(event, 0x482B17);
            } else if (view == FLUID_CANDY) {
                setFogColor(event, 0xE674CA);
            } else if (view == FLUID_GRENADINE) {
                setFogColor(event, 0xF22929);
            } else if (view == FLUID_CARAMEL) {
                setFogColor(event, 0x914000);
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
                portalOverlayTicks = 0;
                clearCandyWorldLoadingState(false);
                return;
            }
            tickCandyWorldLoadingScreen(minecraft);
            updateCandyPortalOverlay(minecraft);
        }

        @SubscribeEvent
        public static void renderLoadingBackground(ScreenEvent.BackgroundRendered event) {
            Screen screen = event.getScreen();
            if (!(screen instanceof ReceivingLevelScreen || screen instanceof LevelLoadingScreen || screen instanceof GenericDirtMessageScreen)) {
                return;
            }

            loadingBackgroundDrawnThisFrame = true;
            renderCustomLoadingBackground(event.getGuiGraphics(), screen);
        }

        @SubscribeEvent
        public static void beforeLoadingScreenRender(ScreenEvent.Render.Pre event) {
            Screen screen = event.getScreen();
            if (!isLevelLoadingScreen(screen)) {
                return;
            }
            loadingBackgroundDrawnThisFrame = false;
            if (isDungeonLoadingContext()) {
                loadingBackgroundDrawnThisFrame = true;
                renderDungeonLoadingFrame(event.getGuiGraphics());
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void afterLoadingScreenRender(ScreenEvent.Render.Post event) {
            if (!loadingBackgroundDrawnThisFrame
                    && isLevelLoadingScreen(event.getScreen())
                    && !isDungeonLoadingContext()) {
                renderCustomLoadingBackground(event.getGuiGraphics(), event.getScreen());
            }
        }

        private static boolean isDungeonLoadingContext() {
            Minecraft minecraft = Minecraft.getInstance();
            return isDungeonLevel(minecraft.level);
        }

        private static void renderDungeonLoadingFrame(GuiGraphics graphics) {
            Minecraft minecraft = Minecraft.getInstance();
            int width = minecraft.getWindow().getGuiScaledWidth();
            int height = minecraft.getWindow().getGuiScaledHeight();
            renderDungeonLoadingBackground(graphics, width, height);
            boolean suguard = minecraft.level != null
                && "suguard_dungeon".equals(minecraft.level.dimension().location().getPath());
            net.minecraft.world.item.ItemStack key = new net.minecraft.world.item.ItemStack(
                suguard ? CCItems.SUGUARD_KEY.get() : CCItems.JELLY_KEY.get()
            );
            // Loading screens can pause game ticks, so drive the curve from wall-clock time.
            double timeSeconds = System.nanoTime() / 1_000_000_000.0D;
            float bob = (float)Math.sin(timeSeconds * 2.2D) * 4.0F;
            graphics.pose().pushPose();
            graphics.pose().translate(width * 0.5F, height * 0.5F + bob, 0.0F);
            graphics.pose().scale(2.0F, 2.0F, 1.0F);
            graphics.renderItem(key, -8, -8);
            graphics.pose().popPose();
        }

        private static void renderCustomLoadingBackground(GuiGraphics graphics, Screen screen) {
            Minecraft minecraft = Minecraft.getInstance();
            boolean dungeon = isDungeonLevel(minecraft.level);
            int width = minecraft.getWindow().getGuiScaledWidth();
            int height = minecraft.getWindow().getGuiScaledHeight();
            if (dungeon) {
                renderDungeonLoadingBackground(graphics, width, height);
            } else if (candyWorldLoadingActive || isCandyWorldLevel(minecraft.level)) {
                renderCandyWorldLoadingBackground(graphics, width, height);
            }
        }

        private static void beginCandyWorldLoadingScreen() {
            Minecraft minecraft = Minecraft.getInstance();
            boolean inCandyWorld = isCandyWorldLevel(minecraft.level);
            if (!candyWorldLoadingActive) {
                candyWorldLoadingExit = inCandyWorld;
            } else {
                candyWorldLoadingExit |= inCandyWorld;
            }
            candyWorldLoadingActive = true;
            candyWorldLoadingTimeoutTicks = 20 * 30;
            candyWorldLoadingGraceTicks = 20 * 3;
        }

        private static void tickCandyWorldLoadingScreen(Minecraft minecraft) {
            if (!candyWorldLoadingActive) {
                return;
            }
            if (minecraft.screen instanceof ReceivingLevelScreen || minecraft.screen instanceof LevelLoadingScreen) {
                if (--candyWorldLoadingTimeoutTicks <= 0) {
                    clearCandyWorldLoadingState(false);
                }
                return;
            }

            if (portalOverlayTicks > 0) {
                candyWorldLoadingGraceTicks = 20 * 3;
                return;
            }

            if (--candyWorldLoadingGraceTicks <= 0 || --candyWorldLoadingTimeoutTicks <= 0) {
                clearCandyWorldLoadingState(true);
            }
        }

        private static void clearCandyWorldLoadingState(boolean closeLegacyScreen) {
            candyWorldLoadingActive = false;
            candyWorldLoadingTimeoutTicks = 0;
            candyWorldLoadingGraceTicks = 0;
            candyWorldLoadingExit = false;
            if (closeLegacyScreen && Minecraft.getInstance().screen instanceof GenericDirtMessageScreen) {
                Minecraft.getInstance().setScreen(null);
            }
        }

        private static boolean isLevelLoadingScreen(Screen screen) {
            return screen instanceof ReceivingLevelScreen || screen instanceof LevelLoadingScreen || screen instanceof GenericDirtMessageScreen;
        }

        private static void updateCandyPortalOverlay(Minecraft minecraft) {
            if (minecraft.level == null || minecraft.player.isSpectator()) {
                portalOverlayTicks = 0;
                return;
            }
            net.minecraft.core.BlockPos eyePos = net.minecraft.core.BlockPos.containing(minecraft.player.getEyePosition());
            net.minecraft.world.level.block.state.BlockState state = minecraft.level.getBlockState(eyePos);
            if (state.is(CCBlocks.CANDY_PORTAL.get()) || state.is(CCBlocks.LIQUID_CANDY_PORTAL.get())) {
                portalOverlayTicks = Math.min(80, portalOverlayTicks + 1);
                beginCandyWorldLoadingScreen();
            } else {
                portalOverlayTicks = Math.max(0, portalOverlayTicks - 4);
            }
        }

        private static void renderDungeonLoadingBackground(GuiGraphics graphics, int width, int height) {
            renderTiledBackground(graphics, JAWBREAKER_LOADING_BACKGROUND, width, height, 32, 0xFFFFFFFF);
            int tile = 32;
            for (int x = 0; x < width + tile; x += tile * 4) {
                for (int y = ((x / tile) % 2) * tile * 2; y < height + tile; y += tile * 4) {
                    graphics.blit(JAWBREAKER_RUNE_BACKGROUND, x, y, 0, 0, tile, tile, tile, tile);
                }
            }
        }

        private static void renderCandyWorldLoadingBackground(GuiGraphics graphics, int width, int height) {
            int tile = 32;
            renderTiledBackground(graphics, FLOUR_LOADING_BACKGROUND, width, height, tile, 0xFFFFFFFF);
            for (int x = 0; x < width + tile; x += tile) {
                graphics.blit(PUDDING_LOADING_TOP, x, 0, 0, 0, tile, tile, tile, tile);
            }
        }

        private static void renderTiledBackground(GuiGraphics graphics, ResourceLocation texture, int width, int height, int tile, int color) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            for (int x = 0; x < width + tile; x += tile) {
                for (int y = 0; y < height + tile; y += tile) {
                    graphics.blit(texture, x, y, 0, 0, tile, tile, tile, tile);
                }
            }
            if ((color >>> 24) < 255) {
                graphics.fill(0, 0, width, height, color);
            }
            RenderSystem.disableBlend();
        }

        @SubscribeEvent
        public static void renderFog(ViewportEvent.RenderFog event) {
            int view = fluidView(event.getCamera());
            if (view == FLUID_CHOCOLATE || view == FLUID_CANDY) {
                event.setNearPlaneDistance(0.25F);
                event.setFarPlaneDistance(1.0F);
                event.setCanceled(true);
                return;
            }
            if (view == FLUID_GRENADINE || view == FLUID_CARAMEL) {
                event.setNearPlaneDistance(-8.0F);
                event.setFarPlaneDistance(Math.min(event.getFarPlaneDistance(), 48.0F));
                event.setCanceled(true);
                return;
            }
        }

        private static int fluidView(net.minecraft.client.Camera camera) {
            Level level = camera.getEntity().level();
            net.minecraft.core.BlockPos pos = camera.getBlockPosition();
            FluidState state = level.getFluidState(pos);
            if (!state.isEmpty() && camera.getPosition().y >= (double)(pos.getY() + state.getHeight(level, pos))) {
                // Eye is above the fluid surface within the same block cell:
                // vanilla water applies no fluid fog here, so neither do we.
                return FLUID_NONE;
            }
            if (state.is(CCFluids.SOURCE_LIQUID_CHOCOLATE.get()) || state.is(CCFluids.FLOWING_LIQUID_CHOCOLATE.get())) {
                return FLUID_CHOCOLATE;
            }
            if (state.is(CCFluids.SOURCE_LIQUID_CANDY.get()) || state.is(CCFluids.FLOWING_LIQUID_CANDY.get())) {
                return FLUID_CANDY;
            }
            if (state.is(CCFluids.SOURCE_GRENADINE.get()) || state.is(CCFluids.FLOWING_GRENADINE.get())) {
                return FLUID_GRENADINE;
            }
            if (state.is(CCFluids.SOURCE_CARAMEL.get()) || state.is(CCFluids.FLOWING_CARAMEL.get())) {
                return FLUID_CARAMEL;
            }
            return FLUID_NONE;
        }

        private static void setFogColor(ViewportEvent.ComputeFogColor event, int rgb) {
            event.setRed(((rgb >> 16) & 255) / 255.0F);
            event.setGreen(((rgb >> 8) & 255) / 255.0F);
            event.setBlue((rgb & 255) / 255.0F);
        }

        private static boolean isCandyWorld(net.minecraft.client.Camera camera) {
            ResourceLocation dimension = camera.getEntity().level().dimension().location();
            return CandyCraft.MODID.equals(dimension.getNamespace()) && "candy_world".equals(dimension.getPath());
        }

        private static boolean isDungeonLevel(Level level) {
            if (level == null) {
                return false;
            }
            ResourceLocation dimension = level.dimension().location();
            return CandyCraft.MODID.equals(dimension.getNamespace())
                && ("jelly_dungeon".equals(dimension.getPath()) || "suguard_dungeon".equals(dimension.getPath()));
        }

        private static boolean isCandyWorldLevel(Level level) {
            if (level == null) {
                return false;
            }
            ResourceLocation dimension = level.dimension().location();
            return CandyCraft.MODID.equals(dimension.getNamespace()) && "candy_world".equals(dimension.getPath());
        }

        private static float candyDayFactor(Level level, float partialTick) {
            float value = (float)Math.cos(level.getTimeOfDay(partialTick) * ((float)Math.PI * 2.0F)) * 2.0F + 0.5F;
            return Math.max(CANDY_WORLD_MIN_DAY_FACTOR, Math.min(1.0F, value));
        }
    }
}
