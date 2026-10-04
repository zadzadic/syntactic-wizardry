package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.RitualDefinition;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Client view of rituals known to an Armillary.
 * Server ritual runtime can populate and update this registry when implemented.
 */
public final class ActiveRitualClientRegistry {
    public static final class Entry {
        private final UUID id;
        private String name;
        private final RitualDefinition ritual;
        private final BlockPos center;
        private boolean paused;
        private boolean powered;
        private double manaDrawPerSecond;
        private boolean stopping;

        private Entry(UUID id, String name, RitualDefinition ritual, BlockPos center,
                      boolean paused, boolean powered, double manaDrawPerSecond, boolean stopping) {
            this.id = id;
            this.name = name;
            this.ritual = ritual;
            this.center = center.immutable();
            this.paused = paused;
            this.powered = powered;
            this.manaDrawPerSecond = Math.max(0.0D, manaDrawPerSecond);
            this.stopping = stopping;
        }

        public UUID id() { return id; }
        public String name() { return name; }
        public RitualDefinition ritual() { return ritual; }
        public BlockPos center() { return center; }
        public boolean paused() { return paused; }
        public boolean powered() { return powered; }
        public double manaDrawPerSecond() { return manaDrawPerSecond; }
        public boolean stopping() { return stopping; }
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    private ActiveRitualClientRegistry() {}

    public static List<Entry> entries() {
        return Collections.unmodifiableList(ENTRIES);
    }

    public static Entry register(UUID id, String name, RitualDefinition ritual, BlockPos center, boolean paused) {
        return register(id, name, ritual, center, paused, true, 1.0D, false);
    }

    public static Entry register(UUID id, String name, RitualDefinition ritual, BlockPos center,
                                 boolean paused, boolean powered, double manaDrawPerSecond) {
        return register(id, name, ritual, center, paused, powered, manaDrawPerSecond, false);
    }

    public static Entry register(UUID id, String name, RitualDefinition ritual, BlockPos center,
                                 boolean paused, boolean powered, double manaDrawPerSecond,
                                 boolean stopping) {
        Entry existing = find(id);
        if (existing != null) {
            existing.name = sanitize(name, ritual);
            existing.paused = paused;
            existing.powered = powered;
            existing.manaDrawPerSecond = Math.max(0.0D, manaDrawPerSecond);
            existing.stopping = stopping;
            return existing;
        }
        Entry entry = new Entry(
                id,
                sanitize(name, ritual),
                ritual,
                center,
                paused,
                powered,
                manaDrawPerSecond,
                stopping);
        ENTRIES.add(entry);
        return entry;
    }

    public static void replaceAll(List<Entry> entries) {
        ENTRIES.clear();
        if (entries != null) ENTRIES.addAll(entries);
    }

    public static Entry find(UUID id) {
        if (id == null) return null;
        for (Entry entry : ENTRIES) if (id.equals(entry.id)) return entry;
        return null;
    }

    public static void setPaused(UUID id, boolean paused) {
        Entry entry = find(id);
        if (entry != null) entry.paused = paused;
    }

    public static void rename(UUID id, String name) {
        Entry entry = find(id);
        if (entry != null) entry.name = sanitize(name, entry.ritual);
    }

    public static void stop(UUID id) {
        ENTRIES.removeIf(entry -> entry.id.equals(id));
    }

    public static double totalManaDrawPerSecond() {
        double total = 0.0D;
        for (Entry entry : ENTRIES) {
            if (entry.paused || entry.stopping || !entry.powered) continue;
            total += entry.manaDrawPerSecond;
        }
        return total;
    }

    public static void clearRitual(RitualDefinition ritual) {
        ENTRIES.removeIf(entry -> entry.ritual == ritual);
    }

    public static void clear() {
        ENTRIES.clear();
    }

    private static String sanitize(String name, RitualDefinition ritual) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) return ritual == null ? "Ritual" : ritual.displayName();
        return trimmed.length() > 32 ? trimmed.substring(0, 32) : trimmed;
    }
}
