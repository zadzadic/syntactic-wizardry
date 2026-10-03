package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.SpellComponentDefinition;
import com.proxpero.syntacticwizardry.SpellComponents;
import com.proxpero.syntacticwizardry.SpellPresentation;
import com.proxpero.syntacticwizardry.SpellPropertyDefinition;
import com.proxpero.syntacticwizardry.SpellPropertyKey;
import com.proxpero.syntacticwizardry.SpellPropertyKind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.List;

final class PermanentSpellEditor {
    private enum SelectorTab { EFFECTS, MODIFIERS }

    private static final int WIDTH = 320;
    private static final int HEIGHT = 232;
    private static final int GRID_X = 20;
    private static final int GRID_Y = 47;
    private static final int CELL = 16;
    private static final int STEP = 18;
    private static final int SELECTOR_START_X = 175;
    private static final int SELECTOR_GRID_Y = 52;
    private static final int SELECTOR_COLS = 5;
    private static final int SELECTOR_COL_STEP = 24;
    private static final int SELECTOR_ROW_STEP = 20;
    private static final int SELECTOR_VISIBLE_ROWS = 5;
    private static final int NO_CELL = -1;

    private final int[] plan = SpellPresentation.emptyPlan();
    private final int[] settings = SpellPresentation.emptySettings();

    private boolean open;
    private boolean leftWasDown;
    private boolean rightWasDown;
    private SelectorTab selectorTab = SelectorTab.EFFECTS;
    private final int[] selectorScrollRows = new int[SelectorTab.values().length];

    private int selectedCell = NO_CELL;
    private int dragSource = NO_CELL;
    private int dragNewType = SpellPresentation.TYPE_EMPTY;
    private SpellPropertyKey openOptionsProperty;

    boolean isOpen() {
        return open;
    }

    void open() {
        open = true;
        leftWasDown = false;
        rightWasDown = false;
        dragSource = NO_CELL;
        dragNewType = SpellPresentation.TYPE_EMPTY;
    }

    void close() {
        open = false;
        dragSource = NO_CELL;
        dragNewType = SpellPresentation.TYPE_EMPTY;
        openOptionsProperty = null;
    }

    boolean hasType(int type) {
        for (int cell = 0; cell < SpellPresentation.CELLS; cell++) {
            if (SpellPresentation.typeAt(plan, cell) == type) return true;
        }
        return false;
    }

    int[] snapshotPlan() {
        return plan.clone();
    }

    int[] snapshotSettings() {
        return settings.clone();
    }

    boolean tick(Minecraft mc) {
        if (!open || mc == null) return false;

        long window = mc.getWindow().getWindow();
        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rightDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        boolean leftPressed = leftDown && !leftWasDown;
        boolean leftReleased = !leftDown && leftWasDown;
        boolean rightPressed = rightDown && !rightWasDown;

        double mx = guiMouseX(mc);
        double my = guiMouseY(mc);
        int x = left(mc);
        int y = top(mc);

        if (leftPressed) {
            if (inside(mx, my, x + 12, y + 202, 62, 18)) {
                close();
                leftWasDown = leftDown;
                rightWasDown = rightDown;
                return true;
            }

            if (selectedCell >= 0) {
                if (inside(mx, my, x + 291, y + 15, 14, 12)) {
                    selectedCell = NO_CELL;
                    openOptionsProperty = null;
                    leftWasDown = leftDown;
                    rightWasDown = rightDown;
                    return true;
                }
                if (handlePropertyClick(mx, my, x, y)) {
                    leftWasDown = leftDown;
                    rightWasDown = rightDown;
                    return true;
                }
            } else {
                if (inside(mx, my, x + 170, y + 30, 61, 16)) {
                    selectorTab = SelectorTab.EFFECTS;
                    leftWasDown = leftDown;
                    rightWasDown = rightDown;
                    return true;
                }
                if (inside(mx, my, x + 233, y + 30, 72, 16)) {
                    selectorTab = SelectorTab.MODIFIERS;
                    leftWasDown = leftDown;
                    rightWasDown = rightDown;
                    return true;
                }

                SpellComponentDefinition selector = selectorComponentAt(mx, my, x, y);
                if (selector != null) {
                    dragNewType = selector.typeId();
                    dragSource = NO_CELL;
                    leftWasDown = leftDown;
                    rightWasDown = rightDown;
                    return true;
                }
            }

            int cell = workspaceCellAt(mx, my, x, y);
            if (cell >= 0 && typeAt(cell) != SpellPresentation.TYPE_EMPTY) {
                dragSource = cell;
                dragNewType = SpellPresentation.TYPE_EMPTY;
            }
        }

        if (leftReleased && draggedType() != SpellPresentation.TYPE_EMPTY) {
            int target = workspaceCellAt(mx, my, x, y);
            if (target >= 0) {
                if (dragNewType != SpellPresentation.TYPE_EMPTY) {
                    setCellType(target, dragNewType);
                    selectedCell = target;
                } else if (dragSource >= 0) {
                    if (target != dragSource) swapCells(dragSource, target);
                    selectedCell = target;
                }
                openOptionsProperty = null;
            } else if (dragSource >= 0) {
                selectedCell = dragSource;
                openOptionsProperty = null;
            }
            dragSource = NO_CELL;
            dragNewType = SpellPresentation.TYPE_EMPTY;
        }

        if (rightPressed) {
            int cell = workspaceCellAt(mx, my, x, y);
            if (cell >= 0 && typeAt(cell) != SpellPresentation.TYPE_EMPTY) {
                clearCell(cell);
                if (selectedCell == cell) {
                    selectedCell = NO_CELL;
                    openOptionsProperty = null;
                }
                rightWasDown = rightDown;
                leftWasDown = leftDown;
                return true;
            }
        }

        leftWasDown = leftDown;
        rightWasDown = rightDown;
        return inside(mx, my, x, y, WIDTH, HEIGHT);
    }

