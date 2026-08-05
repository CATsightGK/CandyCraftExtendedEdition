package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCParticleTypes;
import com.valentin4311.candycraftmod.world.CandyPrecipitation;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class MilkRainRenderer {
    private static final ResourceLocation MILK_RAIN = new ResourceLocation(CandyCraft.MODID, "textures/environment/milk_rain.png");
    private static final ResourceLocation SNOW = new ResourceLocation("textures/environment/snow.png");
    private static final float[] RAIN_SIZE_X = new float[1024];
    private static final float[] RAIN_SIZE_Z = new float[1024];
    private static final float[][] RAIN_COLORS = {
        {0.612F, 0.357F, 0.235F}, // Chocolate
        {1.0F, 0.878F, 0.639F} // Cream
    };
    private static int rainSoundTime;

    static {
        for (int z = 0; z < 32; z++) {
            for (int x = 0; x < 32; x++) {
                float offsetX = x - 16;
                float offsetZ = z - 16;
                float length = Mth.sqrt(offsetX * offsetX + offsetZ * offsetZ);
                int index = z * 32 + x;
                if (length == 0.0F) {
                    RAIN_SIZE_X[index] = 0.0F;
                    RAIN_SIZE_Z[index] = 0.0F;
                } else {
                    RAIN_SIZE_X[index] = -offsetZ / length;
                    RAIN_SIZE_Z[index] = offsetX / length;
                }
            }
        }
    }

    private MilkRainRenderer() {
    }

    public static void render(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture,
            double cameraX, double cameraY, double cameraZ) {
        float strength = level.getRainLevel(partialTick);
        if (strength <= 0.0F) {
            return;
        }

        lightTexture.turnOnLightLayer();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(Minecraft.useShaderTransparency());
        RenderSystem.setShader(GameRenderer::getParticleShader);

        int radius = Minecraft.useFancyGraphics() ? 10 : 5;
        renderRain(level, ticks, partialTick, cameraX, cameraY, cameraZ, strength, radius);
        renderSnow(level, ticks, partialTick, cameraX, cameraY, cameraZ, strength, radius);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        lightTexture.turnOffLightLayer();
    }

    private static void renderRain(ClientLevel level, int ticks, float partialTick, double cameraX, double cameraY,
            double cameraZ, float strength, int radius) {
        RenderSystem.setShaderTexture(0, MILK_RAIN);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

        int centerX = Mth.floor(cameraX);
        int centerY = Mth.floor(cameraY);
        int centerZ = Mth.floor(cameraZ);
        BlockPos.MutableBlockPos sample = new BlockPos.MutableBlockPos();

        for (int worldZ = centerZ - radius; worldZ <= centerZ + radius; worldZ++) {
            for (int worldX = centerX - radius; worldX <= centerX + radius; worldX++) {
                int columnHash = rainColumnHash(worldX, worldZ);
                int index = (worldZ - centerZ + 16) * 32 + worldX - centerX + 16;
                double sideX = RAIN_SIZE_X[index] * 0.5D;
                double sideZ = RAIN_SIZE_Z[index] * 0.5D;
                sample.set(worldX, cameraY, worldZ);
                if (CandyPrecipitation.at(level, sample) != Biome.Precipitation.RAIN) {
                    continue;
                }

                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, worldX, worldZ);
                int bottomY = Math.max(centerY - radius, surfaceY);
                int topY = Math.max(centerY + radius, surfaceY);
                int lightY = Math.max(surfaceY, centerY);
                if (bottomY == topY) {
                    continue;
                }

                long seed = (long)(worldX * worldX * 3121 + worldX * 45238971
                    + worldZ * worldZ * 418711 + worldZ * 13761);
                RandomSource random = RandomSource.create(seed);
                int phase = ticks + worldX * worldX * 3121 + worldX * 45238971
                    + worldZ * worldZ * 418711 + worldZ * 13761 & 31;
                float scroll = -((phase + partialTick) / 32.0F) * (3.0F + random.nextFloat());
                double distanceX = worldX + 0.5D - cameraX;
                double distanceZ = worldZ + 0.5D - cameraZ;
                float distance = (float)Math.sqrt(distanceX * distanceX + distanceZ * distanceZ) / radius;
                float alpha = ((1.0F - distance * distance) * 0.5F + 0.5F) * strength;
                sample.set(worldX, lightY, worldZ);
                int light = LevelRenderer.getLightColor(level, sample);

                double x = worldX - cameraX + 0.5D;
                double z = worldZ - cameraZ + 0.5D;
                double bottom = bottomY - cameraY;
                double top = topY - cameraY;
                float bottomV = bottomY * 0.25F + scroll;
                float topV = topY * 0.25F + scroll;
                float[] color = RAIN_COLORS[columnHash & 1];

                // Vanilla intentionally maps bottomV to the top vertices. Reversing this makes rain move upward.
                buffer.vertex(x - sideX, top, z - sideZ).uv(0.0F, bottomV).color(color[0], color[1], color[2], alpha).uv2(light).endVertex();
                buffer.vertex(x + sideX, top, z + sideZ).uv(1.0F, bottomV).color(color[0], color[1], color[2], alpha).uv2(light).endVertex();
                buffer.vertex(x + sideX, bottom, z + sideZ).uv(1.0F, topV).color(color[0], color[1], color[2], alpha).uv2(light).endVertex();
                buffer.vertex(x - sideX, bottom, z - sideZ).uv(0.0F, topV).color(color[0], color[1], color[2], alpha).uv2(light).endVertex();
            }
        }
        tesselator.end();
    }

    private static void renderSnow(ClientLevel level, int ticks, float partialTick, double cameraX, double cameraY,
            double cameraZ, float strength, int radius) {
        RenderSystem.setShaderTexture(0, SNOW);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);

        int centerX = Mth.floor(cameraX);
        int centerY = Mth.floor(cameraY);
        int centerZ = Mth.floor(cameraZ);
        float scroll = -((ticks & 511) + partialTick) / 512.0F;
        BlockPos.MutableBlockPos sample = new BlockPos.MutableBlockPos();

        for (int worldZ = centerZ - radius; worldZ <= centerZ + radius; worldZ++) {
            for (int worldX = centerX - radius; worldX <= centerX + radius; worldX++) {
                int index = (worldZ - centerZ + 16) * 32 + worldX - centerX + 16;
                double sideX = RAIN_SIZE_X[index] * 0.5D;
                double sideZ = RAIN_SIZE_Z[index] * 0.5D;
                sample.set(worldX, cameraY, worldZ);
                if (CandyPrecipitation.at(level, sample) != Biome.Precipitation.SNOW) {
                    continue;
                }

                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, worldX, worldZ);
                int bottomY = Math.max(centerY - radius, surfaceY);
                int topY = Math.max(centerY + radius, surfaceY);
                int lightY = Math.max(surfaceY, centerY);
                if (bottomY == topY) {
                    continue;
                }

                long seed = (long)(worldX * worldX * 3121 + worldX * 45238971
                    + worldZ * worldZ * 418711 + worldZ * 13761);
                RandomSource random = RandomSource.create(seed);
                float uOffset = random.nextFloat() + (ticks + partialTick) * 0.01F * (float)random.nextGaussian();
                float vOffset = random.nextFloat() + (ticks + partialTick) * 0.001F * (float)random.nextGaussian();
                double distanceX = worldX + 0.5D - cameraX;
                double distanceZ = worldZ + 0.5D - cameraZ;
                float distance = (float)Math.sqrt(distanceX * distanceX + distanceZ * distanceZ) / radius;
                float alpha = ((1.0F - distance * distance) * 0.3F + 0.5F) * strength;
                sample.set(worldX, lightY, worldZ);
                int light = LevelRenderer.getLightColor(level, sample);

                double x = worldX - cameraX + 0.5D;
                double z = worldZ - cameraZ + 0.5D;
                double bottom = bottomY - cameraY;
                double top = topY - cameraY;
                float bottomV = bottomY * 0.25F + scroll + vOffset;
                float topV = topY * 0.25F + scroll + vOffset;

                buffer.vertex(x - sideX, top, z - sideZ).uv(uOffset, bottomV).color(1.0F, 1.0F, 1.0F, alpha).uv2(light).endVertex();
                buffer.vertex(x + sideX, top, z + sideZ).uv(uOffset + 1.0F, bottomV).color(1.0F, 1.0F, 1.0F, alpha).uv2(light).endVertex();
                buffer.vertex(x + sideX, bottom, z + sideZ).uv(uOffset + 1.0F, topV).color(1.0F, 1.0F, 1.0F, alpha).uv2(light).endVertex();
                buffer.vertex(x - sideX, bottom, z - sideZ).uv(uOffset, topV).color(1.0F, 1.0F, 1.0F, alpha).uv2(light).endVertex();
            }
        }
        tesselator.end();
    }

    public static void tick(ClientLevel level, int ticks, Camera camera) {
        float strength = level.getRainLevel(1.0F) / (Minecraft.useFancyGraphics() ? 1.0F : 2.0F);
        if (strength <= 0.0F) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        RandomSource random = RandomSource.create((long)ticks * 312987231L);
        BlockPos cameraPos = BlockPos.containing(camera.getPosition());
        BlockPos lastLanding = null;
        int attempts = (int)(100.0F * strength * strength)
            / (minecraft.options.particles().get() == ParticleStatus.DECREASED ? 2 : 1);

        for (int i = 0; i < attempts; i++) {
            int offsetX = random.nextInt(21) - 10;
            int offsetZ = random.nextInt(21) - 10;
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, cameraPos.offset(offsetX, 0, offsetZ));
            if (surface.getY() <= level.getMinBuildHeight()
                || surface.getY() > cameraPos.getY() + 10
                || surface.getY() < cameraPos.getY() - 10
                || CandyPrecipitation.at(level, surface) != Biome.Precipitation.RAIN) {
                continue;
            }

            BlockPos landing = surface.below();
            lastLanding = landing;
            if (minecraft.options.particles().get() == ParticleStatus.MINIMAL) {
                break;
            }

            double localX = random.nextDouble();
            double localZ = random.nextDouble();
            BlockState state = level.getBlockState(landing);
            FluidState fluid = level.getFluidState(landing);
            VoxelShape shape = state.getCollisionShape(level, landing);
            double collisionHeight = shape.max(Direction.Axis.Y, localX, localZ);
            double fluidHeight = fluid.getHeight(level, landing);
            double landingHeight = Math.max(collisionHeight, fluidHeight);
            ParticleOptions particle = !fluid.is(FluidTags.LAVA)
                && !state.is(Blocks.MAGMA_BLOCK)
                && !CampfireBlock.isLitCampfire(state)
                ? CCParticleTypes.MILK_RAIN_SPLASH.get()
                : ParticleTypes.SMOKE;
            level.addParticle(particle, landing.getX() + localX, landing.getY() + landingHeight,
                landing.getZ() + localZ, 0.0D, 0.0D, 0.0D);
        }

        if (lastLanding != null && random.nextInt(3) < rainSoundTime++) {
            rainSoundTime = 0;
            if (lastLanding.getY() > cameraPos.getY() + 1
                && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, cameraPos).getY() > Mth.floor(cameraPos.getY())) {
                level.playLocalSound(lastLanding, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1F, 0.5F, false);
            } else {
                level.playLocalSound(lastLanding, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F, 1.0F, false);
            }
        }
    }

    private static int rainColumnHash(int x, int z) {
        int hash = x * 73428767 ^ z * 912931;
        hash ^= hash >>> 16;
        return hash;
    }
}
