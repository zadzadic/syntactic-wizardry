package com.proxpero.syntacticwizardry;

import net.minecraft.world.entity.LivingEntity;

public final class HealthService {
    private HealthService() {}

    public static float addHealth(LivingEntity target, float amount) {
        if (target == null || !target.isAlive() || amount <= 0.0F) return 0.0F;
        float before = target.getHealth();
        target.heal(amount);
        return Math.max(0.0F, target.getHealth() - before);
    }
}
