package com.proxpero.syntacticwizardry.client;

import com.proxpero.syntacticwizardry.LuminalBridgeSyncPayload;
import com.proxpero.syntacticwizardry.RitualDefinition;
import net.minecraft.core.BlockPos;

public final class LuminalBridgeClientState {
    private LuminalBridgeClientState() {}

    public static void apply(LuminalBridgeSyncPayload payload) {
        ActiveRitualClientRegistry.clearRitual(RitualDefinition.LUMINAL_BRIDGE);
        for (LuminalBridgeSyncPayload.Entry entry : payload.entries()) {
            ActiveRitualClientRegistry.register(
                    entry.id(),
                    entry.name(),
                    RitualDefinition.LUMINAL_BRIDGE,
                    BlockPos.of(entry.center()),
                    entry.paused(),
                    entry.powered(),
                    1.0D,
                    false);
        }
    }
}
