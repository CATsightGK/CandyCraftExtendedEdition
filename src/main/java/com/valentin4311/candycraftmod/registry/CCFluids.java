package com.valentin4311.candycraftmod.registry;

import com.valentin4311.candycraftmod.CandyCraft;
import java.util.function.Consumer;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.joml.Vector3f;

public final class CCFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, CandyCraft.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, CandyCraft.MODID);

    private static final ResourceLocation GRENADINE_STILL = new ResourceLocation(CandyCraft.MODID, "block/grenadine_static");
    private static final ResourceLocation GRENADINE_FLOWING = new ResourceLocation(CandyCraft.MODID, "block/grenadine_flow");
    private static final ResourceLocation CARAMEL_STILL = new ResourceLocation(CandyCraft.MODID, "block/caramel_static");
    private static final ResourceLocation LIQUID_CHOCOLATE_STILL = new ResourceLocation(CandyCraft.MODID, "block/liquid_chocolate_still");
    private static final ResourceLocation LIQUID_CHOCOLATE_FLOWING = new ResourceLocation(CandyCraft.MODID, "block/liquid_chocolate_flow");
    private static final ResourceLocation LIQUID_CANDY_STILL = new ResourceLocation(CandyCraft.MODID, "block/liquid_candy_still");
    private static final ResourceLocation LIQUID_CANDY_FLOWING = new ResourceLocation(CandyCraft.MODID, "block/liquid_candy_flow");
    private static final ResourceLocation VANILLA_UNDERWATER_OVERLAY = new ResourceLocation("minecraft", "textures/misc/underwater.png");
    private static final Vector3f GRENADINE_FOG = color(0xF22929);
    private static final Vector3f CARAMEL_FOG = color(0x914000);
    private static final Vector3f LIQUID_CHOCOLATE_FOG = color(0x482B17);
    private static final Vector3f LIQUID_CANDY_FOG = color(0xE674CA);

    public static final RegistryObject<FluidType> GRENADINE_TYPE = FLUID_TYPES.register("grenadine", () -> new FluidType(
        FluidType.Properties.create()
            .descriptionId("fluid." + CandyCraft.MODID + ".grenadine")
            .canSwim(true)
            .density(1000)
            .viscosity(1000)
            .rarity(Rarity.COMMON)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
    ) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return GRENADINE_STILL;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return GRENADINE_FLOWING;
                }

                @Override
                public int getTintColor() {
                    return 0xB0FFFFFF;
                }

                @Override
                public void renderOverlay(Minecraft minecraft, PoseStack poseStack) {
                    renderVanillaWaterOverlay(minecraft, poseStack);
                }

                @Override
                public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                    return new Vector3f(GRENADINE_FOG);
                }

                @Override
                public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                    renderVanillaWaterFog(renderDistance, shape, 48.0F);
                }

            });
        }
    });

    public static final RegistryObject<ForgeFlowingFluid.Source> SOURCE_GRENADINE = FLUIDS.register("grenadine", () -> new ForgeFlowingFluid.Source(grenadineProperties()));
    public static final RegistryObject<ForgeFlowingFluid.Flowing> FLOWING_GRENADINE = FLUIDS.register("flowing_grenadine", () -> new ForgeFlowingFluid.Flowing(grenadineProperties()));
    public static final RegistryObject<FluidType> CARAMEL_TYPE = FLUID_TYPES.register("caramel", () -> new FluidType(
        FluidType.Properties.create()
            .descriptionId("fluid." + CandyCraft.MODID + ".caramel")
            .canSwim(true)
            .density(1000)
            .viscosity(1000)
            .rarity(Rarity.COMMON)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
    ) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return CARAMEL_STILL;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return CARAMEL_STILL;
                }

                @Override
                public int getTintColor() {
                    return 0xB0FFFFFF;
                }

                @Override
                public void renderOverlay(Minecraft minecraft, PoseStack poseStack) {
                    renderVanillaWaterOverlay(minecraft, poseStack);
                }

                @Override
                public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                    return new Vector3f(CARAMEL_FOG);
                }

                @Override
                public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                    renderVanillaWaterFog(renderDistance, shape, 48.0F);
                }

            });
        }
    });
    public static final RegistryObject<ForgeFlowingFluid.Source> SOURCE_CARAMEL = FLUIDS.register("caramel", () -> new ForgeFlowingFluid.Source(caramelProperties()));
    public static final RegistryObject<ForgeFlowingFluid.Flowing> FLOWING_CARAMEL = FLUIDS.register("flowing_caramel", () -> new ForgeFlowingFluid.Flowing(caramelProperties()));
    public static final RegistryObject<FluidType> LIQUID_CHOCOLATE_TYPE = FLUID_TYPES.register("liquid_chocolate", () -> new FluidType(
        FluidType.Properties.create()
            .descriptionId("fluid." + CandyCraft.MODID + ".liquid_chocolate")
            .canSwim(true)
            .density(1030)
            .viscosity(1000)
            .temperature(315)
            .rarity(Rarity.COMMON)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
    ) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return LIQUID_CHOCOLATE_STILL;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return LIQUID_CHOCOLATE_FLOWING;
                }

                @Override
                public int getTintColor() {
                    return 0xFFFFFFFF;
                }

                @Override
                public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                    return new Vector3f(LIQUID_CHOCOLATE_FOG);
                }

                @Override
                public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                    renderVanillaLavaFog(camera, renderDistance, shape);
                }

            });
        }
    });
    public static final RegistryObject<ForgeFlowingFluid.Source> SOURCE_LIQUID_CHOCOLATE = FLUIDS.register("liquid_chocolate", () -> new ForgeFlowingFluid.Source(liquidChocolateProperties()));
    public static final RegistryObject<ForgeFlowingFluid.Flowing> FLOWING_LIQUID_CHOCOLATE = FLUIDS.register("flowing_liquid_chocolate", () -> new ForgeFlowingFluid.Flowing(liquidChocolateProperties()));
    public static final RegistryObject<FluidType> LIQUID_CANDY_TYPE = FLUID_TYPES.register("liquid_candy", () -> new FluidType(
        FluidType.Properties.create()
            .descriptionId("fluid." + CandyCraft.MODID + ".liquid_candy")
            .canSwim(true)
            .density(2000)
            .viscosity(3000)
            .temperature(1000)
            .lightLevel(12)
            .rarity(Rarity.COMMON)
            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
    ) {
        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                @Override
                public ResourceLocation getStillTexture() {
                    return LIQUID_CANDY_STILL;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return LIQUID_CANDY_FLOWING;
                }

                @Override
                public int getTintColor() {
                    return 0xFFFFFFFF;
                }

                @Override
                public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector3f fluidFogColor) {
                    return new Vector3f(LIQUID_CANDY_FOG);
                }

                @Override
                public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                    renderVanillaLavaFog(camera, renderDistance, shape);
                }

            });
        }
    });
    public static final RegistryObject<ForgeFlowingFluid.Source> SOURCE_LIQUID_CANDY = FLUIDS.register("liquid_candy", () -> new ForgeFlowingFluid.Source(liquidCandyProperties()));
    public static final RegistryObject<ForgeFlowingFluid.Flowing> FLOWING_LIQUID_CANDY = FLUIDS.register("flowing_liquid_candy", () -> new ForgeFlowingFluid.Flowing(liquidCandyProperties()));

    private CCFluids() {
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
        FLUIDS.register(eventBus);
    }

    private static void renderVanillaWaterOverlay(Minecraft minecraft, PoseStack poseStack) {
        ScreenEffectRenderer.renderFluid(minecraft, poseStack, VANILLA_UNDERWATER_OVERLAY);
    }

    private static void renderVanillaWaterFog(float renderDistance, FogShape shape, float maxDistance) {
        RenderSystem.setShaderFogStart(-8.0F);
        RenderSystem.setShaderFogEnd(Math.min(maxDistance, renderDistance));
        RenderSystem.setShaderFogShape(shape);
    }

    private static Vector3f color(int rgb) {
        return new Vector3f(
            ((rgb >> 16) & 0xFF) / 255.0F,
            ((rgb >> 8) & 0xFF) / 255.0F,
            (rgb & 0xFF) / 255.0F
        );
    }

    private static void renderVanillaLavaFog(Camera camera, float renderDistance, FogShape shape) {
        if (camera.getEntity().isSpectator()) {
            RenderSystem.setShaderFogStart(-8.0F);
            RenderSystem.setShaderFogEnd(renderDistance * 0.5F);
        } else if (camera.getEntity() instanceof LivingEntity living && living.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            RenderSystem.setShaderFogStart(0.0F);
            RenderSystem.setShaderFogEnd(3.0F);
        } else {
            RenderSystem.setShaderFogStart(0.25F);
            RenderSystem.setShaderFogEnd(1.0F);
        }
        RenderSystem.setShaderFogShape(shape);
    }

    private static ForgeFlowingFluid.Properties grenadineProperties() {
        return new ForgeFlowingFluid.Properties(
            GRENADINE_TYPE,
            SOURCE_GRENADINE,
            FLOWING_GRENADINE
        )
            .bucket(CCItems.GRENADINE_BUCKET)
            .block(CCBlocks.GRENADINE)
            .slopeFindDistance(4)
            .levelDecreasePerBlock(1)
            .tickRate(5)
            .explosionResistance(100.0F);
    }

    private static ForgeFlowingFluid.Properties caramelProperties() {
        return new ForgeFlowingFluid.Properties(
            CARAMEL_TYPE,
            SOURCE_CARAMEL,
            FLOWING_CARAMEL
        )
            .bucket(CCItems.CARAMEL_BUCKET)
            .block(CCBlocks.CARAMEL)
            .slopeFindDistance(4)
            .levelDecreasePerBlock(1)
            .tickRate(5)
            .explosionResistance(100.0F);
    }

    private static ForgeFlowingFluid.Properties liquidChocolateProperties() {
        return new ForgeFlowingFluid.Properties(
            LIQUID_CHOCOLATE_TYPE,
            SOURCE_LIQUID_CHOCOLATE,
            FLOWING_LIQUID_CHOCOLATE
        )
            .bucket(CCItems.LIQUID_CHOCOLATE_BUCKET)
            .block(CCBlocks.LIQUID_CHOCOLATE)
            .slopeFindDistance(4)
            .levelDecreasePerBlock(1)
            .tickRate(5)
            .explosionResistance(100.0F);
    }

    private static ForgeFlowingFluid.Properties liquidCandyProperties() {
        return new ForgeFlowingFluid.Properties(
            LIQUID_CANDY_TYPE,
            SOURCE_LIQUID_CANDY,
            FLOWING_LIQUID_CANDY
        )
            .bucket(CCItems.LIQUID_CANDY_BUCKET)
            .block(CCBlocks.LIQUID_CANDY)
            .slopeFindDistance(4)
            .levelDecreasePerBlock(1)
            .tickRate(30)
            .explosionResistance(100.0F);
    }
}

