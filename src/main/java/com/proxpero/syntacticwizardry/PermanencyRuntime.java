package com.proxpero.syntacticwizardry;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Executes the Effect/Modifier program stored by an active Permanency ritual. */
public final class PermanencyRuntime {
    private static final Vec3 WORLD_UP = new Vec3(0.0D, 1.0D, 0.0D);

    public static final class State {
        private final Map<Integer, Set<UUID>> createOnceEntities = new HashMap<>();
        private final Set<Integer> createOnceBlocks = new HashSet<>();

        private void clear() {
            createOnceEntities.clear();
            createOnceBlocks.clear();
        }
    }

    private PermanencyRuntime() {}

    public static void reset(PermanencyRitualData.Entry entry) {
        if (entry != null) entry.runtimeState().clear();
    }

    public static void tick(ServerLevel level, PermanencyRitualData.Entry entry) {
        if (level == null || entry == null) return;

        int[] plan = entry.planInternal();
        int[] settings = entry.settingsInternal();
        List<BlockPos> voxels = areaVoxels(entry);
        if (voxels.isEmpty()) return;

        Vec3 origin = areaCenter(entry);
        Vec3 direction = direction(entry.yaw(), entry.pitch());
        ShapeResolution area = new ShapeResolution(
                origin,
                direction,
                WORLD_UP,
                null,
                voxels,
                null,
                VectorPolicy.FORWARD);

        Entity owner = owner(level, entry.ownerId());
        List<Entity> allTargets = ResolvedTargets.entities(level, area);
        long gameTime = level.getGameTime();

        for (int cell = 0; cell < SpellPresentation.CELLS; cell++) {
            SpellComponentDefinition definition =
                    SpellComponents.byType(SpellPresentation.typeAt(plan, cell));
            if (definition == null || !SpellComponents.isEffect(definition)) continue;

            int row = cell / SpellPresentation.COLS;
            int col = cell % SpellPresentation.COLS;
            int durationTicks = SpellComponents.attachedDurationTicks(plan, settings, row, col);
            boolean blockInteraction =
                    SpellComponents.hasAttachedModifier(plan, row, col, SpellComponents.TYPE_BLOCK_INTERACTION);
            boolean excludeCaster =
                    SpellComponents.hasAttachedModifier(plan, row, col, SpellComponents.TYPE_EXCLUDE_CASTER);
            EffectReplayPolicy policy = definition.replayPolicy();

            boolean periodic = switch (policy) {
                case STATEFUL -> gameTime % 20L == 0L;
                case INSTANT, TICK, DELTA_VOXELS, CREATE_ONCE -> true;
            };
            if (!periodic) continue;

            int effectiveDuration = policy == EffectReplayPolicy.STATEFUL
                    ? Math.max(durationTicks, 40)
                    : durationTicks;

            applyToEntities(
                    level, entry, definition, cell, row, plan, settings,
                    area, allTargets, owner, direction,
                    effectiveDuration, blockInteraction, excludeCaster, policy);

            if (appliesToBlocks(cell, plan, settings, blockInteraction)) {
                applyToBlocks(
                        level, entry, definition, cell, row, plan, settings,
                        area, owner, direction,
                        effectiveDuration, blockInteraction, excludeCaster, policy);
            }
        }
    }

    private static void applyToEntities(
            ServerLevel level,
            PermanencyRitualData.Entry entry,
            SpellComponentDefinition definition,
            int cell,
            int row,
            int[] plan,
            int[] settings,
            ShapeResolution area,
            List<Entity> allTargets,
            Entity owner,
            Vec3 direction,
            int durationTicks,
            boolean blockInteraction,
            boolean excludeCaster,
            EffectReplayPolicy policy) {

        Set<UUID> currentlyInside = policy == EffectReplayPolicy.CREATE_ONCE
                ? new HashSet<>()
                : null;
        Set<UUID> alreadySeen = policy == EffectReplayPolicy.CREATE_ONCE
                ? entry.runtimeState().createOnceEntities.computeIfAbsent(cell, ignored -> new HashSet<>())
                : null;

        for (Entity target : allTargets) {
            if (target == null || target.isRemoved() || !definition.acceptsDirectEntity(target)) continue;
            if (excludeCaster && owner != null && target == owner) continue;

            UUID targetId = target.getUUID();
            if (currentlyInside != null) {
                currentlyInside.add(targetId);
                if (alreadySeen.contains(targetId)) continue;
                alreadySeen.add(targetId);
            }

            if (!payEffect(level, entry.center(), plan, settings, cell)) continue;

            ShapeResolution direct = new ShapeResolution(
                    area.origin(),
                    direction,
                    WORLD_UP,
                    null,
                    List.of(),
                    target,
                    VectorPolicy.FORWARD);
            SpellExecutionContext context = new SpellExecutionContext(
                    level,
                    owner,
                    plan,
                    settings,
                    row,
                    cell,
                    direct,
                    false,
                    direction,
                    durationTicks,
                    blockInteraction,
                    excludeCaster);
            definition.execute(context);
        }

        if (alreadySeen != null) alreadySeen.retainAll(currentlyInside);
    }

