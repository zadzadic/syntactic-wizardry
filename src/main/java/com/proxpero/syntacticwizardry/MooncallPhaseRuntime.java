package com.proxpero.syntacticwizardry;

import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.WeakHashMap;

public final class MooncallPhaseRuntime {
    private static final Map<Level, Integer> PHASE_OVERRIDE = new WeakHashMap<>();

    private MooncallPhaseRuntime() {}

    public static synchronized void set(Level level, int phase) {
        if (level == null) return;
        if (phase < 0 || phase > 7) PHASE_OVERRIDE.remove(level);
        else PHASE_OVERRIDE.put(level, phase);
    }

    public static synchronized int get(Level level) {
        if (level == null) return -1;
        return PHASE_OVERRIDE.getOrDefault(level, -1);
    }

    public static synchronized void clear(Level level) {
        if (level != null) PHASE_OVERRIDE.remove(level);
    }
}
