package com.proxpero.syntacticwizardry;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/** Metadata-free Move mechanic. Shape semantics arrive through ShapeResolution. */
public final class MoveEffect {
    private MoveEffect() {}

    public static void apply(ServerLevel level, Entity owner, ShapeResolution resolution, int targetType, int mode, int potence) {
        ForceService.apply(level, owner, resolution, targetType, mode, potence);
    }
}
