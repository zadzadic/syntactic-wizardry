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

public final class PermanencyRitualData extends SavedData {
    public static final String DATA_NAME = "syntacticwizardry_permanency_rituals_v1";

    public static final class Entry {
        private final UUID id;
        private final BlockPos center;
        private final UUID ownerId;
        private final ProtectionAreaShape shape;
        private final int minX;
        private final int minY;
        private final int minZ;
        private final int maxX;
        private final int maxY;
        private final int maxZ;
        private final float yaw;
        private final float pitch;
        private final int[] plan;
        private final int[] settings;
        private final int potence;
        private String name;
        private int effectTicks;
        private boolean paused;
        private boolean powered;
        private boolean stopping;
        private final transient PermanencyRuntime.State runtimeState = new PermanencyRuntime.State();

        private Entry(
                UUID id,
                BlockPos center,
                UUID ownerId,
                ProtectionAreaShape shape,
                int minX, int minY, int minZ,
                int maxX, int maxY, int maxZ,
                float yaw,
                float pitch,
                int[] plan,
                int[] settings,
                String name,
                int effectTicks,
                boolean paused,
                boolean powered,
                boolean stopping) {
            this.id = id;
            this.center = center.immutable();
            this.ownerId = ownerId;
            this.shape = shape == null ? ProtectionAreaShape.BOX : shape;
            this.minX = Math.min(minX, maxX);
            this.minY = Math.min(minY, maxY);
            this.minZ = Math.min(minZ, maxZ);
            this.maxX = Math.max(minX, maxX);
            this.maxY = Math.max(minY, maxY);
            this.maxZ = Math.max(minZ, maxZ);
            this.yaw = Float.isFinite(yaw) ? yaw : 0.0F;
            this.pitch = Float.isFinite(pitch) ? Math.max(-89.0F, Math.min(89.0F, pitch)) : 0.0F;
            this.plan = plan == null ? SpellPresentation.emptyPlan() : plan.clone();
            this.settings = settings == null ? SpellPresentation.emptySettings() : settings.clone();
            this.potence = highestPotence(this.plan, this.settings);
            this.name = sanitizeName(name);
            this.effectTicks = RitualTransitionRules.clampTicks(effectTicks);
            this.paused = paused;
            this.powered = powered;
            this.stopping = stopping;
        }

        public UUID id() { return id; }
        public BlockPos center() { return center; }
        public UUID ownerId() { return ownerId; }
        public ProtectionAreaShape shape() { return shape; }
        public int minX() { return minX; }
        public int minY() { return minY; }
        public int minZ() { return minZ; }
        public int maxX() { return maxX; }
        public int maxY() { return maxY; }
        public int maxZ() { return maxZ; }
        public float yaw() { return yaw; }
        public float pitch() { return pitch; }
        public int potence() { return potence; }
        public String name() { return name; }
        public int effectTicks() { return effectTicks; }
        public boolean paused() { return paused; }
        public boolean powered() { return powered; }
        public boolean stopping() { return stopping; }

        public int[] plan() { return plan.clone(); }
        public int[] settings() { return settings.clone(); }

        int[] planInternal() { return plan; }
        int[] settingsInternal() { return settings; }
        PermanencyRuntime.State runtimeState() { return runtimeState; }

        public boolean targetActive() {
            return !paused && !stopping && powered;
        }
    }

    private final List<Entry> entries = new ArrayList<>();

    private PermanencyRitualData() {}

    public static Factory<PermanencyRitualData> factory() {
        return new Factory<>(
                PermanencyRitualData::new,
                PermanencyRitualData::load,
                DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    public static PermanencyRitualData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public static PermanencyRitualData load(
            CompoundTag tag,
            HolderLookup.Provider registries) {
        PermanencyRitualData data = new PermanencyRitualData();
        ListTag list = tag.getList("Rituals", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag ritualTag = list.getCompound(i);
            if (!ritualTag.hasUUID("Id") || !ritualTag.hasUUID("Owner")) continue;
            data.entries.add(new Entry(
                    ritualTag.getUUID("Id"),
                    BlockPos.of(ritualTag.getLong("Center")),
                    ritualTag.getUUID("Owner"),
                    ProtectionAreaShape.fromOrdinal(ritualTag.getInt("Shape")),
                    ritualTag.getInt("MinX"),
                    ritualTag.getInt("MinY"),
                    ritualTag.getInt("MinZ"),
                    ritualTag.getInt("MaxX"),
                    ritualTag.getInt("MaxY"),
                    ritualTag.getInt("MaxZ"),
                    ritualTag.getFloat("Yaw"),
                    ritualTag.getFloat("Pitch"),
                    ritualTag.getIntArray("Plan"),
                    ritualTag.getIntArray("Settings"),
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
            ritualTag.putInt("Shape", entry.shape.ordinal());
            ritualTag.putInt("MinX", entry.minX);
            ritualTag.putInt("MinY", entry.minY);
            ritualTag.putInt("MinZ", entry.minZ);
            ritualTag.putInt("MaxX", entry.maxX);
            ritualTag.putInt("MaxY", entry.maxY);
            ritualTag.putInt("MaxZ", entry.maxZ);
            ritualTag.putFloat("Yaw", entry.yaw);
            ritualTag.putFloat("Pitch", entry.pitch);
            ritualTag.putIntArray("Plan", entry.plan);
            ritualTag.putIntArray("Settings", entry.settings);
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

    public Entry activate(
            ServerLevel level,
            BlockPos center,
            UUID ownerId,
            PermanencyPreparedConfigData.Config config) {
        Entry entry = new Entry(
                UUID.randomUUID(),
                center,
                ownerId,
                config.shape(),
                config.minX(),
                config.minY(),
                config.minZ(),
                config.maxX(),
                config.maxY(),
                config.maxZ(),
                config.yaw(),
                config.pitch(),
                config.plan(),
                config.settings(),
                "Permanency",
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

            if (!entry.stopping && !PermanencyRitualStructure.activeStructureValid(level, entry.center)) {
                entry.stopping = true;
                entry.paused = false;
                entry.powered = false;
                PermanencyRuntime.reset(entry);
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

            if (entry.targetActive() && entry.effectTicks >= RitualTransitionRules.DURATION_TICKS) {
                PermanencyRuntime.tick(level, entry);
            } else {
                PermanencyRuntime.reset(entry);
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
        if (paused) {
            entry.powered = false;
            PermanencyRuntime.reset(entry);
        }
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
        PermanencyRuntime.reset(entry);
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

    private static int highestPotence(int[] plan, int[] settings) {
        int result = 1;
        if (plan == null || settings == null) return result;
        for (int cell = 0; cell < SpellPresentation.CELLS; cell++) {
            SpellComponentDefinition definition =
                    SpellComponents.byType(SpellPresentation.typeAt(plan, cell));
            if (definition == null || !SpellComponents.isEffect(definition)) continue;
            for (SpellPropertyDefinition property : definition.settings()) {
                if (property.key() != SpellPropertyKey.POTENCE) continue;
                result = Math.max(result, SpellPresentation.potenceAt(settings, cell));
                break;
            }
        }
        return result;
    }

    private static String sanitizeName(String name) {
        String value = name == null ? "" : name.trim();
        if (value.isEmpty()) return "Permanency";
        return value.length() > 32 ? value.substring(0, 32) : value;
    }
}
