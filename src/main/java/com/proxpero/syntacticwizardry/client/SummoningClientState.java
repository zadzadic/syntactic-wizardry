package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.RitualDefinition;
import com.proxpero.syntacticwizardry.SummoningSyncPayload;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class SummoningClientState {
    public record Entry(SummoningSyncPayload.Entry payload, long syncGameTime) {}

    private static List<Entry> entries = List.of();

    private SummoningClientState() {}

    public static void apply(SummoningSyncPayload payload) {
        List<Entry> next = new ArrayList<>();
        ActiveRitualClientRegistry.clearRitual(RitualDefinition.SUMMONING);

        for (SummoningSyncPayload.Entry entry : payload.entries()) {
            next.add(new Entry(entry, payload.syncGameTime()));

            if (PreparedRitualPreview.prepared()
                    && PreparedRitualPreview.ritual() == RitualDefinition.SUMMONING
                    && PreparedRitualPreview.center() != null
                    && PreparedRitualPreview.center().asLong() == entry.center()) {
                PreparedRitualPreview.clear();
            }

            ActiveRitualClientRegistry.register(
                    entry.id(),
                    entry.name(),
                    RitualDefinition.SUMMONING,
                    BlockPos.of(entry.center()),
                    entry.paused(),
                    entry.powered(),
                    1.0D,
                    entry.stopping());
        }

        entries = List.copyOf(next);
    }

    public static void clear() {
        entries = List.of();
        ActiveRitualClientRegistry.clearRitual(RitualDefinition.SUMMONING);
    }
}
