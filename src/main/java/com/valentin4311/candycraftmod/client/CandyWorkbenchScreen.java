package com.valentin4311.candycraftmod.client;

import com.valentin4311.candycraftmod.block.CandyWorkbenchBlock;
import com.valentin4311.candycraftmod.menu.CandyWorkbenchMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CandyWorkbenchScreen extends AbstractContainerScreen<CandyWorkbenchMenu> {
    public CandyWorkbenchScreen(CandyWorkbenchMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        CandyWorkbenchBlock.CandyWorkbenchTheme theme = menu.theme();
        int x = leftPos;
        int y = topPos;
        int base = withAlpha(theme.baseColor(), 0xFF);
        int light = withAlpha(theme.lightColor(), 0xFF);
        int dark = withAlpha(theme.darkColor(), 0xFF);

        graphics.fill(x, y, x + imageWidth, y + imageHeight, dark);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, base);
        graphics.fill(x + 5, y + 5, x + imageWidth - 5, y + imageHeight - 5, tint(theme.baseColor(), 0.88F));
        graphics.fill(x + 7, y + 7, x + imageWidth - 7, y + 17, tint(theme.lightColor(), 0.95F));
        graphics.fill(x + 7, y + 81, x + imageWidth - 7, y + imageHeight - 7, tint(theme.lightColor(), 0.82F));

        drawSlot(graphics, x + 29, y + 16, light, dark);
        drawSlot(graphics, x + 47, y + 16, light, dark);
        drawSlot(graphics, x + 65, y + 16, light, dark);
        drawSlot(graphics, x + 29, y + 34, light, dark);
        drawSlot(graphics, x + 47, y + 34, light, dark);
        drawSlot(graphics, x + 65, y + 34, light, dark);
        drawSlot(graphics, x + 29, y + 52, light, dark);
        drawSlot(graphics, x + 47, y + 52, light, dark);
        drawSlot(graphics, x + 65, y + 52, light, dark);
        drawArrow(graphics, x + 88, y + 35, light, dark);
        drawSlot(graphics, x + 123, y + 34, light, dark);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlot(graphics, x + 7 + column * 18, y + 83 + row * 18, light, dark);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlot(graphics, x + 7 + column * 18, y + 141, light, dark);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private static void drawSlot(GuiGraphics graphics, int x, int y, int light, int dark) {
        graphics.fill(x, y, x + 18, y + 18, dark);
        graphics.fill(x + 1, y + 1, x + 18, y + 2, 0xAA000000);
        graphics.fill(x + 1, y + 1, x + 2, y + 18, 0xAA000000);
        graphics.fill(x + 2, y + 2, x + 17, y + 17, 0xFFEFEFEF);
        graphics.fill(x + 2, y + 2, x + 17, y + 3, light);
        graphics.fill(x + 2, y + 2, x + 3, y + 17, light);
    }

    private static void drawArrow(GuiGraphics graphics, int x, int y, int light, int dark) {
        graphics.fill(x, y + 6, x + 22, y + 11, dark);
        graphics.fill(x + 16, y + 2, x + 20, y + 15, dark);
        graphics.fill(x + 20, y + 5, x + 24, y + 12, dark);
        graphics.fill(x + 1, y + 7, x + 20, y + 10, light);
        graphics.fill(x + 17, y + 4, x + 19, y + 13, light);
        graphics.fill(x + 20, y + 7, x + 22, y + 10, light);
    }

    private static int withAlpha(int rgb, int alpha) {
        return (alpha & 255) << 24 | rgb & 0xFFFFFF;
    }

    private static int tint(int rgb, float factor) {
        int red = Math.min(255, Math.round(((rgb >> 16) & 255) * factor));
        int green = Math.min(255, Math.round(((rgb >> 8) & 255) * factor));
        int blue = Math.min(255, Math.round((rgb & 255) * factor));
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }
}
