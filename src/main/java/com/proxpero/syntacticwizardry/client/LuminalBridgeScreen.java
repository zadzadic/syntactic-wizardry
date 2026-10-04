package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.LuminalBridgeDestinationsPayload;
import com.proxpero.syntacticwizardry.LuminalBridgeTravelPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public final class LuminalBridgeScreen extends Screen {
    private static final int ROW_H = 30;
    private final LuminalBridgeDestinationsPayload payload;
    private int scroll;

    public LuminalBridgeScreen(LuminalBridgeDestinationsPayload payload) {
        super(Component.literal("Luminal Bridge"));
        this.payload = payload;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int panelW = Math.min(360, width - 32);
        int panelH = Math.min(280, height - 32);
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;

        graphics.fill(x, y, x + panelW, y + panelH, 0xE0182232);
        graphics.renderOutline(x, y, panelW, panelH, 0xFF8E72C7);
        graphics.drawCenteredString(font, title, width / 2, y + 10, 0xFFFFFFFF);

        List<LuminalBridgeDestinationsPayload.Destination> destinations = payload.destinations();
        if (destinations.isEmpty()) {
            graphics.drawCenteredString(
                    font,
                    "No linked Luminal Bridges are available.",
                    width / 2,
                    y + 54,
                    0xFFB9C5D6);
            return;
        }

        int listTop = y + 30;
        int listBottom = y + panelH - 12;
        int visible = Math.max(1, (listBottom - listTop) / ROW_H);
        int maxScroll = Math.max(0, destinations.size() - visible);
        scroll = Math.max(0, Math.min(maxScroll, scroll));

        for (int row = 0; row < visible; row++) {
            int index = scroll + row;
            if (index >= destinations.size()) break;

            LuminalBridgeDestinationsPayload.Destination destination = destinations.get(index);
            int rowY = listTop + row * ROW_H;
            boolean hovered = mouseX >= x + 12 && mouseX < x + panelW - 12
                    && mouseY >= rowY && mouseY < rowY + ROW_H - 4;

            graphics.fill(
                    x + 12,
                    rowY,
                    x + panelW - 12,
                    rowY + ROW_H - 4,
                    hovered ? 0xD05D4A86 : 0xA02B3548);
            graphics.renderOutline(
                    x + 12,
                    rowY,
                    panelW - 24,
                    ROW_H - 4,
                    hovered ? 0xFFD2B7FF : 0xFF66738A);

            graphics.drawString(font, destination.label(), x + 20, rowY + 5, 0xFFFFFFFF, false);
            String detail = destination.x() + ", " + destination.y() + ", " + destination.z()
                    + "  [" + shortDimension(destination.dimension()) + "]";
            graphics.drawString(font, detail, x + 20, rowY + 16, 0xFF9FAEC4, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int panelW = Math.min(360, width - 32);
        int panelH = Math.min(280, height - 32);
        int x = (width - panelW) / 2;
        int y = (height - panelH) / 2;
        int listTop = y + 30;
        int listBottom = y + panelH - 12;
        int visible = Math.max(1, (listBottom - listTop) / ROW_H);

        if (mouseX < x + 12 || mouseX >= x + panelW - 12
                || mouseY < listTop || mouseY >= listBottom) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int row = (int)((mouseY - listTop) / ROW_H);
        if (row < 0 || row >= visible) return true;

        int index = scroll + row;
        if (index < 0 || index >= payload.destinations().size()) return true;

        LuminalBridgeDestinationsPayload.Destination destination = payload.destinations().get(index);
        PacketDistributor.sendToServer(
                new LuminalBridgeTravelPayload(payload.sourceId(), destination.id()));
        onClose();
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY > 0.0D) scroll--;
        else if (scrollY < 0.0D) scroll++;
        return true;
    }

    private static String shortDimension(String value) {
        if (value == null || value.isBlank()) return "unknown";
        int split = value.indexOf(':');
        return split >= 0 && split + 1 < value.length() ? value.substring(split + 1) : value;
    }
}
