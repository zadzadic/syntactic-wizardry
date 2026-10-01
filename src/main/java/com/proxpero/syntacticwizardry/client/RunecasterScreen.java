package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.RunecasterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class RunecasterScreen extends AbstractContainerScreen<RunecasterMenu> {
    public RunecasterScreen(RunecasterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 73;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, -535685342);
        graphics.renderOutline(leftPos, topPos, imageWidth, imageHeight, -8758459);

        for (int i = 0; i < 3; i++) {
            slotBox(graphics, leftPos + 79, topPos + 17 + i * 18);
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                slotBox(graphics, leftPos + 7 + col * 18, topPos + 83 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            slotBox(graphics, leftPos + 7 + col * 18, topPos + 141);
        }
    }

    private static void slotBox(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, -14669773);
        graphics.renderOutline(x, y, 18, 18, -11115404);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, title, imageWidth / 2, 6, 15787480);
        graphics.drawCenteredString(font, "Runestones", imageWidth / 2, 64, 14203274);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 13162728, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
