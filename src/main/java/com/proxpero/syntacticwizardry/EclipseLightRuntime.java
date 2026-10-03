package com.proxpero.syntacticwizardry;

import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.WeakHashMap;

public final class EclipseLightRuntime {
    private static final Map<Level, Integer> EXTRA_DARKEN = new WeakHashMap<>();

    private EclipseLightRuntime() {}

    public static synchronized void set(Level level, int value) {
        if (level == null) return;
        int clamped = Math.max(0, Math.min(15, value));
        if (clamped == 0) EXTRA_DARKEN.remove(level);
        else EXTRA_DARKEN.put(level, clamped);
    }

    public static synchronized int get(Level level) {
        if (level == null) return 0;
        return EXTRA_DARKEN.getOrDefault(level, 0);
    }

    public static synchronized void clear(Level level) {
        if (level != null) EXTRA_DARKEN.remove(level);
    }
}
