package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** Records the block-space immediately above a struck surface for later Recall. */
public final class MarkEffect {
    private MarkEffect() {}

    public static void apply(SpellExecutionContext context) {
        if (context == null || context.owner() == null || context.parent().directEntity() != null) return;
        BlockPos hitBlock = findHitBlock(context.level(), context.parent());
        if (hitBlock == null) return;
        TeleportMarkSavedData data = context.level().getServer().overworld().getDataStorage()
                .computeIfAbsent(TeleportMarkSavedData.factory(), TeleportMarkSavedData.DATA_NAME);
        data.setMark(context.owner().getUUID(), context.level().dimension().location(), hitBlock.above());
    }

    private static BlockPos findHitBlock(ServerLevel level, ShapeResolution resolution) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : resolution.voxels()) {
            if (level.getBlockState(pos).isAir()) continue;
            double distance = Vec3.atCenterOf(pos).distanceToSqr(resolution.origin());
            if (distance < bestDistance) {
                best = pos;
                bestDistance = distance;
            }
        }
        if (best != null) return best;

        Vec3 normal = resolution.surfaceNormal();
        if (normal == null || normal.lengthSqr() <= 1.0E-8D) return null;
        BlockPos frame = BlockPos.containing(resolution.origin());
        int dx = 0, dy = 0, dz = 0;
        double ax = Math.abs(normal.x), ay = Math.abs(normal.y), az = Math.abs(normal.z);
        if (ax >= ay && ax >= az) dx = normal.x >= 0.0D ? 1 : -1;
        else if (ay >= ax && ay >= az) dy = normal.y >= 0.0D ? 1 : -1;
        else dz = normal.z >= 0.0D ? 1 : -1;
        BlockPos behind = frame.offset(-dx, -dy, -dz);
        if (!level.getBlockState(behind).isAir()) return behind;
        if (!level.getBlockState(frame).isAir()) return frame;
        return null;
    }
}
