package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.GoldenOrbMenu;
import com.proxpero.syntacticwizardry.ManaService;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import java.util.Locale;

public final class GoldenOrbScreen extends AbstractContainerScreen<GoldenOrbMenu> {
    public GoldenOrbScreen(GoldenOrbMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 240;
        imageHeight = 170;
    }

    @Override
    protected void init() {
        super.init();
        int y = topPos + 130;
        addRenderableWidget(Button.builder(Component.literal("-10"), b -> sendAction(GoldenOrbMenu.ACTION_MINUS_TEN)).bounds(leftPos + 38, y, 38, 20).build());
        addRenderableWidget(Button.builder(Component.literal("-1"), b -> sendAction(GoldenOrbMenu.ACTION_MINUS_ONE)).bounds(leftPos + 80, y, 38, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+1"), b -> sendAction(GoldenOrbMenu.ACTION_PLUS_ONE)).bounds(leftPos + 122, y, 38, 20).build());
        addRenderableWidget(Button.builder(Component.literal("+10"), b -> sendAction(GoldenOrbMenu.ACTION_PLUS_TEN)).bounds(leftPos + 164, y, 38, 20).build());
    }

    private void sendAction(int action) {
        if (minecraft != null && minecraft.gameMode != null) minecraft.gameMode.handleInventoryButtonClick(menu.containerId, action);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xF0181408);
        graphics.renderOutline(x, y, imageWidth, imageHeight, 0xFFFFD54A);
        graphics.fill(x + 8, y + 8, x + imageWidth - 8, y + 30, 0xFF6A5200);
        graphics.drawCenteredString(font, "THE GOLDEN ORB", x + imageWidth / 2, y + 15, 0xFFFFE58A);

        Player player = minecraft == null ? null : minecraft.player;
        if (player == null) return;

        int level = ManaService.getCastingLevel(player);
        float xp = ManaService.getCastingExperience(player);
        float maxMana = ManaService.getMaxMana(player);
        float regen = ManaService.getPassiveRegenPerSecond(player);

        int tx = x + 24;
        graphics.drawString(font, "Lore: N/A", tx, y + 45, 0xFFE5D79D, false);
        graphics.drawString(font, "Casting Level: " + level, tx, y + 62, 0xFFFFFFFF, false);
        graphics.drawString(font, "Casting Experience: " + format(xp), tx, y + 79, 0xFFFFFFFF, false);
        graphics.drawString(font, "Maximum Mana: " + format(maxMana), tx, y + 96, 0xFFFFFFFF, false);
        graphics.drawString(font, "Mana Regen: " + format(regen) + " / second", tx, y + 113, 0xFFFFFFFF, false);
    }

    private static String format(float value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {}

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
