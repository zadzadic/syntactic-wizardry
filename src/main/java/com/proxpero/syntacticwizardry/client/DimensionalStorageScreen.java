package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.DimensionalStorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class DimensionalStorageScreen extends AbstractContainerScreen<DimensionalStorageMenu> {
    private Button previousPage;
    private Button nextPage;

    public DimensionalStorageScreen(DimensionalStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = DimensionalStorageMenu.WIDTH;
        imageHeight = DimensionalStorageMenu.HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        previousPage = addRenderableWidget(Button.builder(Component.literal("<"), button -> sendAction(DimensionalStorageMenu.ACTION_PREVIOUS_PAGE))
                .bounds(leftPos + 8, topPos + 113, 20, 16).build());
        nextPage = addRenderableWidget(Button.builder(Component.literal(">"), button -> sendAction(DimensionalStorageMenu.ACTION_NEXT_PAGE))
                .bounds(leftPos + 168, topPos + 113, 20, 16).build());
        previousPage.active = menu.hasPreviousPage();
        nextPage.active = menu.hasNextPage();
    }

    private void sendAction(int id) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xE0101826);
        graphics.renderOutline(leftPos, topPos, imageWidth, imageHeight, 0xFF5B86B8);
        for (int local = 0; local < menu.storageSlotCount(); local++) {
            int col = local % DimensionalStorageMenu.STORAGE_COLS;
            int row = local / DimensionalStorageMenu.STORAGE_COLS;
            int x = leftPos + 7 + col * 18;
            int y = topPos + 19 + row * 18;
            graphics.fill(x, y, x + 18, y + 18, 0xFF243247);
            graphics.renderOutline(x, y, 18, 18, 0xFF506A88);
        }
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) {
            int x = leftPos + 16 + col * 18;
            int y = topPos + 138 + row * 18;
            graphics.fill(x, y, x + 18, y + 18, 0xFF202D40);
        }
        for (int col = 0; col < 9; col++) {
            int x = leftPos + 16 + col * 18;
            int y = topPos + 196;
            graphics.fill(x, y, x + 18, y + 18, 0xFF202D40);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 7, 0xE8F3FF, false);
        graphics.drawCenteredString(font, "Page " + (menu.page() + 1) + "/" + menu.pageCount() + "  •  " + menu.capacity() + " slots", imageWidth / 2, 117, 0xC8D8E8);
        graphics.drawString(font, playerInventoryTitle, 17, 130, 0xC8D8E8, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
