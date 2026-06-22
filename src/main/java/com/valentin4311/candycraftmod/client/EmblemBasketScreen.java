package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.menu.EmblemBasketMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class EmblemBasketScreen extends AbstractContainerScreen<EmblemBasketMenu> {
    private static final ResourceLocation GENERIC_54 = new ResourceLocation("textures/gui/container/generic_54.png");
    private static final int SLOT_ROW_HEIGHT = 18;

    public EmblemBasketScreen(EmblemBasketMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 132;
        inventoryLabelY = 38;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(GENERIC_54, x, y, 0, 0, imageWidth, SLOT_ROW_HEIGHT + 17);
        graphics.blit(GENERIC_54, x, y + SLOT_ROW_HEIGHT + 17, 0, 126, imageWidth, 96);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
