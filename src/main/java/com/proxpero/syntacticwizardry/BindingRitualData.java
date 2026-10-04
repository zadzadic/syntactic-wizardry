package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class BindingRitualData extends SavedData {
    public static final String DATA_NAME = "syntacticwizardry_binding_rituals_v1";

    public static final class Entry {
        private final UUID id;
        private final BlockPos center;
        private final UUID ownerId;
        private String name;
        private int effectTicks;
        private boolean paused;
        private boolean powered;
        private boolean stopping;

        private Entry(
                UUID id,
                BlockPos center,
                UUID ownerId,
                String name,
                int effectTicks,
                boolean paused,
                boolean powered,
                boolean stopping) {
            this.id = id;
            this.center = center.immutable();
            this.ownerId = ownerId;
            this.name = sanitizeName(name);
            this.effectTicks = RitualTransitionRules.clampTicks(effectTicks);
            this.paused = paused;
            this.powered = powered;
            this.stopping = stopping;
        }

        public UUID id() { return id; }
        public BlockPos center() { return center; }
        public UUID ownerId() { return ownerId; }
        public String name() { return name; }
        public int effectTicks() { return effectTicks; }
        public boolean paused() { return paused; }
        public boolean powered() { return powered; }
        public boolean stopping() { return stopping; }

        public boolean targetActive() {
            return !paused && !stopping && powered;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    private BindingRitualData() {}

    public static Factory<BindingRitualData> factory() {
        return new Factory<>(
                BindingRitualData::new,
                BindingRitualData::load,
                DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    public static BindingRitualData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public static BindingRitualData load(CompoundTag tag, HolderLookup.Provider registries) {
        BindingRitualData data = new BindingRitualData();
        ListTag list = tag.getList("Rituals", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag ritualTag = list.getCompound(i);
            if (!ritualTag.hasUUID("Id") || !ritualTag.hasUUID("Owner")) continue;

            data.entries.add(new Entry(
                    ritualTag.getUUID("Id"),
                    BlockPos.of(ritualTag.getLong("Center")),
                    ritualTag.getUUID("Owner"),
                    ritualTag.getString("Name"),
                    ritualTag.getInt("EffectTicks"),
                    ritualTag.getBoolean("Paused"),
                    ritualTag.getBoolean("Powered"),
                    ritualTag.getBoolean("Stopping")));
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
            ritualTag.putUUID("Owner", entry.ownerId);
            ritualTag.putString("Name", entry.name);
            ritualTag.putInt("EffectTicks", entry.effectTicks);
            ritualTag.putBoolean("Paused", entry.paused);
            ritualTag.putBoolean("Powered", entry.powered);
            ritualTag.putBoolean("Stopping", entry.stopping);
            list.add(ritualTag);
        }

        tag.put("Rituals", list);
        return tag;
    }

    public List<Entry> entries() {
        return List.copyOf(entries);
    }

    public Entry activate(ServerLevel level, BlockPos center, UUID ownerId) {
        Entry entry = new Entry(
                UUID.randomUUID(),
                center,
                ownerId,
                "Binding",
                0,
                false,
                RitualManaSupport.tryConsumeUpkeep(level, center, 1.0D),
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
                    && !BindingRitualStructure.activeStructureValid(level, entry.center)) {
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

            if (entry.targetActive()
                    && entry.effectTicks >= RitualTransitionRules.DURATION_TICKS) {
                int bound = applyBinding(level, entry);
                notifyOwner(level, entry.ownerId, bound);
                iterator.remove();
                syncChanged = true;
                dirty = true;
                continue;
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

    private static int applyBinding(ServerLevel level, Entry entry) {
        AABB area = new AABB(
                entry.center.getX() - 3.0D,
                entry.center.getY() - 0.25D,
                entry.center.getZ() - 3.0D,
                entry.center.getX() + 4.0D,
                entry.center.getY() + 4.0D,
                entry.center.getZ() + 4.0D);

        List<Mob> mobs = level.getEntitiesOfClass(Mob.class, area, Mob::isAlive);
        BoundCreatureData bindings = BoundCreatureData.get(level.getServer());

        int count = 0;
        for (Mob mob : mobs) {
            bindings.bind(mob, entry.ownerId);
            count++;
        }
        return count;
    }

    private static void notifyOwner(ServerLevel level, UUID ownerId, int bound) {
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(ownerId);
        if (owner == null) return;

        owner.displayClientMessage(
                net.minecraft.network.chat.Component.literal(
                        "Binding complete: " + bound + (bound == 1 ? " creature bound." : " creatures bound.")),
                false);
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

    private static String sanitizeName(String name) {
        String value = name == null ? "" : name.trim();
        if (value.isEmpty()) return "Binding";
        return value.length() > 32 ? value.substring(0, 32) : value;
    }
}