    boolean scroll(Minecraft mc, double delta) {
        if (!open || delta == 0.0D || selectedCell >= 0) return false;
        double mx = guiMouseX(mc);
        double my = guiMouseY(mc);
        int x = left(mc);
        int y = top(mc);
        if (!inside(mx, my, x + 170, y + 49, 135, 126)) return false;

        List<SpellComponentDefinition> definitions = selectorDefinitions();
        int max = selectorMaxScroll(definitions.size());
        int current = selectorScrollRows[selectorTab.ordinal()];
        current += delta > 0.0D ? -1 : 1;
        selectorScrollRows[selectorTab.ordinal()] = Math.max(0, Math.min(max, current));
        return true;
    }

    void render(GuiGraphics g, Minecraft mc) {
        if (!open) return;
        int x = left(mc);
        int y = top(mc);
        int mx = (int) guiMouseX(mc);
        int my = (int) guiMouseY(mc);

        g.fill(x, y, x + WIDTH, y + HEIGHT, 0xF0101826);
        panel(g, mc, x + 10, y + 10, 145, 184, "Permanent Spell");
        panel(g, mc, x + 165, y + 10, 145, 184,
                selectedCell >= 0 ? "Properties: " + definitionAt(selectedCell).displayName() : "Component Selector");

        renderWorkspace(g, mc, x, y);
        if (selectedCell >= 0) renderProperties(g, mc, x, y);
        else renderSelector(g, mc, x, y, mx, my);

        button(g, mc, x + 12, y + 202, 62, 18, "Close");
        g.drawString(mc.font, "Effects and Modifiers use the same settings as the Scribe Lectern.",
                x + 82, y + 207, 0xFF9FB2C9, false);

        int dragged = draggedType();
        if (dragged != SpellPresentation.TYPE_EMPTY) {
            ItemStack stack = componentStack(dragged);
            g.renderItem(stack, mx - 8, my - 8);
        }
    }

    private void renderWorkspace(GuiGraphics g, Minecraft mc, int x, int y) {
        for (int row = 0; row < SpellPresentation.ROWS; row++) {
            for (int col = 0; col < SpellPresentation.COLS; col++) {
                int cell = row * SpellPresentation.COLS + col;
                int sx = x + GRID_X + col * STEP;
                int sy = y + GRID_Y + row * STEP;
                g.fill(sx, sy, sx + CELL, sy + CELL, cell == selectedCell ? 0xFF4B7197 : 0x7030445E);
                int type = typeAt(cell);
                if (type != SpellPresentation.TYPE_EMPTY) g.renderItem(componentStack(type), sx, sy);
            }
        }
        g.drawString(mc.font, "Drag components into the grid.", x + 20, y + 148, 0xFFE8F2FF, false);
        g.drawString(mc.font, "Drag placed components to reorder.", x + 20, y + 160, 0xFFB8CCE0, false);
        g.drawString(mc.font, "Right-click removes a component.", x + 20, y + 172, 0xFFB8CCE0, false);
    }

