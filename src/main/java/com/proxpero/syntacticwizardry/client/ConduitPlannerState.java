package com.proxpero.syntacticwizardry.client;

import com.arcane.magic.builder.BuilderInventoryPanel;
import com.arcane.magic.builder.BuilderVolumeSupport;
import com.arcane.magic.client.ArcaneBuilderClientEvents;
import com.arcane.magic.client.ArcaneBuilderEditEvents;
import com.proxpero.syntacticwizardry.RitualDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ConduitPlannerState {
    public enum AreaShape {
        BOX("Box"),
        SPHERE("Sphere");

        private final String displayName;
        AreaShape(String displayName) { this.displayName = displayName; }
        public String displayName() { return displayName; }
    }

    private static final int PANEL_X = 8;
    private static final int PANEL_Y = 8;
    private static final int PANEL_W = 176;
    private static final int ENTRY_H = 14;
    private static final int LIST_TOP = 28;
    private static final int LIST_BOTTOM_MARGIN = 28;

    private static boolean active;
    private static BlockPos conduitPos = BlockPos.ZERO;
    private static RitualDefinition ritual = RitualDefinition.PROTECTION;
    private static AreaShape areaShape = AreaShape.BOX;
    private static int listScroll;
    private static boolean leftWasDown;
    private static boolean uiConsumed;

    private static Method enterMethod;
    private static Method exitMethod;
    private static Field selectedField;
    private static Field modeField;
    private static Field selectedIdField;
    private static Field boxActiveField;
    private static Field selectingField;
    private static Field resizingField;
    private static Field minXField;
    private static Field minYField;
    private static Field minZField;
    private static Field maxXField;
    private static Field maxYField;
    private static Field maxZField;

    private ConduitPlannerState() {}

    public static boolean active() {
        return active;
    }

    public static void begin(BlockPos pos) {
        if (active || ArcaneBuilderClientEvents.active) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;

        clearSelection();
        forceBuilderSelectionMode();
        ritual = RitualDefinition.PROTECTION;
        areaShape = AreaShape.BOX;
        listScroll = 0;
        leftWasDown = false;
        uiConsumed = false;
        conduitPos = pos.immutable();

        try {
            if (enterMethod == null) {
                enterMethod = ArcaneBuilderClientEvents.class.getDeclaredMethod("enter", BlockPos.class);
                enterMethod.setAccessible(true);
            }
            enterMethod.invoke(null, conduitPos);
            active = ArcaneBuilderClientEvents.active;
        } catch (Throwable ignored) {
            active = false;
        }
    }

    public static void tick(ClientTickEvent.Post event) {
        if (!active) return;
        if (!ArcaneBuilderClientEvents.active) {
            deactivate(false);
            return;
        }

        forceBuilderSelectionMode();

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;

        long window = mc.getWindow().getWindow();
        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        double mouseX = guiMouseX(mc);
        double mouseY = guiMouseY(mc);

        if (leftDown && !leftWasDown && overPlannerUi(mc, mouseX, mouseY)) {
            uiConsumed = true;
            handleUiClick(mc, mouseX, mouseY);
        }

        if (!leftDown) uiConsumed = false;
        leftWasDown = leftDown;

        if (!ritual.variableArea()) {
            if (!selection().isEmpty()) clearSelection();
            return;
        }

        if (!uiConsumed) BuilderVolumeSupport.clientTick(event);
    }

    public static void render(GuiGraphics graphics) {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        double mouseX = guiMouseX(mc);
        double mouseY = guiMouseY(mc);

        int bottom = height - 8;
        graphics.fill(PANEL_X, PANEL_Y, PANEL_X + PANEL_W, bottom, 0xC0182232);
        graphics.renderOutline(PANEL_X, PANEL_Y, PANEL_W, bottom - PANEL_Y, 0xFF8E72C7);
        graphics.drawString(mc.font, "Rituals", PANEL_X + 7, PANEL_Y + 7, 0xFFF0E8FF, false);

        List<RitualDefinition> entries = RitualDefinition.entries();
        int visible = visibleRows(height);
        int maxScroll = Math.max(0, entries.size() - visible);
        listScroll = Math.max(0, Math.min(maxScroll, listScroll));

        for (int row = 0; row < visible; row++) {
            int index = listScroll + row;
            if (index >= entries.size()) break;
            RitualDefinition entry = entries.get(index);
            int y = LIST_TOP + row * ENTRY_H;
            boolean selected = entry == ritual;
            boolean hovered = mouseX >= PANEL_X + 4 && mouseX < PANEL_X + PANEL_W - 4
                    && mouseY >= y && mouseY < y + ENTRY_H - 1;
            int fill = selected ? 0xD05D4A86 : hovered ? 0xB03B465D : 0x90252F42;
            graphics.fill(PANEL_X + 4, y, PANEL_X + PANEL_W - 4, y + ENTRY_H - 1, fill);
            graphics.drawString(mc.font, entry.displayName(), PANEL_X + 8, y + 3,
                    selected ? 0xFFFFFFFF : 0xFFD7DDEA, false);
        }

        if (maxScroll > 0) {
            String scroll = (listScroll + 1) + "-" + Math.min(entries.size(), listScroll + visible)
                    + " / " + entries.size();
            graphics.drawString(mc.font, scroll, PANEL_X + 7, bottom - 13, 0xFF9FAEC4, false);
        }

        renderAreaPanel(graphics, mc, width);
        renderInstructions(graphics, mc, width, height);
    }

    public static boolean scrollRitualList(double delta) {
        if (!active || delta == 0.0D) return false;
        Minecraft mc = Minecraft.getInstance();
        double mouseX = guiMouseX(mc);
        double mouseY = guiMouseY(mc);
        int height = mc.getWindow().getGuiScaledHeight();

        if (!inside(mouseX, mouseY, PANEL_X, PANEL_Y, PANEL_W, height - 16)) return false;
        if (net.minecraft.client.gui.screens.Screen.hasControlDown()) return false;

        int max = Math.max(0, RitualDefinition.entries().size() - visibleRows(height));
        if (delta > 0.0D) listScroll--;
        else listScroll++;
        listScroll = Math.max(0, Math.min(max, listScroll));
        return true;
    }

    public static boolean renderBuilderSelection(Object renderEvent) {
        if (!active || !ritual.variableArea()) return false;
        return BuilderVolumeSupport.renderSelection(renderEvent);
    }

    public static RitualDefinition ritual() {
        return ritual;
    }

    public static AreaShape areaShape() {
        return areaShape;
    }

    public static BlockPos conduitPos() {
        return conduitPos;
    }

    private static void handleUiClick(Minecraft mc, double mouseX, double mouseY) {
        RitualDefinition clicked = ritualAt(mc, mouseX, mouseY);
        if (clicked != null) {
            if (clicked != ritual) {
                ritual = clicked;
                clearSelection();
            }
            return;
        }

        if (!ritual.variableArea()) return;
        int width = mc.getWindow().getGuiScaledWidth();
        int x = width - 178;
        int y = 8;

        if (inside(mouseX, mouseY, x + 7, y + 24, 70, 18)) {
            areaShape = AreaShape.BOX;
        } else if (inside(mouseX, mouseY, x + 82, y + 24, 80, 18)) {
            areaShape = AreaShape.SPHERE;
        } else if (inside(mouseX, mouseY, x + 7, y + 47, 70, 18)) {
            clearSelection();
        }
    }

    private static RitualDefinition ritualAt(Minecraft mc, double mouseX, double mouseY) {
        int height = mc.getWindow().getGuiScaledHeight();
        if (mouseX < PANEL_X + 4 || mouseX >= PANEL_X + PANEL_W - 4) return null;
        if (mouseY < LIST_TOP || mouseY >= LIST_TOP + visibleRows(height) * ENTRY_H) return null;
        int row = (int)((mouseY - LIST_TOP) / ENTRY_H);
        int index = listScroll + row;
        List<RitualDefinition> entries = RitualDefinition.entries();
        return index >= 0 && index < entries.size() ? entries.get(index) : null;
    }

    private static void renderAreaPanel(GuiGraphics graphics, Minecraft mc, int width) {
        int x = width - 178;
        int y = 8;
        int w = 170;
        int h = ritual.variableArea() ? 92 : 50;
        graphics.fill(x, y, x + w, y + h, 0xC0182232);
        graphics.renderOutline(x, y, w, h, 0xFF8E72C7);
        graphics.drawString(mc.font, ritual.displayName(), x + 7, y + 7, 0xFFF0E8FF, false);

        if (!ritual.variableArea()) {
            graphics.drawString(mc.font, "Fixed / no local area", x + 7, y + 25, 0xFFB9C5D6, false);
            return;
        }

        drawButton(graphics, mc, x + 7, y + 24, 70, 18, "Box", areaShape == AreaShape.BOX);
        drawButton(graphics, mc, x + 82, y + 24, 80, 18, "Sphere", areaShape == AreaShape.SPHERE);
        drawButton(graphics, mc, x + 7, y + 47, 70, 18, "Clear", false);

        int[] bounds = bounds();
        if (bounds == null) {
            graphics.drawString(mc.font, "No area selected", x + 82, y + 52, 0xFFB9C5D6, false);
            return;
        }

        if (areaShape == AreaShape.BOX) {
            int sx = bounds[3] - bounds[0] + 1;
            int sy = bounds[4] - bounds[1] + 1;
            int sz = bounds[5] - bounds[2] + 1;
            graphics.drawString(mc.font, sx + " x " + sy + " x " + sz, x + 82, y + 52, 0xFFDCE6F3, false);
        } else {
            graphics.drawString(mc.font,
                    "Radius " + String.format(Locale.ROOT, "%.1f", sphereRadius(bounds)),
                    x + 82, y + 52, 0xFFDCE6F3, false);
        }

        int sx = bounds[3] - bounds[0] + 1;
        int sy = bounds[4] - bounds[1] + 1;
        int sz = bounds[5] - bounds[2] + 1;
        long volume = (long)sx * sy * sz;
        graphics.drawString(mc.font, volume + " block volume", x + 7, y + 72, 0xFF9FAEC4, false);
    }

    private static void renderInstructions(GuiGraphics graphics, Minecraft mc, int width, int height) {
        String text = ritual.variableArea()
                ? "LMB drag: area   Drag handles: resize   MMB: pan   Ctrl+RMB: rotate   Ctrl+wheel: zoom   Shift: exit"
                : "MMB: pan   Ctrl+RMB: rotate   Ctrl+wheel: zoom   Shift: exit";
        int y = height - 18;
        int x = Math.max(PANEL_X + PANEL_W + 8, (width - mc.font.width(text)) / 2);
        int right = Math.min(width - 6, x + mc.font.width(text) + 5);
        graphics.fill(x - 5, y - 3, right, y + 11, 0xA0101622);
        graphics.drawString(mc.font, text, x, y, 0xFFD2DCEB, false);
    }

    private static void drawButton(
            GuiGraphics graphics,
            Minecraft mc,
            int x,
            int y,
            int w,
            int h,
            String label,
            boolean selected) {
        graphics.fill(x, y, x + w, y + h, selected ? 0xD05D4A86 : 0xB02B3548);
        graphics.renderOutline(x, y, w, h, selected ? 0xFFD2B7FF : 0xFF66738A);
        graphics.drawCenteredString(mc.font, label, x + w / 2, y + 5, 0xFFFFFFFF);
    }

    private static boolean overPlannerUi(Minecraft mc, double mouseX, double mouseY) {
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        if (inside(mouseX, mouseY, PANEL_X, PANEL_Y, PANEL_W, height - 16)) return true;
        return inside(mouseX, mouseY, width - 178, 8, 170, ritual.variableArea() ? 92 : 50);
    }

    private static int visibleRows(int height) {
        return Math.max(5, (height - LIST_TOP - LIST_BOTTOM_MARGIN) / ENTRY_H);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static double guiMouseX(Minecraft mc) {
        double screenWidth = Math.max(1.0D, mc.getWindow().getScreenWidth());
        return mc.mouseHandler.xpos() * mc.getWindow().getGuiScaledWidth() / screenWidth;
    }

    private static double guiMouseY(Minecraft mc) {
        double screenHeight = Math.max(1.0D, mc.getWindow().getScreenHeight());
        return mc.mouseHandler.ypos() * mc.getWindow().getGuiScaledHeight() / screenHeight;
    }

    private static double sphereRadius(int[] b) {
        double cx = conduitPos.getX() + 0.5D;
        double cy = conduitPos.getY() + 0.5D;
        double cz = conduitPos.getZ() + 0.5D;

        double dx = Math.max(Math.abs(b[0] - cx), Math.abs((b[3] + 1.0D) - cx));
        double dy = Math.max(Math.abs(b[1] - cy), Math.abs((b[4] + 1.0D) - cy));
        double dz = Math.max(Math.abs(b[2] - cz), Math.abs((b[5] + 1.0D) - cz));
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static int[] bounds() {
        try {
            if (selection().isEmpty()) return null;
            return new int[]{
                    intField("minX").getInt(null),
                    intField("minY").getInt(null),
                    intField("minZ").getInt(null),
                    intField("maxX").getInt(null),
                    intField("maxY").getInt(null),
                    intField("maxZ").getInt(null)
            };
        } catch (Throwable ignored) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Set<Long> selection() {
        try {
            if (selectedField == null) {
                selectedField = ArcaneBuilderEditEvents.class.getDeclaredField("SELECTED");
                selectedField.setAccessible(true);
            }
            return (Set<Long>) selectedField.get(null);
        } catch (Throwable ignored) {
            return java.util.Collections.emptySet();
        }
    }

    private static void clearSelection() {
        try {
            selection().clear();
            boolField("boxActive").setBoolean(null, false);
            boolField("selecting").setBoolean(null, false);
            boolField("resizing").setBoolean(null, false);
        } catch (Throwable ignored) {
        }
    }

    private static void forceBuilderSelectionMode() {
        try {
            if (modeField == null) {
                modeField = ArcaneBuilderEditEvents.class.getDeclaredField("mode");
                modeField.setAccessible(true);
            }
            modeField.setInt(null, 0);

            if (selectedIdField == null) {
                selectedIdField = BuilderInventoryPanel.class.getDeclaredField("selectedId");
                selectedIdField.setAccessible(true);
            }
            selectedIdField.set(null, null);
        } catch (Throwable ignored) {
        }
    }

    private static Field intField(String name) throws Exception {
        Field existing = switch (name) {
            case "minX" -> minXField;
            case "minY" -> minYField;
            case "minZ" -> minZField;
            case "maxX" -> maxXField;
            case "maxY" -> maxYField;
            case "maxZ" -> maxZField;
            default -> null;
        };
        if (existing != null) return existing;

        Field field = BuilderVolumeSupport.class.getDeclaredField(name);
        field.setAccessible(true);
        switch (name) {
            case "minX" -> minXField = field;
            case "minY" -> minYField = field;
            case "minZ" -> minZField = field;
            case "maxX" -> maxXField = field;
            case "maxY" -> maxYField = field;
            case "maxZ" -> maxZField = field;
        }
        return field;
    }

    private static Field boolField(String name) throws Exception {
        if ("boxActive".equals(name) && boxActiveField != null) return boxActiveField;
        if ("selecting".equals(name) && selectingField != null) return selectingField;
        if ("resizing".equals(name) && resizingField != null) return resizingField;

        Field field = BuilderVolumeSupport.class.getDeclaredField(name);
        field.setAccessible(true);
        if ("boxActive".equals(name)) boxActiveField = field;
        else if ("selecting".equals(name)) selectingField = field;
        else if ("resizing".equals(name)) resizingField = field;
        return field;
    }

    private static void deactivate(boolean callBuilderExit) {
        if (callBuilderExit && ArcaneBuilderClientEvents.active) {
            try {
                if (exitMethod == null) {
                    exitMethod = ArcaneBuilderClientEvents.class.getDeclaredMethod("exit");
                    exitMethod.setAccessible(true);
                }
                exitMethod.invoke(null);
            } catch (Throwable ignored) {
            }
        }

        active = false;
        leftWasDown = false;
        uiConsumed = false;
        clearSelection();
    }
}
