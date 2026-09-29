package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side homing navigation for spell missiles.
 *
 * Target acquisition uses the caster's crosshair ray. Flight uses a bounded 3D A* route through
 * projectile-sized collision-free cells. The projectile then follows that route with limited-angle
 * steering so the actual flight path is curved rather than snapping between waypoints.
 */
public final class MissileHomingNavigation {
    private static final double ACQUISITION_RADIUS = 4.0D;
    private static final int REPLAN_INTERVAL_TICKS = 5;
    private static final double TARGET_REPLAN_DISTANCE_SQR = 0.75D * 0.75D;
    private static final int MAX_VISITED_NODES = 3072;
    private static final double NODE_CLEARANCE = 0.035D;
    private static final double ROUTE_REACHED_DISTANCE_SQR = 0.55D * 0.55D;
    private static final double GOAL_CONNECTION_DISTANCE = 2.25D;
    private static final double LOOK_AHEAD_DISTANCE = 1.75D;
    private static final double MAX_TURN_RADIANS = Math.toRadians(18.0D);
    private static final double HOMING_SPEED = 1.5D;
    private static final int MAX_FAILED_REPLANS = 3;

    private Entity target;
    private UUID targetId;
    private List<Vec3> route = List.of();
    private int routeIndex;
    private Vec3 lastTargetPoint;
    private int replanCooldown;
    private int failedReplans;

    public void setTarget(Entity target) {
        this.target = target;
        this.targetId = target == null ? null : target.getUUID();
        this.route = List.of();
        this.routeIndex = 0;
        this.lastTargetPoint = null;
        this.replanCooldown = 0;
        this.failedReplans = 0;
    }

    public boolean hasTarget() {
        return targetId != null;
    }

    public boolean isTarget(Entity entity) {
        return entity != null && targetId != null && targetId.equals(entity.getUUID());
    }

    public void clearTarget() {
        setTarget(null);
    }

    public void forceReplan() {
        replanCooldown = 0;
        route = List.of();
        routeIndex = 0;
    }

    /** Returns true while Homing still owns steering. */
    public boolean tick(SpellMissile missile, double remainingRange) {
        if (!(missile.level() instanceof ServerLevel level) || targetId == null) return false;

        if (!isValidTarget(level, target)) {
            target = level.getEntity(targetId);
        }
        if (!isValidTarget(level, target)) {
            clearTarget();
            return false;
        }

        Vec3 targetPoint = target.getBoundingBox().getCenter();
        boolean targetMoved = lastTargetPoint == null || lastTargetPoint.distanceToSqr(targetPoint) >= TARGET_REPLAN_DISTANCE_SQR;
        boolean routeInvalid = routeIndex >= route.size() || !routeStillUsable(level, missile);
        if (replanCooldown > 0) replanCooldown--;

        if (routeInvalid || (targetMoved && replanCooldown <= 0)) {
            List<Vec3> planned = findRoute(level, missile, targetPoint, remainingRange);
            if (planned.isEmpty()) {
                failedReplans++;
                replanCooldown = REPLAN_INTERVAL_TICKS;
                if (failedReplans >= MAX_FAILED_REPLANS) {
                    clearTarget();
                    return false;
                }
            } else {
                route = planned;
                routeIndex = 0;
                lastTargetPoint = targetPoint;
                replanCooldown = REPLAN_INTERVAL_TICKS;
                failedReplans = 0;
            }
        }

        if (route.isEmpty() || routeIndex >= route.size()) return hasTarget();

        Vec3 position = missile.position();
        while (routeIndex < route.size() && position.distanceToSqr(route.get(routeIndex)) <= ROUTE_REACHED_DISTANCE_SQR) {
            routeIndex++;
        }
        if (routeIndex >= route.size()) return hasTarget();

        /*
         * Homing has a deliberate launch commitment. The projectile first preserves its
         * original heading, then progressively gains steering authority. This produces
         * a visible curved pursuit path instead of an immediate turn toward the route.
         */
        Vec3 current = missile.getDeltaMovement();
        if (distanceSinceAcquisition < FORWARD_COMMIT_DISTANCE) {
            if (current.lengthSqr() > 1.0E-8D) missile.setDeltaMovement(current.normalize().scale(HOMING_SPEED));
            return true;
        }

        Vec3 aim = lookAheadPoint(position);
        Vec3 desired = aim.subtract(position);
        if (desired.lengthSqr() <= 1.0E-8D) return hasTarget();
        desired = desired.normalize();

        Vec3 currentDirection = current.lengthSqr() > 1.0E-8D ? current.normalize() : desired;
        double rampDistance = distanceSinceAcquisition - FORWARD_COMMIT_DISTANCE;
        double t = Math.max(0.0D, Math.min(1.0D, rampDistance / TURN_RAMP_DISTANCE));
        double eased = t * t * (3.0D - 2.0D * t);
        double turnLimit = MIN_TURN_RADIANS + (MAX_TURN_RADIANS - MIN_TURN_RADIANS) * eased;
        Vec3 steered = rotateToward(currentDirection, desired, turnLimit);
        missile.setDeltaMovement(steered.scale(HOMING_SPEED));
        return true;
    }

