package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.CandyCraft;
import com.valentin4311.candycraftmod.menu.SugarFactoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SugarFactoryScreen extends AbstractContainerScreen<SugarFactoryMenu> {
    private static final ResourceLocation SUGAR_FACTORY = new ResourceLocation(CandyCraft.MODID, "textures/gui/gui_sugar.png");
    private static final ResourceLocation ADVANCED_SUGAR_FACTORY = new ResourceLocation(CandyCraft.MODID, "textures/gui/gui_advancedsugar.png");

    public SugarFactoryScreen(SugarFactoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        titleLabelY = 28;
        inventoryLabelY = 85;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        ResourceLocation texture = menu.isAdvanced() ? ADVANCED_SUGAR_FACTORY : SUGAR_FACTORY;
        int x = leftPos + 1;
        int y = topPos + 26;
        graphics.blit(texture, x, y, 0, 0, 174, 114);
        graphics.blit(texture, x + 27, y + 9, 0, 114, menu.getProgressScaled(120), 12);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, title, imageWidth / 2, 8, 0xFFFFFF);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
