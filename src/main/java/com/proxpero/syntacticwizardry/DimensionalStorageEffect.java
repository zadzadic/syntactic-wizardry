package com.proxpero.syntacticwizardry;

import net.minecraft.server.level.ServerPlayer;

/** Opens the dimensional inventory owned by the directly resolved player target. */
public final class DimensionalStorageEffect {
    private DimensionalStorageEffect() {}

    public static void apply(SpellExecutionContext context) {
        if (!(context.owner() instanceof ServerPlayer viewer)) return;
        if (!(context.parent().directEntity() instanceof ServerPlayer target)) return;
        if (context.excludeCaster() && target == context.owner()) return;
        int capacity = DimensionalStorageService.capacityForPotence(context.potence());
        if (viewer.containerMenu instanceof DimensionalStorageMenu menu && menu.matches(target.getUUID(), capacity)) return;
        DimensionalStorageService.open(viewer, target, context.potence());
    }
}
