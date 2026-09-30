package com.arcane.magic.mana;

import net.minecraft.server.level.ServerLevel;

/** Compatibility bridge used by the copied Arcane Builder item. */
public final class HighManaField {
    private HighManaField() {}
    public static boolean isHighMana(ServerLevel level, int chunkX, int chunkZ) {
        return com.proxpero.syntacticwizardry.HighManaZones.isHighMana(level, chunkX, chunkZ);
    }
}
