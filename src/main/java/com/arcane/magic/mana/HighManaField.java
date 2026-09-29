package com.arcane.magic.mana;

import com.proxpero.syntacticwizardry.HighManaZones;
import net.minecraft.server.level.ServerLevel;

/** Compatibility bridge from the legacy Builder charge check to the current High Mana system. */
public final class HighManaField {
    private HighManaField() {}
    public static boolean isHighMana(ServerLevel level, int chunkX, int chunkZ) {
        return HighManaZones.isHighMana(level, chunkX, chunkZ);
    }
}
