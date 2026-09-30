package com.arcane.magic.mana;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Compatibility bridge used by the copied Arcane Builder item. */
public final class PowerCoreSupport {
    private PowerCoreSupport() {}
    public static boolean isPoweredChunk(ServerLevel level, BlockPos position) {
        int cx = position.getX() >> 4;
        int cz = position.getZ() >> 4;
        for (BlockPos core : com.proxpero.syntacticwizardry.PylonNetworkData.get(level).linkedCores(level)) {
            if ((core.getX() >> 4) != cx || (core.getZ() >> 4) != cz) continue;
            if (!com.proxpero.syntacticwizardry.PowerCoreSupport.isValid(level, core)) continue;
            if (com.proxpero.syntacticwizardry.ManaGridSupport.coreStored(level, core) > 1.0E-6D) return true;
        }
        return false;
    }
}
