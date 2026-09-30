package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/** Resolves a point relative to the parent Shape frame, stopping at the first collision. */
public final class RelativeShape {
    private static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D);

    private RelativeShape() {}

    public static ShapeResolution resolve(SpellExecutionContext context) {
        Entity owner = context.owner();
        ShapeResolution parent = context.parent();
        Vec3 yaw = SpellExecutor.normalizeYaw(context.castYaw());
        Vec3 inheritedFacing = normalize(parent.direction(), yaw);
        Vec3 inheritedUp = normalize(parent.up(), WORLD_UP);
        Vec3 outputFacing = normalize(yaw, inheritedFacing);
        Vec3 right = new Vec3(yaw.z, 0.0D, -yaw.x).normalize();
        int directionId = context.relativeDirection();
        Vec3 offsetDirection = switch (directionId) {
            case SpellPresentation.RELATIVE_BACK -> inheritedFacing.scale(-1.0D);
            case SpellPresentation.RELATIVE_LEFT -> right.scale(-1.0D);
            case SpellPresentation.RELATIVE_RIGHT -> right;
            case SpellPresentation.RELATIVE_UP -> inheritedUp;
            case SpellPresentation.RELATIVE_DOWN -> inheritedUp.scale(-1.0D);
            default -> inheritedFacing;
        };
        offsetDirection = normalize(offsetDirection, inheritedFacing);

        int distance = Math.max(SpellPresentation.DISTANCE_MIN, Math.min(SpellPresentation.DISTANCE_MAX, context.distance()));

        Vec3 start = parent.origin();
        Vec3 requestedEnd = start.add(offsetDirection.scale(distance));

        ServerLevel level = context.level();
        BlockHitResult blockHit = level.clip(new ClipContext(
                start, requestedEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        boolean hasBlock = blockHit.getType() != HitResult.Type.MISS;
        double blockDistanceSqr = hasBlock ? start.distanceToSqr(blockHit.getLocation()) : start.distanceToSqr(requestedEnd);

        EntitySelection entityHit = findEntity(context, start, requestedEnd, blockDistanceSqr);
        if (entityHit != null && (!hasBlock || entityHit.distanceSqr() < blockDistanceSqr)) {
            return new ShapeResolution(
                    entityHit.location(),
                    outputFacing,
                    WORLD_UP,
                    null,
                    List.of(),
                    entityHit.entity(),
                    VectorPolicy.FORWARD
            );
        }

        if (hasBlock) {
            return new ShapeResolution(
                    blockHit.getLocation(),
                    outputFacing,
                    WORLD_UP,
                    null,
                    List.of(blockHit.getBlockPos().immutable()),
                    null,
                    VectorPolicy.FORWARD
            );
        }

        BlockPos targetBlock = new BlockPos(
                (int) Math.floor(requestedEnd.x),
                (int) Math.floor(requestedEnd.y),
                (int) Math.floor(requestedEnd.z)
        );
        Vec3 targetOrigin = new Vec3(
                targetBlock.getX() + 0.5D,
                targetBlock.getY() + 0.5D,
                targetBlock.getZ() + 0.5D
        );
        return new ShapeResolution(
                targetOrigin,
                outputFacing,
                WORLD_UP,
                null,
                List.of(targetBlock.immutable()),
                null,
                VectorPolicy.FORWARD
        );
    }

    public static Vec3 previewLocation(ServerLevel level, Entity owner, int directionId, int distance) {
        Vec3 start = new Vec3(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
        Vec3 look = normalize(owner.getLookAngle(), new Vec3(0.0D, 0.0D, 1.0D));
        Vec3 yaw = SpellExecutor.normalizeYaw(look);
        Vec3 right = new Vec3(yaw.z, 0.0D, -yaw.x).normalize();
        Vec3 offsetDirection = switch (directionId) {
            case SpellPresentation.RELATIVE_BACK -> look.scale(-1.0D);
            case SpellPresentation.RELATIVE_LEFT -> right.scale(-1.0D);
            case SpellPresentation.RELATIVE_RIGHT -> right;
            case SpellPresentation.RELATIVE_UP -> WORLD_UP;
            case SpellPresentation.RELATIVE_DOWN -> WORLD_UP.scale(-1.0D);
            default -> look;
        };
        offsetDirection = normalize(offsetDirection, look);

        int resolvedDistance = Math.max(SpellPresentation.DISTANCE_MIN, Math.min(SpellPresentation.DISTANCE_MAX, distance));
        Vec3 requestedEnd = start.add(offsetDirection.scale(resolvedDistance));
        BlockHitResult blockHit = level.clip(new ClipContext(
                start, requestedEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        boolean hasBlock = blockHit.getType() != HitResult.Type.MISS;
        double blockDistanceSqr = hasBlock ? start.distanceToSqr(blockHit.getLocation()) : start.distanceToSqr(requestedEnd);

        EntitySelection entityHit = findEntity(level, owner, start, requestedEnd, blockDistanceSqr);
        if (entityHit != null && (!hasBlock || entityHit.distanceSqr() < blockDistanceSqr)) return entityHit.location();
        if (hasBlock) return blockHit.getLocation();
        return requestedEnd;
    }

    private static EntitySelection findEntity(SpellExecutionContext context, Vec3 start, Vec3 end, double maximumDistanceSqr) {
        return findEntity(context.level(), context.owner(), start, end, maximumDistanceSqr);
    }

    private static EntitySelection findEntity(ServerLevel level, Entity owner, Vec3 start, Vec3 end, double maximumDistanceSqr) {
        AABB search = new AABB(start, end).inflate(1.0D);
        EntitySelection best = null;
        for (Entity entity : level.getEntities(
                owner,
                search,
                e -> e != owner && !e.isRemoved() && e.isPickable() && !e.isSpectator())) {
            AABB bounds = entity.getBoundingBox().inflate(entity.getPickRadius());
            Vec3 location;
            if (bounds.contains(start)) {
                location = start;
            } else {
                Optional<Vec3> clipped = bounds.clip(start, end);
                if (clipped.isEmpty()) continue;
                location = clipped.get();
            }
            double distanceSqr = start.distanceToSqr(location);
            if (distanceSqr > maximumDistanceSqr) continue;
            if (best == null || distanceSqr < best.distanceSqr()) {
                best = new EntitySelection(entity, location, distanceSqr);
            }
        }
        return best;
    }

    private static Vec3 normalize(Vec3 value, Vec3 fallback) {
        return value != null && value.lengthSqr() > 1.0E-12D ? value.normalize() : fallback;
    }

    private record EntitySelection(Entity entity, Vec3 location, double distanceSqr) {}
}
