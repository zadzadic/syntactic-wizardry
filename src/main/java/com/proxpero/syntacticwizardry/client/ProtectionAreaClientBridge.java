package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.ProtectionAreaShape;
import com.proxpero.syntacticwizardry.ProtectionPreparedAreaPayload;
import com.proxpero.syntacticwizardry.RitualDefinition;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ProtectionAreaClientBridge {
    private ProtectionAreaClientBridge() {}

    public static void onPrepared(RitualDefinition ritual, BlockPos center) {
        if (ritual != RitualDefinition.PROTECTION || center == null) {
            clearPrepared();
            return;
        }

        int[] bounds = ConduitPlannerState.bounds();
        if (bounds == null || bounds.length < 6) {
            PacketDistributor.sendToServer(new ProtectionPreparedAreaPayload(
                    center.asLong(), ConduitPlannerState.areaShape().ordinal(),
                    0, 0, 0, 0, 0, 0, false));
            return;
        }

        PacketDistributor.sendToServer(new ProtectionPreparedAreaPayload(
                center.asLong(), ConduitPlannerState.areaShape().ordinal(),
                bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5], true));
    }

    public static void clearPrepared() {
        PacketDistributor.sendToServer(new ProtectionPreparedAreaPayload(
                0L, ProtectionAreaShape.SPHERE.ordinal(),
                0, 0, 0, 0, 0, 0, false));
    }
}