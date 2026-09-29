package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Persistent Recall marks, keyed by the caster that created them. */
public final class TeleportMarkSavedData extends SavedData {
    public static final String DATA_NAME = "syntacticwizardry_teleport_marks";

    public record Mark(ResourceLocation dimension, BlockPos position) {}

    private final Map<UUID, Mark> marks = new HashMap<>();

    public static Factory<TeleportMarkSavedData> factory() {
        return new Factory<>(TeleportMarkSavedData::new, TeleportMarkSavedData::load, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    public Mark mark(UUID caster) {
        return caster == null ? null : marks.get(caster);
    }

    public void setMark(UUID caster, ResourceLocation dimension, BlockPos position) {
        if (caster == null || dimension == null || position == null) return;
        marks.put(caster, new Mark(dimension, position.immutable()));
        setDirty();
    }

    public static TeleportMarkSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        TeleportMarkSavedData data = new TeleportMarkSavedData();
        ListTag stored = tag.getList("Marks", Tag.TAG_COMPOUND);
        for (int i = 0; i < stored.size(); i++) {
            CompoundTag entry = stored.getCompound(i);
            if (!entry.hasUUID("Caster") || !entry.contains("Dimension", Tag.TAG_STRING) || !entry.contains("Position", Tag.TAG_LONG)) continue;
            ResourceLocation dimension = ResourceLocation.tryParse(entry.getString("Dimension"));
            if (dimension == null) continue;
            data.marks.put(entry.getUUID("Caster"), new Mark(dimension, BlockPos.of(entry.getLong("Position"))));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag stored = new ListTag();
        for (Map.Entry<UUID, Mark> entry : marks.entrySet()) {
            CompoundTag mark = new CompoundTag();
            mark.putUUID("Caster", entry.getKey());
            mark.putString("Dimension", entry.getValue().dimension().toString());
            mark.putLong("Position", entry.getValue().position().asLong());
            stored.add(mark);
        }
        tag.put("Marks", stored);
        return tag;
    }
}
