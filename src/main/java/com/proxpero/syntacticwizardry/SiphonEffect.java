package com.proxpero.syntacticwizardry;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** Generic resource transfer Effect over targets supplied by ShapeResolution. */
public final class SiphonEffect {
    private SiphonEffect() {}

    public static void apply(ServerLevel level, Entity owner, ShapeResolution resolution, int resource, int mode, int potence, boolean excludeCaster) {
        int resolvedPotence = Math.max(SpellPresentation.POTENCE_MIN, Math.min(SpellPresentation.POTENCE_MAX, potence));
        float requestedDamage = 1.0F + resolvedPotence;
        for (LivingEntity target : ResolvedTargets.living(level, resolution, excludeCaster ? owner : null)) {
            if (mode == SpellPresentation.SIPHON_SEND) {
                send(level, owner, target, resource, requestedDamage);
            } else {
                drain(level, owner, target, resource, requestedDamage);
            }
        }
    }

    private static void drain(ServerLevel level, Entity owner, LivingEntity target, int resource, float requestedDamage) {
        float siphoned = damage(level, target, requestedDamage);
        replenish(owner, resource, siphoned);
    }

    private static void send(ServerLevel level, Entity owner, LivingEntity target, int resource, float requestedDamage) {
        if (!(owner instanceof LivingEntity caster) || !caster.isAlive()) return;
        float siphoned = damage(level, caster, requestedDamage);
        replenish(target, resource, siphoned);
    }

    private static float damage(ServerLevel level, LivingEntity victim, float requestedDamage) {
        if (victim == null || !victim.isAlive() || requestedDamage <= 0.0F) return 0.0F;
        float before = victim.getHealth();
        if (!victim.hurt(level.damageSources().generic(), requestedDamage)) return 0.0F;
        return Math.max(0.0F, before - victim.getHealth());
    }

    private static void replenish(Entity recipient, int resource, float siphoned) {
        if (recipient == null || siphoned <= 0.0F) return;
        if (resource == SpellPresentation.SIPHON_MANA) {
            ManaService.addMana(recipient, siphoned * 0.25F);
        } else if (recipient instanceof LivingEntity living) {
            HealthService.addHealth(living, siphoned * 0.50F);
        }
    }
}
