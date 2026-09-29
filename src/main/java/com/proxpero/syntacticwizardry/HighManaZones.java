package com.proxpero.syntacticwizardry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Deterministic seed-based High Mana chunk property, analogous to slime chunks. */
public final class HighManaZones {
    public static final int RARITY_DENOMINATOR = 256;
    private static final int MAX_SEARCH_RADIUS_CHUNKS = 256;

    public record Target(int chunkX, int chunkZ) {
        public int blockX() { return (chunkX << 4) + 8; }
        public int blockZ() { return (chunkZ << 4) + 8; }
    }

    private HighManaZones() {}

    public static boolean isNaturallyHighMana(ServerLevel level, int chunkX, int chunkZ) {
        long dimensionSalt = mix64(level.dimension().location().toString().hashCode() * 0x9E3779B97F4A7C15L);
        long value = level.getSeed() ^ dimensionSalt;
        value ^= mix64((long) chunkX * 0xC2B2AE3D27D4EB4FL);
        value ^= mix64((long) chunkZ * 0x165667B19E3779F9L);
        return Long.remainderUnsigned(mix64(value), RARITY_DENOMINATOR) == 0L;
    }

    public static boolean isHighMana(ServerLevel level, int chunkX, int chunkZ) {
        return isNaturallyHighMana(level, chunkX, chunkZ)
                && !PylonNetworkData.get(level).isSuppressed(level, chunkX, chunkZ);
    }

    public static Target nearestUnclaimed(ServerLevel level, Player player, List<HighManaClaimSavedData.Claim> claimed) {
        ResourceLocation dimension = level.dimension().location();
        Set<Long> excluded = new HashSet<>();
        for (HighManaClaimSavedData.Claim claim : claimed) {
            if (claim.dimension().equals(dimension)) excluded.add(chunkKey(claim.chunkX(), claim.chunkZ()));
        }
        return nearest(level, player, excluded);
    }

    private static Target nearest(ServerLevel level, Player player, Set<Long> excluded) {
        int originX = player.chunkPosition().x;
        int originZ = player.chunkPosition().z;
        double px = player.getX();
        double pz = player.getZ();
        Target best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        int firstFoundRing = -1;

        for (int radius = 0; radius <= MAX_SEARCH_RADIUS_CHUNKS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    int chunkX = originX + dx;
                    int chunkZ = originZ + dz;
                    if (excluded.contains(chunkKey(chunkX, chunkZ))) continue;
                    if (!isHighMana(level, chunkX, chunkZ)) continue;
                    double tx = (chunkX << 4) + 8.0D;
                    double tz = (chunkZ << 4) + 8.0D;
                    double distance = (tx - px) * (tx - px) + (tz - pz) * (tz - pz);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = new Target(chunkX, chunkZ);
                    }
                    if (firstFoundRing < 0) firstFoundRing = radius;
                }
            }
            if (firstFoundRing >= 0 && radius >= firstFoundRing + 2) break;
        }
        return best;
    }

    private static long chunkKey(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }
}
