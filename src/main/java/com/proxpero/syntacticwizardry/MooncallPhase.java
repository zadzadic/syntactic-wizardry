package com.proxpero.syntacticwizardry;

public enum MooncallPhase {
    FULL_MOON(0, "Full Moon"),
    WANING_GIBBOUS(1, "Waning Gibbous"),
    LAST_QUARTER(2, "Last Quarter"),
    WANING_CRESCENT(3, "Waning Crescent"),
    NEW_MOON(4, "New Moon"),
    WAXING_CRESCENT(5, "Waxing Crescent"),
    FIRST_QUARTER(6, "First Quarter"),
    WAXING_GIBBOUS(7, "Waxing Gibbous");

    private final int vanillaPhase;
    private final String displayName;

    MooncallPhase(int vanillaPhase, String displayName) {
        this.vanillaPhase = vanillaPhase;
        this.displayName = displayName;
    }

    public int vanillaPhase() {
        return vanillaPhase;
    }

    public String displayName() {
        return displayName;
    }

    public MooncallPhase previous() {
        MooncallPhase[] values = values();
        return values[Math.floorMod(ordinal() - 1, values.length)];
    }

    public MooncallPhase next() {
        MooncallPhase[] values = values();
        return values[(ordinal() + 1) % values.length];
    }
}
