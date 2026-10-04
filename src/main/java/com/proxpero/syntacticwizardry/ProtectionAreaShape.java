package com.proxpero.syntacticwizardry;

public enum ProtectionAreaShape {
    BOX,
    SPHERE,
    CYLINDER;

    public static ProtectionAreaShape fromOrdinal(int ordinal) {
        ProtectionAreaShape[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : SPHERE;
    }
}