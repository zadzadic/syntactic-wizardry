package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.EclipseRitualData;
import com.proxpero.syntacticwizardry.EclipseSyncPayload;
import com.proxpero.syntacticwizardry.RitualDefinition;
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
            ActiveRitualClientRegistry.register(
                    entry.id(),
                    entry.name(),
                    RitualDefinition.ECLIPSE,
                    BlockPos.of(entry.center()),
                    entry.paused(),
                    entry.powered(),
                    1.0D);
        }
        entries = List.copyOf(next);
    }

    public static int currentReduction(ClientLevel level, float partialTick) {
        if (level == null) return 0;
        int result = 0;
        double now = level.getGameTime() + partialTick;
        for (Entry entry : entries) {
            EclipseSyncPayload.Entry state = entry.payload();
            if (state.paused() || !state.powered()) continue;
            double ticks = state.transitionTicks() + Math.max(0.0D, now - entry.syncGameTime());
            double progress = Math.min(1.0D, ticks / EclipseRitualData.TRANSITION_TICKS);
            result = Math.max(result, Math.min(15, (int)Math.floor(state.potence() * progress)));
        }
        return result;
    }

    public static float visualProgress(ClientLevel level, float partialTick) {
        if (level == null) return 0.0F;
        float result = 0.0F;
        double now = level.getGameTime() + partialTick;
        for (Entry entry : entries) {
            EclipseSyncPayload.Entry state = entry.payload();
            if (state.paused() || !state.powered()) continue;
            double ticks = state.transitionTicks() + Math.max(0.0D, now - entry.syncGameTime());
            result = Math.max(result, (float)Math.min(1.0D, ticks / EclipseRitualData.TRANSITION_TICKS));
        }
        return result;
    }

    public static void clear() {
        entries = List.of();
        ActiveRitualClientRegistry.clearRitual(RitualDefinition.ECLIPSE);
    }
}
