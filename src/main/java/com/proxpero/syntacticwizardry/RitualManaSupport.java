package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class RitualManaSupport {
    public static final int CHUNK_RADIUS = 3;

    private RitualManaSupport() {}

    public static boolean tryConsumeUpkeep(ServerLevel level, BlockPos center, double amount) {
        double cost = Math.max(0.0D, amount);
        if (cost <= 0.0D) return true;

        PylonNetworkData network = PylonNetworkData.get(level);

        for (BlockPos core : network.linkedCores(level)) {
            if (!withinRange(center, core)) continue;
            double stored = ManaGridSupport.coreStored(level, core);
            if (stored + 1.0E-6D < cost) continue;
            return ManaGridSupport.consume(level, core, cost) + 1.0E-6D >= cost;
        }

        for (BlockPos pylon : network.activePylons(level)) {
            if (withinRange(center, pylon)) return true;
        }

        int cx = center.getX() >> 4;
        int cz = center.getZ() >> 4;
        for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
            for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
                if (HighManaZones.isHighMana(level, cx + dx, cz + dz)) return true;
            }
        }

        return false;
    }

    private static boolean withinRange(BlockPos a, BlockPos b) {
        int ax = a.getX() >> 4;
        int az = a.getZ() >> 4;
        int bx = b.getX() >> 4;
        int bz = b.getZ() >> 4;
        return Math.max(Math.abs(ax - bx), Math.abs(az - bz)) <= CHUNK_RADIUS;
    }
}
