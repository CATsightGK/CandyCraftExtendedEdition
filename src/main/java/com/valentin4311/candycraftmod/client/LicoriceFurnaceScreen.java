package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.menu.LicoriceFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class LicoriceFurnaceScreen extends AbstractContainerScreen<LicoriceFurnaceMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(CandyCraft.MODID, "textures/gui/gui_sugarfurnace.png");

    public LicoriceFurnaceScreen(LicoriceFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int lit = menu.getLitProgress();
        if (lit > 0) {
            graphics.blit(TEXTURE, leftPos + 56, topPos + 36 + 12 - lit, 176, 12 - lit, 14, lit + 1);
        }
        int burn = menu.getBurnProgress();
        graphics.blit(TEXTURE, leftPos + 79, topPos + 34, 176, 14, burn + 1, 16);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, title, imageWidth / 2, 6, 0xA0A0A0);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
