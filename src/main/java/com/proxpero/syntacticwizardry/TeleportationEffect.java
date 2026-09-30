package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Teleport destinations are derived entirely from the incoming Shape frame and the selected mode. */
public final class TeleportationEffect {
    private static final int BLOCKS_PER_POTENCE = 10;
    private static final int BLINK_ATTEMPTS = 128;
    private static final int PORTAL_SEARCH_RADIUS = 1024;
    private static final int PORTAL_LANDING_RADIUS = 10;

    private TeleportationEffect() {}

    public static void apply(SpellExecutionContext context) {
        if (context == null) return;
        List<Entity> targets = ResolvedTargets.entities(context.level(), context.parent(), context.excludeCaster() ? context.owner() : null);
        if (targets.isEmpty()) return;
        int potence = Math.max(SpellPresentation.POTENCE_MIN, Math.min(SpellPresentation.POTENCE_MAX, context.potence()));
        for (Entity target : targets) {
            switch (context.teleportMode()) {
                case SpellPresentation.TELEPORT_BLINK -> blink(context.level(), target, potence);
                case SpellPresentation.TELEPORT_HOME -> home(target);
                case SpellPresentation.TELEPORT_RECALL -> recall(context, target);
                default -> directional(context, target, potence);
            }
        }
    }

    private static void directional(SpellExecutionContext context, Entity target, int potence) {
        Vec3 direction = context.parent().direction();
        if (direction == null || direction.lengthSqr() <= 1.0E-8D) direction = context.owner().getLookAngle();
        if (direction.lengthSqr() <= 1.0E-8D) return;
        direction = direction.normalize();
        double maximumDistance = BLOCKS_PER_POTENCE * (double) potence;
        for (double distance = maximumDistance; distance >= 1.0D; distance -= 1.0D) {
            Vec3 destination = target.position().add(direction.scale(distance));
            if (canFit(context.level(), target, destination)) {
                teleport(target, context.level(), destination);
                return;
            }
        }
    }

    private static void blink(ServerLevel level, Entity target, int potence) {
        int radius = BLOCKS_PER_POTENCE * potence;
        Vec3 origin = target.position();
        for (int attempt = 0; attempt < BLINK_ATTEMPTS; attempt++) {
            int dx = level.getRandom().nextInt(radius * 2 + 1) - radius;
            int dy = level.getRandom().nextInt(radius * 2 + 1) - radius;
            int dz = level.getRandom().nextInt(radius * 2 + 1) - radius;
            if (dx == 0 && dy == 0 && dz == 0) continue;
            if ((long) dx * dx + (long) dy * dy + (long) dz * dz > (long) radius * radius) continue;
            Vec3 destination = origin.add(dx, dy, dz);
            if (!canFit(level, target, destination)) continue;
            teleport(target, level, destination);
            return;
        }
    }

    private static void home(Entity target) {
        if (!(target instanceof ServerPlayer player)) return;
        MinecraftServer server = player.server;
        if (player.level().dimension().equals(Level.OVERWORLD)) {
            ServerLevel overworld = server.overworld();
            BlockPos respawn = player.getRespawnPosition();
            if (respawn != null && player.getRespawnDimension().equals(Level.OVERWORLD)) {
                Vec3 destination = findSafeGroundNear(overworld, player, respawn.above(), 3);
                if (destination != null) {
                    teleport(player, overworld, destination);
                    return;
                }
            }
            BlockPos spawn = overworld.getSharedSpawnPos();
            Vec3 destination = findSafeGroundNear(overworld, player, spawn, 10);
            if (destination != null) teleport(player, overworld, destination);
            return;
        }
        if (player.level().dimension().equals(Level.NETHER) && player.level() instanceof ServerLevel nether) {
            Optional<BlockPos> portal = nether.getPoiManager().findClosest(
                    holder -> holder.is(PoiTypes.NETHER_PORTAL),
                    player.blockPosition(),
                    PORTAL_SEARCH_RADIUS,
                    PoiManager.Occupancy.ANY
            );
            if (portal.isEmpty()) return;
            Vec3 destination = findSafeGroundNear(nether, player, portal.get(), PORTAL_LANDING_RADIUS);
            if (destination != null) teleport(player, nether, destination);
        }
    }

    private static void recall(SpellExecutionContext context, Entity target) {
        TeleportMarkSavedData data = context.level().getServer().overworld().getDataStorage()
                .computeIfAbsent(TeleportMarkSavedData.factory(), TeleportMarkSavedData.DATA_NAME);
        TeleportMarkSavedData.Mark mark = data.mark(context.owner().getUUID());
        if (mark == null) return;
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, mark.dimension());
        ServerLevel destinationLevel = context.level().getServer().getLevel(key);
        if (destinationLevel == null) return;
        Vec3 destination = new Vec3(mark.position().getX() + 0.5D, mark.position().getY(), mark.position().getZ() + 0.5D);
        if (!canFit(destinationLevel, target, destination)) return;
        teleport(target, destinationLevel, destination);
    }

    private static Vec3 findSafeGroundNear(ServerLevel level, Entity entity, BlockPos center, int radius) {
        Vec3 best = null;
        double bestDistance = Double.MAX_VALUE;
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    int distanceSqr = dx * dx + dy * dy + dz * dz;
                    if (distanceSqr > radius * radius) continue;
                    BlockPos feet = center.offset(dx, dy, dz);
                    if (!level.isInWorldBounds(feet) || !level.getBlockState(feet).isAir() || !hasGround(level, feet.below())) continue;
                    Vec3 destination = new Vec3(feet.getX() + 0.5D, feet.getY(), feet.getZ() + 0.5D);
                    if (!canFit(level, entity, destination)) continue;
                    if (distanceSqr < bestDistance) {
                        best = destination;
                        bestDistance = distanceSqr;
                    }
                }
            }
        }
        return best;
    }

    private static boolean hasGround(ServerLevel level, BlockPos ground) {
        return !level.getBlockState(ground).getCollisionShape(level, ground).isEmpty();
    }

    private static boolean canFit(ServerLevel level, Entity entity, Vec3 destination) {
        BlockPos feet = BlockPos.containing(destination);
        if (!level.isInWorldBounds(feet)) return false;
        Vec3 delta = destination.subtract(entity.position());
        AABB moved = entity.getBoundingBox().move(delta);
        return level.noCollision(entity, moved);
    }

    private static void teleport(Entity entity, ServerLevel level, Vec3 destination) {
        entity.stopRiding();
        entity.teleportTo(level, destination.x, destination.y, destination.z, Set.of(), entity.getYRot(), entity.getXRot());
        entity.setDeltaMovement(Vec3.ZERO);
        entity.fallDistance = 0.0F;
    }
}