    private void renderSelector(GuiGraphics g, Minecraft mc, int x, int y, int mx, int my) {
        tab(g, mc, x + 170, y + 30, 61, 16, "Effects", selectorTab == SelectorTab.EFFECTS);
        tab(g, mc, x + 233, y + 30, 72, 16, "Modifiers", selectorTab == SelectorTab.MODIFIERS);

        List<SpellComponentDefinition> definitions = selectorDefinitions();
        int scroll = selectorScrollRows[selectorTab.ordinal()];
        for (int index = 0; index < definitions.size(); index++) {
            int col = index % SELECTOR_COLS;
            int row = index / SELECTOR_COLS - scroll;
            if (row < 0 || row >= SELECTOR_VISIBLE_ROWS) continue;
            int sx = x + SELECTOR_START_X + col * SELECTOR_COL_STEP;
            int sy = y + SELECTOR_GRID_Y + row * SELECTOR_ROW_STEP;
            g.fill(sx, sy, sx + CELL, sy + CELL, 0xFF2A3B55);
            SpellComponentDefinition definition = definitions.get(index);
            g.renderItem(definition.createEditorIcon(), sx, sy);
            if (inside(mx, my, sx, sy, CELL, CELL)) {
                g.renderTooltip(mc.font, Component.literal(definition.displayName()), mx, my);
            }
        }
    }

    private void renderProperties(GuiGraphics g, Minecraft mc, int x, int y) {
        button(g, mc, x + 291, y + 15, 14, 12, "<");
        SpellComponentDefinition definition = definitionAt(selectedCell);
        if (definition == null) return;

        if (definition.settings().isEmpty()) {
            g.drawCenteredString(mc.font, "No settings", x + 237, y + 73, 0xFF9EB7CF);
            return;
        }

        if (openOptionsProperty != null) {
            SpellPropertyDefinition property = findSetting(definition, openOptionsProperty);
            if (property == null) {
                openOptionsProperty = null;
                return;
            }
            g.drawString(mc.font, property.label(), x + 174, y + 51, 0xFFD9E9F7, false);
            int count = property.maxValue() - property.minValue() + 1;
            for (int i = 0; i < count; i++) {
                int value = property.minValue() + i;
                int col = i % 2;
                int row = i / 2;
                int sx = x + 174 + col * 64;
                int sy = y + 66 + row * 18;
                boolean selected = propertyValue(selectedCell, property.key()) == value;
                g.fill(sx, sy, sx + 61, sy + 15, selected ? 0xFF36587A : 0xB02A3B55);
                g.drawCenteredString(mc.font, property.format(value), sx + 30, sy + 4, 0xFFE8F3FF);
            }
            return;
        }

        int rowY = y + 52;
        for (SpellPropertyDefinition property : definition.settings()) {
            int value = propertyValue(selectedCell, property.key());
            if (property.kind() == SpellPropertyKind.STEPPER) {
                g.drawString(mc.font, property.label() + ":", x + 175, rowY + 4, 0xFFD9E9F7, false);
                button(g, mc, x + 245, rowY, 16, 16, "<");
                g.drawCenteredString(mc.font, property.format(value), x + 272, rowY + 4, 0xFFE8F3FF);
                button(g, mc, x + 284, rowY, 16, 16, ">");
            } else {
                button(g, mc, x + 174, rowY, 126, 18,
                        property.label() + ": " + property.format(value));
            }
            rowY += 25;
        }
    }

