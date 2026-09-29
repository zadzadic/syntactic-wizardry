package com.arcane.magic.mana;

import com.proxpero.syntacticwizardry.PylonNetworkData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/** Compatibility bridge from the legacy Builder charge check to current Power Cores. */
public final class PowerCoreSupport {
    private PowerCoreSupport() {}
    public static boolean isPoweredChunk(ServerLevel level, BlockPos pos) {
        int cx = pos.getX() >> 4;
        int cz = pos.getZ() >> 4;
        for (BlockPos core : PylonNetworkData.get(level).linkedCores(level)) {
            if ((core.getX() >> 4) == cx && (core.getZ() >> 4) == cz
                    && PylonNetworkData.get(level).outputForCore(level, core) > 0) return true;
        }
        return false;
    }
}
