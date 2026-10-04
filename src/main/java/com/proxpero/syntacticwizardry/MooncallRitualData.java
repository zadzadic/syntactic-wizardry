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

public final class MooncallRitualData extends SavedData {
    public static final String DATA_NAME = "syntacticwizardry_mooncall_rituals_v1";

    public static final class Entry {
        private final UUID id;
        private final BlockPos center;
        private final MooncallPhase phase;
        private String name;
        private int effectTicks;
        private boolean paused;
        private boolean powered;
        private boolean stopping;
        private boolean effectApplied;

        private Entry(UUID id, BlockPos center, MooncallPhase phase, String name,
                      int effectTicks, boolean paused, boolean powered,
                      boolean stopping, boolean effectApplied) {
            this.id = id;
            this.center = center.immutable();
            this.phase = phase == null ? MooncallPhase.FULL_MOON : phase;
            this.name = sanitizeName(name);
            this.effectTicks = RitualTransitionRules.clampTicks(effectTicks);
            this.paused = paused;
            this.powered = powered;
            this.stopping = stopping;
            this.effectApplied = effectApplied;
        }

        public UUID id() { return id; }
        public BlockPos center() { return center; }
        public MooncallPhase phase() { return phase; }
        public String name() { return name; }
        public int effectTicks() { return effectTicks; }
        public boolean paused() { return paused; }
        public boolean powered() { return powered; }
        public boolean stopping() { return stopping; }
        public boolean effectApplied() { return effectApplied; }

        public boolean targetActive() {
            return !paused && !stopping && powered;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    private MooncallRitualData() {}

    public static Factory<MooncallRitualData> factory() {
        return new Factory<>(MooncallRitualData::new, MooncallRitualData::load, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    public static MooncallRitualData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public static MooncallRitualData load(CompoundTag tag, HolderLookup.Provider registries) {
        MooncallRitualData data = new MooncallRitualData();
        ListTag list = tag.getList("Rituals", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag ritualTag = list.getCompound(i);
            if (!ritualTag.hasUUID("Id")) continue;
            int phaseIndex = ritualTag.getInt("Phase");
            MooncallPhase[] phases = MooncallPhase.values();
            MooncallPhase phase = phaseIndex >= 0 && phaseIndex < phases.length
                    ? phases[phaseIndex]
                    : MooncallPhase.FULL_MOON;

            data.entries.add(new Entry(
                    ritualTag.getUUID("Id"),
                    BlockPos.of(ritualTag.getLong("Center")),
                    phase,
                    ritualTag.getString("Name"),
                    ritualTag.getInt("EffectTicks"),
                    ritualTag.getBoolean("Paused"),
                    ritualTag.getBoolean("Powered"),
                    ritualTag.getBoolean("Stopping"),
                    ritualTag.getBoolean("EffectApplied")));
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
            ritualTag.putInt("Phase", entry.phase.ordinal());
            ritualTag.putString("Name", entry.name);
            ritualTag.putInt("EffectTicks", entry.effectTicks);
            ritualTag.putBoolean("Paused", entry.paused);
            ritualTag.putBoolean("Powered", entry.powered);
            ritualTag.putBoolean("Stopping", entry.stopping);
            ritualTag.putBoolean("EffectApplied", entry.effectApplied);
            list.add(ritualTag);
        }
        tag.put("Rituals", list);
        return tag;
    }

    public List<Entry> entries() {
        return List.copyOf(entries);
    }

    public Entry activate(ServerLevel level, BlockPos center, MooncallPhase phase) {
        Entry entry = new Entry(
                UUID.randomUUID(),
                center,
                phase,
                "Mooncall",
                0,
                false,
                RitualManaSupport.tryConsumeUpkeep(level, center, 1.0D),
                false,
                false);
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

            if (!entry.stopping
                    && !MooncallRitualStructure.activeStructureValid(level, entry.center, entry.phase)) {
                entry.stopping = true;
                entry.paused = false;
                entry.powered = false;
                syncChanged = true;
                dirty = true;
            }

            if (entry.stopping || entry.paused) {
                if (entry.powered) {
                    entry.powered = false;
                    syncChanged = true;
                    dirty = true;
                }
            } else if (time % 20L == 0L) {
                boolean powered = RitualManaSupport.tryConsumeUpkeep(level, entry.center, 1.0D);
                if (powered != entry.powered) {
                    entry.powered = powered;
                    syncChanged = true;
                    dirty = true;
                }
            }

            int previousTicks = entry.effectTicks;
            entry.effectTicks = RitualTransitionRules.step(entry.effectTicks, entry.targetActive());
            if (entry.effectTicks != previousTicks) dirty = true;

            if (!entry.effectApplied
                    && entry.targetActive()
                    && entry.effectTicks >= RitualTransitionRules.DURATION_TICKS) {
                entry.effectApplied = true;
                syncChanged = true;
                dirty = true;
            }

            if (entry.effectApplied
                    && !entry.targetActive()
                    && entry.effectTicks <= 0) {
                entry.effectApplied = false;
                syncChanged = true;
                dirty = true;
            }

            if (entry.stopping && entry.effectTicks <= 0) {
                iterator.remove();
                syncChanged = true;
                dirty = true;
            }
        }

        if (dirty) setDirty();
        return syncChanged;
    }

    public Entry find(UUID id) {
        if (id == null) return null;
        for (Entry entry : entries) if (entry.id.equals(id)) return entry;
        return null;
    }

    public boolean setPaused(UUID id, boolean paused) {
        Entry entry = find(id);
        if (entry == null || entry.stopping || entry.paused == paused) return false;
        entry.paused = paused;
        if (paused) entry.powered = false;
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
        Entry entry = find(id);
        if (entry == null || entry.stopping) return false;

        if (entry.effectTicks <= 0) {
            entries.remove(entry);
        } else {
            entry.stopping = true;
            entry.paused = false;
            entry.powered = false;
        }

        setDirty();
        return true;
    }

    public int currentPhaseOverride() {
        for (int i = entries.size() - 1; i >= 0; i--) {
            Entry entry = entries.get(i);
            if (entry.effectApplied) return entry.phase.vanillaPhase();
        }
        return -1;
    }

    private static String sanitizeName(String name) {
        String value = name == null ? "" : name.trim();
        if (value.isEmpty()) return "Mooncall";
        return value.length() > 32 ? value.substring(0, 32) : value;
    }
}
