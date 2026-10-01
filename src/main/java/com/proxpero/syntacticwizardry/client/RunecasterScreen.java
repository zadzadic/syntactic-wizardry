package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.RunecasterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class RunecasterScreen extends AbstractContainerScreen<RunecasterMenu> {
    public RunecasterScreen(RunecasterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = RunecasterMenu.WIDTH;
        imageHeight = RunecasterMenu.HEIGHT;
        inventoryLabelY = 73;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xE0121722);
        graphics.renderOutline(leftPos, topPos, imageWidth, imageHeight, 0xFF7A5B45);

        graphics.fill(leftPos + 79, topPos + 23, leftPos + 97, topPos + 41, 0xFF2A3240);
        graphics.renderOutline(leftPos + 79, topPos + 23, 18, 18, 0xFFD8A767);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) slotBox(graphics, leftPos + 7 + col * 18, topPos + 83 + row * 18);
        }
        for (int col = 0; col < 9; col++) slotBox(graphics, leftPos + 7 + col * 18, topPos + 141);
    }

    private static void slotBox(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF202833);
        graphics.renderOutline(x, y, 18, 18, 0xFF566474);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, title, imageWidth / 2, 8, 0xF0E5D8);
        graphics.drawCenteredString(font, "Mounted Rune", imageWidth / 2, 48, 0xD8B98A);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0xC8D8E8, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