    private static void applyToBlocks(
            ServerLevel level,
            PermanencyRitualData.Entry entry,
            SpellComponentDefinition definition,
            int cell,
            int row,
            int[] plan,
            int[] settings,
            ShapeResolution area,
            Entity owner,
            Vec3 direction,
            int durationTicks,
            boolean blockInteraction,
            boolean excludeCaster,
            EffectReplayPolicy policy) {

        if (policy == EffectReplayPolicy.CREATE_ONCE
                && !entry.runtimeState().createOnceBlocks.add(cell)) {
            return;
        }

        if (!payEffect(level, entry.center(), plan, settings, cell)) return;

        SpellExecutionContext context = new SpellExecutionContext(
                level,
                owner,
                plan,
                settings,
                row,
                cell,
                area,
                false,
                direction,
                durationTicks,
                blockInteraction,
                excludeCaster);
        definition.execute(context);
    }

    private static boolean payEffect(
            ServerLevel level,
            BlockPos ritualCenter,
            int[] plan,
            int[] settings,
            int cell) {
        double cost = Math.max(0.0D, SpellManaCost.componentCost(plan, settings, cell));
        return RitualManaSupport.tryConsumeUpkeep(level, ritualCenter, cost);
    }

    private static boolean appliesToBlocks(
            int cell,
            int[] plan,
            int[] settings,
            boolean blockInteraction) {
        int type = SpellPresentation.typeAt(plan, cell);
        return switch (type) {
            case SpellPresentation.TYPE_DIG,
                    SpellComponents.TYPE_RESTORE,
                    SpellComponents.TYPE_TEMPORARY_BLOCK,
                    SpellComponents.TYPE_LIGHT,
                    SpellComponents.TYPE_MARK -> true;
            case SpellComponents.TYPE_MOVE ->
                    SpellPresentation.targetTypeAt(settings, cell) != SpellPresentation.TARGET_ENTITIES;
            case SpellComponents.TYPE_GRAVITY -> blockInteraction;
            default -> false;
        };
    }

    private static Entity owner(ServerLevel level, UUID ownerId) {
        if (ownerId == null || level.getServer() == null) return null;
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(ownerId);
        return player != null && !player.isRemoved() ? player : null;
    }

    private static Vec3 direction(float yaw, float pitch) {
        double y = Math.toRadians(yaw);
        double p = Math.toRadians(pitch);
        double cp = Math.cos(p);
        Vec3 result = new Vec3(
                -Math.sin(y) * cp,
                -Math.sin(p),
                Math.cos(y) * cp);
        return result.lengthSqr() > 1.0E-8D
                ? result.normalize()
                : new Vec3(0.0D, 0.0D, 1.0D);
    }

    private static Vec3 areaCenter(PermanencyRitualData.Entry entry) {
        return new Vec3(
                (entry.minX() + entry.maxX() + 1.0D) * 0.5D,
                (entry.minY() + entry.maxY() + 1.0D) * 0.5D,
                (entry.minZ() + entry.maxZ() + 1.0D) * 0.5D);
    }

    private static List<BlockPos> areaVoxels(PermanencyRitualData.Entry entry) {
        List<BlockPos> result = new ArrayList<>();
        int minX = entry.minX();
        int minY = entry.minY();
        int minZ = entry.minZ();
        int maxX = entry.maxX();
        int maxY = entry.maxY();
        int maxZ = entry.maxZ();

        double cx = (minX + maxX + 1.0D) * 0.5D;
        double cy = (minY + maxY + 1.0D) * 0.5D;
        double cz = (minZ + maxZ + 1.0D) * 0.5D;
        double rx = Math.max(0.5D, (maxX - minX + 1) * 0.5D);
        double ry = Math.max(0.5D, (maxY - minY + 1) * 0.5D);
        double rz = Math.max(0.5D, (maxZ - minZ + 1) * 0.5D);

        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = minX; x <= maxX; x++) {
                    if (entry.shape() == ProtectionAreaShape.BOX) {
                        result.add(new BlockPos(x, y, z));
                        continue;
                    }

                    double dx = (x + 0.5D - cx) / rx;
                    double dz = (z + 0.5D - cz) / rz;
                    if (entry.shape() == ProtectionAreaShape.CYLINDER) {
                        if (dx * dx + dz * dz <= 1.0D + 1.0E-8D) {
                            result.add(new BlockPos(x, y, z));
                        }
                        continue;
                    }

                    double dy = (y + 0.5D - cy) / ry;
                    if (dx * dx + dy * dy + dz * dz <= 1.0D + 1.0E-8D) {
                        result.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return List.copyOf(result);
    }
}