    private Vec3 lookAheadPoint(Vec3 position) {
        if (routeIndex >= route.size()) return position;
        double remaining = LOOK_AHEAD_DISTANCE;
        Vec3 cursor = position;
        int index = routeIndex;
        while (index < route.size()) {
            Vec3 next = route.get(index);
            double segment = cursor.distanceTo(next);
            if (segment >= remaining && segment > 1.0E-8D) {
                return cursor.add(next.subtract(cursor).scale(remaining / segment));
            }
            remaining -= segment;
            cursor = next;
            index++;
        }
        return route.get(route.size() - 1);
    }

    private boolean routeStillUsable(ServerLevel level, SpellMissile missile) {
        if (routeIndex >= route.size()) return false;
        Vec3 next = route.get(routeIndex);
        return sweptFree(level, missile, missile.position(), next);
    }

    private static boolean isValidTarget(ServerLevel level, Entity entity) {
        return entity instanceof LivingEntity living
                && living.isAlive()
                && !entity.isRemoved()
                && entity.level() == level
                && entity.isPickable()
                && !entity.isSpectator();
    }

    public static List<Entity> acquireTargets(ServerLevel level, Entity owner, int[] plan, int row, double range) {
        if (owner == null || range <= 0.0D) return List.of();

        Vec3 start = owner.getEyePosition();
        Vec3 look = owner.getLookAngle();
        if (look.lengthSqr() <= 1.0E-8D) return List.of();
        look = look.normalize();
        Vec3 requestedEnd = start.add(look.scale(range));
        BlockHitResult blockHit = level.clip(new ClipContext(start, requestedEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        Vec3 end = blockHit.getType() == HitResult.Type.MISS ? requestedEnd : blockHit.getLocation();

        AABB search = new AABB(start, end).inflate(ACQUISITION_RADIUS);
        List<Candidate> candidates = new ArrayList<>();
        for (Entity entity : level.getEntities(owner, search, e -> validAcquisitionTarget(owner, plan, row, e))) {
            Vec3 center = entity.getBoundingBox().getCenter();
            Projection projection = projectToSegment(center, start, end);
            double allowance = ACQUISITION_RADIUS + Math.max(entity.getBbWidth(), entity.getBbHeight()) * 0.5D;
            if (projection.distanceSqr() > allowance * allowance) continue;
            candidates.add(new Candidate(entity, projection.distanceSqr(), start.distanceToSqr(projection.point())));
        }

        candidates.sort(Comparator
                .comparingDouble(Candidate::distanceToRaySqr)
                .thenComparingDouble(Candidate::distanceAlongRaySqr));
        return candidates.stream().map(Candidate::entity).toList();
    }

    private static boolean validAcquisitionTarget(Entity owner, int[] plan, int row, Entity entity) {
        return entity != owner
                && entity instanceof LivingEntity living
                && living.isAlive()
                && !entity.isRemoved()
                && entity.isPickable()
                && !entity.isSpectator()
                && SpellComponents.acceptsContinuationTarget(plan, row, entity);
    }

    private static Projection projectToSegment(Vec3 point, Vec3 start, Vec3 end) {
        Vec3 segment = end.subtract(start);
        double lengthSqr = segment.lengthSqr();
        if (lengthSqr <= 1.0E-12D) return new Projection(start, point.distanceToSqr(start));
        double t = point.subtract(start).dot(segment) / lengthSqr;
        t = Math.max(0.0D, Math.min(1.0D, t));
        Vec3 projected = start.add(segment.scale(t));
        return new Projection(projected, point.distanceToSqr(projected));
    }

    private static List<Vec3> findRoute(ServerLevel level, SpellMissile missile, Vec3 targetPoint, double remainingRange) {
        Vec3 start = missile.position();
        if (sweptFree(level, missile, start, targetPoint)) return List.of(targetPoint);

        BlockPos startCell = BlockPos.containing(start);
        double maximumPathCost = Math.max(4.0D, remainingRange + 1.5D);
        PriorityQueue<SearchNode> open = new PriorityQueue<>(Comparator.comparingDouble(SearchNode::f));
        Map<Long, Double> bestCost = new HashMap<>();
        Set<Long> closed = new HashSet<>();
        Map<Long, Long> parent = new HashMap<>();

        double startHeuristic = cellCenter(startCell).distanceTo(targetPoint);
        open.add(new SearchNode(startCell, 0.0D, startHeuristic));
        bestCost.put(startCell.asLong(), 0.0D);

        BlockPos reached = null;
        int visited = 0;
        while (!open.isEmpty() && visited < MAX_VISITED_NODES) {
            SearchNode current = open.poll();
            long currentKey = current.pos().asLong();
            if (!closed.add(currentKey)) continue;
            visited++;

            Vec3 currentCenter = cellCenter(current.pos());
            if (currentCenter.distanceTo(targetPoint) <= GOAL_CONNECTION_DISTANCE
                    && sweptFree(level, missile, currentCenter, targetPoint)) {
                reached = current.pos();
                break;
            }

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos next = current.pos().offset(dx, dy, dz);
                        if (!level.isInWorldBounds(next)) continue;
                        long nextKey = next.asLong();
                        if (closed.contains(nextKey)) continue;

                        Vec3 nextCenter = cellCenter(next);
                        double stepCost = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        double newCost = current.g() + stepCost;
                        if (newCost > maximumPathCost) continue;
                        if (!cellFree(level, missile, nextCenter)) continue;
                        if (!sweptFree(level, missile, currentCenter, nextCenter)) continue;

                        double known = bestCost.getOrDefault(nextKey, Double.POSITIVE_INFINITY);
                        if (newCost + 1.0E-6D >= known) continue;
                        bestCost.put(nextKey, newCost);
                        parent.put(nextKey, currentKey);
                        double heuristic = nextCenter.distanceTo(targetPoint);
                        open.add(new SearchNode(next, newCost, newCost + heuristic));
                    }
                }
            }
        }

