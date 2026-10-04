package com.proxpero.syntacticwizardry;

/**
 * Shared transition timing for all sustained Rituals.
 *
 * Ritual effects ramp from zero to full strength over ten seconds and ramp
 * back to zero over ten seconds whenever the Ritual is paused, stopped,
 * unpowered, or otherwise ceases to be active.
 */
public final class RitualTransitionRules {
    public static final int DURATION_TICKS = 20 * 10;

    private RitualTransitionRules() {}

    public static int step(int effectTicks, boolean targetActive) {
        int current = clampTicks(effectTicks);
        if (targetActive) return Math.min(DURATION_TICKS, current + 1);
        return Math.max(0, current - 1);
    }

    public static int clampTicks(int effectTicks) {
        return Math.max(0, Math.min(DURATION_TICKS, effectTicks));
    }

    public static float progress(int effectTicks) {
        return clampTicks(effectTicks) / (float)DURATION_TICKS;
    }

    public static double interpolatedTicks(int effectTicks, double elapsedTicks, boolean targetActive) {
        double elapsed = Math.max(0.0D, elapsedTicks);
        double result = clampTicks(effectTicks) + (targetActive ? elapsed : -elapsed);
        return Math.max(0.0D, Math.min(DURATION_TICKS, result));
    }

    public static float interpolatedProgress(int effectTicks, double elapsedTicks, boolean targetActive) {
        return (float)(interpolatedTicks(effectTicks, elapsedTicks, targetActive) / DURATION_TICKS);
    }
}
