package com.proxpero.syntacticwizardry;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public final class FocusSpellInventory extends SimpleContainer {
    private final ItemStack focus;
    private boolean loading = true;

    public FocusSpellInventory(ItemStack focus) {
        super(Math.max(1, FocusSpellStorage.slotCount(focus)));
        this.focus = focus;
        for (int i = 0; i < getContainerSize(); i++) super.setItem(i, FocusSpellStorage.getSlot(focus, i));
        loading = false;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (loading) return;
        for (int i = 0; i < getContainerSize(); i++) FocusSpellStorage.setSlot(focus, i, getItem(i));
    }
}
