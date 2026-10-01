package com.proxpero.syntacticwizardry;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public final class RunecasterRuneInventory extends SimpleContainer {
    private final ItemStack runecaster;
    private boolean loading = true;

    public RunecasterRuneInventory(ItemStack runecaster) {
        super(1);
        this.runecaster = runecaster;
        super.setItem(0, RunecasterRuneStorage.getRune(runecaster));
        loading = false;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!loading) RunecasterRuneStorage.setRune(runecaster, getItem(0));
    }
}
