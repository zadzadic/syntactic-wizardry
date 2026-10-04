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

public final class EclipseRitualData extends SavedData {
    public static final String DATA_NAME = "syntacticwizardry_eclipse_rituals_v1";
    public static final int TRANSITION_TICKS = 200;

    public static final class Entry {
        private final UUID id;
        private final BlockPos center;
        private final int potence;
        private final List<EclipseRitualStructure.FocusRef> foci;
        private String name;
        private int transitionTicks;
        private boolean paused;
        private boolean powered;

        private Entry(UUID id, BlockPos center, int potence,
                      List<EclipseRitualStructure.FocusRef> foci,
                      String name, int transitionTicks, boolean paused, boolean powered) {
            this.id = id;
            this.center = center.immutable();
            this.potence = Math.max(1, potence);
            this.foci = List.copyOf(foci);
            this.name = sanitizeName(name);
            this.transitionTicks = Math.max(0, Math.min(TRANSITION_TICKS, transitionTicks));
            this.paused = paused;
            this.powered = powered;
        }

        public UUID id() { return id; }
        public BlockPos center() { return center; }
        public int potence() { return potence; }
        public List<EclipseRitualStructure.FocusRef> foci() { return foci; }
        public String name() { return name; }
        public int transitionTicks() { return transitionTicks; }
        public boolean paused() { return paused; }
        public boolean powered() { return powered; }
    }

    private final List<Entry> entries = new ArrayList<>();

    private EclipseRitualData() {}

    public static Factory<EclipseRitualData> factory() {
        return new Factory<>(EclipseRitualData::new, EclipseRitualData::load, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    public static EclipseRitualData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public static EclipseRitualData load(CompoundTag tag, HolderLookup.Provider registries) {
        EclipseRitualData data = new EclipseRitualData();
        ListTag list = tag.getList("Rituals", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag ritualTag = list.getCompound(i);
            if (!ritualTag.hasUUID("Id")) continue;

            List<EclipseRitualStructure.FocusRef> foci = new ArrayList<>();
            ListTag focusTags = ritualTag.getList("Foci", Tag.TAG_COMPOUND);
            for (int j = 0; j < focusTags.size(); j++) {
                CompoundTag focusTag = focusTags.getCompound(j);
                int materialIndex = focusTag.getInt("Material");
                if (materialIndex < 0 || materialIndex >= RitualStructureRules.FocusMaterial.values().length) continue;
                foci.add(new EclipseRitualStructure.FocusRef(
                        BlockPos.of(focusTag.getLong("Pos")),
                        RitualStructureRules.FocusMaterial.values()[materialIndex]));
            }

            data.entries.add(new Entry(
                    ritualTag.getUUID("Id"),
                    BlockPos.of(ritualTag.getLong("Center")),
                    ritualTag.getInt("Potence"),
                    foci,
                    ritualTag.getString("Name"),
                    ritualTag.getInt("Transition"),
                    ritualTag.getBoolean("Paused"),
                    ritualTag.getBoolean("Powered")));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Entry entry : entries) {
            CompoundTag ritualTag = new CompoundTag();
            ritualTag.putUUID("Id", entry.id);
            ritualTag.putLong("Center", entry.center.asLong());
            ritualTag.putInt("Potence", entry.potence);
            ritualTag.putString("Name", entry.name);
            ritualTag.putInt("Transition", entry.transitionTicks);
            ritualTag.putBoolean("Paused", entry.paused);
            ritualTag.putBoolean("Powered", entry.powered);

            ListTag focusTags = new ListTag();
            for (EclipseRitualStructure.FocusRef focus : entry.foci) {
                CompoundTag focusTag = new CompoundTag();
                focusTag.putLong("Pos", focus.position().asLong());
                focusTag.putInt("Material", focus.material().ordinal());
                focusTags.add(focusTag);
            }
            ritualTag.put("Foci", focusTags);
            list.add(ritualTag);
        }
        tag.put("Rituals", list);
        return tag;
    }

    public List<Entry> entries() {
        return List.copyOf(entries);
    }

    public Entry activate(ServerLevel level, BlockPos center, int potence,
                          List<EclipseRitualStructure.FocusRef> foci) {
        Entry entry = new Entry(
                UUID.randomUUID(),
                center,
                potence,
                foci,
                "Eclipse",
                0,
                false,
                RitualManaSupport.tryConsumeUpkeep(level, center, 1.0D));
        entries.add(entry);
        setDirty();
        return entry;
    }

    public boolean tick(ServerLevel level) {
        boolean syncChanged = false;
        boolean dirty = false;
        long time = level.getGameTime();

        Iterator<Entry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Entry entry = iterator.next();
            if (!EclipseRitualStructure.activeStructureValid(level, entry.center, entry.foci)) {
                iterator.remove();
                syncChanged = true;
                dirty = true;
                continue;
            }

            if (entry.paused) {
                if (entry.powered) {
                    entry.powered = false;
                    syncChanged = true;
                }
            } else if (time % 20L == 0L) {
                boolean powered = RitualManaSupport.tryConsumeUpkeep(level, entry.center, 1.0D);
                if (powered != entry.powered) {
                    entry.powered = powered;
                    syncChanged = true;
                }
            }

            if (!entry.paused && entry.powered && entry.transitionTicks < TRANSITION_TICKS) {
                entry.transitionTicks++;
                if (time % 20L == 0L || entry.transitionTicks == TRANSITION_TICKS) dirty = true;
            }
        }

        if (dirty || syncChanged) setDirty();
        return syncChanged;
    }

    public Entry find(UUID id) {
        if (id == null) return null;
        for (Entry entry : entries) if (entry.id.equals(id)) return entry;
        return null;
    }

    public boolean setPaused(UUID id, boolean paused) {
        Entry entry = find(id);
        if (entry == null || entry.paused == paused) return false;
        entry.paused = paused;
        setDirty();
        return true;
    }

    public boolean rename(UUID id, String name) {
        Entry entry = find(id);
        if (entry == null) return false;
        String sanitized = sanitizeName(name);
        if (entry.name.equals(sanitized)) return false;
        entry.name = sanitized;
        setDirty();
        return true;
    }

    public boolean stop(UUID id) {
        boolean removed = entries.removeIf(entry -> entry.id.equals(id));
        if (removed) setDirty();
        return removed;
    }

    public int currentReduction() {
        int result = 0;
        for (Entry entry : entries) {
            if (entry.paused || !entry.powered) continue;
            int reduction = (int)Math.floor(entry.potence * (entry.transitionTicks / (double)TRANSITION_TICKS));
            result = Math.max(result, Math.min(15, reduction));
        }
        return result;
    }

    private static String sanitizeName(String name) {
        String value = name == null ? "" : name.trim();
        if (value.isEmpty()) return "Eclipse";
        return value.length() > 32 ? value.substring(0, 32) : value;
    }
}
