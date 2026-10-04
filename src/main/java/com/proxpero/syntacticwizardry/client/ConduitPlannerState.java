package com.proxpero.syntacticwizardry.client;

import com.arcane.magic.builder.BuilderInventoryPanel;
import com.arcane.magic.builder.BuilderVolumeSupport;
import com.arcane.magic.client.ArcaneBuilderClientEvents;
import com.arcane.magic.client.ArcaneBuilderEditEvents;
import com.proxpero.syntacticwizardry.MooncallPhase;
import com.proxpero.syntacticwizardry.RitualDefinition;
import com.proxpero.syntacticwizardry.SpellComponentDefinition;
import com.proxpero.syntacticwizardry.SpellComponents;
import com.proxpero.syntacticwizardry.SpellPresentation;
import com.proxpero.syntacticwizardry.SpellPropertyDefinition;
import com.proxpero.syntacticwizardry.SpellPropertyKey;
import com.proxpero.syntacticwizardry.RitualStructureRules;
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
    private static final int ACTIVE_LIST_TOP = 44;
    private static final int ACTIVE_ROW_H = 26;
    private static final int ACTIVE_BOTTOM_MARGIN = 18;

    private static boolean active;
    private static ViewMode viewMode = ViewMode.MANAGEMENT;
    private static BlockPos conduitPos = BlockPos.ZERO;
    private static BlockPos ritualCenter;
    private static BlockPos ritualHoverCenter;
    private static boolean placingRitualCenter;
    private static RitualDefinition ritual = RitualDefinition.PROTECTION;
    private static AreaShape areaShape = AreaShape.BOX;
    private static int listScroll;
    private static int ritualPotence = 1;
    private static MooncallPhase mooncallPhase = MooncallPhase.FULL_MOON;
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
    private static Method placementTargetMethod;
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
        ritualPotence = 1;
        mooncallPhase = MooncallPhase.FULL_MOON;
        ritualCenter = null;
        ritualHoverCenter = null;
        placingRitualCenter = false;
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

        boolean pointerOverUi = overPlannerUi(mc, mouseX, mouseY);
        if (justPressed && pointerOverUi) {
            uiConsumed = true;
            handleUiClick(mc, mouseX, mouseY);
        }

        if (placingRitualCenter && !pointerOverUi) {
            ritualHoverCenter = builderPlacementTarget(mc, rawX, rawY);
            if (justPressed && ritualHoverCenter != null) {
                ritualCenter = ritualHoverCenter.immutable();
                ritualHoverCenter = null;
                placingRitualCenter = false;
                clearSelection();
                uiConsumed = true;
            }
        } else if (placingRitualCenter) {
            ritualHoverCenter = null;
        }

        boolean gizmoConsumed = false;
        if (!placingRitualCenter && ritualCenter != null && !uiConsumed && ritual.variableArea()) {
            gizmoConsumed = handleGizmos(mc, leftDown, justPressed, rawX, rawY);
        }

        if (placingRitualCenter || ritualCenter == null) {
            if (!selection().isEmpty()) clearSelection();
        } else if (!ritual.variableArea()) {
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
        if (ritual == RitualDefinition.MOONCALL) {
            renderMooncallPanel(graphics, mc, width);
        } else if (ritual == RitualDefinition.SUMMONING) {
            renderNoPotencePanel(graphics, mc, width, "Summoning");
        } else if (ritual == RitualDefinition.BINDING) {
            renderNoPotencePanel(graphics, mc, width, "Binding");
        } else if (ritual == RitualDefinition.PROTECTION) {
            renderNoPotencePanel(graphics, mc, width, "Protection");
        } else if (ritual != RitualDefinition.PERMANENCY) {
            renderRitualStrengthPanel(graphics, mc, width);
        }
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
        if (!active || viewMode != ViewMode.PLANNER) return false;
        if (!(renderEvent instanceof net.neoforged.neoforge.client.event.RenderLevelStageEvent event)) return false;

        BlockPos previewCenter = placingRitualCenter ? ritualHoverCenter : ritualCenter;
        boolean ritualRendered = previewCenter != null;
        if (previewCenter != null) {
            PreparedRitualPreview.renderGhosts(
                    event,
                    PreparedRitualPreview.previewGhosts(
                            ritual,
                            previewCenter,
                            requestedRitualPotence(),
                            mooncallPhase),
                    placingRitualCenter ? 0.55F : 0.90F);
        }

        if (placingRitualCenter || ritualCenter == null || !ritual.variableArea()) return ritualRendered;

        int[] b = bounds();
        if (b == null) return ritualRendered;
        if (areaShape == AreaShape.BOX) {
            boolean rendered = BuilderVolumeSupport.renderSelection(renderEvent);
            ConduitAreaRender.renderGizmos(event, b, permanencyUsesFacing(), facingYaw, facingPitch);
            return rendered || ritualRendered;
        }
        return ConduitAreaRender.render(event, areaShape, b, true, permanencyUsesFacing(), facingYaw, facingPitch)
                || ritualRendered;
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
        if (ritual == RitualDefinition.MOONCALL) {
            int phaseY = 66;
            if (inside(mouseX, mouseY, width - 171, phaseY + 20, 20, 18)) {
                mooncallPhase = mooncallPhase.previous();
                return;
            }
            if (inside(mouseX, mouseY, width - 35, phaseY + 20, 20, 18)) {
                mooncallPhase = mooncallPhase.next();
                return;
            }
            if (inside(mouseX, mouseY, width - 171, phaseY + 44, 156, 18)) {
                ritualCenter = null;
                ritualHoverCenter = null;
                placingRitualCenter = true;
                clearSelection();
                return;
            }
        } else if (ritual == RitualDefinition.SUMMONING
                || ritual == RitualDefinition.BINDING
                || ritual == RitualDefinition.PROTECTION) {
            int noPotenceY = ritual.variableArea() ? 108 : 66;
            if (inside(mouseX, mouseY, width - 171, noPotenceY + 44, 156, 18)) {
                ritualCenter = null;
                ritualHoverCenter = null;
                placingRitualCenter = true;
                clearSelection();
                return;
            }
        } else if (ritual != RitualDefinition.PERMANENCY) {
            int strengthY = ritual.variableArea() ? 108 : 66;
            if (inside(mouseX, mouseY, width - 171, strengthY + 20, 20, 18)) {
                ritualPotence = Math.max(1, ritualPotence - 1);
                return;
            }
            if (inside(mouseX, mouseY, width - 35, strengthY + 20, 20, 18)) {
                ritualPotence = Math.min(32, ritualPotence + 1);
                return;
            }
            if (inside(mouseX, mouseY, width - 171, strengthY + 44, 156, 18)) {
                ritualCenter = null;
                ritualHoverCenter = null;
                placingRitualCenter = true;
                clearSelection();
                return;
            }
        }
        if (inside(mouseX, mouseY, width - 178, height - 38, 170, 22)) {
            if (ritualCenter == null) {
                placingRitualCenter = true;
                return;
            }
            PreparedRitualPreview.prepare(
                    ritual,
                    ritualCenter,
                    requestedRitualPotence(),
                    mooncallPhase);
            return;
        }

        RitualDefinition clicked = ritualAt(mc, mouseX, mouseY);
        if (clicked != null) {
            if (clicked != ritual) {
                ritual = clicked;
                ritualPotence = 1;
                mooncallPhase = MooncallPhase.FULL_MOON;
                ritualCenter = null;
                ritualHoverCenter = null;
                placingRitualCenter = true;
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
            if (PreparedRitualPreview.ritual() == RitualDefinition.PROTECTION) {
                ProtectionAreaClientBridge.clearPrepared();
            }
            PreparedRitualPreview.clear();
            return;
        }

        ActiveRitualClientRegistry.Entry entry = activeRitualAt(height, mouseX, mouseY);
        if (entry == null) return;

        int rowY = activeRowY(height, entry);
        if (rightPressed) {
            mc.setScreen(new ArmillaryRenameScreen(null, entry.name(),
                    value -> RitualClientControl.rename(entry.id(), value)));
            return;
        }

        if (!leftPressed) return;
        if (inside(mouseX, mouseY, PANEL_X + 196, rowY + 4, 52, 18)) {
            RitualClientControl.pause(entry.id(), !entry.paused());
            return;
        }
        if (inside(mouseX, mouseY, PANEL_X + 252, rowY + 4, 48, 18)) {
            RitualClientControl.stop(entry.id());
            return;
        }

        focusCamera(entry.center());
    }

    private static void enterPlanner() {
        viewMode = ViewMode.PLANNER;
        ritual = RitualDefinition.PROTECTION;
        areaShape = AreaShape.BOX;
        listScroll = 0;
        ritualPotence = 1;
        mooncallPhase = MooncallPhase.FULL_MOON;
        ritualCenter = null;
        ritualHoverCenter = null;
        placingRitualCenter = true;
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
        double ritualMana = ActiveRitualClientRegistry.totalManaDrawPerSecond();
        String manaText = String.format(java.util.Locale.ROOT, "Mana to Rituals: %.1f/s", ritualMana);
        graphics.drawString(mc.font, manaText, PANEL_X + 8, PANEL_Y + 21, 0xFF8FD8FF, false);

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
            String status = entry.stopping()
                    ? "Stopping"
                    : entry.paused()
                    ? "Paused"
                    : !entry.powered()
                    ? "Dormant"
                    : entry.ritual().displayName();
            int statusColor = entry.stopping()
                    ? 0xFFFFB38A
                    : entry.paused()
                    ? 0xFFFFD88A
                    : !entry.powered()
                    ? 0xFFFF9E80
                    : 0xFF9FAEC4;
            graphics.drawString(mc.font, status, PANEL_X + 9, y + 15, statusColor, false);
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
            if (PreparedRitualPreview.ritual() == RitualDefinition.MOONCALL) {
                prepared += " - " + PreparedRitualPreview.mooncallPhase().displayName();
            }
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

    private static void renderMooncallPanel(GuiGraphics graphics, Minecraft mc, int width) {
        int x = width - 178;
        int y = ritual.variableArea() ? 108 : 66;
        int w = 170;
        int h = 68;

        graphics.fill(x, y, x + w, y + h, 0xC0182232);
        graphics.renderOutline(x, y, w, h, 0xFF8E72C7);
        graphics.drawString(mc.font, "Moon Phase", x + 7, y + 7, 0xFFF0E8FF, false);

        drawButton(graphics, mc, x + 7, y + 20, 20, 18, "<", false);
        graphics.drawCenteredString(
                mc.font,
                mooncallPhase.displayName(),
                x + 85,
                y + 25,
                0xFFDCE6F3);
        drawButton(graphics, mc, x + 143, y + 20, 20, 18, ">", false);

        String button = ritualCenter == null ? "Place Ritual Center" : "Relocate Ritual Center";
        drawButton(graphics, mc, x + 7, y + 44, 156, 18, button, false);
    }

    private static void renderNoPotencePanel(
            GuiGraphics graphics,
            Minecraft mc,
            int width,
            String title) {
        int x = width - 178;
        int y = 66;
        int w = 170;
        int h = 68;

        graphics.fill(x, y, x + w, y + h, 0xC0182232);
        graphics.renderOutline(x, y, w, h, 0xFF8E72C7);
        graphics.drawString(mc.font, title, x + 7, y + 7, 0xFFF0E8FF, false);
        graphics.drawCenteredString(mc.font, "No Potence", x + 85, y + 27, 0xFFB9C5D6);

        String button = ritualCenter == null ? "Place Ritual Center" : "Relocate Ritual Center";
        drawButton(graphics, mc, x + 7, y + 44, 156, 18, button, false);
    }

    private static void renderRitualStrengthPanel(GuiGraphics graphics, Minecraft mc, int width) {
        int x = width - 178;
        int y = ritual.variableArea() ? 108 : 66;
        int w = 170;
        int h = 68;

        graphics.fill(x, y, x + w, y + h, 0xC0182232);
        graphics.renderOutline(x, y, w, h, 0xFF8E72C7);
        graphics.drawString(mc.font, "Ritual Strength", x + 7, y + 7, 0xFFF0E8FF, false);

        drawButton(graphics, mc, x + 7, y + 20, 20, 18, "<", false);
        graphics.drawCenteredString(mc.font, "Potence " + requestedRitualPotence(), x + 85, y + 25, 0xFFDCE6F3);
        drawButton(graphics, mc, x + 143, y + 20, 20, 18, ">", false);

        String focusText = "Focus: " + RitualStructureRules.focusPlan(requestedRitualPotence()).summary();
        graphics.drawString(mc.font, mc.font.plainSubstrByWidth(focusText, 156),
                x + 7, y + 42, 0xFFB9C5D6, false);

        String button = ritualCenter == null ? "Place Ritual Center" : "Relocate Ritual Center";
        drawButton(graphics, mc, x + 7, y + 48, 156, 18, button, false);
    }

    private static void renderPlannerControls(GuiGraphics graphics, Minecraft mc, int width, int height) {
        drawButton(graphics, mc, PANEL_X + 7, height - 38, 72, 22, "Back", false);

        String prepareText;
        if (ritualCenter == null) prepareText = "Place Center First";
        else prepareText = "Prepare Ritual";
        drawButton(graphics, mc, width - 178, height - 38, 170, 22, prepareText, false);

        if (ritualCenter != null) {
            String centerText = "Center: " + ritualCenter.getX() + ", " + ritualCenter.getY() + ", " + ritualCenter.getZ();
            graphics.drawString(mc.font, mc.font.plainSubstrByWidth(centerText, 170),
                    width - 178, height - 50, 0xFF9FAEC4, false);
        } else if (placingRitualCenter) {
            graphics.drawString(mc.font, "Click the world to place the Ritual.", width - 178, height - 50, 0xFFFFD88A, false);
        }
    }

    private static int requestedRitualPotence() {
        if (ritual == RitualDefinition.MOONCALL
                || ritual == RitualDefinition.SUMMONING
                || ritual == RitualDefinition.BINDING
                || ritual == RitualDefinition.PROTECTION) return 0;
        if (ritual != RitualDefinition.PERMANENCY) return ritualPotence;

        int[] plan = permanentSpellEditor.snapshotPlan();
        int[] settings = permanentSpellEditor.snapshotSettings();
        int result = 1;

        for (int cell = 0; cell < SpellPresentation.CELLS; cell++) {
            SpellComponentDefinition definition = SpellComponents.byType(SpellPresentation.typeAt(plan, cell));
            if (definition == null) continue;

            for (SpellPropertyDefinition property : definition.settings()) {
                if (property.key() != SpellPropertyKey.POTENCE) continue;
                result = Math.max(result, SpellPresentation.potenceAt(settings, cell));
                break;
            }
        }

        return result;
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