    private boolean handlePropertyClick(double mx, double my, int x, int y) {
        SpellComponentDefinition definition = definitionAt(selectedCell);
        if (definition == null) return false;

        if (openOptionsProperty != null) {
            SpellPropertyDefinition property = findSetting(definition, openOptionsProperty);
            if (property == null) {
                openOptionsProperty = null;
                return true;
            }
            int count = property.maxValue() - property.minValue() + 1;
            for (int i = 0; i < count; i++) {
                int value = property.minValue() + i;
                int col = i % 2;
                int row = i / 2;
                int sx = x + 174 + col * 64;
                int sy = y + 66 + row * 18;
                if (inside(mx, my, sx, sy, 61, 15)) {
                    setProperty(selectedCell, property.key(), value);
                    openOptionsProperty = null;
                    return true;
                }
            }
            if (inside(mx, my, x + 165, y + 10, 145, 184)) {
                openOptionsProperty = null;
                return true;
            }
            return false;
        }

        int rowY = y + 52;
        for (SpellPropertyDefinition property : definition.settings()) {
            if (property.kind() == SpellPropertyKind.STEPPER) {
                if (inside(mx, my, x + 245, rowY, 16, 16)) {
                    setProperty(selectedCell, property.key(), propertyValue(selectedCell, property.key()) - 1);
                    return true;
                }
                if (inside(mx, my, x + 284, rowY, 16, 16)) {
                    setProperty(selectedCell, property.key(), propertyValue(selectedCell, property.key()) + 1);
                    return true;
                }
            } else if (inside(mx, my, x + 174, rowY, 126, 18)) {
                openOptionsProperty = property.key();
                return true;
            }
            rowY += 25;
        }
        return false;
    }

    private SpellComponentDefinition selectorComponentAt(double mx, double my, int x, int y) {
        List<SpellComponentDefinition> definitions = selectorDefinitions();
        int scroll = selectorScrollRows[selectorTab.ordinal()];
        for (int index = 0; index < definitions.size(); index++) {
            int col = index % SELECTOR_COLS;
            int row = index / SELECTOR_COLS - scroll;
            if (row < 0 || row >= SELECTOR_VISIBLE_ROWS) continue;
            int sx = x + SELECTOR_START_X + col * SELECTOR_COL_STEP;
            int sy = y + SELECTOR_GRID_Y + row * SELECTOR_ROW_STEP;
            if (inside(mx, my, sx, sy, CELL, CELL)) return definitions.get(index);
        }
        return null;
    }

    private List<SpellComponentDefinition> selectorDefinitions() {
        return selectorTab == SelectorTab.EFFECTS ? SpellComponents.effects() : SpellComponents.modifiers();
    }

    private int selectorMaxScroll(int count) {
        int rows = (count + SELECTOR_COLS - 1) / SELECTOR_COLS;
        return Math.max(0, rows - SELECTOR_VISIBLE_ROWS);
    }

    private int workspaceCellAt(double mx, double my, int x, int y) {
        for (int row = 0; row < SpellPresentation.ROWS; row++) {
            for (int col = 0; col < SpellPresentation.COLS; col++) {
                int sx = x + GRID_X + col * STEP;
                int sy = y + GRID_Y + row * STEP;
                if (inside(mx, my, sx, sy, CELL, CELL)) return row * SpellPresentation.COLS + col;
            }
        }
        return NO_CELL;
    }

    private int draggedType() {
        if (dragNewType != SpellPresentation.TYPE_EMPTY) return dragNewType;
        return dragSource >= 0 ? typeAt(dragSource) : SpellPresentation.TYPE_EMPTY;
    }

    private int typeAt(int cell) {
        return SpellPresentation.typeAt(plan, cell);
    }

    private SpellComponentDefinition definitionAt(int cell) {
        return SpellComponents.byType(typeAt(cell));
    }

    private int propertyValue(int cell, SpellPropertyKey key) {
        if (key == SpellPropertyKey.STYLE) return SpellPresentation.styleAt(plan, cell);
        if (key == SpellPropertyKey.VISUAL) return SpellPresentation.visualAt(plan, cell);
        return SpellPresentation.settingAt(settings, cell, key);
    }

    private void setProperty(int cell, SpellPropertyKey key, int value) {
        SpellComponentDefinition definition = definitionAt(cell);
        if (definition == null) return;
        SpellPropertyDefinition property = findSetting(definition, key);
        if (property == null) return;
        SpellPresentation.setSetting(settings, cell, key, Math.max(property.minValue(), Math.min(property.maxValue(), value)));
    }

    private SpellPropertyDefinition findSetting(SpellComponentDefinition definition, SpellPropertyKey key) {
        for (SpellPropertyDefinition property : definition.settings()) {
            if (property.key() == key) return property;
        }
        return null;
    }

