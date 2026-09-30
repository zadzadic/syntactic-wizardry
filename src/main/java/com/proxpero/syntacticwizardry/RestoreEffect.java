package com.proxpero.syntacticwizardry;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

/** Applies Restore to targets already selected by the incoming Shape resolution. */
public final class RestoreEffect {
    private RestoreEffect() {}

    public static void apply(ServerLevel level, net.minecraft.world.entity.Entity owner, ShapeResolution resolution, int potence, boolean excludeCaster) {
        int resolvedPotence = Math.max(SpellPresentation.POTENCE_MIN, Math.min(SpellPresentation.POTENCE_MAX, potence));
        float amount = 0.5F + 0.5F * resolvedPotence;
        for (LivingEntity living : ResolvedTargets.living(level, resolution, excludeCaster ? owner : null)) {
            living.heal(amount);
        }
        BlockRestorationService.restore(level, resolution.voxels());
    }
}
