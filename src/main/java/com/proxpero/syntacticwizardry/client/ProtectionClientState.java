package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.ProtectionSyncPayload;
import com.proxpero.syntacticwizardry.RitualDefinition;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class ProtectionClientState {
    public record Entry(ProtectionSyncPayload.Entry payload, long syncGameTime) {}

    private static List<Entry> entries = List.of();

    private ProtectionClientState() {}

    public static void apply(ProtectionSyncPayload payload) {
        List<Entry> next = new ArrayList<>();
        ActiveRitualClientRegistry.clearRitual(RitualDefinition.PROTECTION);

        for (ProtectionSyncPayload.Entry entry : payload.entries()) {
            next.add(new Entry(entry, payload.syncGameTime()));

            if (PreparedRitualPreview.prepared()
                    && PreparedRitualPreview.ritual() == RitualDefinition.PROTECTION
                    && PreparedRitualPreview.center() != null
                    && PreparedRitualPreview.center().asLong() == entry.center()) {
                PreparedRitualPreview.clear();
            }

            ActiveRitualClientRegistry.register(
                    entry.id(), entry.name(), RitualDefinition.PROTECTION,
                    BlockPos.of(entry.center()), entry.paused(), entry.powered(), 1.0D, entry.stopping());
        }

        entries = List.copyOf(next);
    }

    public static void clear() {
        entries = List.of();
        ActiveRitualClientRegistry.clearRitual(RitualDefinition.PROTECTION);
    }
}