package com.proxpero.syntacticwizardry;

import net.minecraft.world.item.ItemStack;

public final class RunecasterSpellBuilder {
    private RunecasterSpellBuilder() {}

    public static ItemStack build(ItemStack rune) {
        if (!RunestoneItem.isRunestone(rune) || !RunestoneItem.isCarved(rune)) return ItemStack.EMPTY;

        int[] runeCells = RunestoneItem.cells(rune);
        int slotCount = RunestoneItem.slots(rune);
        int[] plan = SpellPresentation.emptyPlan();
        int[] settings = SpellPresentation.emptySettings();

        int row = 0;
        for (int i = 0; i < slotCount && row < SpellPresentation.ROWS; i++) {
            int type = runeCells[i];
            if (type == SpellPresentation.TYPE_EMPTY) continue;

            SpellComponentDefinition definition = SpellComponents.byType(type);
            if (definition == null) continue;

            int cell = row * SpellPresentation.COLS;
            SpellPresentation.setCell(plan, cell, definition.typeId(), definition.defaultStyle(), definition.defaultVisual());
            row++;
        }

        if (row == 0) return ItemStack.EMPTY;

        ItemStack spell = WrittenSpellItem.create(plan, settings, rune.getHoverName().getString());
        SpellPresentation.ensureManaCosts(spell);
        return spell;
    }
}
