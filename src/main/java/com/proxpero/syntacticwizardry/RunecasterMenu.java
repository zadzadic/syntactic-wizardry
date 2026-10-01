package com.proxpero.syntacticwizardry;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class RunecasterMenu extends AbstractContainerMenu {
    public static final int WIDTH = 176;
    public static final int HEIGHT = 166;
    private static final int RUNE_SLOT_COUNT = 3;

    public static RunecasterMenu client(int id, Inventory inventory) {
        return new RunecasterMenu(id, inventory, new SimpleContainer(RUNE_SLOT_COUNT));
    }

    public RunecasterMenu(int id, Inventory inventory, ItemStack runecaster) {
        this(id, inventory, new RunecasterRuneInventory(runecaster));
    }

    private RunecasterMenu(int id, Inventory inventory, Container runes) {
        super(SyntacticWizardry.RUNECASTER_MENU.get(), id);

        for (int i = 0; i < RUNE_SLOT_COUNT; i++) {
            addSlot(new RuneSlot(runes, i, 80, 18 + i * 18));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < RUNE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, RUNE_SLOT_COUNT, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!RunestoneItem.isRunestone(stack) || !RunestoneItem.isCarved(stack)) return ItemStack.EMPTY;
            if (!moveItemStackTo(stack, 0, RUNE_SLOT_COUNT, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private static final class RuneSlot extends Slot {
        RuneSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return RunestoneItem.isRunestone(stack) && RunestoneItem.isCarved(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
