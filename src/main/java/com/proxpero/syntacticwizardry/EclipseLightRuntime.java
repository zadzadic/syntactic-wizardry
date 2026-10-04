package com.proxpero.syntacticwizardry;

import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.WeakHashMap;

public final class EclipseLightRuntime {
    private static final Map<Level, Float> EXTRA_DARKEN = new WeakHashMap<>();

    private EclipseLightRuntime() {}

    public static synchronized void set(Level level, float value) {
        if (level == null) return;
        float clamped = Math.max(0.0F, Math.min(15.0F, value));
        if (clamped <= 0.0001F) EXTRA_DARKEN.remove(level);
        else EXTRA_DARKEN.put(level, clamped);
    }

    public static synchronized int get(Level level) {
        return Math.round(getFloat(level));
    }

    public static synchronized float getFloat(Level level) {
        if (level == null) return 0.0F;
        return EXTRA_DARKEN.getOrDefault(level, 0.0F);
    }

    public static synchronized void clear(Level level) {
        if (level != null) EXTRA_DARKEN.remove(level);
    }
}
