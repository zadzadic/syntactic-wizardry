package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.BindingSyncPayload;
import com.proxpero.syntacticwizardry.RitualDefinition;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class BindingClientState {
    public record Entry(BindingSyncPayload.Entry payload, long syncGameTime) {}

    private static List<Entry> entries = List.of();

    private BindingClientState() {}

    public static void apply(BindingSyncPayload payload) {
        List<Entry> next = new ArrayList<>();
        ActiveRitualClientRegistry.clearRitual(RitualDefinition.BINDING);

        for (BindingSyncPayload.Entry entry : payload.entries()) {
            next.add(new Entry(entry, payload.syncGameTime()));

            if (PreparedRitualPreview.prepared()
                    && PreparedRitualPreview.ritual() == RitualDefinition.BINDING
                    && PreparedRitualPreview.center() != null
                    && PreparedRitualPreview.center().asLong() == entry.center()) {
                PreparedRitualPreview.clear();
            }

            ActiveRitualClientRegistry.register(
                    entry.id(),
                    entry.name(),
                    RitualDefinition.BINDING,
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
        ActiveRitualClientRegistry.clearRitual(RitualDefinition.BINDING);
    }
}
