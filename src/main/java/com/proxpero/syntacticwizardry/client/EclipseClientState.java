package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.EclipseSyncPayload;
import com.proxpero.syntacticwizardry.RitualDefinition;
import com.proxpero.syntacticwizardry.RitualTransitionRules;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class EclipseClientState {
    public record Entry(EclipseSyncPayload.Entry payload, long syncGameTime) {}

    private static List<Entry> entries = List.of();

    private EclipseClientState() {}

    public static void apply(EclipseSyncPayload payload) {
        List<Entry> next = new ArrayList<>();
        ActiveRitualClientRegistry.clearRitual(RitualDefinition.ECLIPSE);
        for (EclipseSyncPayload.Entry entry : payload.entries()) {
            next.add(new Entry(entry, payload.syncGameTime()));
            if (PreparedRitualPreview.prepared()
                    && PreparedRitualPreview.ritual() == RitualDefinition.ECLIPSE
                    && PreparedRitualPreview.center() != null
                    && PreparedRitualPreview.center().asLong() == entry.center()) {
                PreparedRitualPreview.clear();
            }
            ActiveRitualClientRegistry.register(
                    entry.id(),
                    entry.name(),
                    RitualDefinition.ECLIPSE,
                    BlockPos.of(entry.center()),
                    entry.paused(),
                    entry.powered(),
                    1.0D,
                    entry.stopping());
        }
        entries = List.copyOf(next);
    }

    public static int currentReduction(ClientLevel level, float partialTick) {
        return Math.round(currentReductionFloat(level, partialTick));
    }

    public static float currentReductionFloat(ClientLevel level, float partialTick) {
        if (level == null) return 0.0F;
        float result = 0.0F;
        for (Entry entry : entries) {
            EclipseSyncPayload.Entry state = entry.payload();
            float progress = interpolatedProgress(level, partialTick, entry);
            result = Math.max(result, Math.min(15.0F, state.potence() * progress));
        }
        return result;
    }

    public static float visualProgress(ClientLevel level, float partialTick) {
        if (level == null) return 0.0F;
        float result = 0.0F;
        for (Entry entry : entries) {
            result = Math.max(result, interpolatedProgress(level, partialTick, entry));
        }
        return result;
    }

    private static float interpolatedProgress(ClientLevel level, float partialTick, Entry entry) {
        EclipseSyncPayload.Entry state = entry.payload();
        double now = level.getGameTime() + partialTick;
        double elapsed = Math.max(0.0D, now - entry.syncGameTime());
        boolean targetActive = !state.paused() && !state.stopping() && state.powered();
        return RitualTransitionRules.interpolatedProgress(state.effectTicks(), elapsed, targetActive);
    }

    public static void clear() {
        entries = List.of();
        ActiveRitualClientRegistry.clearRitual(RitualDefinition.ECLIPSE);
    }
}