        if (reached == null) return List.of();

        List<Vec3> reversed = new ArrayList<>();
        long key = reached.asLong();
        long startKey = startCell.asLong();
        reversed.add(cellCenter(reached));
        while (key != startKey) {
            Long previous = parent.get(key);
            if (previous == null) return List.of();
            key = previous;
            if (key != startKey) reversed.add(cellCenter(BlockPos.of(key)));
        }

        List<Vec3> raw = new ArrayList<>(reversed.size() + 1);
        for (int i = reversed.size() - 1; i >= 0; i--) raw.add(reversed.get(i));
        raw.add(targetPoint);
        return simplify(level, missile, start, raw);
    }

    private static List<Vec3> simplify(ServerLevel level, SpellMissile missile, Vec3 start, List<Vec3> raw) {
        if (raw.isEmpty()) return raw;
        List<Vec3> simplified = new ArrayList<>();
        Vec3 anchor = start;
        int index = 0;
        while (index < raw.size()) {
            int furthest = index;
            for (int candidate = raw.size() - 1; candidate >= index; candidate--) {
                if (sweptFree(level, missile, anchor, raw.get(candidate))) {
                    furthest = candidate;
                    break;
                }
            }
            Vec3 point = raw.get(furthest);
            simplified.add(point);
            anchor = point;
            index = furthest + 1;
        }
        return simplified;
    }

    private static boolean cellFree(ServerLevel level, SpellMissile missile, Vec3 center) {
        AABB moved = missile.getBoundingBox().move(center.subtract(missile.position())).inflate(NODE_CLEARANCE);
        return level.noBlockCollision(missile, moved);
    }

    private static boolean sweptFree(ServerLevel level, SpellMissile missile, Vec3 start, Vec3 end) {
        Vec3 delta = end.subtract(start);
        double length = delta.length();
        if (length <= 1.0E-8D) return cellFree(level, missile, start);
        int steps = Math.max(1, (int) Math.ceil(length / 0.35D));
        for (int i = 1; i <= steps; i++) {
            Vec3 sample = start.add(delta.scale(i / (double) steps));
            if (!cellFree(level, missile, sample)) return false;
        }
        return true;
    }

    private static Vec3 cellCenter(BlockPos pos) {
        return new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    private static Vec3 rotateToward(Vec3 current, Vec3 desired, double maximumAngle) {
        double dot = Math.max(-1.0D, Math.min(1.0D, current.dot(desired)));
        double angle = Math.acos(dot);
        if (angle <= maximumAngle || angle <= 1.0E-6D) return desired;
        double t = maximumAngle / angle;
        Vec3 blended = current.scale(1.0D - t).add(desired.scale(t));
        return blended.lengthSqr() > 1.0E-8D ? blended.normalize() : desired;
    }

    private record Candidate(Entity entity, double distanceToRaySqr, double distanceAlongRaySqr) {}
    private record Projection(Vec3 point, double distanceSqr) {}
    private record SearchNode(BlockPos pos, double g, double f) {}
}
