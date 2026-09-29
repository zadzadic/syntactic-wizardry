package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.FocusSpellMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class FocusSpellScreen extends AbstractContainerScreen<FocusSpellMenu> {
    public FocusSpellScreen(FocusSpellMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = FocusSpellMenu.WIDTH;
        imageHeight = FocusSpellMenu.HEIGHT;
        inventoryLabelY = 73;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xE0121722);
        graphics.renderOutline(leftPos, topPos, imageWidth, imageHeight, 0xFF8A63B8);
        int count = menu.focusSlotCount();
        int startX = count == 1 ? 80 : 62;
        for (int i = 0; i < count; i++) {
            int x = leftPos + startX + i * 18 - 1;
            int y = topPos + 23;
            graphics.fill(x, y, x + 18, y + 18, i == menu.activeSlot() ? 0xFF6D4F8D : 0xFF2A3240);
            graphics.renderOutline(x, y, 18, 18, i == menu.activeSlot() ? 0xFFF0D77A : 0xFF69798C);
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) slotBox(graphics, leftPos + 7 + col * 18, topPos + 83 + row * 18);
        for (int col = 0; col < 9; col++) slotBox(graphics, leftPos + 7 + col * 18, topPos + 141);
    }

    private static void slotBox(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, 0xFF202833);
        graphics.renderOutline(x, y, 18, 18, 0xFF566474);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, title, imageWidth / 2, 8, 0xEDE7F6);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0xC8D8E8, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
