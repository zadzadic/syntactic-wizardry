package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.MooncallSyncPayload;
import com.proxpero.syntacticwizardry.RitualDefinition;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class MooncallClientState {
    public record Entry(MooncallSyncPayload.Entry payload, long syncGameTime) {}

    private static List<Entry> entries = List.of();

    private MooncallClientState() {}

    public static void apply(MooncallSyncPayload payload) {
        List<Entry> next = new ArrayList<>();
        ActiveRitualClientRegistry.clearRitual(RitualDefinition.MOONCALL);

        for (MooncallSyncPayload.Entry entry : payload.entries()) {
            next.add(new Entry(entry, payload.syncGameTime()));

            if (PreparedRitualPreview.prepared()
                    && PreparedRitualPreview.ritual() == RitualDefinition.MOONCALL
                    && PreparedRitualPreview.center() != null
                    && PreparedRitualPreview.center().asLong() == entry.center()) {
                PreparedRitualPreview.clear();
            }

            ActiveRitualClientRegistry.register(
                    entry.id(),
                    entry.name(),
                    RitualDefinition.MOONCALL,
                    BlockPos.of(entry.center()),
                    entry.paused(),
                    entry.powered(),
                    1.0D,
                    entry.stopping());
        }

        entries = List.copyOf(next);
    }

    public static int currentPhaseOverride() {
        for (int i = entries.size() - 1; i >= 0; i--) {
            MooncallSyncPayload.Entry entry = entries.get(i).payload();
            if (entry.effectApplied()) return entry.phase();
        }
        return -1;
    }

    public static void clear() {
        entries = List.of();
        ActiveRitualClientRegistry.clearRitual(RitualDefinition.MOONCALL);
    }
}
