package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.SpellRandomizerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public final class SpellRandomizerScreen extends AbstractContainerScreen<SpellRandomizerMenu> {
    public SpellRandomizerScreen(SpellRandomizerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 220;
        imageHeight = 118;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(
                Component.literal("Randomize"),
                button -> sendAction(SpellRandomizerMenu.ACTION_RANDOMIZE)
        ).bounds(leftPos + 65, topPos + 84, 90, 20).build());
    }

    private void sendAction(int action) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, action);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;

        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xF0161A22);
        graphics.renderOutline(x, y, imageWidth, imageHeight, 0xFF8B6BC5);
        graphics.drawCenteredString(font, "Random Spell Generator", x + imageWidth / 2, y + 14, 0xFFFFFFFF);

        for (int i = 0; i < SpellRandomizerMenu.SLOT_COUNT; i++) {
            int sx = x + 43 + i * 27;
            int sy = y + 51;
            graphics.fill(sx, sy, sx + 18, sy + 18, 0xFF2B303B);
            graphics.renderOutline(sx, sy, 18, 18, 0xFF8B6BC5);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
