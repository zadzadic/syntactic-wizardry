package com.proxpero.syntacticwizardry;

import net.minecraft.world.entity.LivingEntity;

public final class HighManaCompassClientState {
    private static boolean hasTarget;
    private static String dimension = "";
    private static int chunkX;
    private static int chunkZ;

    private HighManaCompassClientState() {}

    public static void set(HighManaCompassStatePayload payload) {
        hasTarget = payload.hasTarget();
        dimension = payload.dimension();
        chunkX = payload.chunkX();
        chunkZ = payload.chunkZ();
    }

    public static float angle(LivingEntity entity) {
        if (!hasTarget || entity == null) return 0.0F;
        if (!entity.level().dimension().location().toString().equals(dimension)) return 0.0F;
        double targetX = (chunkX << 4) + 8.0D;
        double targetZ = (chunkZ << 4) + 8.0D;
        double dx = targetX - entity.getX();
        double dz = targetZ - entity.getZ();
        double targetDegrees = Math.toDegrees(Math.atan2(-dx, dz));
        double relative = (targetDegrees - entity.getYRot()) % 360.0D;
        if (relative < 0.0D) relative += 360.0D;
        int frame = ((int) Math.floor((relative + 5.625D) / 11.25D)) & 31;
        return frame / 32.0F;
    }
}
