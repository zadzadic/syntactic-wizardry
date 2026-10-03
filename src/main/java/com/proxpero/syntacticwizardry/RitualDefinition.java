package com.proxpero.syntacticwizardry;

import java.util.List;

/** Design-time ritual entries exposed by the Conduit planner. */
public enum RitualDefinition {
    LUMINAL_BRIDGE("Luminal Bridge", false),
    PERMANENCY("Permanency", true),
    SUMMONING("Summoning", false),
    BINDING("Binding", false),
    PROTECTION("Protection", true),
    ECLIPSE("Eclipse", false),
    SUN_LORD("Sun-Lord", false),
    CLEAR_SKIES("Clear Skies", false),
    STORMLORD("Stormlord", false),
    ICE_CROWN("Ice Crown", false),
    CONSECRATE("Consecrate", true),
    ATTUNE("Attune", true),
    ALTER_BIOME("Alter Biome", true),
    ANCHOR("Anchor", true),
    SEEK("Seek", false),
    VERDANCY("Verdancy", true),
    MOONCALL("Mooncall", false),
    PURGE("Purge", true),
    LURE("Lure", true),
    DELUGE("Deluge", true),
    DROUGHT("Drought", true),
    MENDING("Mending", false),
    ILLUMINATE("Illuminate", true);

    private static final List<RitualDefinition> ENTRIES = List.of(values());

    private final String displayName;
    private final boolean variableArea;

    RitualDefinition(String displayName, boolean variableArea) {
        this.displayName = displayName;
        this.variableArea = variableArea;
    }

    public String displayName() {
        return displayName;
    }

    public boolean variableArea() {
        return variableArea;
    }

    public static List<RitualDefinition> entries() {
        return ENTRIES;
    }
}
