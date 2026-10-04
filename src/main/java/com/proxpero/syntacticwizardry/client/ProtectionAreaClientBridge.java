package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.ProtectionAreaShape;
import com.proxpero.syntacticwizardry.ProtectionPreparedAreaPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ProtectionAreaClientBridge {
    private static boolean committed;

    private ProtectionAreaClientBridge() {}

    public static boolean commitCurrentArea() {
        int[] bounds = ConduitPlannerState.bounds();
        if (bounds == null || bounds.length < 6) return false;

        PacketDistributor.sendToServer(new ProtectionPreparedAreaPayload(
                0L, ConduitPlannerState.areaShape().ordinal(),
                bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5], true));
        committed = true;
        return true;
    }

    public static boolean committed() {
        return committed;
    }

    public static void areaChanged() {
        if (!committed) return;
        clearPrepared();
    }

    public static void clearPrepared() {
        committed = false;
        PacketDistributor.sendToServer(new ProtectionPreparedAreaPayload(
                0L, ProtectionAreaShape.SPHERE.ordinal(),
                0, 0, 0, 0, 0, 0, false));
    }
}