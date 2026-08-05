package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class WikiPageButton extends Button {
    private static final ResourceLocation TEXTURE = new ResourceLocation(
        CandyCraft.MODID,
        "textures/gui/wiki_page_buttons.png"
    );
    private static final int SIZE = 20;
    private static final int TEXTURE_WIDTH = 40;
    private static final int TEXTURE_HEIGHT = 60;

    private final boolean forward;

    public WikiPageButton(int x, int y, boolean forward, OnPress onPress) {
        super(
            x,
            y,
            SIZE,
            SIZE,
            Component.translatable(forward
                ? "button.candycraftmod.wiki.next"
                : "button.candycraftmod.wiki.previous"),
            onPress,
            DEFAULT_NARRATION
        );
        this.forward = forward;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int textureX = forward ? SIZE : 0;
        int textureY = !active ? SIZE * 2 : isHoveredOrFocused() ? SIZE : 0;
        graphics.blit(TEXTURE, getX(), getY(), textureX, textureY, SIZE, SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
