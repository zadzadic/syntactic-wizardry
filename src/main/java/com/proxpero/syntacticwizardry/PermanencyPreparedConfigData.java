package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/** Persisted Armillary Permanency configurations waiting for physical activation. */
public final class PermanencyPreparedConfigData extends SavedData {
    public static final String DATA_NAME = "syntacticwizardry_permanency_prepared_configs_v1";

    public record Config(
            UUID ownerId,
            BlockPos ritualCenter,
            ProtectionAreaShape shape,
            int minX, int minY, int minZ,
            int maxX, int maxY, int maxZ,
            float yaw,
            float pitch,
            int[] plan,
            int[] settings) {
        public Config {
            ritualCenter = ritualCenter == null ? BlockPos.ZERO : ritualCenter.immutable();
            shape = shape == null ? ProtectionAreaShape.BOX : shape;
            plan = normalizePlan(plan);
            settings = normalizeSettings(settings);
            int loX = Math.min(minX, maxX);
            int loY = Math.min(minY, maxY);
            int loZ = Math.min(minZ, maxZ);
            int hiX = Math.max(minX, maxX);
            int hiY = Math.max(minY, maxY);
            int hiZ = Math.max(minZ, maxZ);
            minX = loX;
            minY = loY;
            minZ = loZ;
            maxX = hiX;
            maxY = hiY;
            maxZ = hiZ;
            yaw = Float.isFinite(yaw) ? yaw : 0.0F;
            pitch = Float.isFinite(pitch) ? Math.max(-89.0F, Math.min(89.0F, pitch)) : 0.0F;
        }

        @Override
        public int[] plan() {
            return plan.clone();
        }

        @Override
        public int[] settings() {
            return settings.clone();
        }
    }

    private final List<Config> configs = new ArrayList<>();

    private PermanencyPreparedConfigData() {}

    public static Factory<PermanencyPreparedConfigData> factory() {
        return new Factory<>(
                PermanencyPreparedConfigData::new,
                PermanencyPreparedConfigData::load,
                DataFixTypes.SAVED_DATA_COMMAND_STORAGE);
    }

    public static PermanencyPreparedConfigData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public static PermanencyPreparedConfigData load(
            CompoundTag tag,
            HolderLookup.Provider registries) {
        PermanencyPreparedConfigData data = new PermanencyPreparedConfigData();
        ListTag list = tag.getList("Configs", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            if (!entry.hasUUID("Owner")) continue;
            data.configs.add(new Config(
                    entry.getUUID("Owner"),
                    BlockPos.of(entry.getLong("Center")),
                    ProtectionAreaShape.fromOrdinal(entry.getInt("Shape")),
                    entry.getInt("MinX"),
                    entry.getInt("MinY"),
                    entry.getInt("MinZ"),
                    entry.getInt("MaxX"),
                    entry.getInt("MaxY"),
                    entry.getInt("MaxZ"),
                    entry.getFloat("Yaw"),
                    entry.getFloat("Pitch"),
                    entry.getIntArray("Plan"),
                    entry.getIntArray("Settings")));
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
            entry.putFloat("Yaw", config.yaw());
            entry.putFloat("Pitch", config.pitch());
            entry.putIntArray("Plan", config.plan());
            entry.putIntArray("Settings", config.settings());
            list.add(entry);
        }
        tag.put("Configs", list);
        return tag;
    }

    public void put(Config config) {
        if (config == null || config.ownerId() == null) return;
        configs.removeIf(existing -> existing.ownerId().equals(config.ownerId()));
        configs.add(config);
        setDirty();
    }

    public void removeOwner(UUID ownerId) {
        if (ownerId == null) return;
        if (configs.removeIf(existing -> existing.ownerId().equals(ownerId))) setDirty();
    }

    public boolean has(UUID ownerId, BlockPos center) {
        return find(ownerId, center) != null;
    }

    public Config find(UUID ownerId, BlockPos center) {
        if (ownerId == null || center == null) return null;
        for (Config config : configs) {
            if (config.ownerId().equals(ownerId) && config.ritualCenter().equals(center)) return config;
        }
        return null;
    }

    public Config consume(UUID ownerId, BlockPos center) {
        if (ownerId == null || center == null) return null;
        Iterator<Config> iterator = configs.iterator();
        while (iterator.hasNext()) {
            Config config = iterator.next();
            if (!config.ownerId().equals(ownerId) || !config.ritualCenter().equals(center)) continue;
            iterator.remove();
            setDirty();
            return config;
        }
        return null;
    }

    public static boolean hasEffect(int[] plan) {
        if (plan == null) return false;
        for (int cell = 0; cell < SpellPresentation.CELLS; cell++) {
            SpellComponentDefinition definition =
                    SpellComponents.byType(SpellPresentation.typeAt(plan, cell));
            if (definition != null && SpellComponents.isEffect(definition)) return true;
        }
        return false;
    }

    private static int[] normalizePlan(int[] source) {
        int[] result = SpellPresentation.emptyPlan();
        if (source != null) {
            System.arraycopy(source, 0, result, 0, Math.min(source.length, result.length));
        }
        for (int cell = 0; cell < SpellPresentation.CELLS; cell++) {
            int type = SpellPresentation.typeAt(result, cell);
            SpellComponentDefinition definition = SpellComponents.byType(type);
            if (definition == null || definition.isShape()) {
                SpellPresentation.setCell(
                        result,
                        cell,
                        SpellPresentation.TYPE_EMPTY,
                        SpellPresentation.STYLE_DEFAULT,
                        SpellPresentation.VISUAL_DEFAULT);
            }
        }
        return result;
    }

    private static int[] normalizeSettings(int[] source) {
        int[] result = SpellPresentation.emptySettings();
        if (source == null) return result;

        for (int cell = 0; cell < SpellPresentation.CELLS; cell++) {
            for (SpellPropertyKey key : SpellPropertyKey.values()) {
                if (!key.isSetting()) continue;
                int index = cell * SpellPropertyKey.SETTING_COUNT + key.settingIndex();
                if (index >= source.length) continue;
                SpellPresentation.setSetting(result, cell, key, source[index]);
            }
        }
        return result;
    }
}
