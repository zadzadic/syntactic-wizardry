package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.HighManaClaimSavedData;
import com.proxpero.syntacticwizardry.HighManaCompassMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public final class HighManaCompassScreen extends AbstractContainerScreen<HighManaCompassMenu> {
    private static final int ENTRIES_PER_PAGE = 7;
    private int page;

    public HighManaCompassScreen(HighManaCompassMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = HighManaCompassMenu.WIDTH;
        imageHeight = HighManaCompassMenu.HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        rebuildButtons();
    }

    private void rebuildButtons() {
        clearWidgets();
        List<HighManaClaimSavedData.Claim> claims = menu.claims();
        int start = page * ENTRIES_PER_PAGE;
        int end = Math.min(claims.size(), start + ENTRIES_PER_PAGE);
        int y = topPos + 38;
        for (int index = start; index < end; index++) {
            HighManaClaimSavedData.Claim claim = claims.get(index);
            String dimension = claim.dimension().getPath();
            String label = dimension + "  •  " + claim.blockX() + ", " + claim.blockZ();
            int action = HighManaCompassMenu.ACTION_TELEPORT_BASE + index;
            addRenderableWidget(Button.builder(Component.literal(label), button -> sendAction(action))
                    .bounds(leftPos + 20, y, imageWidth - 40, 20).build());
            y += 23;
        }
        if (page > 0) addRenderableWidget(Button.builder(Component.literal("<"), button -> { page--; rebuildButtons(); })
                .bounds(leftPos + 20, topPos + imageHeight - 30, 30, 20).build());
        int pageCount = Math.max(1, (claims.size() + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE);
        if (page + 1 < pageCount) addRenderableWidget(Button.builder(Component.literal(">"), button -> { page++; rebuildButtons(); })
                .bounds(leftPos + imageWidth - 50, topPos + imageHeight - 30, 30, 20).build());
    }

    private void sendAction(int id) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xE0121722);
        graphics.renderOutline(leftPos, topPos, imageWidth, imageHeight, 0xFFB88A2A);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(font, title, imageWidth / 2, 12, 0xF5E3AF);
        int pageCount = Math.max(1, (menu.claims().size() + ENTRIES_PER_PAGE - 1) / ENTRIES_PER_PAGE);
        graphics.drawCenteredString(font, "Page " + (page + 1) + "/" + pageCount, imageWidth / 2, imageHeight - 24, 0xC8D8E8);
        if (menu.claims().isEmpty()) graphics.drawCenteredString(font, "No High Mana Zones claimed.", imageWidth / 2, 70, 0xC8D8E8);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
