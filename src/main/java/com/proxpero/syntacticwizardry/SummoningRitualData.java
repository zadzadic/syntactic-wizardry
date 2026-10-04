package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class SummoningRitualData extends SavedData {
    public static final String DATA_NAME = "syntacticwizardry_summoning_rituals_v1";

    public static final class Entry {
        private final UUID id;
        private final BlockPos center;
        private String name;
        private int effectTicks;
        private boolean paused;
        private boolean powered;
        private boolean stopping;
        private final Set<UUID> processedItems = new HashSet<>();

        private Entry(UUID id, BlockPos center, String name, int effectTicks,
                      boolean paused, boolean powered, boolean stopping) {
            this.id = id;
            this.center = center.immutable();
            this.name = sanitizeName(name);
            this.effectTicks = RitualTransitionRules.clampTicks(effectTicks);
            this.paused = paused;
            this.powered = powered;
            this.stopping = stopping;
        }

        public UUID id() { return id; }
        public BlockPos center() { return center; }
        public String name() { return name; }
        public int effectTicks() { return effectTicks; }
        public boolean paused() { return paused; }
        public boolean powered() { return powered; }
        public boolean stopping() { return stopping; }

        public boolean targetActive() {
            return !paused && !stopping && powered;
        }

        public boolean acceptsCatalysts() {
            if (targetActive()) return effectTicks >= RitualTransitionRules.DURATION_TICKS;
            return effectTicks > 0;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    private SummoningRitualData() {}

    public static Factory<SummoningRitualData> factory() {
        return new Factory<>(SummoningRitualData::new, SummoningRitualData::load, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    public static SummoningRitualData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public static SummoningRitualData load(CompoundTag tag, HolderLookup.Provider registries) {
        SummoningRitualData data = new SummoningRitualData();
        ListTag list = tag.getList("Rituals", Tag.TAG_COMPOUND);

        for (int i = 0; i < list.size(); i++) {
            CompoundTag ritualTag = list.getCompound(i);
            if (!ritualTag.hasUUID("Id")) continue;

            data.entries.add(new Entry(
                    ritualTag.getUUID("Id"),
                    BlockPos.of(ritualTag.getLong("Center")),
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

    public Entry activate(ServerLevel level, BlockPos center) {
        Entry entry = new Entry(
                UUID.randomUUID(),
                center,
                "Summoning",
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
                    && !SummoningRitualStructure.activeStructureValid(level, entry.center)) {
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

            if (entry.acceptsCatalysts()) {
                processCatalysts(level, entry);
            } else {
                entry.processedItems.clear();
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

    private static void processCatalysts(ServerLevel level, Entry entry) {
        AABB area = new AABB(
                entry.center.getX() - 1.0D,
                entry.center.getY() - 0.25D,
                entry.center.getZ() - 1.0D,
                entry.center.getX() + 2.0D,
                entry.center.getY() + 2.5D,
                entry.center.getZ() + 2.0D);

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area, ItemEntity::isAlive);
        Set<UUID> present = new HashSet<>();
        for (ItemEntity item : items) present.add(item.getUUID());
        entry.processedItems.retainAll(present);

        for (ItemEntity item : items) {
            if (entry.processedItems.contains(item.getUUID())) continue;

            ItemStack stack = item.getItem();
            EntityType<? extends Mob> type = SummoningCatalysts.resolve(stack);
            if (type == null) continue;

            Mob spawned = type.spawn(level, entry.center, MobSpawnType.TRIGGERED);
            if (spawned == null) continue;

            entry.processedItems.add(item.getUUID());

            if (stack.getCount() <= 1) {
                item.discard();
            } else {
                ItemStack remainder = stack.copy();
                remainder.shrink(1);
                item.setItem(remainder);
            }

            break;
        }
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
        if (value.isEmpty()) return "Summoning";
        return value.length() > 32 ? value.substring(0, 32) : value;
    }
}
