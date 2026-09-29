package com.proxpero.syntacticwizardry;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class DimensionalStorageMenu extends AbstractContainerMenu {
    public static final int WIDTH = 196;
    public static final int HEIGHT = 222;
    public static final int STORAGE_COLS = 10;
    public static final int STORAGE_ROWS = 5;
    public static final int ACTION_PREVIOUS_PAGE = 1;
    public static final int ACTION_NEXT_PAGE = 2;

    private final Container storage;
    private final UUID targetId;
    private final int capacity;
    private final int page;
    private final int storageSlotCount;

    public static DimensionalStorageMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        int capacity = data.readInt();
        int page = data.readInt();
        return new DimensionalStorageMenu(id, inventory, new SimpleContainer(DimensionalStorageSavedData.MAX_SLOTS), null, capacity, page);
    }

    public DimensionalStorageMenu(int id, Inventory inventory, Container storage, UUID targetId, int capacity, int page) {
        super(SyntacticWizardry.DIMENSIONAL_STORAGE_MENU.get(), id);
        this.storage = storage;
        this.targetId = targetId;
        this.capacity = Math.max(DimensionalStorageService.SLOTS_PER_POTENCE, Math.min(DimensionalStorageSavedData.MAX_SLOTS, capacity));
        this.page = Math.max(0, Math.min(pageCount() - 1, page));
        int offset = this.page * DimensionalStorageService.PAGE_SIZE;
        this.storageSlotCount = Math.min(DimensionalStorageService.PAGE_SIZE, this.capacity - offset);

        for (int local = 0; local < storageSlotCount; local++) {
            int col = local % STORAGE_COLS;
            int row = local / STORAGE_COLS;
            addSlot(new Slot(storage, offset + local, 8 + col * 18, 20 + row * 18));
        }

        int playerX = 17;
        int playerY = 139;
        for (int row = 0; row < 3; row++) for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col + row * 9 + 9, playerX + col * 18, playerY + row * 18));
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, playerX + col * 18, 197));
    }

    public int capacity() { return capacity; }
    public int page() { return page; }
    public int pageCount() { return Math.max(1, (capacity + DimensionalStorageService.PAGE_SIZE - 1) / DimensionalStorageService.PAGE_SIZE); }
    public int storageSlotCount() { return storageSlotCount; }
    public boolean hasPreviousPage() { return page > 0; }
    public boolean hasNextPage() { return page + 1 < pageCount(); }
    public boolean matches(UUID target, int requestedCapacity) { return targetId != null && targetId.equals(target) && capacity == requestedCapacity; }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!(player instanceof ServerPlayer serverPlayer) || targetId == null) return false;
        if (id == ACTION_PREVIOUS_PAGE && hasPreviousPage()) {
            DimensionalStorageService.openPage(serverPlayer, targetId, capacity, page - 1);
            return true;
        }
        if (id == ACTION_NEXT_PAGE && hasNextPage()) {
            DimensionalStorageService.openPage(serverPlayer, targetId, capacity, page + 1);
            return true;
        }
        return false;
    }

    @Override public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem();
        ItemStack copy = source.copy();
        if (index < storageSlotCount) {
            if (!moveItemStackTo(source, storageSlotCount, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(source, 0, storageSlotCount, false)) return ItemStack.EMPTY;
        }
        if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }
}
