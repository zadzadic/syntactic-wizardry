package com.proxpero.syntacticwizardry;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public final class RunecasterRuneInventory extends SimpleContainer {
    private final ItemStack runecaster;
    private boolean loading = true;

    public RunecasterRuneInventory(ItemStack runecaster) {
        super(RunecasterRuneStorage.RUNE_SLOTS);
        this.runecaster = runecaster;
        for (int i = 0; i < RunecasterRuneStorage.RUNE_SLOTS; i++) {
            super.setItem(i, RunecasterRuneStorage.getMountedRune(runecaster, i));
        }
        loading = false;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!loading) {
            for (int i = 0; i < RunecasterRuneStorage.RUNE_SLOTS; i++) {
                RunecasterRuneStorage.setMountedRune(runecaster, i, getItem(i));
            }
        }
    }
}
