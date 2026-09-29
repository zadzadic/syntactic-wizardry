package com.proxpero.syntacticwizardry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Runtime support for entity Move travel. Block Move uses MovedBlockEntity. */
public final class MoveTravelRuntime {
    public static final double BLOCKS_PER_POTENCE = 1.5D;
    private static final double MAX_DIRECTIONAL_SPEED = 1.5D;
    private static final double STOP_EPSILON = 0.025D;
    private static final int BLOCKED_TICKS_LIMIT = 3;
    private static final Map<UUID, Motion> ACTIVE = new HashMap<>();

    static { NeoForge.EVENT_BUS.addListener(MoveTravelRuntime::onServerTick); }

    private MoveTravelRuntime() {}

    public static void launch(Entity entity, Vec3 direction, int potence) {
        if (entity == null || entity.isRemoved()) return;
        Vec3 dir = normalized(direction);
        int p = clampPotence(potence);
        double distance = BLOCKS_PER_POTENCE * p;
        Motion motion = new Motion(entity, dir, distance, entity.position());
        ACTIVE.put(entity.getUUID(), motion);
        drive(motion);
    }

    static double blockImpulseForPotence(int potence) {
        return 0.15D * clampPotence(potence);
    }

    private static int clampPotence(int potence) {
        return Math.max(SpellPresentation.POTENCE_MIN, Math.min(SpellPresentation.POTENCE_MAX, potence));
    }

    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<Motion> iterator = ACTIVE.values().iterator();
        while (iterator.hasNext()) {
            Motion motion = iterator.next();
            Entity entity = motion.entity;

            if (entity == null || entity.isRemoved() || entity.level().isClientSide()) {
                iterator.remove();
                continue;
            }

            Vec3 now = entity.position();
            double projected = dot(now.subtract(motion.lastPosition), motion.direction);
            if (projected > 0.0D) motion.remaining = Math.max(0.0D, motion.remaining - projected);

            if (motion.remaining <= STOP_EPSILON) {
                // Reaching the nominal distance ends spell assistance only.
                // It never subtracts or zeroes the entity's remaining momentum.
                entity.hurtMarked = true;
                iterator.remove();
                continue;
            }

            if (projected <= STOP_EPSILON) motion.blockedTicks++;
            else motion.blockedTicks = 0;

            if (motion.blockedTicks >= BLOCKED_TICKS_LIMIT) {
                entity.hurtMarked = true;
                iterator.remove();
                continue;
            }

            drive(motion);
            motion.lastPosition = now;
        }
    }

    private static void drive(Motion motion) {
        Entity entity = motion.entity;
        double desired = Math.min(MAX_DIRECTIONAL_SPEED, motion.remaining);
        Vec3 current = entity.getDeltaMovement();
        double along = dot(current, motion.direction);
        double correction = desired - along;
        if (correction > 0.0D) entity.push(motion.direction.scale(correction));
        entity.hurtMarked = true;
    }

    private static Vec3 normalized(Vec3 value) {
        if (value == null || value.lengthSqr() <= 1.0E-8D) return new Vec3(0.0D, 1.0D, 0.0D);
        return value.normalize();
    }

    private static double dot(Vec3 a, Vec3 b) {
        return a.x * b.x + a.y * b.y + a.z * b.z;
    }

    private static final class Motion {
        final Entity entity;
        final Vec3 direction;
        double remaining;
        Vec3 lastPosition;
        int blockedTicks;

        Motion(Entity entity, Vec3 direction, double remaining, Vec3 lastPosition) {
            this.entity = entity;
            this.direction = direction;
            this.remaining = remaining;
            this.lastPosition = lastPosition;
        }
    }
}
