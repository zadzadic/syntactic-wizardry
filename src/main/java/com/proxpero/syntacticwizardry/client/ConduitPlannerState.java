package com.proxpero.syntacticwizardry.client;

import com.arcane.magic.builder.BuilderInventoryPanel;
import com.arcane.magic.builder.BuilderVolumeSupport;
import com.arcane.magic.client.ArcaneBuilderClientEvents;
import com.arcane.magic.client.ArcaneBuilderEditEvents;
import com.proxpero.syntacticwizardry.RitualDefinition;
import com.proxpero.syntacticwizardry.SpellComponentDefinition;
import com.proxpero.syntacticwizardry.SpellComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ConduitPlannerState {
    private enum ViewMode { MANAGEMENT, PLANNER }

    public enum AreaShape {
        BOX("Box"),
        SPHERE("Sphere"),
        CYLINDER("Cylinder");

        private final String displayName;
        AreaShape(String displayName) { this.displayName = displayName; }
        public String displayName() { return displayName; }
    }

    private static final int PANEL_X = 8;
    private static final int PANEL_Y = 8;
    private static final int PANEL_W = 176;
    private static final int ENTRY_H = 14;
    private static final int LIST_TOP = 28;
    private static final int LIST_BOTTOM_MARGIN = 52;
    private static final int MANAGEMENT_PANEL_W = 310;
    private static final int ACTIVE_LIST_TOP = 34;
    private static final int ACTIVE_ROW_H = 26;
    private static final int ACTIVE_BOTTOM_MARGIN = 18;

    private static boolean active;
    private static ViewMode viewMode = ViewMode.MANAGEMENT;
    private static BlockPos conduitPos = BlockPos.ZERO;
    private static RitualDefinition ritual = RitualDefinition.PROTECTION;
    private static AreaShape areaShape = AreaShape.BOX;
    private static int listScroll;
    private static int activeScroll;
    private static boolean leftWasDown;
    private static boolean rightWasDown;
    private static boolean uiConsumed;

    private static boolean movingArea;
    private static int moveAxis = -1;
    private static double moveStartParam;
    private static int[] moveStartBounds;

    private static boolean rotatingFacing;
    private static double facingDragStartX;
    private static double facingDragStartY;
    private static float facingStartYaw;
    private static float facingStartPitch;
    private static float facingYaw;
    private static float facingPitch;

    private static final LinkedHashSet<Integer> permanentEffects = new LinkedHashSet<>();
    private static final LinkedHashSet<Integer> permanentModifiers = new LinkedHashSet<>();
    private static PermanentSpellEditor permanentSpellEditor = new PermanentSpellEditor();

    private static Method enterMethod;
    private static Method exitMethod;
    private static Method cursorRayMethod;
    private static Method handleHitMethod;
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

    public static RitualDefinition ritual() {
        return ritual;
    }

    public static AreaShape areaShape() {
        return areaShape;
    }

    public static BlockPos conduitPos() {
        return conduitPos;
    }

    public static void begin(BlockPos pos) {
        if (active || ArcaneBuilderClientEvents.active) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;

        clearSelection();
        forceBuilderSelectionMode();
        ritual = RitualDefinition.PROTECTION;
        areaShape = AreaShape.BOX;
        viewMode = ViewMode.MANAGEMENT;
        listScroll = 0;
        activeScroll = 0;
        leftWasDown = false;
        rightWasDown = false;
        uiConsumed = false;
        movingArea = false;
        rotatingFacing = false;
        moveAxis = -1;
        facingYaw = 0.0F;
        facingPitch = 0.0F;
        permanentEffects.clear();
        permanentModifiers.clear();
        permanentSpellEditor = new PermanentSpellEditor();
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

        if (permanentSpellEditor.isOpen()) {
            permanentSpellEditor.tick(mc);
            long editorWindow = mc.getWindow().getWindow();
            leftWasDown = GLFW.glfwGetMouseButton(editorWindow, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            rightWasDown = GLFW.glfwGetMouseButton(editorWindow, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
            return;
        }

        long window = mc.getWindow().getWindow();
        boolean leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rightDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        boolean justPressed = leftDown && !leftWasDown;
        boolean rightJustPressed = rightDown && !rightWasDown;
        double rawX = mc.mouseHandler.xpos();
        double rawY = mc.mouseHandler.ypos();
        double mouseX = guiMouseX(mc);
        double mouseY = guiMouseY(mc);

        if (viewMode == ViewMode.MANAGEMENT) {
            tickManagement(mc, justPressed, rightJustPressed, mouseX, mouseY);
            leftWasDown = leftDown;
            rightWasDown = rightDown;
            return;
        }

        if (justPressed && overPlannerUi(mc, mouseX, mouseY)) {
            uiConsumed = true;
            handleUiClick(mc, mouseX, mouseY);
        }

        boolean gizmoConsumed = false;
        if (!uiConsumed && ritual.variableArea()) {
            gizmoConsumed = handleGizmos(mc, leftDown, justPressed, rawX, rawY);
        }

        if (!ritual.variableArea()) {
            if (!selection().isEmpty()) clearSelection();
        } else if (!uiConsumed && !gizmoConsumed
                && shouldRunBuilderVolume(mc, leftDown, justPressed, rawX, rawY)) {
            int[] before = bounds();
            BuilderVolumeSupport.clientTick(event);
            int[] after = bounds();
            if (after != null && !Arrays.equals(before, after)) normalizeShapeBounds(before, after);
        }

        if (!leftDown) {
            uiConsumed = false;
            movingArea = false;
            rotatingFacing = false;
            moveAxis = -1;
        }
        leftWasDown = leftDown;
        rightWasDown = rightDown;
    }

    public static void render(GuiGraphics graphics) {
        if (!active) return;
        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int mouseX = (int) guiMouseX(mc);
        int mouseY = (int) guiMouseY(mc);

        if (viewMode == ViewMode.MANAGEMENT) {
            renderManagement(graphics, mc, width, height, mouseX, mouseY);
            return;
        }

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
        if (ritual == RitualDefinition.PERMANENCY) {
            renderPermanencyPanel(graphics, mc, width, height, mouseX, mouseY);
        }
        renderPlannerControls(graphics, mc, width, height);
        renderInstructions(graphics, mc, width, height);
        if (permanentSpellEditor.isOpen()) permanentSpellEditor.render(graphics, mc);
    }

    public static boolean scrollRitualList(double delta) {
        if (!active || delta == 0.0D) return false;
        Minecraft mc = Minecraft.getInstance();
        if (permanentSpellEditor.isOpen() && permanentSpellEditor.scroll(mc, delta)) return true;
        double mouseX = guiMouseX(mc);
        double mouseY = guiMouseY(mc);
        int height = mc.getWindow().getGuiScaledHeight();

        if (viewMode == ViewMode.MANAGEMENT) {
            if (!inside(mouseX, mouseY, PANEL_X, PANEL_Y, MANAGEMENT_PANEL_W, height - 16)) return false;
            int max = Math.max(0, ActiveRitualClientRegistry.entries().size() - activeVisibleRows(height));
            if (delta > 0.0D) activeScroll--;
            else activeScroll++;
            activeScroll = Math.max(0, Math.min(max, activeScroll));
            return max > 0;
        }

        if (!inside(mouseX, mouseY, PANEL_X, PANEL_Y, PANEL_W, height - 16)) return false;
        if (net.minecraft.client.gui.screens.Screen.hasControlDown()) return false;

        int max = Math.max(0, RitualDefinition.entries().size() - visibleRows(height));
        if (delta > 0.0D) listScroll--;
        else listScroll++;
        listScroll = Math.max(0, Math.min(max, listScroll));
        return true;
    }

    public static boolean renderBuilderSelection(Object renderEvent) {
        if (!active || viewMode != ViewMode.PLANNER || !ritual.variableArea()) return false;
        int[] b = bounds();
        if (b == null) return false;
        if (!(renderEvent instanceof net.neoforged.neoforge.client.event.RenderLevelStageEvent event)) return false;
        if (areaShape == AreaShape.BOX) {
            boolean rendered = BuilderVolumeSupport.renderSelection(renderEvent);
            ConduitAreaRender.renderGizmos(event, b, permanencyUsesFacing(), facingYaw, facingPitch);
            return rendered;
        }
        return ConduitAreaRender.render(event, areaShape, b, true, permanencyUsesFacing(), facingYaw, facingPitch);
    }

    public static void commitVirtualBounds() {
        int[] b = rawBounds();
        if (b == null) return;
        Set<Long> selected = selection();
        try {
            selected.clear();
            addCorner(selected, b[0], b[1], b[2]);
            addCorner(selected, b[0], b[1], b[5]);
            addCorner(selected, b[0], b[4], b[2]);
            addCorner(selected, b[0], b[4], b[5]);
            addCorner(selected, b[3], b[1], b[2]);
            addCorner(selected, b[3], b[1], b[5]);
            addCorner(selected, b[3], b[4], b[2]);
            addCorner(selected, b[3], b[4], b[5]);
            boolField("boxActive").setBoolean(null, true);
        } catch (Throwable ignored) {
        }
    }

    public static int[] bounds() {
        try {
            boolean boxActive = boolField("boxActive").getBoolean(null);
            if (!boxActive && selection().isEmpty()) return null;
            return rawBounds();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static int[] rawBounds() {
        try {
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

    private static void handleUiClick(Minecraft mc, double mouseX, double mouseY) {
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        if (inside(mouseX, mouseY, PANEL_X + 7, height - 38, 72, 22)) {
            viewMode = ViewMode.MANAGEMENT;
            permanentSpellEditor.close();
            clearSelection();
            return;
        }
        if (inside(mouseX, mouseY, width - 178, height - 38, 170, 22)) {
            PreparedRitualPreview.prepare(ritual, conduitPos);
            return;
        }

        RitualDefinition clicked = ritualAt(mc, mouseX, mouseY);
        if (clicked != null) {
            if (clicked != ritual) {
                ritual = clicked;
                permanentSpellEditor.close();
                clearSelection();
                movingArea = false;
                rotatingFacing = false;
            }
            return;
        }

        if (!ritual.variableArea()) {
            if (ritual == RitualDefinition.PERMANENCY) handlePermanencyClick(mc, mouseX, mouseY);
            return;
        }
        int x = width - 178;
        int y = 8;

        if (inside(mouseX, mouseY, x + 7, y + 24, 48, 18)) {
            setAreaShape(AreaShape.BOX);
            return;
        }
        if (inside(mouseX, mouseY, x + 59, y + 24, 48, 18)) {
            setAreaShape(AreaShape.SPHERE);
            return;
        }
        if (inside(mouseX, mouseY, x + 111, y + 24, 52, 18)) {
            setAreaShape(AreaShape.CYLINDER);
            return;
        }
        if (inside(mouseX, mouseY, x + 7, y + 47, 70, 18)) {
            clearSelection();
            return;
        }

        if (ritual == RitualDefinition.PERMANENCY) handlePermanencyClick(mc, mouseX, mouseY);
    }

    private static void setAreaShape(AreaShape shape) {
        areaShape = shape;
        int[] b = bounds();
        if (b != null) normalizeShapeBounds(null, b);
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

        drawButton(graphics, mc, x + 7, y + 24, 48, 18, "Box", areaShape == AreaShape.BOX);
        drawButton(graphics, mc, x + 59, y + 24, 48, 18, "Sphere", areaShape == AreaShape.SPHERE);
        drawButton(graphics, mc, x + 111, y + 24, 52, 18, "Cylinder", areaShape == AreaShape.CYLINDER);
        drawButton(graphics, mc, x + 7, y + 47, 70, 18, "Clear", false);

        int[] b = bounds();
        if (b == null) {
            graphics.drawString(mc.font, "No area selected", x + 82, y + 52, 0xFFB9C5D6, false);
            return;
        }

        int sx = b[3] - b[0] + 1;
        int sy = b[4] - b[1] + 1;
        int sz = b[5] - b[2] + 1;
        if (areaShape == AreaShape.SPHERE) {
            graphics.drawString(mc.font, "Diameter " + sx, x + 82, y + 52, 0xFFDCE6F3, false);
        } else if (areaShape == AreaShape.CYLINDER) {
            graphics.drawString(mc.font, "D " + sx + "  H " + sy, x + 82, y + 52, 0xFFDCE6F3, false);
        } else {
            graphics.drawString(mc.font, sx + " x " + sy + " x " + sz, x + 82, y + 52, 0xFFDCE6F3, false);
        }

        graphics.drawString(mc.font, "Drag center axes to move", x + 7, y + 72, 0xFF9FAEC4, false);
    }

    private static void renderPermanencyPanel(GuiGraphics graphics, Minecraft mc, int width, int height,
                                              int mouseX, int mouseY) {
        int x = width - 178;
        int y = 104;
        int w = 170;
        int h = 72;
        graphics.fill(x, y, x + w, y + h, 0xC0182232);
        graphics.renderOutline(x, y, w, h, 0xFF8E72C7);
        graphics.drawString(mc.font, "Permanent Spell", x + 7, y + 6, 0xFFF0E8FF, false);
        graphics.drawString(mc.font, "Effects + Modifiers", x + 7, y + 23, 0xFFB9C5D6, false);
        drawButton(graphics, mc, x + 7, y + 40, 156, 20, "Edit Permanent Spell", false);
        if (permanencyUsesFacing()) {
            graphics.drawString(mc.font, "Facing uses the gold world gizmo.", x + 7, y + 62, 0xFFFFD88A, false);
        }
    }

    private static void renderComponentGrid(GuiGraphics graphics, Minecraft mc,
                                            List<SpellComponentDefinition> definitions,
                                            Set<Integer> selected,
                                            int x, int y, int mouseX, int mouseY) {
        for (int i = 0; i < definitions.size(); i++) {
            SpellComponentDefinition definition = definitions.get(i);
            int col = i % 7;
            int row = i / 7;
            int sx = x + col * 22;
            int sy = y + row * 22;
            boolean on = selected.contains(definition.typeId());
            graphics.fill(sx - 1, sy - 1, sx + 18, sy + 18, on ? 0xD05D4A86 : 0x90252F42);
            if (on) graphics.renderOutline(sx - 1, sy - 1, 19, 19, 0xFFD2B7FF);
            graphics.renderItem(definition.createEditorIcon(), sx, sy);
            if (inside(mouseX, mouseY, sx - 1, sy - 1, 19, 19)) {
                graphics.renderTooltip(mc.font, Component.literal(definition.displayName()), mouseX, mouseY);
            }
        }
    }

    private static void handlePermanencyClick(Minecraft mc, double mouseX, double mouseY) {
        int width = mc.getWindow().getGuiScaledWidth();
        int x = width - 178;
        int y = 104;
        if (inside(mouseX, mouseY, x + 7, y + 40, 156, 20)) {
            permanentSpellEditor.open();
        }
    }

    private static SpellComponentDefinition componentAt(List<SpellComponentDefinition> definitions,
                                                        int x, int y, double mouseX, double mouseY) {
        for (int i = 0; i < definitions.size(); i++) {
            int sx = x + (i % 7) * 22;
            int sy = y + (i / 7) * 22;
            if (inside(mouseX, mouseY, sx - 1, sy - 1, 19, 19)) return definitions.get(i);
        }
        return null;
    }

    private static void toggle(Set<Integer> set, int value) {
        if (!set.remove(value)) set.add(value);
    }

    private static boolean permanencyUsesFacing() {
        return ritual == RitualDefinition.PERMANENCY
                && (permanentSpellEditor.hasType(SpellComponents.TYPE_MOVE)
                || permanentSpellEditor.hasType(SpellComponents.TYPE_TELEPORTATION));
    }

    private static void renderInstructions(GuiGraphics graphics, Minecraft mc, int width, int height) {
        String text = ritual.variableArea()
                ? "LMB: select/resize   Center axes: move area   MMB: pan   Ctrl+RMB: rotate view   Ctrl+wheel: zoom   Shift: exit"
                : "MMB: pan   Ctrl+RMB: rotate view   Ctrl+wheel: zoom   Shift: exit";
        if (permanencyUsesFacing()) {
            text = "LMB: select/resize   Center axes: move area   Gold arrow: rotate facing   Shift: exit";
        }
        int y = height - 18;
        int x = Math.max(PANEL_X + PANEL_W + 8, (width - mc.font.width(text)) / 2);
        int right = Math.min(width - 6, x + mc.font.width(text) + 5);
        graphics.fill(x - 5, y - 3, right, y + 11, 0xA0101622);
        graphics.drawString(mc.font, text, x, y, 0xFFD2DCEB, false);
    }

    private static void drawButton(GuiGraphics graphics, Minecraft mc, int x, int y, int w, int h,
                                   String label, boolean selected) {
        graphics.fill(x, y, x + w, y + h, selected ? 0xD05D4A86 : 0xB02B3548);
        graphics.renderOutline(x, y, w, h, selected ? 0xFFD2B7FF : 0xFF66738A);
        graphics.drawCenteredString(mc.font, label, x + w / 2, y + 5, 0xFFFFFFFF);
    }

    private static void tickManagement(Minecraft mc, boolean leftPressed, boolean rightPressed,
                                       double mouseX, double mouseY) {
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        if (leftPressed && inside(mouseX, mouseY, width - 138, 12, 126, 24)) {
            enterPlanner();
            return;
        }

        if (leftPressed
                && PreparedRitualPreview.prepared()
                && inside(mouseX, mouseY, width - 138, 80, 126, 22)) {
            PreparedRitualPreview.clear();
            return;
        }

        ActiveRitualClientRegistry.Entry entry = activeRitualAt(height, mouseX, mouseY);
        if (entry == null) return;

        int rowY = activeRowY(height, entry);
        if (rightPressed) {
            mc.setScreen(new ArmillaryRenameScreen(null, entry.name(),
                    value -> ActiveRitualClientRegistry.rename(entry.id(), value)));
            return;
        }

        if (!leftPressed) return;
        if (inside(mouseX, mouseY, PANEL_X + 196, rowY + 4, 52, 18)) {
            ActiveRitualClientRegistry.setPaused(entry.id(), !entry.paused());
            return;
        }
        if (inside(mouseX, mouseY, PANEL_X + 252, rowY + 4, 48, 18)) {
            ActiveRitualClientRegistry.stop(entry.id());
            return;
        }

        focusCamera(entry.center());
    }

    private static void enterPlanner() {
        viewMode = ViewMode.PLANNER;
        ritual = RitualDefinition.PROTECTION;
        areaShape = AreaShape.BOX;
        listScroll = 0;
        clearSelection();
        movingArea = false;
        rotatingFacing = false;
        moveAxis = -1;
        permanentSpellEditor.close();
        focusCamera(conduitPos);
    }

    private static void focusCamera(BlockPos center) {
        if (center == null) return;
        ArcaneBuilderClientEvents.targetX = center.getX() + 0.5D;
        ArcaneBuilderClientEvents.targetY = center.getY() + 0.5D;
        ArcaneBuilderClientEvents.targetZ = center.getZ() + 0.5D;
    }

    private static void renderManagement(GuiGraphics graphics, Minecraft mc, int width, int height,
                                         int mouseX, int mouseY) {
        int bottom = height - 8;
        graphics.fill(PANEL_X, PANEL_Y, PANEL_X + MANAGEMENT_PANEL_W, bottom, 0xC0182232);
        graphics.renderOutline(PANEL_X, PANEL_Y, MANAGEMENT_PANEL_W, bottom - PANEL_Y, 0xFF8E72C7);
        graphics.drawString(mc.font, "Active Rituals", PANEL_X + 8, PANEL_Y + 8, 0xFFF0E8FF, false);

        List<ActiveRitualClientRegistry.Entry> entries = ActiveRitualClientRegistry.entries();
        int visible = activeVisibleRows(height);
        int maxScroll = Math.max(0, entries.size() - visible);
        activeScroll = Math.max(0, Math.min(maxScroll, activeScroll));

        if (entries.isEmpty()) {
            graphics.drawString(mc.font, "No active rituals.", PANEL_X + 10, ACTIVE_LIST_TOP + 8, 0xFF9FAEC4, false);
        }

        for (int row = 0; row < visible; row++) {
            int index = activeScroll + row;
            if (index >= entries.size()) break;
            ActiveRitualClientRegistry.Entry entry = entries.get(index);
            int y = ACTIVE_LIST_TOP + row * ACTIVE_ROW_H;
            boolean hovered = inside(mouseX, mouseY, PANEL_X + 4, y, MANAGEMENT_PANEL_W - 12, ACTIVE_ROW_H - 2);
            graphics.fill(PANEL_X + 4, y, PANEL_X + MANAGEMENT_PANEL_W - 8, y + ACTIVE_ROW_H - 2,
                    hovered ? 0xB03B465D : 0x90252F42);
            String name = mc.font.plainSubstrByWidth(entry.name(), 174);
            graphics.drawString(mc.font, name, PANEL_X + 9, y + 5, 0xFFFFFFFF, false);
            graphics.drawString(mc.font, entry.paused() ? "Paused" : entry.ritual().displayName(),
                    PANEL_X + 9, y + 15, entry.paused() ? 0xFFFFD88A : 0xFF9FAEC4, false);
            drawButton(graphics, mc, PANEL_X + 196, y + 4, 52, 18, entry.paused() ? "Resume" : "Pause", false);
            drawButton(graphics, mc, PANEL_X + 252, y + 4, 48, 18, "Stop", false);
        }

        if (maxScroll > 0) {
            int trackX = PANEL_X + MANAGEMENT_PANEL_W - 7;
            int trackY = ACTIVE_LIST_TOP;
            int trackH = Math.max(24, visible * ACTIVE_ROW_H - 2);
            graphics.fill(trackX, trackY, trackX + 4, trackY + trackH, 0xFF1C293A);
            int thumbH = Math.max(12, trackH * visible / Math.max(visible + maxScroll, 1));
            int travel = Math.max(1, trackH - thumbH);
            int thumbY = trackY + (maxScroll == 0 ? 0 : travel * activeScroll / maxScroll);
            graphics.fill(trackX, thumbY, trackX + 4, thumbY + thumbH, 0xFF7C67A3);
        }

        graphics.fill(width - 146, 8, width - 8, 46, 0xC0182232);
        graphics.renderOutline(width - 146, 8, 138, 38, 0xFF8E72C7);
        drawButton(graphics, mc, width - 138, 12, 126, 24, "New Ritual", false);

        if (PreparedRitualPreview.prepared()) {
            String prepared = "Prepared: " + PreparedRitualPreview.ritual().displayName();
            int tx = width - 146;
            int ty = 54;
            graphics.fill(tx, ty, width - 8, 108, 0xC0182232);
            graphics.renderOutline(tx, ty, 138, 54, 0xFF8E72C7);
            String shown = mc.font.plainSubstrByWidth(prepared, 126);
            graphics.drawString(mc.font, shown, tx + 6, ty + 7, 0xFFD2B7FF, false);
            drawButton(graphics, mc, width - 138, 80, 126, 22, "Cancel Prepared Ritual", false);
        }

        String help = "Click a ritual to focus it. Right-click to rename it.";
        graphics.drawString(mc.font, help, PANEL_X + 8, bottom - 14, 0xFF9FAEC4, false);
    }

    private static ActiveRitualClientRegistry.Entry activeRitualAt(int height, double mouseX, double mouseY) {
        if (mouseX < PANEL_X + 4 || mouseX >= PANEL_X + MANAGEMENT_PANEL_W - 8) return null;
        int visible = activeVisibleRows(height);
        if (mouseY < ACTIVE_LIST_TOP || mouseY >= ACTIVE_LIST_TOP + visible * ACTIVE_ROW_H) return null;
        int row = (int)((mouseY - ACTIVE_LIST_TOP) / ACTIVE_ROW_H);
        int index = activeScroll + row;
        List<ActiveRitualClientRegistry.Entry> entries = ActiveRitualClientRegistry.entries();
        return index >= 0 && index < entries.size() ? entries.get(index) : null;
    }

    private static int activeRowY(int height, ActiveRitualClientRegistry.Entry target) {
        List<ActiveRitualClientRegistry.Entry> entries = ActiveRitualClientRegistry.entries();
        int index = entries.indexOf(target);
        if (index < activeScroll || index >= activeScroll + activeVisibleRows(height)) return -1000;
        return ACTIVE_LIST_TOP + (index - activeScroll) * ACTIVE_ROW_H;
    }

    private static int activeVisibleRows(int height) {
        return Math.max(3, (height - ACTIVE_LIST_TOP - ACTIVE_BOTTOM_MARGIN) / ACTIVE_ROW_H);
    }

    private static void renderPlannerControls(GuiGraphics graphics, Minecraft mc, int width, int height) {
        drawButton(graphics, mc, PANEL_X + 7, height - 38, 72, 22, "Back", false);
        drawButton(graphics, mc, width - 178, height - 38, 170, 22, "Prepare Ritual", false);
    }

    private static boolean handleGizmos(Minecraft mc, boolean leftDown, boolean justPressed,
                                        double rawX, double rawY) {
        int[] b = bounds();
        if (b == null) return false;

        if (rotatingFacing) {
            if (!leftDown) return true;
            facingYaw = wrapDegrees(facingStartYaw + (float)((rawX - facingDragStartX) * 0.45D));
            facingPitch = clamp(facingStartPitch + (float)((rawY - facingDragStartY) * 0.45D), -89.0F, 89.0F);
            return true;
        }

        if (movingArea) {
            if (!leftDown) return true;
            RayData ray = cursorRay(mc, rawX, rawY);
            if (ray == null || moveStartBounds == null || moveAxis < 0) return true;
            double[] center = ConduitAreaRender.center(moveStartBounds);
            double current = axisParam(ray, center, moveAxis);
            int delta = (int)Math.round(current - moveStartParam);
            int[] shifted = Arrays.copyOf(moveStartBounds, moveStartBounds.length);
            shifted[moveAxis] += delta;
            shifted[moveAxis + 3] += delta;
            setBounds(shifted);
            return true;
        }

        if (!justPressed) return false;
        RayData ray = cursorRay(mc, rawX, rawY);
        if (ray == null) return false;

        if (permanencyUsesFacing()) {
            double[] origin = ConduitAreaRender.facingOrigin(b);
            var dir = ConduitAreaRender.facingDirection(facingYaw, facingPitch);
            if (distanceRaySegment(ray,
                    origin[0], origin[1], origin[2],
                    origin[0] + dir.x * 2.2D,
                    origin[1] + dir.y * 2.2D,
                    origin[2] + dir.z * 2.2D) < 0.28D) {
                rotatingFacing = true;
                facingDragStartX = rawX;
                facingDragStartY = rawY;
                facingStartYaw = facingYaw;
                facingStartPitch = facingPitch;
                return true;
            }
        }

        double[] center = ConduitAreaRender.center(b);
        int axis = moveAxisHit(ray, center);
        if (axis >= 0) {
            movingArea = true;
            moveAxis = axis;
            moveStartBounds = Arrays.copyOf(b, b.length);
            moveStartParam = axisParam(ray, center, axis);
            return true;
        }
        return false;
    }

    private static boolean shouldRunBuilderVolume(Minecraft mc, boolean leftDown, boolean justPressed,
                                                  double rawX, double rawY) {
        if (bounds() == null) return true;

        try {
            if (boolField("selecting").getBoolean(null) || boolField("resizing").getBoolean(null)) {
                return true;
            }
        } catch (Throwable ignored) {
        }

        if (!leftDown) return true;
        if (!justPressed) return false;
        return builderResizeHandleHit(mc, rawX, rawY);
    }

    private static boolean builderResizeHandleHit(Minecraft mc, double rawX, double rawY) {
        try {
            if (handleHitMethod == null) {
                handleHitMethod = BuilderVolumeSupport.class.getDeclaredMethod(
                        "handleHit", Object.class, double.class, double.class);
                handleHitMethod.setAccessible(true);
            }
            Object hit = handleHitMethod.invoke(null, mc, rawX, rawY);
            return hit instanceof Number number && number.intValue() >= 0;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static int moveAxisHit(RayData ray, double[] center) {
        for (int axis = 0; axis < 3; axis++) {
            double x2 = center[0] + (axis == 0 ? 1.65D : 0.0D);
            double y2 = center[1] + (axis == 1 ? 1.65D : 0.0D);
            double z2 = center[2] + (axis == 2 ? 1.65D : 0.0D);
            if (distanceRaySegment(ray, center[0], center[1], center[2], x2, y2, z2) < 0.22D) return axis;
        }
        return -1;
    }

    private static double axisParam(RayData ray, double[] center, int axis) {
        double ax = axis == 0 ? 1.0D : 0.0D;
        double ay = axis == 1 ? 1.0D : 0.0D;
        double az = axis == 2 ? 1.0D : 0.0D;
        double wx = ray.ox - center[0];
        double wy = ray.oy - center[1];
        double wz = ray.oz - center[2];
        double b = ray.dx * ax + ray.dy * ay + ray.dz * az;
        double dW = ray.dx * wx + ray.dy * wy + ray.dz * wz;
        double aW = ax * wx + ay * wy + az * wz;
        double denom = 1.0D - b * b;
        if (Math.abs(denom) < 1.0E-5D) return axis == 0 ? wx : axis == 1 ? wy : wz;
        return (aW - b * dW) / denom;
    }

    private static double distanceRaySegment(RayData ray,
                                             double ax, double ay, double az,
                                             double bx, double by, double bz) {
        double best = Double.POSITIVE_INFINITY;
        for (int i = 0; i <= 32; i++) {
            double u = i / 32.0D;
            double px = ax + (bx - ax) * u;
            double py = ay + (by - ay) * u;
            double pz = az + (bz - az) * u;
            double vx = px - ray.ox;
            double vy = py - ray.oy;
            double vz = pz - ray.oz;
            double t = vx * ray.dx + vy * ray.dy + vz * ray.dz;
            if (t < 0.0D) t = 0.0D;
            double rx = ray.ox + ray.dx * t;
            double ry = ray.oy + ray.dy * t;
            double rz = ray.oz + ray.dz * t;
            double dx = px - rx;
            double dy = py - ry;
            double dz = pz - rz;
            best = Math.min(best, Math.sqrt(dx * dx + dy * dy + dz * dz));
        }
        return best;
    }

    private static RayData cursorRay(Minecraft mc, double rawX, double rawY) {
        try {
            if (cursorRayMethod == null) {
                cursorRayMethod = ArcaneBuilderEditEvents.class.getDeclaredMethod(
                        "cursorRay", Object.class, double.class, double.class);
                cursorRayMethod.setAccessible(true);
            }
            Object ray = cursorRayMethod.invoke(null, mc, rawX, rawY);
            if (ray == null) return null;
            Class<?> type = ray.getClass();
            return new RayData(
                    rayField(type, "ox").getDouble(ray),
                    rayField(type, "oy").getDouble(ray),
                    rayField(type, "oz").getDouble(ray),
                    rayField(type, "dx").getDouble(ray),
                    rayField(type, "dy").getDouble(ray),
                    rayField(type, "dz").getDouble(ray));
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Field rayField(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static void normalizeShapeBounds(int[] before, int[] after) {
        if (after == null || areaShape == AreaShape.BOX) return;
        int[] normalized = Arrays.copyOf(after, after.length);
        int sx = size(after, 0);
        int sy = size(after, 1);
        int sz = size(after, 2);

        if (areaShape == AreaShape.SPHERE) {
            int changed = changedAxis(before, after);
            int diameter = changed >= 0 ? size(after, changed) : Math.max(sx, Math.max(sy, sz));
            for (int axis = 0; axis < 3; axis++) {
                if (axis == changed) continue;
                centerAxis(normalized, axis, diameter);
            }
        } else if (areaShape == AreaShape.CYLINDER) {
            int changed = changedHorizontalAxis(before, after);
            int diameter = changed >= 0 ? size(after, changed) : Math.max(sx, sz);
            if (changed != 0) centerAxis(normalized, 0, diameter);
            if (changed != 2) centerAxis(normalized, 2, diameter);
        }

        if (!Arrays.equals(after, normalized)) setBounds(normalized);
    }

    private static int changedAxis(int[] before, int[] after) {
        if (before == null) return -1;
        int found = -1;
        for (int axis = 0; axis < 3; axis++) {
            if (size(before, axis) != size(after, axis)) {
                if (found >= 0) return -1;
                found = axis;
            }
        }
        return found;
    }

    private static int changedHorizontalAxis(int[] before, int[] after) {
        if (before == null) return -1;
        boolean x = size(before, 0) != size(after, 0);
        boolean z = size(before, 2) != size(after, 2);
        if (x == z) return -1;
        return x ? 0 : 2;
    }

    private static int size(int[] b, int axis) {
        return b[axis + 3] - b[axis] + 1;
    }

    private static void centerAxis(int[] b, int axis, int size) {
        double center = (b[axis] + b[axis + 3] + 1.0D) * 0.5D;
        int min = (int)Math.floor(center - size * 0.5D);
        b[axis] = min;
        b[axis + 3] = min + size - 1;
    }

    private static void setBounds(int[] b) {
        try {
            intField("minX").setInt(null, b[0]);
            intField("minY").setInt(null, b[1]);
            intField("minZ").setInt(null, b[2]);
            intField("maxX").setInt(null, b[3]);
            intField("maxY").setInt(null, b[4]);
            intField("maxZ").setInt(null, b[5]);
            commitVirtualBounds();
        } catch (Throwable ignored) {
        }
    }

    private static void addCorner(Set<Long> selected, int x, int y, int z) {
        selected.add(new BlockPos(x, y, z).asLong());
    }

    private static boolean overPlannerUi(Minecraft mc, double mouseX, double mouseY) {
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        if (inside(mouseX, mouseY, PANEL_X, PANEL_Y, PANEL_W, height - 16)) return true;
        if (inside(mouseX, mouseY, width - 178, 8, 170, ritual.variableArea() ? 92 : 50)) return true;
        if (inside(mouseX, mouseY, PANEL_X + 7, height - 38, 72, 22)) return true;
        if (inside(mouseX, mouseY, width - 178, height - 38, 170, 22)) return true;
        return ritual == RitualDefinition.PERMANENCY
                && inside(mouseX, mouseY, width - 178, 104, 170, 72);
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

    private static float wrapDegrees(float value) {
        float result = value % 360.0F;
        if (result >= 180.0F) result -= 360.0F;
        if (result < -180.0F) result += 360.0F;
        return result;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
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
        viewMode = ViewMode.MANAGEMENT;
        leftWasDown = false;
        rightWasDown = false;
        uiConsumed = false;
        movingArea = false;
        rotatingFacing = false;
        moveAxis = -1;
        clearSelection();
    }

    private record RayData(double ox, double oy, double oz, double dx, double dy, double dz) {}
}
