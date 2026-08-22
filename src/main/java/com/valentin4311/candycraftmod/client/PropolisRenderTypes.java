package com.valentin4311.candycraftmod.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.valentin4311.candycraftmod.CandyCraft;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public final class PropolisRenderTypes extends RenderStateShard {
    private static final ResourceLocation PROPOLIS_GLINT_TEXTURE =
        new ResourceLocation(CandyCraft.MODID, "textures/misc/propolis_glint.png");
    private static final TexturingStateShard PROPOLIS_GLINT_TEXTURING = new TexturingStateShard(
        CandyCraft.MODID + ":propolis_glint_texturing",
        PropolisRenderTypes::setupPropolisGlintTexturing,
        RenderSystem::resetTextureMatrix
    );

    public static final RenderType ENTITY_GLINT = RenderType.create(
        CandyCraft.MODID + ":propolis_entity_glint",
        DefaultVertexFormat.POSITION_TEX,
        VertexFormat.Mode.QUADS,
        256,
        false,
        true,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER)
            .setTextureState(new TextureStateShard(PROPOLIS_GLINT_TEXTURE, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(LEQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(PROPOLIS_GLINT_TEXTURING)
            .setLayeringState(POLYGON_OFFSET_LAYERING)
            .createCompositeState(false)
    );

    private PropolisRenderTypes() {
        super(CandyCraft.MODID + ":propolis_render_types", () -> { }, () -> { });
    }

    private static void setupPropolisGlintTexturing() {
        long time = (long)(Util.getMillis() * Minecraft.getInstance().options.glintSpeed().get() * 8.0D);
        float horizontal = (time % 110000L) / 110000.0F;
        float vertical = (time % 30000L) / 30000.0F;
        Matrix4f matrix = new Matrix4f()
            .translation(-horizontal, vertical, 0.0F)
            .rotateZ(0.17453292F)
            .scale(PropolisVisualSettings.textureScale());
        RenderSystem.setTextureMatrix(matrix);
    }
}
