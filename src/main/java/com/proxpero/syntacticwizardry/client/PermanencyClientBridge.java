package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.PermanencyPreparedConfigPayload;
import com.proxpero.syntacticwizardry.SpellComponentDefinition;
import com.proxpero.syntacticwizardry.SpellComponents;
import com.proxpero.syntacticwizardry.SpellPresentation;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.PacketDistributor;

public final class PermanencyClientBridge {
    private static boolean committed;

    private PermanencyClientBridge() {}

    public static boolean commitCurrentConfig() {
        BlockPos center = ConduitPlannerState.ritualCenter();
        int[] bounds = ConduitPlannerState.bounds();
        int[] plan = ConduitPlannerState.permanencyPlan();
        int[] settings = ConduitPlannerState.permanencySettings();

        if (center == null || bounds == null || bounds.length < 6 || !hasEffect(plan)) return false;

        PacketDistributor.sendToServer(new PermanencyPreparedConfigPayload(
                center.asLong(),
                ConduitPlannerState.areaShape().ordinal(),
                bounds[0], bounds[1], bounds[2],
                bounds[3], bounds[4], bounds[5],
                ConduitPlannerState.facingYaw(),
                ConduitPlannerState.facingPitch(),
                plan,
                settings,
                true));
        committed = true;
        return true;
    }

    public static boolean committed() {
        return committed;
    }

    public static void areaChanged() {
        if (committed) clearPrepared();
    }

    public static void clearPrepared() {
        if (!committed) return;
        committed = false;
        PacketDistributor.sendToServer(new PermanencyPreparedConfigPayload(
                0L, 0,
                0, 0, 0, 0, 0, 0,
                0.0F, 0.0F,
                new int[0], new int[0], false));
    }

    public static void markConsumed() {
        committed = false;
    }

    private static boolean hasEffect(int[] plan) {
        if (plan == null) return false;
        for (int cell = 0; cell < SpellPresentation.CELLS; cell++) {
            SpellComponentDefinition definition =
                    SpellComponents.byType(SpellPresentation.typeAt(plan, cell));
            if (definition != null && SpellComponents.isEffect(definition)) return true;
        }
        return false;
    }
}
