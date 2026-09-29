package com.proxpero.syntacticwizardry;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/** Opens and mutates persistent target-owned dimensional inventories. */
public final class DimensionalStorageService {
    public static final int SLOTS_PER_POTENCE = 10;
    public static final int PAGE_SIZE = 50;

    private DimensionalStorageService() {}

    public static int capacityForPotence(int potence) {
        int clamped = Math.max(SpellPresentation.POTENCE_MIN, Math.min(SpellPresentation.POTENCE_MAX, potence));
        return clamped * SLOTS_PER_POTENCE;
    }

    public static void open(ServerPlayer viewer, ServerPlayer target, int potence) {
        open(viewer, target, capacityForPotence(potence), 0);
    }

    static void open(ServerPlayer viewer, ServerPlayer target, int capacity, int page) {
        if (viewer == null || target == null) return;
        int safeCapacity = Math.max(SLOTS_PER_POTENCE, Math.min(DimensionalStorageSavedData.MAX_SLOTS, capacity));
        int pages = Math.max(1, (safeCapacity + PAGE_SIZE - 1) / PAGE_SIZE);
        int safePage = Math.max(0, Math.min(pages - 1, page));
        DimensionalStorageSavedData data = viewer.server.overworld().getDataStorage().computeIfAbsent(DimensionalStorageSavedData.factory(), DimensionalStorageSavedData.DATA_NAME);
        Container storage = new SavedStorageContainer(data, target.getUUID());
        UUID targetId = target.getUUID();
        Component title = Component.literal("Dimensional Storage");
        viewer.openMenu(new SimpleMenuProvider(
                (containerId, playerInventory, player) -> new DimensionalStorageMenu(containerId, playerInventory, storage, targetId, safeCapacity, safePage),
                title
        ), buffer -> {
            buffer.writeInt(safeCapacity);
            buffer.writeInt(safePage);
        });
    }

    public static void openPage(ServerPlayer viewer, UUID targetId, int capacity, int page) {
        if (viewer == null || targetId == null) return;
        ServerPlayer target = viewer.server.getPlayerList().getPlayer(targetId);
        if (target != null) open(viewer, target, capacity, page);
    }

    private static final class SavedStorageContainer implements Container {
        private final DimensionalStorageSavedData data;
        private final NonNullList<ItemStack> items;

        private SavedStorageContainer(DimensionalStorageSavedData data, UUID owner) {
            this.data = data;
            this.items = data.inventory(owner);
        }

        @Override public int getContainerSize() { return items.size(); }
        @Override public boolean isEmpty() { for (ItemStack stack : items) if (!stack.isEmpty()) return false; return true; }
        @Override public ItemStack getItem(int slot) { return slot >= 0 && slot < items.size() ? items.get(slot) : ItemStack.EMPTY; }
        @Override public ItemStack removeItem(int slot, int amount) { ItemStack result = ContainerHelper.removeItem(items, slot, amount); if (!result.isEmpty()) setChanged(); return result; }
        @Override public ItemStack removeItemNoUpdate(int slot) { ItemStack result = ContainerHelper.takeItem(items, slot); if (!result.isEmpty()) setChanged(); return result; }
        @Override public void setItem(int slot, ItemStack stack) { if (slot < 0 || slot >= items.size()) return; items.set(slot, stack); if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize()); setChanged(); }
        @Override public void setChanged() { data.setDirty(); }
        @Override public boolean stillValid(Player player) { return true; }
        @Override public void clearContent() { items.clear(); setChanged(); }
    }
}
