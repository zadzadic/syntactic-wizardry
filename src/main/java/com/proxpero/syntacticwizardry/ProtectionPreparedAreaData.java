package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/** Persisted Armillary area selections waiting for their physical Protection ritual to be activated. */
public final class ProtectionPreparedAreaData extends SavedData {
    public static final String DATA_NAME = "syntacticwizardry_protection_prepared_areas_v1";

    public record Config(UUID ownerId, BlockPos ritualCenter, ProtectionAreaShape shape,
                         int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {}

    private final List<Config> configs = new ArrayList<>();

    private ProtectionPreparedAreaData() {}

    public static Factory<ProtectionPreparedAreaData> factory() {
        return new Factory<>(ProtectionPreparedAreaData::new, ProtectionPreparedAreaData::load,
                DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    public static ProtectionPreparedAreaData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public static ProtectionPreparedAreaData load(CompoundTag tag, HolderLookup.Provider registries) {
        ProtectionPreparedAreaData data = new ProtectionPreparedAreaData();
        ListTag list = tag.getList("Areas", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!entry.hasUUID("Owner")) continue;
            data.configs.add(new Config(
                    entry.getUUID("Owner"),
                    BlockPos.of(entry.getLong("Center")),
                    ProtectionAreaShape.fromOrdinal(entry.getInt("Shape")),
                    entry.getInt("MinX"), entry.getInt("MinY"), entry.getInt("MinZ"),
                    entry.getInt("MaxX"), entry.getInt("MaxY"), entry.getInt("MaxZ")));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Config config : configs) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("Owner", config.ownerId());
            entry.putLong("Center", config.ritualCenter().asLong());
            entry.putInt("Shape", config.shape().ordinal());
            entry.putInt("MinX", config.minX());
            entry.putInt("MinY", config.minY());
            entry.putInt("MinZ", config.minZ());
            entry.putInt("MaxX", config.maxX());
            entry.putInt("MaxY", config.maxY());
            entry.putInt("MaxZ", config.maxZ());
            list.add(entry);
        }
        tag.put("Areas", list);
        return tag;
    }

    public void put(Config config) {
        if (config == null) return;
        // The Armillary only exposes one prepared ritual per player at a time.
        // Replacing by owner prevents stale area records from competing with the current plan.
        configs.removeIf(existing -> existing.ownerId().equals(config.ownerId()));
        configs.add(config);
        setDirty();
    }

    public void remove(UUID ownerId, BlockPos center) {
        if (ownerId == null || center == null) return;
        if (configs.removeIf(existing -> existing.ownerId().equals(ownerId)
                && existing.ritualCenter().equals(center))) setDirty();
    }

    public void removeOwner(UUID ownerId) {
        if (ownerId == null) return;
        if (configs.removeIf(existing -> existing.ownerId().equals(ownerId))) setDirty();
    }

    public boolean hasOwner(UUID ownerId) {
        if (ownerId == null) return false;
        for (Config config : configs) {
            if (config.ownerId().equals(ownerId)) return true;
        }
        return false;
    }

    public Config consume(UUID ownerId) {
        if (ownerId == null) return null;
        Iterator<Config> iterator = configs.iterator();
        while (iterator.hasNext()) {
            Config config = iterator.next();
            if (!config.ownerId().equals(ownerId)) continue;
            iterator.remove();
            setDirty();
            return config;
        }
        return null;
    }
}