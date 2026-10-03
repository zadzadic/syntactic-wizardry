package com.proxpero.syntacticwizardry.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

public final class ArmillaryRenameScreen extends Screen {
    private final Screen parent;
    private final String initialName;
    private final Consumer<String> onRename;
    private EditBox nameBox;

    public ArmillaryRenameScreen(Screen parent, String initialName, Consumer<String> onRename) {
        super(Component.literal("Rename Ritual"));
        this.parent = parent;
        this.initialName = initialName == null ? "" : initialName;
        this.onRename = onRename;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int cy = height / 2;
        nameBox = new EditBox(font, cx - 100, cy - 24, 200, 20, Component.literal("Ritual Name"));
        nameBox.setMaxLength(32);
        nameBox.setValue(initialName);
        nameBox.setFocused(true);
        addRenderableWidget(nameBox);

        addRenderableWidget(Button.builder(Component.literal("Rename"), button -> apply())
                .bounds(cx - 100, cy + 4, 96, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> onClose())
                .bounds(cx + 4, cy + 4, 96, 20)
                .build());
        setInitialFocus(nameBox);
    }

    private void apply() {
        if (onRename != null) onRename.accept(nameBox.getValue());
        onClose();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) {
            apply();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (minecraft != null) minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderTransparentBackground(graphics);
        int cx = width / 2;
        int cy = height / 2;
        graphics.drawCenteredString(font, title, cx, cy - 46, 0xFFFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
