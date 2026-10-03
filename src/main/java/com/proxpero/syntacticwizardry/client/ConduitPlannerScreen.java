package com.proxpero.syntacticwizardry.client;

import com.arcane.magic.client.ArcaneBuilderEditEvents;
import com.proxpero.syntacticwizardry.RitualDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public final class ConduitPlannerScreen extends Screen {
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
    private static final int LIST_BOTTOM_MARGIN = 44;
    private static final int MAX_SELECTED = 8192;

    private final BlockPos conduitPos;
    private final LinkedHashSet<BlockPos> selectedBlocks = new LinkedHashSet<>();

    private RitualDefinition ritual = RitualDefinition.PROTECTION;
    private AreaShape areaShape = AreaShape.BOX;
    private int listScroll;
    private boolean cameraEntered;
    private boolean painting;
    private boolean paintAdd;
    private BlockPos lastPainted;

    private static Method blockUnderCursor;

    public ConduitPlannerScreen(BlockPos conduitPos) {
        super(Component.literal("Conduit"));
        this.conduitPos = conduitPos.immutable();
    }

    @Override
    protected void init() {
        if (!cameraEntered) cameraEntered = ConduitPlannerCamera.enter(conduitPos);
    }

    @Override
    public void tick() {
        ConduitPlannerCamera.update();
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.keyUp.isDown()) ConduitPlannerCamera.panKeyboard(1.0D, 0.0D);
        if (mc.options.keyDown.isDown()) ConduitPlannerCamera.panKeyboard(-1.0D, 0.0D);
        if (mc.options.keyLeft.isDown()) ConduitPlannerCamera.panKeyboard(0.0D, -1.0D);
        if (mc.options.keyRight.isDown()) ConduitPlannerCamera.panKeyboard(0.0D, 1.0D);
    }

    @Override
    public void removed() {
        painting = false;
        ConduitPlannerCamera.exit();
        cameraEntered = false;
        super.removed();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderRitualPanel(graphics, mouseX, mouseY);
        renderAreaPanel(graphics);
        renderInstructions(graphics);
    }

    private void renderRitualPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        int bottom = height - 8;
        graphics.fill(PANEL_X, PANEL_Y, PANEL_X + PANEL_W, bottom, 0xC0182232);
        graphics.renderOutline(PANEL_X, PANEL_Y, PANEL_W, bottom - PANEL_Y, 0xFF8E72C7);
        graphics.drawString(font, "Rituals", PANEL_X + 7, PANEL_Y + 7, 0xFFF0E8FF, false);

        List<RitualDefinition> entries = RitualDefinition.entries();
        int visible = visibleRows();
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
            graphics.drawString(font, entry.displayName(), PANEL_X + 8, y + 3,
                    selected ? 0xFFFFFFFF : 0xFFD7DDEA, false);
        }

        String scrollText = (listScroll + 1) + "-" + Math.min(entries.size(), listScroll + visible)
                + " / " + entries.size();
        graphics.drawString(font, scrollText, PANEL_X + 7, bottom - 13, 0xFF9FAEC4, false);
    }

    private void renderAreaPanel(GuiGraphics graphics) {
        int x = width - 178;
        int y = 8;
        int w = 170;
        int h = ritual.variableArea() ? 92 : 50;
        graphics.fill(x, y, x + w, y + h, 0xC0182232);
        graphics.renderOutline(x, y, w, h, 0xFF8E72C7);
        graphics.drawString(font, ritual.displayName(), x + 7, y + 7, 0xFFF0E8FF, false);

        if (!ritual.variableArea()) {
            graphics.drawString(font, "Fixed / no local area", x + 7, y + 25, 0xFFB9C5D6, false);
            return;
        }

        drawButton(graphics, x + 7, y + 24, 70, 18, "Box", areaShape == AreaShape.BOX);
        drawButton(graphics, x + 82, y + 24, 80, 18, "Sphere", areaShape == AreaShape.SPHERE);
        drawButton(graphics, x + 7, y + 47, 70, 18, "Clear", false);

        if (selectedBlocks.isEmpty()) {
            graphics.drawString(font, "No area selected", x + 82, y + 52, 0xFFB9C5D6, false);
        } else if (areaShape == AreaShape.BOX) {
            int[] size = boxSize();
            graphics.drawString(font, size[0] + " x " + size[1] + " x " + size[2],
                    x + 82, y + 52, 0xFFDCE6F3, false);
        } else {
            graphics.drawString(font, "Radius " + String.format(java.util.Locale.ROOT, "%.1f", sphereRadius()),
                    x + 82, y + 52, 0xFFDCE6F3, false);
        }

        graphics.drawString(font, selectedBlocks.size() + " selected block" + (selectedBlocks.size() == 1 ? "" : "s"),
                x + 7, y + 72, 0xFF9FAEC4, false);
    }

    private void renderInstructions(GuiGraphics graphics) {
        int y = height - 18;
        String text = ritual.variableArea()
                ? "LMB drag: select area   RMB drag: rotate   MMB drag: pan   Wheel: zoom   Esc: exit"
                : "RMB drag: rotate   MMB drag: pan   Wheel: zoom   Esc: exit";
        int x = Math.max(PANEL_X + PANEL_W + 8, (width - font.width(text)) / 2);
        graphics.fill(x - 5, y - 3, Math.min(width - 6, x + font.width(text) + 5), y + 11, 0xA0101622);
        graphics.drawString(font, text, x, y, 0xFFD2DCEB, false);
    }

    private void drawButton(GuiGraphics graphics, int x, int y, int w, int h, String label, boolean active) {
        graphics.fill(x, y, x + w, y + h, active ? 0xD05D4A86 : 0xB02B3548);
        graphics.renderOutline(x, y, w, h, active ? 0xFFD2B7FF : 0xFF66738A);
        graphics.drawCenteredString(font, label, x + w / 2, y + 5, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            RitualDefinition clicked = ritualAt(mouseX, mouseY);
            if (clicked != null) {
                if (clicked != ritual) {
                    ritual = clicked;
                    selectedBlocks.clear();
                    lastPainted = null;
                }
                return true;
            }

            if (ritual.variableArea() && handleAreaControls(mouseX, mouseY)) return true;

            if (ritual.variableArea() && !overUi(mouseX, mouseY)) {
                BlockPos pos = blockAtCursor(mouseX, mouseY);
                if (pos != null) {
                    paintAdd = !selectedBlocks.contains(pos);
                    painting = true;
                    lastPainted = null;
                    paint(pos);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && painting) {
            painting = false;
            lastPainted = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && painting && ritual.variableArea() && !overUi(mouseX, mouseY)) {
            BlockPos pos = blockAtCursor(mouseX, mouseY);
            if (pos != null) paint(pos);
            return true;
        }
        if (button == 1) {
            ConduitPlannerCamera.rotate(dragX, dragY);
            return true;
        }
        if (button == 2) {
            ConduitPlannerCamera.pan(dragX, dragY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (inside(mouseX, mouseY, PANEL_X, PANEL_Y, PANEL_W, height - 16)) {
            int before = listScroll;
            if (scrollY > 0.0D) listScroll--;
            else if (scrollY < 0.0D) listScroll++;
            int max = Math.max(0, RitualDefinition.entries().size() - visibleRows());
            listScroll = Math.max(0, Math.min(max, listScroll));
            return before != listScroll || max > 0;
        }
        ConduitPlannerCamera.zoom(scrollY);
        return true;
    }

    private boolean handleAreaControls(double mouseX, double mouseY) {
        int x = width - 178;
        int y = 8;
        if (inside(mouseX, mouseY, x + 7, y + 24, 70, 18)) {
            areaShape = AreaShape.BOX;
            return true;
        }
        if (inside(mouseX, mouseY, x + 82, y + 24, 80, 18)) {
            areaShape = AreaShape.SPHERE;
            return true;
        }
        if (inside(mouseX, mouseY, x + 7, y + 47, 70, 18)) {
            selectedBlocks.clear();
            return true;
        }
        return false;
    }

    private RitualDefinition ritualAt(double mouseX, double mouseY) {
        if (mouseX < PANEL_X + 4 || mouseX >= PANEL_X + PANEL_W - 4) return null;
        if (mouseY < LIST_TOP || mouseY >= LIST_TOP + visibleRows() * ENTRY_H) return null;
        int row = (int)((mouseY - LIST_TOP) / ENTRY_H);
        int index = listScroll + row;
        List<RitualDefinition> entries = RitualDefinition.entries();
        return index >= 0 && index < entries.size() ? entries.get(index) : null;
    }

    private void paint(BlockPos pos) {
        if (pos.equals(lastPainted)) return;
        if (paintAdd) {
            if (selectedBlocks.size() < MAX_SELECTED) selectedBlocks.add(pos.immutable());
        } else {
            selectedBlocks.remove(pos);
        }
        lastPainted = pos.immutable();
    }

    private BlockPos blockAtCursor(double mouseX, double mouseY) {
        try {
            if (blockUnderCursor == null) {
                blockUnderCursor = ArcaneBuilderEditEvents.class.getDeclaredMethod(
                        "blockUnderCursor", Object.class, double.class, double.class);
                blockUnderCursor.setAccessible(true);
            }
            Object value = blockUnderCursor.invoke(null, Minecraft.getInstance(), mouseX, mouseY);
            if (!(value instanceof Long packed) || packed == Long.MIN_VALUE) return null;
            return BlockPos.of(packed);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private boolean overUi(double mouseX, double mouseY) {
        return inside(mouseX, mouseY, PANEL_X, PANEL_Y, PANEL_W, height - 16)
                || inside(mouseX, mouseY, width - 178, 8, 170, ritual.variableArea() ? 92 : 50);
    }

    private int visibleRows() {
        return Math.max(5, (height - LIST_TOP - LIST_BOTTOM_MARGIN) / ENTRY_H);
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    public Collection<BlockPos> selectedBlocks() {
        return selectedBlocks;
    }

    public RitualDefinition ritual() {
        return ritual;
    }

    public AreaShape areaShape() {
        return areaShape;
    }

    public BlockPos conduitPos() {
        return conduitPos;
    }

    public int[] boxBounds() {
        if (selectedBlocks.isEmpty()) return null;
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPos pos : selectedBlocks) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        return new int[]{minX, minY, minZ, maxX, maxY, maxZ};
    }

    private int[] boxSize() {
        int[] bounds = boxBounds();
        if (bounds == null) return new int[]{0, 0, 0};
        return new int[]{bounds[3] - bounds[0] + 1, bounds[4] - bounds[1] + 1, bounds[5] - bounds[2] + 1};
    }

    public double sphereRadius() {
        double cx = conduitPos.getX() + 0.5D;
        double cy = conduitPos.getY() + 0.5D;
        double cz = conduitPos.getZ() + 0.5D;
        double radius = 0.0D;
        for (BlockPos pos : selectedBlocks) {
            double dx = pos.getX() + 0.5D - cx;
            double dy = pos.getY() + 0.5D - cy;
            double dz = pos.getZ() + 0.5D - cz;
            radius = Math.max(radius, Math.sqrt(dx * dx + dy * dy + dz * dz) + 0.5D);
        }
        return radius;
    }
}
