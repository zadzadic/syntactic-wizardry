package com.proxpero.syntacticwizardry;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Shape-scoped gravity. Potence controls directional suppression and active force. */
public final class GravityEffect {
    /** Potence 10 reproduces the old full gravity impulse. */
    public static final double FULL_FORCE_PER_TICK = 0.08D;
    private static final double POTENCE_STEP = 0.20D;
    static final double EPSILON = 1.0E-8D;

    private GravityEffect() {}

    public static void apply(ServerLevel level, Entity owner, ShapeResolution resolution, int potence, int mode, boolean blockInteraction) {
        if (level == null || resolution == null) return;

        boolean repel = mode == SpellPresentation.GRAVITY_REPEL;
        Vec3 center = resolution.origin();

        for (Entity target : ResolvedTargets.entities(level, resolution)) {
            if (target == null || target.isRemoved() || target instanceof GravityBlockEntity) continue;

            int effectivePotence = effectivePotence(target, potence);
            if (effectivePotence <= 0) continue;

            Vec3 gravityVector = gravityVector(center, target.position(), repel);
            applyMovementRule(target, gravityVector, effectivePotence);
        }

        if (blockInteraction) {
            GravityBlockRuntime.spawnArea(level, owner, resolution, potence, repel);
        }
    }

    static int effectivePotence(Entity target, int basePotence) {
        int clampedBase = Math.max(SpellPresentation.POTENCE_MIN, Math.min(SpellPresentation.POTENCE_MAX, basePotence));
        int heightBlocks = Math.max(1, (int) Math.ceil(target.getBbHeight()));
        int adjusted = clampedBase + (2 - heightBlocks);
        return Math.max(0, Math.min(SpellPresentation.POTENCE_MAX, adjusted));
    }

    static Vec3 gravityVector(Vec3 center, Vec3 targetPosition, boolean repel) {
        Vec3 delta = repel ? targetPosition.subtract(center) : center.subtract(targetPosition);
        return delta.lengthSqr() > EPSILON ? delta.normalize() : Vec3.ZERO;
    }

    static void applyMovementRule(Entity target, Vec3 gravityVector, int effectivePotence) {
        double suppression = Math.min(1.0D, effectivePotence * POTENCE_STEP);
        Vec3 current = target.getDeltaMovement();

        Vec3 constrained;
        if (target instanceof LivingEntity && effectivePotence >= 5) {
            // At Potence 5+, living entities cannot supply their own movement.
            constrained = Vec3.ZERO;
        } else {
            constrained = suppressNonGravityMovement(current, gravityVector, suppression);
        }

        target.setDeltaMovement(constrained);
        target.hurtMarked = true;

        if (effectivePotence >= 6 && gravityVector.lengthSqr() > EPSILON) {
            double activeStrength = Math.min(1.0D, (effectivePotence - 5) * POTENCE_STEP);
            double impulse = FULL_FORCE_PER_TICK * activeStrength;
            target.push(gravityVector.x * impulse, gravityVector.y * impulse, gravityVector.z * impulse);
            target.hurtMarked = true;
        }
    }

    private static Vec3 suppressNonGravityMovement(Vec3 movement, Vec3 gravityVector, double suppression) {
        if (movement.lengthSqr() <= EPSILON) return movement;
        if (gravityVector.lengthSqr() <= EPSILON) return movement.scale(1.0D - suppression);

        double along = movement.dot(gravityVector);
        Vec3 permitted = along > 0.0D ? gravityVector.scale(along) : Vec3.ZERO;
        Vec3 opposedOrSideways = movement.subtract(permitted);
        return permitted.add(opposedOrSideways.scale(1.0D - suppression));
    }
}
