package com.proxpero.syntacticwizardry;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** World-persistent dimensional inventories, keyed by their owning player's UUID. */
public final class DimensionalStorageSavedData extends SavedData {
    public static final String DATA_NAME = "syntacticwizardry_dimensional_storage";
    public static final int MAX_SLOTS = SpellPresentation.POTENCE_MAX * 10;
    private final Map<UUID, NonNullList<ItemStack>> inventories = new HashMap<>();

    public static Factory<DimensionalStorageSavedData> factory() {
        return new Factory<>(DimensionalStorageSavedData::new, DimensionalStorageSavedData::load, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    public NonNullList<ItemStack> inventory(UUID owner) {
        return inventories.computeIfAbsent(owner, ignored -> NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY));
    }

    public static DimensionalStorageSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        DimensionalStorageSavedData data = new DimensionalStorageSavedData();
        ListTag players = tag.getList("Players", Tag.TAG_COMPOUND);
        for (int i = 0; i < players.size(); i++) {
            CompoundTag entry = players.getCompound(i);
            if (!entry.hasUUID("Owner")) continue;
            NonNullList<ItemStack> items = NonNullList.withSize(MAX_SLOTS, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(entry, items, registries);
            data.inventories.put(entry.getUUID("Owner"), items);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag players = new ListTag();
        for (Map.Entry<UUID, NonNullList<ItemStack>> entry : inventories.entrySet()) {
            CompoundTag stored = new CompoundTag();
            stored.putUUID("Owner", entry.getKey());
            ContainerHelper.saveAllItems(stored, entry.getValue(), true, registries);
            players.add(stored);
        }
        tag.put("Players", players);
        return tag;
    }
}
