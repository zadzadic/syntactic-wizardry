package com.proxpero.syntacticwizardry;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

/** Applies the timed, Potence-scaled Flight state to resolved players. */
public final class FlightEffect {
    private FlightEffect() {}

    public static void apply(ServerLevel level, Entity owner, ShapeResolution resolution, int potence, int durationExtensionTicks, boolean excludeCaster) {
        if (level == null || resolution == null) return;
        for (Entity entity : ResolvedTargets.entities(level, resolution, excludeCaster ? owner : null)) {
            if (entity instanceof ServerPlayer player && player.isAlive()) {
                FlightService.apply(level, player, potence, durationExtensionTicks);
            }
        }
    }
}
