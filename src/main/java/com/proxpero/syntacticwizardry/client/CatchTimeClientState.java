package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.CatchTimeSetting;
import com.proxpero.syntacticwizardry.CatchTimeSyncPayload;
import com.proxpero.syntacticwizardry.RitualDefinition;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public final class CatchTimeClientState {
    public record Entry(CatchTimeSyncPayload.Entry payload, long syncGameTime) {}

    private static List<Entry> entries = List.of();

    private CatchTimeClientState() {}

    public static void apply(CatchTimeSyncPayload payload) {
        List<Entry> next = new ArrayList<>();
        ActiveRitualClientRegistry.clearRitual(RitualDefinition.CATCH_TIME);

        for (CatchTimeSyncPayload.Entry entry : payload.entries()) {
            next.add(new Entry(entry, payload.syncGameTime()));

            if (PreparedRitualPreview.prepared()
                    && PreparedRitualPreview.ritual() == RitualDefinition.CATCH_TIME
                    && PreparedRitualPreview.center() != null
                    && PreparedRitualPreview.center().asLong() == entry.center()) {
                PreparedRitualPreview.clear();
            }

            ActiveRitualClientRegistry.register(
                    entry.id(), entry.name(), RitualDefinition.CATCH_TIME,
                    BlockPos.of(entry.center()), entry.paused(), entry.powered(), 1.0D, entry.stopping());
        }

        entries = List.copyOf(next);
    }

    public static CatchTimeSetting currentSettingOverride() {
        CatchTimeSetting[] settings = CatchTimeSetting.values();
        for (int i = entries.size() - 1; i >= 0; i--) {
            CatchTimeSyncPayload.Entry entry = entries.get(i).payload();
            if (!entry.effectApplied()) continue;
            int index = entry.setting();
            return index >= 0 && index < settings.length ? settings[index] : CatchTimeSetting.NOON;
        }
        return null;
    }

    public static void clear() {
        entries = List.of();
        ActiveRitualClientRegistry.clearRitual(RitualDefinition.CATCH_TIME);
    }
}