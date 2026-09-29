package com.proxpero.syntacticwizardry;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class SpellMissile extends Snowball {
    private static final double BASE_RANGE = 16.0D;
    private static final double RANGE_BLOCKS_PER_VALUE = 2.0D;
    private static final double MIN_RANGE = 1.0D;
    private static final double SPLIT_OFFSET = 0.65D;
    private static final double CHAIN_SEARCH_RANGE = 10.0D;
    private static final double CHAIN_SEARCH_RANGE_SQR = CHAIN_SEARCH_RANGE * CHAIN_SEARCH_RANGE;

    private double remainingRange = BASE_RANGE;
    private Vec3 previousPosition = Vec3.ZERO;
    private final Set<UUID> chainHitTargets = new HashSet<>();
    private final Set<UUID> piercedHitTargets = new HashSet<>();
    private final MissileHomingNavigation homingNavigation = new MissileHomingNavigation();

    public SpellMissile(EntityType<? extends SpellMissile> type, Level level) {
        super(type, level);
    }

    public SpellMissile(Level level, LivingEntity owner) {
        this(SyntacticWizardry.SPELL_MISSILE.get(), level);
        setOwner(owner);
        setPos(owner.getX(), owner.getEyeY() - 0.1D, owner.getZ());
        previousPosition = position();
    }

    public static void spawnGroup(SpellExecutionContext context) {
        Vec3 direction = normalizedDirection(context.shapeDirection());
        Vec3 origin = context.parent().origin();
        int ownerCol = context.cell() % SpellPresentation.COLS;
        int extra = SpellComponents.attachedSplitPotence(context.plan(), context.settings(), context.row(), ownerCol);
        boolean homing = SpellComponents.hasAttachedModifier(context.plan(), context.row(), ownerCol, SpellComponents.TYPE_HOMING);
        double range = rangeBudget(context.plan(), context.settings(), context.row(), context.cell());
        java.util.List<Entity> homingTargets = homing
                ? MissileHomingNavigation.acquireTargets(context.level(), context.owner(), context.plan(), context.row(), range)
                : java.util.List.of();

        spawnPrepared(context, origin, direction, homingTargetForIndex(homingTargets, 0));
        if (extra <= 0) return;

        int pattern = SpellComponents.attachedSplitPattern(context.plan(), context.settings(), context.row(), ownerCol);
        Basis basis = basis(direction);
        for (int i = 0; i < extra; i++) {
            Vec3 offset;
            if (pattern == SpellPresentation.SPLIT_LINEAR) {
                int ordinal = i / 2 + 1;
                double sign = (i & 1) == 0 ? 1.0D : -1.0D;
                offset = basis.right.scale(SPLIT_OFFSET * ordinal * sign);
            } else {
                double angle = (Math.PI * 2.0D * i) / Math.max(1, extra);
                offset = basis.right.scale(Math.cos(angle) * SPLIT_OFFSET)
                        .add(basis.up.scale(Math.sin(angle) * SPLIT_OFFSET));
            }
            spawnPrepared(context, origin.add(offset), direction, homingTargetForIndex(homingTargets, i + 1));
        }
    }

    private static Entity homingTargetForIndex(java.util.List<Entity> targets, int index) {
        if (targets == null || targets.isEmpty()) return null;
        return targets.get(index < targets.size() ? index : 0);
    }

    private static void spawnPrepared(SpellExecutionContext context, Vec3 origin, Vec3 direction, Entity homingTarget) {
        SpellMissile missile = new SpellMissile(SyntacticWizardry.SPELL_MISSILE.get(), context.level());
        missile.prepare(
                context.owner(), origin, direction, context.castYaw(), context.plan(), context.settings(),
                context.row(), context.cell(), context.activeDurationTicks(), context.blockInteraction()
        );
        if (homingTarget != null) missile.homingNavigation.setTarget(homingTarget);
        context.level().addFreshEntity(missile);
    }

    public void prepare(Entity owner, Vec3 origin, Vec3 direction, Vec3 castYaw, int[] plan, int[] settings,
                        int row, int cell, int activeDurationTicks, boolean blockInteraction) {
        setOwner(owner);
        setPos(origin.x, origin.y, origin.z);
        setItem(SpellPresentation.projectileStack(plan, settings, row, cell, castYaw, activeDurationTicks, blockInteraction));
        previousPosition = position();
        remainingRange = rangeBudget(plan, settings, row, cell);
        Vec3 dir = normalizedDirection(direction);
        shoot(dir.x, dir.y, dir.z, 1.5F, 0.0F);
    }

    private static double rangeBudget(int[] plan, int[] settings, int row, int cell) {
        int rangeValue = SpellComponents.attachedRangeValue(plan, settings, row, cell % SpellPresentation.COLS);
        return Math.max(MIN_RANGE, BASE_RANGE + RANGE_BLOCKS_PER_VALUE * rangeValue);
    }

    public int presentationStyle() {
        return SpellPresentation.readStyle(getItem());
    }

    public int presentationVisual() {
        return SpellPresentation.readVisual(getItem());
    }

    private boolean isChainProjectile() {
        int[] plan = SpellPresentation.readPlan(getItem());
        return SpellPresentation.typeAt(plan, SpellPresentation.readCell(getItem())) == SpellComponents.TYPE_CHAIN;
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity)) return false;
        if (entity == getOwner()) return false;
        UUID id = entity.getUUID();
        if (piercedHitTargets.contains(id)) return false;
        return !isChainProjectile() || !chainHitTargets.contains(id);
    }

    private Vec3 tickStartPosition = Vec3.ZERO;
    private Vec3 continuationStartPosition;
    private double travelBeforeContinuation;

    @Override
    public void tick() {
        Vec3 before = position();
        tickStartPosition = before;
        continuationStartPosition = null;
        travelBeforeContinuation = 0.0D;

        if (!level().isClientSide && homingNavigation.hasTarget()) {
            if (homingNavigation.tick(this, remainingRange)) hasImpulse = true;
        }

        super.tick();

        if (level().isClientSide || isRemoved()) return;

        double travelled;
        if (continuationStartPosition != null) {
            travelled = travelBeforeContinuation + continuationStartPosition.distanceTo(position());
        } else {
            travelled = before.distanceTo(position());
        }

        if (travelled > 0.0D) remainingRange -= travelled;
        previousPosition = position();
        if (remainingRange <= 0.0D) discard();
    }

    @Override
    protected void onHit(HitResult result) {
        int[] plan = SpellPresentation.readPlan(getItem());
        int[] settings = SpellPresentation.readSettings(getItem());
        int row = SpellPresentation.readRow(getItem());
        int cell = SpellPresentation.readCell(getItem());
        int ownerCol = cell % SpellPresentation.COLS;
        boolean ricochet = SpellComponents.hasAttachedModifier(plan, row, ownerCol, SpellComponents.TYPE_RICOCHET);
        boolean piercing = SpellComponents.hasAttachedModifier(plan, row, ownerCol, SpellComponents.TYPE_PIERCING);
        Entity hit = result instanceof EntityHitResult entityHit ? entityHit.getEntity() : null;
        boolean hitHomingTarget = homingNavigation.isTarget(hit);
        Vec3 incomingVelocity = getDeltaMovement();

        /*
         * Traversal modifiers are resolved before the Missile is handed off.
         * This matches the old Arcane Missile order:
         * impact -> traversal decision -> resolution -> continuation.
         */
        boolean continues = false;
        if (result instanceof BlockHitResult blockHit && ricochet && remainingRange > 0.0D) {
            continues = applyRicochet(blockHit, incomingVelocity);
        } else if (hit != null && piercing && remainingRange > 0.0D) {
            piercedHitTargets.add(hit.getUUID());
            if (hitHomingTarget) homingNavigation.clearTarget();
            continues = applyPiercing(result.getLocation(), incomingVelocity);
        }

        /*
         * Client collision is visual only. The server owns spell resolution.
         * The client still applies traversal immediately so the projectile does
         * not visually disappear or travel through the wall before correction.
         */
        if (level().isClientSide) return;
        if (!(level() instanceof ServerLevel server)) return;

        Vec3 castYaw = SpellPresentation.readCastYaw(getItem());
        boolean chain = SpellPresentation.typeAt(plan, cell) == SpellComponents.TYPE_CHAIN;

        ShapeResolution resolved;
        if (result instanceof BlockHitResult blockHit) {
            int impactDirection = SpellPresentation.impactDirectionAt(settings, cell);
            resolved = ShapeResolution.projectileBlock(blockHit.getBlockPos(), blockHit.getDirection(), impactDirection);
        } else {
            resolved = ShapeResolution.impact(result.getLocation(), incomingVelocity, hit);
        }

        int visualDuration = VisualDurationSupport.durationTicksForShape(plan, settings, row);
        if (visualDuration > 0) {
            Vec3 facing = incomingVelocity.lengthSqr() > 1.0E-8D ? incomingVelocity.normalize() : resolved.direction();
            PointVisualEntity visualEntity = new PointVisualEntity(server, resolved.origin(), facing,
                    presentationStyle(), presentationVisual(), visualDuration);
            server.addFreshEntity(visualEntity);
        }

        SpellExecutor.continueFrom(server, getOwner(), plan, settings, row, resolved, castYaw,
                SpellPresentation.readScopeDuration(getItem()), SpellPresentation.readScopeBlockInteraction(getItem()));

        if (chain && hit != null && hit != getOwner() && SpellComponents.acceptsContinuationTarget(plan, row, hit)) {
            chainHitTargets.add(hit.getUUID());
            int[] reducedSettings = SpellComponents.reducedChainSettings(plan, settings, row);
            if (reducedSettings != null) {
                Entity nextTarget = findNearestChainTarget(server, result.getLocation(), plan, row);
                if (nextTarget != null && spawnChainJump(server, result.getLocation(), nextTarget, plan,
                        reducedSettings, row, cell, castYaw)) {
                    discard();
                    return;
                }
            }
        }

        if (continues) return;
        discard();
    }

    private boolean applyRicochet(BlockHitResult hit, Vec3 incomingVelocity) {
        if (incomingVelocity == null || incomingVelocity.lengthSqr() <= 1.0E-8D) return false;

        Direction face = hit.getDirection();
        Vec3 normal = new Vec3(face.getStepX(), face.getStepY(), face.getStepZ());
        Vec3 reflected = incomingVelocity.subtract(normal.scale(2.0D * incomingVelocity.dot(normal)));
        if (reflected.lengthSqr() <= 1.0E-8D) return false;

        double separation = Math.max(0.16D, getBbWidth() * 0.5D + 0.02D);
        Vec3 escaped = hit.getLocation().add(normal.scale(separation));

        travelBeforeContinuation = tickStartPosition.distanceTo(hit.getLocation());
        continuationStartPosition = escaped;

        setPos(escaped.x, escaped.y, escaped.z);
        setDeltaMovement(reflected);
        hasImpulse = true;
        homingNavigation.forceReplan();
        return true;
    }

    private boolean applyPiercing(Vec3 impact, Vec3 incomingVelocity) {
        if (incomingVelocity == null || incomingVelocity.lengthSqr() <= 1.0E-8D) return false;

        Vec3 direction = incomingVelocity.normalize();
        double separation = Math.max(0.16D, getBbWidth() * 0.5D + 0.02D);
        Vec3 escaped = impact.add(direction.scale(separation));

        travelBeforeContinuation = tickStartPosition.distanceTo(impact);
        continuationStartPosition = escaped;

        setPos(escaped.x, escaped.y, escaped.z);
        setDeltaMovement(incomingVelocity);
        hasImpulse = true;
        if (homingNavigation.hasTarget()) homingNavigation.forceReplan();
        return true;
    }

    private Entity findNearestChainTarget(ServerLevel level, Vec3 origin, int[] plan, int row) {
        AABB search = new AABB(origin, origin).inflate(CHAIN_SEARCH_RANGE);
        Entity best = null;
        double bestDistance = CHAIN_SEARCH_RANGE_SQR + 1.0D;
        for (Entity candidate : level.getEntities(this, search,
                entity -> entity != getOwner()
                        && !chainHitTargets.contains(entity.getUUID())
                        && !piercedHitTargets.contains(entity.getUUID())
                        && SpellComponents.acceptsContinuationTarget(plan, row, entity))) {
            Vec3 point = candidate.getBoundingBox().getCenter();
            double distance = origin.distanceToSqr(point);
            if (distance <= CHAIN_SEARCH_RANGE_SQR && distance < bestDistance) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }

    private boolean spawnChainJump(ServerLevel level, Vec3 origin, Entity target, int[] plan, int[] settings,
                                   int row, int cell, Vec3 castYaw) {
        Vec3 targetPoint = target.getBoundingBox().getCenter();
        Vec3 direction = targetPoint.subtract(origin);
        if (direction.lengthSqr() <= 1.0E-8D) return false;
        SpellMissile next = new SpellMissile(SyntacticWizardry.SPELL_MISSILE.get(), level);
        next.prepare(getOwner(), origin, direction, castYaw, plan, settings, row, cell,
                SpellPresentation.readScopeDuration(getItem()), SpellPresentation.readScopeBlockInteraction(getItem()));
        if (SpellComponents.hasAttachedModifier(plan, row, cell % SpellPresentation.COLS, SpellComponents.TYPE_HOMING)) {
            next.homingNavigation.setTarget(target);
        }
        next.chainHitTargets.addAll(chainHitTargets);
        next.piercedHitTargets.addAll(piercedHitTargets);
        return level.addFreshEntity(next);
    }

    private static Vec3 normalizedDirection(Vec3 direction) {
        return direction != null && direction.lengthSqr() > 1.0E-8D
                ? direction.normalize()
                : new Vec3(0.0D, 0.0D, 1.0D);
    }

    private static Basis basis(Vec3 direction) {
        Vec3 reference = Math.abs(direction.y) < 0.99D
                ? new Vec3(0.0D, 1.0D, 0.0D)
                : new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 right = direction.cross(reference).normalize();
        Vec3 up = right.cross(direction).normalize();
        return new Basis(right, up);
    }

    private record Basis(Vec3 right, Vec3 up) {}
}
