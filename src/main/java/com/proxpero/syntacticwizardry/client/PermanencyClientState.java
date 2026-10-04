package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.PermanencySyncPayload;
import com.proxpero.syntacticwizardry.RitualDefinition;
import com.proxpero.syntacticwizardry.RitualTransitionRules;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class PermanencyClientState {
    public record Entry(PermanencySyncPayload.Entry payload, long syncGameTime) {}

    private static List<Entry> entries = List.of();

    private PermanencyClientState() {}

    public static void apply(PermanencySyncPayload payload) {
        List<Entry> next = new ArrayList<>();
        ActiveRitualClientRegistry.clearRitual(RitualDefinition.PERMANENCY);

        for (PermanencySyncPayload.Entry entry : payload.entries()) {
            next.add(new Entry(entry, payload.syncGameTime()));

            if (PreparedRitualPreview.prepared()
                    && PreparedRitualPreview.ritual() == RitualDefinition.PERMANENCY
                    && PreparedRitualPreview.center() != null
                    && PreparedRitualPreview.center().asLong() == entry.center()) {
                PreparedRitualPreview.clear();
                PermanencyClientBridge.markConsumed();
            }

            ActiveRitualClientRegistry.register(
                    entry.id(),
                    entry.name(),
                    RitualDefinition.PERMANENCY,
                    BlockPos.of(entry.center()),
                    entry.paused(),
                    entry.powered(),
                    1.0D,
                    entry.stopping());
        }

        entries = List.copyOf(next);
    }

    public static List<Entry> entries() {
        return entries;
    }

    public static float interpolatedProgress(ClientLevel level, float partialTick, Entry entry) {
        if (level == null || entry == null) return 0.0F;
        PermanencySyncPayload.Entry state = entry.payload();
        double now = level.getGameTime() + partialTick;
        double elapsed = Math.max(0.0D, now - entry.syncGameTime());
        boolean targetActive = !state.paused() && !state.stopping() && state.powered();
        return RitualTransitionRules.interpolatedProgress(state.effectTicks(), elapsed, targetActive);
    }

    public static void clear() {
        entries = List.of();
        ActiveRitualClientRegistry.clearRitual(RitualDefinition.PERMANENCY);
    }
}