    private void setCellType(int cell, int type) {
        SpellComponentDefinition definition = SpellComponents.byType(type);
        if (definition == null || definition.isShape()) return;
        resetCell(cell);
        SpellPresentation.setCell(plan, cell, type, definition.defaultStyle(), definition.defaultVisual());
        for (SpellPropertyDefinition property : definition.settings()) {
            SpellPresentation.setSetting(settings, cell, property.key(), property.defaultValue());
        }
    }

    private void clearCell(int cell) {
        resetCell(cell);
    }

    private void resetCell(int cell) {
        SpellPresentation.setCell(plan, cell, SpellPresentation.TYPE_EMPTY,
                SpellPresentation.STYLE_DEFAULT, SpellPresentation.VISUAL_DEFAULT);
        for (SpellPropertyKey key : SpellPropertyKey.values()) {
            if (key.isSetting()) SpellPresentation.setSetting(settings, cell, key, SpellPresentation.settingDefault(key));
        }
    }

    private void swapCells(int a, int b) {
        int typeA = typeAt(a);
        int typeB = typeAt(b);
        int styleA = SpellPresentation.styleAt(plan, a);
        int styleB = SpellPresentation.styleAt(plan, b);
        int visualA = SpellPresentation.visualAt(plan, a);
        int visualB = SpellPresentation.visualAt(plan, b);

        int[] settingsA = new int[SpellPropertyKey.SETTING_COUNT];
        int[] settingsB = new int[SpellPropertyKey.SETTING_COUNT];
        for (SpellPropertyKey key : SpellPropertyKey.values()) {
            if (!key.isSetting()) continue;
            settingsA[key.settingIndex()] = SpellPresentation.settingAt(settings, a, key);
            settingsB[key.settingIndex()] = SpellPresentation.settingAt(settings, b, key);
        }

        SpellPresentation.setCell(plan, a, typeB, styleB, visualB);
        SpellPresentation.setCell(plan, b, typeA, styleA, visualA);
        for (SpellPropertyKey key : SpellPropertyKey.values()) {
            if (!key.isSetting()) continue;
            SpellPresentation.setSetting(settings, a, key, settingsB[key.settingIndex()]);
            SpellPresentation.setSetting(settings, b, key, settingsA[key.settingIndex()]);
        }
    }

    private ItemStack componentStack(int type) {
        SpellComponentDefinition definition = SpellComponents.byType(type);
        return definition == null ? ItemStack.EMPTY : definition.createEditorIcon();
    }

    private int left(Minecraft mc) {
        return (mc.getWindow().getGuiScaledWidth() - WIDTH) / 2;
    }

    private int top(Minecraft mc) {
        return (mc.getWindow().getGuiScaledHeight() - HEIGHT) / 2;
    }

    private double guiMouseX(Minecraft mc) {
        double screenWidth = Math.max(1.0D, mc.getWindow().getScreenWidth());
        return mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / screenWidth;
    }

    private double guiMouseY(Minecraft mc) {
        double screenHeight = Math.max(1.0D, mc.getWindow().getScreenHeight());
        return mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / screenHeight;
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void panel(GuiGraphics g, Minecraft mc, int x, int y, int w, int h, String name) {
        g.fill(x, y, x + w, y + h, 0xC0202D42);
        g.renderOutline(x, y, w, h, 0xFF5B86B8);
        g.drawString(mc.font, name, x + 7, y + 6, 0xFFE8F3FF, false);
    }

    private void tab(GuiGraphics g, Minecraft mc, int x, int y, int w, int h, String text, boolean active) {
        g.fill(x, y, x + w, y + h, active ? 0xFF466B91 : 0xFF30445E);
        g.renderOutline(x, y, w, h, 0xFF5B86B8);
        g.drawCenteredString(mc.font, text, x + w / 2, y + 4, 0xFFE8F3FF);
    }

    private void button(GuiGraphics g, Minecraft mc, int x, int y, int w, int h, String text) {
        g.fill(x, y, x + w, y + h, 0xFF30445E);
        g.renderOutline(x, y, w, h, 0xFF5B86B8);
        g.drawCenteredString(mc.font, text, x + w / 2, y + 4, 0xFFE8F3FF);
    }
}
