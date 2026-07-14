package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.registry.CCParticleTypes;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public final class MilkRainRenderer {
    private static final ResourceLocation MILK_RAIN = new ResourceLocation(CandyCraft.MODID, "textures/environment/milk_rain.png");
    private static final ResourceLocation SNOW = new ResourceLocation("textures/environment/snow.png");
    private static final int RENDER_RADIUS = 10;

    private MilkRainRenderer() {
    }

    public static void render(ClientLevel level, int ticks, float partialTick, double cameraX, double cameraY, double cameraZ) {
        float strength = level.getRainLevel(partialTick);
        if (strength <= 0.0F) {
            return;
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(true);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        renderType(level, ticks, partialTick, cameraX, cameraY, cameraZ, strength, Biome.Precipitation.RAIN, MILK_RAIN);
        renderType(level, ticks, partialTick, cameraX, cameraY, cameraZ, strength, Biome.Precipitation.SNOW, SNOW);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderType(ClientLevel level, int ticks, float partialTick, double cameraX, double cameraY,
            double cameraZ, float strength, Biome.Precipitation wanted, ResourceLocation texture) {
        RenderSystem.setShaderTexture(0, texture);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        int centerX = Mth.floor(cameraX);
        int centerY = Mth.floor(cameraY);
        int centerZ = Mth.floor(cameraZ);
        float scroll = wanted == Biome.Precipitation.RAIN
            ? -((ticks + partialTick) % 32.0F) / 32.0F
            : ((ticks + partialTick) % 64.0F) / 128.0F;

        for (int dz = -RENDER_RADIUS; dz <= RENDER_RADIUS; dz++) {
            for (int dx = -RENDER_RADIUS; dx <= RENDER_RADIUS; dx++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                float distance = Mth.sqrt(dx * dx + dz * dz);
                if (distance > RENDER_RADIUS) {
                    continue;
                }
                int worldX = centerX + dx;
                int worldZ = centerZ + dz;
                int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, worldX, worldZ);
                int bottomY = Math.max(surfaceY, centerY - RENDER_RADIUS);
                int topY = Math.max(surfaceY, centerY + RENDER_RADIUS);
                if (bottomY == topY) {
                    continue;
                }
                BlockPos sample = new BlockPos(worldX, Math.max(surfaceY, centerY), worldZ);
                if (level.getBiome(sample).value().getPrecipitationAt(sample) != wanted) {
                    continue;
                }

                float sideX = -dz / distance * 0.5F;
                float sideZ = dx / distance * 0.5F;
                float alpha = Mth.clamp((1.0F - distance / (RENDER_RADIUS + 1.0F)) * strength, 0.05F, 0.9F);
                float x = (float)(worldX + 0.5D - cameraX);
                float z = (float)(worldZ + 0.5D - cameraZ);
                float bottom = (float)(bottomY - cameraY);
                float top = (float)(topY - cameraY);
                float v0 = bottomY * 0.25F + scroll;
                float v1 = topY * 0.25F + scroll;

                buffer.vertex(x - sideX, top, z - sideZ).uv(0.0F, v1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
                buffer.vertex(x + sideX, top, z + sideZ).uv(1.0F, v1).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
                buffer.vertex(x + sideX, bottom, z + sideZ).uv(1.0F, v0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
                buffer.vertex(x - sideX, bottom, z - sideZ).uv(0.0F, v0).color(1.0F, 1.0F, 1.0F, alpha).endVertex();
            }
        }
        tesselator.end();
    }

    public static void tick(ClientLevel level, Camera camera) {
        float strength = level.getRainLevel(1.0F);
        if (strength <= 0.0F) {
            return;
        }
        Vec3 view = camera.getPosition();
        int attempts = Math.max(4, (int)(28.0F * strength * strength));
        for (int i = 0; i < attempts; i++) {
            int x = Mth.floor(view.x) + level.random.nextInt(21) - 10;
            int z = Mth.floor(view.z) + level.random.nextInt(21) - 10;
            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            BlockPos surface = new BlockPos(x, surfaceY, z);
            if (level.getBiome(surface).value().getPrecipitationAt(surface) != Biome.Precipitation.RAIN) {
                continue;
            }
            double px = x + level.random.nextDouble();
            double pz = z + level.random.nextDouble();
            if (level.random.nextInt(3) == 0) {
                double startY = Math.max(surfaceY + 1.5D, view.y + 6.0D + level.random.nextDouble() * 5.0D);
                level.addParticle(CCParticleTypes.MILK_RAIN_DROP.get(), px, startY, pz, 0.0D, -0.8D, 0.0D);
            }
            level.addParticle(CCParticleTypes.MILK_RAIN_SPLASH.get(), px, surfaceY + 0.02D, pz,
                (level.random.nextDouble() - 0.5D) * 0.04D, 0.03D, (level.random.nextDouble() - 0.5D) * 0.04D);
        }
    }
}
