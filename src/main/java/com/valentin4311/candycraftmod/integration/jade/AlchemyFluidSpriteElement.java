package com.valentin4311.candycraftmod.integration.jade;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.ui.Element;

final class AlchemyFluidSpriteElement extends Element {
    private static final Vec2 DEFAULT_SIZE = new Vec2(16.0F, 16.0F);
    private final ResourceLocation spriteId;

    AlchemyFluidSpriteElement(ResourceLocation spriteId) {
        this.spriteId = spriteId;
    }

    @Override
    public Vec2 getSize() {
        return DEFAULT_SIZE;
    }

    @Override
    public void render(GuiGraphics graphics, float x, float y, float maxX, float maxY) {
        Vec2 renderedSize = getCachedSize();
        TextureAtlasSprite sprite = Minecraft.getInstance()
            .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS)
            .apply(spriteId);
        int width = Math.max(1, Math.round(renderedSize.x));
        int height = Math.max(1, Math.round(renderedSize.y));
        for (int offsetX = 0; offsetX < width; offsetX += 16) {
            int tileWidth = Math.min(16, width - offsetX);
            graphics.blit(Math.round(x) + offsetX, Math.round(y), 0,
                tileWidth, height, sprite);
        }
    }

    @Override
    public String getMessage() {
        return null;
    }
}
