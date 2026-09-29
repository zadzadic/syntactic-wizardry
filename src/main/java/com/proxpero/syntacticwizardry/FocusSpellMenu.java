package com.proxpero.syntacticwizardry;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class FocusSpellMenu extends AbstractContainerMenu {
    public static final int WIDTH = 176;
    public static final int HEIGHT = 166;
    private final int focusSlotCount;
    private final int activeSlot;

    public static FocusSpellMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        int slots = Math.max(1, Math.min(3, data.readVarInt()));
        int active = Math.max(0, Math.min(slots - 1, data.readVarInt()));
        return new FocusSpellMenu(id, inventory, new SimpleContainer(slots), slots, active);
    }

    public FocusSpellMenu(int id, Inventory inventory, ItemStack focus) {
        this(id, inventory, new FocusSpellInventory(focus), Math.max(1, FocusSpellStorage.slotCount(focus)), FocusSpellStorage.activeSlot(focus));
    }

    private FocusSpellMenu(int id, Inventory inventory, net.minecraft.world.Container focusInventory, int slots, int active) {
        super(SyntacticWizardry.FOCUS_SPELL_MENU.get(), id);
        this.focusSlotCount = slots;
        this.activeSlot = active;

        int startX = slots == 1 ? 80 : 62;
        for (int i = 0; i < slots; i++) addSlot(new FocusSlot(focusInventory, i, startX + i * 18, 24));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
    }

    public int focusSlotCount() { return focusSlotCount; }
    public int activeSlot() { return activeSlot; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < focusSlotCount) {
            if (!moveItemStackTo(stack, focusSlotCount, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!stack.is(SyntacticWizardry.WRITTEN_SPELL.get())) return ItemStack.EMPTY;
            if (!moveItemStackTo(stack, 0, focusSlotCount, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return copy;
    }

    @Override public boolean stillValid(Player player) { return true; }

    private static final class FocusSlot extends Slot {
        FocusSlot(net.minecraft.world.Container container, int index, int x, int y) { super(container, index, x, y); }
        @Override public boolean mayPlace(ItemStack stack) { return stack.is(SyntacticWizardry.WRITTEN_SPELL.get()); }
        @Override public int getMaxStackSize() { return 1; }
    }
}
